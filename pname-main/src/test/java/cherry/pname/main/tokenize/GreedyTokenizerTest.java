package cherry.pname.main.tokenize;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
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
        List<String> result = tokenizer.tokenize("顧客管理");
        assertEquals(Arrays.asList("顧客管理"), result);
        
        result = tokenizer.tokenize("顧客情報");
        assertEquals(Arrays.asList("顧客", "情報"), result);
    }
    
    @Test
    void testLongestMatchPriority() {
        // 最長マッチの優先度テスト
        // "顧客管理" vs "顧客" + "管理"
        List<String> result = tokenizer.tokenize("顧客管理システム");
        assertEquals(Arrays.asList("顧客管理", "システム"), result);
    }
    
    @Test
    void testUnknownWords() {
        // 未知語を含む場合のテスト
        List<String> result = tokenizer.tokenize("顧客X情報");
        assertEquals(Arrays.asList("顧客", "X", "情報"), result);
    }
    
    @Test
    void testComplexTokenization() {
        // 複雑な分割テスト
        List<String> result = tokenizer.tokenize("注文明細管理システム");
        assertEquals(Arrays.asList("注文", "明細", "管理", "システム"), result);
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
        List<String> result = tokenizer.tokenize("ABC");
        assertEquals(Arrays.asList("A", "B", "C"), result);
    }
    
    @Test
    void testMixedKnownUnknown() {
        // 既知語と未知語の混在
        List<String> result = tokenizer.tokenize("顧客ABC情報");
        assertEquals(Arrays.asList("顧客", "A", "B", "C", "情報"), result);
    }
}