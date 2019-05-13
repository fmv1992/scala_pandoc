# Scala\_Pandoc

A [Pandoc](http://pandoc.org) library written in Scala.

A command line utility to process Pandoc’s json.

## Library

The library is partially implemented.

It must follow the conventions and types from:
<https://hackage.haskell.org/package/pandoc-types-1.19/docs/Text-Pandoc-Definition.html>.

## Command Line Utility

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

The first day of 2010 was: Friday.

See the evaluate functionality to get a better usage of embedding.

### `Evaluate` functionality

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

### `Farsi-to-rtl` functionality

Encapsulate any sequence of Farsi characters with a `\rl{` prefix and a
`}` suffix. This allows for seamless composition with Farsi and Latin
characters.

    echo 'A translation of the sentence "اسم مولف این برنمه فِلیپه است." is "The name of the author of this program is Felipe.".' \
        | pandoc2 --from markdown --to json \
        | java -jar ./scala_pandoc/target/scala-2.12/scala_pandoc.jar --farsi-to-rtl \
        | pandoc2 --from json --to markdown

Gives us:

    A translation of the sentence "\rl{اسم} \rl{مولف} \rl{این} \rl{برنمه}
    \rl{فِلیپه} \rl{است}." is "The name of the author of this program is
    Felipe.".

See: <https://ctan.org/pkg/xepersian?lang=en>.

## Credits

The functionality of `embed` and `evaluate` were inspired by:

1.  <https://github.com/Warbo/panhandle>

2.  <https://github.com/Warbo/panpipe>

However neither of them work with `pandoc2`.

## TODO

  - Have 90% of code coverage.

  - Document the code according to:
    
    1.  <https://docs.scala-lang.org/style/scaladoc.html>
    
    2.  <https://docs.scala-lang.org/overviews/scaladoc/for-library-authors.html>
    
    <!-- end list -->
    
      - Enforce/Create a GNU style documentation.

## Bugs

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
