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

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RestController;

import cherry.pname.dict.DictLoader;
import cherry.pname.processor.PnameType;
import cherry.pname.processor.Processor;
import cherry.pname.processor.ProcessorBuilder;

@RestController
public class PnameControllerImpl implements PnameController, InitializingBean {

	@Value("${charset}")
	private Charset charset;

	@Value("${dict}")
	private File dict;

	@Value("${delim}")
	private String delim;

	@Autowired
	private DictLoader dictLoader;

	@Autowired
	private ProcessorBuilder processorBuilder;

	private Map<String, List<String>> dictMap;

	@Override
	public void afterPropertiesSet() throws IOException {
		dictMap = dictLoader.load(dict, charset, false, delim, dict.getName().endsWith(".tsv"));
	}

	@Override
	public List<Result> generate(String ln, PnameType type) {
		Processor processor = processorBuilder.build(dictMap, type);
		try (StringReader reader = new StringReader(ln); CSVParser parser = CSVParser.parse(reader, CSVFormat.TDF)) {
			return StreamSupport.stream(parser.spliterator(), false).filter(rec -> rec.size() > 0)
					.map(rec -> rec.get(0)).map(processor::process)
					.map(pr -> new Result(pr.getLname(), pr.getPname(), pr.getDesc())).collect(Collectors.toList());
		} catch (IOException ex) {
			throw new IllegalStateException(ex);
		}
	}

	@Override
	public String generateTsv(String ln, PnameType type) {
		Processor processor = processorBuilder.build(dictMap, type);
		try (StringReader reader = new StringReader(ln);
				CSVParser parser = CSVParser.parse(reader, CSVFormat.TDF);
				StringWriter writer = new StringWriter();
				CSVPrinter printer = new CSVPrinter(writer, CSVFormat.TDF)) {
			StreamSupport.stream(parser.spliterator(), false).filter(rec -> rec.size() > 0).map(rec -> rec.get(0))
					.map(processor::process).forEach(pr -> {
						try {
							printer.printRecord(pr.getLname(), pr.getPname(), pr.getDesc());
						} catch (IOException ex) {
							throw new IllegalStateException(ex);
						}
					});
			printer.flush();
			return writer.toString();
		} catch (IOException ex) {
			throw new IllegalStateException(ex);
		}
	}

}