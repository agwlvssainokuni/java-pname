/*
 * Copyright 2017,2025 agwlvssainokuni
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

package cherry.pname.dict;

import com.google.common.collect.Maps;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.Reader;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Component
public class DictLoaderImpl implements DictLoader {

    @Override
    public Map<String, List<String>> load(
            Reader r,
            boolean withHeader,
            String delim,
            boolean tsv
    ) throws IOException {

        Map<String, List<String>> dict = Maps.newHashMap();
        try (var parser = CSVParser.parse(r, tsv ? CSVFormat.TDF : CSVFormat.EXCEL)) {
            var skip = withHeader ? true : false;
            for (var record : parser) {

                if (skip) {
                    skip = false;
                    continue;
                }

                if (record.size() < 2) {
                    continue;
                }

                dict.put(
                        record.get(0),
                        Arrays.stream(record.get(1).split(delim))
                                .filter(StringUtils::isNotBlank)
                                .toList()
                );
            }
        }

        return dict;
    }

}
