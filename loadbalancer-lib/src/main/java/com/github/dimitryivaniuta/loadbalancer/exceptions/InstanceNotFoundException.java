package com.github.dimitryivaniuta.loadbalancer.exceptions;

public class InstanceNotFoundException extends RuntimeException {
    public InstanceNotFoundException(String address) {
        super("Instance not found: " + address);
    }
}
