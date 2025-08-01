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

package cherry.pname.main;

import cherry.pname.main.dictionary.DictionaryLoader;
import cherry.pname.main.romaji.RomajiConverter;
import cherry.pname.main.tokenize.Token;
import cherry.pname.main.tokenize.Tokenizer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 論理名から物理名を生成するメインクラス
 */
@Component
public class PhysicalNameGenerator {

    private final DictionaryLoader csvDictionaryLoader;
    private final DictionaryLoader tsvDictionaryLoader;
    private final DictionaryLoader jsonDictionaryLoader;
    private final Tokenizer greedyTokenizer;
    private final Tokenizer optimalTokenizer;
    private final RomajiConverter romajiConverter;

    private Map<String, List<String>> dictionary = new HashMap<>();

    public PhysicalNameGenerator(
            @Qualifier("csvDictionaryLoader") DictionaryLoader csvDictionaryLoader,
            @Qualifier("tsvDictionaryLoader") DictionaryLoader tsvDictionaryLoader,
            @Qualifier("jsonDictionaryLoader") DictionaryLoader jsonDictionaryLoader,
            @Qualifier("greedyTokenizer") Tokenizer greedyTokenizer,
            @Qualifier("optimalTokenizer") Tokenizer optimalTokenizer,
            RomajiConverter romajiConverter) {
        this.csvDictionaryLoader = csvDictionaryLoader;
        this.tsvDictionaryLoader = tsvDictionaryLoader;
        this.jsonDictionaryLoader = jsonDictionaryLoader;
        this.greedyTokenizer = greedyTokenizer;
        this.optimalTokenizer = optimalTokenizer;
        this.romajiConverter = romajiConverter;
    }

    /**
     * 辞書データを文字列から設定する
     *
     * @param format 辞書データの形式
     * @param data   辞書データ
     * @throws IOException 辞書の読み込みに失敗した場合
     */
    public void loadDictionary(DictionaryFormat format, String data) throws IOException {
        DictionaryLoader loader = getDictionaryLoader(format);
        this.dictionary = loader.load(data);
    }

    /**
     * 辞書データをリソースから設定する
     *
     * @param format   辞書データの形式
     * @param resource 辞書リソース
     * @param charset  文字エンコーディング
     * @throws IOException 辞書の読み込みに失敗した場合
     */
    public void loadDictionary(DictionaryFormat format, Resource resource, Charset charset) throws IOException {
        String data = resource.getContentAsString(charset);
        loadDictionary(format, data);
    }

    /**
     * 辞書データをリソースから設定する（UTF-8）
     *
     * @param format   辞書データの形式
     * @param resource 辞書リソース
     * @throws IOException 辞書の読み込みに失敗した場合
     */
    public void loadDictionary(DictionaryFormat format, Resource resource) throws IOException {
        loadDictionary(format, resource, StandardCharsets.UTF_8);
    }

    /**
     * 形式に応じた辞書ローダーを取得する
     *
     * @param format 辞書データの形式
     * @return 対応する辞書ローダー
     * @throws IllegalArgumentException サポートされていない形式の場合
     */
    private DictionaryLoader getDictionaryLoader(DictionaryFormat format) {
        return switch (format) {
            case CSV -> csvDictionaryLoader;
            case TSV -> tsvDictionaryLoader;
            case JSON -> jsonDictionaryLoader;
        };
    }

    /**
     * 指定されたトークナイザーで論理名をトークン化する
     *
     * @param type        トークナイザーの種類
     * @param logicalName 論理名（日本語）
     * @return トークンのリスト
     */
    public List<Token> tokenize(TokenizerType type, String logicalName) {
        Tokenizer tokenizer = getTokenizer(type);
        return tokenizer.tokenize(dictionary, logicalName);
    }

    /**
     * 種類に応じたトークナイザーを取得する
     *
     * @param type トークナイザーの種類
     * @return 対応するトークナイザー
     * @throws IllegalArgumentException サポートされていない種類の場合
     */
    private Tokenizer getTokenizer(TokenizerType type) {
        return switch (type) {
            case GREEDY -> greedyTokenizer;
            case OPTIMAL -> optimalTokenizer;
        };
    }

