package com.example.api.stream;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class StreamController {

    private final KinesisStreamService kinesisStreamService;

    public StreamController(KinesisStreamService kinesisStreamService) {
        this.kinesisStreamService = kinesisStreamService;
    }

    @GetMapping("/api/stream/live")
    public ResponseEntity<Map<String, String>> getLiveStream() {
        System.out.println("LIVE endpoint HIT");
        String url = kinesisStreamService.issueLiveHlsUrl();
        return ResponseEntity.ok(Map.of("streamUrl", url));
    }
}