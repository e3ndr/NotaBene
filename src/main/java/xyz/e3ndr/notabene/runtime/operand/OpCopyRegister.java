package xyz.e3ndr.notabene.runtime.operand;

import xyz.e3ndr.notabene.runtime.ExecutionException;
import xyz.e3ndr.notabene.runtime.NBRuntime;

public class OpCopyRegister implements NBOperand {

    @Override
    public void execute(NBRuntime runtime) throws ExecutionException {
        int regA = runtime.read();
        int regB = runtime.read();

        int regBVal = runtime.registers[regB];

        runtime.registers[regA] = regBVal;
    }

    @Override
    public String getDebugInfo(NBRuntime runtime) throws ExecutionException {
        return String.format("$cpr #%d #%d", runtime.peek(), runtime.peek());
    }

}
