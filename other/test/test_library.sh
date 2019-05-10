#! /bin/bash

function test_fails () {
    result="$?"
    if [ "$result" -ne 0 ]; then
        return 0
    else
        exit 1
    fi
}

function test_suceeds () {
    result="$?"
    if [ "$result" -eq 0 ]; then
        return 0
    else
        exit 1
    fi
}

# vim: set filetype=sh fileformat=unix nowrap:
