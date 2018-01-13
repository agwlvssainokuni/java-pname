/*
 * Copyright 2017,2018 agwlvssainokuni
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

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.google.common.collect.Lists;

@Component
@Lazy(true)
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class TokenizerImpl implements Tokenizer {

	private final Map<String, List<String>> dict;

	private final List<String> lnmlist;

	@Autowired
	public TokenizerImpl(Map<String, List<String>> dict) {
		this.dict = dict;
		this.lnmlist = Lists.newArrayList(dict.keySet());
		this.lnmlist.sort((a, b) -> b.length() - a.length());
	}

	@Override
	public List<Token> tokenize(String text) {

		List<Token> result = Lists.newArrayListWithCapacity(text.length());
		StringBuilder unmatch = new StringBuilder(text.length());
		for (int offset = 0; offset < text.length(); offset++) {

			int lnmlen = lookupLnm(text, offset, lnmlist);
			if (lnmlen < 0) {
				unmatch.append(text.charAt(offset));
				continue;
			}

			if (unmatch.length() > 0) {
				String un = unmatch.toString();
				result.add(new Token(un, Arrays.asList(un), false));
				unmatch = new StringBuilder(text.length());
			}

			String lnm = text.substring(offset, offset + lnmlen);
			result.add(new Token(lnm, dict.get(lnm), true));

			offset += lnmlen - 1;
		}
		if (unmatch.length() > 0) {
			String un = unmatch.toString();
			result.add(new Token(un, Arrays.asList(un), false));
		}
		return result;
	}

	private int lookupLnm(String text, int offset, List<String> lnmlist) {
		LOOP: for (String lnm : lnmlist) {
			if (lnm.length() <= 0) {
				continue;
			}
			for (int i = 0; i < lnm.length(); i++) {
				if (offset + i >= text.length()) {
					continue LOOP;
				}
				if (text.charAt(offset + i) != lnm.charAt(i)) {
					continue LOOP;
				}
			}
			return lnm.length();
		}
		return -1;
	}
}
