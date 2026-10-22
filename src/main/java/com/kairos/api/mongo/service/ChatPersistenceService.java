package com.kairos.api.mongo.service;

import com.kairos.api.mongo.dto.AiRecordResponse;
import com.kairos.api.mongo.dto.request.RuntimeRequests.*;
import com.kairos.api.mongo.exception.ApiException;
import com.kairos.api.mongo.mapper.AiResponseMapper;
import com.kairos.api.mongo.model.AiResource;
import java.util.*;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.*;
import org.springframework.data.mongodb.core.query.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

/** Additive operations for the chatbot, without replacing the existing CRUD contracts. */
@Service
public class ChatPersistenceService {
    private final MongoTemplate mongo;
    private final AiResponseMapper mapper;
    private final boolean writesEnabled;
    private final long leaseSeconds;
    public ChatPersistenceService(MongoTemplate mongo, AiResponseMapper mapper,
            @Value("${app.mongodb.writes-enabled:false}") boolean writesEnabled,
            @Value("${app.chat.lease-seconds:900}") long leaseSeconds) {
        if (leaseSeconds < 30 || leaseSeconds > 3600) throw new IllegalArgumentException("Invalid chat lease duration.");
        this.mongo=mongo; this.mapper=mapper; this.writesEnabled=writesEnabled; this.leaseSeconds=leaseSeconds;
    }
    private void writable(AiResource resource) {
        if (!writesEnabled) throw new ApiException(HttpStatus.FORBIDDEN,"WRITES_DISABLED","Database writes are disabled for this environment.");
        if (!mongo.collectionExists(resource.collection)) throw new ApiException(HttpStatus.SERVICE_UNAVAILABLE,"COLLECTION_UNAVAILABLE","The collection is unavailable.");
    }
    private Criteria owned(int userId, String id) {
        if (userId <= 0) throw ApiException.badRequest("Invalid userId.");
        return Criteria.where("_id").is(AiResourceService.id(id)).and("user_id").is(userId);
    }
    private Document conversation(int userId, String id) {
        var document=mongo.findOne(Query.query(owned(userId,id)),Document.class,"conversations");
        if (document==null) throw ApiException.notFound("Conversation not found for this user.");
        return document;
    }
    public AiRecordResponse find(int userId,String id) { return mapper.response(AiResource.CONVERSATION,conversation(userId,id)); }
    public AiRecordResponse active(int userId) {
        if (userId <= 0) throw ApiException.badRequest("Invalid userId.");
        var query=Query.query(Criteria.where("user_id").is(userId).and("status").is("ACTIVE"))
                .with(Sort.by(Sort.Direction.DESC,"updated_at","_id"));
        var existing=mongo.findOne(query,Document.class,"conversations");
        if (existing!=null) return mapper.response(AiResource.CONVERSATION,existing);
        writable(AiResource.CONVERSATION);
        var now=new Date();
        var document=new Document("_id",new ObjectId()).append("user_id",userId).append("status","ACTIVE")
                .append("messages",List.of()).append("summary",null).append("summarized_count",0)
                .append("created_at",now).append("updated_at",now);
        return mapper.response(AiResource.CONVERSATION,mongo.insert(document,"conversations"));
    }
    private void leaseScope(Query query,String owner) {
        if (owner==null) query.addCriteria(new Criteria().orOperator(
                Criteria.where("runtime_lease_until").exists(false),Criteria.where("runtime_lease_until").lte(new Date())));
        else query.addCriteria(Criteria.where("runtime_lease_owner").is(owner).and("runtime_lease_until").gt(new Date()));
    }
    public AiRecordResponse appendTurn(int userId,String id,Turn turn,String leaseOwner) {
        writable(AiResource.CONVERSATION);
        var userMessageId=turn.turnId()+":USER";
        var aiMessageId=turn.turnId()+":AI";
        var now=new Date();
        var messages=List.of(new Document("message_id",userMessageId).append("sender","USER").append("content",turn.question()).append("timestamp",now),
                new Document("message_id",aiMessageId).append("sender","AI").append("content",turn.answer()).append("timestamp",now));
        var query=Query.query(owned(userId,id).and("status").is("ACTIVE").and("messages.message_id").nin(userMessageId,aiMessageId));
        leaseScope(query,leaseOwner);
        var updated=mongo.findAndModify(query,new Update().push("messages").each(messages.toArray()).set("updated_at",now),
                FindAndModifyOptions.options().returnNew(true),Document.class,"conversations");
        if (updated==null) {
            var current=conversation(userId,id);
            var stored=current.getList("messages",Document.class,List.of());
            boolean duplicate=messages.stream().allMatch(expected->stored.stream().anyMatch(m->
                    Objects.equals(m.get("message_id"),expected.get("message_id")) && Objects.equals(m.get("content"),expected.get("content"))));
            if (!duplicate) throw ApiException.conflict("Turn conflicts with the current conversation.");
            updated=current;
        }
        return mapper.response(AiResource.CONVERSATION,updated);
    }
    public AiRecordResponse summarize(int userId,String id,Summary summary,String leaseOwner) {
        writable(AiResource.CONVERSATION);
        if (summary.previousCount()%2!=0 || summary.summarizedCount()%2!=0 || summary.summarizedCount()<=summary.previousCount())
            throw ApiException.badRequest("Summary markers must advance over complete turns.");
        var condition=new Document("$and",List.of(
                new Document("$eq",List.of(new Document("$ifNull",List.of("$summarized_count",0)),summary.previousCount())),
                new Document("$gte",List.of(new Document("$size",new Document("$ifNull",List.of("$messages",List.of()))),summary.summarizedCount()))));
        var filter=owned(userId,id).and("summary").is(summary.previousSummary()).getCriteriaObject();
        filter.put("$expr",condition);
        var query=new BasicQuery(filter);
        leaseScope(query,leaseOwner);
        var updated=mongo.findAndModify(query,new Update().set("summary",summary.summary()).set("summary_updated_at",new Date())
                        .set("summarized_count",summary.summarizedCount()),
                FindAndModifyOptions.options().returnNew(true),Document.class,"conversations");
        if (updated==null) { conversation(userId,id); throw ApiException.conflict("The summary version changed or the marker exceeds the history."); }
        return mapper.response(AiResource.CONVERSATION,updated);
    }
    public AiRecordResponse appendMemories(int userId,Memories request) {
        if (userId<=0) throw ApiException.badRequest("Invalid userId.");
        writable(AiResource.USER_MEMORY);
        var existing=mongo.findOne(Query.query(Criteria.where("user_id").is(userId)).with(Sort.by("_id")),Document.class,"user_memory");
        // Stable ID only for new runtime records prevents concurrent upserts from creating duplicates.
        var id=existing==null ? new ObjectId("ffffffffffffffff"+String.format("%08x",userId)) : existing.getObjectId("_id");
        var now=new Date();
        var entries=request.memories().stream().map(m->new Document("memory_id",m.memoryId()==null?new ObjectId():AiResourceService.id(m.memoryId()))
                .append("access_scope",m.accessScope()).append("type",m.type()).append("content",m.content()).append("importance",m.importance())
                .append("created_at",m.createdAt()==null?now:Date.from(m.createdAt()))
                .append("updated_at",m.updatedAt()==null?now:Date.from(m.updatedAt()))).toList();
        var query=Query.query(Criteria.where("_id").is(id).and("user_id").is(userId));
        var update=new Update().push("memories").each(entries.toArray()).set("updated_at",now).setOnInsert("user_id",userId);
        Document result;
        try { result=mongo.findAndModify(query,update,FindAndModifyOptions.options().upsert(true).returnNew(true),Document.class,"user_memory"); }
        catch (DuplicateKeyException conflict) {
            result=mongo.findAndModify(query,update,FindAndModifyOptions.options().returnNew(true),Document.class,"user_memory");
            if (result==null) throw ApiException.conflict("User memory changed concurrently.");
        }
        return mapper.response(AiResource.USER_MEMORY,result);
    }
    public void acquire(int userId,String id,Lease lease) {
        writable(AiResource.CONVERSATION);
        var now=new Date();
        var query=Query.query(owned(userId,id).and("status").is("ACTIVE").orOperator(
                Criteria.where("runtime_lease_until").exists(false),Criteria.where("runtime_lease_until").lte(now)));
        var result=mongo.updateFirst(query,new Update().set("runtime_lease_owner",lease.owner())
                .set("runtime_lease_until",new Date(now.getTime()+leaseSeconds*1000)),"conversations");
        if (result.getMatchedCount()==0) { conversation(userId,id); throw ApiException.conflict("Conversation already processing a request or inactive."); }
    }
    public void release(int userId,String id,Lease lease) {
        writable(AiResource.CONVERSATION);
        mongo.updateFirst(Query.query(owned(userId,id).and("runtime_lease_owner").is(lease.owner())),
                new Update().unset("runtime_lease_owner").unset("runtime_lease_until"),"conversations");
    }
    public void renew(int userId,String id,Lease lease) {
        writable(AiResource.CONVERSATION);
        var now=new Date();
        var result=mongo.updateFirst(Query.query(owned(userId,id).and("runtime_lease_owner").is(lease.owner())
                        .and("runtime_lease_until").gt(now)),
                new Update().set("runtime_lease_until",new Date(now.getTime()+leaseSeconds*1000)),"conversations");
        if (result.getMatchedCount()==0) throw ApiException.conflict("The processing lease expired or changed.");
    }
}
