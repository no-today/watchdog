package io.github.no.today.watchdog.socket

import io.github.no.today.socket.remoting.core.supper.RemotingUtil
import io.github.no.today.watchdog.WatchdogHelper
import io.github.no.today.watchdog.helper.ScreenHelper
import io.github.no.today.watchdog.helper.ShellHelper
import io.github.no.today.watchdog.helper.TouchHelper
import java.lang.Thread.sleep
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

enum class Cmd(val help: String, val parser: (args: List<String>) -> Command) {
    Help(
        """
            指令可以用分号隔开组合成使用
            
            从下往上滑动: 
                down 500 1500;move 500 1000;up 500 1000
            从上往下滑动: 
                down 500 1000;move 500 1500;up 500 1500
            从左往右滑动: 
                down 500 1000;move 700 1000;up 700 1000
            从右往左滑动: 
                down 500 1000;move 300 1000;up 300 1000
            
            点亮屏幕手势解锁: 
                key 224 1000
                down 550 1850;move 550 1200;up 550 1200 1000
                down 550 1200;move 550 1780;move 830 1780;up 830 1780 1000
                capture
        """.trimIndent(),
        { args -> Help(args.getOrNull(1)) }),

    Key(
        """
            Usage: key <keycode> [sleep]
            
            The keycode are:
                3	HOME 键
                4	返回键
                5	打开拨号应用
                6	挂断电话
                24	增加音量
                25	降低音量
                26	电源键
                27	拍照（需要在相机应用里）
                64	打开浏览器
                82	菜单键
                85	播放/暂停
                86	停止播放
                87	播放下一首
                88	播放上一首
                122	移动光标到行首或列表顶部
                123	移动光标到行末或列表底部
                126	恢复播放
                127	暂停播放
                164	静音
                176	打开系统设置
                187	切换应用
                207	打开联系人
                208	打开日历
                209	打开音乐
                210	打开计算器
                220	降低屏幕亮度
                221	提高屏幕亮度
                223	系统休眠
                224	点亮屏幕
                231	打开语音助手
                276	如果没有 wakelock 则让系统休眠
        """.trimIndent(),
        { args -> Key(args[1].toInt(), parseSleep(args, 2)) }),

    Down(
        """
            Usage: down <x> <y> [sleep]
        """.trimIndent(),
        { args -> Down(x = args[1].toFloat(), y = args[2].toFloat(), parseSleep(args, 3)) }),

    Move(
        """
            Usage: move <x> <y> [sleep]
        """.trimIndent(),
        { args -> Move(x = args[1].toFloat(), y = args[2].toFloat(), parseSleep(args, 3)) }),

    Up(
        """
            Usage: move <x> <y> [sleep]
        """.trimIndent(),
        { args -> Up(x = args[1].toFloat(), y = args[2].toFloat(), parseSleep(args, 3)) }),

    Capture(
        """
            Usage: capture [test]
        """.trimIndent(),
        { args -> Capture(args.getOrNull(1)) }),

    AdbShell(
        """
            Usage: adb shell <cmd...>
        """.trimIndent(),
        { args -> AdbShell(args.joinToString(" ")) });

    companion object {
        fun of(cmd: String): Cmd {
            try {
                return valueOf(cmd.capitalize())
            } catch (e: Exception) {
                if (cmd == "adb") {
                    return AdbShell;
                }
                throw RuntimeException("Unsupported command: $cmd")
            }
        }

        fun parse(args: List<String>) = of(args[0]).parser(args)
        private fun parseSleep(args: List<String>, index: Int) = args.getOrNull(index)?.toLong()
    }
}


// --------------------------------------------------

abstract class Command(val cmd: Cmd, val sleep: Long? = null) {
    abstract fun run(): ExecResult?

    fun exec(): ExecResult? {
        return try {
            val result = run()
            sleep?.let { sleep(it) }
            result
        } catch (e: Exception) {
            ExecResult(cmd, null, RemotingUtil.exceptionSimpleDesc(e))
        }
    }

    protected fun filename(suffix: String) =
        "${cmd}-${LocalDateTime.now().format(formatter)}.$suffix"
}

// 特殊指令
class Help(val c: String?) : Command(Cmd.Help) {
    override fun run(): ExecResult {
        return if (c.isNullOrBlank()) {
            ExecResult.success(cmd, cmd.help)
        } else {
            ExecResult.success(cmd, Cmd.of(c).help)
        }
    }
}

class AdbShell(val shell: String) : Command(Cmd.AdbShell) {
    override fun run(): ExecResult? {
        var exec = shell.replace("  ", " ")
        if (!exec.startsWith("adb shell")) {
            return ExecResult.failure(cmd, "Invalid command")
        }
        exec = exec.replace("adb shell", "")
        val result = ShellHelper.execCommand(exec, false)
        return if (result.result == 0) {
            ExecResult.success(cmd, result.successMsg)
        } else {
            ExecResult.failure(cmd, result.errorMsg)
        }
    }
}

// 移动
class Down(val x: Float, val y: Float, sleep: Long?) : Command(Cmd.Down, sleep) {
    override fun run(): ExecResult? {
        TouchHelper.touchDown(x, y)
        return null
    }
}

class Move(val x: Float, val y: Float, sleep: Long?) : Command(Cmd.Move, sleep) {
    override fun run(): ExecResult? {
        TouchHelper.touchMove(x, y)
        return null
    }
}


class Up(val x: Float, val y: Float, sleep: Long?) : Command(Cmd.Up, sleep) {
    override fun run(): ExecResult? {
        TouchHelper.touchUp(x, y)
        return null
    }
}

// 按键
class Key(val key: Int, sleep: Long?) : Command(Cmd.Key, sleep) {
    override fun run(): ExecResult? {
        TouchHelper.entryKey(key)
        return null
    }
}

// 功能
class Capture(val arg: String?) : Command(Cmd.Capture) {
    override fun run(): ExecResult? {
        val data: ByteArray? = if ("test" == arg) {
            WatchdogHelper.createBlankImage(800, 600)
        } else {
            ScreenHelper.screenshot();
        }

        if (data != null) return ExecResult.success(cmd, data, filename("png"))
        return null
    }
}

// --------------------------------------------------

class ExecResult(
    val cmd: Cmd,
    val bytes: ByteArray?,

    val info: String? = null,
    val error: String? = null,
) {
    companion object {
        fun success(cmd: Cmd, bytes: ByteArray?, info: String?): ExecResult =
            ExecResult(cmd, bytes, info, null)

        fun success(cmd: Cmd, info: String): ExecResult = ExecResult(cmd, null, info, null)
        fun failure(cmd: Cmd, error: String): ExecResult = ExecResult(cmd, null, null, error)
    }
}