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

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * OptimalTokenizerのテストクラス
 */
class OptimalTokenizerTest extends TokenizerTestBase {
    
    private OptimalTokenizer tokenizer;
    
    @BeforeEach
    void setUp() {
        Map<String, List<String>> dictionary = createTestDictionary();
        tokenizer = new OptimalTokenizer(dictionary);
    }
    
    @Test
    void testSimpleTokenization() {
        // 基本的な分割テスト
        List<Token> result = tokenizer.tokenize("顧客管理");
        assertEquals(1, result.size());
        assertEquals("顧客管理", result.get(0).word());
        assertEquals(Arrays.asList("customer_management", "crm"), result.get(0).physicalNames());
        assertFalse(result.get(0).isUnknown());
        
        result = tokenizer.tokenize("顧客情報");
        assertEquals(2, result.size());
        assertEquals("顧客", result.get(0).word());
        assertEquals(Arrays.asList("customer", "client"), result.get(0).physicalNames());
        assertFalse(result.get(0).isUnknown());
        assertEquals("情報", result.get(1).word());
        assertEquals(Arrays.asList("information", "info"), result.get(1).physicalNames());
        assertFalse(result.get(1).isUnknown());
    }
    
    @Test
    void testOptimalChoice() {
        // 最適選択のテスト
        // "顧客管理" (1語、未知語0) vs "顧客" + "管理" (2語、未知語0)
        // → 分割数が少ない "顧客管理" が選ばれる
        List<Token> result = tokenizer.tokenize("顧客管理");
        assertEquals(1, result.size());
        assertEquals("顧客管理", result.get(0).word());
        assertFalse(result.get(0).isUnknown());
    }
    
    @Test
    void testUnknownWordMinimization() {
        // 未知語最小化のテスト
        // 辞書にない文字が含まれている場合
        List<Token> result = tokenizer.tokenize("顧客Y管理");
        // "顧客" + "Y" + "管理" (3語、未知語1) vs "顧客Y管理" (1語、未知語1)
        // 同じ未知語数の場合、辞書にある単語を多く使う方が良い
        // → 辞書語数2個 vs 辞書語数0個で前者が優位
        assertEquals(3, result.size());
        assertEquals("顧客", result.get(0).word());
        assertFalse(result.get(0).isUnknown());
        assertEquals("Y", result.get(1).word());
        assertTrue(result.get(1).isUnknown());
        assertEquals("管理", result.get(2).word());
        assertFalse(result.get(2).isUnknown());
    }
    
    @Test
    void testComplexOptimization() {
        // 複雑な最適化テスト
        List<Token> result = tokenizer.tokenize("商品管理システム");
        // "商品管理" + "システム" (2語、未知語0) が最適
        assertEquals(2, result.size());
        assertEquals("商品管理", result.get(0).word());
        assertFalse(result.get(0).isUnknown());
        assertEquals("システム", result.get(1).word());
        assertFalse(result.get(1).isUnknown());
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
        List<Token> result = tokenizer.tokenize("XYZ");
        assertEquals(1, result.size());
        assertEquals("XYZ", result.get(0).word());
        assertTrue(result.get(0).isUnknown());
    }
    
    @Test
    void testCompareWithGreedy() {
        // GreedyTokenizerとの比較テスト
        GreedyTokenizer greedyTokenizer = new GreedyTokenizer(createTestDictionary());
        
        String testInput = "注文管理システム";
        
        List<Token> greedyResult = greedyTokenizer.tokenize(testInput);
        List<Token> optimalResult = tokenizer.tokenize(testInput);
        
        // どちらも同じ結果になるはず（この場合は最適解が一意）
        assertEquals(2, greedyResult.size());
        assertEquals("注文管理", greedyResult.get(0).word());
        assertEquals("システム", greedyResult.get(1).word());
        
        assertEquals(2, optimalResult.size());
        assertEquals("注文管理", optimalResult.get(0).word());
        assertEquals("システム", optimalResult.get(1).word());
    }
    
    @Test
    void testDifferenceFromGreedy() {
        // GreedyとOptimalで結果が異なる可能性があるケース
        // より複雑な辞書構造が必要だが、現在の辞書でもテスト可能
        String testInput = "売上明細";
        
        List<Token> result = tokenizer.tokenize(testInput);
        assertEquals(2, result.size());
        assertEquals("売上", result.get(0).word());
        assertEquals("明細", result.get(1).word());
    }
}
