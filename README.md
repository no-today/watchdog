# Watchdog

通过 adb app_process 后台运行 Socket 接收指令，从而控制 Android 设备。

## Usage

```shell
# 推送包到设备
adb push app/release/app-release.apk /data/local/tmp

# app_process 启动后台进程
adb shell app_process -Djava.class.path=/data/local/tmp/app-release.apk /system/bin io.github.no.today.watchdog.socket.Main

# 可以将进程后台挂起, 终端可以关闭, 但是把USB线拔掉进程就挂了
nohup adb shell app_process -Djava.class.path=/data/local/tmp/app-release.apk /system/bin io.github.no.today.watchdog.socket.Main 192.168.31.229 &
```

## Commands

- Help: 帮助
    - `Usage: help [cmd]`
- Capture: 截屏
    - `Usage: capture [test]`
- AdbShell: 执行AdbShell
    - `Usage: adb shell <cmd...>`
- Key: 按键
    - `Usage: key <keycode> [sleep]`
- Down\Move\Up: 按下\移动\抬起
    - `Usage: down\move\up <x> <y> [sleep]`

### Examples

```shell
# 从下往上滑动
down 500 1500;move 500 1000;up 500 1000

# 从上往下滑动
down 500 1000;move 500 1500;up 500 1500

# 从左往右滑动
down 500 1000;move 700 1000;up 700 1000

# 从右往左滑动
down 500 1000;move 300 1000;up 300 1000

# 点亮屏幕后等待1秒
key 224 1000
# 从下往上滑动后等待1秒 进入手势解锁界面
down 550 1850;move 550 1200;up 550 1200 1000
# 手势解锁 L形状 先向下滑动 再向右滑动 完成后等待1秒
down 550 1200;move 550 1780;move 830 1780;up 830 1780 1000
# 截屏
capture

# 获取屏幕分辨率
adb shell wm size
```

### Keycode

- 3	    HOME 键
- 4	    返回键
- 5	    打开拨号应用
- 6	    挂断电话
- 24	增加音量
- 25	降低音量
- 26	电源键
- 27	拍照（需要在相机应用里）
- 64	打开浏览器
- 82	菜单键
- 85	播放/暂停
- 86	停止播放
- 87	播放下一首
- 88	播放上一首
- 122	移动光标到行首或列表顶部
- 123	移动光标到行末或列表底部
- 126	恢复播放
- 127	暂停播放
- 164	静音
- 176	打开系统设置
- 187	切换应用
- 207	打开联系人
- 208	打开日历
- 209	打开音乐
- 210	打开计算器
- 220	降低屏幕亮度
- 221	提高屏幕亮度
- 223	系统休眠
- 224	点亮屏幕
- 231	打开语音助手
- 276	如果没有 wakelock 则让系统休眠

## Dependencies

- [socket-remoting](https://github.com/no-today/socket-remoting)

## References

- [ADB 用法大全](https://github.com/mzlogin/awesome-adb)
- [利用 app_process 实现免 root 调用 shell](https://github.com/gtf35/app_process-shell-use)
- [Android投屏及控制](https://github.com/android-notes/androidScreenShare)