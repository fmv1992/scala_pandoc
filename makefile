# Set variables.
SHELL := /bin/bash
ROOT_DIR := $(shell dirname $(realpath $(lastword $(MAKEFILE_LIST))))

export PROJECT_NAME ?= $(notdir $(ROOT_DIR))

FINAL_TARGET := ./scala_pandoc/target/scala-2.12/scala_pandoc.jar

SCALA_FILES := $(shell find . -name 'tmp' -prune -o -iname '*.scala' -print)
SBT_FILES := $(shell find $(PROJECT_NAME) -iname "build.sbt")

# export SCALAC_OPTS := -Ywarn-dead-code -Xlint:unused
#                                        ↑↑↑↑↑↑↑↑↑↑↑↑↑
# 🐛: <https://github.com/oleg-py/better-monadic-for/issues/37>.
export SCALAC_OPTS := -Ywarn-dead-code
export METALS_ENABLED ?= false
export PATH := $(PATH):$(ROOT_DIR)/other/bin/

# IMPORTANT: spaces are important here.
FILTER_OUT = $(foreach v,$(2),$(if $(findstring $(1),$(v)),,$(v)))

BASH_TEST_FILES := $(shell find . -name 'tmp' -prune -o -iname '*test*.sh' -print)
# ???: How to handle farsi files?
MD_EXAMPLE_FILES := $(shell find ./other/example -mindepth 1 -maxdepth 1 -iname '*.md')
MD_EXAMPLE_VALID_FILES := $(call FILTER_OUT,has_error,$(MD_EXAMPLE_FILES))
JSON_EXAMPLE_FILES := $(addprefix tmp/, \
    $(notdir $(patsubst %.md, %.json, $(MD_EXAMPLE_FILES))))
PDF_EXAMPLE_FILES := $(addprefix tmp/, \
    $(notdir $(patsubst %.md, %.pdf, $(MD_EXAMPLE_VALID_FILES))))

all: $(FINAL_TARGET)

check:
	@# Check that `scala_script` exists.
	command -v scala_script

clean:
	find . -iname '*.class' -print0 | xargs -0 rm -rf
	find . -path '*/project/*' -type d -prune -print0 | xargs -0 rm -rf
	find . -iname 'target' -print0 | xargs -0 rm -rf
	find . -iname '.bsp' -print0 | xargs -0 rm -rf
	find . -iname '.metals' -print0 | xargs -0 rm -rf
	find . -iname '.bloop' -print0 | xargs -0 rm -rf
	find . -type d -empty -delete
	rm $(FINAL_TARGET) $(JSON_EXAMPLE_FILES) $(PDF_EXAMPLE_FILES) || true

format:
	scalafmt --config ./$(PROJECT_NAME)/.scalafmt.conf $(SCALA_FILES) $(SBT_FILES)

coverage:
	make clean
	make json
	cd ./scala_pandoc && sbt clean coverage test coverageReport
	echo "Report can be found on '$$(find . -iname "index.html")'."

assembly: $(FINAL_TARGET)

$(FINAL_TARGET): $(JSON_EXAMPLE_FILES) $(SCALA_FILES) $(SBT_FILES)
	@# https://stackoverflow.com/questions/27447705/grep-without-filtering
	@# cd ./scala_pandoc && { { sbt test assembly | awk -v rc=0 '/\[.*error.*\]/ { rc=1 } 1; END {exit rc}' ; } || exit 1 ; }
	cd ./scala_pandoc && sbt assembly
	touch --no-create -m $@

sbt_shell:
	cd ./scala_pandoc && sbt

test: check dev json pdf test_sbt test_bash readme.md ./tmp/readme.html

test_format:
	scalafmt --config ./$(PROJECT_NAME)/.scalafmt.conf --test $(SCALA_FILES) $(SBT_FILES)

test_sbt: json | $(FINAL_TARGET)
	cd ./scala_pandoc && sbt test

test_bash: json $(BASH_TEST_FILES) | $(FINAL_TARGET)

test_pandoc2: json | $(FINAL_TARGET)
	pandoc2 --to json $(firstword $(JSON_EXAMPLE_FILES)) \
		| pandoc2 --to json --from json \
		| pandoc2 --to json --from json \
		| pandoc2 --to json --from json \
		| pandoc2 --to json --from json \
		| pandoc2 --to json --from json \
		| pandoc2 --from json --to markdown > ./tmp/makefile_test_pandoc2.md

compile: $(SBT_FILES) $(SCALA_FILES)
	cd ./scala_pandoc && sbt compile

dev:
	cp -f ./other/git_hooks/git_pre_commit_hook.sh ./.git/hooks/pre-commit || true
	chmod a+x ./.git/hooks/pre-commit
	rm ./.git/hooks/pre-push || true  # ???: Remove this after 2022-10-20.

json: $(JSON_EXAMPLE_FILES)

vpath %.md other/example
tmp/%.json: %.md
	pandoc2 --to json $< | python3 -m json.tool --sort-keys > $@
	touch --no-create -m $@

pdf: $(PDF_EXAMPLE_FILES)

tmp/%.pdf: tmp/%.json | $(FINAL_TARGET)
	{ \
		set -e ;\
		pandoc2 --to json $< \
			| java -jar ./scala_pandoc/target/scala-2.12/scala_pandoc.jar \
					--evaluate \
					--embed \
					--farsi-to-rtl \
			| pandoc2 \
				--from json \
				--to latex \
				--atx-headers \
				--columns=79 \
				--dpi 300 \
				--pdf-engine xelatex \
				--self-contained \
				--standalone \
				-s - -o $@ ;\
	}

test%.sh: .FORCE | $(FINAL_TARGET)
	bash -xv $@

readme.md: $(FINAL_TARGET) ./documentation/readme.md
	pandoc2 --from markdown --to json ./documentation/readme.md \
		| java -jar ./scala_pandoc/target/scala-2.12/scala_pandoc.jar \
				--evaluate \
				--embed \
		| pandoc2 \
			--from json \
			--to gfm \
			> /tmp/$(notdir $@)
	cat \
		<(echo '[comment]: # ( ???: XXX: Do not edit this file directly! Edit `./documentation/readme.md` and `make` this file.)') \
		<(echo ) \
		/tmp/$(notdir $@) > $@
	rm /tmp/$(notdir $@)

tmp/readme.html: readme.md | $(FINAL_TARGET)
	pandoc2 --output $@ --from gfm --to html $<

.FORCE:

# .EXPORT_ALL_VARIABLES:
#

.PHONY: all clean coverage assembly test test_sbt test_bash test_pandoc2 compile dev json pdf

# vim: set noexpandtab foldmethod=marker fileformat=unix filetype=make nowrap foldtext=foldtext():
