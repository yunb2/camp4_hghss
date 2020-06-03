package com.smilegate.auth.repository;

import com.smilegate.auth.domain.User;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.session.SqlSession;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;

@Slf4j
@Repository
public class UserRepositoryImpl implements UserRepository {

    private final SqlSession session;

    public UserRepositoryImpl(SqlSession session) {
        this.session = session;
    }

    @Override
    public User findByEmail(String email) {
        return session.selectOne("user.findByEmail", email);
    }

    @Override
    public int countUser(String email) {
        return session.selectOne("user.countUser", email);
    }

    @Override
    public int registerUser(User user) {
        session.insert("user.signup", user);
        return user.getId();
    }

    @Override
    public List<User> findUsers() {
        return session.selectList("user.findUsers");
    }

    @Override
    public String getNickname(int id) {
        return session.selectOne("user.getNickname", id);
    }

    @Override
    public int updatePassword(String email, String passwd, String updatedAt) {
        HashMap<String, String> map = new HashMap<>();
        map.put("email", email);
        map.put("passwd", passwd);
        map.put("updatedAt", updatedAt);
        return session.update("user.updatePassword", map);
    }

    @Override
    public int updateNickname(int userId, String nickname, String updatedAt) {
        HashMap<String, String> map = new HashMap<>();
        map.put("id", String.valueOf(userId));
        map.put("nickname", nickname);
        map.put("updatedAt", updatedAt);
        return session.update("user.updateNickname", map);
    }

    @Override
    public int updateRole(int id, int role, String updatedAt) {
        HashMap<String, String> map = new HashMap<>();
        map.put("id", String.valueOf(id));
        map.put("role", String.valueOf(role));
        map.put("updatedAt", updatedAt);
        return session.update("user.updateRole",map);
    }

    @Override
    public int updateAccessedAt(Integer id, String accessedAt) {
        log.info("=== repo");
        HashMap<String, String> map = new HashMap<>();
        map.put("id", String.valueOf(id));
        map.put("accessedAt", accessedAt);
        return session.update("user.updateAccessedAt", map);
    }

}
