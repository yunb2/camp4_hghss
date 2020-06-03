package com.smilegate.auth.service;

import com.smilegate.auth.dto.request.SigninRequestDto;
import com.smilegate.auth.dto.request.SignupRequestDto;
import com.smilegate.auth.dto.response.TokenResponseDto;
import io.jsonwebtoken.ExpiredJwtException;

public interface UserService {
    TokenResponseDto signin(SigninRequestDto signinRequestDto);

    void signout(String refreshToken);

    TokenResponseDto refreshToken(String refreshToken) throws ExpiredJwtException;

    void sendSignupMail(SignupRequestDto signupRequestDto);

    void registerUser(String key);

    void sendPasswordMail(String email);

    void updatePassword(String key, String passwd);
}
