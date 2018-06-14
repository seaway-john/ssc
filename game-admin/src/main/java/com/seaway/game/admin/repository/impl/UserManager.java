package com.seaway.game.admin.repository.impl;

import com.seaway.game.admin.repository.AuthorityRepository;
import com.seaway.game.admin.repository.RoleRepository;
import com.seaway.game.admin.repository.UserRepository;
import com.seaway.game.common.entity.admin.UserSetting;
import com.seaway.game.common.entity.mysql.Authority;
import com.seaway.game.common.entity.mysql.Role;
import com.seaway.game.common.entity.mysql.User;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Slf4j
@Component
public class UserManager {

    private final UserRepository userRepository;

    private final RoleRepository roleRepository;

    private final AuthorityRepository authorityRepository;

    private final String ROLE_PREFIX = "ROLE_";

    @Autowired
    public UserManager(UserRepository userRepository, RoleRepository roleRepository, AuthorityRepository authorityRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.authorityRepository = authorityRepository;
    }

    public List<User> getList(String username) {
        List<Authority> authorities = authorityRepository.findByUsername(username);
        if (authorities == null) {
            return new ArrayList<>();
        }

        List<String> roleNames = new ArrayList<>();
        authorities.forEach(authority -> roleNames.add(authority.getRoleName()));

        List<User> users = userRepository.findByRoleNameInOrderByRoleNameAscUsernameAsc(roleNames);
        if (users == null) {
            users = new ArrayList<>();
        }

        users.forEach(user -> user.setPassword(null));

        return users;
    }

    public List<Role> getRoleList(String username) {
        List<Authority> authorities = authorityRepository.findByUsername(username);
        if (authorities == null) {
            return new ArrayList<>();
        }

        List<String> roleNames = new ArrayList<>();
        authorities.forEach(authority -> roleNames.add(authority.getRoleName()));

        List<Role> roles = roleRepository.findByRoleNameInOrderByRoleNameAsc(roleNames);
        if (roles == null) {
            return new ArrayList<>();
        }

        return roles;
    }

    public void add(User user, String username) {
        if (!hasRole(username, user.getRoleName())) {
            log.warn("username {} don't have {} role", username, user.getRoleName());
            return;
        }

        if (!checkUnique(user.getUsername())) {
            log.warn("Exist username {}", user.getUsername());
            return;
        }

        user.setPassword(encodePassword(user.getPassword()));
        user.setCreatedBy(username);
        user.setCreated(new Date());

        userRepository.save(user);

        updateAuthority(user);
    }

    public void update(User user, String username) {
        if (!hasRole(username, user.getRoleName())) {
            log.warn("username {} don't have {} role", username, user.getRoleName());
            return;
        }

        User dbUser = userRepository.findByUsername(user.getUsername());
        if (dbUser == null) {
            log.warn("Not exist username {}", user.getUsername());
            return;
        }

        dbUser.setName(user.getName());
        dbUser.setLastUpdate(new Date());

        if (!user.getUsername().equals(username)) {
            dbUser.setEnabled(user.isEnabled());

            if (!StringUtils.isEmpty(user.getPassword())) {
                dbUser.setPassword(encodePassword(user.getPassword()));
            }

            if (!user.getRoleName().equals(dbUser.getRoleName())) {
                dbUser.setRoleName(user.getRoleName());
                updateAuthority(user);
            }
        }

        userRepository.save(dbUser);
    }

    @Transactional
    public void delete(List<User> users, String username) {
        List<Authority> authorities = authorityRepository.findByUsername(username);
        if (authorities == null || authorities.isEmpty()) {
            return;
        }

        List<String> roleNames = new ArrayList<>();
        authorities.forEach(authority -> roleNames.add(authority.getRoleName()));

        List<String> usernames = new ArrayList<>();
        for (User user : users) {
            if (user.getUsername().equals(username)) {
                log.warn("Can't delete current username {}", username);
                continue;
            }

            if (roleNames.contains(user.getRoleName())) {
                usernames.add(user.getUsername());
            }
        }

        if (!usernames.isEmpty()) {
            authorityRepository.deleteByUsernameIn(usernames);
            userRepository.deleteByUsernameIn(usernames);
        }
    }

