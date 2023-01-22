package net.shadew.grammar;

import java.util.Collection;

/**
 * The never-match predicate. See {@link Expressor#NONE}.
 *
 * @see Expressor#NONE
 */
public enum None implements Expressor {
    NONE;

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
        return "!";
    }

    @Override
    public Expressor optional() {
        return EPS;
    }

    @Override
    public Expressor zeroOrMore() {
        return EPS;
    }

    @Override
    public Expressor oneOrMore() {
        return this;
    }

    @Override
    public Expressor atLeast(int min) {
        return min == 0 ? EPS : this;
    }

    @Override
    public Expressor atMost(int max) {
        return EPS;
    }

    @Override
    public Expressor oneAndAtMost(int max) {
        return this;
    }

    @Override
    public Expressor range(int min, int max) {
        return min == 0 ? EPS : this;
    }

    @Override
    public Expressor exactly(int amt) {
        return this;
    }

    @Override
    public Expressor or(Expressor... expr) {
        return Expressor.expr(expr);
    }

    @Override
    public Expressor then(Expressor... expr) {
        return this; // We cannot match anything, so the entire expression can neither
    }

    @Override
    public String toString() {
        return describe();
    }
}
