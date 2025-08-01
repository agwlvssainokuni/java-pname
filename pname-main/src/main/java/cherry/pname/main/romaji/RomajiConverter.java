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

package cherry.pname.main.romaji;

import java.util.List;

/**
 * 日本語をローマ字に変換するインターフェース
 */
public interface RomajiConverter {

    /**
     * 日本語文字列をローマ字の要素リストに変換する
     *
     * @param japaneseText 日本語テキスト
     * @return ローマ字要素のリスト
     */
    List<String> convertToRomaji(String japaneseText);

    /**
     * 変換器が利用可能かどうかを確認する
     *
     * @return 利用可能な場合true
     */
    boolean isAvailable();
}
