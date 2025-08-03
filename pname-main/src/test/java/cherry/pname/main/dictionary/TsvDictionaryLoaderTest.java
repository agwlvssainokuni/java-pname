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
 * TsvDictionaryLoaderのテストクラス
 *
 * <p>TSV(タブ区切り)形式の辞書ファイル読み込み機能を階層的にテストします：</p>
 * <ul>
 *   <li>基本的なTSVフォーマット処理</li>
 *   <li>物理名候補の単一・複数対応</li>
 *   <li>タブ文字とホワイトスペース処理</li>
 *   <li>エラーハンドリングと特殊ケース</li>
 * </ul>
 */
class TsvDictionaryLoaderTest {

    private TsvDictionaryLoader loader;

    @BeforeEach
    void setUp() {
        loader = new TsvDictionaryLoader();
    }

    /**
     * 基本的なTSVフォーマット処理のテスト
     * TSV(タブ区切り)形式の辞書データの正常な読み込みと解析をテストします
     */
    @Nested
    class BasicTsvFormatProcessing {

        /**
         * 基本的なTSV読み込みテスト
         *
         * <p>検証内容:</p>
         * <ul>
         *   <li>タブ文字(\t)でのフィールド分割が正常動作する</li>
         *   <li>論理名と物理名のマッピングが正確</li>
         *   <li>辞書サイズが正しくカウントされる</li>
         *   <li>各エントリのデータが正確に格納される</li>
         * </ul>
         *
         * <p>期待動作:</p>
         * 「顧客\tcustomer client」のような形式で、5行のTSVデータが
         * 正しく5個の辞書エントリに変換される。
         */
        @Test
        void testBasicTsvLoad() throws IOException {
            String tsvData = """
                    顧客\tcustomer client
                    注文\torder
                    商品\tproduct item
                    管理\tmanagement admin
                    システム\tsystem
                    """;

            Map<String, List<String>> dictionary = loader.load(tsvData);

            assertEquals(5, dictionary.size());
            assertEquals(List.of("customer", "client"), dictionary.get("顧客"));
            assertEquals(List.of("order"), dictionary.get("注文"));
            assertEquals(List.of("product", "item"), dictionary.get("商品"));
            assertEquals(List.of("management", "admin"), dictionary.get("管理"));
            assertEquals(List.of("system"), dictionary.get("システム"));
        }

        /**
         * タブ文字を含むデータの処理テスト
         *
         * <p>検証内容:</p>
         * <ul>
         *   <li>TSVフォーマットの特徴であるタブ文字区切りの確認</li>
         *   <li>タブ文字を正しくフィールド区切り文字として認識</li>
         *   <li>物理名部分の空白区切り処理の確認</li>
         * </ul>
         *
         * <p>期待動作:</p>
         * 「年月日\tdate」のようなシンプルなタブ区切りデータが
         * 正しく処理される。
         */
        @Test
        void testTabsInData() throws IOException {
            String tsvData = """
                    年月日\tdate
                    金額\tamount price
                    """;

            Map<String, List<String>> dictionary = loader.load(tsvData);

            assertEquals(2, dictionary.size());
            assertEquals(List.of("date"), dictionary.get("年月日"));
            assertEquals(List.of("amount", "price"), dictionary.get("金額"));
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
         * 「データ\tdata」→{"データ": ["data"]} として
         * 単一要素のリストとして格納される。
         */
        @Test
        void testSinglePhysicalName() throws IOException {
            String tsvData = """
                    データ\tdata
                    情報\tinformation
                    """;

            Map<String, List<String>> dictionary = loader.load(tsvData);

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
         * 「顧客管理\tcustomer_management crm client_management」→
         * {"顧客管理": ["customer_management", "crm", "client_management"]}
         * として複数候補がリスト化される。
         */
        @Test
        void testMultiplePhysicalNames() throws IOException {
            String tsvData = """
                    顧客管理\tcustomer_management crm client_management
                    売上明細\tsales_detail revenue_line order_line
                    """;

            Map<String, List<String>> dictionary = loader.load(tsvData);

            assertEquals(2, dictionary.size());
            assertEquals(List.of("customer_management", "crm", "client_management"), dictionary.get("顧客管理"));
            assertEquals(List.of("sales_detail", "revenue_line", "order_line"), dictionary.get("売上明細"));
        }

    }

    /**
     * タブ文字とホワイトスペース処理のテスト
     * TSV特有のタブ区切りと空白文字の除去やデータ正規化機能をテストします
     */
    @Nested
    class TabCharacterAndWhitespaceHandling {

        /**
         * 空白文字処理テスト
         *
         * <p>検証内容:</p>
         * <ul>
         *   <li>論理名と物理名の前後の空白文字除去</li>
         *   <li>物理名候補間の空白文字正規化</li>
         *   <li>タブ文字と空白文字の区別</li>
         * </ul>
         *
         * <p>期待動作:</p>
         * 「  顧客  \t  customer   client  」のような
         * 余分な空白を含むデータが正しく正規化される。
         */
        @Test
        void testWhitespaceHandling() throws IOException {
            String tsvData = """
                      顧客  \t  customer   client  
                     注文 \t order 
                    """;

            Map<String, List<String>> dictionary = loader.load(tsvData);

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
         *   <li>不完全なTSVレコードのスキップ</li>
         *   <li>有効なデータのみが辞書に含まれる</li>
         * </ul>
         *
         * <p>期待動作:</p>
         * 空のフィールドや不完全な行が除外され、
         * 有効なデータのみが処理される。
         */
        @Test
        void testEmptyLines() throws IOException {
            String tsvData = """
                    顧客\tcustomer
                    \tempty_logical
                    商品\t
                    \t
                    注文\torder
                    """;

            Map<String, List<String>> dictionary = loader.load(tsvData);

            assertEquals(2, dictionary.size());
            assertEquals(List.of("customer"), dictionary.get("顧客"));
            assertEquals(List.of("order"), dictionary.get("注文"));
            assertFalse(dictionary.containsKey(""));
            assertFalse(dictionary.containsKey("商品"));
        }

        /**
         * 不完全なTSVレコード処理テスト
         *
         * <p>検証内容:</p>
         * <ul>
         *   <li>タブ文字が不足している行の処理</li>
         *   <li>不完全なレコードのスキップ</li>
         *   <li>有効なレコードのみの処理継続</li>
         * </ul>
         *
         * <p>期待動作:</p>
         * 「注文」のように物理名部分(タブ文字以降)がない行はスキップされ、
         * 有効なデータのみが辞書に含まれる。
         */
        @Test
        void testIncompleteTsvRecords() throws IOException {
            String tsvData = """
                    顧客\tcustomer
                    注文
                    商品\tproduct item
                    """;

            Map<String, List<String>> dictionary = loader.load(tsvData);

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
            String tsvData = "";

            Map<String, List<String>> dictionary = loader.load(tsvData);

            assertTrue(dictionary.isEmpty());
        }
    }
}
