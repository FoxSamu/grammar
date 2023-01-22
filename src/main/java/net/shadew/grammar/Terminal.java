package net.shadew.grammar;

import java.util.Collection;
import java.util.List;

/**
 * A terminal symbol.
 *
 * @implSpec It is of great importance that {@link Object#equals equals} and {@link Object#hashCode hashCode} work
 *     on implementations. It is best achieved by using an {@code enum.}
 */
public non-sealed interface Terminal extends Symbol {
    /**
     * The terminal that matches the end of the file.
     */
    public static final Terminal EOF = Eof.EOF;

    /**
     * {@inheritDoc}
     * <p>
     * This does nothing because we are a terminal.
     * </p>
     *
     * @param nonterminals The collection to write symbols to
     */
    @Override
    default void nonterminals(Collection<? super Nonterminal> nonterminals) {
        // We aren't this
    }

    /**
     * {@inheritDoc}
     * <p>
     * This just adds itself.
     * </p>
     *
     * @param terminals The collection to write symbols to
     */
    @Override
    default void terminals(Collection<? super Terminal> terminals) {
        terminals.add(this);
    }

    /**
     * {@inheritDoc}
     *
     * @return True.
     */
    @Override
    default boolean isTerminal() {
        return true;
    }

    /**
     * {@inheritDoc}
     *
     * @return False.
     */
    @Override
    default boolean isNonterminal() {
        return false;
    }

    /**
     * {@inheritDoc}
     *
     * @return This.
     */
    @Override
    default Terminal asTerminal() {
        return this;
    }

    /**
     * {@inheritDoc}
     *
     * @return Nothing, it throws.
     *
     * @throws UnsupportedOperationException Because we are a terminal.
     */
    @Override
    default Nonterminal asNonterminal() {
        throw new UnsupportedOperationException("Symbol was not a non-terminal");
    }

    /**
     * Returns an expressor matching any terminal but this one.
     *
     * @return The new expressor
     */
    default Expressor negate() {
        return new Negate(List.of(this));
    }
}
