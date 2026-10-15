package com.kairos.api.mongo.repository;
import com.kairos.api.mongo.model.AiResource;
import com.kairos.api.mongo.exception.ApiException;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.*;
import org.springframework.data.mongodb.core.query.*;
import org.springframework.stereotype.Repository;
import org.springframework.http.HttpStatus;
import org.springframework.data.domain.Sort;
import java.util.List;
import java.util.Map;
@Repository
public class AiDocumentRepository {
    private final MongoTemplate mongo;
    public AiDocumentRepository(MongoTemplate mongo) { this.mongo=mongo; }
    public Document find(AiResource resource,ObjectId id) {
        return mongo.findOne(Query.query(Criteria.where("_id").is(id)),Document.class,resource.collection);
    }
    public List<Document> list(AiResource resource,Map<String,Object> filters,int page,int size) {
        Query query=new Query();
        filters.forEach((field,value)->query.addCriteria(Criteria.where(field).is(value)));
        query.with(Sort.by(Sort.Direction.ASC,"_id")).skip((long)page*size).limit(size+1);
        return mongo.find(query,Document.class,resource.collection);
    }
    public boolean exists(AiResource resource,String field,Object value) {
        return mongo.exists(Query.query(Criteria.where(field).is(value)),resource.collection);
    }
    private void requireCollection(AiResource resource) {
        if (!mongo.collectionExists(resource.collection)) throw new ApiException(HttpStatus.SERVICE_UNAVAILABLE,
                "COLLECTION_UNAVAILABLE","The observed collection is unavailable; automatic creation is disabled.");
    }
    public Document insert(AiResource resource,Document document) {
        requireCollection(resource); return mongo.insert(document,resource.collection);
    }
    public Document patch(AiResource resource,ObjectId id,Map<String,Object> fields) {
        requireCollection(resource); Update update=new Update(); fields.forEach(update::set);
        return mongo.findAndModify(Query.query(Criteria.where("_id").is(id)),update,
                FindAndModifyOptions.options().returnNew(true),Document.class,resource.collection);
    }
    public Document delete(AiResource resource,ObjectId id) {
        requireCollection(resource); return mongo.findAndRemove(Query.query(Criteria.where("_id").is(id)),Document.class,resource.collection);
    }
}
