/*
 * Copyright 2017,2021 agwlvssainokuni
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

package cherry.pname.dict;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.Resource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import cherry.pname.Main;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@ContextConfiguration(classes = Main.class)
public class DictLoaderImplTest {

	@Autowired
	private DictLoader loader;

	@Value("classpath:cherry/pname/dict/dict.csv")
	private Resource dictCsv;

	@Value("classpath:cherry/pname/dict/dict_hdr.csv")
	private Resource dictHdrCsv;

	@Value("classpath:cherry/pname/dict/dict_uc.csv")
	private Resource dictUcCsv;

	@Value("classpath:cherry/pname/dict/dict.tsv")
	private Resource dictTsv;

	@Test
	public void 初期化() {
		assertNotNull(loader);
	}

	@Test
	public void 辞書CSV() throws IOException {
		assertDict(loader.load(dictCsv.getInputStream(), UTF_8, false, " ", false));
	}

	@Test
	public void 辞書CSV_ヘッダ() throws IOException {
		assertDict(loader.load(dictHdrCsv.getInputStream(), UTF_8, true, " ", false));
	}

	@Test
	public void 辞書CSV_アンスコ() throws IOException {
		assertDict(loader.load(dictUcCsv.getInputStream(), UTF_8, false, "_", false));
	}

	@Test
	public void 辞書TSV() throws IOException {
		assertDict(loader.load(dictTsv.getInputStream(), UTF_8, false, " ", true));
	}

	private void assertDict(Map<String, List<String>> dict) {
		assertEquals(3, dict.size());
		assertTrue(dict.containsKey("a"));
		assertEquals(asList("A"), dict.get("a"));
		assertTrue(dict.containsKey("aa"));
		assertEquals(asList("A", "A"), dict.get("aa"));
		assertTrue(dict.containsKey("b"));
		assertEquals(asList("B"), dict.get("b"));
	}

}
