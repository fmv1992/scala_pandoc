```{#id01 .scala computationTreeId="a" pipe="scala_script"}
val a = 10
```

```{#id02 .scala computationTreeId="a" pipe="scala_script"}
println(f"""
${a}%d
${a}%03d
${a}%05d
""".trim)
```

<!-- [EvalAndSubstsCorrect] -->