    /**
     * 物理名を生成する
     *
     * @param tokenizerType    トークナイザーの種類
     * @param namingConvention 命名規則
     * @param logicalName      元の日本語名
     * @return 物理名生成結果
     */
    public PhysicalNameResult generatePhysicalName(TokenizerType tokenizerType, NamingConvention namingConvention, String logicalName) {
        List<Token> tokens = tokenize(tokenizerType, logicalName);

        // 全トークンの物理名要素を収集
        List<String> allPhysicalElements = tokens.stream()
                .flatMap(token -> getPhysicalElements(token).stream())
                .toList();

        String physicalName = formatPhysicalName(allPhysicalElements, namingConvention);

        List<String> tokenMappings = tokens.stream()
                .map(this::formatTokenMapping)
                .toList();

        return new PhysicalNameResult(logicalName, physicalName, tokenMappings);
    }

    /**
     * トークンから物理名要素を取得する
     */
    private List<String> getPhysicalElements(Token token) {
        if (token.isUnknown() || token.physicalNames().isEmpty()) {
            return splitAndRomanizeUnknownWord(token.word());
        }
        return token.physicalNames();
    }

    /**
     * 未知語を分割してローマ字化する
     */
    private List<String> splitAndRomanizeUnknownWord(String word) {
        return romajiConverter.convertToRomaji(word);
    }

    /**
     * 物理名要素リストを指定された命名規則でフォーマットする
     */
    private String formatPhysicalName(List<String> elements, NamingConvention convention) {
        return switch (convention) {
            case CAMEL_CASE -> formatCamelCase(elements, false);
            case PASCAL_CASE -> formatCamelCase(elements, true);
            case SNAKE_CASE -> formatSnakeCase(elements, false);
            case SNAKE_CASE_UPPER -> formatSnakeCase(elements, true);
            case KEBAB_CASE -> formatKebabCase(elements, false);
            case KEBAB_CASE_UPPER -> formatKebabCase(elements, true);
        };
    }

    /**
     * camelCase/PascalCase形式でフォーマット
     */
    private String formatCamelCase(List<String> elements, boolean pascalCase) {
        if (elements.isEmpty()) return "";

        StringBuilder result = new StringBuilder();
        for (int i = 0; i < elements.size(); i++) {
            String element = elements.get(i);
            if (i == 0 && !pascalCase) {
                result.append(element.toLowerCase());
            } else {
                result.append(capitalize(element.toLowerCase()));
            }
        }
        return result.toString();
    }

    /**
     * snake_case形式でフォーマット
     */
    private String formatSnakeCase(List<String> elements, boolean upper) {
        String joined = String.join("_", elements);
        return upper ? joined.toUpperCase() : joined.toLowerCase();
    }

    /**
     * kebab-case形式でフォーマット
     */
    private String formatKebabCase(List<String> elements, boolean upper) {
        String joined = String.join("-", elements);
        return upper ? joined.toUpperCase() : joined.toLowerCase();
    }

    /**
     * 文字列の最初の文字を大文字にする
     */
    private String capitalize(String str) {
        if (str == null || str.isEmpty()) return str;
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    /**
     * トークンマッピングを文字列形式でフォーマット
     */
    private String formatTokenMapping(Token token) {
        if (token.isUnknown()) {
            // 既に変換済みの物理名要素を取得
            List<String> physicalElements = getPhysicalElements(token);
            String romajiResult = String.join(" ", physicalElements);
            return token.word() + "=>(romaji: " + romajiResult + ")";
        }
        String physicalNamesStr = String.join(", ", token.physicalNames());
        return token.word() + "=>" + physicalNamesStr;
    }

    /**
     * 設定されている辞書のサイズを取得する
     *
     * @return 辞書のエントリ数
     */
    public int getDictionarySize() {
        return dictionary.size();
    }

    /**
     * 辞書が設定されているかどうかを確認する
     *
     * @return 辞書が設定されている場合true
     */
    public boolean hasDictionary() {
        return !dictionary.isEmpty();
    }
}
