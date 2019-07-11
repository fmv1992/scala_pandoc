First paragraph: `val x1 = "x1a"`{.scala computationTreeId="a"}.

Second paragraph: `val x1 = 1`{.scala computationTreeId="b"}. Then `val x2 = x1 + 1`{.scala computationTreeId="b"}.

*   Then on a nested bullet point and itemization:

    1.  Enumeration:

        ```{.scala joiner="Gives:" computationTreeId="b" pipe="scala_script"}
        val x3 = x2 + 1
        println("x2: " + x2)
        println("x3: " + x3)
        ```

    2.  Second enumeration point.

    Normal paragraph.

    ```{.scala computationTreeId="b" pipe="scala_script"}
    val x4 = x3 + 1
    println("x4: " + x4)

    object NoObject {
        val attr = "attr"
    }
    ```

    Then another itemization:

    *   The following code is part of the computation, but it is not evaluated:

        ```{.scala computationTreeId="b"}
        val x5 = x4 + 1
        println("x5: " + x5)
        println(NoObject.attr)
        ```

        There are no print results above.

        This uses computationTreeId `a`:

        ```{.scala computationTreeId="a"}
        /** If the line below get uncommented:
         *
         * ```
         * // require(1 == 2)
         * ```
         *
         * Then an error is generated. That is because even though this block it is
         * not directly marked for execution (that is, there are no `pipe=""`) it is
         * still part of a larger computation (that is, ti has
         * a `computationTreeId="a"}`).
         *
         */

        require(x1 + "|" == "x1a|", "here1")
        // Be sure that NoObject is different from before is not in scope.
        object NoObject {
            val attr = "no_attr"
        }
        require((NoObject.attr == "no_attr") && (NoObject.attr != "attr"), "here2")
        ```

        It is important to add a final print (testwise):

        ```{.scala computationTreeId="a"}
        print("nonewlineprint_")
        println(NoObject)
        ```

Then a final change which depends on the evaluated but unsubstituted code above:

```{.scala joiner="Gives:" computationTreeId="b" pipe="scala_script"}
val x6 = x5 + 1
assert(x6 == 6)
println("x6: " + x6)
println("NoObject still present: " + NoObject.attr)
```

[comment]: # ( vim: set filetype=markdown fileformat=unix nowrap spell spelllang=en: )
