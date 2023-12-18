package com.basktpay.dfsapi.reponse;

import lombok.Getter;

@Getter
public class FileContentResponse {
    private String message;
    private String status;

    public void setStatus(String status) {
        this.status = status;
    }


    public void setMessage(String message) {
        this.message = message;
    }
}
