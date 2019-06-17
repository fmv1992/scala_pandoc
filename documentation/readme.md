![Build status](https://travis-ci.com/fmv1992/scala_pandoc.svg?branch=dev)
[![codecov](https://codecov.io/gh/fmv1992/scala_pandoc/branch/dev/graph/badge.svg)](https://codecov.io/gh/fmv1992/scala_pandoc)

# scala_pandoc

A [Pandoc][pandoc] library written in Scala.

Also a command line utility to process Pandoc's json.

## Library

The library is partially implemented.

It must follow the conventions and types from: <https://hackage.haskell.org/package/pandoc-types-1.19/docs/Text-Pandoc-Definition.html>.

## Command Line Utility

The command line utility has the following options:

```{pipe="sh -"}
echo "# java -jar ./scala_pandoc/target/scala-2.12/scala_pandoc.jar --help"
java -jar ./scala_pandoc/target/scala-2.12/scala_pandoc.jar --help
```

[comment]: # ( ???: Put these as itemized bullets.)

### `Embed` functionality

Embeds code as part of text. Mostly used after `evaluate`.

Example with embed only:

~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
```{.embed}
This is inside a code block.
```
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

Becomes:

```{.embed}
This is inside a code block.
```

Example with embed and evaluate:

~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
```{.embed pipe="sh -"}
echo "The first day of 2010 was: $(date -d '2010-01-01' '+%A')."
```
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

Becomes:

```{.embed pipe="sh -"}
echo "The first day of 2010 was: $(date -d '2010-01-01' '+%A')."
```

See the evaluate functionality to get a better usage of embedding.

### `Evaluate` functionality

#### Regular (independent) code

Evaluate code blocks and substitute their results as a code block instead of the original code block.

~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
```{pipe="python3 -"}
print("scala_pandoc")
```
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

Gives:

```{pipe="python3 -"}
print("scala_pandoc")
```

Similarly one can use `joiner="SomeWord:"` to give an explanation flow:

~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
```{joiner="Tells the name of a great software:" pipe="python3 -"}
print("scala_pandoc")
```
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

Becomes:

* * *

```{joiner="Tells the name of a great software:" pipe="python3 -"}
print("scala_pandoc")
```

* * *

#### Sequential code

Evaluate code blocks sequentially. Currently only Scala is supported. Consider the document:

```{pipe="sh -"}
cat ./other/example/documentation/example_documentation_01_sequential_code.md
```

It outputs:

```{pipe="sh -"}
pandoc2 --from markdown --to json ./other/example/documentation/example_documentation_01_sequential_code.md \
    | java -jar ./scala_pandoc/target/scala-2.12/scala_pandoc.jar --evaluate \
    | pandoc2 --from json --to markdown
```

### `Farsi-to-rtl` functionality

Encapsulate any sequence of Farsi characters with a `\rl{` prefix and a `}` suffix. This allows for seamless composition with Farsi and Latin characters.

```{joiner="Gives us:" pipe="sh -"}
echo 'A translation of the sentence "اسم مولف این برنمه فِلیپه است." is "The name of the author of this program is Felipe.".' \
    | pandoc2 --from markdown --to json \
    | java -jar ./scala_pandoc/target/scala-2.12/scala_pandoc.jar --farsi-to-rtl \
    | pandoc2 --from json --to markdown
```

The use of the `computationTreeId=""` map create blocks of independent code which can be used in the same file. In other words there can be `computationTreeId="id01"` and `computationTreeId="id02"` in the same file, and both computations would run independently.

See: <https://ctan.org/pkg/xepersian?lang=en>.

## Dependencies

<https://github.com/fmv1992/fmv1992_scala_utilities>.

## See also

The functionality of `embed` and `evaluate` were inspired by:

1.  <https://github.com/Warbo/panhandle>.

1.  <https://github.com/Warbo/panpipe>.

However neither of them work with `pandoc2`.

## TODO

*   Have 90% of code coverage.

*   Document the code according to:

    1.  <https://docs.scala-lang.org/style/scaladoc.html>.

    1.  <https://docs.scala-lang.org/overviews/scaladoc/for-library-authors.html>.

    *   Enforce/Create a GNU style documentation.

## Bugs

1.  Evaluate Scala code serially using a hack to signal end of blocks: [✘]

    Tag: `[PrintToMarkCodeBlock]`.

1.  Replacement of variables: [✘]

    Tag: `[BugReplacementOfVariables]`.

    Description: Replacement of variables is really tricky. See for example
    `commfad88b8`:

    ```
    // On markdown:
    replace-variables:
        ...
        - expensiveComputation = `echo "linux"`{pipe="sh"}
    // On json:
            ...
                {
                    "c": "expensiveComputation.",
                    "t": "Str"
                }
            ],
            "t": "Para"
            ...
    // On pdf:
    ...
    The usefulness of this is that expensiveComputations can be cached:
    expensiveComputation.
    ```

    The trailing dot does not get split by Pandoc. Thus reliable substitution is not possible.

1.  Status: [✔]

    Description: Quoting only contiguous فارسی characters.

    ```
    * [۵۵%] Fyodor Dostoevsky - The Brothers Karamazov.
    ```

    Goes to:

    ```
    \rl{[۵۵%]} Fyodor Dostoevsky - The Brothers Karamazov.
    ```

    And it should put `{}` around [

    ```
    {[}✘{]} Learn about fraud on your own (don't expect invitations).
    ```

    The JSON representation is:

    ```
    {
        "t": "Str",
        "c": "[\u06f5\u06f5%]"
    },
    ```

1.  Status: [✘]

    Description: ???.

    Tag: `[note01]`.

[pandoc]: http://pandoc.org

[comment]: # ( vim: set filetype=markdown fileformat=unix nowrap spell: )
