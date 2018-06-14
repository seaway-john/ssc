package com.seaway.game.admin.repository;

import com.seaway.game.common.entity.mysql.Authority;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AuthorityRepository extends JpaRepository<Authority, Integer> {

    int countByUsernameAndRoleName(String username, String roleName);

    List<Authority> findByUsername(String username);

    void deleteByUsernameIn(List<String> usernames);

}
