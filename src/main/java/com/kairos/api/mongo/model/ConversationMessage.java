package com.kairos.api.mongo.model;
import org.springframework.data.mongodb.core.mapping.Field;
import java.util.List;
public record ConversationMessage(@Field("message_id") Object messageId, String sender, String content, Object timestamp,
                                  List<String> sources) { }
