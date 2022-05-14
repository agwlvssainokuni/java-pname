/*
 * Copyright 2022 agwlvssainokuni
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

package cherry.pname.batch;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.Optional;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemStreamException;
import org.springframework.batch.item.ItemStreamReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.file.ResourceAwareItemReaderItemStream;
import org.springframework.batch.item.file.builder.MultiResourceItemReaderBuilder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.ApplicationArguments;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;

import cherry.pname.dict.DictLoader;
import cherry.pname.processor.Processor.Result;
import cherry.pname.processor.ProcessorBuilder;
import cherry.pname.processor.ResultConsumer;

@Configuration
public class PnameBatch {

    @Bean
    public Job pnameBatchJob(
            JobBuilderFactory jobBuilderFactory,
            @Qualifier("pnameBatchStep") Step pnameBatchStep) {
        return jobBuilderFactory.get("PnameBatchJob")
                .flow(pnameBatchStep)
                .end()
                .build();
    }

    @Bean
    public Step pnameBatchStep(
            StepBuilderFactory stepBuilderFactory,
            BatchConfig batchConfig,
            @Qualifier("pnameReader") ItemReader<String> pnameReader,
            @Qualifier("pnameProcessor") ItemProcessor<String, Result> pnameProcessor,
            @Qualifier("pnameWriter") ItemWriter<Result> pnameWriter) {
        return stepBuilderFactory.get("PnameBatchStep")
                .<String, Result>chunk(1)
                .reader(pnameReader)
                .processor(pnameProcessor)
                .writer(pnameWriter)
                .build();
    }

    @Bean
    public ItemStreamReader<String> pnameReader(
            BatchConfig batchConfig,
            ApplicationArguments args) {
        var itemreader = new CsvResourceItemReader(batchConfig.getCharset());
        if (args.getNonOptionArgs().isEmpty()) {
            itemreader.setResource(new InputStreamResource(System.in));
            return itemreader;
        } else {
            return new MultiResourceItemReaderBuilder<String>()
                    .delegate(itemreader)
                    .resources(args.getNonOptionArgs().stream().map(File::new).filter(File::isFile)
                            .map(FileSystemResource::new).toList().toArray(new Resource[] {}))
                    .saveState(false)
                    .build();
        }
    }

    @Bean
    public ItemProcessor<String, Result> pnameProcessor(
            BatchConfig batchConfig,
            DictLoader dictLoader,
            ProcessorBuilder processorBuilder) throws IOException {
        var dictMap = dictLoader.load(batchConfig.getDict(), batchConfig.getCharset(), false, batchConfig.getDelim(),
                batchConfig.getDict().getFilename().endsWith(".tsv"));
        var processor = processorBuilder.build(dictMap, batchConfig.getType());
        return processor::process;
    }

    @Bean
    public ItemWriter<Result> pnameWriter(
            BatchConfig batchConfig,
            ApplicationArguments args) {
        var output = Optional.ofNullable(args.getOptionValues("output"))
                .flatMap(list -> list.stream().filter(StringUtils::isNotBlank).findFirst());
        var csvformat = batchConfig.isTsvout() ? CSVFormat.TDF : CSVFormat.EXCEL;
        if (output.isPresent()) {
            return items -> {
                try (var out = new FileOutputStream(output.get(), true);
                        var writer = new OutputStreamWriter(out, batchConfig.getCharset());
                        var printer = new CSVPrinter(writer, csvformat)) {
                    ResultConsumer consumer = batchConfig.isDesc()
                            ? pr -> printer.printRecord(pr.getLname(), pr.getPname(), pr.getDesc())
                            : pr -> printer.printRecord(pr.getLname(), pr.getPname());
                    items.forEach(consumer);
                }
            };
        } else {
            return items -> {
                var out = System.out;
                var writer = new OutputStreamWriter(out, batchConfig.getCharset());
                @SuppressWarnings("resource")
                var printer = new CSVPrinter(writer, csvformat);
                ResultConsumer consumer = batchConfig.isDesc()
                        ? pr -> printer.printRecord(pr.getLname(), pr.getPname(), pr.getDesc())
                        : pr -> printer.printRecord(pr.getLname(), pr.getPname());
                items.forEach(consumer);
            };
        }
    }

    static class CsvResourceItemReader implements ResourceAwareItemReaderItemStream<String> {

        private final Charset charset;
        private Resource resource;
        private CSVParser parser;

        CsvResourceItemReader(Charset charset) {
            this.charset = charset;
            this.resource = null;
            this.parser = null;
        }

        @Override
        public void setResource(Resource resource) {
            this.resource = resource;
        }

        @Override
        public void open(ExecutionContext executionContext) throws ItemStreamException {
            try {
                var in = resource.getInputStream();
                var reader = new InputStreamReader(in, charset);
                parser = CSVParser.parse(reader, CSVFormat.TDF);
            } catch (IOException ex) {
                throw new ItemStreamException(ex);
            }
        }

        @Override
        public void update(ExecutionContext executionContext) {
            // 何もしない。
        }

        @Override
        public void close() throws ItemStreamException {
            try {
                parser.close();
            } catch (IOException ex) {
                throw new ItemStreamException(ex);
            }
        }

        @Override
        public String read() {
            return parser.iterator().hasNext() ? parser.iterator().next().get(0) : null;
        }
    }
}
