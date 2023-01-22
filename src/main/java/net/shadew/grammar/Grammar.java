package net.shadew.grammar;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static net.shadew.grammar.Decision.*;
import static net.shadew.grammar.Expressor.*;

public final class Grammar {
    /** List of rules. */
    private List<RuleMeta> rules;

    /** Mapping of LHS to rule. */
    private Map<Nonterminal, RuleMeta> lhsToRule;

    /** All referenced symbols. */
    private Set<Symbol> symbols;

    /** All referenced terminal symbols. */
    private Set<Terminal> terminals;

    /** All referenced non-terminal symbols. */
    private Set<Nonterminal> nonterminals;

    /** All referenced non-terminal symbols that were not defined. */
    private Set<Nonterminal> undefined;

    /** All occurences of left-recursivity. */
    private Set<List<Nonterminal>> leftRecursive;

    private GrammarException problem;

    public Grammar(Collection<Rule> rules) {
        rebuild(rules);
    }

    /**
     * Reset this grammar instance and rebuild it with a new collection of rules.
     *
     * @param rules The new rules
     */
    public void rebuild(Collection<Rule> rules) {
        try {
            init();
            load(rules);
            collectSymbols();

            // Check for undefined symbols, we cannot continue if not all
            // non-terminals have an associated rule
            checkUndefined();

            // Compute rules that match nothing, which is essential information
            // to find first sets
            computeEmpty();

            // Compute first sets, which is essential information for parsing
            computeFirst();
        } catch (GrammarException exception) {
            problem = exception;
        } finally {
            freeze();
        }
    }

    private void init() {
        rules = new ArrayList<>();
        lhsToRule = new LinkedHashMap<>();

        symbols = new HashSet<>();
        terminals = new HashSet<>();
        nonterminals = new HashSet<>();

        undefined = new HashSet<>();
        leftRecursive = new HashSet<>();

        problem = null;
    }

    private void freeze() {
        this.rules = Collections.unmodifiableList(this.rules);
        this.lhsToRule = Collections.unmodifiableMap(this.lhsToRule);

        this.symbols = Collections.unmodifiableSet(this.symbols);
        this.terminals = Collections.unmodifiableSet(this.terminals);
        this.nonterminals = Collections.unmodifiableSet(this.nonterminals);

        this.undefined = Collections.unmodifiableSet(this.undefined);
        this.leftRecursive = Collections.unmodifiableSet(this.leftRecursive);

        for (RuleMeta meta : rules)
            meta.freeze();
    }

    /**
     * Load rules into the LHS to rule map, and merge duplicate rules.
     *
     * @param r The collection of rules
     */
    private void load(Collection<Rule> r) {
        // Clean up rules
        for (Rule rule : r) {
            RuleMeta curr = lhsToRule.get(rule.lhs());
            if (curr == null)
                lhsToRule.put(rule.lhs(), new RuleMeta(rule));
            else
                lhsToRule.put(rule.lhs(), new RuleMeta(curr.rule.merge(rule)));
        }
    }

    /**
     * Collect symbols and list of rules.
     */
    private void collectSymbols() {
        for (RuleMeta info : lhsToRule.values()) {
            rules.add(info);

            symbols.add(info.rule.lhs());
            nonterminals.add(info.rule.lhs());

            symbols.addAll(info.rule.rhsSymbols());
            terminals.addAll(info.rule.rhsTerminals());
            nonterminals.addAll(info.rule.rhsNonterminals());
        }
    }

    /**
     * Figure out if there are any non-terminal symbols not defined by any rule.
     *
     * @throws UndefinedSymbolsException In case a rule was missing
     */
    private void checkUndefined() {
        for (Nonterminal nt : nonterminals) {
            if (!lhsToRule.containsKey(nt))
                undefined.add(nt);
        }

        if (!undefined.isEmpty()) {
            throw new UndefinedSymbolsException(this, undefined);
        }
    }


