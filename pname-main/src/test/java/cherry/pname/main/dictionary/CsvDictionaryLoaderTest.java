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
 * CsvDictionaryLoaderのテストクラス
 *
 * <p>CSV形式の辞書ファイル読み込み機能を階層的にテストします：</p>
 * <ul>
 *   <li>基本的なCSVフォーマット処理</li>
 *   <li>物理名候補の単一・複数対応</li>
 *   <li>ホワイトスペースとデータクリーニング</li>
 *   <li>エラーハンドリングと特殊ケース</li>
 * </ul>
 */
class CsvDictionaryLoaderTest {

    private CsvDictionaryLoader loader;

    @BeforeEach
    void setUp() {
        loader = new CsvDictionaryLoader();
    }

    /**
     * 基本的なCSVフォーマット処理のテスト
     * CSV形式の辞書データの正常な読み込みと解析をテストします
     */
    @Nested
    class BasicCsvFormatProcessing {

        /**
         * 基本的なCSV読み込みテスト
         *
         * <p>検証内容:</p>
         * <ul>
         *   <li>Apache Commons CSVによるCSVパーシングが正常動作する</li>
         *   <li>論理名と物理名のマッピングが正確</li>
         *   <li>辞書サイズが正しくカウントされる</li>
         *   <li>各エントリのデータが正確に格納される</li>
         * </ul>
         *
         * <p>期待動作:</p>
         * 「顧客,customer client」のような形式で、5行のCSVデータが
         * 正しく5個の辞書エントリに変換される。
         */
        @Test
        void testBasicCsvLoad() throws IOException {
            String csvData = """
                    顧客,customer client
                    注文,order
                    商品,product item
                    管理,management admin
                    システム,system
                    """;

            Map<String, List<String>> dictionary = loader.load(csvData);

            assertEquals(5, dictionary.size());
            assertEquals(List.of("customer", "client"), dictionary.get("顧客"));
            assertEquals(List.of("order"), dictionary.get("注文"));
            assertEquals(List.of("product", "item"), dictionary.get("商品"));
            assertEquals(List.of("management", "admin"), dictionary.get("管理"));
            assertEquals(List.of("system"), dictionary.get("システム"));
        }

        /**
         * CSV特殊文字処理テスト
         *
         * <p>検証内容:</p>
         * <ul>
         *   <li>CSVフィールド内のカンマを含むデータの処理</li>
         *   <li>ダブルクォートエスケープの正しい解析</li>
         *   <li>特殊文字を含むキーの適切な処理</li>
         * </ul>
         *
         * <p>期待動作:</p>
         * 「"顧客,管理","customer_management"」のような
         * カンマを含むフィールドが正しく処理される。
         */
        @Test
        void testSpecialCharacters() throws IOException {
            String csvData = """
                    "顧客,管理","customer_management"
                    年月日,date
                    """;

            Map<String, List<String>> dictionary = loader.load(csvData);

            assertEquals(2, dictionary.size());
            assertEquals(List.of("customer_management"), dictionary.get("顧客,管理"));
            assertEquals(List.of("date"), dictionary.get("年月日"));
        }
    }

    /**
     * 物理名候補の単一・複数対応のテスト
     * 1つの論理名に対して単一または複数の物理名候補を扱う機能をテストします
     */
    @Nested
    class PhysicalNameCandidateHandling {

        /**
         * 単一物理名処理テスト
         *
         * <p>検証内容:</p>
         * <ul>
         *   <li>1つの論理名に対して1つの物理名の処理</li>
         *   <li>リスト形式での物理名格納</li>
         *   <li>シンプルなマッピングの正確性</li>
         * </ul>
         *
         * <p>期待動作:</p>
         * 「データ,data」→{"データ": ["data"]} として
         * 単一要素のリストとして格納される。
         */
        @Test
        void testSinglePhysicalName() throws IOException {
            String csvData = """
                    データ,data
                    情報,information
                    """;

            Map<String, List<String>> dictionary = loader.load(csvData);

            assertEquals(2, dictionary.size());
            assertEquals(List.of("data"), dictionary.get("データ"));
            assertEquals(List.of("information"), dictionary.get("情報"));
        }

