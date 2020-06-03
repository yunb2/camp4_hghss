package com.smilegate.auth.repository;

import com.smilegate.auth.domain.User;

import java.util.List;

public interface UserRepository {

    User findByEmail(String email);

    int countUser(String email);

    int registerUser(User user);

    List<User> findUsers();

    String getNickname(int userId);

    int updatePassword(String email, String passwd, String updatedAt);

    int updateNickname(int userId, String nickname, String updatedAt);

    int updateRole(int id, int role, String updatedAt);

    int updateAccessedAt(Integer id, String accessedAt);
}
