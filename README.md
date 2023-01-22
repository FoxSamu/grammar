# Grammar
A small Java project for dealing with grammars for language recognition. It is very in development, and I don't really
need much of it at the moment so don't expect much of it.

# Expressors
An expressor expresses a pattern in the language. There are various and they have a certain syntax to notate them in a
file as well (although there is no way to actually parse that).

## Terminals
Terminals are the purest form of an expressor, they match one token of one specific type. Their syntax is purely their
name in the grammar, but by convention they should preferably have uppercase names.

```
HELLO
WORLD
FOO
BAR
PLUS
MINUS
PAREN_OPEN
PAREN_CLOSE
```

## Non-Terminals
Non-terminals are references to other rules that must match to match the non-terminal expressor. Like terminals, their
syntax is purely their name in the grammar, but by convention they should preferably have camel case names

```
hello
world
foo
bar
expr
statement
```

## Any
The "any"-symbol matches one token of any kind. It simply is an period: `.`.

```
.
```

## None
The "none"-symbol never matches anything. It is basically the inverse of the "any"-symbol. It is written as a `!`.

```
!
```

## Negation
A negation matches one token of any kind, except any of the ones listed in the negation itself.

```
~(FOO|BAR)
```

## EOF
The end-of-file (EOF) matches, as its name quite obviously suggests, the end of the file. It is simply a dollar sign. It
is a terminal itself and lexers must generate this token at the end of the file.

```
$
```

## Epsilon
The epsilon symbol is a special symbol that matches 0 tokens. It is usually unnecessary to add to grammar, but it can
be useful in some cases. It is simply a `#` symbol.

```
#
```

## Expression
An expression matches a sequence of expressors in order. It is each individual expressor just separated by spaces and
can optionally be wrapped in parentheses.

```
FOO bar baz FOO
FOO (BAZ foo bar BAZ) FOO
mul PLUS add
```

## Quantification
A quantification repeats one expressor a certain amount of times. It has a minimum and a maximum bound. A minimum bound
of 0 means the quantification is optional. A finite maximum bound means the quantification is finite, an infinite
maximum bound means the quantification is infinite. It can be written in various ways by putting something after the
targeted expressor (wrap expressions and alternatives in parentheses, otherwise it will just target one symbol):

- 0 or 1 time: `?`
- 1 or more times: `+`
- 0 or more times: `*` or `[..]`
- N or more times: `[N..]` (`+` = `[1..]`)
- 0 up to N times: `[..N]` (`?` = `[..1]`)
- M up to N times: `[M..N]`
- Exactly N times: `[N]` or `[N..N]`
- Useless (1 time): `[1]` or `[1..1]`
- Epsilon (0 times): `[0]` or `[0..0]`

```
FOO?
FOO+
FOO*
FOO[..]
FOO[3..]
FOO[..9]
FOO[3..9]
FOO[6]
statement+
(element (COMMA element)*)?
```

## Alternatives
An alternatives expressor expresses a list of alterntive expressors. One of them must match. Expressors are separated
by vertical bars, and are optionally wrapped in parentheses:

```
FOO | BAR | baz
FOO (bar | baz) GUS
HELLO WORLD | HELLO GRAMMAR
HELLO (WORLD | GRAMMAR)
FOO BAR | (foo | baz) | BAR
expr SEMICOLON | ifStatement | forLoop | whileLoop
```

