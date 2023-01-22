package net.shadew.grammar;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * A state object walks step by step over an expressor and calculates what can be seen next, and whether the expressor
 * is actually complete.
 */
public final class State {
    private final Grammar grammar;
    private final State parent;
    private final Expressor expr;
    private final Set<Terminal> next = new HashSet<>();
    private final Set<Terminal> nextI = Collections.unmodifiableSet(next);
    private boolean matches;
    private boolean end;
    private int index = 0;

    public State(Grammar grammar, State parent, Expressor expr) {
        this.grammar = grammar;
        this.parent = parent;
        this.expr = expr;

        update();
    }

    /**
     * The grammar this state is part of.
     *
     * @return The grammar
     */
    public Grammar grammar() {
        return grammar;
    }

    /**
     * The parent state. For example, if the expressor this state walks over contains a quantifier, it will have to
     * instantiate a child state to process the quantifier. This state is then the parent state of that child state. The
     * parent state is a useful bit of information, because it gives us access to the set of symbols that can be seen
     * after this state completes. When this state completes, the parser would go back to the parent state.
     *
     * @return The parent state
     */
    public State parent() {
        return parent;
    }

    /**
     * Returns the entire expressor this state is going over.
     *
     * @return The expressor of this state
     */
    public Expressor expr() {
        return expr;
    }

    /**
     * Returns the expressor that this state is expecting next. If this returns null, this state expects nothing more.
     * This method calls {@link Expressor#get(int)} on {@link #expr()}, with {@link #index()} as parameter.
     *
     * @return The upcoming expressor
     *
     * @see Expressor#get(int)
     * @see #expr()
     * @see #index()
     */
    public Expressor now() {
        return expr.get(index);
    }

    /**
     * Advances through the expressor, i.e. it increases the index of this state by 1.
     */
    public void advance() {
        index++;
        update();
    }

    /**
     * The current progress through {@link #expr()}.
     *
     * @return The index
     *
     * @see Expressor#get(int)
     */
    public int index() {
        return index;
    }

    /**
     * Sets the current progress through {@link #expr()}
     *
     * @param index The new indes
     * @see Expressor#get(int)
     */
    public void index(int index) {
        this.index = index;
        update();
    }

    private void update() {
        next.clear();
        matches = grammar.next(expr, index, next);
        end = now() == null;
    }

    /**
     * An unmodifiable set of terminal symbols that this state can see next. If this state progressed through the entire
     * expressor, this set is empty.
     *
     * @return The set of symbols this state can see next
     */
    public Set<Terminal> next() {
        return nextI;
    }

    /**
     * A flag indicating whether this state can actually finish parsing and return to the parent. Note that this does
     * not mean that this state cannot have more input, it just means that the expressor it matches actually matches.
     * There could, for example, be another optional symbol to match, or a non-terminal that can match nothing.
     *
     * @return Whether the expressor matches
     *
     * @see #end()
     */
    public boolean matches() {
        return matches;
    }

    /**
     * A flag indicating whether this state is progressed through the entire expressor. If this returns true,
     * {@link #next()} returns an empty set.
     *
     * @return Whether the state progressed through the entire expressor
     *
     * @see #matches()
     */
    public boolean end() {
        return end;
    }
}
