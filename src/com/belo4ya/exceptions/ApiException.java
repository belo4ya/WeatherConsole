package com.belo4ya.exceptions;

public class ApiException extends Exception {

    public ApiException() {
        super("There are problems with the service that provides the api");
    }

}
