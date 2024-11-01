package com.ms.test_api.dto.request;

import java.io.Serializable;

import lombok.Getter;

@Getter
public class SignInRequest implements Serializable {
    
    private String username;

    private String password;
}
