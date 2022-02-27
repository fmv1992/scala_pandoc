#! /bin/bash

# Halt on error.
set -e

source ./other/test/test_library.sh

cat ./tmp/example_03_code_block.json \
    | java -jar ./scala_pandoc/target/scala-2.13/scala_pandoc.jar --evaluate \
    | python3 -m json.tool \
    | pandoc2 --output /dev/null --from json --to markdown -
test_suceeds

# vim: set filetype=sh fileformat=unix nowrap:
