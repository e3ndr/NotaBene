package xyz.e3ndr.notabene.runtime.operand;

import xyz.e3ndr.notabene.runtime.NBRuntime;

public class OpVar implements NBOperand {

    @Override
    public void execute(NBRuntime runtime) {
        // No-OP
    }

    @Override
    public String getDebugInfo(NBRuntime runtime) {
        return "$var var:";
    }

}
