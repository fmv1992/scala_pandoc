#! /bin/bash

# Halt on error.
set -e

source ./other/test/test_library.sh

java -jar ./scala_pandoc/target/scala-2.13/scala_pandoc.jar --help
java -jar ./scala_pandoc/target/scala-2.13/scala_pandoc.jar --version

# vim: set filetype=sh fileformat=unix nowrap:
