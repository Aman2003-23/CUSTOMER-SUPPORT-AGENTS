package com.example.demo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ReplyRequest {

    @NotBlank
    @Size(max = 5000)
    private String body;

    public String getBody() { return body; }
    public void setBody(String body) { this.body = body; }
}