/*
 * Copyright 2025 agwlvssainokuni
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

package cherry.pname.main;

import cherry.pname.main.dictionary.CsvDictionaryLoader;
import cherry.pname.main.dictionary.JsonDictionaryLoader;
import cherry.pname.main.dictionary.TsvDictionaryLoader;
import cherry.pname.main.tokenize.GreedyTokenizer;
import cherry.pname.main.tokenize.OptimalTokenizer;
import cherry.pname.main.tokenize.Token;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * PhysicalNameGeneratorのテストクラス
 */
class PhysicalNameGeneratorTest {

    private PhysicalNameGenerator generator;

    @BeforeEach
    void setUp() {
        generator = new PhysicalNameGenerator(
                new CsvDictionaryLoader(),
                new TsvDictionaryLoader(),
                new JsonDictionaryLoader(),
                new GreedyTokenizer(),
                new OptimalTokenizer()
        );
    }

    @Test
    void testLoadCsvDictionaryFromString() throws IOException {
        String csvData = """
                顧客,customer client
                注文,order
                商品,product item
                """;

        generator.loadDictionary(DictionaryFormat.CSV, csvData);

        assertTrue(generator.hasDictionary());
        assertEquals(3, generator.getDictionarySize());

        List<Token> tokens = generator.tokenizeGreedy("顧客注文");
        assertEquals(2, tokens.size());
        assertEquals("顧客", tokens.get(0).word());
        assertEquals("注文", tokens.get(1).word());
    }

    @Test
    void testLoadTsvDictionaryFromString() throws IOException {
        String tsvData = """
                顧客\tcustomer client
                注文\torder
                商品\tproduct item
                """;

        generator.loadDictionary(DictionaryFormat.TSV, tsvData);

        assertTrue(generator.hasDictionary());
        assertEquals(3, generator.getDictionarySize());

        List<Token> tokens = generator.tokenizeOptimal("顧客商品");
        assertEquals(2, tokens.size());
        assertEquals("顧客", tokens.get(0).word());
        assertEquals("商品", tokens.get(1).word());
    }

    @Test
    void testLoadJsonDictionaryFromString() throws IOException {
        String jsonData = """
                {
                  "顧客": ["customer", "client"],
                  "注文": ["order"],
                  "商品": ["product", "item"]
                }
                """;

        generator.loadDictionary(DictionaryFormat.JSON, jsonData);

        assertTrue(generator.hasDictionary());
        assertEquals(3, generator.getDictionarySize());

        List<Token> tokens = generator.tokenizeGreedy("注文商品");
        assertEquals(2, tokens.size());
        assertEquals("注文", tokens.get(0).word());
        assertEquals("商品", tokens.get(1).word());
    }

    @Test
    void testLoadCsvDictionaryFromResource() throws IOException {
        String csvData = """
                管理,management admin
                システム,system
                データ,data
                """;

        Resource resource = new ByteArrayResource(csvData.getBytes(StandardCharsets.UTF_8));
        generator.loadDictionary(DictionaryFormat.CSV, resource);

        assertTrue(generator.hasDictionary());
        assertEquals(3, generator.getDictionarySize());

        List<Token> tokens = generator.tokenizeGreedy("管理システム");
        assertEquals(2, tokens.size());
        assertEquals("管理", tokens.get(0).word());
        assertEquals("システム", tokens.get(1).word());
    }

    @Test
    void testLoadCsvDictionaryFromResourceWithCharset() throws IOException {
        String csvData = """
                情報,information info
                処理,process
                """;

        Resource resource = new ByteArrayResource(csvData.getBytes(StandardCharsets.UTF_8));
        generator.loadDictionary(DictionaryFormat.CSV, resource, StandardCharsets.UTF_8);

        assertTrue(generator.hasDictionary());
        assertEquals(2, generator.getDictionarySize());

        List<Token> tokens = generator.tokenizeOptimal("情報処理");
        assertEquals(2, tokens.size());
        assertEquals("情報", tokens.get(0).word());
        assertEquals("処理", tokens.get(1).word());
    }

