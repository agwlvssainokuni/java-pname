package cherry.pname.main.tokenize;

import java.util.List;

/**
 * 論理名を単語に分割するトークナイザーのインターフェース
 */
public interface Tokenizer {
    
    /**
     * 論理名を単語のリストに分割する
     * 
     * @param logicalName 分割対象の論理名（日本語）
     * @return 分割された単語のリスト
     */
    List<String> tokenize(String logicalName);
}