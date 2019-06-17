This is the first paragraph.

This is the second paragraph and today is: `date '+%B %d, %Y'`{.embed pipe="sh -"}. This date should **BE** expanded and part of the natural text.

This is the third paragraph there follows some random data: `gpg --armor --gen-random 0 10`{pipe="sh"}. This date should **NOT** be expanded and should be presented as code.

* * *

This is the fourth BLOCK OF TEXT:

```{pipe="sh -"}
tree "$HOME/downloads"
```

It should contain the user name expanded.

This is the end of the fourth BLOCK OF TEXT.

* * *
