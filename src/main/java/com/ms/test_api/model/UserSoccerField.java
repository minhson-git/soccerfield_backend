package com.ms.test_api.model;

import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Data
@Table(name = "user")
public class UserSoccerField implements UserDetails {

    @Id
    private String CCCD;
    private String username;
    @Column(name = "pwd")
    private String password;
    private String email;
    private String fullname;
    private String phone;
    private String role;
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of();
    }

}
