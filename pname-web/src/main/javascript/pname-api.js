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

import { uri, csrfToken } from "./resolver";

export { ln2pn };

const ln2pn = ((action) => {
	let headers = new Headers();
	if (csrfToken.header != null) {
		headers.append(csrfToken.header, csrfToken.token);
	}
	return async (pnameType, lnVal) => {
		let response = await fetch(action, {
			method: "POST",
			headers: headers,
			body: new URLSearchParams({
				type: pnameType,
				ln: lnVal
			})
		});
		let result = await response.text();
		return result;
	}
})(uri("/pname?tsv"));