    /**
     * Figure out which rules can potentially match an empty list of symbols. It also finds left recursivity on its way
     * as the 'emptiness' of a rule cannot be determined if it is so.
     *
     * @throws LeftRecursionException If left recursion happens
     */
    private void computeEmpty() {
        record QueueElement(RuleMeta meta, List<Nonterminal> reachPath) {
            Rule rule() {
                return meta.rule;
            }

            Nonterminal lhs() {
                return meta.rule.lhs();
            }
        }

        // Wrapped in a class for the sole purpose of the print function
        class EmptyQueue {
            final Queue<QueueElement> queue = new ArrayDeque<>();

            QueueElement poll() {
                return queue.remove();
            }

            void add(QueueElement el) {
                queue.add(el);
            }

            boolean empty() {
                return queue.isEmpty();
            }

            void print() {
                for (var el : queue) {
                    System.out.println(
                        el.rule().describe()
                        + "   (reached via "
                        + el.reachPath.stream().map(Nonterminal::describe).collect(Collectors.joining(" -> "))
                        + ")"
                    );
                }

                System.out.println();
            }
        }


        var queue = new EmptyQueue();
        var indecisive = new LinkedHashSet<Nonterminal>();
        var reachPath = new ArrayList<Nonterminal>();

        // Enqueue all rules we want to analyze
        // We want to analyze all rules
        for (var meta : rules) {
            queue.add(new QueueElement(meta, List.of(meta.rule.lhs())));
        }

        while (!queue.empty()) {
            // queue.print();

            var current = queue.poll();
            var meta = current.meta;

            // Do the check
            indecisive.clear();
            var empty = checkEmpty(meta, indecisive);

            if (empty == INDECISIVE) {
                // Check is indecisive, therefore the indecisive set contains
                // all symbols we must necessarily know to decide.

                // Precompute new reach path
                // No need to redo this every time in the loop
                var index = current.reachPath.size();
                reachPath.clear();
                reachPath.addAll(current.reachPath);
                reachPath.add(null);

                // Add all indecisive symbols to queue
                for (var nt : indecisive) {
                    RuleMeta ntMeta = info(nt);

                    // Compute reach path
                    reachPath.set(index, nt);

                    // If this nonterminal occurs in an earlier location in
                    // the reach path, there is left-recursion
                    int prevIndex = reachPath.indexOf(nt);
                    if (prevIndex >= 0 && prevIndex != index) {
                        List<Nonterminal> leftRec = List.copyOf(reachPath.subList(prevIndex, index + 1));
                        leftRecursive.add(leftRec);
                        ntMeta.leftRecursive.add(leftRec);
                    } else if (ntMeta.leftRecursive.isEmpty()) {
                        queue.add(new QueueElement(ntMeta, List.copyOf(reachPath)));
                    }
                }

                // Then add self
                if (current.meta.leftRecursive.isEmpty())
                    queue.add(current);

                // Note how we never add symbols that have been found to be
                // left-recursive. There is no point of checking these again,
                // we will never find out and instead keep looping forever.
            }
        }

        if (!leftRecursive.isEmpty())
            throw new LeftRecursionException(this, leftRecursive);
    }

    /**
     * Decide whether the given rule can match empty. This is a quick decision only based on things that are known at
     * the very moment. When it references non-terminals for whose rules has not been decided whether they can match
     * empty, it will likely be indecisive, and such non-terminals are collected in the provided set. These are of great
     * importance to find left recursivity to avoid the algorithm from going on forever.
     *
     * @param info       The metadata of the rule in question
     * @param indecisive The set to output indecisive symbols in, must be writable
     * @return POSITIVE if it can in fact match empty, NEGATIVE if it for sure can't, INDECISIVE otherwise
     */
    private Decision checkEmpty(RuleMeta info, Set<Nonterminal> indecisive) {
        Rule rule = info.rule;
        if (info.empty == INDECISIVE) {
            info.empty = checkEmpty(rule.rhs(), indecisive);
        }
        return info.empty;
    }

