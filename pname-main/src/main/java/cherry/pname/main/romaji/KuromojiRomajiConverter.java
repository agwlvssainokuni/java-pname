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
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Kuromojiを使用したローマ字変換実装
 */
@Component
public class KuromojiRomajiConverter implements RomajiConverter {

    private final Tokenizer tokenizer;
    private final Pattern hiraganaPattern = Pattern.compile("[ひらがな一-龯]+");
    private final Pattern katakanaPattern = Pattern.compile("[ァ-ヶー]+");

    public KuromojiRomajiConverter() {
        this.tokenizer = new Tokenizer();
    }

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

            if (isJapanese(surface)) {
                if (reading != null && !reading.equals("*")) {
                    result.add(katakanaToRomaji(reading));
                } else {
                    result.add(fallbackRomanization(surface));
                }
            } else {
                result.add(surface);
            }
        }

        return result;
    }

    @Override
    public boolean isAvailable() {
        return tokenizer != null;
    }

    private boolean isJapanese(String text) {
        return hiraganaPattern.matcher(text).find() ||
                katakanaPattern.matcher(text).find() ||
                containsKanji(text);
    }

    private boolean containsKanji(String text) {
        return text.chars().anyMatch(c ->
                (c >= 0x4E00 && c <= 0x9FAF) || // CJK統合漢字
                        (c >= 0x3400 && c <= 0x4DBF)    // CJK拡張A
        );
    }

    private String katakanaToRomaji(String katakana) {
        return katakana
                .replace("カ", "ka").replace("キ", "ki").replace("ク", "ku").replace("ケ", "ke").replace("コ", "ko")
                .replace("サ", "sa").replace("シ", "shi").replace("ス", "su").replace("セ", "se").replace("ソ", "so")
                .replace("タ", "ta").replace("チ", "chi").replace("ツ", "tsu").replace("テ", "te").replace("ト", "to")
                .replace("ナ", "na").replace("ニ", "ni").replace("ヌ", "nu").replace("ネ", "ne").replace("ノ", "no")
                .replace("ハ", "ha").replace("ヒ", "hi").replace("フ", "hu").replace("ヘ", "he").replace("ホ", "ho")
                .replace("マ", "ma").replace("ミ", "mi").replace("ム", "mu").replace("メ", "me").replace("モ", "mo")
                .replace("ヤ", "ya").replace("ユ", "yu").replace("ヨ", "yo")
                .replace("ラ", "ra").replace("リ", "ri").replace("ル", "ru").replace("レ", "re").replace("ロ", "ro")
                .replace("ワ", "wa").replace("ヲ", "wo").replace("ン", "n")
                .replace("ア", "a").replace("イ", "i").replace("ウ", "u").replace("エ", "e").replace("オ", "o")
                .replace("ガ", "ga").replace("ギ", "gi").replace("グ", "gu").replace("ゲ", "ge").replace("ゴ", "go")
                .replace("ザ", "za").replace("ジ", "ji").replace("ズ", "zu").replace("ゼ", "ze").replace("ゾ", "zo")
                .replace("ダ", "da").replace("ヂ", "ji").replace("ヅ", "zu").replace("デ", "de").replace("ド", "do")
                .replace("バ", "ba").replace("ビ", "bi").replace("ブ", "bu").replace("ベ", "be").replace("ボ", "bo")
                .replace("パ", "pa").replace("ピ", "pi").replace("プ", "pu").replace("ペ", "pe").replace("ポ", "po")
                .replace("キャ", "kya").replace("キュ", "kyu").replace("キョ", "kyo")
                .replace("シャ", "sha").replace("シュ", "shu").replace("ショ", "sho")
                .replace("チャ", "cha").replace("チュ", "chu").replace("チョ", "cho")
                .replace("ニャ", "nya").replace("ニュ", "nyu").replace("ニョ", "nyo")
                .replace("ヒャ", "hya").replace("ヒュ", "hyu").replace("ヒョ", "hyo")
                .replace("ミャ", "mya").replace("ミュ", "myu").replace("ミョ", "myo")
                .replace("リャ", "rya").replace("リュ", "ryu").replace("リョ", "ryo")
                .replace("ギャ", "gya").replace("ギュ", "gyu").replace("ギョ", "gyo")
                .replace("ジャ", "ja").replace("ジュ", "ju").replace("ジョ", "jo")
                .replace("ビャ", "bya").replace("ビュ", "byu").replace("ビョ", "byo")
                .replace("ピャ", "pya").replace("ピュ", "pyu").replace("ピョ", "pyo")
                .replace("ー", "")
                .toLowerCase();
    }

    private String fallbackRomanization(String japaneseText) {
        return japaneseText
                .replace("管理", "management")
                .replace("システム", "system")
                .replace("データ", "data")
                .replace("情報", "information")
                .replace("処理", "process")
                .replace("売上", "sales")
                .replace("明細", "detail")
                .replace("年月日", "date")
                .replace("金額", "amount");
    }
}
