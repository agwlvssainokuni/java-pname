/*
 * Copyright 2017 agwlvssainokuni
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

package cherry.pname.processor;

import static java.text.MessageFormat.format;

import java.io.IOException;
import java.io.Reader;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Component;

import cherry.pname.tokenizer.Token;
import cherry.pname.tokenizer.Tokenizer;

import com.google.common.collect.Lists;

@Component
public class ProcessorImpl implements Processor {

	@Override
	public List<Result> process(Tokenizer tokenizer, Function<List<Token>, String> pnFunc, Reader r, boolean tsv) {
		try (CSVParser parser = CSVParser.parse(r, tsv ? CSVFormat.TDF : CSVFormat.EXCEL)) {

			List<Result> list = Lists.newArrayList();
			for (CSVRecord record : parser) {

				if (record.size() <= 0) {
					continue;
				}

				String lname = record.get(0);
				List<Token> token = tokenizer.tokenize(lname);
				String pname = pnFunc.apply(token);
				List<String> desc = token.stream().map(this::getDesc).collect(Collectors.toList());

				list.add(new Result(lname, pname, desc));
			}

			return list;

		} catch (IOException ex) {
			throw new IllegalStateException(ex);
		}
	}

	private String getDesc(Token token) {
		if (token.isOk()) {
			return format("{0}=>{1}", token.getWord(), token.getName());
		} else {
			return format("{0}=*", token.getWord(), token.getName());
		}
	}

}
