package com.smilegate.auth.service;

import com.smilegate.auth.domain.User;
import com.smilegate.auth.dto.response.TokenResponseDto;
import com.smilegate.auth.repository.UserRepository;
import com.smilegate.auth.utils.JwtUtil;
import com.smilegate.auth.utils.RedisUtil;
import lombok.RequiredArgsConstructor;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;

@RequiredArgsConstructor
@Service
public class OAuth2Service {

    @Value("${oauth2.naver.client-id}")
    private String clientId;

    @Value("${oauth2.naver.client-secret}")
    private String clientSecret;

    private final UserRepository userRepository;
    private final RedisUtil redisUtil;
    private final JwtUtil jwtUtil;

    public TokenResponseDto createToken(String code) {

        String token = getAccessToken(code);
        String email = getEmail(token);
        String nickname = "temp";
        User user = userRepository.findByEmail(email);
        if(user == null) {
            String now = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
            user = User.builder()
                    .email(email)
                    .passwd(null)
                    .nickname(nickname)
                    .role(1)
                    .status(2)
                    .createdAt(now)
                    .updatedAt(now)
                    .accessedAt(now)
                    .build();

            int userId = userRepository.registerUser(user);
            nickname = userRepository.getNickname(userId);
            userRepository.updateNickname(userId, nickname, now);
            user.setNickname(nickname);
        }

        String accessToken = jwtUtil.createToken(user.getId(), user.getEmail(), user.getNickname(), user.getRole(), "ACCESS_TOKEN", 30);
        String refreshToken = jwtUtil.createToken(user.getId(), user.getEmail(), user.getNickname(), user.getRole(), "REFRESH_TOKEN", 60*24*14);

        redisUtil.set(refreshToken, user.getRole(), 60*24*14);

        return TokenResponseDto.builder()
                    .id(user.getId())
                    .email(email)
                    .nickname(user.getNickname())
                    .role(user.getRole())
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .build();

    }

    private String getAccessToken(String code) {

        String accessToken = null;

        StringBuilder apiURL = new StringBuilder("https://nid.naver.com/oauth2.0/token");
        apiURL.append("?client_id=").append(this.clientId);
        apiURL.append("&client_secret=").append(this.clientSecret);
        apiURL.append("&grant_type=authorization_code&state=123");
        apiURL.append("&code=").append(code);

        try {
            URL url = new URL(apiURL.toString());
            HttpURLConnection con = (HttpURLConnection)url.openConnection();
            con.setRequestMethod("GET");

            int responseCode = con.getResponseCode();
            BufferedReader br;
            if(responseCode==200) {
                br = new BufferedReader(new InputStreamReader(con.getInputStream()));
            } else {
                br = new BufferedReader(new InputStreamReader(con.getErrorStream()));
            }

            StringBuffer response = new StringBuffer();
            String inputLine;
            while ((inputLine = br.readLine()) != null) {
                response.append(inputLine);
            }
            br.close();

            JSONObject json = new JSONObject(response.toString());
            accessToken = (String) json.get("access_token");

        }catch (Exception e) {
            e.printStackTrace();
        }

        return accessToken;
    }

    private String getEmail(String accessToken) {

        String email = null;
        String header = "Bearer " + accessToken;

        try {
            String apiURL = "https://openapi.naver.com/v1/nid/me";
            URL url = new URL(apiURL);
            HttpURLConnection con = (HttpURLConnection)url.openConnection();
            con.setRequestMethod("GET");
            con.setRequestProperty("Authorization", header);
            int responseCode = con.getResponseCode();

            BufferedReader br;
            if(responseCode==200) {
                br = new BufferedReader(new InputStreamReader(con.getInputStream()));
            } else {
                br = new BufferedReader(new InputStreamReader(con.getErrorStream()));
            }

            String inputLine;
            StringBuilder response = new StringBuilder();
            while ((inputLine = br.readLine()) != null) {
                response.append(inputLine);
            }
            br.close();

            JSONObject json = new JSONObject(response.toString());
            email = json.getJSONObject("response").getString("email");

        } catch (Exception e) {
            e.printStackTrace();
        }

        return email;
    }

}
