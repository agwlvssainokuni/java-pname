package cherry.pname.main.tokenize;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 前方最長マッチ方式のトークナイザー
 * 左から右へ順次、辞書で最も長くマッチする単語を選択する
 */
public class GreedyTokenizer implements Tokenizer {
    
    private final Map<String, List<String>> dictionary;
    
    public GreedyTokenizer(Map<String, List<String>> dictionary) {
        this.dictionary = dictionary;
    }
    
    @Override
    public List<String> tokenize(String logicalName) {
        if (logicalName == null || logicalName.isEmpty()) {
            return new ArrayList<>();
        }
        
        List<String> tokens = new ArrayList<>();
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
                tokens.add(longestMatch);
                pos += longestLength;
            } else {
                // マッチしない場合は1文字を未知語として扱う
                tokens.add(logicalName.substring(pos, pos + 1));
                pos++;
            }
        }
        
        return tokens;
    }
}