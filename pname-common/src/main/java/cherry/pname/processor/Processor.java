/*
 * Copyright 2017,2020 agwlvssainokuni
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

package cherry.pname.processor;

import java.util.List;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public interface Processor {

	Result process(String pname);

	public static class Result {

		private final String lname;

		private final String pname;

		private final List<String> desc;

		@Override
		public String toString() {
			return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
		}

		public Result(String lname, String pname, List<String> desc) {
			super();
			this.lname = lname;
			this.pname = pname;
			this.desc = desc;
		}

		public String getLname() {
			return lname;
		}

		public String getPname() {
			return pname;
		}

		public List<String> getDesc() {
			return desc;
		}
	}

}
