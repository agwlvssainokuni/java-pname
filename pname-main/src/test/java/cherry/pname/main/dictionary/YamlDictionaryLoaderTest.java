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

package cherry.pname.main.dictionary;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * YamlDictionaryLoaderのテストクラス
 *
 * <p>YAML形式の辞書ファイル読み込み機能を階層的にテストします：</p>
 * <ul>
 *   <li>基本的なYAMLフォーマット処理</li>
 *   <li>物理名候補の単一・複数対応</li>
 *   <li>ホワイトスペースとデータクリーニング</li>
 *   <li>エラーハンドリングとYAML特有機能</li>
 * </ul>
 */
class YamlDictionaryLoaderTest {

    private YamlDictionaryLoader loader;

    @BeforeEach
    void setUp() {
        loader = new YamlDictionaryLoader();
    }

    /**
     * 基本的なYAMLフォーマット処理のテスト
     * YAML形式の辞書データの正常な読み込みと解析をテストします
     */
    @Nested
    class BasicYamlFormatProcessing {

        /**
         * 基本的なYAML読み込みテスト
         *
         * <p>検証内容:</p>
         * <ul>
         *   <li>JacksonライブラリによるYAMLパーシングが正常動作する</li>
         *   <li>YAMLオブジェクトからMapへの変換が正確</li>
         *   <li>日本語Unicode文字の正しい処理</li>
         *   <li>配列形式の物理名候補の適切な解析</li>
         * </ul>
         *
         * <p>期待動作:</p>
         * YAMLオブジェクト形式の辞書データが3個のエントリに
         * 正しくパーシングされ、配列値がリストとして格納される。
         */
        @Test
        void testBasicYamlLoad() throws IOException {
            String yamlContent = """
                    顧客:
                      - customer
                      - client
                    注文:
                      - order
                    商品:
                      - product
                      - item
                    """;

            Map<String, List<String>> result = loader.load(yamlContent);

            assertNotNull(result);
            assertEquals(3, result.size());
            assertEquals(List.of("customer", "client"), result.get("顧客"));
            assertEquals(List.of("order"), result.get("注文"));
            assertEquals(List.of("product", "item"), result.get("商品"));
        }

        /**
         * 単一値YAML処理テスト
         *
         * <p>検証内容:</p>
         * <ul>
         *   <li>YAML単一値（配列でない）の適切な処理</li>
         *   <li>単一値でもリスト形式での格納</li>
         *   <li>YAML構造の柔軟な解析</li>
         * </ul>
         *
         * <p>期待動作:</p>
         * 「顧客: customer」のような単一値形式でも
         * ["customer"] というリスト形式で格納される。
         */
        @Test
        void testSingleValueYamlProcessing() throws IOException {
            String yamlContent = """
                    顧客: customer
                    注文: order
                    """;

            Map<String, List<String>> result = loader.load(yamlContent);

            assertNotNull(result);
            assertEquals(2, result.size());
            assertEquals(List.of("customer"), result.get("顧客"));
            assertEquals(List.of("order"), result.get("注文"));
        }

        /**
         * 混合フォーマットYAML処理テスト
         *
         * <p>検証内容:</p>
         * <ul>
         *   <li>配列形式と単一値形式の混在データの処理</li>
         *   <li>YAML構造の柔軟性への対応</li>
         *   <li>各エントリの適切な正規化</li>
         * </ul>
         *
         * <p>期待動作:</p>
         * 配列形式「顧客: [customer, client]」と
         * 単一値形式「注文: order」が同一辞書内で正しく処理される。
         */
        @Test
        void testMixedFormatYamlProcessing() throws IOException {
            String yamlContent = """
                    顧客:
                      - customer
                      - client
                    注文: order
                    商品:
                      - product
                    """;

            Map<String, List<String>> result = loader.load(yamlContent);

            assertNotNull(result);
            assertEquals(3, result.size());
            assertEquals(List.of("customer", "client"), result.get("顧客"));
            assertEquals(List.of("order"), result.get("注文"));
            assertEquals(List.of("product"), result.get("商品"));
        }

        /**
         * YAMLコメント処理テスト
         *
         * <p>検証内容:</p>
         * <ul>
         *   <li>YAML内のコメント文の適切な無視</li>
         *   <li>インラインコメントの正しい処理</li>
         *   <li>コメント除去後のデータ精度</li>
         * </ul>
         *
         * <p>期待動作:</p>
         * 「# コメント」や「key: value # インライン」のような
         * コメントが適切に無視され、有効なデータのみが処理される。
         */
        @Test
        void testYamlCommentsIgnored() throws IOException {
            String yamlContent = """
                    # This is a comment
                    顧客: # inline comment
                      - customer
                      - client
                    # Another comment
                    注文:
                      - order
                    """;

            Map<String, List<String>> result = loader.load(yamlContent);

            assertNotNull(result);
            assertEquals(2, result.size());
            assertEquals(List.of("customer", "client"), result.get("顧客"));
            assertEquals(List.of("order"), result.get("注文"));
        }
    }

    /**
     * 物理名候補の単一・複数対応のテスト
     * YAML配列形式での単一または複数の物理名候補を扱う機能をテストします
     */
    @Nested
    class PhysicalNameCandidateHandling {

