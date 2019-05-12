#! /bin/bash

# Halt on error.
set -e

# Go to execution directory.
cd $(dirname $0)

# Arg 01: full path of the project.
# Arg 02: number of commits to check.

# ???: command to clean.
# ???: command to find the test files.

# if [ "$#" -gt 2 ]
# then
#     echo "Illegal number of parameters ($#): '$*'. Expected 1 or 2."
#     echo "The parameters are 'file' and 'separator'."
#     return 1
# fi
# if [ "$#" -eq 1 ]
# then
#     file=$1
#     sep=','
# elif [ "$#" -eq 2 ]
# then
#     file=$1
#     sep=$2
# fi

tempfolder=$(mktemp -d)

cd $tempfolder

# git clone "file://$1" "$(basename $1)_temp"
cp -rf "$1" "$(basename $1)_temp"

cd "$(basename $1)_temp"


set +e
for i in $(seq 0 "$2")
do
    make clean | tee /dev/stderr
    # echo "Loop: $i"
    latest_commit=$(git rev-parse --verify "HEAD~$i")
    test_files=$(git ls-tree --name-only -r "$latest_commit" | grep -i test)
    echo "${test_files}" | xargs -I % -d '\n' git checkout "${latest_commit}" -- %
    # ???: How to introduce a timeout?
    make test | tee /dev/stderr
    pipestatus="${PIPESTATUS[0]}"
    if test "${pipestatus}" -eq 0
    then
        :
    else
        echo "$(git rev-parse --verify "HEAD~$((i-1))")" | tee /dev/stderr
        exit "${pipestatus}"
    fi
done
set -e

exit 0
