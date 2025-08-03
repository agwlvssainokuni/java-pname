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
import cherry.pname.main.dictionary.YamlDictionaryLoader;
import cherry.pname.main.romaji.KuromojiRomajiConverter;
import cherry.pname.main.tokenize.GreedyTokenizer;
import cherry.pname.main.tokenize.OptimalTokenizer;
import cherry.pname.main.tokenize.Token;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * PhysicalNameGeneratorのテストクラス
 * 
 * <p>コア機能を階層的にテストします：</p>
 * <ul>
 *   <li>辞書データ読み込み機能（String/Resource）</li>
 *   <li>トークン化処理（既知語/未知語）</li>
 *   <li>物理名生成（各種命名規則）</li>
 *   <li>フォールバック制御機能</li>
 *   <li>エラーハンドリング</li>
 * </ul>
 */
class PhysicalNameGeneratorTest {

    private PhysicalNameGenerator generator;

    @BeforeEach
    void setUp() {
        generator = new PhysicalNameGenerator(
                new CsvDictionaryLoader(),
                new TsvDictionaryLoader(),
                new JsonDictionaryLoader(),
                new YamlDictionaryLoader(),
                new GreedyTokenizer(),
                new OptimalTokenizer(),
                new KuromojiRomajiConverter()
        );
    }

    /**
     * 辞書データ読み込み機能のテスト（String形式）
     * 文字列として渡された辞書データの読み込み処理をテストします
     */
    @Nested
    class DictionaryLoadingFromString {

        /**
         * CSV形式辞書の文字列読み込みテスト
         * 
         * <p>検証内容:</p>
         * <ul>
         *   <li>CSV形式の辞書データが正しく解析される</li>
         *   <li>複数の物理名候補が適切に格納される</li>
         *   <li>辞書読み込み後のトークン化が正常動作する</li>
         *   <li>辞書サイズが正確にカウントされる</li>
         * </ul>
         * 
         * <p>期待動作:</p>
         * 「顧客,customer client」のような形式で、1つの論理名に
         * 複数の物理名候補が空白区切りで指定できる。
         */
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

            List<Token> tokens = generator.tokenize(TokenizerType.GREEDY, "顧客注文");
            assertEquals(2, tokens.size());
            assertEquals("顧客", tokens.get(0).word());
            assertEquals("注文", tokens.get(1).word());
        }

        /**
         * TSV形式辞書の文字列読み込みテスト
         * 
         * <p>検証内容:</p>
         * <ul>
         *   <li>TSV形式（タブ区切り）の辞書データが正しく解析される</li>
         *   <li>CSVと同様に複数の物理名候補に対応する</li>
         *   <li>OPTIMALトークナイザーでの動作確認</li>
         * </ul>
         * 
         * <p>期待動作:</p>
         * タブ文字で論理名と物理名を区切る形式。
         * 物理名部分は空白区切りで複数指定可能。
         */
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

