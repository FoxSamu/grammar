package net.shadew.grammar;

import java.util.Collection;

public record Quantify(Expressor inner, int min, int max) implements Expressor {
    public Quantify {
        if (inner == null)
            throw new IllegalArgumentException("inner == null");

        if (max < 0)
            max = -1;

        if (min < 0) {
            throw new IllegalArgumentException("min < 0");
        }
        if (max >= 0 && max < min) {
            throw new IllegalArgumentException("max < min");
        }
    }

    @Override
    public void symbols(Collection<? super Symbol> symbols) {
        inner.symbols(symbols);
    }

    @Override
    public void nonterminals(Collection<? super Nonterminal> nonterminals) {
        inner.nonterminals(nonterminals);
    }

    @Override
    public void terminals(Collection<? super Terminal> terminals) {
        inner.terminals(terminals);
    }

    @Override
    public Expressor flatten() {
        Expressor inner = this.inner.flatten();
        if (inner == EPS)
            // Many times nothing is still nothing
            return EPS;

        if (inner == NONE)
            // If min > 0 we cannot match anything because NONE never matches
            // Otherwise the only option to match is to match 0 times
            return min == 0 ? EPS : NONE;

        if (inner instanceof Quantify q) {
            if (q.max() == -1 && min >= 1) {
                // Infinite merge rule:
                // Inner can match many elements, but at least P. If we require at least N
                // elements then we get a range of at least N*P elements. Because inner can
                // consume infinitely many elements, our upper bound becomes irrelevant.
                // Ex.: (foo[3..])[3] and we see 10 foos, then it can match that by doing
                //      foo[3] foo[3] foo[4]. If we see 90, it can be matched in many ways
                //      but it will likely be foo[3] foo[3] foo[84].
                //      But if we see 8, we cannot match this so the minimum is 9 and there
                //      is no upper bound.

                int nmin = min * q.min();
                return new Quantify(q.inner(), nmin, -1);
            }

            if (min * q.max() >= q.min() * (min + 1) - 1) {
                // Merge rule:
                // If all possible repetitions of ranges overlap we can merge to one range.
                // This is restricted by the following condition:
                // min * q.max() >= q.min() * (min + 1) - 1
                //
                // Given that N is our lower bound, this condition takes the maximum amount
                // of symbols inner can match if it were repeated as little times as possible
                // by this quantifier, i.e. N times, let this be P.
                // It then takes the minimum amount of symbols inner can match if it were
                // repeated one extra time: i.e. N+1 times, let this be Q.
                // If P >= Q-1, the merge rule applies, because if we can match P elements with
                // N repetitions then P+1 >= Q and we can match this with at least N+1
                // repetitions.
                // If P < Q-1, the merge rule cannot apply, because P elements can be matched
                // at most with N repetitions. Now P+1 < Q, so we cannot match P+1 elements
                // since this is the limit of N repetitions, and with N+1 repetitions we must
                // match at least Q repetitions. There is thus a gap in the range and we cannot
                // merge.
                //
                // As an example, take (foo[5..6])+ and (foo[5..6])[10..].
                // (foo[5..6])+ is not optimized because it can match 5 and 6 times, but not 7, 8,
                // and 9 times, but it can match 10, 11 and 12 elements.
                // (foo[5..6])[10..] is optimized because when matched 10 times it can match 50 to 60
                // elements, and when matched 11 times it can match 55 to 66 times, and these ranges
                // overlap.
                //
                // If we can apply this optimization for a lower bound of N, we can also apply it for any lower
                // bound B > N as well. This is because we can conclude the following:
                //     NY >= (N + 1)X - 1
                // <=> NY >= NX + X - 1
                // <=> (Y - X)N >= X - 1
                // <=> N >= (X - 1)/(Y - X)
                //
                // For any B >= 0 we can make a new lower bound M = N+B.
                // Because M >= N, we have M >= (X - 1)/(Y - X), which we can deduce back
                // to MY >= (M + 1)X - 1.
                //
                // This proves that we only need to check the condition with our lower bound.
                //
                // For a special edge case of the merge rule, see the fixed rule below.

                int nmin = min * q.min();
                int nmax = min <= 1 ? -1 : max * q.max();

                return new Quantify(q.inner(), nmin, nmax);
            }

            if (q.max() < 0 && min == 0) {
                // Optional rule:
                // If inner has no upper bound and we are optional, then it doesn't matter
                // what our upper bound is, since inner can match infinite elements anyway,
                // and we can replace ourselves with just an optional quantifier.
                return new Quantify(inner, 0, 1);
            }

            if (q.min() == q.max() && min == max) {
                // Fixed rule:
                // Both inner and we match one fixed number of elements, so we can simplify
                // this easily. Special variant on the merge rule.
                // This is a special case of the merge rule where we can also apply it, even
                // though it does not match the condition.
                // I.e. NX < (N+1)X - 1; NX < NX+X - 1; 0 < X - 1; so the merge rule only
                // takes this if our lower bound is 0 or 1.
                // Ex. (foo[10])[20]: we match 10 foos 20 times, so in total 200 times and
                //     we can instead write foo[200].
                return new Quantify(q.inner(), q.min() * min, q.min() * min);
            }
        }
        if (min == 1 && max == 1) {
            // One time and one time only, so we are unnecessary
            return inner;
        }
        if (min == 0 && max == 0) {
            // Zero times anything is nothing
            return EPS;
        }

        return new Quantify(inner, min, max);
    }

    @Override
    public String describe() {
        if (inner instanceof Quantify)
            return "(" + inner.describe() + ")" + describeQuantification(min, max);
        return inner.describe() + describeQuantification(min, max);
    }

    public static String describeQuantification(int min, int max) {
        if (min == max) {
            return "[" + min + "]";
        }

        String quantifier;
        if (max < 0) {
            if (min == 0)
                quantifier = "*";
            else if (min == 1)
                quantifier = "+";
            else
                quantifier = "[" + min + "..]";
        } else {
            if (min == 0 && max == 1)
                quantifier = "?";
            else if (min == 0)
                quantifier = "[.." + max + "]";
            else
                quantifier = "[" + min + ".." + max + "]";
        }

        return quantifier;
    }

    @Override
    public String toString() {
        return describe();
    }

    @Override
    public Expressor get(int index) {
        if (amount(index).atMax())
            return null;
        return inner;
    }

    public Amount amount(int index) {
        if (index < min)
            return Amount.TOO_LITTLE;

        if (max < 0 || index < max)
            return Amount.ENOUGH;

        if (index == max)
            return Amount.LIMIT;

        return Amount.TOO_MUCH;
    }

    public enum Amount {
        TOO_LITTLE(false, false),
        ENOUGH(true, false),
        LIMIT(true, true),
        TOO_MUCH(false, true);

        private final boolean valid;
        private final boolean atMax;

        Amount(boolean valid, boolean atMax) {
            this.valid = valid;
            this.atMax = atMax;
        }

        public boolean valid() {
            return valid;
        }

        public boolean atMax() {
            return atMax;
        }
    }
}
