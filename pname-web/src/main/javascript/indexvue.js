/*
 * Copyright 2021 agwlvssainokuni
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
// ENTRY
const qs = require("querystring");

const uri = (function (root) {
	if (root.endsWith("/")) {
		root = root.substring(0, root.length - 1);
	}
	return function (path) {
		return root + path;
	}
})(document.querySelector("meta[name='context-root']").getAttribute("content"));

const data = {
	pnameln: ""
}

function convert(event) {
	const vm = this;
	const lnVal = vm.pnameln;
	const pnameType = event.target.value;
	axios.post(uri("/pname?tsv"), qs.stringify({
		ln: lnVal,
		type: pnameType
	})).then(function (response) {
		vm.pnameln = response.data;
	});
}

const vm = new Vue({
	el: "#pname-web",
	data: data,
	methods: {
		convert: convert
	}
});