            List<Token> tokens = generator.tokenize(TokenizerType.OPTIMAL, "顧客商品");
            assertEquals(2, tokens.size());
            assertEquals("顧客", tokens.get(0).word());
            assertEquals("商品", tokens.get(1).word());
        }

        /**
         * JSON形式辞書の文字列読み込みテスト
         * 
         * <p>検証内容:</p>
         * <ul>
         *   <li>JSON形式の辞書データが正しく解析される</li>
         *   <li>配列形式での複数物理名候補が処理される</li>
         *   <li>JSONの構造的表現が適切に扱われる</li>
         * </ul>
         * 
         * <p>期待動作:</p>
         * {"論理名": ["物理名1", "物理名2"]} の形式で
         * 構造化された辞書データを扱える。
         */
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

            List<Token> tokens = generator.tokenize(TokenizerType.GREEDY, "注文商品");
            assertEquals(2, tokens.size());
            assertEquals("注文", tokens.get(0).word());
            assertEquals("商品", tokens.get(1).word());
        }

        /**
         * YAML形式辞書の文字列読み込みテスト
         * 
         * <p>検証内容:</p>
         * <ul>
         *   <li>YAML形式の辞書データが正しく解析される</li>
         *   <li>YAML配列と単一値の両方が処理される</li>
         *   <li>人間が読みやすい形式での辞書管理</li>
         * </ul>
         * 
         * <p>期待動作:</p>
         * YAMLのリスト記法と単一値記法の両方をサポート。
         * 可読性の高い辞書ファイル管理が可能。
         */
        @Test
        void testLoadYamlDictionaryFromString() throws IOException {
            String yamlData = """
                    顧客:
                      - customer
                      - client
                    注文: order
                    商品:
                      - product
                      - item
                    """;
            generator.loadDictionary(DictionaryFormat.YAML, yamlData);
            assertTrue(generator.hasDictionary());
            assertEquals(3, generator.getDictionarySize());
            List<Token> tokens = generator.tokenize(TokenizerType.GREEDY, "注文商品");
            assertEquals(2, tokens.size());
            assertEquals("注文", tokens.get(0).word());
            assertEquals("商品", tokens.get(1).word());
        }
    }

    /**
     * 辞書データ読み込み機能のテスト（Resource形式）
     * Resourceインターフェースを通じた辞書データの読み込み処理をテストします
     */
    @Nested
    class DictionaryLoadingFromResource {

        /**
         * CSV辞書のResource読み込みテスト
         * 
         * <p>検証内容:</p>
         * <ul>
         *   <li>ByteArrayResourceからCSVデータが読み込める</li>
         *   <li>ファイルシステム経由での辞書読み込みパターン</li>
         *   <li>Resource抽象化の動作確認</li>
         * </ul>
         */
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

            List<Token> tokens = generator.tokenize(TokenizerType.GREEDY, "管理システム");
            assertEquals(2, tokens.size());
            assertEquals("管理", tokens.get(0).word());
            assertEquals("システム", tokens.get(1).word());
        }

        /**
         * 文字エンコーディング指定でのCSV辞書読み込みテスト
         * 
         * <p>検証内容:</p>
         * <ul>
         *   <li>明示的な文字エンコーディング指定が動作する</li>
         *   <li>UTF-8以外のエンコーディングへの対応確認</li>
         *   <li>国際化対応の辞書読み込み</li>
         * </ul>
         */
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

            List<Token> tokens = generator.tokenize(TokenizerType.OPTIMAL, "情報処理");
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

            List<Token> tokens = generator.tokenize(TokenizerType.GREEDY, "売上明細");
            assertEquals(2, tokens.size());
            assertEquals("売上", tokens.get(0).word());
            assertEquals("明細", tokens.get(1).word());
        }

        @Test
        void testLoadYamlDictionaryFromResource() throws IOException {
            String yamlData = """
                    年月日: date
                    金額:
                      - amount
                      - price
                    """;
            Resource resource = new ByteArrayResource(yamlData.getBytes(StandardCharsets.UTF_8));
            generator.loadDictionary(DictionaryFormat.YAML, resource);
            assertTrue(generator.hasDictionary());
            assertEquals(2, generator.getDictionarySize());
            List<Token> tokens = generator.tokenize(TokenizerType.OPTIMAL, "年月日金額");
            assertEquals(2, tokens.size());
            assertEquals("年月日", tokens.get(0).word());
            assertEquals("金額", tokens.get(1).word());
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

            List<Token> tokens = generator.tokenize(TokenizerType.OPTIMAL, "年月日金額");
            assertEquals(2, tokens.size());
            assertEquals("年月日", tokens.get(0).word());
            assertEquals("金額", tokens.get(1).word());
        }
    }

    /**
     * トークン化処理のテスト
     * 日本語文字列の単語分割処理をテストします
     */
    @Nested
    class Tokenization {

        /**
         * 未知語を含むトークン化テスト
         * 
         * <p>検証内容:</p>
         * <ul>
         *   <li>辞書にある単語と未知語が混在する文字列の処理</li>
         *   <li>既知語のisUnknown()がfalseになる</li>
         *   <li>未知語のisUnknown()がtrueになる</li>
         *   <li>GreedyとOptimalトークナイザーの両方での動作</li>
         * </ul>
         * 
         * <p>期待動作:</p>
         * 「顧客XY管理」→["顧客"(既知), "XY"(未知), "管理"(既知)]
         * として適切に分割される。
         */
        @Test
        void testTokenizeWithUnknownWords() throws IOException {
            String csvData = """
                    顧客,customer
                    管理,management
                    """;

            generator.loadDictionary(DictionaryFormat.CSV, csvData);

            List<Token> greedyTokens = generator.tokenize(TokenizerType.GREEDY, "顧客XY管理");
            assertEquals(3, greedyTokens.size());
            assertEquals("顧客", greedyTokens.get(0).word());
            assertFalse(greedyTokens.get(0).isUnknown());
            assertEquals("XY", greedyTokens.get(1).word());
            assertTrue(greedyTokens.get(1).isUnknown());
            assertEquals("管理", greedyTokens.get(2).word());
            assertFalse(greedyTokens.get(2).isUnknown());

            List<Token> optimalTokens = generator.tokenize(TokenizerType.OPTIMAL, "顧客XY管理");
            assertEquals(3, optimalTokens.size());
            assertEquals("顧客", optimalTokens.get(0).word());
            assertEquals("XY", optimalTokens.get(1).word());
            assertEquals("管理", optimalTokens.get(2).word());
        }

        /**
         * 空辞書でのトークン化テスト
         * 
         * <p>検証内容:</p>
         * <ul>
         *   <li>辞書が読み込まれていない状態での動作</li>
         *   <li>hasDictionary()がfalseを返す</li>
         *   <li>辞書サイズが0になる</li>
         *   <li>すべての単語が未知語として扱われる</li>
         * </ul>
         */
        @Test
        void testEmptyDictionary() {
            assertFalse(generator.hasDictionary());
            assertEquals(0, generator.getDictionarySize());

            List<Token> tokens = generator.tokenize(TokenizerType.GREEDY, "テスト");
            assertEquals(1, tokens.size());
            assertEquals("テスト", tokens.get(0).word());
            assertTrue(tokens.get(0).isUnknown());
        }

        /**
         * 物理名候補の確認テスト
         * 
         * <p>検証内容:</p>
         * <ul>
         *   <li>トークン化後の各単語に物理名候補が正しく設定される</li>
         *   <li>複数の物理名候補が適切にリスト化される</li>
         *   <li>単一候補と複数候補の両方が処理される</li>
         * </ul>
         */
        @Test
        void testPhysicalNameGeneration() throws IOException {
            String csvData = """
                    顧客,customer client
                    管理,management admin
                    システム,system
                    """;

            generator.loadDictionary(DictionaryFormat.CSV, csvData);

            List<Token> tokens = generator.tokenize(TokenizerType.GREEDY, "顧客管理システム");
            assertEquals(3, tokens.size());

            assertEquals(List.of("customer", "client"), tokens.get(0).physicalNames());
            assertEquals(List.of("management", "admin"), tokens.get(1).physicalNames());
            assertEquals(List.of("system"), tokens.get(2).physicalNames());
        }
    }

    /**
     * 物理名生成機能のテスト
     * 各種命名規則による物理名生成処理をテストします
     */
    @Nested
    class PhysicalNameGeneration {

        /**
         * lowerCamel形式での物理名生成テスト
         * 
         * <p>検証内容:</p>
         * <ul>
         *   <li>最初の単語は小文字、以降は大文字始まりの結合</li>
         *   <li>論理名と物理名の対応関係</li>
         *   <li>トークンマッピング情報の正確性</li>
         *   <li>フォールバック有効時の動作</li>
         * </ul>
         * 
         * <p>期待動作:</p>
         * 「顧客管理システム」→「customerManagementSystem」
         */
        @Test
        void testGeneratePhysicalNameCamelCase() throws IOException {
            String csvData = """
                    顧客,customer
                    管理,management
                    システム,system
                    """;
            generator.loadDictionary(DictionaryFormat.CSV, csvData);

            PhysicalNameResult result = generator.generatePhysicalName(
                    TokenizerType.GREEDY, NamingConvention.LOWER_CAMEL, "顧客管理システム", true);

            assertEquals("顧客管理システム", result.logicalName());
            assertEquals("customerManagementSystem", result.physicalName());
            assertEquals(3, result.tokenMappings().size());
            assertEquals("顧客=>customer", result.tokenMappings().get(0));
            assertEquals("管理=>management", result.tokenMappings().get(1));
            assertEquals("システム=>system", result.tokenMappings().get(2));
        }

        /**
         * PascalCase形式での物理名生成テスト
         * 
         * <p>検証内容:</p>
         * <ul>
         *   <li>すべての単語が大文字始まりで結合される</li>
         *   <li>UPPER_CAMELとPASCALが同等であることの確認</li>
         * </ul>
         * 
         * <p>期待動作:</p>
         * 「顧客管理」→「CustomerManagement」
         */
        @Test
        void testGeneratePhysicalNamePascalCase() throws IOException {
            String csvData = """
                    顧客,customer
                    管理,management
                    """;
            generator.loadDictionary(DictionaryFormat.CSV, csvData);

            PhysicalNameResult result = generator.generatePhysicalName(
                    TokenizerType.GREEDY, NamingConvention.UPPER_CAMEL, "顧客管理", true);

            assertEquals("顧客管理", result.logicalName());
            assertEquals("CustomerManagement", result.physicalName());
            assertEquals(2, result.tokenMappings().size());
        }

        @Test
        void testGeneratePhysicalNameSnakeCase() throws IOException {
            String csvData = """
                    顧客,customer
                    管理,management
                    """;
            generator.loadDictionary(DictionaryFormat.CSV, csvData);

            PhysicalNameResult result = generator.generatePhysicalName(
                    TokenizerType.GREEDY, NamingConvention.SNAKE, "顧客管理", true);

            assertEquals("customer_management", result.physicalName());
        }

        @Test
        void testGeneratePhysicalNameLowerSnakeCase() throws IOException {
            String csvData = """
                    顧客,customer
                    管理,management
                    """;
            generator.loadDictionary(DictionaryFormat.CSV, csvData);

            PhysicalNameResult result = generator.generatePhysicalName(
                    TokenizerType.GREEDY, NamingConvention.LOWER_SNAKE, "顧客管理", true);

            assertEquals("customer_management", result.physicalName());
        }

        @Test
        void testGeneratePhysicalNameSnakeCaseUpper() throws IOException {
            String csvData = """
                    顧客,customer
                    管理,management
                    """;
            generator.loadDictionary(DictionaryFormat.CSV, csvData);

            PhysicalNameResult result = generator.generatePhysicalName(
                    TokenizerType.GREEDY, NamingConvention.UPPER_SNAKE, "顧客管理", true);

            assertEquals("CUSTOMER_MANAGEMENT", result.physicalName());
        }

        @Test
        void testGeneratePhysicalNameKebabCase() throws IOException {
            String csvData = """
                    顧客,customer
                    管理,management
                    """;
            generator.loadDictionary(DictionaryFormat.CSV, csvData);

            PhysicalNameResult result = generator.generatePhysicalName(
                    TokenizerType.GREEDY, NamingConvention.KEBAB, "顧客管理", true);

            assertEquals("customer-management", result.physicalName());
        }

        @Test
        void testGeneratePhysicalNameLowerKebabCase() throws IOException {
            String csvData = """
                    顧客,customer
                    管理,management
                    """;
            generator.loadDictionary(DictionaryFormat.CSV, csvData);

            PhysicalNameResult result = generator.generatePhysicalName(
                    TokenizerType.GREEDY, NamingConvention.LOWER_KEBAB, "顧客管理", true);

            assertEquals("customer-management", result.physicalName());
        }

        @Test
        void testGeneratePhysicalNameKebabCaseUpper() throws IOException {
            String csvData = """
                    顧客,customer
                    管理,management
                    """;
            generator.loadDictionary(DictionaryFormat.CSV, csvData);

            PhysicalNameResult result = generator.generatePhysicalName(
                    TokenizerType.GREEDY, NamingConvention.UPPER_KEBAB, "顧客管理", true);

            assertEquals("CUSTOMER-MANAGEMENT", result.physicalName());
        }

        /**
         * 複数物理名候補を持つ単語での生成テスト
         * 
         * <p>検証内容:</p>
         * <ul>
         *   <li>1つの論理名に複数の物理名候補がある場合の処理</li>
         *   <li>すべての候補が物理名に含まれること</li>
         *   <li>トークンマッピングでの複数候補表示</li>
         * </ul>
         * 
         * <p>期待動作:</p>
         * 「顧客,customer client」→「customerClientManagementAdmin」
         * のように全候補が結合される。
         */
        @Test
        void testGeneratePhysicalNameWithMultipleElements() throws IOException {
            String csvData = """
                    顧客,customer client
                    管理,management admin
                    """;
            generator.loadDictionary(DictionaryFormat.CSV, csvData);

            PhysicalNameResult result = generator.generatePhysicalName(
                    TokenizerType.GREEDY, NamingConvention.LOWER_CAMEL, "顧客管理", true);

            assertEquals("customerClientManagementAdmin", result.physicalName());
            assertEquals("顧客=>customer, client", result.tokenMappings().get(0));
            assertEquals("管理=>management, admin", result.tokenMappings().get(1));
        }
    }

    /**
     * フォールバック制御機能のテスト
     * 未知語に対するローマ字変換の有効/無効制御をテストします
     */
    @Nested
    class FallbackControl {

        /**
         * フォールバック有効時の物理名生成テスト
         * 
         * <p>検証内容:</p>
         * <ul>
         *   <li>enableFallback=trueでの未知語ローマ字変換</li>
         *   <li>既知語と未知語が混在する場合の処理</li>
         *   <li>ローマ字変換結果の物理名への組み込み</li>
         *   <li>トークンマッピングでのローマ字変換詳細表示</li>
         * </ul>
         * 
         * <p>期待動作:</p>
         * 「顧客XY管理」→「customerXyKanri」として
         * 未知語部分「XY管理」がローマ字変換される。
         */
        @Test
        void testGeneratePhysicalNameWithUnknownWords() throws IOException {
            String csvData = """
                    顧客,customer
                    """;
            generator.loadDictionary(DictionaryFormat.CSV, csvData);

            PhysicalNameResult result = generator.generatePhysicalName(
                    TokenizerType.GREEDY, NamingConvention.LOWER_CAMEL, "顧客XY管理", true);

            assertEquals("customerXyKanri", result.physicalName());
            assertEquals(2, result.tokenMappings().size());
            assertEquals("顧客=>customer", result.tokenMappings().get(0));
            assertEquals("XY管理=>(romaji: XY kanri)", result.tokenMappings().get(1));
        }

        /**
         * フォールバック無効時の物理名生成テスト
         * 
         * <p>検証内容:</p>
         * <ul>
         *   <li>enableFallback=falseでの未知語処理</li>
         *   <li>未知語が元の日本語のまま保持される</li>
         *   <li>既知語は正常に変換される</li>
         *   <li>より安全な動作モードの確認</li>
         * </ul>
         * 
         * <p>期待動作:</p>
         * 「顧客XY管理」→「customerXy管理」として
         * 未知語「XY管理」は日本語のまま残る。
         */
        @Test
        void testGeneratePhysicalNameWithoutFallback() throws IOException {
            String csvData = """
                    顧客,customer
                    """;
            generator.loadDictionary(DictionaryFormat.CSV, csvData);

            PhysicalNameResult result = generator.generatePhysicalName(
                    TokenizerType.GREEDY, NamingConvention.LOWER_CAMEL, "顧客XY管理", false);

            assertEquals("customerXy管理", result.physicalName());
            assertEquals(2, result.tokenMappings().size());
            assertEquals("顧客=>customer", result.tokenMappings().get(0));
            assertEquals("XY管理=>(unknown: XY管理)", result.tokenMappings().get(1));
        }
    }

    /**
     * エラーハンドリングと特殊ケースのテスト
     * 異常系や境界値での動作をテストします
     */
    @Nested
    class ErrorHandlingAndEdgeCases {

        /**
         * 無効な辞書データでの例外発生テスト
         * 
         * <p>検証内容:</p>
         * <ul>
         *   <li>不正なJSON形式での適切な例外発生</li>
         *   <li>IOException継承の例外型</li>
         *   <li>例外安全性の確認</li>
         * </ul>
         */
        @Test
        void testInvalidDictionary() {
            assertThrows(IOException.class, () -> {
                generator.loadDictionary(DictionaryFormat.JSON, "invalid json");
            });
        }

        /**
         * 辞書置換機能のテスト
         * 
         * <p>検証内容:</p>
         * <ul>
         *   <li>新しい辞書で既存辞書が完全に置き換わる</li>
         *   <li>古い辞書の内容が使用できなくなる</li>
         *   <li>辞書サイズが正しく更新される</li>
         *   <li>メモリリークが発生しない</li>
         * </ul>
         * 
         * <p>期待動作:</p>
         * 辞書を再読み込みすると、前の辞書内容は完全に破棄され
         * 新しい辞書のみが有効になる。
         */
        @Test
        void testDictionaryReplacement() throws IOException {
            generator.loadDictionary(DictionaryFormat.CSV, "テスト,test");
            assertEquals(1, generator.getDictionarySize());

            String newCsvData = """
                    顧客,customer
                    注文,order
                    """;
            generator.loadDictionary(DictionaryFormat.CSV, newCsvData);
            assertEquals(2, generator.getDictionarySize());

            List<Token> tokens = generator.tokenize(TokenizerType.GREEDY, "テスト");
            assertEquals(1, tokens.size());
            assertTrue(tokens.get(0).isUnknown());
        }
    }
}
