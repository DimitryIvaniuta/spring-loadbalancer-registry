package com.github.dimitryivaniuta.loadbalancer.exceptions;

public class CapacityExceededException extends RuntimeException {
    public CapacityExceededException(int max) {
        super("Load balancer capacity exceeded. Max instances: " + max);
    }
}
