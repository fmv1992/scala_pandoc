# Set variables.
SHELL := /bin/bash
ROOT_DIR := $(shell dirname $(realpath $(lastword $(MAKEFILE_LIST))))

FINAL_TARGET := ./scala_pandoc/target/scala-2.12/scala_pandoc.jar

SCALA_FILES := $(shell find . -name 'tmp' -prune -o -iname '*.scala' -print)

export SCALAC_OPTS := -Ywarn-dead-code -Xlint:unused

# IMPORTANT: spaces are important here.
FILTER_OUT = $(foreach v,$(2),$(if $(findstring $(1),$(v)),,$(v)))

BASH_TEST_FILES := $(shell find . -name 'tmp' -prune -o -iname '*test*.sh' -print)
# ???: How to handle farsi files?
MD_EXAMPLE_FILES := $(shell find ./other/example -iname '*.md')
MD_EXAMPLE_VALID_FILES := $(call FILTER_OUT,has_error,$(MD_EXAMPLE_FILES))
JSON_EXAMPLE_FILES := $(addprefix tmp/, \
	$(notdir $(patsubst %.md, %.json, $(MD_EXAMPLE_FILES))))
JSON_EXAMPLE_VALID_FILES := $(call FILTER_OUT,has_error,$(JSON_EXAMPLE_FILES))
PDF_EXAMPLE_FILES := $(addprefix tmp/, \
	$(notdir $(patsubst %.md, %.pdf, $(MD_EXAMPLE_VALID_FILES))))

all: $(FINAL_TARGET)

clean:
	find . -iname '*.class' -print0 | xargs -0 rm -rf
	find . -path '*/project/*' -type d -prune -print0 | xargs -0 rm -rf
	find . -iname 'target' -print0 | xargs -0 rm -rf
	find . -type d -empty -delete
	rm $(FINAL_TARGET) $(JSON_EXAMPLE_VALID_FILES) $(PDF_EXAMPLE_FILES) || true

coverage:
	make clean json
	cd ./scala_pandoc && sbt clean coverage test coverageReport
	echo "Report can be found on '$$(find . -iname "index.html")'."

# ???: make the assembly process general.
# ???: (note01): Shipping for "pandoc 1.16.0.2": it has to be shipped as an
# executable filter and be used as a `--filter` parameter as pandocs arguments.
assembly: $(FINAL_TARGET)

$(FINAL_TARGET): $(JSON_EXAMPLE_VALID_FILES) $(SCALA_FILES) $(SBT_FILES)
	@# https://stackoverflow.com/questions/27447705/grep-without-filtering
	@# cd ./scala_pandoc && { { sbt test assembly | awk -v rc=0 '/\[.*error.*\]/ { rc=1 } 1; END {exit rc}' ; } || exit 1 ; }
	cd ./scala_pandoc && sbt test assembly
	touch --no-create -m $@

test: dev json $(FINAL_TARGET) pdf test_sbt test_bash

test_sbt: json
	cd ./scala_pandoc && sbt test

test_bash: json $(BASH_TEST_FILES)

test_pandoc2: json
	pandoc2 --to json $(firstword $(JSON_EXAMPLE_VALID_FILES)) \
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
	cp -f ./other/git_hooks/git_pre_push.sh ./.git/hooks/pre-push || true
	chmod a+x ./.git/hooks/pre-commit
	chmod a+x ./.git/hooks/pre-push

json: $(JSON_EXAMPLE_VALID_FILES)

vpath %.md other/example
tmp/%.json: %.md
	pandoc2 --to json $< | python3 -m json.tool --sort-keys > $@
	touch --no-create -m $@

pdf: $(PDF_EXAMPLE_FILES) $(FINAL_TARGET)

tmp/%.pdf: tmp/%.json
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

test%.sh: .FORCE
	bash -xv $@

.FORCE:

# .EXPORT_ALL_VARIABLES:

.PHONY: all clean coverage assembly test test_sbt test_bash test_pandoc2 compile dev json pdf

# vim: set noexpandtab foldmethod=marker fileformat=unix filetype=make nowrap foldtext=foldtext():