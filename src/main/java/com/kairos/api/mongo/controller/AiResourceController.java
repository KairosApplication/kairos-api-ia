package com.kairos.api.mongo.controller;
import com.kairos.api.mongo.dto.*;
import com.kairos.api.mongo.model.AiResource;
import com.kairos.api.mongo.service.AiResourceService;
import java.net.URI;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
@RestController
@RequestMapping("/api/v1")
public class AiResourceController {
    private final AiResourceService service;
    public AiResourceController(AiResourceService service) { this.service=service; }
    @GetMapping("/{resource}")
    public PageResponse list(@PathVariable String resource,@RequestParam Map<String,String> params) { return service.list(AiResource.fromRoute(resource),params); }
    @GetMapping("/{resource}/{id}")
    public AiRecordResponse find(@PathVariable String resource,@PathVariable String id) { return service.find(AiResource.fromRoute(resource),id); }
    @PostMapping("/{resource}")
    public ResponseEntity<AiRecordResponse> create(@PathVariable String resource,@RequestBody Map<String,Object> body) {
        var result=service.create(AiResource.fromRoute(resource),body);
        return ResponseEntity.created(URI.create("/api/v1/"+resource+"/"+result.id())).body(result);
    }
    @PatchMapping("/{resource}/{id}")
    public AiRecordResponse patch(@PathVariable String resource,@PathVariable String id,@RequestBody Map<String,Object> body) { return service.patch(AiResource.fromRoute(resource),id,body); }
    @DeleteMapping("/{resource}/{id}")
    public ResponseEntity<Void> delete(@PathVariable String resource,@PathVariable String id) {
        service.delete(AiResource.fromRoute(resource),id);return ResponseEntity.noContent().build();
    }
}
