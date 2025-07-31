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

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 前方最長マッチ方式のトークナイザー
 * 左から右へ順次、辞書で最も長くマッチする単語を選択する
 */
@Component("greedyTokenizer")
public class GreedyTokenizer implements Tokenizer {
    
    private final Map<String, List<String>> dictionary;
    
    public GreedyTokenizer(Map<String, List<String>> dictionary) {
        this.dictionary = dictionary;
    }
    
    @Override
    public List<Token> tokenize(String logicalName) {
        if (logicalName == null || logicalName.isEmpty()) {
            return new ArrayList<>();
        }
        
        List<Token> tokens = new ArrayList<>();
        int pos = 0;
        
        while (pos < logicalName.length()) {
            String longestMatch = null;
            int longestLength = 0;
            
            // 現在位置から始まる最長の辞書マッチを探す
            for (int end = pos + 1; end <= logicalName.length(); end++) {
                String candidate = logicalName.substring(pos, end);
                if (dictionary.containsKey(candidate) && candidate.length() > longestLength) {
                    longestMatch = candidate;
                    longestLength = candidate.length();
                }
            }
            
            if (longestMatch != null) {
                List<String> physicalNames = dictionary.get(longestMatch);
                tokens.add(new Token(longestMatch, physicalNames, false));
                pos += longestLength;
            } else {
                // 連続する未知語をまとめて処理
                int unknownStart = pos;
                while (pos < logicalName.length()) {
                    // 現在位置から辞書マッチがあるかチェック
                    boolean foundMatch = false;
                    for (int end = pos + 1; end <= logicalName.length(); end++) {
                        String candidate = logicalName.substring(pos, end);
                        if (dictionary.containsKey(candidate)) {
                            foundMatch = true;
                            break;
                        }
                    }
                    if (foundMatch) {
                        break;
                    }
                    pos++;
                }
                
                String unknownWord = logicalName.substring(unknownStart, pos);
                tokens.add(new Token(unknownWord, new ArrayList<>(), true));
            }
        }
        
        return tokens;
    }
}
