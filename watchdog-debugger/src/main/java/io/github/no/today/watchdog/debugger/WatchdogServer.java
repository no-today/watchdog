package io.github.no.today.watchdog.debugger;

import com.alibaba.fastjson2.JSON;
import io.github.no.today.socket.remoting.RemotingServer;
import io.github.no.today.socket.remoting.core.SocketRemotingServer;
import io.github.no.today.socket.remoting.core.supper.RemotingUtil;
import io.github.no.today.socket.remoting.protocol.RemotingCommand;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author no-today
 * @date 2024/02/27 22:29
 */
public class WatchdogServer {

    @Getter
    private final RemotingServer server;

    @Setter
    @Getter
    private String peer;

    @Setter
    @Getter
    private String dataSaveDir = "./data";

    public WatchdogServer(int port) {
        server = new SocketRemotingServer(port);
        new Thread(server::start).start();
    }

    public void shutdown() {
        server.shutdown();
    }

    @Data
    static class ExecResult {
        private String cmd;
        private byte[] bytes;
        private String info;
        private String error;
    }

    public String exec(String cmd) {
        RemotingCommand response = server.syncRequest(peer, RemotingCommand.request(0, cmd.getBytes()), 60000);
        if (!response.isSuccess()) {
            return "Exec failed: " + response.getMessage();
        } else {
            return JSON.parseArray(response.getBody(), ExecResult.class).stream().map(this::handleResult).collect(Collectors.joining("\n"));
        }
    }

    private String handleResult(ExecResult e) {
        try {
            if (e.getError() != null) {
                return "Exec [" + e.getCmd() + "] failed: " + e.getError();
            }

            if (Objects.nonNull(dataSaveDir) && Objects.nonNull(e.getBytes()) && e.getBytes().length > 0) {
                mkdirs();

                Path path = Paths.get(dataSaveDir, e.getInfo());
                Files.write(path, e.getBytes());
                return path.toAbsolutePath().toString();
            }

            if (e.getInfo() != null) {
                return e.getInfo();
            }
        } catch (IOException ex) {
            return "save data failed: " + RemotingUtil.exceptionSimpleDesc(ex);
        }

        return null;
    }

    private void mkdirs() {
        File file = Paths.get(dataSaveDir).toFile();
        if (!file.exists()) file.mkdirs();
    }
}