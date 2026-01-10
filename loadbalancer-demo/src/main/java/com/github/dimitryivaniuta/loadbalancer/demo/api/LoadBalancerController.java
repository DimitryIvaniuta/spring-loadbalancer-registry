package com.github.dimitryivaniuta.loadbalancer.demo.api;

import com.github.dimitryivaniuta.loadbalancer.service.LoadBalancerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/lb")
@RequiredArgsConstructor
public class LoadBalancerController {

    private final LoadBalancerService lb;

    @PostMapping("/instances")
    public ResponseEntity<Long> register(@RequestBody RegisterRequest req) {
        return ResponseEntity.ok(lb.register(req.address()));
    }

    @DeleteMapping("/instances")
    public ResponseEntity<Void> unregister(@RequestParam("address") String address) {
        lb.unregister(address);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/instances")
    public ResponseEntity<List<String>> list() {
        return ResponseEntity.ok(lb.listAddresses());
    }

    @GetMapping("/instances/next")
    public ResponseEntity<Map<String, String>> next() {
        return lb.nextAddress()
                .map(a -> ResponseEntity.ok(Map.of("address", a)))
                .orElseGet(() -> ResponseEntity.noContent().build());
    }
}