        /**
         * 複数物理名処理テスト
         *
         * <p>検証内容:</p>
         * <ul>
         *   <li>空白区切りで区切られた複数物理名の処理</li>
         *   <li>各物理名候補が正しい順序でリスト化される</li>
         *   <li>3個以上の物理名候補への対応</li>
         * </ul>
         *
         * <p>期待動作:</p>
         * 「顧客管理,customer_management crm client_management」→
         * {"顧客管理": ["customer_management", "crm", "client_management"]}
         * として複数候補がリスト化される。
         */
        @Test
        void testMultiplePhysicalNames() throws IOException {
            String csvData = """
                    顧客管理,customer_management crm client_management
                    売上明細,sales_detail revenue_line order_line
                    """;

            Map<String, List<String>> dictionary = loader.load(csvData);

            assertEquals(2, dictionary.size());
            assertEquals(List.of("customer_management", "crm", "client_management"), dictionary.get("顧客管理"));
            assertEquals(List.of("sales_detail", "revenue_line", "order_line"), dictionary.get("売上明細"));
        }
    }

    /**
     * ホワイトスペースとデータクリーニングのテスト
     * 余分な空白文字の除去やデータ正規化機能をテストします
     */
    @Nested
    class WhitespaceAndDataCleaning {

        /**
         * 空白文字処理テスト
         *
         * <p>検証内容:</p>
         * <ul>
         *   <li>論理名と物理名の前後の空白文字除去</li>
         *   <li>物理名候補間の空白文字正規化</li>
         *   <li>データクリーニング後の正確なマッピング</li>
         * </ul>
         *
         * <p>期待動作:</p>
         * 「  顧客  ,  customer   client  」のような
         * 余分な空白を含むデータが正しく正規化される。
         */
        @Test
        void testWhitespaceHandling() throws IOException {
            String csvData = """
                      顧客  ,  customer   client  
                     注文 , order 
                    """;

            Map<String, List<String>> dictionary = loader.load(csvData);

            assertEquals(2, dictionary.size());
            assertEquals(List.of("customer", "client"), dictionary.get("顧客"));
            assertEquals(List.of("order"), dictionary.get("注文"));
        }
    }

    /**
     * エラーハンドリングと特殊ケースのテスト
     * 不正なデータや境界条件での動作をテストします
     */
    @Nested
    class ErrorHandlingAndSpecialCases {

        /**
         * 空行と不完全データのフィルタリングテスト
         *
         * <p>検証内容:</p>
         * <ul>
         *   <li>空の論理名または物理名を持つ行の除外</li>
         *   <li>不完全なCSVレコードのスキップ</li>
         *   <li>有効なデータのみが辞書に含まれる</li>
         * </ul>
         *
         * <p>期待動作:</p>
         * 空のフィールドや不完全な行が除外され、
         * 有効なデータのみが処理される。
         */
        @Test
        void testEmptyLines() throws IOException {
            String csvData = """
                    顧客,customer
                    ,empty_logical
                    商品,
                    ,
                    注文,order
                    """;

            Map<String, List<String>> dictionary = loader.load(csvData);

            assertEquals(2, dictionary.size());
            assertEquals(List.of("customer"), dictionary.get("顧客"));
            assertEquals(List.of("order"), dictionary.get("注文"));
            assertFalse(dictionary.containsKey(""));
            assertFalse(dictionary.containsKey("商品"));
        }

        /**
         * 不完全なCSVレコード処理テスト
         *
         * <p>検証内容:</p>
         * <ul>
         *   <li>フィールド数が不足している行の処理</li>
         *   <li>不完全なレコードのスキップ</li>
         *   <li>有効なレコードのみの処理継続</li>
         * </ul>
         *
         * <p>期待動作:</p>
         * 「注文」のように物理名がない行はスキップされ、
         * 有効なデータのみが辞書に含まれる。
         */
        @Test
        void testIncompleteCsvRecords() throws IOException {
            String csvData = """
                    顧客,customer
                    注文
                    商品,product item
                    """;

            Map<String, List<String>> dictionary = loader.load(csvData);

            assertEquals(2, dictionary.size());
            assertEquals(List.of("customer"), dictionary.get("顧客"));
            assertEquals(List.of("product", "item"), dictionary.get("商品"));
            assertFalse(dictionary.containsKey("注文"));
        }

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
            String csvData = "";

            Map<String, List<String>> dictionary = loader.load(csvData);

            assertTrue(dictionary.isEmpty());
        }
    }
}
