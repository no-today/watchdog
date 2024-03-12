package io.github.no.today.watchdog.socket

import io.github.no.today.socket.remoting.RemotingClient
import io.github.no.today.socket.remoting.core.SocketRemotingClient
import io.github.no.today.socket.remoting.core.supper.RemotingUtil.exceptionSimpleDesc
import io.github.no.today.socket.remoting.protocol.RemotingCommand
import io.github.no.today.watchdog.WatchdogHelper.JSON


class SocketClient(private val host: String, private val port: Int) {
    private lateinit var client: RemotingClient

    fun start() {
        try {
            client = SocketRemotingClient(host, port, true)

            // 全部通过默认处理器进行处理
            client.registerDefaultProcessor { _, req ->
                val line = String(req.body)
                println("Exec cmds: $line")

                val results: List<ExecResult> = line.split(";")
                    .map { Cmd.parse(it.split(" ")) }
                    .mapNotNull { it.exec() }

                RemotingCommand.success(JSON.toJson(results).toByteArray())
            }

            client.connect()
        } catch (e: Exception) {
            println("Connect failure, ${exceptionSimpleDesc(e)}")
            client.disconnect()
        }
    }

    fun asyncStart() {
        Thread { start() }.start()
    }
}