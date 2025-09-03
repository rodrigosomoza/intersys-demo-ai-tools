package ch.innovation.ai.tools.demo.exception;

public class TransactionIdMismatchException extends RuntimeException {
    
    public TransactionIdMismatchException(String message) {
        super(message);
    }
    
    public TransactionIdMismatchException(String message, Throwable cause) {
        super(message, cause);
    }
}