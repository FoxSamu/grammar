package net.shadew.grammar;

import java.util.Collection;

/**
 * The empty-string predicate, also known as <em>epsilon</em>. See {@link Expressor#EPS}.
 *
 * @see Expressor#EPS
 */
public enum Eps implements Expressor {
    EPS;

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
        return "#";
    }

    @Override
    public Expressor optional() {
        return this;
    }

    @Override
    public Expressor zeroOrMore() {
        return this;
    }

    @Override
    public Expressor oneOrMore() {
        return this;
    }

    @Override
    public Expressor atLeast(int min) {
        return this;
    }

    @Override
    public Expressor atMost(int max) {
        return this;
    }

    @Override
    public Expressor oneAndAtMost(int max) {
        return this;
    }

    @Override
    public Expressor range(int min, int max) {
        return this;
    }

    @Override
    public Expressor exactly(int amt) {
        return this;
    }

    @Override
    public Expressor then(Expressor... expr) {
        return Expressor.expr(expr);
    }

    @Override
    public String toString() {
        return describe();
    }
}
