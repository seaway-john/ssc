package com.seaway.game.web.controller.common;

import com.seaway.game.admin.menu.MenuManager;
import com.seaway.game.admin.repository.impl.UserManager;
import com.seaway.game.common.utils.Constants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/topology")
@PreAuthorize("hasRole('" + Constants.ROLE_RO + "')")
public class TopologyController {

    private final MenuManager menuManager;

    private final UserManager userManager;

    @Autowired
    public TopologyController(MenuManager menuManager, UserManager userManager) {
        this.menuManager = menuManager;
        this.userManager = userManager;
    }

    @RequestMapping(value = "/sidebar-left/map", method = RequestMethod.GET)
    public Map<String, Object> getList(Principal principal) {
        Map<String, Object> map = new HashMap<>();
        map.put("menu", menuManager.getSidebarLeft(principal.getName()));
        map.put("user", principal.getName());
        map.put("role", userManager.getDecodeRoleNameByUsername(principal.getName()));

        return map;
    }

}
