package xyz.e3ndr.notabene;

import java.io.File;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.file.Files;
import java.util.concurrent.TimeUnit;

import lombok.Getter;
import lombok.SneakyThrows;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import xyz.e3ndr.consoleutil.ConsoleUtil;
import xyz.e3ndr.fastloggingframework.FastLoggingFramework;
import xyz.e3ndr.fastloggingframework.logging.FastLogger;
import xyz.e3ndr.fastloggingframework.logging.LogLevel;
import xyz.e3ndr.notabene.assembler.NBAssembler;
import xyz.e3ndr.notabene.runtime.NBRuntime;

@Command(name = "nb", mixinStandardHelpOptions = true)
public class Launcher implements Runnable {
    private static @Getter Thread runtimeThread;

    @Option(names = {
            "-d",
            "--debug"
    }, description = "Enables debug logging")
    private boolean debug = false;

    @Option(names = {
            "-t",
            "--trace"
    }, description = "Enables trace logging")
    private boolean trace = false;

    @Option(names = {
            "-w",
            "--window"
    }, description = "Summon a new window")
    private boolean summonNewWindow = false;

    @Parameters(index = "0", description = "The action to perform")
    private LauncherMode mode;

    @Parameters(index = "1", description = "The file to use")
    private File file;

    public static void main(String[] args) throws Exception {
        new CommandLine(new Launcher())
            .setCaseInsensitiveEnumValuesAllowed(true)
            .execute(args);
    }

    @SneakyThrows
    @Override
    public void run() {
        runtimeThread = Thread.currentThread();

        ConsoleUtil.getPlatform(); // Init it before the FLF debug set.

        if (this.summonNewWindow) {
            ConsoleUtil.summonConsoleWindow();
        }

        if (this.trace) {
            FastLoggingFramework.setDefaultLevel(LogLevel.TRACE);
        } else if (this.debug) {
            FastLoggingFramework.setDefaultLevel(LogLevel.DEBUG);
        }

        byte[] contents = Files.readAllBytes(this.file.toPath());

        if (this.mode == LauncherMode.ASSEMBLE) {
            String destName = String.format("%s.nbprg", this.file.getName().split("\\.", 2)[0]);
            File destFile = new File(this.file.getParentFile(), destName);

            try {
                new NBAssembler(this.file, destFile, contents)
                    .assemble();
            } catch (Exception e) {
                FastLogger.logException(e);
            }

            FastLoggingFramework.close();
            System.exit(0);
        } else {
            IntBuffer buf = ByteBuffer
                .wrap(contents)
                .asIntBuffer();

            int[] program = new int[buf.capacity()];

            buf.get(program);
            buf = null;

            NBRuntime runtime = new NBRuntime(program);

            System.gc();

            try {
                while (runtime.isRunning()) {
                    long start = System.nanoTime();
                    runtime.tick();
                    long executionTime = System.nanoTime() - start;

                    long sleep = runtime.tickInterval - executionTime;

                    if (sleep > 0) {
                        try {
                            TimeUnit.NANOSECONDS.sleep(sleep);
                        } catch (InterruptedException e) {} // Swallow.
                    }
                }
            } catch (Throwable e) {
                FastLogger.logException(e);
            }

            if (this.summonNewWindow) {
                runtime.getRuntimeLogger().info("Exiting in 10 seconds.");
            }

            FastLoggingFramework.close();

            // We do this down here so we can get the message through
            // but only timeout when the user sees the message.
            if (this.summonNewWindow) {
                TimeUnit.SECONDS.sleep(10);
            }

            System.exit(runtime.getExitCode());
        }
    }

    private static enum LauncherMode {
        ASSEMBLE,
        EXECUTE;

    }

}
