package net.shadew.grammar;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class LeftRecursionException extends GrammarException {
    private final Set<List<Nonterminal>> recursion;

    public LeftRecursionException(Grammar grammar, Set<List<Nonterminal>> leftRecursive) {
        super(grammar, formatMessage(leftRecursive));
        this.recursion = leftRecursive.stream().map(List::copyOf).collect(Collectors.toUnmodifiableSet());
    }

    public Set<List<Nonterminal>> recursion() {
        return recursion;
    }

    private static String formatMessage(Set<List<Nonterminal>> leftRecursive) {
        return "Left recursivity has been found:" + System.lineSeparator() +
               leftRecursive.stream()
                            .map(r -> r.stream().map(Nonterminal::name).collect(Collectors.joining(" -> ", "  - ", "")))
                            .collect(Collectors.joining(System.lineSeparator()));
    }
}
