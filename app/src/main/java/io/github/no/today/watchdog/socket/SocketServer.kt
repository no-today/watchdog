package io.github.no.today.watchdog.socket

import com.google.gson.reflect.TypeToken
import io.github.no.today.socket.remoting.RemotingServer
import io.github.no.today.socket.remoting.core.SocketRemotingServer
import io.github.no.today.socket.remoting.protocol.RemotingCommand
import io.github.no.today.watchdog.WatchdogHelper.JSON
import java.lang.Thread.sleep
import java.util.*

/**
 * 一个简单的例子, 上下滑动
 */
class SocketServer(port: Int = 20300) {
    var server: RemotingServer = SocketRemotingServer(port);

    fun asyncStart() {
        Thread { server.start() }.start()
        Thread {
            while (true) {
                server.channels.parallelStream()
                    .forEach { exec("down 500 1000;move 500 1500;up 500 1500", it) }

                sleep(2000)
                server.channels.parallelStream()
                    .forEach { exec("down 500 1500;move 500 1000;up 500 1000", it) }

                sleep(2000)
                server.channels.parallelStream().forEach { exec("capture test", it) }
            }
        }.start()
    }

    fun shutdown() {
        server.shutdown()
    }

    data class ExecResult(
        var cmd: String,
        var bytes: ByteArray?,
        val info: String?,
        var error: String?
    )

    fun exec(cmd: String, peer: String) {
        val response =
            server.syncRequest(peer, RemotingCommand.request(0, cmd.toByteArray()), 60000)
        if (!response.isSuccess) {
            System.err.println("Exec failed: ${response.message}")
        } else {
            val results: List<ExecResult> = JSON.fromJson(
                String(response.body),
                object : TypeToken<List<ExecResult?>?>() {}.type
            )

            results.stream()
                .filter(Objects::nonNull)
                .forEach { e: ExecResult -> this.handleResult(e) }
        }
    }

    private fun handleResult(e: ExecResult) {
        if (e.error != null) {
            System.err.println("Exec [${e.cmd}] failed: ${e.error}")
        } else {
            println("Exec [${e.cmd}] response data: ${e.info}, ${e.bytes?.size}Bytes, info: ${e.info}")
        }
    }
}