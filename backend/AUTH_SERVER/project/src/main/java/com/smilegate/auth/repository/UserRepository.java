package com.smilegate.auth.repository;

import com.smilegate.auth.domain.User;

import java.util.List;
import java.util.concurrent.Future;

public interface UserRepository {

    User findByEmail(String email);

    int countUser(String email);

    int registerUser(User user);

    List<User> findUsers();

    String getNickname(int userId);

    int updatePassword(String email, String passwd, String updatedAt);

    int updateNickname(int userId, String nickname, String updatedAt);

    int updateRole(int id, int role, String updatedAt);

    Future<Integer> updateAccessedAt(Integer id, String accessedAt);
}
