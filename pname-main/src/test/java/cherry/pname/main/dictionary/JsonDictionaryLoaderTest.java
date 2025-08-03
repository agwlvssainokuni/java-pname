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
 * JsonDictionaryLoaderのテストクラス
 *
 * <p>JSON形式の辞書ファイル読み込み機能を階層的にテストします：</p>
 * <ul>
 *   <li>基本的なJSONフォーマット処理</li>
 *   <li>物理名候補の単一・複数対応</li>
 *   <li>ホワイトスペースとデータクリーニング</li>
 *   <li>エラーハンドリングとJSON特有機能</li>
 * </ul>
 */
class JsonDictionaryLoaderTest {

    private JsonDictionaryLoader loader;

    @BeforeEach
    void setUp() {
        loader = new JsonDictionaryLoader();
    }

    /**
     * 基本的なJSONフォーマット処理のテスト
     * JSON形式の辞書データの正常な読み込みと解析をテストします
     */
    @Nested
    class BasicJsonFormatProcessing {

        /**
         * 基本的なJSON読み込みテスト
         *
         * <p>検証内容:</p>
         * <ul>
         *   <li>JacksonライブラリによるJSONパーシングが正常動作する</li>
         *   <li>JSONオブジェクトからMapへの変換が正確</li>
         *   <li>日本語Unicode文字の正しい処理</li>
         *   <li>配列形式の物理名候補の適切な解析</li>
         * </ul>
         *
         * <p>期待動作:</p>
         * JSONオブジェクト形式の辞書データが5個のエントリに
         * 正しくパーシングされ、配列値がリストとして格納される。
         */
        @Test
        void testBasicJsonLoad() throws IOException {
            String jsonData = """
                    {
                      "顧客": ["customer", "client"],
                      "注文": ["order"],
                      "商品": ["product", "item"],
                      "管理": ["management", "admin"],
                      "システム": ["system"]
                    }
                    """;

            Map<String, List<String>> dictionary = loader.load(jsonData);

            assertEquals(5, dictionary.size());
            assertEquals(List.of("customer", "client"), dictionary.get("顧客"));
            assertEquals(List.of("order"), dictionary.get("注文"));
            assertEquals(List.of("product", "item"), dictionary.get("商品"));
            assertEquals(List.of("management", "admin"), dictionary.get("管理"));
            assertEquals(List.of("system"), dictionary.get("システム"));
        }

        /**
         * コンパクトJSON形式処理テスト
         *
         * <p>検証内容:</p>
         * <ul>
         *   <li>空白文字や改行なしのコンパクトJSONの処理</li>
         *   <li>フォーマットに関係なく正しいパーシング</li>
         *   <li>異なるフォーマットでのデータ一貫性</li>
         * </ul>
         */
        @Test
        void testCompactJson() throws IOException {
            String jsonData = """
                    {"顧客":["customer","client"],"注文":["order"],"商品":["product","item"]}
                    """;

            Map<String, List<String>> dictionary = loader.load(jsonData);

            assertEquals(3, dictionary.size());
            assertEquals(List.of("customer", "client"), dictionary.get("顧客"));
            assertEquals(List.of("order"), dictionary.get("注文"));
            assertEquals(List.of("product", "item"), dictionary.get("商品"));
        }

        /**
         * Unicode文字処理テスト
         *
         * <p>検証内容:</p>
         * <ul>
         *   <li>日本語漢字やひらがなのUnicode文字処理</li>
         *   <li>JSON内でのUnicodeエスケープへの対応</li>
         *   <li>国際化対応の確認</li>
         * </ul>
         */
        @Test
        void testUnicodeCharacters() throws IOException {
            String jsonData = """
                    {
                      "年月日": ["date"],
                      "金額": ["amount", "price"],
                      "数量": ["quantity", "qty"]
                    }
                    """;

            Map<String, List<String>> dictionary = loader.load(jsonData);

            assertEquals(3, dictionary.size());
            assertEquals(List.of("date"), dictionary.get("年月日"));
            assertEquals(List.of("amount", "price"), dictionary.get("金額"));
            assertEquals(List.of("quantity", "qty"), dictionary.get("数量"));
        }
    }

    /**
     * 物理名候補の単一・複数対応のテスト
     * JSON配列形式での単一または複数の物理名候補を扱う機能をテストします
     */
    @Nested
    class PhysicalNameCandidateHandling {

        /**
         * 単一物理名処理テスト
         *
         * <p>検証内容:</p>
         * <ul>
         *   <li>JSON配列形式での単一要素処理</li>
         *   <li>単一値でもリスト形式での格納</li>
         *   <li>JSON構造の一貫性維持</li>
         * </ul>
         */
        @Test
        void testSinglePhysicalName() throws IOException {
            String jsonData = """
                    {
                      "データ": ["data"],
                      "情報": ["information"]
                    }
                    """;

            Map<String, List<String>> dictionary = loader.load(jsonData);

            assertEquals(2, dictionary.size());
            assertEquals(List.of("data"), dictionary.get("データ"));
            assertEquals(List.of("information"), dictionary.get("情報"));
        }

