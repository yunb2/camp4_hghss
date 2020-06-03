package com.smilegate.auth.service;

import com.smilegate.auth.domain.User;

import java.util.List;

public interface AdminService {
    List<User> getUsers();
    boolean updateUserRole(int id, int role);
}
