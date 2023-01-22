package net.shadew.grammar;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.stream.Collectors;

public record Negate(Collection<Terminal> terminals) implements Expressor {
    public Negate {
        terminals = Collections.unmodifiableSet(new LinkedHashSet<>(terminals));
    }

    @Override
    public void symbols(Collection<? super Symbol> symbols) {
        symbols.addAll(terminals);
    }

    @Override
    public void nonterminals(Collection<? super Nonterminal> nonterminals) {
    }

    @Override
    public void terminals(Collection<? super Terminal> terminals) {
        terminals.addAll(this.terminals);
    }

    @Override
    public Expressor flatten() {
        if (terminals.isEmpty())
            return ANY;
        return this;
    }

    @Override
    public String describe() {
        return "~(" + terminals.stream().map(Terminal::describe).collect(Collectors.joining(" | ")) + ")";
    }
}
