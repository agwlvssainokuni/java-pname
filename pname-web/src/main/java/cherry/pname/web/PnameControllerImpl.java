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

package cherry.pname.web;

import cherry.pname.dict.DictLoader;
import cherry.pname.processor.PnameType;
import cherry.pname.processor.ProcessorBuilder;
import cherry.pname.processor.ResultConsumer;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.List;
import java.util.Map;

@RestController
public class PnameControllerImpl implements PnameController {

    private final WebConfig webConfig;
    private final DictLoader dictLoader;
    private final ProcessorBuilder processorBuilder;
    private Map<String, List<String>> dictMap;

    public PnameControllerImpl(
            WebConfig webConfig,
            DictLoader dictLoader,
            ProcessorBuilder processorBuilder
    ) throws IOException {
        this.webConfig = webConfig;
        this.dictLoader = dictLoader;
        this.processorBuilder = processorBuilder;
        dictMap = dictLoader.load(
                webConfig.getDict(),
                webConfig.getCharset(),
                false,
                webConfig.getDelim(),
                webConfig.getDict().getFilename().endsWith(".tsv")
        );
    }

    @Override
    public List<Result> generate(String ln, PnameType type) {
        var processor = processorBuilder.build(dictMap, type);
        try (var reader = new StringReader(ln);
             var parser = CSVParser.parse(reader, CSVFormat.TDF)) {
            return parser.stream()
                    .filter(rec -> rec.size() > 0)
                    .map(rec -> rec.get(0))
                    .map(processor::process)
                    .map(pr -> new Result(pr.lname(), pr.pname(), pr.desc()))
                    .toList();
        } catch (IOException ex) {
            throw new IllegalStateException(ex);
        }
    }

    @Override
    public String generateTsv(String ln, PnameType type) {
        var processor = processorBuilder.build(dictMap, type);
        try (var reader = new StringReader(ln);
             var parser = CSVParser.parse(reader, CSVFormat.TDF);
             var writer = new StringWriter();
             var printer = new CSVPrinter(writer, CSVFormat.TDF)) {
            ResultConsumer consumer = webConfig.isDesc()
                    ? pr -> printer.printRecord(pr.lname(), pr.pname(), pr.desc())
                    : pr -> printer.printRecord(pr.lname(), pr.pname());
            parser.stream()
                    .filter(rec -> rec.size() > 0)
                    .map(rec -> rec.get(0))
                    .map(processor::process)
                    .forEach(consumer);
            printer.flush();
            return writer.toString();
        } catch (IOException ex) {
            throw new IllegalStateException(ex);
        }
    }

    @Override
    public int uploadDictText(String dicttext) throws IOException {
        try (var reader = new StringReader(dicttext)) {
            dictMap = dictLoader.load(
                    reader,
                    false,
                    webConfig.getDelim(),
                    webConfig.getDict().getFilename().endsWith(".tsv")
            );
        }
        return dictMap.size();
    }

    @Override
    public int reloadDict() throws IOException {
        dictMap = dictLoader.load(
                webConfig.getDict(),
                webConfig.getCharset(),
                false,
                webConfig.getDelim(),
                webConfig.getDict().getFilename().endsWith(".tsv")
        );
        return dictMap.size();
    }

}
