package com.seaway.game.admin.repository;

import com.seaway.game.common.entity.mysql.User;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {

    List<User> findByRoleNameInOrderByRoleNameAscUsernameAsc(List<String> roleNames);

    User findByUsername(String username);

    int countByUsername(String username);

    void deleteByUsernameIn(List<String> usernames);

}
