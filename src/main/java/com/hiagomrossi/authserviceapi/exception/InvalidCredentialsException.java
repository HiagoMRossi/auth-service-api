package com.hiagomrossi.authserviceapi.exception;

public class InvalidCredentialsException extends RuntimeException {

    public InvalidCredentialsException() {
        super("Invalid credentials. Check your email and password.");
    }
}
