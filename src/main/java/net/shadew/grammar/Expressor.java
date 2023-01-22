package net.shadew.grammar;

import java.util.*;

/**
 * An expressor is any kind of individual grammar expression unit. The following expressors exist:
 * <ul>
 * <li>{@code FOO}: A terminal symbol. This symbol must match exactly here. See {@link Terminal}.</li>
 * <li>{@code bar}: A non-terminal symbol. This symbol must match exactly here. See {@link Nonterminal}.</li>
 * <li>{@code $}: EOF: a special kind of terminal symbol that matches the end. See {@link #EOF}.</li>
 * <li>{@code _}: Any: matches any terminal symbol. See {@link #ANY}.</li>
 * <li>{@code ~(FOO | BAR)}: A negated list of terminal symbols. This matches any terminal but the given ones.
 *     See {@link Nonterminal}.</li>
 * <li>{@code (foo bar)}: An expression of sequential expressors. See {@link Expression}.</li>
 * <li>{@code (foo | bar)}: A list of alternative expressions. See {@link Alternatives}.</li>
 * <li>{@code foo[3..]}: A quantified expression. See {@link Quantify}.</li>
 * <li>{@code #}: Epsilon: match zero input. See {@link #EPS}.</li>
 * <li>{@code !}: None: never match. See {@link #NONE}.</li>
 * </ul>
 *
 * @see Symbol
 * @see Terminal
 * @see Nonterminal
 * @see Expression
 * @see Alternatives
 * @see Quantify
 * @see Negate
 * @see #ANY
 * @see #EPS
 * @see #NONE
 * @see #EOF
 */
public sealed interface Expressor permits Alternatives, Any, Eps, Expression, Negate, None, Quantify, Symbol {
    /**
     * Epsilon: the expressor that matches a zero length input. Equivalent to an empty {@link Expression} or a
     * {@link Quantify} which matches at most 0 symbols.
     */
    public static final Expressor EPS = Eps.EPS;

    /**
     * The expressor that never matches. Equivalent to an empty {@link Alternatives} (i.e. among zero alternatives there
     * is never one that matches).
     */
    public static final Expressor NONE = None.NONE;

    /**
     * The expressor that matches one terminal, no matter which. Equivalent to an empty {@link Negate}.
     */
    public static final Expressor ANY = Any.ANY;

    /**
     * The terminal that matches the end of the file.
     */
    public static final Terminal EOF = Eof.EOF;

    /**
     * Obtain all {@link Symbol}s used in this expressor. The symbols are added to the given collection and the same
     * symbol may be added multiple times (therefore, pass a {@link Set} if you only need the distinct symbols).
     *
     * @param symbols The collection to write symbols to
     */
    void symbols(Collection<? super Symbol> symbols);

    /**
     * Obtain all {@link Nonterminal}s used in this expressor. The symbols are added to the given collection and the
     * same symbol may be added multiple times (therefore, pass a {@link Set} if you only need the distinct symbols).
     *
     * @param nonterminals The collection to write symbols to.
     */
    void nonterminals(Collection<? super Nonterminal> nonterminals);

    /**
     * Obtain all {@link Terminal}s used in this expressor. The symbols are added to the given collection and the same
     * symbol may be added multiple times (therefore, pass a {@link Set} if you only need the distinct symbols).
     *
     * @param terminals The collection to write symbols to.
     */
    void terminals(Collection<? super Terminal> terminals);

    /**
     * Flattens this expressor, i.e. it tries to reduce the depth of the expressor by merging unnecessary wrapping. E.g.
     * {@code ((FOO | BAR) | BAZ)} becomes {@code (FOO | BAR | BAZ)}, and {@code ((FOO BAR) (BAZ GUS))} becomes
     * {@code (FOO BAR BAZ GUS)}.
     * <p>
     * This also attempts to reduce quantifiers, e.g. {@code (FOO+)?} becomes {@code FOO*}. This is a quite aggressive
     * optimization but it won't change any semantics. I.e. using {@code (FOO[5])+} to match any multiple of 5
     * occurences is not optimized, and neither is {@code (FOO[5..6])+}, but {@code (FOO[5..6])[10..]} is optimized to
     * {@code FOO[50..]}. The reason is explained in the code of {@link Quantify#flatten()}.
     * </p>
     *
     * @return The flattened
     */
    Expressor flatten();

    /**
     * Describes this expressor as a string.
     *
     * @return The description string
     */
    String describe();

    /**
     * Returns an expressor to be parsed at a specific progress through this expressor.
     * <p>
     * The progress through an expressor is given by an index. The index in an expressor can mean several things
     * depending on the type of expressor:
     * <ul>
     * <li>For a terminal predicate, a non-terminal symbol or a set of alternatives, it is 0 for the location before the
     * expressor and 1 for the location after the expressor.</li>
     * <li>For an expression, it is the location before the expressor in the expression at that index, or at the end of
     * the entire expression if the index is the length of the expression.</li>
     * <li>For a quantifier, it is the amount of times the quantifier has matched.</li>
     * </ul>
     * </p>
     *
     * @param index The index through the expressor
     * @return The expressor that is going to be parsed next
     */
    default Expressor get(int index) {
        return index == 0 ? this : null;
    }

    /**
     * With {@code this} being this expressor, returns {@code this?}, i.e. makes it optional.
     *
     * @return The new expressor
     */
    default Expressor optional() {
        return new Quantify(this, 0, 1);
    }

