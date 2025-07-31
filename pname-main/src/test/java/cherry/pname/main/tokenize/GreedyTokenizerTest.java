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
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * GreedyTokenizerのテストクラス
 */
class GreedyTokenizerTest extends TokenizerTestBase {

    private GreedyTokenizer tokenizer;

    @BeforeEach
    void setUp() {
        Map<String, List<String>> dictionary = createTestDictionary();
        tokenizer = new GreedyTokenizer(dictionary);
    }

    @Test
    void testSimpleTokenization() {
        // 基本的な分割テスト
        List<Token> result = tokenizer.tokenize("顧客管理");
        assertEquals(1, result.size());
        assertEquals("顧客管理", result.get(0).word());
        assertEquals(List.of("customer_management", "crm"), result.get(0).physicalNames());
        assertFalse(result.get(0).isUnknown());

        result = tokenizer.tokenize("顧客情報");
        assertEquals(2, result.size());
        assertEquals("顧客", result.get(0).word());
        assertEquals(List.of("customer", "client"), result.get(0).physicalNames());
        assertFalse(result.get(0).isUnknown());
        assertEquals("情報", result.get(1).word());
        assertEquals(List.of("information", "info"), result.get(1).physicalNames());
        assertFalse(result.get(1).isUnknown());
    }

    @Test
    void testLongestMatchPriority() {
        // 最長マッチの優先度テスト
        // "顧客管理" vs "顧客" + "管理"
        List<Token> result = tokenizer.tokenize("顧客管理システム");
        assertEquals(2, result.size());
        assertEquals("顧客管理", result.get(0).word());
        assertEquals(List.of("customer_management", "crm"), result.get(0).physicalNames());
        assertFalse(result.get(0).isUnknown());
        assertEquals("システム", result.get(1).word());
        assertEquals(List.of("system"), result.get(1).physicalNames());
        assertFalse(result.get(1).isUnknown());
    }

    @Test
    void testUnknownWords() {
        // 未知語を含む場合のテスト
        List<Token> result = tokenizer.tokenize("顧客X情報");
        assertEquals(3, result.size());
        assertEquals("顧客", result.get(0).word());
        assertFalse(result.get(0).isUnknown());
        assertEquals("X", result.get(1).word());
        assertTrue(result.get(1).isUnknown());
        assertTrue(result.get(1).physicalNames().isEmpty());
        assertEquals("情報", result.get(2).word());
        assertFalse(result.get(2).isUnknown());
    }

    @Test
    void testComplexTokenization() {
        // 複雑な分割テスト
        List<Token> result = tokenizer.tokenize("注文明細管理システム");
        assertEquals(4, result.size());
        assertEquals("注文", result.get(0).word());
        assertEquals("明細", result.get(1).word());
        assertEquals("管理", result.get(2).word());
        assertEquals("システム", result.get(3).word());
    }

    @Test
    void testEmptyAndNullInput() {
        // 空文字列とnullのテスト
        assertTrue(tokenizer.tokenize("").isEmpty());
        assertTrue(tokenizer.tokenize(null).isEmpty());
    }

    @Test
    void testAllUnknownWords() {
        // すべて未知語の場合（連続する未知語は一つにまとめられる）
        List<Token> result = tokenizer.tokenize("ABC");
        assertEquals(1, result.size());
        assertEquals("ABC", result.get(0).word());
        assertTrue(result.get(0).isUnknown());
    }

    @Test
    void testMixedKnownUnknown() {
        // 既知語と未知語の混在（連続する未知語は一つにまとめられる）
        List<Token> result = tokenizer.tokenize("顧客ABC情報");
        assertEquals(3, result.size());
        assertEquals("顧客", result.get(0).word());
        assertFalse(result.get(0).isUnknown());
        assertEquals("ABC", result.get(1).word());
        assertTrue(result.get(1).isUnknown());
        assertEquals("情報", result.get(2).word());
        assertFalse(result.get(2).isUnknown());
    }
}
