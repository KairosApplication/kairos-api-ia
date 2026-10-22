package com.kairos.api.mongo.service;
import com.kairos.api.mongo.dto.*;
import com.kairos.api.mongo.exception.ApiException;
import com.kairos.api.mongo.mapper.AiResponseMapper;
import com.kairos.api.mongo.model.AiResource;
import com.kairos.api.mongo.repository.AiDocumentRepository;
import java.lang.reflect.RecordComponent;
import java.time.Instant;
import java.util.*;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
@Service
public class AiResourceService {
    private final AiDocumentRepository repository;
    private final MutationValidator validator;
    private final AiResponseMapper mapper;
    private final boolean writesEnabled;
    public AiResourceService(AiDocumentRepository repository,MutationValidator validator,AiResponseMapper mapper,
            @Value("${app.mongodb.writes-enabled:false}") boolean writesEnabled) {
        this.repository=repository;this.validator=validator;this.mapper=mapper;this.writesEnabled=writesEnabled;
    }
    public static ObjectId id(String id) {
        if (id==null || !ObjectId.isValid(id)) throw ApiException.badRequest("Expected a 24-character hexadecimal ObjectId.");return new ObjectId(id);
    }
    public AiRecordResponse find(AiResource resource,String id) { return mapper.response(resource,require(resource,id(id))); }
    private Document require(AiResource resource,ObjectId id) {
        Document document=repository.find(resource,id);if (document==null) throw ApiException.notFound("Document not found.");return document;
    }
    public PageResponse list(AiResource resource,Map<String,String> params) {
        Map<String,String> filters=new LinkedHashMap<>(params);
        int page=integer(filters.remove("page"),0,0,10000),size=integer(filters.remove("size"),20,1,100);
        Map<String,Object> query=new LinkedHashMap<>();
        filters.forEach((field,value)->{
            if (!resource.hasField(field) || !Set.of("userId","conversationId","executionId","documentId","status","active","accessScope").contains(field)) {
                throw ApiException.badRequest("Unsupported filter for this resource.");
            }
            Object converted=value;
            if (field.equals("accessScope")) {
                if (!value.matches("[a-f0-9]{64}")) throw ApiException.badRequest("Invalid accessScope.");
            } else if (field.equals("userId")) {
                try { int user=Integer.parseInt(value);if (user<=0) throw new NumberFormatException();converted=user; }
                catch (NumberFormatException failure) { throw ApiException.badRequest("Invalid userId."); }
            } else if (field.endsWith("Id")) converted=id(value);
            else if (field.equals("active")) {
                if (!value.equals("true") && !value.equals("false")) throw ApiException.badRequest("Invalid active filter.");converted=Boolean.parseBoolean(value);
            }
            query.put(resource.storedField(field),converted);
        });
        var documents=repository.list(resource,query,page,size);
        return new PageResponse(documents.stream().limit(size).map(document->mapper.response(resource,document)).toList(),page,size,documents.size()>size);
    }
    private int integer(String value,int fallback,int min,int max) {
        if (value==null) return fallback;
        try { int parsed=Integer.parseInt(value);if (parsed<min || parsed>max) throw new NumberFormatException();return parsed; }
        catch (NumberFormatException failure) { throw ApiException.badRequest("Invalid pagination bounds."); }
    }
    private void allowWrites() {
        if (!writesEnabled) throw new ApiException(HttpStatus.FORBIDDEN,"WRITES_DISABLED","Database writes are disabled for this environment.");
    }
    public AiRecordResponse create(AiResource resource,Map<String,Object> body) {
        allowWrites();Object request=validator.validate(resource,body,true);
        Document document=new Document("_id",new ObjectId());document.putAll(fields(resource,request,body.keySet(),true));
        checkReferences(resource,document,document.keySet());return mapper.response(resource,repository.insert(resource,document));
    }
    public AiRecordResponse patch(AiResource resource,String id,Map<String,Object> body) {
        allowWrites();if (body.isEmpty()) throw ApiException.badRequest("A patch must contain at least one field.");
        ObjectId objectId=id(id);Document existing=require(resource,objectId);Object request=validator.validate(resource,body,false);
        Map<String,Object> updates=fields(resource,request,body.keySet(),false);
        if (updates.isEmpty()) throw ApiException.badRequest("A patch must contain a mutable field.");
        Document effective=new Document(existing);effective.putAll(updates);checkReferences(resource,effective,updates.keySet());
        Document updated=repository.patch(resource,objectId,updates);if (updated==null) throw ApiException.notFound("Document not found.");
        return mapper.response(resource,updated);
    }
    public void delete(AiResource resource,String id) {
        allowWrites();ObjectId objectId=id(id);require(resource,objectId);
        if (resource==AiResource.CONVERSATION && repository.exists(AiResource.AI_EXECUTION,"conversation_id",objectId)) throw ApiException.conflict("Conversation is referenced by executions.");
        if (resource==AiResource.RAG_DOCUMENT && repository.exists(AiResource.RAG_CHUNK,"document_id",objectId)) throw ApiException.conflict("RAG document is referenced by chunks.");
        if (resource==AiResource.AI_EXECUTION) {
            for (var child:List.of(AiResource.FEEDBACK,AiResource.AGENT_TRACE,AiResource.GUARDRAIL_EVENT,AiResource.JUDGE_EVALUATION)) {
                if (repository.exists(child,"execution_id",objectId)) throw ApiException.conflict("Execution is referenced by dependent records.");
            }
        }
        if (repository.delete(resource,objectId)==null) throw ApiException.notFound("Document not found.");
    }
    private void checkReferences(AiResource resource,Document document,Set<String> changed) {
        String field=null;AiResource target=null;
        if (resource==AiResource.AI_EXECUTION) { field="conversation_id";target=AiResource.CONVERSATION; }
        else if (resource==AiResource.RAG_CHUNK) { field="document_id";target=AiResource.RAG_DOCUMENT; }
        else if (List.of(AiResource.FEEDBACK,AiResource.AGENT_TRACE,AiResource.GUARDRAIL_EVENT,AiResource.JUDGE_EVALUATION).contains(resource)) { field="execution_id";target=AiResource.AI_EXECUTION; }
        if (field!=null && (changed.contains(field) || changed.contains("user_id"))) {
            Document parent=require(target,(ObjectId)document.get(field));
            if (document.containsKey("user_id") && parent.containsKey("user_id")
                    && !Objects.equals(((Number)document.get("user_id")).longValue(),((Number)parent.get("user_id")).longValue())) throw ApiException.conflict("userId does not match the referenced record.");
        }
    }
    private Map<String,Object> fields(AiResource resource,Object request,Set<String> submitted,boolean create) {
        var result=new LinkedHashMap<String,Object>();
        try {
            for (RecordComponent field:request.getClass().getRecordComponents()) {
                if (!create && !submitted.contains(field.getName())) continue;
                Object value=field.getAccessor().invoke(request);
                if (create && value==null && Set.of("createdAt","updatedAt","startedAt").contains(field.getName())) value=Instant.now();
                if (create && value==null && Set.of("messages","memories","agentsUsed").contains(field.getName())) value=List.of();
                String stored=resource.storedField(field.getName());
                if (!create && value instanceof Map<?,?> map) flatten(stored,map,result);
                else result.put(stored,storedValue(field.getName(),value));
            }
            return result;
        } catch (ReflectiveOperationException failure) { throw new IllegalStateException("Invalid field mapping."); }
    }
    private void flatten(String prefix,Map<?,?> map,Map<String,Object> result) {
        map.forEach((key,value)->{
            String path=prefix+"."+key;
            if (value instanceof Map<?,?> nested) flatten(path,nested,result);
            else result.put(path,value);
        });
    }
    private Object storedValue(String field,Object value) {
        if (value==null) return null;
        if (Set.of("executionId","conversationId","documentId","memoryId").contains(field)) return id((String)value);
        if (value instanceof Instant time) return Date.from(time);
        if (value instanceof List<?> list) return list.stream().map(item->storedValue("",item)).toList();
        if (value.getClass().isRecord()) {
            var nested=new Document();
            try {
                for (RecordComponent component:value.getClass().getRecordComponents()) {
                    Object item=component.getAccessor().invoke(value);
                    if (item==null && Set.of("memoryId","messageId").contains(component.getName())) item=new ObjectId();
                    if (item==null && Set.of("timestamp","createdAt","updatedAt").contains(component.getName())) item=Date.from(Instant.now());
                    String stored=component.getName().replaceAll("([a-z])([A-Z])","$1_$2").toLowerCase(Locale.ROOT);
                    nested.put(stored,item instanceof ObjectId ? item : storedValue(component.getName(),item));
                }
                return nested;
            } catch (ReflectiveOperationException failure) { throw new IllegalStateException("Invalid nested mapping."); }
        }
        return value;
    }
}
