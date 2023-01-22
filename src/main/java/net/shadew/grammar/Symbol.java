package net.shadew.grammar;

import java.util.Collection;

/**
 * A symbol is an individual element of an expression, it eiter matches one token (i.e. it's a terminal symbol), or it
 * references another rule (i.e. it's a non-terminal symbol). Subinterfaces {@link Terminal} and {@link Nonterminal} are
 * preferably implemented by an {@code enum}. This way, names of terminals and non-terminals nicely align with the
 * defined fields immediately because it indirectly implements the {@link #name()} method.
 *
 * @see Terminal
 * @see Nonterminal
 */
public sealed interface Symbol extends Expressor permits Nonterminal, Terminal {
    /**
     * The grammatical name of the symbol.
     *
     * @return The grammatical name
     *
     * @implSpec Must return a valid Java identifier that is not {@code $} or {@code _}. This method is implemented
     *     automatically by enums, where it matches the field name.
     */
    String name();

    /**
     * {@inheritDoc}
     * <p>
     * This returns itself because an individual symbol is already on its flattest.
     * </p>
     *
     * @return This
     */
    @Override
    default Expressor flatten() {
        return this;
    }

    /**
     * {@inheritDoc}
     * <p>
     * This just adds itself.
     * </p>
     *
     * @param symbols The collection to write symbols to
     */
    @Override
    default void symbols(Collection<? super Symbol> symbols) {
        symbols.add(this);
    }

    /**
     * Returns whether this symbol is a terminal symbol. If so, it can be cast to {@link Terminal}.
     *
     * @return True if this is a terminal symbol.
     */
    boolean isTerminal();

    /**
     * Returns whether this symbol is a non-terminal symbol. If so, it can be cast to {@link Nonterminal}.
     *
     * @return True if this is a non-terminal symbol.
     */
    boolean isNonterminal();

    /**
     * Returns this symbol as a {@link Terminal}.
     *
     * @return This symbol as a terminal symbol
     *
     * @throws UnsupportedOperationException Thrown when this symbol is a non-terminal symbol
     */
    Terminal asTerminal();

    /**
     * Returns this symbol as a {@link Nonterminal}.
     *
     * @return This symbol as a non-terminal symbol
     *
     * @throws UnsupportedOperationException Thrown when this symbol is a terminal symbol
     */
    Nonterminal asNonterminal();

    /**
     * {@inheritDoc}
     * <p>
     * This return {@link #name()}.
     * </p>
     *
     * @return {@link #name()}.
     */
    @Override
    default String describe() {
        return name();
    }
}
