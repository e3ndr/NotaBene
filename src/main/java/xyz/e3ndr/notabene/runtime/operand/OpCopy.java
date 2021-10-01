package xyz.e3ndr.notabene.runtime.operand;

import xyz.e3ndr.notabene.runtime.ExecutionException;
import xyz.e3ndr.notabene.runtime.NBRuntime;

public class OpCopy implements NBOperand {

    @Override
    public void execute(NBRuntime runtime) throws ExecutionException {
        int memA = runtime.read();
        int memB = runtime.read();

        int memBVal = runtime.memory[memB];

        runtime.memory[memA] = memBVal;
    }

    @Override
    public String getDebugInfo(NBRuntime runtime) throws ExecutionException {
        return String.format("$cpy [%d] [%d]", runtime.peek(), runtime.peek());
    }

}