    /**
     * Decide whether the given expressor can match empty. This is a quick decision, like described in
     * {@link #checkEmpty(RuleMeta, Set)}.
     *
     * @param e          The expressor in question
     * @param indecisive The set to output indecisive symbols in
     * @return POSITIVE if it can in fact match empty, NEGATIVE if it for sure can't, INDECISIVE otherwise
     */
    private Decision checkEmpty(Expressor e, Set<Nonterminal> indecisive) {
        // None: matches nothing, so neither empty
        // Any: matches one single terminal, no matter which
        // Negate: matches one single terminal not in the provided blacklist
        // Terminal: matches one single terminal of the explicit type
        if (e == NONE || e == ANY || e instanceof Negate || e instanceof Terminal)
            return NEGATIVE;

        // Nonterminal: this is beyond what this function needs to do.
        // Just take for granted what we find in the rule info.
        if (e instanceof Nonterminal nt) {
            RuleMeta info = info(nt);
            if (info.empty == INDECISIVE)
                indecisive.add(nt);
            return info.empty;
        }

        // Expression:
        // - NEGATIVE, if there exists at least one expressor that is also NEGATIVE
        // - POSITIVE, if all expressors are POSITIVE
        // - INDECISIVE, otherwise
        if (e instanceof Expression expr) {
            Set<Nonterminal> export = new LinkedHashSet<>();

            int i = 0;
            Decision result = POSITIVE;
            int firstIndecisive = -1;

            for (Expressor inner : expr.expressors()) {
                export.clear();
                Decision empty = checkEmpty(inner, export);

                if (empty == NEGATIVE)
                    return NEGATIVE;

                if (empty == INDECISIVE) {
                    result = INDECISIVE;
                    if (firstIndecisive < 0) {
                        firstIndecisive = i;

                        // We only export indecisive symbols if this is the
                        // first indecisive expressor we find.
                        // This way, we can allow further recursion but
                        // particularly disallow left-recursion.
                        indecisive.addAll(export);
                    }
                }
                i++;
            }

            return result;
        }

        // Alternatives:
        // - POSITIVE, if there exists at least one alternatives that is also POSITIVE
        // - NEGATIVE, if all alternatives are NEGATIVE
        // - INDECISIVE, otherwise
        if (e instanceof Alternatives alts) {
            Set<Nonterminal> export = new LinkedHashSet<>();

            Decision result = NEGATIVE;
            for (Expressor inner : alts.alternatives()) {
                Decision empty = checkEmpty(inner, export);

                if (empty == POSITIVE)
                    return POSITIVE;

                if (empty == INDECISIVE)
                    result = INDECISIVE;
            }

            indecisive.addAll(export);
            return result;
        }

        // Quantifier: if it can match 0 times, it can definitely match empty.
        // Otherwise the result is directly dependent on the inner expressor.
        if (e instanceof Quantify q) {
            if (q.min() == 0)
                return POSITIVE;

            Expressor inner = q.inner();
            return checkEmpty(inner, indecisive);
        }

        // EPS: the definition of matching empty and therefore POSITIVE
        assert e == EPS;
        return POSITIVE;
    }


    private void computeFirst() {
        class FirstQueue {
            final Queue<RuleMeta> queue = new ArrayDeque<>();

            RuleMeta poll() {
                return queue.remove();
            }

            void add(RuleMeta el) {
                queue.add(el);
            }

            boolean empty() {
                return queue.isEmpty();
            }

            void print() {
                for (var el : queue) {
                    System.out.println(
                        el.rule().describe()
                    );
                }

                System.out.println();
            }
        }



        var queue = new FirstQueue();
        var indecisive = new LinkedHashSet<Nonterminal>();

        // Enqueue all rules we want to analyze
        // We want to analyze all rules
        for (var meta : rules) {
            queue.add(meta);
        }

        while (!queue.empty()) {
            // queue.print();

            var meta = queue.poll();

            if (!meta.leftRecursive.isEmpty())
                continue; // We'll never find out, stop trying

            // Do the check
            indecisive.clear();
            var empty = computeFirst(meta, indecisive);

            if (empty == INDECISIVE) {
                // Check is indecisive, therefore the indecisive set contains
                // all symbols we must necessarily know to decide.

                // Add all indecisive symbols to queue
                for (var nt : indecisive) {
                    RuleMeta ntMeta = info(nt);

                    if (ntMeta.leftRecursive.isEmpty()) {
                        queue.add(ntMeta);
                    }
                }

                // Then add self
                if (meta.leftRecursive.isEmpty())
                    queue.add(meta);

                // Note how we never add symbols that have been found to be
                // left-recursive. There is no point of checking these again,
                // we will never find out and instead keep looping forever.
            }
        }
    }

