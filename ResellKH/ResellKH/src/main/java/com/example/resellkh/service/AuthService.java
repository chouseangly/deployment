package com.example.resellkh.service;
import com.example.resellkh.model.entity.Auth;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

public interface AuthService extends UserDetailsService {
    Auth findByEmail(String email);
    void registerUser(Auth auth);

    UserDetails loadUserByUsername(String email);

    void enableUser(String email);
    Auth resetPassword(String email, String password);

}

