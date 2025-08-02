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

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * YAML形式の辞書データを読み込むローダー
 * フォーマット:
 * 論理名1:
 *   - 物理名1
 *   - 物理名2
 * 論理名2:
 *   - 物理名3
 */
@Component("yamlDictionaryLoader")
public class YamlDictionaryLoader implements DictionaryLoader {

    private final ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());

    @Override
    public Map<String, List<String>> load(String source) throws IOException {
        if (source == null || source.trim().isEmpty()) {
            return new HashMap<>();
        }

        try {
            // YAMLは単一値と配列の混在をサポートするため、Objectとして読み取り
            TypeReference<Map<String, Object>> typeRef = new TypeReference<>() {
            };
            Map<String, Object> rawMap = yamlMapper.readValue(source, typeRef);

            // 空のキーや値をフィルタリング
            Map<String, List<String>> dictionary = new HashMap<>();
            for (Map.Entry<String, Object> entry : rawMap.entrySet()) {
                String logicalName = entry.getKey();
                Object value = entry.getValue();

                if (logicalName != null && !logicalName.trim().isEmpty() && value != null) {
                    List<String> physicalNames = convertToStringList(value);

                    // 空でない物理名のみをフィルタリング
                    List<String> filteredPhysicalNames = physicalNames.stream()
                            .filter(name -> name != null && !name.trim().isEmpty())
                            .map(String::trim)
                            .toList();

                    if (!filteredPhysicalNames.isEmpty()) {
                        dictionary.put(logicalName.trim(), filteredPhysicalNames);
                    }
                }
            }

            return dictionary;
        } catch (Exception e) {
            throw new IOException("Failed to parse YAML dictionary: " + e.getMessage(), e);
        }
    }

    /**
     * YAMLの値を文字列リストに変換する
     * 単一値の場合は1要素のリストに、配列の場合はそのまま文字列リストに変換
     */
    @SuppressWarnings("unchecked")
    private List<String> convertToStringList(Object value) {
        if (value instanceof String) {
            return List.of((String) value);
        } else if (value instanceof List<?>) {
            return ((List<?>) value).stream()
                    .map(Object::toString)
                    .toList();
        } else {
            return List.of(value.toString());
        }
    }
}