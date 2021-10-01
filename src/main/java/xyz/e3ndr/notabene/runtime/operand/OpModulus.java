package xyz.e3ndr.notabene.runtime.operand;

import xyz.e3ndr.notabene.runtime.ExecutionException;
import xyz.e3ndr.notabene.runtime.NBRuntime;

public class OpModulus implements NBOperand {

    @Override
    public void execute(NBRuntime runtime) throws ExecutionException {
        int regA = runtime.read();
        int regB = runtime.read();

        int regAVal = runtime.registers[regA];
        int regBVal = runtime.registers[regB];

        if (regBVal != 0) {
            runtime.registers[regA] = regAVal % regBVal;
        }
    }

    @Override
    public String getDebugInfo(NBRuntime runtime) throws ExecutionException {
        return String.format("$mod #%d #%d", runtime.peek(), runtime.peek());
    }

}
