package com.ms.test_api.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class UserDTO {

    private String cccd;
    private String username;
    
    private String email;
    private String fullname;
    private String phone;
    private RoleDTO role;

    public UserDTO(String cccd, String fullName, RoleDTO roleDTO) {
        this.cccd = cccd;
        this.fullname = fullName;
        this.role = roleDTO;
    }

}
