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
        List<String> result = tokenizer.tokenize("顧客管理");
        assertEquals(Arrays.asList("顧客管理"), result);
        
        result = tokenizer.tokenize("顧客情報");
        assertEquals(Arrays.asList("顧客", "情報"), result);
    }
    
    @Test
    void testOptimalChoice() {
        // 最適選択のテスト
        // "顧客管理" (1語、未知語0) vs "顧客" + "管理" (2語、未知語0)
        // → 分割数が少ない "顧客管理" が選ばれる
        List<String> result = tokenizer.tokenize("顧客管理");
        assertEquals(Arrays.asList("顧客管理"), result);
    }
    
    @Test
    void testUnknownWordMinimization() {
        // 未知語最小化のテスト
        // 辞書にない文字が含まれている場合
        List<String> result = tokenizer.tokenize("顧客Y管理");
        // "顧客" + "Y" + "管理" (3語、未知語1) vs "顧客Y管理" (1語、未知語1)
        // 同じ未知語数の場合、辞書にある単語を多く使う方が良い
        // → 辞書語数2個 vs 辞書語数0個で前者が優位
        assertEquals(Arrays.asList("顧客", "Y", "管理"), result);
    }
    
    @Test
    void testComplexOptimization() {
        // 複雑な最適化テスト
        List<String> result = tokenizer.tokenize("商品管理システム");
        // "商品管理" + "システム" (2語、未知語0) が最適
        assertEquals(Arrays.asList("商品管理", "システム"), result);
    }
    
    @Test
    void testEmptyAndNullInput() {
        // 空文字列とnullのテスト
        assertTrue(tokenizer.tokenize("").isEmpty());
        assertTrue(tokenizer.tokenize(null).isEmpty());
    }
    
    @Test
    void testAllUnknownWords() {
        // すべて未知語の場合
        List<String> result = tokenizer.tokenize("XYZ");
        // 未知語数は同じなので、分割数が少ない方が選ばれる
        // この場合は "XYZ" (1語、未知語1) vs "X"+"Y"+"Z" (3語、未知語3)
        // → "XYZ" が選ばれる
        assertEquals(Arrays.asList("XYZ"), result);
    }
    
    @Test
    void testCompareWithGreedy() {
        // GreedyTokenizerとの比較テスト
        GreedyTokenizer greedyTokenizer = new GreedyTokenizer(createTestDictionary());
        
        String testInput = "注文管理システム";
        
        List<String> greedyResult = greedyTokenizer.tokenize(testInput);
        List<String> optimalResult = tokenizer.tokenize(testInput);
        
        // どちらも同じ結果になるはず（この場合は最適解が一意）
        assertEquals(Arrays.asList("注文管理", "システム"), greedyResult);
        assertEquals(Arrays.asList("注文管理", "システム"), optimalResult);
    }
    
    @Test
    void testDifferenceFromGreedy() {
        // GreedyとOptimalで結果が異なる可能性があるケース
        // より複雑な辞書構造が必要だが、現在の辞書でもテスト可能
        String testInput = "売上明細";
        
        List<String> result = tokenizer.tokenize(testInput);
        assertEquals(Arrays.asList("売上", "明細"), result);
    }
}