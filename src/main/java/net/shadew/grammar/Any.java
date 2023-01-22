package net.shadew.grammar;

import java.util.Collection;

/**
 * The any-terminal predicate. See {@link Expressor#ANY}.
 *
 * @see Expressor#ANY
 */
public enum Any implements Expressor {
    ANY;

    @Override
    public void symbols(Collection<? super Symbol> symbols) {
        // None here
    }

    @Override
    public void nonterminals(Collection<? super Nonterminal> nonterminals) {
        // None here
    }

    @Override
    public void terminals(Collection<? super Terminal> terminals) {
        // None here
    }

    @Override
    public Expressor flatten() {
        return this;
    }

    @Override
    public String describe() {
        return ".";
    }

    @Override
    public String toString() {
        return describe();
    }
}
