#!/bin/sh -e
#
# Shell dependencies: cp, mkdir, mv (of coreutils); find (of findutils).
#
# Compile, test, and package the project classes.

agenda=1				# Compilation only.

case "$1" in
-h | --help)
	echo >&2 "Usage: $0 [test | package]"
	exit 100
	;;
test)	agenda=$((${agenda} | 2))	# Compilation and testing.
	shift
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
tests_bin_dir="bin/tests/modules"
test -x "${src}/" || exit 101		# Check the current working directory.
test -x "`command -v find`" || exit 102
test -x "`command -v cp`" || exit 103
test -x "`command -v mkdir`" || exit 104
test -x "`command -v mv`" || exit 105
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
####	jar --describe-module --module-path arch --file arch/tester.jar
	test -d "${common_bin_dir}/org.module.info.demo/META-INF" || \
		mkdir -p "${common_bin_dir}/org.module.info.demo/META-INF"
	cp -p -t "${common_bin_dir}/org.module.info.demo/META-INF/" ../LICENSE
	jar --verbose --create --file="${arch}/demo.jar" \
		--module-version=0.0.1 \
		--module-path "${demo_src_dir}/classes" \
		-C "${common_bin_dir}/org.module.info.demo" .
####	jar --describe-module --module-path arch --file arch/demo.jar
elif test $((${agenda} & 2)) -ne 0
then
	javac -Xdiags:verbose -Xlint -d "${common_bin_dir}" \
		--module-source-path "${src}/*/classes" \
		$(find src/*/classes/ -type f -name \*.java)
	test ! -e "${demo_src_dir}/tests/module-info.peekaboo" ||
		mv "${demo_src_dir}/tests/module-info.peekaboo" \
			"${demo_src_dir}/tests/module-info.java"
	trap 'test -e "${demo_src_dir}/tests/module-info.java" &&
		mv "${demo_src_dir}/tests/module-info.java" \
			"${demo_src_dir}/tests/module-info.peekaboo" || :' \
							EXIT HUP INT QUIT TERM
	javac -Xdiags:verbose -Xlint -d "${tests_bin_dir}" \
		--module-path "${common_bin_dir}" \
		--module-source-path "${src}/*/tests" \
		--patch-module "org.module.info.demo=${demo_src_dir}/classes" \
		$(find src/*/tests/ -type f -name \*.java)
	cp -p -t "${tests_bin_dir}/org.module.info.demo/org/demo/tests/" \
		"${demo_src_dir}/tests/org/demo/tests/logging.properties"
	echo >&2 "TESTING..."
	java -Xdiag -XX:StartFlightRecording:class-loading=true \
		--add-modules org.module.info.tester \
		--module-path "${tests_bin_dir}:${common_bin_dir}" \
		--module org.module.info.demo/org.demo.tests.Tester
####	java --describe-module org.module.info.demo --module-path bin/tests/modules
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
