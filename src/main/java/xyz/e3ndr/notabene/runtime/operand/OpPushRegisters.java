package xyz.e3ndr.notabene.runtime.operand;

import xyz.e3ndr.notabene.runtime.NBRuntime;

public class OpPushRegisters implements NBOperand {

    @Override
    public void execute(NBRuntime runtime) {
        for (int i = 0; i < 11; i++) {
            runtime.pushStack(runtime.registers[i]);

            runtime.registers[i] = 0;
        }
    }

    @Override
    public String getDebugInfo(NBRuntime runtime) {
        return "$pur";
    }

}
