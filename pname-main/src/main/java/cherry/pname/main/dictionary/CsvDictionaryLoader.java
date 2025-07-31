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

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * CSV形式の辞書データを読み込むローダー
 * フォーマット: 論理名,物理名1 物理名2 物理名3...
 */
@Component("csvDictionaryLoader")
public class CsvDictionaryLoader implements DictionaryLoader {

    private static final CSVFormat CSV_FORMAT = CSVFormat.DEFAULT;

    @Override
    public Map<String, List<String>> load(String source) throws IOException {
        Map<String, List<String>> dictionary = new HashMap<>();

        try (StringReader reader = new StringReader(source);
             CSVParser parser = CSV_FORMAT.parse(reader)) {

            for (CSVRecord record : parser) {
                if (record.size() < 2) {
                    continue; // 最低2列（論理名、物理名）が必要
                }

                String logicalName = record.get(0).trim();
                String physicalNames = record.get(1).trim();

                if (logicalName.isEmpty() || physicalNames.isEmpty()) {
                    continue; // 空の場合はスキップ
                }

                // 物理名を空白で分割
                List<String> physicalNameList = List.of(physicalNames.split("\\s+"));
                dictionary.put(logicalName, physicalNameList);
            }
        }

        return dictionary;
    }
}