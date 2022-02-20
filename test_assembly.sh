#! /usr/bin/env bash

# Halt on error.
set -euxo pipefail

# Go to execution directory.
{ cd "$(dirname $(readlink -f "${0}"))" && git rev-parse --is-inside-work-tree > /dev/null 2>&1 && cd "$(git rev-parse --show-toplevel)"; } || cd "$(dirname "$(readlink -f ${0})")"
# Close identation: }
test -d ./.git

# Check that the state is clean.
! git status --untracked-files=all --porcelain=2 | grep '' 2>/dev/null

find . -iname '*.jar' | one

# vim: set filetype=sh fileformat=unix nowrap:
