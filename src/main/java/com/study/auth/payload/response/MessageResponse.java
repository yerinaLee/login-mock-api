package com.study.auth.payload.response;

import lombok.Data;

@Data
public class MessageResponse {

    private  String message;

    public MessageResponse(String message) {this.message = message;}
}