    /**
     * Compute the set of terminals that may appear as first terminal in a sequence matched by the given rule. The set
     * is stored in the rule metadata.
     *
     * @param meta       The metadata of the rule in question
     * @param indecisive The set of indecisive symbols
     * @return Whether the rule matches empty or not, or INDECISIVE if the set could not be computed yet
     */
    private Decision computeFirst(RuleMeta meta, Set<Nonterminal> indecisive) {
        Set<Terminal> first = new HashSet<>();
        Decision empty = computeFirst(meta.rule.rhs(), first, indecisive);
        if (empty != INDECISIVE) {
            meta.firstSet = first;
        }
        return empty;
    }

    /**
     * Compute the set of terminals that may appear as first terminal in a sequence matched by the given expressor.
     *
     * @param e          The expressor in question
     * @param first      The first set output
     * @param indecisive The set of indecisive symbols
     * @return Whether the expressor matches empty or not, or INDECISIVE if the set could not be computed yet
     */
    private Decision computeFirst(Expressor e, Set<Terminal> first, Set<Nonterminal> indecisive) {
        // Terminal: its first set is just itself
        if (e instanceof Terminal term) {
            first.add(term);
            return NEGATIVE;
        }

        // None: its first set is an empty set
        if (e == NONE) {
            // Normally, EPS would appear in first sets. However, we have a special flag in
            // the rule meta saying it matches empty, plus EPS does not implement Terminal.
            return NEGATIVE;
        }

        // Any: its first set is the set of all involved terminal symbols
        if (e == ANY) {
            first.addAll(terminals);
            return NEGATIVE;
        }

        // Negate: its first set is the set of all involved terminal symbols minus the ones listed
        if (e instanceof Negate neg) {
            for (Terminal term : terminals) {
                if (!neg.terminals().contains(term))
                    first.add(term);
            }
            return NEGATIVE;
        }

        // Nonterminal: decision taken based on what is known about this non-terminal at the moment
        if (e instanceof Nonterminal nt) {
            RuleMeta info = info(nt);
            if (info.firstSet == null) {
                indecisive.add(nt);
                return INDECISIVE;
            }
            first.addAll(info.firstSet);
            return info.empty;
        }

        // Quantify: first set directly is that of its inner, unless it can match at most 0 elements
        if (e instanceof Quantify q) {
            if (q.max() != 0) {
                Decision empty = computeFirst(q.inner(), first, indecisive);
                return q.min() == 0 ? POSITIVE : empty;
            }
            return POSITIVE;
        }

        // Expression:
        // - If there are any leading expressors that can match empty, these are all included in the first set
        // - The first one that does not match empty is also included
        // - After that, none are included
        if (e instanceof Expression expr) {
            Set<Terminal> expFirst = new HashSet<>();
            for (Expressor inner : expr.expressors()) {
                Decision empty = computeFirst(inner, expFirst, indecisive);

                if (empty == INDECISIVE) {
                    return INDECISIVE;
                }
                if (empty == NEGATIVE) {
                    first.addAll(expFirst);
                    return NEGATIVE;
                }
            }

            first.addAll(expFirst);
            return POSITIVE;
        }

        // Alternatives: simply the union of the first sets of the individual alternatives
        if (e instanceof Alternatives expr) {
            Decision result = NEGATIVE;
            Set<Terminal> expFirst = new HashSet<>();
            for (Expressor inner : expr.alternatives()) {
                Decision empty = computeFirst(inner, expFirst, indecisive);

                if (empty == INDECISIVE) {
                    return INDECISIVE;
                }
                if (empty == POSITIVE) {
                    result = POSITIVE;
                }
            }

            first.addAll(expFirst);
            return result;
        }

        // EPS: Although theoretically included in the first set, we don't consider this to be a terminal symbol so we
        // leave it out. Instead, when it would have been included, we give a separate signal that it can match empty.
        assert e == EPS;
        return POSITIVE;
    }


    private RuleMeta info(Nonterminal lhs) {
        return lhsToRule.get(lhs);
    }

