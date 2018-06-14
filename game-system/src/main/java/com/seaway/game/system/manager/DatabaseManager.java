package com.seaway.game.system.manager;

import com.seaway.game.common.entity.ResponseEntity;
import com.seaway.game.common.entity.system.FileInfo;
import com.seaway.game.common.utils.Constants;
import com.seaway.game.common.utils.FileUtils;
import com.seaway.game.common.utils.ScriptHelper;

import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;

import java.io.OutputStream;
import java.util.List;

@Slf4j
@Component
public class DatabaseManager extends FileUtils {

	private final String directory = Constants.DATABASE_BACKUP_FOLDER;

	public List<FileInfo> getList() {
		return getFileInfo(directory);
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

	public ResponseEntity backup() {
		ResponseEntity response = new ResponseEntity();

		try {
			String ret = ScriptHelper.execScript(ScriptHelper
					.getScriptPath("database-backup.sh"));

			response.setStatus(true);
			response.setMessage(ret);
		} catch (Exception e) {
			log.error("Exception in backup, reason {}", e.getMessage());
			response.setMessage("Exception in backup, reason " + e.getMessage());
		}

		return response;
	}

	public ResponseEntity restore(FileInfo file) {
		ResponseEntity response = new ResponseEntity();

		try {
			String filePath = directory + file.getFileName();
			String ret = ScriptHelper
					.execScript(
							ScriptHelper.getScriptPath("database-restore.sh"),
							filePath);

			response.setStatus(true);
			response.setMessage(ret);
		} catch (Exception e) {
			log.error("Exception in restore {}, reason {}", file.getFileName(),
					e.getMessage());
			response.setMessage("Exception in restore " + file.getFileName()
					+ ", reason " + e.getMessage());
		}

		return response;
	}
}