    public boolean changePassword(UserSetting userSetting, String username) {
        if (!userSetting.getUsername().equals(username)) {
            log.warn("Can't change another username of {}", userSetting.getUsername());
            return false;
        }

        User user = getByUsername(userSetting.getUsername());
        if (user == null) {
            log.warn("Not exist username {}", userSetting.getUsername());
            return false;
        }

        if (!validatePassword(userSetting.getCurrentPwd(), user.getPassword())) {
            log.warn("Invalid password of username {}", userSetting.getUsername());
            return false;
        }

        user.setPassword(encodePassword(userSetting.getNewPwd()));
        user.setLastUpdate(new Date());

        userRepository.save(user);

        return true;
    }

    public boolean checkUnique(String name) {
        return userRepository.countByUsername(name) == 0;
    }

    public boolean hasRole(String username, String role) {
        role = encodeRoleName(role);

        return authorityRepository.countByUsernameAndRoleName(username, role) >= 1;
    }

    public String getDecodeRoleNameByUsername(String username) {
        User user = getByUsername(username);
        if (user == null) {
            return null;
        }

        return decodeRoleName(user.getRoleName());
    }

    private void updateAuthority(User user) {
        List<Role> roles = roleRepository.findAll();
        if (roles == null || roles.isEmpty()) {
            return;
        }

        boolean existRoleName = false;
        for (Role role : roles) {
            if (role.getRoleName().equals(user.getRoleName())) {
                existRoleName = true;
                break;
            }
        }

        if (!existRoleName) {
            return;
        }

        List<String> newRoleNames = new ArrayList<>();
        newRoleNames.add(user.getRoleName());

        List<String> subRoleNames = getSubRoleNames(roles, user.getRoleName());
        if (!subRoleNames.isEmpty()) {
            newRoleNames.addAll(subRoleNames);
        }

        List<Authority> authorities = authorityRepository.findByUsername(user.getUsername());
        if (authorities == null) {
            authorities = new ArrayList<>();
        }

        authorities.forEach(authority -> {
            if (!newRoleNames.remove(authority.getRoleName())) {
                authorityRepository.delete(authority);
            }
        });

        newRoleNames.forEach(newRoleName -> {
            Authority authority = new Authority();
            authority.setUsername(user.getUsername());
            authority.setRoleName(newRoleName);

            authorityRepository.save(authority);
        });
    }

    private List<String> getSubRoleNames(List<Role> roles, String parentRoleName) {
        List<String> roleNames = new ArrayList<>();

        roles.forEach(role -> {
            if (parentRoleName.equals(role.getParent())) {
                roleNames.add(role.getRoleName());

                List<String> subRoleNames = getSubRoleNames(roles, role.getRoleName());
                if (!subRoleNames.isEmpty()) {
                    roleNames.addAll(subRoleNames);
                }
            }
        });

        return roleNames;
    }

    private User getByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    private String encodeRoleName(String role) {
        if (role.startsWith(ROLE_PREFIX)) {
            return role;
        }

        return ROLE_PREFIX + role;
    }

    private String decodeRoleName(String role) {
        if (role.startsWith(ROLE_PREFIX)) {
            return role.substring(ROLE_PREFIX.length());
        }

        return role;
    }

    private String encodePassword(String password) {
        BCryptPasswordEncoder encode = new BCryptPasswordEncoder(10);
        return encode.encode(password);
    }

    private boolean validatePassword(String password, String encodePassword) {
        BCryptPasswordEncoder encode = new BCryptPasswordEncoder(10);
        return encode.matches(password, encodePassword);
    }
}
