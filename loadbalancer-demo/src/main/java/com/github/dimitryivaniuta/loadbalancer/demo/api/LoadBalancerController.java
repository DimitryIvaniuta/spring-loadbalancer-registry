package com.github.dimitryivaniuta.loadbalancer.demo.api;

import com.github.dimitryivaniuta.loadbalancer.api.LoadBalancer;
import com.github.dimitryivaniuta.loadbalancer.api.RegistryScope;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/lb")
@RequiredArgsConstructor
public class LoadBalancerController {

    public static final String HDR_TENANT = "X-Tenant-Id";
    public static final String HDR_GROUP = "X-Service-Group";

    private final LoadBalancer lb;

    @PostMapping("/instances")
    public ResponseEntity<Long> register(
            @RequestHeader(value = HDR_TENANT, required = false) String tenantId,
            @RequestHeader(value = HDR_GROUP, required = false) String serviceGroup,
            @RequestBody RegisterRequest req
    ) {
        long id = lb.register(scope(tenantId, serviceGroup), req.address());
        return ResponseEntity.ok(id);
    }

    @DeleteMapping("/instances")
    public ResponseEntity<Void> unregister(
            @RequestParam("address") String address,
            @RequestHeader(value = HDR_TENANT, required = false) String tenantId,
            @RequestHeader(value = HDR_GROUP, required = false) String serviceGroup
    ) {
        lb.unregister(scope(tenantId, serviceGroup), address);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/instances")
    public ResponseEntity<List<String>> list(
            @RequestHeader(value = HDR_TENANT, required = false) String tenantId,
            @RequestHeader(value = HDR_GROUP, required = false) String serviceGroup
    ) {
        return ResponseEntity.ok(lb.listAddresses(scope(tenantId, serviceGroup)));
    }

    @GetMapping("/instances/next")
    public ResponseEntity<InstanceResponse> next(
            @RequestHeader(value = HDR_TENANT, required = false) String tenantId,
            @RequestHeader(value = HDR_GROUP, required = false) String serviceGroup
    ) {
        return lb.nextAddress(scope(tenantId, serviceGroup))
                .map(a -> ResponseEntity.ok(new InstanceResponse(a)))
                .orElseGet(() -> ResponseEntity.noContent().build());
    }

    private RegistryScope scope(String tenantId, String serviceGroup) {
        return RegistryScope.of(tenantId, serviceGroup);
    }

}
