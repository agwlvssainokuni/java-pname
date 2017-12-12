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

package cherry.pname.processor;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.util.List;
import java.util.function.Function;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import cherry.pname.tokenizer.Token;
import cherry.pname.tokenizer.Tokenizer;

public interface Processor {

	List<Result> process(Tokenizer tokenizer, Function<List<Token>, String> pnFunc, Reader r, boolean tsv);

	default List<Result> process(Tokenizer tokenizer, Function<List<Token>, String> pnFunc, String s, boolean tsv) {
		try (StringReader r = new StringReader(s)) {
			return process(tokenizer, pnFunc, r, tsv);
		}
	}

	default List<Result> process(Tokenizer tokenizer, Function<List<Token>, String> pnFunc, InputStream in,
			Charset charset, boolean tsv) {
		try (Reader r = new InputStreamReader(in, charset)) {
			return process(tokenizer, pnFunc, r, tsv);
		} catch (IOException ex) {
			throw new IllegalStateException(ex);
		}
	}

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
