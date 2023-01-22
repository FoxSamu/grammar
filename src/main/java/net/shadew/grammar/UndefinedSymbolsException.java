package net.shadew.grammar;

import java.util.Set;
import java.util.stream.Collectors;

public class UndefinedSymbolsException extends GrammarException {
    private final Set<Nonterminal> undefined;

    public UndefinedSymbolsException(Grammar grammar, Set<Nonterminal> undefined) {
        super(grammar, formatMessage(undefined));
        this.undefined = Set.copyOf(undefined);
    }

    public Set<Nonterminal> undefined() {
        return undefined;
    }

    private static String formatMessage(Set<Nonterminal> undefined) {
        return undefined.stream()
                        .map(Nonterminal::name)
                        .sorted()
                        .collect(Collectors.joining(
                            ", ",
                            "The following non-terminal symbols were not defined by the grammar:" + System.lineSeparator(),
                            ""
                        ));
    }
}