    /**
     * With {@code this} being this expressor, returns {@code this*}.
     *
     * @return The new expressor
     */
    default Expressor zeroOrMore() {
        return new Quantify(this, 0, -1);
    }

    /**
     * With {@code this} being this expressor, returns {@code this+}.
     *
     * @return The new expressor
     */
    default Expressor oneOrMore() {
        return new Quantify(this, 1, -1);
    }

    /**
     * With {@code this} being this expressor, returns {@code this[min..]}.
     *
     * @return The new expressor
     */
    default Expressor atLeast(int min) {
        return new Quantify(this, min, -1);
    }

    /**
     * With {@code this} being this expressor, returns {@code this[..max]}.
     *
     * @return The new expressor
     */
    default Expressor atMost(int max) {
        return new Quantify(this, 0, max);
    }

    /**
     * With {@code this} being this expressor, returns {@code this[1..max]}.
     *
     * @return The new expressor
     */
    default Expressor oneAndAtMost(int max) {
        return new Quantify(this, 1, max);
    }

    /**
     * With {@code this} being this expressor, returns {@code this[min..max]}.
     *
     * @return The new expressor
     */
    default Expressor range(int min, int max) {
        return new Quantify(this, min, max);
    }

    /**
     * With {@code this} being this expressor, returns {@code this[amt]}.
     *
     * @return The new expressor
     */
    default Expressor exactly(int amt) {
        return new Quantify(this, amt, amt);
    }

    /**
     * With {@code this} being this expressor, returns {@code (this | expr)}.
     * <p>
     * Note that multiple arguments does not add multiple alternatives. Instead, it wraps them into an
     * {@link Expression}. I.e. {@code foo.or(bar, baz, gus)} gives {@code (foo | (bar baz gus))}, which is essentially
     * the same as {@code foo.or(Expressor.expr(bar, baz, gus))}. To add multiple alternatives, just call this method
     * multiple times (if this is already an alternatives expression, it will just add to that one).
     * </p>
     * <p>
     * Also note that duplicate alternatives are ignored, so if this is already an alternatives expression, it will only
     * add the given expression if it is not found in the list of alternatives already. However, order will be kept. It
     * is pretty unnecessary to add it again because the previous occurence will already consume the match.
     * </p>
     *
     * @param expr The sequence of expressors to add as alternative
     * @return The new expressor
     */
    default Expressor or(Expressor... expr) {
        if (expr.length == 0)
            return new Alternatives(List.of(this, EPS));
        if (expr.length == 1)
            return new Alternatives(List.of(this, expr[0]));
        return new Alternatives(List.of(this, new Expression(List.of(expr))));
    }

    /**
     * With {@code this} being this expressor, returns {@code (this expr)}, i.e. it appends expressors.
     * <p>
     * Note that multiple arguments add multiple expressors to follow. I.e. {@code foo.then(bar, baz, gus)} gives
     * {@code (foo bar baz gus)}, which is essentially the same as {@code foo.then(Expressor.expr(bar, baz, gus))}. If
     * this is already a sequential expression, it will just append to that.
     * </p>
     *
     * @param expr The sequence of expressors to append
     * @return The new expressor
     */
    default Expressor then(Expressor... expr) {
        if (expr.length == 0)
            return this;
        List<Expressor> exprs = new ArrayList<>();
        exprs.add(this);
        exprs.addAll(Arrays.asList(expr));
        return new Expression(List.copyOf(exprs));
    }

    /**
     * With {@code this} being this expressor, returns {@code (expr this)}, i.e. it prepends expressors.
     * <p>
     * Note that multiple arguments add multiple expressors to appear first. I.e. {@code foo.butFirst(bar, baz, gus)}
     * gives {@code (bar baz gus foo)}, which is essentially the same as
     * {@code foo.butFirst(Expressor.expr(bar, baz, gus))}. If this is already a sequential expression, it will just
     * prepend to that.
     * </p>
     *
     * @param expr The sequence of expressors to prepend
     * @return The new expressor
     */
    default Expressor butFirst(Expressor... expr) {
        if (expr.length == 0)
            return this;
        List<Expressor> exprs = new ArrayList<>(Arrays.asList(expr));
        exprs.add(this);
        return new Expression(List.copyOf(exprs));
    }

    /**
     * Create a new expression that matches given expressors in sequence.
     *
     * @return The new expressor
     */
    static Expressor expr(Expressor... expr) {
        if (expr.length == 0)
            return EPS;
        if (expr.length == 1)
            return expr[0];
        return new Expression(List.of(expr));
    }

    /**
     * Create a new expression that matches given expressors as alternatives.
     *
     * @return The new expressor
     */
    static Expressor alts(Expressor... alts) {
        if (alts.length == 0)
            return NONE;
        if (alts.length == 1)
            return alts[0];
        return new Alternatives(List.of(alts));
    }

    /**
     * Create a new expression that matches any terminal except the given ones.
     *
     * @return The new expressor
     */
    static Expressor neg(Terminal... expr) {
        if (expr.length == 0)
            return ANY;
        return new Negate(List.of(expr));
    }

    /**
     * Returns true when the given expressor matches a single terminal. This doesn't look recursively, it only checks
     * whether the given expressor is {@link Terminal}, {@link Negate} or {@link #ANY}.
     *
     * @param exp The expressor in question
     * @return True if it matches a single terminal
     */
    static boolean terminalMatch(Expressor exp) {
        return exp instanceof Terminal || exp instanceof Negate || exp == ANY;
    }
}
