package com.seaway.game.web.controller.common;

import com.seaway.game.common.entity.system.FileInfo;
import com.seaway.game.common.entity.system.ImageInfo;
import com.seaway.game.common.utils.Constants;
import com.seaway.game.system.manager.UpgradeManager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/header")
@PreAuthorize("hasRole('" + Constants.ROLE_ADMIN + "')")
public class HeaderController {

    private final UpgradeManager upgradeManager;

    @Autowired
    public HeaderController(UpgradeManager upgradeManager) {
        this.upgradeManager = upgradeManager;
    }

    @RequestMapping(value = "/refresh", method = RequestMethod.GET)
    public List<FileInfo> getList() {
        return upgradeManager.getList();
    }

    @RequestMapping(value = "/version", method = RequestMethod.GET)
    public ImageInfo getVersion() {
        return upgradeManager.getImageInfo();
    }

}
