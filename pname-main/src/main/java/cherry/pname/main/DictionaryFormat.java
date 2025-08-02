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

/**
 * 辞書データの形式を表すenum
 */
public enum DictionaryFormat {
    /**
     * CSV形式（カンマ区切り）
     * フォーマット: 論理名,物理名1 物理名2 物理名3...
     */
    CSV,

    /**
     * TSV形式（タブ区切り）
     * フォーマット: 論理名\t物理名1 物理名2 物理名3...
     */
    TSV,

    /**
     * JSON形式
     * フォーマット: {"論理名1": ["物理名1", "物理名2"], "論理名2": ["物理名3"]}
     */
    JSON,

    /**
     * YAML形式
     * フォーマット:
     * 論理名1:
     *   - 物理名1
     *   - 物理名2
     * 論理名2:
     *   - 物理名3
     */
    YAML
}
