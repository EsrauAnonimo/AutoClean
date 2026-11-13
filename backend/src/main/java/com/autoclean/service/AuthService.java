package com.autoclean.service;

import com.autoclean.dto.auth.CompleteProfileRequest;
import com.autoclean.dto.auth.LoginRequest;
import com.autoclean.dto.auth.LoginResponse;
import com.autoclean.dto.auth.RegisterRequest;

/**
 * Contrato de servicio para autenticación y registro de usuarios.
 */
public interface AuthService {

    LoginResponse login(LoginRequest request);

    LoginResponse register(RegisterRequest request);

    LoginResponse completeProfile(String email, CompleteProfileRequest request);
}

