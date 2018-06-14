package com.seaway.game.web.controller.admin;

import com.seaway.game.common.entity.ResponseEntity;
import com.seaway.game.common.entity.system.FileInfo;
import com.seaway.game.common.utils.Constants;
import com.seaway.game.system.manager.DatabaseManager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/admin/database")
@PreAuthorize("hasRole('" + Constants.ROLE_ADMIN + "')")
public class DatabaseController {

    private final DatabaseManager databaseManager;

    @Autowired
    public DatabaseController(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    @RequestMapping(value = "/refresh", method = RequestMethod.GET)
    public List<FileInfo> getList() {
        return databaseManager.getList();
    }

    @RequestMapping(value = "/delete", method = RequestMethod.POST)
    public void delete(@RequestBody List<FileInfo> files) {
        databaseManager.delete(files);
    }

    @RequestMapping(value = "/download", method = RequestMethod.GET)
    public void download(HttpServletResponse response,
                         @RequestParam(value = "fileName") String fileName) throws IOException {
        response.setHeader("Content-disposition", "attachment;fileName=" + fileName);

        databaseManager.download(fileName, response.getOutputStream());
    }

    @RequestMapping(value = "/upload", method = RequestMethod.POST)
    public ResponseEntity upload(@RequestParam("file") MultipartFile file) throws IOException {
        return databaseManager.upload(file.getOriginalFilename(), file.getBytes());
    }

    @RequestMapping(value = "/backup", method = RequestMethod.GET)
    public ResponseEntity backup() {
        return databaseManager.backup();
    }

    @RequestMapping(value = "/restore", method = RequestMethod.POST)
    public ResponseEntity restore(@RequestBody FileInfo file) {
        return databaseManager.restore(file);
    }

}
