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

package cherry.pname.main.tokenize;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * GreedyTokenizerのテストクラス
 *
 * <p>前方最長マッチアルゴリズムによるトークン化処理を階層的にテストします：</p>
 * <ul>
 *   <li>基本的なトークン化機能</li>
 *   <li>最長マッチ優先度処理</li>
 *   <li>未知語処理とマーキング</li>
 *   <li>境界値・エラーケース</li>
 * </ul>
 */
class GreedyTokenizerTest extends TokenizerTestBase {

    private GreedyTokenizer tokenizer;
    private Map<String, List<String>> dictionary;

    @BeforeEach
    void setUp() {
        tokenizer = new GreedyTokenizer();
        dictionary = createTestDictionary();
    }

    /**
     * 基本的なトークン化機能のテスト
     * 辞書による単語マッチングと分割処理をテストします
     */
    @Nested
    class BasicTokenization {

        /**
         * 単純なトークン化処理テスト
         *
         * <p>検証内容:</p>
         * <ul>
         *   <li>辞書に完全一致する複合語が正しく認識される</li>
         *   <li>複数の物理名候補が適切に設定される</li>
         *   <li>既知語のisUnknown()がfalseになる</li>
         *   <li>複数単語への分割が正常動作する</li>
         * </ul>
         *
         * <p>期待動作:</p>
         * 「顧客管理」→["顧客管理"(1語、既知)]、
         * 「顧客情報」→["顧客"(既知), "情報"(既知)] として分割される。
         */
        @Test
        void testSimpleTokenization() {
            List<Token> result = tokenizer.tokenize(dictionary, "顧客管理");
            assertEquals(1, result.size());
            assertEquals("顧客管理", result.get(0).word());
            assertEquals(List.of("customer_management", "crm"), result.get(0).physicalNames());
            assertFalse(result.get(0).isUnknown());

            result = tokenizer.tokenize(dictionary, "顧客情報");
            assertEquals(2, result.size());
            assertEquals("顧客", result.get(0).word());
            assertEquals(List.of("customer", "client"), result.get(0).physicalNames());
            assertFalse(result.get(0).isUnknown());
            assertEquals("情報", result.get(1).word());
            assertEquals(List.of("information", "info"), result.get(1).physicalNames());
            assertFalse(result.get(1).isUnknown());
        }

        /**
         * 複雑なトークン化処理テスト
         *
         * <p>検証内容:</p>
         * <ul>
         *   <li>長い文字列が適切に複数単語に分割される</li>
         *   <li>各トークンが正しい単語を表す</li>
         *   <li>分割順序が入力順序と一致する</li>
         * </ul>
         *
         * <p>期待動作:</p>
         * 「注文明細管理システム」→["注文", "明細", "管理", "システム"]
         * の順序で4つのトークンに分割される。
         */
        @Test
        void testComplexTokenization() {
            List<Token> result = tokenizer.tokenize(dictionary, "注文明細管理システム");
            assertEquals(4, result.size());
            assertEquals("注文", result.get(0).word());
            assertEquals("明細", result.get(1).word());
            assertEquals("管理", result.get(2).word());
            assertEquals("システム", result.get(3).word());
        }
    }

    /**
     * 最長マッチ優先度処理のテスト
     * Greedyアルゴリズムの特徴である前方最長マッチの動作をテストします
     */
    @Nested
    class LongestMatchPriority {

        /**
         * 最長マッチ優先度確認テスト
         *
         * <p>検証内容:</p>
         * <ul>
         *   <li>「顧客管理」vs「顧客」+「管理」で長い方が選ばれる</li>
         *   <li>前方最長マッチアルゴリズムの正確性</li>
         *   <li>複合語の優先的な認識</li>
         * </ul>
         *
         * <p>期待動作:</p>
         * 「顧客管理システム」において、「顧客」と「管理」に分割するより
         * 「顧客管理」として認識することを優先する。
         */
        @Test
        void testLongestMatchPriority() {
            List<Token> result = tokenizer.tokenize(dictionary, "顧客管理システム");
            assertEquals(2, result.size());
            assertEquals("顧客管理", result.get(0).word());
            assertEquals(List.of("customer_management", "crm"), result.get(0).physicalNames());
            assertFalse(result.get(0).isUnknown());
            assertEquals("システム", result.get(1).word());
            assertEquals(List.of("system"), result.get(1).physicalNames());
            assertFalse(result.get(1).isUnknown());
        }
    }

