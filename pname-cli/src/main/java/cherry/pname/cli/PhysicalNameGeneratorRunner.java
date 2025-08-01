/*
 * Copyright 2025 agwlvssainokuni
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

package cherry.pname.cli;

import cherry.pname.main.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.ExitCodeGenerator;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

/**
 * 物理名生成CLIの実行クラス
 */
@Component
public class PhysicalNameGeneratorRunner implements ApplicationRunner, ExitCodeGenerator {

    private static final Logger log = LoggerFactory.getLogger(PhysicalNameGeneratorRunner.class);

    private final PhysicalNameGenerator generator;
    private int exitCode = 0;

    public PhysicalNameGeneratorRunner(PhysicalNameGenerator generator) {
        this.generator = generator;
    }

    @Override
    public void run(ApplicationArguments args) {
        try {
            if (args.containsOption("help") || args.getSourceArgs().length == 0) {
                printUsage();
                return;
            }

            processArguments(args);
        } catch (Exception e) {
            log.error("実行中にエラーが発生しました: {}", e.getMessage(), e);
            exitCode = 1;
        }
    }

    private void printUsage() {
        log.info("物理名生成ツール");
        log.info("Usage: java -jar pname-cli.jar [options] <logical-names...>");
        log.info("");
        log.info("Options:");
        log.info("  --help                    このヘルプメッセージを表示");
        log.info("  --dictionary=<file>       辞書ファイルを指定");
        log.info("  --format=<format>         辞書形式を指定 (CSV, TSV, JSON) [default: CSV]");
        log.info("  --tokenizer=<type>        トークナイザーを指定 (GREEDY, OPTIMAL) [default: OPTIMAL]");
        log.info("  --naming=<convention>     命名規則を指定 (CAMEL_CASE, PASCAL_CASE, SNAKE_CASE, SNAKE_CASE_UPPER, KEBAB_CASE, KEBAB_CASE_UPPER) [default: CAMEL_CASE]");
        log.info("  --verbose                 詳細な出力を表示");
        log.info("  --quiet                   最小限の出力のみ表示");
        log.info("");
        log.info("Examples:");
        log.info("  java -jar pname-cli.jar --dictionary=dict.csv 顧客管理システム");
        log.info("  java -jar pname-cli.jar --format=JSON --naming=SNAKE_CASE --dictionary=dict.json 注文明細");
    }

    private void processArguments(ApplicationArguments args) throws IOException {
        // 辞書ファイルの読み込み
        if (args.containsOption("dictionary")) {
            String dictionaryFile = args.getOptionValues("dictionary").get(0);
            DictionaryFormat format = parseDictionaryFormat(args.getOptionValues("format"));

            FileSystemResource resource = new FileSystemResource(dictionaryFile);
            if (!resource.exists()) {
                log.error("辞書ファイルが見つかりません: {}", dictionaryFile);
                exitCode = 1;
                return;
            }

            generator.loadDictionary(format, resource);
            if (!isQuiet(args)) {
                log.info("辞書を読み込みました: {} ({}エントリ)", dictionaryFile, generator.getDictionarySize());
            }
        } else {
            log.warn("辞書ファイルが指定されていません。未知語のみローマ字変換されます。");
        }

        TokenizerType tokenizerType = parseTokenizerType(args.getOptionValues("tokenizer"));
        NamingConvention namingConvention = parseNamingConvention(args.getOptionValues("naming"));

        // 論理名の処理
        List<String> logicalNames = args.getNonOptionArgs();
        if (logicalNames.isEmpty()) {
            log.error("変換対象の論理名が指定されていません");
            exitCode = 1;
            return;
        }

        boolean verbose = isVerbose(args);
        boolean quiet = isQuiet(args);

        for (String logicalName : logicalNames) {
            processLogicalName(logicalName, tokenizerType, namingConvention, verbose, quiet);
        }
    }

    private void processLogicalName(String logicalName, TokenizerType tokenizerType,
                                    NamingConvention namingConvention, boolean verbose, boolean quiet) {
        try {
            PhysicalNameResult result = generator.generatePhysicalName(tokenizerType, namingConvention, logicalName);

            if (quiet) {
                log.info(result.physicalName());
            } else {
                log.info("論理名: {}", result.logicalName());
                log.info("物理名: {}", result.physicalName());

                if (verbose) {
                    log.info("トークン分解:");
                    result.tokenMappings().forEach(mapping -> log.info("  {}", mapping));
                }
                log.info("");
            }
        } catch (Exception e) {
            log.error("論理名の変換に失敗しました: {} - {}", logicalName, e.getMessage());
            exitCode = 1;
        }
    }

    private DictionaryFormat parseDictionaryFormat(List<String> formatOptions) {
        if (formatOptions == null || formatOptions.isEmpty()) {
            return DictionaryFormat.CSV;
        }

        try {
            return DictionaryFormat.valueOf(formatOptions.get(0).toUpperCase());
        } catch (IllegalArgumentException e) {
            log.warn("不正な辞書形式です: {}. デフォルト(CSV)を使用します。", formatOptions.get(0));
            return DictionaryFormat.CSV;
        }
    }

    private TokenizerType parseTokenizerType(List<String> tokenizerOptions) {
        if (tokenizerOptions == null || tokenizerOptions.isEmpty()) {
            return TokenizerType.OPTIMAL;
        }

        try {
            return TokenizerType.valueOf(tokenizerOptions.get(0).toUpperCase());
        } catch (IllegalArgumentException e) {
            log.warn("不正なトークナイザーです: {}. デフォルト(OPTIMAL)を使用します。", tokenizerOptions.get(0));
            return TokenizerType.OPTIMAL;
        }
    }

    private NamingConvention parseNamingConvention(List<String> namingOptions) {
        if (namingOptions == null || namingOptions.isEmpty()) {
            return NamingConvention.CAMEL_CASE;
        }

        try {
            return NamingConvention.valueOf(namingOptions.get(0).toUpperCase());
        } catch (IllegalArgumentException e) {
            log.warn("不正な命名規則です: {}. デフォルト(CAMEL_CASE)を使用します。", namingOptions.get(0));
            return NamingConvention.CAMEL_CASE;
        }
    }

    private boolean isVerbose(ApplicationArguments args) {
        return args.containsOption("verbose");
    }

    private boolean isQuiet(ApplicationArguments args) {
        return args.containsOption("quiet");
    }

    @Override
    public int getExitCode() {
        return exitCode;
    }
}
