---
papersize: a4
documentclass: article
replace-variables:
    - unescapedToday = 2019-01-01
    - †escapedToday = `date -d '2019-01-02' '+%y-%m-%d'`{pipe="sh"}
    - name = Felipe
    - expensiveComputation = `echo "linux"`{pipe="sh"}
    - †anotherEOSentence† = `echo "EOS"`{pipe="sh"}

---

The unescapedToday must be equal to first of January 2019. UnescapedToday shows that the program is case insensitive.

Escapes also work with symbols: †escapedToday and they are also space sensitive: † escapedToday.

Using common names as variables may lead to undesirable effects: such as **n**ame: "My name is Felipe". See?

The usefulness of this is that **e**xpensiveComputations can be cached: expensiveComputation.

Another pagragraph †anotherEOSentence†.

[comment]: # ( vim: set filetype=markdown fileformat=unix nowrap spell: )
