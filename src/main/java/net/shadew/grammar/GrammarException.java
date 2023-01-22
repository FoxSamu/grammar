package net.shadew.grammar;

public class GrammarException extends RuntimeException {
    private final Grammar grammar;

    public GrammarException(Grammar grammar) {
        this.grammar = grammar;
    }

    public GrammarException(Grammar grammar, String message) {
        super(message);
        this.grammar = grammar;
    }

    public GrammarException(Grammar grammar, String message, Throwable cause) {
        super(message, cause);
        this.grammar = grammar;
    }

    public GrammarException(Grammar grammar, Throwable cause) {
        super(cause);
        this.grammar = grammar;
    }

    public Grammar grammar() {
        return grammar;
    }
}
