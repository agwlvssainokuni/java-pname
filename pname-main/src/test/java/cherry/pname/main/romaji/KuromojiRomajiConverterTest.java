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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * KuromojiRomajiConverterのテストクラス
 *
 * <p>Kuromoji形態素解析とICU4Jローマ字変換機能を階層的にテストします：</p>
 * <ul>
 *   <li>漢字・ひらがな・カタカナのローマ字変換</li>
 *   <li>ICU4Jトランスリタレーション機能</li>
 *   <li>混合テキストと英数字処理</li>
 *   <li>境界値・エラーケース</li>
 * </ul>
 */
class KuromojiRomajiConverterTest {

    private KuromojiRomajiConverter converter;

    @BeforeEach
    void setUp() {
        converter = new KuromojiRomajiConverter();
    }


    /**
     * 漢字・ひらがな・カタカナのローマ字変換テスト
     * 日本語文字の基本的なローマ字変換機能をテストします
     */
    @Nested
    class BasicJapaneseRomanization {

        /**
         * 簡単な漢字のローマ字変換テスト
         *
         * <p>検証内容:</p>
         * <ul>
         *   <li>Kuromojiによる形態素解析が正常動作する</li>
         *   <li>ICU4Jによるローマ字変換が正確に実行される</li>
         *   <li>結果リストが空でない</li>
         *   <li>期待されるローマ字形式の確認</li>
         * </ul>
         *
         * <p>期待動作:</p>
         * 「管理」→["kanri"] として正確に変換される。
         */
        @Test
        void testConvertSimpleKanji() {
            List<String> result = converter.convertToRomaji("管理");
            assertFalse(result.isEmpty());
            assertEquals("kanri", result.get(0));
        }

        /**
         * カタカナ単語のローマ字変換テスト
         *
         * <p>検証内容:</p>
         * <ul>
         *   <li>カタカナ語彙の適切な変換</li>
         *   <li>ICU4Jトランスリタレーションの動作確認</li>
         *   <li>システム関連用語の変換精度</li>
         * </ul>
         *
         * <p>期待動作:</p>
         * 「システム」→["shisutemu"] としてICU4Jにより変換される。
         */
        @Test
        void testConvertSystemKanji() {
            List<String> result = converter.convertToRomaji("システム");
            assertFalse(result.isEmpty());
            assertEquals("shisutemu", result.get(0));
        }

        /**
         * 混合テキストのトークン化テスト
         *
         * <p>検証内容:</p>
         * <ul>
         *   <li>複数単語が適切に分割される</li>
         *   <li>各単語が個別にローマ字変換される</li>
         *   <li>結果リストのサイズが適切</li>
         * </ul>
         *
         * <p>期待動作:</p>
         * 「顧客管理」が2つのトークンに分割される。
         */
        @Test
        void testConvertMixedText() {
            List<String> result = converter.convertToRomaji("顧客管理");
            assertEquals(2, result.size());
        }

        /**
         * 複雑な日本語テキストの変換テスト
         *
         * <p>検証内容:</p>
         * <ul>
         *   <li>長い日本語文章の適切な処理</li>
         *   <li>形態素解析による適切な分割</li>
         *   <li>ICU4Jによる精度の高いローマ字変換</li>
         * </ul>
         *
         * <p>期待動作:</p>
         * 「顧客情報管理システム」が3以上のトークンに分割される。
         */
        @Test
        void testConvertComplexText() {
            List<String> result = converter.convertToRomaji("顧客情報管理システム");
            assertTrue(result.size() >= 3);
            assertFalse(result.isEmpty());
        }
    }

    /**
     * ICU4Jトランスリタレーション機能のテスト
     * ICU4Jライブラリの高精度なローマ字変換機能をテストします
     */
    @Nested
    class ICU4JTransliterationFeatures {

        /**
         * ICU4Jカタカナ変換テスト
         *
         * <p>検証内容:</p>
         * <ul>
         *   <li>ICU4Jトランスリタレーションの正確性</li>
         *   <li>カタカナからローマ字への直接変換</li>
         *   <li>結果の一意性と再現性</li>
         * </ul>
         *
         * <p>期待動作:</p>
         * 「カタカナ」→["katakana"] として正確に変換される。
         */
        @Test
        void testICU4JTransliteration() {
            List<String> result = converter.convertToRomaji("カタカナ");
            assertEquals(1, result.size());
            assertEquals("katakana", result.get(0));
        }