    private RuleMeta info(Rule rule) {
        return lhsToRule.get(rule.lhs());
    }

    /**
     * If any problems have been encountered with the grammar, this will return the problem.
     *
     * @return The problem with the grammar, if any
     */
    public GrammarException problem() {
        return problem;
    }

    /**
     * If any problems have been encountered with the grammar, this will throw the problem.
     *
     * @return This
     *
     * @throws GrammarException Thrown as described
     */
    public Grammar throwProblem() {
        if (problem != null)
            throw problem;
        return this;
    }

    /**
     * A list containing all rules.
     *
     * @return The rules list
     */
    public Stream<Rule> rules() {
        return rules.stream().map(RuleMeta::rule);
    }

    /**
     * Get a rule by its LHS. In a grammar, each non-terminal symbol has at most one rule assigned. The rule defines all
     * alternative ways this symbol can be expressed.
     *
     * @param lhs The LHS of the rule
     * @return The associated rule
     */
    public Rule rule(Nonterminal lhs) {
        RuleMeta info = lhsToRule.get(lhs);
        if (info == null)
            return null;
        return info.rule;
    }

    public boolean hasRule(Nonterminal lhs) {
        return lhsToRule.containsKey(lhs);
    }

    /**
     * A set containing all symbols used by this grammar.
     *
     * @return The symbols set
     */
    public Set<Symbol> symbols() {
        return symbols;
    }

    /**
     * A set containing all terminal symbols used by this grammar.
     *
     * @return The terminal symbols set
     */
    public Set<Terminal> terminals() {
        return terminals;
    }

    /**
     * A set containing all non-terminal symbols used by this grammar.
     *
     * @return The non-terminal symbols set
     */
    public Set<Nonterminal> nonterminals() {
        return nonterminals;
    }

    /**
     * Tests whether the following symbol is referenced or defined by the grammar.
     *
     * @param symbol The symbol in question
     * @return True if the symbol is used by the grammar
     */
    public boolean has(Symbol symbol) {
        // Note: It isn't really necessary to also make this method for non-terminals and terminals separately
        return symbols.contains(symbol);
    }

    /**
     * Tests whether the given non-terminal can match an empty input.
     *
     * @param nt The non-terminal in question
     * @return True if it can match empty input
     *
     * @throws GrammarException When there was a problem with the grammar and it wasn't taken care of before
     */
    public boolean canMatchEmpty(Nonterminal nt) {
        throwProblem();
        return info(nt).empty == POSITIVE;
    }

    /**
     * Tests whether the given rule can match an empty input.
     *
     * @param rule The rule in question
     * @return True if it can match empty input
     *
     * @throws GrammarException When there was a problem with the grammar and it wasn't taken care of before
     */
    public boolean canMatchEmpty(Rule rule) {
        throwProblem();
        return info(rule).empty == POSITIVE;
    }

    /**
     * Returns the set of terminal symbols that can appear as the first symbol in an input matched by the given
     * non-terminal. I.e. if the next symbol in the input is not contained in this set, this non-terminal will not
     * match. For a parser with 1 lookahead symbol, this is essential knowledge because this set determines whether the
     * parser should go with parsing the full non-terminal symbol or not.
     *
     * @param nt The non-terminal in question
     * @return The first set of the given non-terminal
     *
     * @throws GrammarException When there was a problem with the grammar and it wasn't taken care of before
     */
    public Set<Terminal> firstSet(Nonterminal nt) {
        throwProblem();
        return info(nt).firstSet;
    }

    /**
     * Returns the set of terminal symbols that can appear as the first symbol in an input matched by the given rule.
     * I.e. if the next symbol in the input is not contained in this set, this non-terminal will not match. For a parser
     * with 1 lookahead symbol, this is essential knowledge because this set determines whether the parser should go
     * with parsing the full non-terminal symbol or not.
     *
     * @param rule The rule in question
     * @return The first set of the given non-terminal
     *
     * @throws GrammarException When there was a problem with the grammar and it wasn't taken care of before
     */
    public Set<Terminal> firstSet(Rule rule) {
        throwProblem();
        return info(rule).firstSet;
    }

