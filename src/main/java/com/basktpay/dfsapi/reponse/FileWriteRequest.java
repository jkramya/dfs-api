package com.basktpay.dfsapi.reponse;

import lombok.Getter;

@Getter
public class FileWriteRequest {
    private String message;

    public void setMessage(String message) {
        this.message = message;
    }

}
