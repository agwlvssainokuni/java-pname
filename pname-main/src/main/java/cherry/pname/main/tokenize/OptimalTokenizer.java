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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 最適分割選択方式のトークナイザー
 * 考え得る分割パターンのうち最も適当な分け方を選択する
 * 評価基準：
 * 1. 辞書にない単語（未知語）が少ない方が良い
 * 2. 分割数が少ない方が良い
 */
public class OptimalTokenizer implements Tokenizer {
    
    private final Map<String, List<String>> dictionary;
    private Map<Integer, TokenizeResult> memoization;
    
    public OptimalTokenizer(Map<String, List<String>> dictionary) {
        this.dictionary = dictionary;
    }
    
    @Override
    public List<Token> tokenize(String logicalName) {
        if (logicalName == null || logicalName.isEmpty()) {
            return new ArrayList<>();
        }
        
        // メモ化用のマップを初期化
        memoization = new HashMap<>();
        
        TokenizeResult best = findOptimalTokenization(logicalName, 0);
        return best != null ? best.tokens : new ArrayList<>();
    }
    
    /**
     * 動的プログラミングとメモ化を使用して最適な分割を探す
     */
    private TokenizeResult findOptimalTokenization(String text, int start) {
        if (start >= text.length()) {
            return new TokenizeResult(new ArrayList<>(), 0, 0, 0);
        }
        
        // メモ化チェック
        if (memoization.containsKey(start)) {
            return memoization.get(start);
        }
        
        TokenizeResult bestResult = null;
        
        // 現在位置から始まるすべての可能な分割を試す
        for (int end = start + 1; end <= text.length(); end++) {
            String word = text.substring(start, end);
            boolean isInDictionary = dictionary.containsKey(word);
            
            // 残りの部分を再帰的に分割
            TokenizeResult remainingResult = findOptimalTokenization(text, end);
            if (remainingResult != null) {
                List<Token> tokens = new ArrayList<>();
                if (isInDictionary) {
                    List<String> physicalNames = dictionary.get(word);
                    tokens.add(new Token(word, physicalNames, false));
                } else {
                    tokens.add(new Token(word, new ArrayList<>(), true));
                }
                tokens.addAll(remainingResult.tokens);
                
                int unknownWords = remainingResult.unknownWords + (isInDictionary ? 0 : 1);
                int unknownLength = remainingResult.unknownLength + (isInDictionary ? 0 : word.length());
                int totalTokens = remainingResult.totalTokens + 1;
                
                TokenizeResult currentResult = new TokenizeResult(tokens, unknownWords, unknownLength, totalTokens);
                
                if (bestResult == null || isBetter(currentResult, bestResult)) {
                    bestResult = currentResult;
                }
            }
        }
        
        // メモ化
        memoization.put(start, bestResult);
        return bestResult;
    }
    
    /**
     * 分割結果の評価：優先順位に従って評価する
     */
    private boolean isBetter(TokenizeResult current, TokenizeResult best) {
        // 1. 未知語の長さで比較（短い方が良い）
        if (current.unknownLength != best.unknownLength) {
            return current.unknownLength < best.unknownLength;
        }
        // 2. 分割数で比較（少ない方が良い）
        if (current.totalTokens != best.totalTokens) {
            return current.totalTokens < best.totalTokens;
        }
        // 3. 辞書語数で比較（多い方が良い）
        int currentKnownWords = current.totalTokens - current.unknownWords;
        int bestKnownWords = best.totalTokens - best.unknownWords;
        if (currentKnownWords != bestKnownWords) {
            return currentKnownWords > bestKnownWords;
        }
        // 4. 未知語数で比較（少ない方が良い）
        return current.unknownWords < best.unknownWords;
    }
    
    /**
     * 分割結果を保持するクラス
     */
    private static class TokenizeResult {
        final List<Token> tokens;
        final int unknownWords;
        final int unknownLength;
        final int totalTokens;
        
        TokenizeResult(List<Token> tokens, int unknownWords, int unknownLength, int totalTokens) {
            this.tokens = new ArrayList<>(tokens);
            this.unknownWords = unknownWords;
            this.unknownLength = unknownLength;
            this.totalTokens = totalTokens;
        }
        
        @Override
        public String toString() {
            return String.format("TokenizeResult{tokens=%s, unknownWords=%d, unknownLength=%d, totalTokens=%d}", 
                               tokens, unknownWords, unknownLength, totalTokens);
        }
    }
}