    /**
     * Computes whether the given expressor can match, with no extra input, after a certain progress through the
     * expressor. If desired, it also computes the set of symbols that can be matched next by this expressor. See
     * {@link Expressor#get(int)} about how the progress through an expressor is measured.
     *
     * @param expr  The expressor in question
     * @param index The progress within the expression
     * @param out   The output set of symbols that can occur next, or null if this is useless information
     * @return True if the expressor can match with no extra input
     *
     * @throws GrammarException When there was a problem with the grammar and it wasn't taken care of before
     */
    public boolean next(Expressor expr, int index, Set<? super Terminal> out) {
        throwProblem();

        if (expr instanceof Terminal term) {
            if (index == 0) {
                if (out != null)
                    out.add(term);
                return false;
            } else {
                return true;
            }
        }

        if (expr instanceof Negate neg) {
            if (index == 0) {
                if (out != null) {
                    for (Terminal term : terminals) {
                        if (!neg.terminals().contains(term))
                            out.add(term);
                    }
                }
                return false;
            } else {
                return true;
            }
        }

        if (expr == ANY) {
            if (index == 0) {
                if (out != null)
                    out.addAll(terminals);
                return false;
            } else {
                return true;
            }
        }

        if (expr == NONE) {
            return false;
        }

        if (expr instanceof Nonterminal nt) {
            if (index == 0) {
                if (out != null)
                    out.addAll(firstSet(nt));
                return canMatchEmpty(nt);
            } else {
                return true;
            }
        }

        if (expr instanceof Quantify q) {
            Quantify.Amount amt = q.amount(index);
            boolean empty = amt.valid();
            if (!amt.atMax())
                empty |= next(q.inner(), 0, out);
            return empty;
        }

        if (expr instanceof Expression e) {
            List<Expressor> expressors = e.expressors();
            for (int i = index, len = expressors.size(); i < len; i++) {
                Expressor inner = expressors.get(i);
                if (!next(inner, 0, out))
                    return false;
            }
            return true;
        }

        if (expr instanceof Alternatives a) {
            boolean empty = false;
            for (Expressor inner : a.alternatives()) {
                empty |= next(inner, 0, out);
            }
            return empty;
        }

        assert expr == EPS;
        return true;
    }

    private static class RuleMeta {
        final Rule rule;

        Set<List<Nonterminal>> leftRecursive = new HashSet<>();
        Decision empty = INDECISIVE;

        Set<Terminal> firstSet;

        private RuleMeta(Rule rule) {
            this.rule = rule;
        }

        public Rule rule() {
            return rule;
        }

        void freeze() {
            leftRecursive = Collections.unmodifiableSet(leftRecursive);

            if (firstSet != null)
                firstSet = Collections.unmodifiableSet(firstSet);
        }
    }


    public static class Builder {
        private final Map<Nonterminal, Rule> rules = new LinkedHashMap<>();

        /**
         * Add a rule to this grammar. If a rule with the same LHS was already added, this one will be added as an
         * alternative.
         *
         * @param rule The rule to add
         * @return This
         */
        public Builder rule(Rule rule) {
            Rule curr = rules.get(rule.lhs());
            if (curr == null)
                rules.put(rule.lhs(), rule);
            else
                rules.put(rule.lhs(), curr.merge(rule));
            return this;
        }

        /**
         * Add a rule to this grammar. If a rule with the same LHS was already added, this one will be added as an
         * alternative.
         *
         * @param lhs The left hand side of the rule to add
         * @param rhs The right hand side of the rule to add
         * @return This
         */
        public Builder rule(Nonterminal lhs, Expressor rhs) {
            return rule(new Rule(lhs, rhs.flatten()));
        }

        /**
         * Add a rule to this grammar. If a rule with the same LHS was already added, this one will be added as an
         * alternative.
         *
         * @param lhs  The left hand side of the rule to add
         * @param expr The right hand side expression of the rule to add, defining the rule as by
         *             {@link Expressor#expr}
         * @return This
         */
        public Builder rule(Nonterminal lhs, Expressor... expr) {
            return rule(new Rule(lhs, expr(expr).flatten()));
        }
    }
}
