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

package cherry.pname.web;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import cherry.pname.dict.DictLoader;
import cherry.pname.processor.PnameType;
import cherry.pname.processor.Processor;
import cherry.pname.processor.ProcessorBuilder;

import com.google.common.collect.Maps;

@RestController
public class PnameControllerImpl implements PnameController {

	@Autowired
	private DictLoader dictLoader;

	@Autowired
	private ProcessorBuilder processorBuilder;

	private Map<String, List<String>> dictMap = Maps.newHashMap();

	@Override
	public boolean register(String dict, String delim) {
		try (Reader r = new StringReader(dict)) {
			dictMap = dictLoader.load(r, false, StringUtils.isEmpty(delim) ? " " : delim, true);
			return true;
		} catch (IOException ex) {
			throw new IllegalArgumentException(ex);
		}
	}

	@Override
	public List<Result> generate(String ln, String pnameType) {
		Processor processor = processorBuilder.build(dictMap, getPnameType(pnameType));
		try (StringReader reader = new StringReader(ln); CSVParser parser = CSVParser.parse(reader, CSVFormat.TDF)) {
			return StreamSupport.stream(parser.spliterator(), false).filter(rec -> rec.size() > 0)
					.map(rec -> rec.get(0)).map(processor::process)
					.map(pr -> new Result(pr.getLname(), pr.getPname(), pr.getDesc())).collect(Collectors.toList());
		} catch (IOException ex) {
			throw new IllegalStateException(ex);
		}
	}

	private PnameType getPnameType(String pnameType) {
		if (StringUtils.isBlank(pnameType)) {
			return null;
		}
		return PnameType.valueOf(pnameType);
	}

}
