```{#id01 .scala computationTreeId="a" pipe="scala_script"}
val a = 10
```

```{#id02 .scala computationTreeId="a" pipe="scala_script"}
println(f"""
${a}%d
${a}%X
${a}%o
""".trim)
```

<!-- https://docs.oracle.com/javase/6/docs/api/java/util/Formatter.html#detail -->

<!-- [EvalAndSubstsCorrect] -->
