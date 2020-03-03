package com.smilegate.auth.service;

import com.smilegate.auth.domain.User;
import com.smilegate.auth.dto.request.SigninRequestDto;
import com.smilegate.auth.dto.request.SignupRequestDto;
import com.smilegate.auth.dto.response.TokenResponseDto;
import com.smilegate.auth.exceptions.*;
import com.smilegate.auth.repository.UserRepository;
import com.smilegate.auth.utils.JwtUtil;
import com.smilegate.auth.utils.MailUtil;
import com.smilegate.auth.utils.RedisUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.Future;

@Slf4j
@RequiredArgsConstructor
@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final MailUtil mailUtil;
    private final RedisUtil redisUtil;

    public TokenResponseDto signin(SigninRequestDto signinRequestDto) {

        String email = signinRequestDto.getEmail();
        String password = signinRequestDto.getPassword();

        User user = userRepository.findByEmail(email);

        if(user==null || user.getPasswd() == null) throw new EmailNotExistException(email);
        if(!passwordEncoder.matches(password, user.getPasswd())) throw new PasswordWrongException();

        Future<Integer> updateAccessedAt = userRepository.updateAccessedAt(user.getId(), new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));

        String accessToken = jwtUtil.createToken(user.getId(), user.getEmail(), user.getNickname(), user.getRole(), "ACCESS_TOKEN", 30);
        String refreshToken = jwtUtil.createToken(user.getId(), user.getEmail(), user.getNickname(), user.getRole(), "REFRESH_TOKEN", 60*24*14);

        redisUtil.set(refreshToken, user.getRole(), 60*24*14);

        while(!updateAccessedAt.isDone());

        return TokenResponseDto.builder()
                    .id(user.getId())
                    .email(email)
                    .nickname(user.getNickname())
                    .role(user.getRole())
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .build();
    }

    public void signout(String refreshToken) {
        if(!redisUtil.delete(refreshToken)) throw new SignoutException();
    }

    public TokenResponseDto refreshToken(String refreshToken) throws ExpiredJwtException {

        if(!redisUtil.hasKey(refreshToken)) throw new UnauthorizedException();

        Claims claims = jwtUtil.getClaims(refreshToken);

        int userId = (int) claims.get("userId");
        String email = claims.getSubject();
        String nickname  = (String) claims.get("nickname");
        int role = (int) claims.get("role");

        String accessToken = jwtUtil.createToken(userId, email, nickname, role, "ACCESS_TOKEN", 30);

        return TokenResponseDto.builder()
                .id(userId)
                .email(email)
                .nickname(nickname)
                .role(role)
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    public void sendSignupMail(SignupRequestDto signupRequestDto) {

        String email = signupRequestDto.getEmail();
        String passwd = passwordEncoder.encode(signupRequestDto.getPassword());

        User user = userRepository.findByEmail(email);
        if(user != null && user.getPasswd() != null) throw new ExistEmailException(email);

        String key = UUID.randomUUID().toString();
        if(user == null) {
            user = User.builder()
                        .email(email)
                        .nickname("temp")
                        .passwd(passwd)
                        .role(1)
                        .status(1)
                        .build();
        } else {
            user.setPasswd(passwd);
        }

        if(mailUtil.sendSignupMail(key, user)) redisUtil.set(key, user, 10);
    }

    public void registerUser(String key) {

        User user = (User)redisUtil.get(key);

        if(user==null) throw new TimeoutException();

        String now = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());

        user.setCreatedAt(now);
        user.setUpdatedAt(now);
        user.setAccessedAt(now);

        int userId = userRepository.registerUser(user);
        if(userId > 0) redisUtil.delete(key);

        String nickname = (user.getNickname().equals("temp"))? userRepository.getNickname(userId) : user.getNickname();

        userRepository.updateNickname(userId, nickname, now);
    }

    public void sendPasswordMail(String email) {

        if(userRepository.countUser(email) == 0) throw new EmailNotExistException(email);

        String key = UUID.randomUUID().toString();

        if(mailUtil.sendPasswordMail(key, email)) redisUtil.set(key, email, 10);
    }

    public void updatePassword(String key, String passwd) {

        String email = (String) redisUtil.get(key);

        if(email == null) throw new UnauthorizedException();

        if(userRepository.countUser(email) == 0) throw new EmailNotExistException(email);

        passwd = passwordEncoder.encode(passwd);

        userRepository.updatePassword(email, passwd, new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));

        redisUtil.delete(key);
    }
}
