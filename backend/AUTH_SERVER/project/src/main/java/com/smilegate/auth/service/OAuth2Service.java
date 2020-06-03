package com.smilegate.auth.service;

import com.smilegate.auth.dto.response.TokenResponseDto;

public interface OAuth2Service {
    TokenResponseDto createToken(String code);
}
