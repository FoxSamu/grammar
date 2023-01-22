package net.shadew.grammar;

import org.junit.jupiter.api.Test;

import static net.shadew.grammar.TestT.*;

public class FlattenTest {
    @Test
    void testFlatten() {
//        Expression expr = new Expression(List.of(
//            new Expression(List.of(FOO, bar)),
//            new Expression(List.of(HELLO, WORLD)),
//            LOREM
//        ));
//
        Expressor expr = FOO.negate().range(5, 7).range(2, 6);

        System.out.println(expr.describe());
        System.out.println(expr.flatten().describe());
    }
}
