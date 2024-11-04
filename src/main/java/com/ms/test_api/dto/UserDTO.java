package com.ms.test_api.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class UserDTO {

    private int userId;
    private String citizenId;
    private String username;
    
    private String email;
    private String fullname;
    private String phone;
    private RoleDTO role;

    public UserDTO(int userId, String fullName, RoleDTO roleDTO) {
        this.userId = userId;
        this.fullname = fullName;
        this.role = roleDTO;
    }

}
