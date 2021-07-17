#!/bin/bash -e
#
# Copyright 2020,2021 agwlvssainokuni
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

# ディレクトリ解決の起点(このスクリプトを配置したディレクトリ)。
basedir=$(cd $(dirname ${BASH_SOURCE[0]}) && pwd)

######################################################################
# (1) コマンドライン引数を解析する。
usage_and_exit() {
	echo "Usage: $0 [options]" 1>&2
	echo "  Options" 1>&2
	echo "    -s SRCDIR" 1>&2
	echo "    -d DESTDIR" 1>&2
	exit $1
}

# 構成項目。
conf_src=./src/main/javascript
conf_dst=src/main/resources/static/javascript
#conf_dst=build/resources/main/static/javascript

while getopts s:d:h OPT; do
        case $OPT in
        s) conf_src=${OPTARG%/};;
        d) conf_dst=${OPTARG%/};;
        h) usage_and_exit 0;;
        \?) usage_and_exit -1;;
        esac
done
shift $((OPTIND - 1))

######################################################################
# (2) 主処理の実体を定義する。

# (a) 「entry」ブロックの中身を形成する。
create_entries() {
	let count=1
	(cd ${conf_src} && find . -type f -name '*.js') | sort | cut -c3- |\
	while read file; do
		(cd ${conf_src} && grep --quiet "^ *// *ENTRY" ${file}) || continue
		[[ ${count} -eq 1 ]] || echo ","
		echo -n "		\"${file}\": \"${conf_src}/${file}\""
		count=$((count + 1))
	done
}

# (b) 「webpack.config.js」ファイルを形成する。
cat <<__END_OF_TEMPLATE__ > webpack.config.js
module.exports = {
	output: {
		filename: "[name]",
		path: __dirname + "/${conf_dst}"
	},
	entry: {
$(create_entries)
	}
};
__END_OF_TEMPLATE__
