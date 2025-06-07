package com.example.resellkh.service.Impl;

import com.example.resellkh.repository.authRepo;
import com.example.resellkh.service.AuthService;
import lombok.RequiredArgsConstructor;
import com.example.resellkh.model.entity.Auth;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final authRepo authRepo;

    @Override
    public Auth findByEmail(String email) {
        return authRepo.findByEmail(email);
    }

    @Override
    public void registerUser(Auth auth) {
        authRepo.insertUser(auth);
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Auth auth = authRepo.findByEmail(email);
        if (auth == null) {
            return  null;
        }
        return auth;
    }

    @Override
    public void enableUser(String email) {
        authRepo.enableUserByEmail(email);
    }

    @Override
    public Auth resetPassword(String email, String password) {
        authRepo.updatePasswordByEmail(email, password);
        return authRepo.findByEmail(email);
    }



}
