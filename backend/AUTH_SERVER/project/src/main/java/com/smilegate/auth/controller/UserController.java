package com.smilegate.auth.controller;

import com.smilegate.auth.dto.ResultResponse;
import com.smilegate.auth.dto.request.FindPasswordRequestDto;
import com.smilegate.auth.dto.request.SigninRequestDto;
import com.smilegate.auth.dto.request.SignupRequestDto;
import com.smilegate.auth.dto.request.UpdatePasswordRequestDto;
import com.smilegate.auth.dto.response.TokenResponseDto;
import com.smilegate.auth.exceptions.UnauthorizedException;
import com.smilegate.auth.service.UserService;
import com.smilegate.auth.utils.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@RequiredArgsConstructor
@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;
    private final JwtUtil jwtUtil;

    @GetMapping("/hello")
    public String hello() {
        return "Hello";
    }

    @PostMapping("/signin")
    public ResponseEntity<ResultResponse> signin(@RequestBody SigninRequestDto signinRequestDto) {

        TokenResponseDto tokenResponseDto = userService.signin(signinRequestDto);

        return ResponseEntity.ok().body(
                ResultResponse.builder()
                        .success("true")
                        .status(HttpStatus.OK.value())
                        .message("로그인 되었습니다.")
                        .data(tokenResponseDto)
                        .build()
        );
    }

    @GetMapping("/signout")
    public ResponseEntity<ResultResponse> signout(@RequestHeader("Authorization")String token) {
        String refreshToken = token.substring("Bearer ".length());
//        String refreshToken = jwtUtil.getToken(request);
        if(!jwtUtil.isRefreshToken(refreshToken)) throw new UnauthorizedException();

        userService.signout(refreshToken);

        return ResponseEntity.ok().body(
                ResultResponse.builder()
                        .success("true")
                        .status(HttpStatus.OK.value())
                        .message("로그아웃 되었습니다.")
                        .build()
        );
    }

    @PostMapping("/signup")
    public ResponseEntity<ResultResponse> signup(@RequestBody SignupRequestDto signupRequestDto) {

        userService.sendSignupMail(signupRequestDto);

        return ResponseEntity.ok().body(
                ResultResponse.builder()
                        .success("true")
                        .status(HttpStatus.OK.value())
                        .message("인증 메일을 보냈습니다.")
                        .build()
        );
    }

    @GetMapping("/signup/confirm")
    public void signupConfirm(@RequestParam("key")String key, HttpServletResponse response) throws IOException {

        userService.registerUser(key);

        // TODO: 인증 완료 후 로그인 페이지로
        response.sendRedirect("http://localhost:8080/");
    }

    @PostMapping("/findPassword")
    public ResponseEntity<ResultResponse> findPassword(@RequestBody FindPasswordRequestDto findPasswordRequestDto) {

        userService.sendPasswordMail(findPasswordRequestDto.getEmail());

        return ResponseEntity.ok().body(
                ResultResponse.builder()
                        .success("true")
                        .status(HttpStatus.OK.value())
                        .message("비밀번호 변경 메일을 보냈습니다.")
                        .build()
        );
    }

    @GetMapping("/findPassword/confirm")
    public void findPasswordConfirm(@RequestParam("key")String key, HttpServletResponse response) throws IOException {

        String token = userService.getUpdatePasswordToken(key);

        // TODO: 토큰전달 어떻게?
        // TODO: 비밀번호 변경 페이지로
        response.sendRedirect("http://localhost:8080/" + token);
    }

    @PostMapping("/updatePassword")
    public ResponseEntity<ResultResponse> update(
            HttpServletRequest request,
            @RequestBody UpdatePasswordRequestDto updatePasswordRequestDto
    ) {

        String accessToken = jwtUtil.getToken(request);
        if(!jwtUtil.isAccessToken(accessToken)) throw new UnauthorizedException();

        String email = jwtUtil.getClaims(accessToken).getSubject();
        String password = updatePasswordRequestDto.getPassword();
        userService.updatePassword(email, password);

        return ResponseEntity.ok().body(
                ResultResponse.builder()
                        .success("true")
                        .status(HttpStatus.OK.value())
                        .message("비밀번호가 변경되었습니다.")
                        .build()
        );
    }

    @GetMapping("/refresh")
    public ResponseEntity<ResultResponse> refreshToken(HttpServletRequest request) {

        String refreshToken = jwtUtil.getToken(request);
        if(!jwtUtil.isRefreshToken(refreshToken)) throw new UnauthorizedException();

        TokenResponseDto tokenResponseDto = userService.refreshToken(refreshToken);
        return ResponseEntity.ok().body(
                ResultResponse.builder()
                        .success("true")
                        .status(HttpStatus.OK.value())
                        .message("새로운 Access Token이 발급되었습니다.")
                        .data(tokenResponseDto)
                        .build()
        );
    }

}