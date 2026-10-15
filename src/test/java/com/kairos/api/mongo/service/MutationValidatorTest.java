package com.kairos.api.mongo.service;
import com.kairos.api.mongo.AiFixtures;
import com.kairos.api.mongo.model.AiResource;
import com.kairos.api.mongo.exception.ApiException;
import jakarta.validation.Validation;
import java.util.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class MutationValidatorTest {
    final MutationValidator validator=new MutationValidator(Validation.buildDefaultValidatorFactory().getValidator());
    @Test void rejectsMissingRequiredAndWrongNumericTypes() {
        assertThrows(ApiException.class,()->validator.validate(AiResource.CONVERSATION,Map.of(),true));
        assertThrows(ApiException.class,()->validator.validate(AiResource.CONVERSATION,Map.of("userId","1","status","ACTIVE"),true));
        assertThrows(ApiException.class,()->validator.validate(AiResource.CONVERSATION,Map.of("userId",1.5,"status","ACTIVE"),true));
        assertThrows(ApiException.class,()->validator.validate(AiResource.CONVERSATION,Map.of("userId",Long.MAX_VALUE,"status","ACTIVE"),true));
        assertThrows(ApiException.class,()->validator.validate(AiResource.CONVERSATION,Map.of("userId",1,"status","  "),true));
    }
    @Test void rejectsImmutableUnknownOperatorFieldsAndNullRequiredPatch() {
        assertThrows(ApiException.class,()->validator.validate(AiResource.CONVERSATION,Map.of("_id","test"),false));
        assertThrows(ApiException.class,()->validator.validate(AiResource.CONVERSATION,Map.of("$set",Map.of()),false));
        var patch=new HashMap<String,Object>();patch.put("userId",null);
        assertThrows(ApiException.class,()->validator.validate(AiResource.CONVERSATION,patch,false));
        assertDoesNotThrow(()->validator.validate(AiResource.CONVERSATION,Map.of("summary","text"),false));
    }
    @Test void enforcesRangesNestedValidationAndFiniteEmbedding() {
        var feedback=new HashMap<String,Object>(AiFixtures.body(AiResource.FEEDBACK));feedback.put("rating",6);
        assertThrows(ApiException.class,()->validator.validate(AiResource.FEEDBACK,feedback,true));
        var chunk=new HashMap<String,Object>(AiFixtures.body(AiResource.RAG_CHUNK));chunk.put("embedding",List.of(Double.NaN));
        assertThrows(ApiException.class,()->validator.validate(AiResource.RAG_CHUNK,chunk,true));
        var conversation=new HashMap<String,Object>(AiFixtures.body(AiResource.CONVERSATION));conversation.put("messages",List.of(Map.of("sender","USER")));
        assertThrows(ApiException.class,()->validator.validate(AiResource.CONVERSATION,conversation,true));
    }
    @Test void arraysMustBeClearedWithAnArrayNotNull() {
        var patch=new HashMap<String,Object>();patch.put("messages",null);
        assertThrows(ApiException.class,()->validator.validate(AiResource.CONVERSATION,patch,false));
        assertDoesNotThrow(()->validator.validate(AiResource.CONVERSATION,Map.of("messages",List.of()),false));
    }
    @Test void rejectsUnsafeMapsAndMalformedDatesWithoutEchoingInput() {
        var trace=new HashMap<String,Object>(AiFixtures.body(AiResource.AGENT_TRACE));trace.put("input",Map.of("$where","private-content"));
        var error=assertThrows(ApiException.class,()->validator.validate(AiResource.AGENT_TRACE,trace,true));assertFalse(error.getMessage().contains("private-content"));
        assertThrows(ApiException.class,()->validator.validate(AiResource.CONVERSATION,Map.of("createdAt","not-a-date"),false));
    }
}
