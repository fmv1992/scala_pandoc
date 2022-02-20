#! /bin/bash

# Halt on error.
set -euxo pipefail

test -d ./.git

stdin=$(< /dev/stdin)

if echo "$stdin" | grep -E '\<master\>' 1> /dev/null 2> /dev/null; then
    full_checks=5
elif echo "$stdin" | grep -E '\<dev\>' 1> /dev/null 2> /dev/null; then
    full_checks=2
else
    full_checks=1
fi

for i in $(seq 1 "$full_checks"); do
    make clean && make test
done

make coverage
make json

# vim: set filetype=sh fileformat=unix wrap:
