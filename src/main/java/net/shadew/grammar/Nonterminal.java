package net.shadew.grammar;

import java.util.Collection;

/**
 * A non-terminal symbol.
 *
 * @implSpec It is of great importance that {@link Object#equals equals} and {@link Object#hashCode hashCode} work
 *     on implementations. It is best achieved by using an {@code enum.}
 */
public non-sealed interface Nonterminal extends Symbol {

    /**
     * {@inheritDoc}
     * <p>
     * This just adds itself.
     * </p>
     *
     * @param nonterminals The collection to write symbols to
     */
    @Override
    default void nonterminals(Collection<? super Nonterminal> nonterminals) {
        nonterminals.add(this);
    }

    /**
     * {@inheritDoc}
     * <p>
     * This does nothing because we are a non-terminal.
     * </p>
     *
     * @param terminals The collection to write symbols to
     */
    @Override
    default void terminals(Collection<? super Terminal> terminals) {
        // We aren't this
    }

    /**
     * {@inheritDoc}
     *
     * @return False.
     */
    @Override
    default boolean isTerminal() {
        return false;
    }

    /**
     * {@inheritDoc}
     *
     * @return True.
     */
    @Override
    default boolean isNonterminal() {
        return true;
    }

    /**
     * {@inheritDoc}
     *
     * @return Nothing, it throws.
     *
     * @throws UnsupportedOperationException Because we are a non-terminal.
     */
    @Override
    default Terminal asTerminal() {
        throw new UnsupportedOperationException("Symbol was not a terminal");
    }

    /**
     * {@inheritDoc}
     *
     * @return This.
     */
    @Override
    default Nonterminal asNonterminal() {
        return this;
    }


    /**
     * Define a rule for this non-terminal.
     *
     * @param expr The expressor defining the rule
     * @return The defined rule
     */
    default Rule define(Expressor expr) {
        return new Rule(this, expr);
    }

    /**
     * Define a rule for this non-terminal by multiple alternatives.
     *
     * @param alts The alternative expressors to define the rule
     * @return The defined rule
     */
    default Rule define(Expressor... alts) {
        return new Rule(this, Expressor.alts(alts));
    }
}
