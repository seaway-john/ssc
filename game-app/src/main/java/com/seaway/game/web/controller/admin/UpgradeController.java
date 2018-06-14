package com.seaway.game.web.controller.admin;

import com.seaway.game.common.entity.ResponseEntity;
import com.seaway.game.common.entity.system.FileInfo;
import com.seaway.game.common.entity.system.ImageInfo;
import com.seaway.game.common.utils.Constants;
import com.seaway.game.system.manager.UpgradeManager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/admin/upgrade")
@PreAuthorize("hasRole('" + Constants.ROLE_ADMIN + "')")
public class UpgradeController {

    private final UpgradeManager upgradeManager;

    @Autowired
    public UpgradeController(UpgradeManager upgradeManager) {
        this.upgradeManager = upgradeManager;
    }

    @RequestMapping(value = "/refresh", method = RequestMethod.GET)
    public List<FileInfo> getList() {
        return upgradeManager.getList();
    }

    @RequestMapping(value = "/image-info", method = RequestMethod.GET)
    public ImageInfo getImageInfo() {
        return upgradeManager.getImageInfo();
    }

    @RequestMapping(value = "/delete", method = RequestMethod.POST)
    public void delete(@RequestBody List<FileInfo> files) {
        upgradeManager.delete(files);
    }

    @RequestMapping(value = "/download", method = RequestMethod.GET)
    public void download(HttpServletResponse response,
                         @RequestParam(value = "fileName") String fileName) throws IOException {
        response.setHeader("Content-disposition", "attachment;fileName=" + fileName);

        upgradeManager.download(fileName, response.getOutputStream());
    }

    @RequestMapping(value = "/upload", method = RequestMethod.POST)
    public ResponseEntity upload(@RequestParam("file") MultipartFile file) throws IOException {
        return upgradeManager.upload(file.getOriginalFilename(), file.getBytes());
    }

    @RequestMapping(value = "/upgrade", method = RequestMethod.POST)
    public ResponseEntity upgrade(@RequestBody FileInfo file) {
        return upgradeManager.upgrade(file);
    }

}
