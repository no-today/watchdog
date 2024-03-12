package io.github.no.today.watchdog.debugger;

import io.github.no.today.socket.remoting.core.supper.RemotingUtil;
import org.jline.reader.EndOfFileException;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.UserInterruptException;
import org.jline.reader.impl.history.DefaultHistory;
import org.jline.terminal.TerminalBuilder;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.List;
import java.util.Objects;

/**
 * @author no-today
 * @date 2024/02/29 16:11
 */
public class WatchdogShell {
    private static final String DEBUGGER_LOG = ".watchdog.log";

    private final static int DEFAULT_PORT = 20300;

    // 保存原始的 System.out
    private static final PrintStream printer = System.out;

    static {
        try {
            System.setOut(new PrintStream(new FileOutputStream(DEBUGGER_LOG)));
            System.setErr(new PrintStream(new FileOutputStream(DEBUGGER_LOG)));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private static int getPort(String[] args) {
        try {
            return Integer.parseInt(args[0]);
        } catch (Exception e) {
            return DEFAULT_PORT;
        }
    }

    public static void main(String[] args) throws IOException {
        WatchdogServer watchdog = new WatchdogServer(getPort(args));

        LineReader lineReader = LineReaderBuilder.builder()
                .terminal(TerminalBuilder.builder().system(true).build())
                .history(new DefaultHistory())
                .build();

        String prompt = "watchdog> ";
        while (true) {
            String line;
            try {
                line = lineReader.readLine(prompt).trim();

                if ("ls".equals(line)) {
                    List<String> channels = watchdog.getServer().getChannels();
                    channels.forEach(printer::println);
                    if (!channels.isEmpty() && Objects.isNull(watchdog.getPeer())) {
                        watchdog.setPeer(channels.get(0));
                        printer.println("auto select " + watchdog.getPeer());
                    }
                    continue;
                }
                if (line.startsWith("select")) {
                    watchdog.setPeer(line.split(" ")[1]);
                    continue;
                }
                if (line.startsWith("sdir")) {
                    watchdog.setDataSaveDir(line.split(" ")[1]);
                    continue;
                }
                if ("gdir".equals(line)) {
                    printer.println(watchdog.getDataSaveDir());
                    continue;
                }

                if (Objects.isNull(watchdog.getPeer())) {
                    printer.println("please select client: (select 127.0.0.1");
                    watchdog.getServer().getChannels().forEach(printer::println);
                    continue;
                }

                try {
                    String result = watchdog.exec(line);
                    if (Objects.nonNull(result) && !result.isBlank()) {
                        printer.println(result);
                    }
                } catch (Exception e) {
                    printer.println("exec failed: " + RemotingUtil.exceptionSimpleDesc(e));
                }
            } catch (UserInterruptException | EndOfFileException e) {
                break;
            }
        }

        printer.println("\nBye.");

        watchdog.shutdown();
    }
}
