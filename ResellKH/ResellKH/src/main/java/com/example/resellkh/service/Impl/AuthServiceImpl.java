package com.example.resellkh.service.Impl;

import com.example.resellkh.model.dto.GoogleUserDto;
import com.example.resellkh.repository.authRepo;
import com.example.resellkh.service.AuthService;
import com.example.resellkh.model.entity.Auth;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final authRepo authRepo;
    private final PasswordEncoder passwordEncoder;

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
            throw new UsernameNotFoundException("User not found with email: " + email);
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

    @Override
    public Auth registerWithGoogle(GoogleUserDto googleUserDto) {
        Optional<Auth> existing = authRepo.findByEmailOptional(googleUserDto.getEmail());
        if (existing.isPresent()) return existing.get();

        Auth auth = new Auth();
        auth.setFirstName(googleUserDto.getFirstName());
        auth.setLastName(googleUserDto.getLastName());
        auth.setUserName(googleUserDto.getFirstName() + " " + googleUserDto.getLastName());
        auth.setEmail(googleUserDto.getEmail());
        auth.setPassword(passwordEncoder.encode("google_oauth_dummy")); // always encrypt dummy
        auth.setRole("USER");
        auth.setEnabled(true);
        auth.setCreatedAt(LocalDateTime.now());

        authRepo.insertUser(auth);
        return authRepo.findByEmail(googleUserDto.getEmail());
    }

}