        /**
         * 複数物理名処理テスト
         *
         * <p>検証内容:</p>
         * <ul>
         *   <li>YAML配列内の複数要素の正しい処理</li>
         *   <li>各物理名候補の順序保持</li>
         *   <li>3個以上の物理名候補への対応</li>
         * </ul>
         *
         * <p>期待動作:</p>
         * 「データベース: [database, db, data_base]」として
         * 複数の物理名候補が正確にリスト化される。
         */
        @Test
        void testMultiplePhysicalNames() throws IOException {
            String yamlContent = """
                    システム:
                      - system
                      - sys
                    データベース:
                      - database
                      - db
                      - data_base
                    ユーザー:
                      - user
                    """;

            Map<String, List<String>> result = loader.load(yamlContent);

            assertNotNull(result);
            assertEquals(3, result.size());
            assertEquals(List.of("system", "sys"), result.get("システム"));
            assertEquals(List.of("database", "db", "data_base"), result.get("データベース"));
            assertEquals(List.of("user"), result.get("ユーザー"));
        }
    }

    /**
     * ホワイトスペースとデータクリーニングのテスト
     * YAML内の余分な空白文字の除去やデータ正規化機能をテストします
     */
    @Nested
    class WhitespaceAndDataCleaning {

        /**
         * 空白文字処理テスト
         *
         * <p>検証内容:</p>
         * <ul>
         *   <li>YAMLキーと値の前後の空白文字除去</li>
         *   <li>物理名候補内の空白文字正規化</li>
         *   <li>データクリーニング後の正確なマッピング</li>
         * </ul>
         *
         * <p>期待動作:</p>
         * 「" 顧客 ": [" customer ", " client "]」のような
         * 余分な空白を含むデータが正しく正規化される。
         */
        @Test
        void testWhitespaceHandling() throws IOException {
            String yamlContent = """
                    " 顧客 ":
                      - " customer "
                      - " client "
                    " 注文 ": " order "
                    """;

            Map<String, List<String>> result = loader.load(yamlContent);

            assertNotNull(result);
            assertEquals(2, result.size());
            assertEquals(List.of("customer", "client"), result.get("顧客"));
            assertEquals(List.of("order"), result.get("注文"));
        }

        /**
         * 空値フィルタリングテスト
         *
         * <p>検証内容:</p>
         * <ul>
         *   <li>空文字列キーや値の除外</li>
         *   <li>空配列や空文字列のスキップ</li>
         *   <li>有効な物理名候補のみの抽出</li>
         *   <li>空白文字のみの値の除外</li>
         * </ul>
         *
         * <p>期待動作:</p>
         * 空文字列「""」や空の配列が含まれる場合、
         * 有効な要素のみが辞書に格納される。
         */
        @Test
        void testEmptyValueFiltering() throws IOException {
            String yamlContent = """
                    顧客:
                      - customer
                      - ""
                      - client
                    注文:
                      - ""
                    商品: ""
                    """;

            Map<String, List<String>> result = loader.load(yamlContent);

            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals(List.of("customer", "client"), result.get("顧客"));
            assertFalse(result.containsKey("注文"));
            assertFalse(result.containsKey("商品"));
        }
    }

    /**
     * エラーハンドリングとYAML特有機能のテスト
     * 不正なYAMLデータや境界条件での動作をテストします
     */
    @Nested
    class ErrorHandlingAndYamlSpecificFeatures {

        /**
         * 空ソース処理テスト
         *
         * <p>検証内容:</p>
         * <ul>
         *   <li>空文字列入力時に空の辞書が返される</li>
         *   <li>例外がスローされない</li>
         *   <li>安全なエラーハンドリング</li>
         * </ul>
         *
         * <p>期待動作:</p>
         * 空文字列に対して安全に処理され、空の辞書が返される。
         */
        @Test
        void testEmptySource() throws IOException {
            Map<String, List<String>> result = loader.load("");
            assertNotNull(result);
            assertTrue(result.isEmpty());
        }

        /**
         * null入力処理テスト
         *
         * <p>検証内容:</p>
         * <ul>
         *   <li>null入力時に空の辞書が返される</li>
         *   <li>NullPointerExceptionがスローされない</li>
         *   <li>安全なエラーハンドリング</li>
         * </ul>
         *
         * <p>期待動作:</p>
         * null入力に対して安全に処理され、空の辞書が返される。
         */
        @Test
        void testNullSource() throws IOException {
            Map<String, List<String>> result = loader.load(null);
            assertNotNull(result);
            assertTrue(result.isEmpty());
        }

        /**
         * 空白文字のみの入力処理テスト
         *
         * <p>検証内容:</p>
         * <ul>
         *   <li>空白文字（スペース、タブ、改行）のみの入力処理</li>
         *   <li>有効なYAMLデータとして認識されない場合の処理</li>
         *   <li>空の辞書が返されること</li>
         * </ul>
         *
         * <p>期待動作:</p>
         * 空白文字のみの入力に対して空の辞書が返される。
         */
        @Test
        void testWhitespaceOnlySource() throws IOException {
            Map<String, List<String>> result = loader.load("   \n\t   ");
            assertNotNull(result);
            assertTrue(result.isEmpty());
        }

        /**
         * 不正YAML処理テスト
         *
         * <p>検証内容:</p>
         * <ul>
         *   <li>構文エラーを含むYAMLでIOExceptionがスローされる</li>
         *   <li>YAMLインデンテーションエラーの検出</li>
         *   <li>適切な例外メッセージの確認</li>
         * </ul>
         *
         * <p>期待動作:</p>
         * 不正なインデンテーションやYAML構文エラーで
         * 適切な例外がスローされる。
         */
        @Test
        void testInvalidYaml() {
            String invalidYaml = """
                    顧客:
                      - customer
                     - invalid indentation
                    """;

            IOException exception = assertThrows(IOException.class, () -> loader.load(invalidYaml));
            assertTrue(exception.getMessage().contains("Failed to parse YAML dictionary"));
        }
    }
}
