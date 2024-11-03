package com.ms.test_api.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
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

    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "role_id")
    private Role role;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority(getRole().getName().toUpperCase()));
        return authorities;
    }

    public UserSoccerField(String CCCD, String username, String password, String email, String fullname, String phone,
            Role role) {
        this.CCCD = CCCD;
        this.username = username;
        this.password = password;
        this.email = email;
        this.fullname = fullname;
        this.phone = phone;
        this.role = role;
    }
    

}
