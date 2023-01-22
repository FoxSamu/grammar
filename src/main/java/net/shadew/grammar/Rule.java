package net.shadew.grammar;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public final class Rule {
    private final Nonterminal lhs;
    private final Expressor rhs;
    private List<Expressor> alts;
    private Set<Symbol> rhsSymbols;
    private Set<Terminal> rhsTerminals;
    private Set<Nonterminal> rhsNonterminals;

    public Rule(Nonterminal lhs, Expressor rhs) {
        if (lhs == null)
            throw new IllegalArgumentException("lhs == null");
        if (rhs == null)
            throw new IllegalArgumentException("rhs == null");

        this.lhs = lhs;
        this.rhs = rhs;
    }

    /**
     * Left hand side of the rule: the nonterminal being rewritten.
     *
     * @return The left hand side
     */
    public Nonterminal lhs() {
        return lhs;
    }

    /**
     * Right hand side of the rule: the expressor rewriting the nonterminal.
     *
     * @return The right hand side
     */
    public Expressor rhs() {
        return rhs;
    }

    /**
     * Returns a list of alternative expressors for the right hand side. If the RHS is an {@link Alternatives}
     * expressor, it returns the individual alternatives of that. Otherwise it returns a singleton list of only the
     * expressor returned by {@link #rhs()}.
     *
     * @return The list of alternative expressors for this rule
     */
    public List<Expressor> rhsAlts() {
        if (alts == null) {
            if (rhs instanceof Alternatives a)
                alts = a.alternatives();
            else
                alts = List.of(rhs);
        }
        return alts;
    }

    public Set<Symbol> rhsSymbols() {
        if (rhsSymbols == null) {
            Set<Symbol> symbols = new LinkedHashSet<>();
            rhs.symbols(symbols);
            rhsSymbols = Set.copyOf(symbols);
        }

        return rhsSymbols;
    }

    public Set<Terminal> rhsTerminals() {
        if (rhsTerminals == null) {
            Set<Terminal> symbols = new LinkedHashSet<>();
            rhs.terminals(symbols);
            rhsTerminals = Set.copyOf(symbols);
        }

        return rhsTerminals;
    }

    public Set<Nonterminal> rhsNonterminals() {
        if (rhsNonterminals == null) {
            Set<Nonterminal> symbols = new LinkedHashSet<>();
            rhs.nonterminals(symbols);
            rhsNonterminals = Set.copyOf(symbols);
        }

        return rhsNonterminals;
    }

    /**
     * Creates a new rule by flattening the right hand side of this rule.
     *
     * @return The flattened rule
     *
     * @see Expressor#flatten()
     */
    public Rule flatten() {
        return new Rule(lhs, rhs.flatten());
    }

    /**
     * Merge this rule and another, so that the other rule is an alternative to this one. So if we merge the rule
     * {@code foo := bar} and {@code foo := gus}, we get {@code foo := (bar | baz)}. Merging only works if this rule and
     * the other rule have the same LHS.
     *
     * @param other The other rule
     * @return A new merged rule
     */
    public Rule merge(Rule other) {
        if (!other.lhs.equals(lhs))
            throw new IllegalArgumentException("Cannot merge rules with different left-hand side symbols");

        List<Expressor> alts = new ArrayList<>();
        alts.addAll(rhsAlts());
        alts.addAll(other.rhsAlts());
        return new Rule(lhs, new Alternatives(alts));
    }

    /**
     * Describes this rule as a readable string in the form of {@code lhs := rhs}.
     *
     * @return A string describing this rule
     */
    public String describe() {
        return lhs.describe() + " := " + rhs.describe();
    }

    @Override
    public String toString() {
        return describe();
    }
}
