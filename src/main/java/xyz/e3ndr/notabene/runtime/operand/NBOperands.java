package xyz.e3ndr.notabene.runtime.operand;

import xyz.e3ndr.notabene.runtime.ExecutionException;

public class NBOperands {
    private static final NBOperand[] OPERANDS = new NBOperand[33];

    static {
        OPERANDS[0] = new OpNoOP();
        OPERANDS[1] = new OpJump();
        OPERANDS[2] = new OpJumpNoReturn();
        OPERANDS[3] = new OpPushRegisters();
        OPERANDS[4] = new OpRestoreRegisters();
        OPERANDS[5] = new OpReturn();
        OPERANDS[6] = new OpIncrement();
        OPERANDS[7] = new OpDecrement();
        OPERANDS[8] = new OpAdd();
        OPERANDS[9] = new OpSubtract();
        OPERANDS[10] = new OpMultiply();
        OPERANDS[11] = new OpDivide();
        OPERANDS[12] = new OpSet();
        OPERANDS[13] = new OpMemoryStore();
        OPERANDS[14] = new OpArrayStore();
        OPERANDS[15] = new OpMemoryLoad();
        OPERANDS[16] = new OpArrayLoad();
        OPERANDS[17] = new OpJumpEqual();
        OPERANDS[18] = new OpJumpNotEqual();
        OPERANDS[19] = new OpPop();
        OPERANDS[20] = new OpHalt();
        OPERANDS[21] = new OpCopy();
        OPERANDS[22] = new OpCopyRegister();
        OPERANDS[23] = new OpClearRegisters();
        OPERANDS[24] = new OpJumpGreaterThan();
        OPERANDS[25] = new OpJumpLessThan();
        OPERANDS[26] = new OpModulus();

        OPERANDS[31] = new OpVar();
        OPERANDS[32] = new OpInterrupt();
    }

    public static NBOperand get(int opCode) throws ExecutionException {
        try {
            return OPERANDS[opCode];
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new ExecutionException("Invalid operand type: " + opCode);
        }
    }

}