    /**
     * 未知語処理とマーキングのテスト
     * 辞書にない単語の処理と未知語フラグの設定をテストします
     */
    @Nested
    class UnknownWordHandling {

        /**
         * 未知語混在時の処理テスト
         *
         * <p>検証内容:</p>
         * <ul>
         *   <li>既知語と未知語が混在する場合の適切な分割</li>
         *   <li>未知語のisUnknown()がtrueになる</li>
         *   <li>未知語の物理名リストが空になる</li>
         *   <li>既知語の処理に影響しない</li>
         * </ul>
         *
         * <p>期待動作:</p>
         * 「顧客X情報」→["顧客"(既知), "X"(未知), "情報"(既知)]
         * として適切にマーキングされる。
         */
        @Test
        void testUnknownWords() {
            List<Token> result = tokenizer.tokenize(dictionary, "顧客X情報");
            assertEquals(3, result.size());
            assertEquals("顧客", result.get(0).word());
            assertFalse(result.get(0).isUnknown());
            assertEquals("X", result.get(1).word());
            assertTrue(result.get(1).isUnknown());
            assertTrue(result.get(1).physicalNames().isEmpty());
            assertEquals("情報", result.get(2).word());
            assertFalse(result.get(2).isUnknown());
        }

        /**
         * 全未知語処理テスト
         *
         * <p>検証内容:</p>
         * <ul>
         *   <li>辞書にまったく一致しない文字列の処理</li>
         *   <li>連続する未知語が1つのトークンにまとめられる</li>
         *   <li>未知語フラグが正しく設定される</li>
         * </ul>
         *
         * <p>期待動作:</p>
         * 「ABC」→["ABC"(未知)] として1つのトークンになる。
         */
        @Test
        void testAllUnknownWords() {
            List<Token> result = tokenizer.tokenize(dictionary, "ABC");
            assertEquals(1, result.size());
            assertEquals("ABC", result.get(0).word());
            assertTrue(result.get(0).isUnknown());
        }

        /**
         * 既知語と未知語の混在処理テスト
         *
         * <p>検証内容:</p>
         * <ul>
         *   <li>連続する未知語文字が1つのトークンにまとめられる</li>
         *   <li>前後の既知語処理に影響しない</li>
         *   <li>各トークンの未知語フラグが正確</li>
         * </ul>
         *
         * <p>期待動作:</p>
         * 「顧客ABC情報」→["顧客"(既知), "ABC"(未知), "情報"(既知)]
         * として適切に分割・マーキングされる。
         */
        @Test
        void testMixedKnownUnknown() {
            List<Token> result = tokenizer.tokenize(dictionary, "顧客ABC情報");
            assertEquals(3, result.size());
            assertEquals("顧客", result.get(0).word());
            assertFalse(result.get(0).isUnknown());
            assertEquals("ABC", result.get(1).word());
            assertTrue(result.get(1).isUnknown());
            assertEquals("情報", result.get(2).word());
            assertFalse(result.get(2).isUnknown());
        }
    }

    /**
     * 境界値・エラーケースのテスト
     * 異常入力や境界条件での動作をテストします
     */
    @Nested
    class EdgeCasesAndErrorHandling {

        /**
         * 空文字列・null入力処理テスト
         *
         * <p>検証内容:</p>
         * <ul>
         *   <li>空文字列入力時に空リストが返される</li>
         *   <li>null入力時に空リストが返される</li>
         *   <li>例外がスローされない</li>
         * </ul>
         *
         * <p>期待動作:</p>
         * 異常入力に対して安全に処理され、空のトークンリストが返される。
         */
        @Test
        void testEmptyAndNullInput() {
            assertTrue(tokenizer.tokenize(dictionary, "").isEmpty());
            assertTrue(tokenizer.tokenize(dictionary, null).isEmpty());
        }
    }
}
