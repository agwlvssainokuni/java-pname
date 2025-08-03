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
 * OptimalTokenizerのテストクラス
 *
 * <p>動的計画法による最適トークン化処理を階層的にテストします：</p>
 * <ul>
 *   <li>基本的なトークン化機能</li>
 *   <li>最適選択アルゴリズムの動作</li>
 *   <li>未知語最小化と評価機能</li>
 *   <li>Greedyアルゴリズムとの比較</li>
 *   <li>境界値・エラーケース</li>
 * </ul>
 */
class OptimalTokenizerTest extends TokenizerTestBase {

    private OptimalTokenizer tokenizer;
    private Map<String, List<String>> dictionary;

    @BeforeEach
    void setUp() {
        tokenizer = new OptimalTokenizer();
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
         * 「顧客管理」→["顧客管理"(既知)]、
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
         * 複雑な最適化処理テスト
         *
         * <p>検証内容:</p>
         * <ul>
         *   <li>複数の分割パターンから最適解が選ばれる</li>
         *   <li>辞書マッチ優先アルゴリズムの動作</li>
         *   <li>トークン数最小化の務先</li>
         * </ul>
         *
         * <p>期待動作:</p>
         * 「商品管理システム」→["商品管理", "システム"](2語)
         * が最適解として選ばれる。
         */
        @Test
        void testComplexOptimization() {
            List<Token> result = tokenizer.tokenize(dictionary, "商品管理システム");
            assertEquals(2, result.size());
            assertEquals("商品管理", result.get(0).word());
            assertFalse(result.get(0).isUnknown());
            assertEquals("システム", result.get(1).word());
            assertFalse(result.get(1).isUnknown());
        }
    }

    /**
     * 最適選択アルゴリズムのテスト
     * 動的計画法による最適解選択の動作をテストします
     */
    @Nested
    class OptimalChoiceAlgorithm {

        /**
         * 最適選択アルゴリズム確認テスト
         *
         * <p>検証内容:</p>
         * <ul>
         *   <li>複数の分割パターンから最適解が選ばれる</li>
         *   <li>トークン数最小化が優先される</li>
         *   <li>同じトークン数の場合の評価機能</li>
         * </ul>
         *
         * <p>期待動作:</p>
         * 「顧客管理」において、
         * 1語("顧客管理") vs 2語("顧客"+"管理")で
         * トークン数が少ない前者が選ばれる。
         */
        @Test
        void testOptimalChoice() {
            List<Token> result = tokenizer.tokenize(dictionary, "顧客管理");
            assertEquals(1, result.size());
            assertEquals("顧客管理", result.get(0).word());
            assertFalse(result.get(0).isUnknown());
        }
    }

    /**
     * 未知語最小化と評価機能のテスト
     * 未知語を含む場合の最適化アルゴリズムをテストします
     */
    @Nested
    class UnknownWordMinimization {

        /**
         * 未知語最小化処理テスト
         *
         * <p>検証内容:</p>
         * <ul>
         *   <li>未知語数最小化が最優先される</li>
         *   <li>同じ未知語数の場合、辞書語数最大化が適用される</li>
         *   <li>既知語と未知語の混在ケースでの最適解</li>
         * </ul>
         *
         * <p>期待動作:</p>
         * 「顧客Y管理」では、
         * 3語("顧客"+"Y"+"管理", 未知語1, 辞書語2) vs
         * 1語("顧客Y管理", 未知語1, 辞書語0)で
         * 辞書語数が多い前者が選ばれる。
         */
        @Test
        void testUnknownWordMinimization() {
            List<Token> result = tokenizer.tokenize(dictionary, "顧客Y管理");
            assertEquals(3, result.size());
            assertEquals("顧客", result.get(0).word());
            assertFalse(result.get(0).isUnknown());
            assertEquals("Y", result.get(1).word());
            assertTrue(result.get(1).isUnknown());
            assertEquals("管理", result.get(2).word());
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
         * 「XYZ」→["XYZ"(未知)] とし1つのトークンになる。
         */
        @Test
        void testAllUnknownWords() {
            List<Token> result = tokenizer.tokenize(dictionary, "XYZ");
            assertEquals(1, result.size());
            assertEquals("XYZ", result.get(0).word());
            assertTrue(result.get(0).isUnknown());
        }
    }

    /**
     * Greedyアルゴリズムとの比較テスト
     * 両アルゴリズムの特性と違いをテストします
     */
    @Nested
    class ComparisonWithGreedyAlgorithm {

        /**
         * GreedyとOptimalの一致ケーステスト
         *
         * <p>検証内容:</p>
         * <ul>
         *   <li>最適解が一意の場合、両アルゴリズムの結果が一致する</li>
         *   <li>シンプルなケースでの両アルゴリズムの正確性</li>
         *   <li>同じトークン分割結果の確認</li>
         * </ul>
         *
         * <p>期待動作:</p>
         * 「注文管理システム」では、どちらのアルゴリズムでも
         * ["注文管理", "システム"] という同じ結果になる。
         */
        @Test
        void testCompareWithGreedy() {
            GreedyTokenizer greedyTokenizer = new GreedyTokenizer();

            String testInput = "注文管理システム";

            List<Token> greedyResult = greedyTokenizer.tokenize(dictionary, testInput);
            List<Token> optimalResult = tokenizer.tokenize(dictionary, testInput);

            assertEquals(2, greedyResult.size());
            assertEquals("注文管理", greedyResult.get(0).word());
            assertEquals("システム", greedyResult.get(1).word());

            assertEquals(2, optimalResult.size());
            assertEquals("注文管理", optimalResult.get(0).word());
            assertEquals("システム", optimalResult.get(1).word());
        }

        /**
         * GreedyとOptimalの潜在的相違確認テスト
         *
         * <p>検証内容:</p>
         * <ul>
         *   <li>現在のテスト辞書でのOptimalアルゴリズムの動作</li>
         *   <li>将来的に複雑な辞書構造で違いが現れる可能性の確認</li>
         *   <li>アルゴリズムの安定性テスト</li>
         * </ul>
         *
         * <p>期待動作:</p>
         * 「売上明細」→["売上", "明細"] として適切に分割される。
         */
        @Test
        void testDifferenceFromGreedy() {
            String testInput = "売上明細";

            List<Token> result = tokenizer.tokenize(dictionary, testInput);
            assertEquals(2, result.size());
            assertEquals("売上", result.get(0).word());
            assertEquals("明細", result.get(1).word());
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
