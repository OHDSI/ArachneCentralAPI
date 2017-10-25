package com.odysseusinc.arachne.portal.security;

public class HostNameIsNotInServiceException extends RuntimeException {

    public HostNameIsNotInServiceException(String host) {

        super(String.format("Requests to '%s' is not in service", host));
    }
}
