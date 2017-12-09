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

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import com.google.common.collect.Lists;

@Component
@Lazy(true)
public class TokenizerImpl implements Tokenizer {

	private final Map<String, List<String>> dict;

	private final List<String> wordList;

	@Autowired
	public TokenizerImpl(Map<String, List<String>> dict) {
		this.dict = dict;
		this.wordList = Lists.newArrayList(dict.keySet());
		this.wordList.sort((a, b) -> b.length() - a.length());
	}

	@Override
	public List<Token> tokenize(String text) {

		List<Token> result = Lists.newArrayListWithCapacity(text.length());
		StringBuilder unmatch = new StringBuilder(text.length());
		for (int offset = 0; offset < text.length(); offset++) {

			int wordlen = lookupWord(text, offset, wordList);
			if (wordlen < 0) {
				unmatch.append(text.charAt(offset));
				continue;
			}

			if (unmatch.length() > 0) {
				String un = unmatch.toString();
				result.add(new Token(un, Arrays.asList(un), false));
				unmatch = new StringBuilder(text.length());
			}

			String word = text.substring(offset, offset + wordlen);
			result.add(new Token(word, dict.get(word), true));

			offset += wordlen - 1;
		}
		if (unmatch.length() > 0) {
			String un = unmatch.toString();
			result.add(new Token(un, Arrays.asList(un), false));
		}
		return result;
	}

	private int lookupWord(String text, int offset, List<String> wordList) {
		LOOP: for (String word : wordList) {
			if (word.length() <= 0) {
				continue;
			}
			for (int i = 0; i < word.length(); i++) {
				if (offset + i >= text.length()) {
					continue LOOP;
				}
				if (text.charAt(offset + i) != word.charAt(i)) {
					continue LOOP;
				}
			}
			return word.length();
		}
		return -1;
	}
}
