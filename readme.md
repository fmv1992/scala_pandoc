Scala\_Pandoc
=============

A [Pandoc](http://pandoc.org) library written in Scala.

A command line utility to process Pandoc's json.

Library
-------

The library is partially implemented.

It must follow the conventions and types from:
<https://hackage.haskell.org/package/pandoc-types-1.19/docs/Text-Pandoc-Definition.html>.

Command Line Utility
--------------------

The command line utility has the following options:

    scala_pandoc --embed --evaluate --evaluate-scala --farsi-to-rtl --help --input --replace-variables --version
        --embed: Embed code as if it were part of the text. Has a good sinergy with evaluate.
        --evaluate: Evaluate code blocks and substitute them in the place of its source code.
        --evaluate-scala: ???| Not implemented.
        --farsi-to-rtl: Add `\rl{` + x + `}` to your farsi text. See| <https|//ctan.org/pkg/xepersian?lang=en>.
        --help: Show this help text and exit.
        --input: Define your input file. Otherwise read from stdin.
        --replace-variables: ???| Not implemented.
        --version: Show program version.

TODO
----

-   Have 90% of code coverage.

-   Document the code according to:

    1.  <https://docs.scala-lang.org/style/scaladoc.html>

    2.  <https://docs.scala-lang.org/overviews/scaladoc/for-library-authors.html>

    -   Create a GNU style documentation.

    -   Move this readme to a documentation folder; create readme
        programatically.

-   Add the following references:

    -   https://github.com/Warbo/panhandle

    -   https://github.com/Warbo/panpipe

    -   https://github.com/jgm/pandocfilters

    -   

-   Learn how to publish a package.

Bugs
----

1.  Evaluate Scala code serially using a hack to signal end of blocks:
    \[✘\]

    Tag: `[PrintToMarkCodeBlock]`.

2.  Replacement of variables: \[✘\]

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

3.  Status: \[✔\]

    Description: Quoting only contiguous فارسی characters.

        * [۵۵%] Fyodor Dostoevsky - The Brothers Karamazov.

    Goes to:

        \rl{[۵۵%]} Fyodor Dostoevsky - The Brothers Karamazov.

    And it should put `{}` around \[

        {[}✘{]} Learn about fraud on your own (don't expect invitations).

    The JSON representation is:

        {
            "t": "Str",
            "c": "[\u06f5\u06f5%]"
        },

4.  Status: \[✘\]

    Description: ???.

    Tag: `[note01]`.
