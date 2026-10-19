package com.kairos.api.mongo.controller;

import com.kairos.api.mongo.dto.AiRecordResponse;
import com.kairos.api.mongo.dto.request.RuntimeRequests.*;
import com.kairos.api.mongo.service.ChatPersistenceService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/** Service-to-service routes protected by the existing API bearer token. */
@RestController
@RequestMapping("/api/v1/runtime/users/{userId}")
public class ChatPersistenceController {
    private final ChatPersistenceService service;
    public ChatPersistenceController(ChatPersistenceService service) { this.service=service; }
    @PostMapping("/conversations/active")
    public AiRecordResponse active(@PathVariable int userId) { return service.active(userId); }
    @GetMapping("/conversations/{id}")
    public AiRecordResponse find(@PathVariable int userId,@PathVariable String id) { return service.find(userId,id); }
    @PostMapping("/conversations/{id}/turns")
    public AiRecordResponse turn(@PathVariable int userId,@PathVariable String id,@Valid @RequestBody Turn turn,
            @RequestHeader(name="X-Chat-Lease",required=false) String owner) { return service.appendTurn(userId,id,turn,owner); }
    @PatchMapping("/conversations/{id}/summary")
    public AiRecordResponse summary(@PathVariable int userId,@PathVariable String id,@Valid @RequestBody Summary summary,
            @RequestHeader(name="X-Chat-Lease",required=false) String owner) { return service.summarize(userId,id,summary,owner); }
    @PostMapping("/memories")
    public AiRecordResponse memories(@PathVariable int userId,@Valid @RequestBody Memories request) { return service.appendMemories(userId,request); }
    @PostMapping("/conversations/{id}/lease")
    public ResponseEntity<Void> acquire(@PathVariable int userId,@PathVariable String id,@Valid @RequestBody Lease lease) { service.acquire(userId,id,lease); return ResponseEntity.noContent().build(); }
    @PostMapping("/conversations/{id}/lease/release")
    public ResponseEntity<Void> release(@PathVariable int userId,@PathVariable String id,@Valid @RequestBody Lease lease) { service.release(userId,id,lease); return ResponseEntity.noContent().build(); }
    @PostMapping("/conversations/{id}/lease/renew")
    public ResponseEntity<Void> renew(@PathVariable int userId,@PathVariable String id,@Valid @RequestBody Lease lease) { service.renew(userId,id,lease); return ResponseEntity.noContent().build(); }
}
