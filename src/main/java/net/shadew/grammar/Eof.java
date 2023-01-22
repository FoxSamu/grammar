package net.shadew.grammar;

/**
 * The end-of-file (EOF) symbol. See {@link Expressor#EOF}.
 *
 * @see Terminal
 * @see Expressor#EOF
 */
public final class Eof implements Terminal {
    public static final Eof EOF = new Eof();

    private Eof() {
    }

    @Override
    public String name() {
        return "$";
    }
}
