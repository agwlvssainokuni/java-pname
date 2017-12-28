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

package cherry.pname.cmd;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.StreamSupport;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.ExitCodeGenerator;
import org.springframework.stereotype.Component;

import cherry.pname.dict.DictLoader;
import cherry.pname.processor.PnameType;
import cherry.pname.processor.Processor;
import cherry.pname.processor.ProcessorBuilder;

@Component
public class CmdHandler implements ApplicationRunner, ExitCodeGenerator, InitializingBean {

	@Value("${charset}")
	private Charset charset;

	@Value("${dict}")
	private File dict;

	@Value("${delim}")
	private String delim;

	@Value("${output}")
	private Optional<File> output;

	@Value("${tsvout}")
	private boolean tsvout;

	@Value("${type}")
	private PnameType type;

	@Autowired
	private DictLoader dictLoader;

	@Autowired
	private ProcessorBuilder processorBuilder;

	private Map<String, List<String>> dictMap;

	private Integer exitCode = null;

	@Override
	public void afterPropertiesSet() throws IOException {
		dictMap = dictLoader.load(dict, charset, false, delim, dict.getName().endsWith(".tsv"));
	}

	@Override
	public void run(ApplicationArguments args) {

		Processor processor = processorBuilder.build(dictMap, type);
		try (OutputStream out = openOutputStream();
				Writer writer = new OutputStreamWriter(out, charset);
				CSVPrinter printer = new CSVPrinter(writer, tsvout ? CSVFormat.TDF : CSVFormat.EXCEL)) {
			if (args.getNonOptionArgs().isEmpty()) {
				generate(System.in, processor, printer);
			} else {
				for (String arg : args.getNonOptionArgs()) {
					try (InputStream in = new FileInputStream(arg)) {
						generate(in, processor, printer);
					}
				}
			}
			setExitCode(0);
		} catch (FileNotFoundException ex) {
			setExitCode(1);
			throw new IllegalArgumentException(ex);
		} catch (IOException ex) {
			setExitCode(-1);
			throw new IllegalStateException(ex);
		} catch (IllegalStateException ex) {
			setExitCode(-1);
			throw ex;
		}
	}

	private synchronized void setExitCode(int code) {
		exitCode = code;
		notifyAll();
	}

	@Override
	public synchronized int getExitCode() {
		while (true) {
			if (exitCode != null) {
				return exitCode.intValue();
			}
			try {
				wait();
			} catch (InterruptedException ex) {
				// NOTHING TO DO
			}
		}
	}

	private OutputStream openOutputStream() throws FileNotFoundException {
		if (output.isPresent()) {
			return new FileOutputStream(output.get());
		} else {
			return System.out;
		}
	}

	private void generate(InputStream in, Processor processor, CSVPrinter printer) throws IOException {
		try (Reader reader = new InputStreamReader(in, charset);
				CSVParser parser = CSVParser.parse(reader, CSVFormat.TDF)) {
			StreamSupport.stream(parser.spliterator(), false).filter(rec -> rec.size() > 0).map(rec -> rec.get(0))
					.map(processor::process).forEach(pr -> {
						try {
							printer.printRecord(pr.getLname(), pr.getPname(), pr.getDesc());
						} catch (IOException ex) {
							throw new IllegalStateException(ex);
						}
					});
		}
	}

}
