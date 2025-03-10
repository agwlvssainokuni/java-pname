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

import org.springframework.core.io.Resource;

import java.io.*;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;

public interface DictLoader {

    Map<String, List<String>> load(
            Reader r,
            boolean withHeader,
            String delim,
            boolean tsv
    ) throws IOException;

    default Map<String, List<String>> load(
            InputStream in,
            Charset charset,
            boolean withHeader,
            String delim,
            boolean tsv
    ) throws IOException {
        try (var r = new InputStreamReader(in, charset)) {
            return load(r, withHeader, delim, tsv);
        }
    }

    default Map<String, List<String>> load(
            File file,
            Charset charset,
            boolean withHeader,
            String delim,
            boolean tsv
    ) throws IOException {
        try (var fin = new FileInputStream(file)) {
            return load(fin, charset, withHeader, delim, tsv);
        }
    }

    default Map<String, List<String>> load(
            Resource resrc,
            Charset charset,
            boolean withHeader,
            String delim,
            boolean tsv
    ) throws IOException {
        try (var in = resrc.getInputStream()) {
            return load(in, charset, withHeader, delim, tsv);
        }
    }

}
