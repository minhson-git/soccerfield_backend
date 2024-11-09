package com.ms.test_api.dto.response;

import java.io.Serializable;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TokenResponse implements Serializable{
    private boolean authenticated;
    private String token;
    private String role;
    private int userId;
}
