package com.seaway.game.system.manager;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.seaway.game.common.entity.ResponseEntity;
import com.seaway.game.common.entity.system.ProcessInfo;
import com.seaway.game.common.utils.ScriptHelper;

import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class SystemManager {
    public List<ProcessInfo> getProcesses() {
        List<ProcessInfo> processes = new ArrayList<>();

        try {
            String json = ScriptHelper.execScript(ScriptHelper.getScriptPath("process-info.sh"));

            Gson gson = new Gson();
            JsonParser jsonParser = new JsonParser();
            JsonArray jsonArr = jsonParser.parse(json).getAsJsonArray();
            jsonArr.forEach((JsonElement jsonElement) -> {
                ProcessInfo process = gson.fromJson(jsonElement, ProcessInfo.class);
                processes.add(process);
            });
        } catch (Exception e) {
            log.error("Exception in getProcesses, reason {}", e.getMessage());
        }

        return processes;
    }

    public ResponseEntity restart(String name) {
        ResponseEntity response = new ResponseEntity();
        try {
            String ret = ScriptHelper.execScript("service", name, "restart");

            response.setStatus(true);
            response.setMessage(ret);
        } catch (Exception e) {
            log.error("Exception in restart {}, reason {}", name, e.getMessage());
            response.setMessage("Exception in restart " + name + ", reason " + e.getMessage());
        }

        return response;
    }

    public ResponseEntity reboot() {
        ResponseEntity response = new ResponseEntity();
        try {
            String ret = ScriptHelper.execScript("shutdown", "-r", "now");

            response.setStatus(true);
            response.setMessage(ret);
        } catch (Exception e) {
            log.error("Exception in reboot, reason {}", e.getMessage());
            response.setMessage("Exception in reboot, reason " + e.getMessage());
        }

        return response;
    }
}
