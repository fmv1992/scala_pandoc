First paragraph: `val x1 = "x1a"`{.scala computationTreeId="a" pipe="scala_script"}.

Second paragraph: `val x1 = 1`{.scala computationTreeId="b" pipe="scala_script"}. Then `val x2 = x1 + 1`{.scala computationTreeId="b" pipe="scala_script"}.

*   Then on a nested bullet point and itemization:

    1.  Enumration:

        ```{.scala computationTreeId="b" pipe="scala_script"}
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

    *   Item:

        ```{.scala computationTreeId="b" pipe="scala_script"}
        val x5 = x4 + 1
        println("x5: " + x5)
        println(NoObject.attr)
        ```

        Which also uses computationTreeId `a`:

        ```{.scala computationTreeId="a" pipe="scala_script"}
        require(x1 + "|" == "x1a|")
        // Be sure that NoObject is different from before is not in scope.
        object NoObject {
            val attr = "no_attr"
        }
        require((NoObject.attr == "no_attr") && (NoObject.attr != "attr"))
        ```

Then a final change:

```{.scala computationTreeId="b" pipe="scala_script"}
val x6 = x5 + 1
assert(x6 == 6)
println("x6: " + x6)
println("NoObject still present: " + NoObject.attr)
```

[comment]: # ( vim: set filetype=markdown fileformat=unix nowrap spell spelllang=en: )
