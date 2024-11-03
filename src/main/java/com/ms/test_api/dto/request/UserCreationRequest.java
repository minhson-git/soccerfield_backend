package com.ms.test_api.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserCreationRequest {

    private String cccd;

    @Size(min = 3, message = "Username must be at least 3 characters")
    private String username;

    @Size(min = 8, message = "Password must be at least 8 characters")
    private String password;
    
    @Email
    private String email;

    private String fullname;

    private String phone;

}
