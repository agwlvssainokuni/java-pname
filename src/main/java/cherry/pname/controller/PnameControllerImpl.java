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

package cherry.pname.controller;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.List;
import java.util.Map;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import cherry.pname.dict.DictLoader;
import cherry.pname.processor.PnameType;
import cherry.pname.processor.Processor;
import cherry.pname.processor.ProcessorBuilder;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

@RestController
public class PnameControllerImpl implements PnameController {

	@Autowired
	private DictLoader loader;

	@Autowired
	private ProcessorBuilder builder;

	private Map<String, List<String>> dictMap = Maps.newHashMap();

	@Override
	public boolean register(String dict, String delim) {
		try (Reader r = new StringReader(dict)) {
			dictMap = loader.load(r, false, StringUtils.isEmpty(delim) ? " " : delim, true);
			return true;
		} catch (IOException ex) {
			throw new IllegalArgumentException(ex);
		}
	}

	@Override
	public List<Result> generate(String ln, String pnameType) {
		Processor processor = builder.build(dictMap, getPnameType(pnameType));
		List<Result> list = Lists.newArrayList();
		try (StringReader reader = new StringReader(ln); CSVParser parser = CSVParser.parse(reader, CSVFormat.TDF)) {
			for (CSVRecord record : parser) {
				if (record.size() <= 0) {
					continue;
				}
				Processor.Result r = processor.process(record.get(0));
				list.add(new Result(r.getLname(), r.getPname(), r.getDesc()));
			}
		} catch (IOException ex) {
			throw new IllegalStateException(ex);
		}
		return list;
	}

	private PnameType getPnameType(String pnameType) {
		if (StringUtils.isBlank(pnameType)) {
			return null;
		}
		return PnameType.valueOf(pnameType);
	}

}
