# Scala_Pandoc

A [Pandoc][pandoc] library written in Scala.

A command line utility to process Pandoc's json.

## Library

The library is partially implemented.

It must follow the conventions and types from: <https://hackage.haskell.org/package/pandoc-types-1.19/docs/Text-Pandoc-Definition.html>.

## Command Line Utility

The command line utility has the following options:

```{pipe="sh -"}
java -jar ./scala_pandoc/target/scala-2.12/scala_pandoc.jar --help
```

## TODO

*   Have 90% of code coverage.

*   Document the code according to:

    1.  <https://docs.scala-lang.org/style/scaladoc.html>

    1.  <https://docs.scala-lang.org/overviews/scaladoc/for-library-authors.html>

    *   Create a GNU style documentation.

    *   Move this readme to a documentation folder; create readme programatically.

*   Add the following references:

    *   https://github.com/Warbo/panhandle

    *   https://github.com/Warbo/panpipe

    *   https://github.com/jgm/pandocfilters

    *   

*   Learn how to publish a package.

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
