/*
 * Copyright 2017 agwlvssainokuni
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

package cherry.pname.controller;

import java.util.List;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@RequestMapping("/pname")
public interface PnameController {

	@RequestMapping("reg")
	boolean register(@RequestParam("dict") String dict);

	@RequestMapping("gen")
	List<Result> generate(@RequestParam("ln") String ln);

	public static class Result {

		private final String ln;

		private final String pn;

		private final List<String> desc;

		@Override
		public String toString() {
			return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
		}

		public Result(String ln, String pn, List<String> desc) {
			super();
			this.ln = ln;
			this.pn = pn;
			this.desc = desc;
		}

		public String getLn() {
			return ln;
		}

		public String getPn() {
			return pn;
		}

		public List<String> getDesc() {
			return desc;
		}
	}

}
