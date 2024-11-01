package com.ms.test_api.dto;

import lombok.Data;

@Data
public class UserDTO {

    private String cccd;
    private String username;
    private String password;
    private String email;
    private String fullname;
    private String phone;
    private RoleDTO role;
    
    public UserDTO(String cccd, String username, String password, String email, String fullname, String phone,
            RoleDTO role) {
        this.cccd = cccd;
        this.username = username;
        this.password = password;
        this.email = email;
        this.fullname = fullname;
        this.phone = phone;
        this.role = role;
    }

    public UserDTO(String cccd, String fullName) {
        this.cccd = cccd;
        this.fullname = fullName;
    }

}
