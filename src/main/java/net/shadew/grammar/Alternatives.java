package net.shadew.grammar;

import java.util.*;
import java.util.stream.Collectors;

/**
 * An expressor providing a bunch of alternative expressors, of which one must match.
 */
public record Alternatives(List<Expressor> alternatives) implements Expressor {
    public Alternatives {
        if (alternatives == null)
            throw new IllegalArgumentException("alternatives == null");

        for (Expressor expressor : alternatives) {
            if (expressor == null)
                throw new IllegalArgumentException("alternatives contains null");
        }

        alternatives = List.copyOf(alternatives);
    }

    /**
     * {@inheritDoc}
     *
     * @param symbols The collection to write symbols to
     */
    @Override
    public void symbols(Collection<? super Symbol> symbols) {
        for (Expressor alt : alternatives)
            alt.symbols(symbols);
    }

    /**
     * {@inheritDoc}
     *
     * @param nonterminals The collection to write symbols to.
     */
    @Override
    public void nonterminals(Collection<? super Nonterminal> nonterminals) {
        for (Expressor alt : alternatives)
            alt.nonterminals(nonterminals);
    }

    /**
     * {@inheritDoc}
     *
     * @param terminals The collection to write symbols to.
     */
    @Override
    public void terminals(Collection<? super Terminal> terminals) {
        for (Expressor alt : alternatives)
            alt.terminals(terminals);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Expressor flatten() {
        if (alternatives.isEmpty())
            return NONE;

        Set<Expressor> flattened = new LinkedHashSet<>();
        for (Expressor expr : alternatives) {
            expr = expr.flatten();

            if (expr == NONE)
                continue;

            if (expr instanceof Alternatives alts) {
                flattened.addAll(alts.alternatives());
            } else {
                flattened.add(expr);
            }
        }

        if (flattened.size() == 0)
            return NONE;
        if (flattened.size() == 1)
            return flattened.iterator().next();

        return new Alternatives(List.copyOf(flattened));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String describe() {
        return alternatives.stream().map(Expressor::describe).collect(Collectors.joining(" | ", "(", ")"));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Expressor or(Expressor... expr) {
        List<Expressor> alts = new ArrayList<>(alternatives);
        if (expr.length == 0)
            alts.add(EPS);
        else if (expr.length == 1)
            alts.add(expr[0]);
        else
            alts.add(new Expression(List.of(expr)));
        return new Alternatives(List.copyOf(alts));
    }

    @Override
    public String toString() {
        return describe();
    }
}
