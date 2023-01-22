package net.shadew.grammar;

import org.junit.jupiter.api.Test;

import java.util.List;

public class GrammarTest {
    @Test
    void test() throws GrammarException {
        Grammar grammar = new Grammar(List.of(
            TestNT.foo.define(
                Expressor.expr(TestNT.bar),
                Expressor.expr(TestNT.baz),
                Expressor.expr(TestT.LOREM)
            ),
            TestNT.bar.define(
                Expressor.expr(TestT.GUS, TestT.HELLO),
                Expressor.expr(TestT.BAR)
            ),
            TestNT.baz.define(
                Expressor.expr(TestT.BAZ),
                Expressor.expr(Expressor.EPS)
            ),
            TestNT.gus.define(
                Expressor.EPS
            )
        ));

        grammar.throwProblem();

        System.out.println(grammar.firstSet(TestNT.foo));
        System.out.println(grammar.firstSet(TestNT.bar));
        System.out.println(grammar.firstSet(TestNT.baz));
        System.out.println(grammar.firstSet(TestNT.gus));
    }
}