        /**
         * ICU4J長音記号処理テスト
         *
         * <p>検証内容:</p>
         * <ul>
         *   <li>長音記号（ー）の適切な処理</li>
         *   <li>ICU4Jによる国際的なローマ字表記対応</li>
         *   <li>マクロン付き文字の出力</li>
         * </ul>
         *
         * <p>期待動作:</p>
         * 「コーヒー」→["kōhī"] として長音記号付きで変換される。
         */
        @Test
        void testICU4JWithLongVowels() {
            List<String> result = converter.convertToRomaji("コーヒー");
            assertEquals(1, result.size());
            assertEquals("kōhī", result.get(0));
        }

        /**
         * ICU4Jカタカナ語彙のローマ字変換テスト
         *
         * <p>検証内容:</p>
         * <ul>
         *   <li>カタカナ語彙の精度の高い変換</li>
         *   <li>ICU4J長音記号処理機能</li>
         *   <li>外来語用語の適切なローマ字化</li>
         * </ul>
         *
         * <p>期待動作:</p>
         * 「データ」→["dēta"] として長音記号付きで変換される。
         */
        @Test
        void testConvertKatakanaReading() {
            List<String> result = converter.convertToRomaji("データ");
            assertFalse(result.isEmpty());
            assertEquals("dēta", result.get(0));
        }
    }

    /**
     * 混合テキストと英数字処理のテスト
     * 日本語以外の文字や記号を含むテキストの処理をテストします
     */
    @Nested
    class MixedTextAndAlphanumericHandling {

        /**
         * 未知語混在テキストの処理テスト
         *
         * <p>検証内容:</p>
         * <ul>
         *   <li>英数字と日本語が混在するテキストの処理</li>
         *   <li>Kuromojiによる適切な分割</li>
         *   <li>結果リストの適切なサイズ</li>
         * </ul>
         *
         * <p>期待動作:</p>
         * 「XY管理」が適切にトークン化され、1以上のトークンに分割される。
         */
        @Test
        void testConvertUnknownKanji() {
            List<String> result = converter.convertToRomaji("XY管理");
            assertFalse(result.isEmpty());
            assertTrue(result.size() >= 1);
        }

        /**
         * 英数字文字列の処理テスト
         *
         * <p>検証内容:</p>
         * <ul>
         *   <li>Kuromojiによる英字と数字の分割</li>
         *   <li>アルファベットと数字の個別処理</li>
         *   <li>結果トークンの正確性</li>
         * </ul>
         *
         * <p>期待動作:</p>
         * 「ABC123」→["ABC", "123"] として英字と数字が分割される。
         */
        @Test
        void testConvertAlphanumeric() {
            List<String> result = converter.convertToRomaji("ABC123");
            assertEquals(2, result.size());
            assertEquals("ABC", result.get(0));
            assertEquals("123", result.get(1));
        }
    }

    /**
     * 境界値・エラーケースのテスト
     * 異常入力や境界条件での動作をテストします
     */
    @Nested
    class EdgeCasesAndErrorHandling {

        /**
         * 空文字列入力処理テスト
         *
         * <p>検証内容:</p>
         * <ul>
         *   <li>空文字列入力時に空リストが返される</li>
         *   <li>例外がスローされない</li>
         *   <li>安全なエラーハンドリング</li>
         * </ul>
         *
         * <p>期待動作:</p>
         * 空文字列に対して安全に処理され、空のリストが返される。
         */
        @Test
        void testConvertEmptyString() {
            List<String> result = converter.convertToRomaji("");
            assertTrue(result.isEmpty());
        }

        /**
         * null入力処理テスト
         *
         * <p>検証内容:</p>
         * <ul>
         *   <li>null入力時に空リストが返される</li>
         *   <li>NullPointerExceptionがスローされない</li>
         *   <li>安全なエラーハンドリング</li>
         * </ul>
         *
         * <p>期待動作:</p>
         * null入力に対して安全に処理され、空のリストが返される。
         */
        @Test
        void testConvertNull() {
            List<String> result = converter.convertToRomaji(null);
            assertTrue(result.isEmpty());
        }
    }
}
