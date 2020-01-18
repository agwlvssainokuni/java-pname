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

package cherry.pname.caseform;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;

import java.util.List;

import org.assertj.core.util.Lists;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import cherry.pname.Main;
import cherry.pname.tokenizer.Token;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = Main.class)
public class CaseFormImplTest {

	@Autowired
	private CaseForm caseForm;

	@Test
	public void toLowerCamel() {
		assertEquals("abcDefGhi", caseForm.toLowerCamel(createTestData()));
	}

	@Test
	public void toUpperCamel() {
		assertEquals("AbcDefGhi", caseForm.toUpperCamel(createTestData()));
	}

	@Test
	public void toLowerSnake() {
		assertEquals("abc_def_ghi", caseForm.toLowerSnake(createTestData()));
	}

	@Test
	public void toUpperScanke() {
		assertEquals("ABC_DEF_GHI", caseForm.toUpperSnake(createTestData()));
	}

	private List<Token> createTestData() {
		List<Token> list = Lists.newArrayList();
		list.add(new Token("abc", asList("ABC"), true));
		list.add(new Token("defghi", asList("DEF", "ghi"), true));
		return list;
	}

}
