
#! /bin/bash

# Halt on error.
set -e

source ./other/test/test_library.sh

cat ./tmp/example_farsi_03.json \
    | java -jar ./scala_pandoc/target/scala-2.12/scala_pandoc.jar --farsi-to-rtl \
    | python3 -m json.tool \
    | pandoc2 --output /dev/null --from json --to markdown -
test_suceeds

# vim: set filetype=sh fileformat=unix nowrap:
