package com.ms.test_api.dto.response;

import java.io.Serializable;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TokenResponse implements Serializable{
    private String message;
    private String accessToken;
    private String refreshToken;
    private String userCCCD;
}
