package xyz.e3ndr.notabene.runtime.operand;

import xyz.e3ndr.notabene.runtime.NBRuntime;

public class OpRestoreRegisters implements NBOperand {

    @Override
    public void execute(NBRuntime runtime) {
        for (int i = 10; i >= 0; i--) {
            runtime.registers[i] = runtime.popStack();
        }
    }

    @Override
    public String getDebugInfo(NBRuntime runtime) {
        return "$rer";
    }

}
