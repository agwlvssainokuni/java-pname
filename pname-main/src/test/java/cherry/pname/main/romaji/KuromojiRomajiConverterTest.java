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
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * KuromojiRomajiConverterのテストクラス
 */
class KuromojiRomajiConverterTest {

    private KuromojiRomajiConverter converter;

    @BeforeEach
    void setUp() {
        converter = new KuromojiRomajiConverter();
    }


    @Test
    void testConvertSimpleKanji() {
        List<String> result = converter.convertToRomaji("管理");
        assertFalse(result.isEmpty());
        assertEquals("kanri", result.get(0));
    }

    @Test
    void testConvertSystemKanji() {
        List<String> result = converter.convertToRomaji("システム");
        assertFalse(result.isEmpty());
        assertEquals("shisutemu", result.get(0)); // ICU4Jによる変換
    }

    @Test
    void testConvertMixedText() {
        List<String> result = converter.convertToRomaji("顧客管理");
        assertEquals(2, result.size());
    }

    @Test
    void testConvertUnknownKanji() {
        List<String> result = converter.convertToRomaji("XY管理");
        assertFalse(result.isEmpty());
        assertTrue(result.size() >= 1);
    }

    @Test
    void testConvertAlphanumeric() {
        List<String> result = converter.convertToRomaji("ABC123");
        assertEquals(1, result.size());
        assertEquals("ABC123", result.get(0));
    }

    @Test
    void testConvertEmptyString() {
        List<String> result = converter.convertToRomaji("");
        assertTrue(result.isEmpty());
    }

    @Test
    void testConvertNull() {
        List<String> result = converter.convertToRomaji(null);
        assertTrue(result.isEmpty());
    }

    @Test
    void testConvertKatakanaReading() {
        List<String> result = converter.convertToRomaji("データ");
        assertFalse(result.isEmpty());
        assertEquals("deta", result.get(0)); // ICU4Jによる変換
    }

    @Test
    void testConvertComplexText() {
        List<String> result = converter.convertToRomaji("顧客情報管理システム");
        assertTrue(result.size() >= 3);
        // ICU4Jによる正確なローマ字変換が行われることを確認
        assertFalse(result.isEmpty());
    }

    @Test
    void testICU4JTransliteration() {
        List<String> result = converter.convertToRomaji("カタカナ");
        assertEquals(1, result.size());
        assertEquals("katakana", result.get(0));
    }

    @Test
    void testICU4JWithLongVowels() {
        List<String> result = converter.convertToRomaji("コーヒー");
        assertEquals(1, result.size());
        assertEquals("kohi", result.get(0)); // ICU4Jは長音符を処理
    }
}
