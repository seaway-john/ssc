package com.seaway.game.system.manager;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.seaway.game.common.entity.ResponseEntity;
import com.seaway.game.common.entity.system.FileInfo;
import com.seaway.game.common.entity.system.ImageInfo;
import com.seaway.game.common.utils.Constants;
import com.seaway.game.common.utils.FileUtils;
import com.seaway.game.common.utils.ScriptHelper;

import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.OutputStream;
import java.util.List;

@Slf4j
@Component
public class UpgradeManager extends FileUtils {

    private final String directory = Constants.UPGRADE_FOLDER;

    public List<FileInfo> getList() {
        return getFileInfo(directory);
    }

    public ImageInfo getImageInfo() {
        String imageContent = getFileContent(Constants.IMAGE_INFO_FILE);
        if (StringUtils.isEmpty(imageContent)) {
            return null;
        }

        Gson gson = new Gson();
        ImageInfo imageInfo = null;
        try {
            imageInfo = gson.fromJson(imageContent, ImageInfo.class);
            imageInfo.setSystemDate(System.currentTimeMillis());
        } catch (JsonSyntaxException e) {
            log.error("JsonSyntaxException in getImageInfo, reason {}", e.getMessage());
        }

        return imageInfo;
    }


    public void delete(List<FileInfo> files) {
        if (files == null || files.isEmpty()) {
            return;
        }

        files.forEach(file -> {
            String filePath = directory + file.getFileName();
            deleteFile(filePath);
        });
    }

    public void download(String fileName, OutputStream os) {
        String filePath = directory + fileName;
        downloadFile(filePath, os);
    }

    public ResponseEntity upload(String fileName, byte[] bytes) {
        String filePath = directory + fileName;
        return uploadFile(filePath, bytes);
    }

    public ResponseEntity upgrade(FileInfo file) {
        ResponseEntity response = new ResponseEntity();

        try {
            String filePath = directory + file.getFileName();
            String ret = ScriptHelper.execScript(ScriptHelper.getScriptPath("check-upgrade.sh"), filePath);
            
            response.setStatus(true);
            response.setMessage(ret);
        } catch (Exception e) {
            log.error("Exception in upgrade {}, reason {}", file.getFileName(), e.getMessage());
            response.setMessage("Exception in upgrade " + file.getFileName() + ", reason " + e.getMessage());
        }

        return response;
    }
}
