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

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import cherry.pname.Main;

import com.google.common.collect.Maps;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = Main.class)
public class TokenizerImplTest {

	@Autowired
	private TokenizerBuilder builder;

	@Test
	public void 初期化() {
		assertNotNull(builder);
		assertNotNull(builder.build(createDict()));
	}

	@Test
	public void tokenize_空文字列() {
		Tokenizer tokenizer = builder.build(createDict());
		List<Token> result = tokenizer.tokenize("");
		assertEquals(0, result.size());
	}

	@Test
	public void tokenize_一文字() {
		Tokenizer tokenizer = builder.build(createDict());
		List<Token> result = tokenizer.tokenize("a");
		assertEquals(1, result.size());
		Token token = result.get(0);
		assertEquals("a", token.getWord());
		assertEquals(asList("A"), token.getName());
		assertTrue(token.isOk());
	}

	@Test
	public void tokenize_二文字() {
		Tokenizer tokenizer = builder.build(createDict());
		List<Token> result = tokenizer.tokenize("aa");
		assertEquals(1, result.size());
		Token token = result.get(0);
		assertEquals("aa", token.getWord());
		assertEquals(asList("A", "A"), token.getName());
		assertTrue(token.isOk());
	}

	@Test
	public void tokenize_アンマッチ() {
		Tokenizer tokenizer = builder.build(createDict());
		List<Token> result = tokenizer.tokenize("cde");
		assertEquals(1, result.size());
		Token token = result.get(0);
		assertEquals("cde", token.getWord());
		assertEquals(asList("cde"), token.getName());
		assertFalse(token.isOk());
	}

	@Test
	public void tokenize_混在() {
		Tokenizer tokenizer = builder.build(createDict());
		List<Token> result = tokenizer.tokenize("abcdeaabb");
		assertEquals(6, result.size());

		Token token0 = result.get(0);
		assertEquals("a", token0.getWord());
		assertEquals(asList("A"), token0.getName());
		assertTrue(token0.isOk());

		Token token1 = result.get(1);
		assertEquals("b", token1.getWord());
		assertEquals(asList("B"), token1.getName());
		assertTrue(token0.isOk());

		Token token2 = result.get(2);
		assertEquals("cde", token2.getWord());
		assertEquals(asList("cde"), token2.getName());
		assertFalse(token2.isOk());

		Token token3 = result.get(3);
		assertEquals("aa", token3.getWord());
		assertEquals(asList("A", "A"), token3.getName());
		assertTrue(token3.isOk());

		Token token4 = result.get(4);
		assertEquals("b", token4.getWord());
		assertEquals(asList("B"), token4.getName());
		assertTrue(token4.isOk());

		Token token5 = result.get(5);
		assertEquals("b", token5.getWord());
		assertEquals(asList("B"), token5.getName());
		assertTrue(token5.isOk());
	}

	private Map<String, List<String>> createDict() {
		Map<String, List<String>> dict = Maps.newHashMap();
		dict.put("a", asList("A"));
		dict.put("aa", asList("A", "A"));
		dict.put("b", asList("B"));
		return dict;
	}

}