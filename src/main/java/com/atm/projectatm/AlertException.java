package com.atm.projectatm;

public class AlertException extends RuntimeException{
    public AlertException(){}
    public AlertException(String message){
        super(message);
    }
}
