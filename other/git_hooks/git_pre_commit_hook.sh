#! /bin/bash

# This git pre commit hook is intended to work on both cygwin and unix
# machines.
# It should be symlinked to ../../.git/hooks/pre-commit.

# Halt on error.
set -euxo pipefail

test -d ./.git

git diff --name-only --cached --diff-filter=ACMRTUXB \
    | { grep '\.scala' || true; } \
    | parallel \
        --no-run-if-empty \
        --verbose \
        -I % \
        --jobs $((2*$(nproc))) \
        "vim -i NONE -n -c 'VimScalafmt' -c 'noautocmd x!' %"
git diff --name-only --cached --diff-filter=ACMRTUXB \
    | xargs --verbose git add --force
# --diff-filter=ACDMRTUXB
#                 ↑
# Removed deleted parameter.

versionfile=./scala_pandoc/src/main/resources/version
test -f $versionfile

make --jobs 4 dev
make --jobs 4 clean
make --jobs 4 json
make --jobs 4 assembly
make --jobs 4 test

# Bump minor version.
fileversion=$(find $versionfile -name 'version' -type f)

# ???: Do not bump if version file is already added:
# git diff --name-only --cached --diff-filter=AM
tmpversion=$(mktemp)
if verify_is_backwards_compatible.sh "$PWD" 0; then
    # Bump patch version.
    cat "${fileversion}" | python3 -c "import sys ; i = sys.stdin.read(); j = i.split('.') ; j[2] = str(int(j[2]) + 1) ; print('.'.join(j), end='')" > "$tmpversion"
else
    # Bump minor and reset patch.
    cat "${fileversion}" | python3 -c "import sys ; i = sys.stdin.read(); j = i.split('.') ; j[-1] = '0'; j[1] = str(int(j[1]) + 1) ; print('.'.join(j), end='')" > "$tmpversion"
fi
mv "$tmpversion" "${fileversion}"
git add -f "$fileversion"

# `verify_is_backwards_compatible.sh` was once displayed here.

echo "Do not forget to run: 'stty sane'"

# vim: set filetype=sh fileformat=unix nowrap:
