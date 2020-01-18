/*
 * Copyright 2017,2020 agwlvssainokuni
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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;

import org.springframework.core.io.Resource;

public interface DictLoader {

	Map<String, List<String>> load(Reader r, boolean withHeader, String delim, boolean tsv) throws IOException;

	default public Map<String, List<String>> load(InputStream in, Charset charset, boolean withHeader, String delim,
			boolean tsv) throws IOException {
		try (InputStreamReader r = new InputStreamReader(in, charset)) {
			return load(r, withHeader, delim, tsv);
		}
	}

	default public Map<String, List<String>> load(File file, Charset charset, boolean withHeader, String delim,
			boolean tsv) throws IOException {
		try (FileInputStream fin = new FileInputStream(file)) {
			return load(fin, charset, withHeader, delim, tsv);
		}
	}

	default public Map<String, List<String>> load(Resource resrc, Charset charset, boolean withHeader, String delim,
			boolean tsv) throws IOException {
		try (InputStream in = resrc.getInputStream()) {
			return load(in, charset, withHeader, delim, tsv);
		}
	}

}
