package io.quarkus.demo.err;

public class NmtLineParseException extends Exception{
    public NmtLineParseException(String message) {
        super(message);
    }
}
