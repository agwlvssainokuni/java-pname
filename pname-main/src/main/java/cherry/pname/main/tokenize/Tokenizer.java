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

import java.util.List;
import java.util.Map;

/**
 * 論理名を単語に分割するトークナイザーのインターフェース
 */
public interface Tokenizer {

    /**
     * 論理名をトークンのリストに分割する
     *
     * @param dictionary 単語辞書（日本語→英語物理名のマップ）
     * @param logicalName 分割対象の論理名（日本語）
     * @return 分割されたトークンのリスト（単語、物理名、未知語フラグを含む）
     */
    List<Token> tokenize(Map<String, List<String>> dictionary, String logicalName);
}
