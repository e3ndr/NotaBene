package xyz.e3ndr.notabene.runtime.operand;

import xyz.e3ndr.notabene.runtime.NBRuntime;

public class OpPop implements NBOperand {

    @Override
    public void execute(NBRuntime runtime) {
        runtime.popStack(); // Discard
    }

    @Override
    public String getDebugInfo(NBRuntime runtime) {
        return "$pop";
    }

}
