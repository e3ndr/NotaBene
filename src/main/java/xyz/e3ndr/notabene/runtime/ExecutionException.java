package xyz.e3ndr.notabene.runtime;

public class ExecutionException extends Exception {
    private static final long serialVersionUID = 7796281626455057950L;

    public ExecutionException(String reason) {
        super(reason);
    }

}
