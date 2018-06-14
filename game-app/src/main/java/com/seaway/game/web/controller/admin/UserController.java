package com.seaway.game.web.controller.admin;

import com.seaway.game.admin.repository.impl.UserManager;
import com.seaway.game.common.entity.admin.UserSetting;
import com.seaway.game.common.entity.mysql.Role;
import com.seaway.game.common.entity.mysql.User;
import com.seaway.game.common.utils.Constants;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin/user")
@PreAuthorize("hasRole('" + Constants.ROLE_ADMIN + "')")
public class UserController {

	private final UserManager userManager;

	@Autowired
	public UserController(UserManager userManager) {
		this.userManager = userManager;
	}

	@PreAuthorize("hasRole('" + Constants.ROLE_ADMIN + "')")
	@RequestMapping(value = "/refresh", method = RequestMethod.GET)
	public List<User> getList(Principal principal) {
		return userManager.getList(principal.getName());
	}

	@PreAuthorize("hasRole('" + Constants.ROLE_ADMIN + "')")
	@RequestMapping(value = "/role/refresh", method = RequestMethod.GET)
	public List<Role> getRoleList(Principal principal) {
		return userManager.getRoleList(principal.getName());
	}

	@PreAuthorize("hasRole('" + Constants.ROLE_ADMIN + "')")
	@RequestMapping(value = "/add", method = RequestMethod.POST)
	public void add(@RequestBody User user, Principal principal) {
		userManager.add(user, principal.getName());
	}

	@PreAuthorize("hasRole('" + Constants.ROLE_ADMIN + "')")
	@RequestMapping(value = "/edit", method = RequestMethod.POST)
	public void update(@RequestBody User user, Principal principal) {
		userManager.update(user, principal.getName());
	}

	@PreAuthorize("hasRole('" + Constants.ROLE_ADMIN + "')")
	@RequestMapping(value = "/delete", method = RequestMethod.POST)
	public void delete(@RequestBody List<User> users, Principal principal) {
		userManager.delete(users, principal.getName());
	}

	@RequestMapping(value = "/change-password", method = RequestMethod.POST)
	public boolean changePassword(@RequestBody UserSetting userSetting,
			Principal principal) {
		return userManager.changePassword(userSetting, principal.getName());
	}

	@PreAuthorize("hasRole('" + Constants.ROLE_ADMIN + "')")
	@RequestMapping(value = "/unique", method = RequestMethod.GET)
	public Map<String, Object> unique(@RequestParam("name") String name) {
		Map<String, Object> map = new HashMap<>();
		map.put("isUnique", userManager.checkUnique(name));

		return map;
	}

}
