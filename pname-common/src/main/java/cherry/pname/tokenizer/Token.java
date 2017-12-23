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

package cherry.pname.tokenizer;

import java.util.List;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class Token {

	private final String word;

	private final List<String> name;

	private final boolean ok;

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
	}

	public Token(String word, List<String> name, boolean ok) {
		super();
		this.word = word;
		this.name = name;
		this.ok = ok;
	}

	public String getWord() {
		return word;
	}

	public List<String> getName() {
		return name;
	}

	public boolean isOk() {
		return ok;
	}

}
