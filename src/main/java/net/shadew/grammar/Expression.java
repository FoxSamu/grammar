package net.shadew.grammar;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * An expression that matches the given expressors sequentially, in provided order. All of them must match in order for
 * this expression to match.
 */
public record Expression(List<Expressor> expressors) implements Expressor {
    public Expression {
        if (expressors == null)
            throw new IllegalArgumentException("expressors == null");

        for (Expressor expressor : expressors) {
            if (expressor == null)
                throw new IllegalArgumentException("expressors contains null");
        }

        expressors = List.copyOf(expressors);
    }

    /**
     * {@inheritDoc}
     *
     * @param symbols The collection to write symbols to
     */
    @Override
    public void symbols(Collection<? super Symbol> symbols) {
        for (Expressor exp : expressors)
            exp.symbols(symbols);
    }

    /**
     * {@inheritDoc}
     *
     * @param nonterminals The collection to write symbols to.
     */
    @Override
    public void nonterminals(Collection<? super Nonterminal> nonterminals) {
        for (Expressor exp : expressors)
            exp.nonterminals(nonterminals);
    }

    /**
     * {@inheritDoc}
     *
     * @param terminals The collection to write symbols to.
     */
    @Override
    public void terminals(Collection<? super Terminal> terminals) {
        for (Expressor exp : expressors)
            exp.terminals(terminals);
    }

    /**
     * The amount of expressors in this expression.
     *
     * @return The length of this expression.
     */
    public int size() {
        return expressors.size();
    }

    /**
     * The first expressor in this expression, or null if this is empty.
     *
     * @return The first expressor.
     */
    public Expressor first() {
        if (size() == 0)
            return null;
        return expressors.get(0);
    }

    /**
     * The last expressor in this expression, or null if this is empty.
     *
     * @return The last expressor.
     */
    public Expressor last() {
        if (size() == 0)
            return null;
        return expressors.get(size() - 1);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Expressor flatten() {
        if (expressors.isEmpty())
            return EPS;

        List<Expressor> flattened = new ArrayList<>();
        for (Expressor expr : expressors) {
            expr = expr.flatten();

            if (expr == EPS)
                continue;

            if (expr == NONE)
                return NONE; // Decision is quickly made: we simply cannot match

            if (expr instanceof Expression alts) {
                flattened.addAll(alts.expressors());
            } else {
                flattened.add(expr);
            }
        }

        if (flattened.size() == 0)
            return EPS;
        if (flattened.size() == 1)
            return flattened.get(0);

        return new Expression(flattened);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String describe() {
        return expressors.stream().map(Expressor::describe).collect(Collectors.joining(" ", "(", ")"));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Expressor then(Expressor... expr) {
        if (expr.length == 0)
            return this;
        List<Expressor> exprs = new ArrayList<>(expressors);
        if (expr.length == 1)
            exprs.add(expr[0]);
        else
            exprs.addAll(Arrays.asList(expr));
        return new Expression(List.copyOf(exprs));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Expressor butFirst(Expressor... expr) {
        if (expr.length == 0)
            return this;
        List<Expressor> exprs = new ArrayList<>(expressors);
        if (expr.length == 1)
            exprs.add(0, expr[0]);
        else
            exprs.addAll(0, Arrays.asList(expr));
        return new Expression(List.copyOf(exprs));
    }

    @Override
    public String toString() {
        return describe();
    }

    @Override
    public Expressor get(int index) {
        if (index < 0 || index >= size())
            return null;
        return expressors.get(index);
    }
}