        /**
         * 複数物理名処理テスト
         *
         * <p>検証内容:</p>
         * <ul>
         *   <li>JSON配列内の複数要素の正しい処理</li>
         *   <li>各物理名候補の順序保持</li>
         *   <li>3個以上の物理名候補への対応</li>
         * </ul>
         */
        @Test
        void testMultiplePhysicalNames() throws IOException {
            String jsonData = """
                    {
                      "顧客管理": ["customer_management", "crm", "client_management"],
                      "売上明細": ["sales_detail", "revenue_line", "order_line"]
                    }
                    """;

            Map<String, List<String>> dictionary = loader.load(jsonData);

            assertEquals(2, dictionary.size());
            assertEquals(List.of("customer_management", "crm", "client_management"), dictionary.get("顧客管理"));
            assertEquals(List.of("sales_detail", "revenue_line", "order_line"), dictionary.get("売上明細"));
        }

    }

    /**
     * ホワイトスペースとデータクリーニングのテスト
     * JSON内の余分な空白文字の除去やデータ正規化機能をテストします
     */
    @Nested
    class WhitespaceAndDataCleaning {

        /**
         * 空白文字処理テスト
         *
         * <p>検証内容:</p>
         * <ul>
         *   <li>JSONキーと値の前後の空白文字除去</li>
         *   <li>物理名候補内の空白文字正規化</li>
         *   <li>データクリーニング後の正確なマッピング</li>
         * </ul>
         */
        @Test
        void testWhitespaceHandling() throws IOException {
            String jsonData = """
                    {
                      "  顧客  ": ["  customer  ", "  client  "],
                      " 注文 ": [" order "]
                    }
                    """;

            Map<String, List<String>> dictionary = loader.load(jsonData);

            assertEquals(2, dictionary.size());
            assertEquals(List.of("customer", "client"), dictionary.get("顧客"));
            assertEquals(List.of("order"), dictionary.get("注文"));
        }

        /**
         * 空値とnull値のフィルタリングテスト
         *
         * <p>検証内容:</p>
         * <ul>
         *   <li>空文字列キーや値の除外</li>
         *   <li>空配列やnull値のスキップ</li>
         *   <li>有効な物理名候補のみの抽出</li>
         *   <li>空白文字のみの値の除外</li>
         * </ul>
         */
        @Test
        void testEmptyAndNullValues() throws IOException {
            String jsonData = """
                    {
                      "顧客": ["customer"],
                      "": ["empty_key"],
                      "商品": [],
                      "注文": ["order"],
                      "管理": ["", "management", " ", "admin"],
                      "システム": null
                    }
                    """;

            Map<String, List<String>> dictionary = loader.load(jsonData);

            assertEquals(3, dictionary.size());
            assertEquals(List.of("customer"), dictionary.get("顧客"));
            assertEquals(List.of("order"), dictionary.get("注文"));
            assertEquals(List.of("management", "admin"), dictionary.get("管理"));
            assertFalse(dictionary.containsKey(""));
            assertFalse(dictionary.containsKey("商品"));
            assertFalse(dictionary.containsKey("システム"));
        }

    }

    /**
     * エラーハンドリングとJSON特有機能のテスト
     * 不正なJSONデータや境界条件での動作をテストします
     */
    @Nested
    class ErrorHandlingAndJsonSpecificFeatures {

        /**
         * 空ソース処理テスト
         *
         * <p>検証内容:</p>
         * <ul>
         *   <li>空文字列入力時に空の辞書が返される</li>
         *   <li>例外がスローされない</li>
         *   <li>安全なエラーハンドリング</li>
         * </ul>
         */
        @Test
        void testEmptySource() throws IOException {
            String jsonData = "";

            Map<String, List<String>> dictionary = loader.load(jsonData);

            assertTrue(dictionary.isEmpty());
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
         */
        @Test
        void testNullSource() throws IOException {
            Map<String, List<String>> dictionary = loader.load(null);

            assertTrue(dictionary.isEmpty());
        }

        /**
         * 空JSONオブジェクト処理テスト
         *
         * <p>検証内容:</p>
         * <ul>
         *   <li>空JSONオブジェクト"{}"の正しい処理</li>
         *   <li>有効なJSONであることの確認</li>
         *   <li>空の辞書が返されること</li>
         * </ul>
         */
        @Test
        void testEmptyJsonObject() throws IOException {
            String jsonData = "{}";

            Map<String, List<String>> dictionary = loader.load(jsonData);

            assertTrue(dictionary.isEmpty());
        }

        /**
         * 不正JSON処理テスト
         *
         * <p>検証内容:</p>
         * <ul>
         *   <li>構文エラーを含むJSONでIOExceptionがスローされる</li>
         *   <li>Jacksonライブラリのエラー処理の確認</li>
         *   <li>適切な例外メッセージの確認</li>
         * </ul>
         */
        @Test
        void testInvalidJson() {
            String jsonData = """
                    {
                      "顧客": ["customer",
                      "注文": "order"
                    """;

            assertThrows(IOException.class, () -> loader.load(jsonData));
        }
    }
}
