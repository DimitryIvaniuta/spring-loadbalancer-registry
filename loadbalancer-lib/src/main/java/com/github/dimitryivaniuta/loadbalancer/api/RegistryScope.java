package com.github.dimitryivaniuta.loadbalancer.api;


import jakarta.annotation.Nullable;

public record RegistryScope(String tenantId, String serviceGroup) {

    public static final String DEFAULT = "default";

    public static RegistryScope of(@Nullable String tenantId, @Nullable String serviceGroup) {
        return new RegistryScope(normalizeKey(tenantId), normalizeKey(serviceGroup));
    }

    private static String normalizeKey(@Nullable String v) {
        if (v == null) return DEFAULT;
        String x = v.trim();
        return x.isEmpty() ? DEFAULT : x;
    }
}
