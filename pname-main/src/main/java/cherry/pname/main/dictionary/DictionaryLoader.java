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

package cherry.pname.main.dictionary;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * 辞書読み込み機能のインターフェース
 */
public interface DictionaryLoader {

    /**
     * 辞書を読み込む
     *
     * @param source 辞書ソース（ファイルパスやリソースパスなど）
     * @return 辞書データ（日本語→英語物理名のマップ）
     * @throws IOException 読み込みエラーが発生した場合
     */
    Map<String, List<String>> load(String source) throws IOException;
}
