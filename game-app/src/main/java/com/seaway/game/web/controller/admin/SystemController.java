package com.seaway.game.web.controller.admin;

import com.seaway.game.common.entity.ResponseEntity;
import com.seaway.game.common.entity.system.ProcessInfo;
import com.seaway.game.common.utils.Constants;
import com.seaway.game.system.manager.SystemManager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/admin/system")
@PreAuthorize("hasRole('" + Constants.ROLE_ADMIN + "')")
public class SystemController {

    private final SystemManager systemManager;

    @Autowired
    public SystemController(SystemManager systemManager) {
        this.systemManager = systemManager;
    }

    @RequestMapping(value = "/process/refresh", method = RequestMethod.GET)
    public List<ProcessInfo> getProcesses() {
        return systemManager.getProcesses();
    }

    @RequestMapping(value = "/process/restart", method = RequestMethod.GET)
    public ResponseEntity restart(@RequestParam(value = "name") String name) {
        return systemManager.restart(name);
    }

    @RequestMapping(value = "/reboot", method = RequestMethod.GET)
    public ResponseEntity reboot() {
        return systemManager.reboot();
    }

}
