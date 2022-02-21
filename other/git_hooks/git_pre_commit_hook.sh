#! /bin/bash

# This git pre commit hook is intended to work on both cygwin and unix
# machines.
# It should be symlinked to ../../.git/hooks/pre-commit.

# Halt on error.
set -euxo pipefail

test -d ./.git

# Always keep these files up to date.
make dev

make test_format test_sbt

# vim: set filetype=sh fileformat=unix nowrap:
