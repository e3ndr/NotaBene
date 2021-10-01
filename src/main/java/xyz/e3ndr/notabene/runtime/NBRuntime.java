package xyz.e3ndr.notabene.runtime;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.jetbrains.annotations.Nullable;

import lombok.Getter;
import xyz.e3ndr.consoleutil.ConsoleUtil;
import xyz.e3ndr.fastloggingframework.logging.FastLogger;
import xyz.e3ndr.fastloggingframework.logging.LogLevel;
import xyz.e3ndr.notabene.runtime.operand.NBOperand;
import xyz.e3ndr.notabene.runtime.operand.NBOperands;

public class NBRuntime {

    public static final int SPEC = 1;
    public static final int INITIAL_MEMORY_SIZE = 8192;
    public static final int STACK_SIZE = 1024;
    public static final int PROGRAM_START = 0;

    private @Getter FastLogger runtimeLogger = new FastLogger("Runtime");
    private @Getter FastLogger programLogger = new FastLogger("Program");

    public int[] registers = new int[16];
    public int[] memory;

    public int OP_POINTER_LOCATION = -1;
    public int STACK_POINTER_LOCATION = -1;
    public int STACK_START = -1;
    public int STACK_END = -1;
    public int MEM_SIZE = -1;

    private @Getter boolean running = true;
    private @Getter int exitCode = 0;
    private int peekOffset = 0;

    private String lastStatus = "";

    public int tickInterval = (int) TimeUnit.MILLISECONDS.toNanos(150);

    public NBRuntime(int[] program) {
        MEM_SIZE = STACK_SIZE + program.length;

        if (MEM_SIZE < INITIAL_MEMORY_SIZE) {
            MEM_SIZE = INITIAL_MEMORY_SIZE;
        }

        OP_POINTER_LOCATION = (MEM_SIZE - STACK_SIZE - 1);
        STACK_POINTER_LOCATION = (MEM_SIZE - STACK_SIZE - 2);
        STACK_START = MEM_SIZE - STACK_SIZE;
        STACK_END = MEM_SIZE;

        this.memory = new int[MEM_SIZE];
        this.memory[OP_POINTER_LOCATION] = PROGRAM_START;

        System.arraycopy(program, 0, this.memory, PROGRAM_START, program.length);

        this.runtimeLogger.info("Memory: %d bytes", MEM_SIZE);

        try {
            ConsoleUtil.setTitle(String.format("%d bytes of memory | Tick interval %d", MEM_SIZE, this.tickInterval));
        } catch (IOException | InterruptedException e) {}
    }

    public void pushStack(int value) {
        int sPointer = this.memory[STACK_POINTER_LOCATION];

        this.memory[STACK_POINTER_LOCATION]++;

        this.runtimeLogger.log(LogLevel.TRACE, String.format("Pushed %d onto the stack.", value));

        this.memory[sPointer + STACK_START] = value;
    }

    public int popStack() {
        int sPointer = this.memory[STACK_POINTER_LOCATION] - 1;

        if (sPointer > 0) {
            this.memory[STACK_POINTER_LOCATION]--;
        }

        int val = this.memory[sPointer + STACK_START];

        this.runtimeLogger.log(LogLevel.TRACE, String.format("Popped %d off of the stack.", val));

        return val;
    }

    public int read() throws ExecutionException {
        try {
            int oPointer = this.memory[OP_POINTER_LOCATION];

            this.memory[OP_POINTER_LOCATION]++;

            int val = this.memory[oPointer];

            this.runtimeLogger.log(LogLevel.TRACE, String.format("Read %d from %d.", val, oPointer));

            return val;
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new ExecutionException("Reached the end of memory.");
        }
    }

    public int peek() throws ExecutionException {
        try {
            int pPointer = this.memory[OP_POINTER_LOCATION] + this.peekOffset;

            this.peekOffset++;

            return this.memory[pPointer];
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new ExecutionException("Reached the end of memory.");
        }
    }

    public void setStatus(@Nullable String status) {
        if (status == null) {
            status = "";
        } else {
            status = "| " + status;
        }

        int intervalInMs = this.tickInterval / 1000000;
        String tickStatus;

        if ((intervalInMs == 0) && (this.tickInterval != 0)) {
            tickStatus = "<1ms/tick";
        } else {
            tickStatus = String.format("%dms/tick", intervalInMs);
        }

        String formatted = String.format("NotaBene (Java) | %d bytes of memory | %s %s", MEM_SIZE, tickStatus, status);

        if (!formatted.equals(this.lastStatus)) {
            this.lastStatus = formatted;

            try {
                ConsoleUtil.setTitle(formatted);
            } catch (IOException | InterruptedException e) {}
        }
    }

    public void tick() {
        this.runtimeLogger.log(LogLevel.TRACE, "--------------------");
        this.runtimeLogger.log(LogLevel.TRACE, String.format("OP Pointer:    %d (%d)", this.memory[OP_POINTER_LOCATION], OP_POINTER_LOCATION));
        this.runtimeLogger.log(LogLevel.TRACE, String.format("Stack Pointer: %d (%d)", this.memory[STACK_POINTER_LOCATION], this.memory[STACK_POINTER_LOCATION] + STACK_START));

        this.peekOffset = 0;

        try {
            int opCode = this.read();

            NBOperand op = NBOperands.get(opCode);

            this.runtimeLogger.debug("Executing instruction: %s", op.getDebugInfo(this));

            this.setStatus(null);

            op.execute(this);
        } catch (Exception e) {
            this.runtimeLogger.exception(e);
            this.halt();
            this.exitCode = 1;
        }
    }

    public void halt() {
        this.runtimeLogger.debug("Halting!");
        this.setStatus("Halted.");
        this.running = false;
    }

}
