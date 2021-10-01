package xyz.e3ndr.notabene.runtime.operand;

import java.awt.Dimension;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

import org.fusesource.jansi.AnsiConsole;

import xyz.e3ndr.consoleutil.ConsoleUtil;
import xyz.e3ndr.consoleutil.input.InputKey;
import xyz.e3ndr.consoleutil.input.KeyHook;
import xyz.e3ndr.consoleutil.input.KeyListener;
import xyz.e3ndr.fastloggingframework.logging.LogLevel;
import xyz.e3ndr.notabene.runtime.ExecutionException;
import xyz.e3ndr.notabene.runtime.NBRegisters;
import xyz.e3ndr.notabene.runtime.NBRuntime;

public class OpInterrupt implements NBOperand {
    private static BlockingQueue<Integer> keyboard = new LinkedBlockingQueue<>();

    private StringBuilder printBuffer = new StringBuilder();
    public static int screenMode = 0;

    static {
        KeyHook.addListener(new KeyListener() {

            @Override
            public void onKey(char key, boolean alt, boolean control) {
                keyboard.add((int) key);
            }

            @Override
            public void onKey(InputKey key) {
                keyboard.add(key.getCode());
            }

        });
    }

    @Override
    public void execute(NBRuntime runtime) throws ExecutionException {
        int interruptMode = runtime.read();

        int lVal = runtime.registers[NBRegisters.L];

        switch (interruptMode) {

            // Print int
            case 0x00: {
                runtime
                    .getRuntimeLogger()
                    .log(LogLevel.TRACE, String.format("[OpInterrupt] Printing [%d] to the display", lVal));

                if (screenMode == 0) {
                    this.printBuffer.append(lVal);
                } else if (screenMode == 1) {
                    AnsiConsole.out.print(lVal);
                }
                break;
            }

            // Print char
            case 0x01: {
                char ch = (char) lVal;

                if (screenMode == 0) {
                    if (ch == '\n') {
                        runtime
                            .getRuntimeLogger()
                            .log(LogLevel.TRACE, "[OpInterrupt] Flushing the display");

                        runtime
                            .getProgramLogger()
                            .info(this.printBuffer);

                        this.printBuffer.setLength(0); // Clear
                    } else {
                        runtime
                            .getRuntimeLogger()
                            .log(LogLevel.TRACE, String.format("[OpInterrupt] Printing '%c' to the display", ch));

                        this.printBuffer.append(ch);
                    }
                } else if (screenMode == 1) {
                    AnsiConsole.out.print(ch);
                }
                break;
            }

            // Print all
            case 0x02: {
                char ch = ' ';

                while (true) {
                    ch = (char) runtime.read();

                    if (ch == '\u0000') {
                        break;
                    } else {
                        if (screenMode == 0) {
                            if (ch == '\n') {
                                runtime
                                    .getRuntimeLogger()
                                    .log(LogLevel.TRACE, "[OpInterrupt] Flushing the display");

                                runtime
                                    .getProgramLogger()
                                    .info(this.printBuffer);

                                this.printBuffer.setLength(0); // Clear
                            } else {
                                runtime
                                    .getRuntimeLogger()
                                    .log(LogLevel.TRACE, String.format("[OpInterrupt] Printing '%c' to the display", ch));

                                this.printBuffer.append(ch);
                            }
                        } else if (screenMode == 1) {
                            AnsiConsole.out.print(ch);
                        }
                    }
                }
                break;
            }

            // Halt
            case 0x03: {
                runtime.halt();
                break;
            }

            // Speed
            case 0x04: {
                runtime
                    .getRuntimeLogger()
                    .log(LogLevel.TRACE, String.format("[OpInterrupt] Setting tickspeed to %d", lVal));

                runtime.tickInterval = lVal;
                break;
            }

            // Epoch
            case 0x05: {
                runtime.registers[NBRegisters.L] = (int) (System.currentTimeMillis() / 1000);
                break;
            }

            // Milli time
            case 0x06: {
                runtime.registers[NBRegisters.L] = (int) (System.nanoTime() / 1000);
                break;
            }

            // Sleep
            case 0x07: {
                try {
                    TimeUnit.NANOSECONDS.sleep(lVal);
                } catch (InterruptedException ignored) {}
                break;
            }

            // Read
            case 0x08: {
                int scancode = -1;

                try {
                    if (keyboard.isEmpty()) {
                        runtime.setStatus("Waiting for input.");
                    }

                    scancode = keyboard.take();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                runtime.registers[NBRegisters.L] = scancode;
                break;
            }

            // Mem
            case 0x09: {
                runtime.getRuntimeLogger().warn("Unsupported interrupt: MEM");
                break;
            }

            // Dump
            case 0x0a: {
                String now = String.valueOf(System.currentTimeMillis() / 1000);

                try {
                    File mdump = new File(String.format("%s.nbmdump", now));
                    File rdump = new File(String.format("%s.nbrdump", now));

                    {
                        StringBuilder sb = new StringBuilder();

                        for (int i : runtime.memory) {
                            sb.append(',').append(i);
                        }

                        mdump.createNewFile();
                        Files.write(mdump.toPath(), sb.substring(1).getBytes());
                    }

                    {
                        StringBuilder sb = new StringBuilder();

                        for (int i : runtime.registers) {
                            sb.append(',').append(i);
                        }

                        rdump.createNewFile();
                        Files.write(rdump.toPath(), sb.substring(1).getBytes());
                    }

                    runtime
                        .getRuntimeLogger()
                        .warn("Dumped memory to %s", mdump)
                        .warn("Dumped registers to %s", rdump);
                } catch (IOException e) {
                    runtime
                        .getRuntimeLogger()
                        .severe("Could not dump memory:")
                        .exception(e);
                }

                runtime.halt();
                break;
            }

            // Spec
            case 0x0b: {
                runtime
                    .getRuntimeLogger()
                    .log(LogLevel.TRACE, String.format("[OpInterrupt] Setting #11 to %d", NBRuntime.SPEC));

                runtime.registers[NBRegisters.L] = NBRuntime.SPEC;
                break;
            }

            // Mem size
            case 0x0c: {
                int targetSize = lVal + 1024;

                runtime
                    .getRuntimeLogger()
                    .log(LogLevel.TRACE, String.format("[OpInterrupt] Changing memory size to %d", targetSize));

                int[] stack = new int[1024];
                int[] programMem = new int[runtime.memory.length - 1024];

                System.arraycopy(runtime.memory, runtime.memory.length - 1024, stack, 0, 1024);
                System.arraycopy(runtime.memory, 0, programMem, 0, programMem.length);

                runtime.memory = new int[targetSize];

                System.arraycopy(stack, 0, runtime.memory, targetSize - 1024, stack.length);
                System.arraycopy(programMem, 0, runtime.memory, 0, programMem.length);

                break;
            }

            // Beep
            case 0x0d: {
                ConsoleUtil.bell();
                break;
            }

            // Set screen width
            case 0x0f: {
                Dimension size = ConsoleUtil.getSize();

                try {
                    ConsoleUtil.setSize(lVal, size.height);
                } catch (IOException | InterruptedException e) {
                    e.printStackTrace();
                }
                break;
            }

            // Set screen height
            case 0x10: {
                Dimension size = ConsoleUtil.getSize();

                try {
                    ConsoleUtil.setSize(size.width, lVal);
                } catch (IOException | InterruptedException e) {
                    e.printStackTrace();
                }
                break;
            }

            // Set screen mode
            case 0x11: {
                if (screenMode != -1) {
                    screenMode = lVal;

                    if (screenMode == 1) {
                        try {
                            ConsoleUtil.clearConsole();
                        } catch (IOException | InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
                break;
            }

            // Random
            case 0x12: {
                int rnd = ThreadLocalRandom.current().nextInt(lVal);

                runtime.registers[NBRegisters.L] = rnd;

                break;
            }

            // Read
            case 0x13: {
                int scancode = -1;

                try {
                    if (keyboard.isEmpty()) {
                        runtime.setStatus("Waiting for input.");
                    }

                    scancode = keyboard.poll(lVal, TimeUnit.NANOSECONDS);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                runtime.registers[NBRegisters.L] = scancode;
                break;
            }

            default:
                throw new ExecutionException("Unknown interrupt mode: " + interruptMode);
        }
    }

    @Override
    public String getDebugInfo(NBRuntime runtime) throws ExecutionException {
        int mode = runtime.peek();

        if (mode == 0x02) {
            StringBuilder contents = new StringBuilder();

            int peek = runtime.peek();

            while (peek != 0) {
                contents.append((char) peek);
                peek = runtime.peek();
            }

            return String.format("$int [%d] \"%s\"", mode, escapeString(contents.toString()));
        } else {
            return String.format("$int [%d]", mode);
        }
    }

    private static String escapeString(String str) {
        return str
            .replace("\\", "\\\\")     // \ -> \\
            .replace("\b", "\\b")      // ? -> \b
            .replace("\f", "\\f")      // ? -> \f
            .replace("\n", "\\n")      // ? -> \n
            .replace("\r", "\\r")      // ? -> \r
            .replace("\t", "\\t")      // ? -> \t
            .replace("\u000b", "\\v") // ? -> \v
            .replace("'", "\\'")       // ' -> \'
            .replace("\"", "\\\"");    // " -> \"
    }

}
