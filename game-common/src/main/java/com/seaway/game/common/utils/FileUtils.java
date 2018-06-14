package com.seaway.game.common.utils;

import com.seaway.game.common.entity.ResponseEntity;
import com.seaway.game.common.entity.system.FileInfo;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.io.IOUtils;

import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.FileTime;
import java.util.*;

@Slf4j
public class FileUtils {

    protected String getFileContent(String filePath) {
        StringBuilder sb = new StringBuilder();
        try {
            Files.lines(Paths.get(filePath)).forEach(sb::append);
        } catch (IOException e) {
            log.error("Exception in get {} content, reason {}", filePath, e.getMessage());
        }

        return sb.toString();
    }

    protected List<FileInfo> getFileInfo(String directory) {
        List<FileInfo> files = new ArrayList<>();

        File f = new File(directory);
        if (!f.exists() || !f.isDirectory()) {
            return files;
        }

        Map<String, FileInfo> map = new HashMap<>();

        DirectoryStream.Filter<Path> filter = entry -> entry.toFile().isFile();

        try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(
                Paths.get(directory), filter)) {
            for (Path path : directoryStream) {
                Map<String, Object> attrs = Files.readAttributes(path, "size,lastModifiedTime,lastAccessTime", LinkOption.NOFOLLOW_LINKS);

                Path pathFileName = path.getFileName();
                if (pathFileName != null) {
                    String fileName = pathFileName.toString();

                    FileInfo file = new FileInfo();
                    file.setFileName(fileName);
                    file.setLastModifiedTime(((FileTime) attrs.get("lastModifiedTime")).toMillis());
                    file.setSize((long) attrs.get("size"));

                    map.put(fileName, file);
                }
            }
        } catch (Exception e) {
            log.error("Exception in get file info, path {}, reason {}", directory, e.getMessage());
        }

        Object[] keys = map.keySet().toArray();
        Arrays.sort(keys);

        for (int i = 0; i < keys.length; i++) {
            files.add(map.get(keys[i]));
        }

        return files;
    }

    protected void downloadFile(String filePath, OutputStream outputStream) {
        File file = new File(filePath);
        if (!file.exists() || !file.isFile()) {
            return;
        }

        OutputStream os = null;
        InputStream is = null;

        try {
            os = new BufferedOutputStream(outputStream);
            is = new FileInputStream(file);
            byte[] b = new byte[(int) file.length()];
            is.read(b);
            os.write(b);
            os.flush();
        } catch (Exception e) {
            log.error("Exception in download {}, reason: {}", filePath, e.getMessage());
        } finally {
            IOUtils.closeQuietly(os);
            IOUtils.closeQuietly(is);
        }
    }

    protected ResponseEntity uploadFile(String filePath, byte[] bytes) {
        ResponseEntity response = new ResponseEntity();

        try {
            Path fileToWrite = Paths.get(filePath);

            Files.write(fileToWrite, bytes,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING);

            response.setStatus(true);
            response.setMessage("Success to upload file " + fileToWrite.getFileName());
        } catch (Exception e) {
            response.setMessage("Failed to upload file " + filePath + ", message: " + e.getMessage());
        }

        return response;
    }

    protected void deleteFile(String filePath) {
        try {
            Files.delete(Paths.get(filePath));
        } catch (IOException e) {
            log.error("IOException in delete file {}, reason: {}", filePath, e.getMessage());
        }
    }

}
