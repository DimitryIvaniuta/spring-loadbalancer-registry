package com.github.dimitryivaniuta.loadbalancer.exceptions;

public class DuplicateAddressException extends RuntimeException {
    public DuplicateAddressException(String address) {
        super("Address already registered: " + address);
    }
}
