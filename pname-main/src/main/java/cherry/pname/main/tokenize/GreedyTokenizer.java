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

import org.apache.commons.collections4.trie.PatriciaTrie;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;

/**
 * 前方最長マッチ方式のトークナイザー
 * 左から右へ順次、辞書で最も長くマッチする単語を選択する
 */
@Component("greedyTokenizer")
public class GreedyTokenizer implements Tokenizer {

    @Override
    public List<Token> tokenize(Map<String, List<String>> dictionary, String logicalName) {
        if (logicalName == null || logicalName.isEmpty()) {
            return new ArrayList<>();
        }

        // Trie構造を事前に構築
        PatriciaTrie<List<String>> trie = buildTrie(dictionary);

        List<Token> tokens = new ArrayList<>();
        int pos = 0;

        while (pos < logicalName.length()) {
            MatchResult matchResult = findLongestMatchWithTrie(trie, logicalName, pos);
            
            if (matchResult.hasMatch()) {
                List<String> physicalNames = trie.get(matchResult.match());
                tokens.add(new Token(matchResult.match(), physicalNames, false));
                pos += matchResult.length();
            } else {
                // 連続する未知語をまとめて処理
                int unknownStart = pos;
                while (pos < logicalName.length()) {
                    MatchResult nextMatch = findLongestMatchWithTrie(trie, logicalName, pos);
                    if (nextMatch.hasMatch()) {
                        break;
                    }
                    pos++;
                }

                String unknownWord = logicalName.substring(unknownStart, pos);
                tokens.add(new Token(unknownWord, List.of(), true));
            }
        }

        return tokens;
    }

    /**
     * 辞書からTrie構造を構築
     */
    private PatriciaTrie<List<String>> buildTrie(Map<String, List<String>> dictionary) {
        PatriciaTrie<List<String>> trie = new PatriciaTrie<>();
        for (Map.Entry<String, List<String>> entry : dictionary.entrySet()) {
            trie.put(entry.getKey(), entry.getValue());
        }
        return trie;
    }

    /**
     * Trie構造を使って指定位置から始まる最長マッチを探す
     */
    private MatchResult findLongestMatchWithTrie(PatriciaTrie<List<String>> trie, String text, int startPos) {
        String longestMatch = null;
        int longestLength = 0;
        
        // startPosから始まる部分文字列で接頭辞マッチを探索
        for (int end = startPos + 1; end <= text.length(); end++) {
            String candidate = text.substring(startPos, end);
            
            if (trie.containsKey(candidate)) {
                longestMatch = candidate;
                longestLength = candidate.length();
            }
            
            // 接頭辞マッチがなくなったら早期終了
            SortedMap<String, List<String>> prefixMatches = trie.prefixMap(candidate);
            if (prefixMatches.isEmpty()) {
                break;
            }
        }

        return longestMatch != null ? 
            new MatchResult(longestMatch, longestLength) : 
            new MatchResult(null, 0);
    }

    /**
     * マッチ結果を保持するレコード
     */
    private record MatchResult(String match, int length) {
        boolean hasMatch() {
            return match != null;
        }
    }
}
