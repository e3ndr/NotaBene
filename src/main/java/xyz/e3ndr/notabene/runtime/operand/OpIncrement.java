package xyz.e3ndr.notabene.runtime.operand;

import xyz.e3ndr.notabene.runtime.ExecutionException;
import xyz.e3ndr.notabene.runtime.NBRuntime;

public class OpIncrement implements NBOperand {

    @Override
    public void execute(NBRuntime runtime) throws ExecutionException {
        int register = runtime.read();

        runtime.registers[register]++;
    }

    @Override
    public String getDebugInfo(NBRuntime runtime) throws ExecutionException {
        return String.format("$inc #%d", runtime.peek());
    }

}