    @Test
    void testLoadTsvDictionaryFromResource() throws IOException {
        String tsvData = """
                売上\tsales revenue
                明細\tdetail line
                """;

        Resource resource = new ByteArrayResource(tsvData.getBytes(StandardCharsets.UTF_8));
        generator.loadDictionary(DictionaryFormat.TSV, resource);

        assertTrue(generator.hasDictionary());
        assertEquals(2, generator.getDictionarySize());

        List<Token> tokens = generator.tokenizeGreedy("売上明細");
        assertEquals(2, tokens.size());
        assertEquals("売上", tokens.get(0).word());
        assertEquals("明細", tokens.get(1).word());
    }

    @Test
    void testLoadJsonDictionaryFromResource() throws IOException {
        String jsonData = """
                {
                  "年月日": ["date"],
                  "金額": ["amount", "price"]
                }
                """;

        Resource resource = new ByteArrayResource(jsonData.getBytes(StandardCharsets.UTF_8));
        generator.loadDictionary(DictionaryFormat.JSON, resource);

        assertTrue(generator.hasDictionary());
        assertEquals(2, generator.getDictionarySize());

        List<Token> tokens = generator.tokenizeOptimal("年月日金額");
        assertEquals(2, tokens.size());
        assertEquals("年月日", tokens.get(0).word());
        assertEquals("金額", tokens.get(1).word());
    }

    @Test
    void testTokenizeWithUnknownWords() throws IOException {
        String csvData = """
                顧客,customer
                管理,management
                """;

        generator.loadDictionary(DictionaryFormat.CSV, csvData);

        List<Token> greedyTokens = generator.tokenizeGreedy("顧客XY管理");
        assertEquals(3, greedyTokens.size());
        assertEquals("顧客", greedyTokens.get(0).word());
        assertFalse(greedyTokens.get(0).isUnknown());
        assertEquals("XY", greedyTokens.get(1).word());
        assertTrue(greedyTokens.get(1).isUnknown());
        assertEquals("管理", greedyTokens.get(2).word());
        assertFalse(greedyTokens.get(2).isUnknown());

        List<Token> optimalTokens = generator.tokenizeOptimal("顧客XY管理");
        assertEquals(3, optimalTokens.size());
        assertEquals("顧客", optimalTokens.get(0).word());
        assertEquals("XY", optimalTokens.get(1).word());
        assertEquals("管理", optimalTokens.get(2).word());
    }

    @Test
    void testEmptyDictionary() {
        assertFalse(generator.hasDictionary());
        assertEquals(0, generator.getDictionarySize());

        List<Token> tokens = generator.tokenizeGreedy("テスト");
        assertEquals(1, tokens.size());
        assertEquals("テスト", tokens.get(0).word());
        assertTrue(tokens.get(0).isUnknown());
    }

    @Test
    void testPhysicalNameGeneration() throws IOException {
        String csvData = """
                顧客,customer client
                管理,management admin
                システム,system
                """;

        generator.loadDictionary(DictionaryFormat.CSV, csvData);

        List<Token> tokens = generator.tokenizeGreedy("顧客管理システム");
        assertEquals(3, tokens.size());

        // 物理名の確認
        assertEquals(List.of("customer", "client"), tokens.get(0).physicalNames());
        assertEquals(List.of("management", "admin"), tokens.get(1).physicalNames());
        assertEquals(List.of("system"), tokens.get(2).physicalNames());
    }

    @Test
    void testInvalidDictionary() {
        assertThrows(IOException.class, () -> {
            generator.loadDictionary(DictionaryFormat.JSON, "invalid json");
        });
    }

    @Test
    void testDictionaryReplacement() throws IOException {
        // 最初の辞書を設定
        generator.loadDictionary(DictionaryFormat.CSV, "テスト,test");
        assertEquals(1, generator.getDictionarySize());

        // 辞書を置き換え
        String newCsvData = """
                顧客,customer
                注文,order
                """;
        generator.loadDictionary(DictionaryFormat.CSV, newCsvData);
        assertEquals(2, generator.getDictionarySize());

        // 古い辞書の内容は使えない
        List<Token> tokens = generator.tokenizeGreedy("テスト");
        assertEquals(1, tokens.size());
        assertTrue(tokens.get(0).isUnknown());
    }
}