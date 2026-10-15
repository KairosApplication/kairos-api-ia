package com.kairos.api.mongo.service;
import com.kairos.api.mongo.exception.ApiException;
import com.kairos.api.mongo.dto.request.OnCreate;
import com.kairos.api.mongo.model.AiResource;
import jakarta.validation.Validator;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.groups.Default;
import java.lang.reflect.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;
import org.springframework.stereotype.Component;
@Component
public class MutationValidator {
    private final Validator validator;
    public MutationValidator(Validator validator) { this.validator=validator; }
    public Object validate(AiResource resource,Map<String,Object> values,boolean create) {
        return bind(resource.requestType,values,create,0);
    }
    private Object bind(Class<?> type,Map<String,Object> values,boolean create,int depth) {
        if (depth>12) throw ApiException.badRequest("Payload nesting exceeds the limit.");
        RecordComponent[] components=type.getRecordComponents(); Set<String> names=new HashSet<>();
        for (var component:components) names.add(component.getName());
        if (values.keySet().stream().anyMatch(name->!names.contains(name))) throw ApiException.badRequest("Unknown or immutable field in the request.");
        Object[] args=new Object[components.length]; Class<?>[] types=new Class<?>[components.length];
        for (int i=0;i<components.length;i++) {
            var component=components[i];types[i]=component.getType();Object value=values.get(component.getName());
            if (values.containsKey(component.getName()) && value==null && component.getType()==List.class) {
                throw ApiException.badRequest("Use an empty array to clear a list field.");
            }
            if (!create && values.containsKey(component.getName()) && value==null && required(type,component)) {
                throw ApiException.badRequest("A required field cannot be cleared.");
            }
            args[i]=convert(value,component.getGenericType(),depth+1);
        }
        try {
            Object request=type.getDeclaredConstructor(types).newInstance(args);
            var violations=create ? validator.validate(request,Default.class,OnCreate.class) : validator.validate(request,Default.class);
            if (!violations.isEmpty()) {
                String paths=String.join(", ",violations.stream().map(v->v.getPropertyPath().toString()).sorted().distinct().toList());
                throw ApiException.badRequest("Invalid fields: "+paths);
            }
            return request;
        } catch (ReflectiveOperationException failure) { throw ApiException.badRequest("Invalid request structure."); }
    }
    private boolean required(Class<?> type,RecordComponent component) {
        try { return type.getDeclaredField(component.getName()).isAnnotationPresent(NotNull.class); }
        catch (NoSuchFieldException failure) { throw new IllegalStateException("Invalid request mapping."); }
    }
    @SuppressWarnings("unchecked")
    private Object convert(Object value,Type type,int depth) {
        if (depth>12) throw ApiException.badRequest("Payload nesting exceeds the limit.");
        if (value==null) return null;
        if (type==String.class) {
            if (!(value instanceof String text) || text.length()>200000) throw ApiException.badRequest("Expected a bounded string.");
            return value;
        }
        if (type==Instant.class) {
            if (!(value instanceof String text)) throw ApiException.badRequest("Expected an ISO timestamp with a timezone.");
            try { return Instant.parse(text); } catch (RuntimeException failure) { throw ApiException.badRequest("Invalid ISO timestamp."); }
        }
        if (type==Boolean.class) {
            if (!(value instanceof Boolean)) throw ApiException.badRequest("Expected a boolean."); return value;
        }
        if (type==Long.class || type==Integer.class || type==Double.class) {
            if (!(value instanceof Number number) || !Double.isFinite(number.doubleValue())) throw ApiException.badRequest("Expected a finite number.");
            try {
                BigDecimal decimal=new BigDecimal(number.toString());
                if (type==Long.class) return decimal.longValueExact();
                if (type==Integer.class) return decimal.intValueExact();return decimal.doubleValue();
            } catch (ArithmeticException failure) { throw ApiException.badRequest("Invalid numeric range or fraction."); }
        }
        if (type instanceof ParameterizedType parameterized && parameterized.getRawType()==List.class) {
            if (!(value instanceof List<?> list) || list.size()>4096) throw ApiException.badRequest("Expected a bounded array.");
            return list.stream().map(item->{
                if (item==null) throw ApiException.badRequest("Array elements cannot be null.");
                return convert(item,parameterized.getActualTypeArguments()[0],depth+1);
            }).toList();
        }
        if (type instanceof ParameterizedType parameterized && parameterized.getRawType()==Map.class) {
            if (!(value instanceof Map<?,?> map)) throw ApiException.badRequest("Expected an object.");validateMap(map,depth);return value;
        }
        if (type instanceof Class<?> record && record.isRecord()) {
            if (!(value instanceof Map<?,?>)) throw ApiException.badRequest("Expected a nested object.");
            return bind(record,(Map<String,Object>)value,true,depth+1);
        }
        throw ApiException.badRequest("Unsupported field type.");
    }
    private void validateMap(Object value,int depth) {
        if (depth>12) throw ApiException.badRequest("Payload nesting exceeds the limit.");
        if (value instanceof Map<?,?> map) {
            if (map.size()>1000) throw ApiException.badRequest("Object exceeds the field limit.");
            map.forEach((key,item)->{
                if (!(key instanceof String name) || name.isEmpty() || name.startsWith("$") || name.contains(".") || name.indexOf(0)>=0) throw ApiException.badRequest("Unsafe object field name.");
                validateMap(item,depth+1);
            });
        } else if (value instanceof List<?> list) {
            if (list.size()>4096) throw ApiException.badRequest("Array exceeds the limit.");list.forEach(item->validateMap(item,depth+1));
        } else if (value instanceof Number number && !Double.isFinite(number.doubleValue())) throw ApiException.badRequest("Expected a finite number.");
        else if (value instanceof String text && text.length()>200000) throw ApiException.badRequest("String exceeds the limit.");
    }
}
