package semantic;

public class SemanticException extends Exception {
    public int lineNumber;
    public String message;

    public SemanticException(int lineNumber, String message) {
        this.lineNumber = lineNumber;
        this.message = message;
    }

    @Override
    public String getMessage() {
        return String.format("Semantic Error at line %d: %s", lineNumber, message);
    }
}

