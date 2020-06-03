package com.smilegate.auth.service;

import com.smilegate.auth.domain.User;
import com.smilegate.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@RequiredArgsConstructor
@Service
public class AdminServiceImpl implements AdminService {

    private final UserRepository userRepository;

    @Override
    public List<User> getUsers() {
        return userRepository.findUsers();
    }

    @Override
    public boolean updateUserRole(int id, int role) {
        userRepository.updateRole(id, role, new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
        return true;
    }
}
