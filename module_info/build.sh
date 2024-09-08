#!/bin/sh -e
#
# Shell dependencies: cp, mkdir (of coreutils); find (of findutils).
#
# Compile and package the project classes.

agenda=1				# Compilation only.

case "$1" in
-h | --help)
	echo >&2 "Usage: $0 [package]"
	exit 100
	;;
package)
	agenda=$((${agenda} | 4))	# Compilation and packaging.
	shift
esac

set +f					# Enable pathname expansion.
arch=arch
src=src
demo_src_dir="${src}/org.module.info.demo"
tester_src_dir="${src}/org.module.info.tester"
common_bin_dir="bin/modules"
test -x "${src}/" || exit 101		# Check the current working directory.
test -x "`command -v find`" || exit 102
test -x "`command -v cp`" || exit 103
test -x "`command -v mkdir`" || exit 104
echo >&2 "COMPILING..."

if test $((${agenda} & 4)) -ne 0
then
	javac -Xdiags:verbose -Xlint -d "${common_bin_dir}" \
		--module-source-path "${src}/*/classes" \
		$(find src/*/classes/ -type f -name \*.java)
	echo >&2 "PACKAGING..."
	test -d "${common_bin_dir}/org.module.info.tester/META-INF" || \
		mkdir -p "${common_bin_dir}/org.module.info.tester/META-INF"
	cp -p -t "${common_bin_dir}/org.module.info.tester/META-INF/" ../LICENSE
	jar --verbose --create --file="${arch}/tester.jar" \
		--module-version=0.0.1 \
		--module-path "${tester_src_dir}/classes" \
		-C "${common_bin_dir}/org.module.info.tester" .
	test -d "${common_bin_dir}/org.module.info.demo/META-INF" || \
		mkdir -p "${common_bin_dir}/org.module.info.demo/META-INF"
	cp -p -t "${common_bin_dir}/org.module.info.demo/META-INF/" ../LICENSE
	jar --verbose --create --file="${arch}/demo.jar" \
		--module-version=0.0.1 \
		--module-path "${demo_src_dir}/classes" \
		-C "${common_bin_dir}/org.module.info.demo" .
else
	javac -Xdiags:verbose -Xlint -d "${common_bin_dir}" \
		--module-source-path "${src}/*/classes" \
		$(find src/*/classes/ -type f -name \*.java)
fi

if false
then
	## The -link target is RELATIVE to the rightmost directory of -d, e.g.
	## for a repository located at /tmp/repos/module-info.git, it can be
	## LINK_TO_JSR='-link ../../../../../../../tmp/docs/JSR-000396/java-se-21-fr-spec/api'.
	javadoc -tag 'implSpec:a:Implementation Requirements:' \
		-d docs/tester_docs/ $LINK_TO_JSR \
		-verbose -source 21 -protected \
		--expand-requires transitive --show-module-contents api \
		--add-modules org.module.info.tester \
		--module-source-path src/\*/classes \
		--module org.module.info.tester
	javadoc -tag 'implSpec:a:Implementation Requirements:' \
		-d docs/demo_docs/ $LINK_TO_JSR \
		-verbose -source 21 -protected \
		--expand-requires transitive --show-module-contents api \
		--add-modules org.module.info.demo,org.module.info.tester \
		--module-source-path src/\*/classes \
		--module org.module.info.demo,org.module.info.tester
fi
