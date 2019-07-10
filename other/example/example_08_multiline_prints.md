```{#id01 .scala computationTreeId="a" pipe="scala_script"}
val a = 10
```

```{#id02 .scala computationTreeId="a" pipe="scala_script"}
println(f"""
${a}%05d
${a}%015d
${a}%025d
""".trim)
```

<!-- [EvalAndSubstsCorrect] -->
