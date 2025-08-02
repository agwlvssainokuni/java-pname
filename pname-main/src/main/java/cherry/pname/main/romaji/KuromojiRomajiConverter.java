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

import com.atilika.kuromoji.ipadic.Token;
import com.atilika.kuromoji.ipadic.Tokenizer;
import com.ibm.icu.text.Transliterator;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Kuromojiを使用したローマ字変換実装
 */
@Component
public class KuromojiRomajiConverter implements RomajiConverter {

    private final Tokenizer tokenizer = new Tokenizer();
    private final Transliterator katakanaToLatin = Transliterator.getInstance("Katakana-Latin");

    @Override
    public List<String> convertToRomaji(String japaneseText) {
        if (japaneseText == null || japaneseText.isEmpty()) {
            return List.of();
        }

        List<String> result = new ArrayList<>();
        List<Token> tokens = tokenizer.tokenize(japaneseText);

        for (Token token : tokens) {
            String surface = token.getSurface();
            String reading = token.getReading();

            if (containsJapanese(surface)) {
                if (reading != null && !reading.equals("*")) {
                    result.add(katakanaToLatin.transliterate(reading).toLowerCase());
                } else {
                    // 読み仮名が取得できない場合、カタカナならICU4Jで直接変換
                    if (isKatakana(surface)) {
                        result.add(katakanaToLatin.transliterate(surface).toLowerCase());
                    } else {
                        result.add(surface.toLowerCase());
                    }
                }
            } else {
                result.add(surface);
            }
        }

        return result;
    }

    private boolean containsJapanese(String text) {
        return text.chars().anyMatch(c ->
                (c >= 0x3040 && c <= 0x309F) || // ひらがな
                (c >= 0x30A0 && c <= 0x30FF) || // カタカナ
                (c >= 0x4E00 && c <= 0x9FAF) || // CJK統合漢字
                (c >= 0x3400 && c <= 0x4DBF)    // CJK拡張A
        );
    }

    private boolean isKatakana(String text) {
        return text.chars().allMatch(c ->
                (c >= 0x30A0 && c <= 0x30FF) || // カタカナ
                c == 0x30FC                     // 長音符「ー」
        );
    }
}
