package xyz.e3ndr.notabene.runtime.operand;

import xyz.e3ndr.notabene.runtime.ExecutionException;
import xyz.e3ndr.notabene.runtime.NBRuntime;

public interface NBOperand {

    public void execute(NBRuntime runtime) throws ExecutionException;

    public String getDebugInfo(NBRuntime runtime) throws ExecutionException;

}
