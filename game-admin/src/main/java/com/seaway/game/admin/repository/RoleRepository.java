package com.seaway.game.admin.repository;

import com.seaway.game.common.entity.mysql.Role;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoleRepository extends JpaRepository<Role, Integer> {

    List<Role> findByRoleNameInOrderByRoleNameAsc(List<String> roleNames);

}
