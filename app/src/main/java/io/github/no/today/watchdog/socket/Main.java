package io.github.no.today.watchdog.socket;

public class Main {

    public static void main(String[] args) {
        String host = parseHost(args);
        int port = parsePort(args);

        System.out.println("Connect to: [" + host + ":" + port + "]");
        new SocketClient(host, port).start();
    }

    private static int parsePort(String[] args) {
        try {
            return Integer.parseInt(args[1]);
        } catch (Throwable e) {
            return 20300;
        }
    }

    private static String parseHost(String[] args) {
        try {
            return args[0];
        } catch (Throwable e) {
            return "127.0.0.1";
        }
    }
}