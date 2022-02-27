[comment]: # ( ???: XXX: Do not edit this file directly! Edit `./documentation/readme.md` and `make` this file.)

![Build
status](https://travis-ci.com/fmv1992/scala_pandoc.svg?branch=dev)
[![codecov](https://codecov.io/gh/fmv1992/scala_pandoc/branch/dev/graph/badge.svg)](https://codecov.io/gh/fmv1992/scala_pandoc)

# scala\_pandoc

A [Pandoc](http://pandoc.org) library written in Scala.

Also a command line utility to process Pandoc’s json.

## Library

This library partially implements all of the type from pandoc. It must
follow the conventions and types from:
<https://hackage.haskell.org/package/pandoc-types-1.19/docs/Text-Pandoc-Definition.html>.

## Command Line Utility

The command line utility has the following options:

    # java -jar ./scala_pandoc/target/scala-2.13/scala_pandoc.jar --help
    scala_pandoc --embed --evaluate --evaluate-scala --farsi-to-rtl --help --input --replace-variables --version
        --embed: Embed code as if it were part of the text. Has a good sinergy with evaluate.
        --evaluate: Evaluate code blocks and substitute them in the place of its source code.
        --evaluate-scala: ???
        --farsi-to-rtl: <https|//ctan.org/pkg/xepersian?lang=en>.
                        <https|//ctan.org/pkg/xepersian?lang=en>.
        --help: Show this help text and exit.
        --input: Define your input file. Otherwise read from stdin.
        --replace-variables: ???
        --version: Show program version.

[This file](https://github.com/fmv1992/scala_pandoc/blob/dev/readme.md)
was creating using `scala_pandoc` from [this other
file](https://github.com/fmv1992/scala_pandoc/blob/dev/documentation/readme.md).

### `Embed` functionality

Embeds code as part of text. Mostly used after `evaluate`.

Example with embed only:

    ```{.embed}
    This is inside a code block.
    ```

Becomes:

This is inside a code block.

Example with embed and evaluate:

    ```{.embed pipe="sh -"}
    echo "The first day of 2010 was: $(date -d '2010-01-01' '+%A')."
    ```

Becomes:

echo “The first day of 2010 was: $(date -d ‘2010-01-01’ ‘+%A’).”

See the evaluate functionality to get a better usage of embedding.

### `Evaluate` functionality

#### Regular (independent) code

Evaluate code blocks and substitute their results as a code block
instead of the original code block.

    ```{pipe="python3 -"}
    print("scala_pandoc")
    ```

Gives:

    scala_pandoc

Similarly one can use `joiner="SomeWord:"` to give an explanation flow:

    ```{joiner="Tells the name of a great software:" pipe="python3 -"}
    print("scala_pandoc")
    ```

Becomes:

-----

    print("scala_pandoc")

Tells the name of a great software:

    scala_pandoc

-----

#### Sequential code

Evaluate code blocks sequentially. Currently only Scala is supported.
Consider the document:

    This document breaks down the code in two parts:
    
    Part 01:
    
    ```{.scala computationTreeId="a" pipe="scala_script"}
    val x1 = 1
    println("x1: " + x1)
    ```
    
    Part 02:
    
    ```{.scala computationTreeId="a" pipe="scala_script"}
    val x2 = x1 + 1
    println("x2: " + x2)
    ```

It outputs:

    This document breaks down the code in two parts:
    
    Part 01:
    
    ``` {.scala}
    x1: 1
    ```
    
    Part 02:
    
    ``` {.scala}
    x2: 2
    ```

The use of the `computationTreeId=""` map create blocks of independent
code which can be used in the same file. In other words there can be
`computationTreeId="id01"` and `computationTreeId="id02"` in the same
file, and both computations would run independently.

### `Farsi-to-rtl` functionality

Encapsulate any sequence of Farsi characters with a `\rl{` prefix and a
`}` suffix. This allows for seamless composition with Farsi and Latin
characters.

    echo 'A translation of the sentence "اسم مولف این برنمه فِلیپه است." is "The name of the author of this program is Felipe.".' \
        | pandoc2 --from markdown --to json \
        | java -jar ./scala_pandoc/target/scala-2.13/scala_pandoc.jar --farsi-to-rtl \
        | pandoc2 --from json --to markdown

Gives us:

    A translation of the sentence "\rl{اسم} \rl{مولف} \rl{این} \rl{برنمه}
    \rl{فِلیپه} \rl{است}." is "The name of the author of this program is
    Felipe.".

See: <https://ctan.org/pkg/xepersian?lang=en>.

## Dependencies

<https://github.com/fmv1992/fmv1992_scala_utilities>.

## See also

The functionality of `embed` and `evaluate` were inspired by:

1.  <https://github.com/Warbo/panhandle>.

2.  <https://github.com/Warbo/panpipe>.

However neither of them work with `pandoc2`.

## TODO

### New TODO

  - Simplify things: `sbt/scala` based tests should run in 10 seconds or
    have a very strong reason not to comply.
    
      - I imagine that happens because the code invokes a shell which
        also invokes `Scala` (which is **slow**). Thus what we can do is
        to use
        [`Tag`](https://github.com/fmv1992/scala_pandoc/blob/1f745dc7823b517db1677927c352f1bf966627e2/scala_pandoc/src/test/scala/fmv1992/scala_pandoc/TestMain.scala#L8)s.

  - The code is not functional. There are several `throw new
    Exception()` being thrown around.

\*̶ F̶i̶x̶ g̶i̶t̶ h̶o̶o̶k̶s̶.̶

\*̶ A̶d̶d̶ a̶u̶t̶o̶m̶a̶t̶i̶c̶ c̶o̶d̶e̶ f̶o̶r̶m̶a̶t̶t̶i̶n̶g̶.̶

\*̶ B̶u̶m̶p̶ t̶h̶e̶ `̶S̶c̶a̶l̶a̶`̶ v̶e̶r̶s̶i̶o̶n̶ t̶o̶ t̶h̶e̶
l̶a̶t̶e̶s̶t̶ `̶2̶.̶x̶`̶.̶

\*̶ B̶u̶m̶p̶ t̶h̶e̶ `̶s̶b̶t̶`̶ v̶e̶r̶s̶i̶o̶n̶ t̶o̶ t̶h̶e̶ l̶a̶t̶e̶s̶t̶.̶

### Old TODO

  - Have 90% of code coverage.

  - Document the code according to:
    
    1.  <https://docs.scala-lang.org/style/scaladoc.html>.
    
    2.  <https://docs.scala-lang.org/overviews/scaladoc/for-library-authors.html>.
    
    <!-- end list -->
    
      - Enforce/Create a GNU style documentation.

  - Add `docker` support.

  - Do not depend on specific paths such as `2.12`/`2.13`.

## Bugs

1.  See commit `3349664ae74e5a73bb7fbd71c02d0acee58bc600` at
    `fundamentals_of_music_processing_audio_analysis_algorithms_applications`
    project.

2.  Sequential evaluation of code blocks and correct substitution: \[✘\]
    
    Tag: `[EvalAndSubstsCorrect]`.
    
    Description: Replacement of variables is really tricky. See for
    example `commfad88b8`:
    
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
    
    The trailing dot does not get split by Pandoc. Thus reliable
    substitution is not possible.

3.  Replacement of variables: \[✘\]
    
    Tag: `[BugReplacementOfVariables]`.
    
    Description: Replacement of variables is really tricky. See for
    example `commfad88b8`:
    
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
    
    The trailing dot does not get split by Pandoc. Thus reliable
    substitution is not possible.

<!-- vim: set filetype=pandoc fileformat=unix nowrap spell -->
