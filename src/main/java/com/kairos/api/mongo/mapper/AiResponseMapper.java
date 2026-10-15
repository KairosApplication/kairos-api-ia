package com.kairos.api.mongo.mapper;
import com.kairos.api.mongo.dto.AiRecordResponse;
import com.kairos.api.mongo.model.AiResource;
import java.lang.reflect.RecordComponent;
import java.util.*;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.convert.MongoConverter;
import org.springframework.stereotype.Component;
@Component
public class AiResponseMapper {
    private final MongoConverter converter;
    public AiResponseMapper(MongoConverter converter) { this.converter=converter; }
    public AiRecordResponse response(AiResource resource,Document document) {
        Object model=converter.read(resource.modelType,document);
        Map<String,Object> fields=recordFields(model);
        Object id=fields.remove("id");
        return new AiRecordResponse((String)id,fields);
    }
    private static Map<String,Object> recordFields(Object model) {
        var fields=new LinkedHashMap<String,Object>();
        try {
            for (RecordComponent field:model.getClass().getRecordComponents()) fields.put(field.getName(),jsonValue(field.getAccessor().invoke(model)));
            return fields;
        } catch (ReflectiveOperationException failure) { throw new IllegalStateException("Document cannot be serialized."); }
    }
    private static Object jsonValue(Object value) {
        if (value instanceof ObjectId id) return id.toHexString();
        if (value instanceof Date date) return date.toInstant().toString();
        if (value instanceof Map<?,?> map) {
            var result=new LinkedHashMap<String,Object>();
            map.forEach((key,item)->result.put(key.toString(),jsonValue(item))); return result;
        }
        if (value instanceof List<?> list) return list.stream().map(AiResponseMapper::jsonValue).toList();
        if (value!=null && value.getClass().isRecord()) return recordFields(value);
        return value;
    }
}
