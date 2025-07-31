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
import cherry.pname.main.tokenize.Token;
import cherry.pname.main.tokenize.Tokenizer;
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

    private Map<String, List<String>> dictionary = new HashMap<>();

    public PhysicalNameGenerator(
            DictionaryLoader csvDictionaryLoader,
            DictionaryLoader tsvDictionaryLoader,
            DictionaryLoader jsonDictionaryLoader,
            Tokenizer greedyTokenizer,
            Tokenizer optimalTokenizer) {
        this.csvDictionaryLoader = csvDictionaryLoader;
        this.tsvDictionaryLoader = tsvDictionaryLoader;
        this.jsonDictionaryLoader = jsonDictionaryLoader;
        this.greedyTokenizer = greedyTokenizer;
        this.optimalTokenizer = optimalTokenizer;
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