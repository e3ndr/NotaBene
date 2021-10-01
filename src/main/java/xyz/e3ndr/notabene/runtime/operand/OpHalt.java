package xyz.e3ndr.notabene.runtime.operand;

import xyz.e3ndr.notabene.runtime.ExecutionException;
import xyz.e3ndr.notabene.runtime.NBRuntime;

public class OpHalt implements NBOperand {

    @Override
    public void execute(NBRuntime runtime) throws ExecutionException {
        runtime.halt();
    }

    @Override
    public String getDebugInfo(NBRuntime runtime) throws ExecutionException {
        return "$hlt";
    }

}
