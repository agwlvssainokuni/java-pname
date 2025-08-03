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
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * 物理名生成CLIの実行クラス
 * <p>
 * このクラスは物理名生成ツールのコマンドラインインターフェースのコアロジックを実装します。
 * コマンドライン引数の処理、辞書の読み込み管理、および様々なオプションと出力形式での
 * 物理名生成を実行します。
 * </p>
 * 
 * <h3>サポートされる操作</h3>
 * <ul>
 *   <li><strong>単一名前変換</strong> - 引数として渡された個別の論理名を変換</li>
 *   <li><strong>バッチファイル処理</strong> - 入力ファイルから複数の名前を処理</li>
 *   <li><strong>辞書管理</strong> - 複数形式（CSV、TSV、JSON、YAML）での辞書読み込み</li>
 *   <li><strong>柔軟な出力</strong> - 詳細、通常、静寂の出力モードをサポート</li>
 * </ul>
 * 
 * <h3>コマンドラインオプション</h3>
 * <table border="1">
 *   <tr><th>オプション</th><th>説明</th><th>デフォルト</th></tr>
 *   <tr><td>--help</td><td>ヘルプメッセージを表示</td><td>-</td></tr>
 *   <tr><td>--dictionary=&lt;file&gt;</td><td>辞書ファイルパス</td><td>-</td></tr>
 *   <tr><td>--format=&lt;format&gt;</td><td>辞書形式（CSV、TSV、JSON、YAML）</td><td>CSV</td></tr>
 *   <tr><td>--tokenizer=&lt;type&gt;</td><td>トークナイザータイプ（GREEDY、OPTIMAL）</td><td>OPTIMAL</td></tr>
 *   <tr><td>--naming=&lt;convention&gt;</td><td>命名規則</td><td>LOWER_CAMEL</td></tr>
 *   <tr><td>--input=&lt;file&gt;</td><td>論理名を含む入力ファイル</td><td>-</td></tr>
 *   <tr><td>--output=&lt;file&gt;</td><td>結果用出力ファイル</td><td>-</td></tr>
 *   <tr><td>--enable-fallback</td><td>未知語のローマ字変換を有効化</td><td>false</td></tr>
 *   <tr><td>--verbose</td><td>詳細な変換情報を表示</td><td>false</td></tr>
 *   <tr><td>--quiet</td><td>物理名のみを表示</td><td>false</td></tr>
 * </table>
 * 
 * <h3>使用例</h3>
 * <pre>{@code
 * # 辞書を使用した基本変換
 * java -jar pname-cli.jar --dictionary=dict.csv 顧客管理システム
 * 
 * # ファイルI/Oでのバッチ処理
 * java -jar pname-cli.jar --dictionary=dict.csv --input=names.txt --output=results.txt
 * 
 * # 特定オプションでの詳細出力
 * java -jar pname-cli.jar --dictionary=dict.json --format=JSON --naming=LOWER_SNAKE --verbose 注文処理
 * }</pre>
 * 
 * <h3>エラーハンドリング</h3>
 * <p>
 * ランナーは適切な終了コードを使用した堅牢なエラーハンドリングを実装しています：
 * </p>
 * <ul>
 *   <li><strong>終了コード 0</strong> - 実行成功</li>
 *   <li><strong>終了コード 1</strong> - エラー発生（ファイル未発見、無効パラメータ、変換失敗等）</li>
 * </ul>
 * 
 * <h3>出力モード</h3>
 * <ul>
 *   <li><strong>通常モード</strong> - 論理名、物理名、基本情報を表示</li>
 *   <li><strong>詳細モード</strong> - 変換プロセスを示す詳細なトークンマッピングを含む</li>
 *   <li><strong>静寂モード</strong> - 生成された物理名のみを表示</li>
 * </ul>
 * 
 * @author Physical Name Generator Team
 * @version 1.0.0
 * @since 1.0.0
 * @see cherry.pname.main.PhysicalNameGenerator
 * @see org.springframework.boot.ApplicationRunner
 * @see org.springframework.boot.ExitCodeGenerator
 */
@Component
public class PhysicalNameGeneratorRunner implements ApplicationRunner, ExitCodeGenerator {

    private static final Logger log = LoggerFactory.getLogger(PhysicalNameGeneratorRunner.class);

    /**
     * コア物理名生成サービス。
     * SpringのDIコンテナによってインジェクションされます。
     */
    private final PhysicalNameGenerator generator;
    
    /**
     * オペレーティングシステムに返される終了コード。
     * 実行成功時は0、エラー時は1に設定されます。
     */
    private int exitCode = 0;

    /**
     * 指定されたジェネレータでPhysicalNameGeneratorRunnerを構築します。
     * 
     * @param generator 変換に使用する物理名生成サービス
     */
    public PhysicalNameGeneratorRunner(PhysicalNameGenerator generator) {
        this.generator = generator;
    }

    /**
     * アプリケーション起動後にSpring Bootによって呼び出されるメイン実行メソッド。
     * <p>
     * このメソッドはCLI実行フロー全体を調整します：
     * </p>
     * <ol>
     *   <li>ヘルプオプションまたは空の引数をチェック</li>
     *   <li>コマンドライン引数を処理・検証</li>
     *   <li>指定されている場合は辞書を読み込み</li>
     *   <li>名前変換を実行</li>
     *   <li>出力フォーマットとファイルI/Oを処理</li>
     * </ol>
     * 
     * <p>
     * 実行中の例外はログに記録され、終了コード1となります。
     * </p>
     * 
     * @param args Spring Bootによって解析されたコマンドライン引数
     * @see ApplicationArguments
     */
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

    /**
     * 包括的な使用方法情報と例をコンソールに出力します。
     * <p>
     * このメソッドは以下を表示します：
     * </p>
     * <ul>
     *   <li>アプリケーションタイトルと基本使用構文</li>
     *   <li>利用可能なコマンドラインオプションの完全なリストと説明</li>
     *   <li>オプションパラメータのデフォルト値</li>
     *   <li>一般的なシナリオの実用的な使用例</li>
     * </ul>
     */
    private void printUsage() {
        log.info("物理名生成ツール");
        log.info("Usage: java -jar pname-cli.jar [options] <logical-names...>");
        log.info("");
        log.info("Options:");
        log.info("  --help                    このヘルプメッセージを表示");
        log.info("  --dictionary=<file>       辞書ファイルを指定");
        log.info("  --format=<format>         辞書形式を指定 (CSV, TSV, JSON) [default: CSV]");
        log.info("  --tokenizer=<type>        トークナイザーを指定 (GREEDY, OPTIMAL) [default: OPTIMAL]");
        log.info("  --naming=<convention>     命名規則を指定 (LOWER_CAMEL, UPPER_CAMEL, CAMEL, PASCAL, LOWER_SNAKE, UPPER_SNAKE, LOWER_KEBAB, UPPER_KEBAB) [default: LOWER_CAMEL]");
        log.info("  --input=<file>            入力ファイルを指定（論理名リスト）");
        log.info("  --output=<file>           出力ファイルを指定");
        log.info("  --enable-fallback         未知語のローマ字変換を有効化");
        log.info("  --verbose                 詳細な出力を表示");
        log.info("  --quiet                   最小限の出力のみ表示");
        log.info("");
        log.info("Examples:");
        log.info("  java -jar pname-cli.jar --dictionary=dict.csv 顧客管理システム");
        log.info("  java -jar pname-cli.jar --format=JSON --naming=LOWER_SNAKE --dictionary=dict.json 注文明細");
        log.info("  java -jar pname-cli.jar --dictionary=dict.csv --input=input.txt --output=output.txt");
    }

    /**
     * すべてのコマンドライン引数を処理・検証します。
     * <p>
     * このメソッドは引数処理パイプライン全体を調整します：
     * </p>
     * <ol>
     *   <li><strong>辞書読み込み</strong> - 指定されている場合、辞書ファイルを読み込み・検証</li>
     *   <li><strong>パラメータ解析</strong> - トークナイザータイプ、命名規則、フラグを解析</li>
     *   <li><strong>実行モード選択</strong> - ファイルベースまたは直接引数処理を選択</li>
     * </ol>
     * 
     * <p>
     * {@code --input}オプションの有無に基づいて、個別論理名変換またはバッチファイル処理を処理します。
     * </p>
     * 
     * @param args オプションと非オプション引数を含む解析済みコマンドライン引数
     * @throws IOException ファイルI/O操作が失敗した場合（辞書読み込み、入出力ファイルアクセス）
     */
    private void processArguments(ApplicationArguments args) throws IOException {
        // 辞書ファイルの読み込み
        if (args.containsOption("dictionary")) {
            String dictionaryFile = args.getOptionValues("dictionary").getFirst();
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

        boolean enableFallback = parseEnableFallback(args);
        boolean verbose = isVerbose(args);
        boolean quiet = isQuiet(args);

        // ファイルベース処理またはコマンドライン引数処理
        if (args.containsOption("input")) {
            processInputFile(args, tokenizerType, namingConvention, enableFallback, verbose, quiet);
        } else {
            processCommandLineArguments(args, tokenizerType, namingConvention, enableFallback, verbose, quiet);
        }
    }

    /**
     * 単一の論理名を処理し、指定されたオプションに従って結果を出力します。
     * <p>
     * このメソッドはコア名前変換ロジックを処理し、選択された詳細レベルに基づいて
     * 出力をフォーマットします：
     * </p>
     * <ul>
     *   <li><strong>静寂モード</strong> - 物理名のみを出力</li>
     *   <li><strong>通常モード</strong> - 論理名と物理名を表示</li>
     *   <li><strong>詳細モード</strong> - 詳細なトークンマッピング情報を含む</li>
     * </ul>
     * 
     * <p>
     * 変換が失敗した場合、エラーメッセージがログに記録され、終了コードが1に設定されます。
     * </p>
     * 
     * @param logicalName 変換する日本語論理名
     * @param tokenizerType 使用するトークン化アルゴリズム（GREEDYまたはOPTIMAL）
     * @param namingConvention 物理名のターゲット命名規則
     * @param enableFallback 未知語のローマ字変換を有効にするかどうか
     * @param verbose 詳細なトークンマッピング情報を含むかどうか
     * @param quiet 物理名のみを出力するかどうか
     */
    private void processLogicalName(String logicalName, TokenizerType tokenizerType,
                                    NamingConvention namingConvention, boolean enableFallback, boolean verbose, boolean quiet) {
        try {
            PhysicalNameResult result = generator.generatePhysicalName(tokenizerType, namingConvention, logicalName, enableFallback);

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

    /**
     * 辞書形式オプションを解析・検証します。
     * <p>
     * サポートされる形式はCSV、TSV、JSON、YAMLです。無効な形式が指定された場合、
     * 警告がログに記録され、デフォルトとしてCSV形式が使用されます。
     * </p>
     * 
     * @param formatOptions コマンドラインからの形式オプション値のリスト
     * @return 解析されたDictionaryFormat、無効/未指定の場合はデフォルトとしてCSV
     */
    private DictionaryFormat parseDictionaryFormat(List<String> formatOptions) {
        if (formatOptions == null || formatOptions.isEmpty()) {
            return DictionaryFormat.CSV;
        }

        try {
            return DictionaryFormat.valueOf(formatOptions.getFirst().toUpperCase());
        } catch (IllegalArgumentException e) {
            log.warn("不正な辞書形式です: {}. デフォルト(CSV)を使用します。", formatOptions.getFirst());
            return DictionaryFormat.CSV;
        }
    }

    /**
     * トークナイザータイプオプションを解析・検証します。
     * <p>
     * サポートされるタイプはGREEDY（前方最長一致）とOPTIMAL（動的プログラミング）です。
     * 無効なタイプが指定された場合、警告がログに記録され、デフォルトとしてOPTIMALが使用されます。
     * </p>
     * 
     * @param tokenizerOptions コマンドラインからのトークナイザーオプション値のリスト
     * @return 解析されたTokenizerType、無効/未指定の場合はデフォルトとしてOPTIMAL
     */
    private TokenizerType parseTokenizerType(List<String> tokenizerOptions) {
        if (tokenizerOptions == null || tokenizerOptions.isEmpty()) {
            return TokenizerType.OPTIMAL;
        }

        try {
            return TokenizerType.valueOf(tokenizerOptions.getFirst().toUpperCase());
        } catch (IllegalArgumentException e) {
            log.warn("不正なトークナイザーです: {}. デフォルト(OPTIMAL)を使用します。", tokenizerOptions.getFirst());
            return TokenizerType.OPTIMAL;
        }
    }

    /**
     * 命名規則オプションを解析・検証します。
     * <p>
     * camelCase系、snake_case系、kebab-case系を含むすべての10種類の命名規則をサポートします。
     * 無効な規則が指定された場合、警告がログに記録され、デフォルトとしてLOWER_CAMELが使用されます。
     * </p>
     * 
     * @param namingOptions コマンドラインからの命名規則オプション値のリスト
     * @return 解析されたNamingConvention、無効/未指定の場合はデフォルトとしてLOWER_CAMEL
     */
    private NamingConvention parseNamingConvention(List<String> namingOptions) {
        if (namingOptions == null || namingOptions.isEmpty()) {
            return NamingConvention.LOWER_CAMEL;
        }

        try {
            return NamingConvention.valueOf(namingOptions.getFirst().toUpperCase());
        } catch (IllegalArgumentException e) {
            log.warn("不正な命名規則です: {}. デフォルト(LOWER_CAMEL)を使用します。", namingOptions.getFirst());
            return NamingConvention.LOWER_CAMEL;
        }
    }

    /**
     * 詳細出力モードが有効かどうかをチェックします。
     * 
     * @param args コマンドライン引数
     * @return --verboseオプションが存在する場合はtrue
     */
    private boolean isVerbose(ApplicationArguments args) {
        return args.containsOption("verbose");
    }

    /**
     * 静寂出力モードが有効かどうかをチェックします。
     * 
     * @param args コマンドライン引数
     * @return --quietオプションが存在する場合はtrue
     */
    private boolean isQuiet(ApplicationArguments args) {
        return args.containsOption("quiet");
    }

    /**
     * 未知語のローマ字フォールバック変換が有効かどうかをチェックします。
     * 
     * @param args コマンドライン引数
     * @return --enable-fallbackオプションが存在する場合はtrue
     */
    private boolean parseEnableFallback(ApplicationArguments args) {
        return args.containsOption("enable-fallback");
    }

    /**
     * 入力ファイルから論理名を処理し、バッチ変換を処理します。
     * <p>
     * このメソッドは包括的なファイルベース処理機能を提供します：
     * </p>
     * <ul>
     *   <li><strong>入力検証</strong> - ファイルの存在と読み込み可能性をチェック</li>
     *   <li><strong>バッチ処理</strong> - 一貫した設定で複数の名前を処理</li>
     *   <li><strong>出力フォーマット</strong> - コンソール出力または複数形式でのファイル出力をサポート</li>
     *   <li><strong>エラーハンドリング</strong> - 個別の名前が失敗しても処理を継続</li>
     * </ul>
     * 
     * <p>
     * 入力ファイルは1行に1つの論理名を含む必要があります。空行は自動的にスキップされます。
     * 出力形式は詳細設定と出力ファイルが指定されているかどうかに依存します。
     * </p>
     * 
     * @param args 入出力ファイルパスを含むコマンドライン引数
     * @param tokenizerType すべての変換に使用するトークン化アルゴリズム
     * @param namingConvention すべての変換に適用する命名規則
     * @param enableFallback 未知語のローマ字変換を有効にするかどうか
     * @param verbose 出力に詳細なトークンマッピング情報を含むかどうか
     * @param quiet 物理名のみを出力するかどうか
     * @throws IOException ファイルI/O操作が失敗した場合
     */
    private void processInputFile(ApplicationArguments args, TokenizerType tokenizerType,
                                  NamingConvention namingConvention, boolean enableFallback, boolean verbose, boolean quiet) throws IOException {
        String inputFile = args.getOptionValues("input").getFirst();
        Path inputPath = Paths.get(inputFile);

        if (!Files.exists(inputPath)) {
            log.error("入力ファイルが見つかりません: {}", inputFile);
            exitCode = 1;
            return;
        }

        List<String> logicalNames = Files.readAllLines(inputPath, StandardCharsets.UTF_8);

        if (!quiet) {
            log.info("入力ファイルから{}件の論理名を読み込みました: {}", logicalNames.size(), inputFile);
        }

        StringBuilder outputContent = new StringBuilder();

        for (String logicalName : logicalNames) {
            String trimmedName = logicalName.trim();
            if (trimmedName.isEmpty()) {
                continue; // 空行をスキップ
            }

            try {
                PhysicalNameResult result = generator.generatePhysicalName(tokenizerType, namingConvention, trimmedName, enableFallback);

                if (args.containsOption("output")) {
                    // ファイル出力用フォーマット
                    if (verbose) {
                        outputContent.append("論理名: ").append(result.logicalName()).append("\n");
                        outputContent.append("物理名: ").append(result.physicalName()).append("\n");
                        outputContent.append("トークン分解:\n");
                        result.tokenMappings().forEach(mapping ->
                                outputContent.append("  ").append(mapping).append("\n"));
                        outputContent.append("\n");
                    } else if (quiet) {
                        outputContent.append(result.physicalName()).append("\n");
                    } else {
                        outputContent.append(result.logicalName()).append(" -> ").append(result.physicalName()).append("\n");
                    }
                } else {
                    // コンソール出力
                    processLogicalName(trimmedName, tokenizerType, namingConvention, enableFallback, verbose, quiet);
                }
            } catch (Exception e) {
                log.error("論理名の変換に失敗しました: {} - {}", trimmedName, e.getMessage());
                exitCode = 1;
            }
        }

        // ファイル出力
        if (args.containsOption("output")) {
            String outputFile = args.getOptionValues("output").getFirst();
            Path outputPath = Paths.get(outputFile);

            Files.writeString(outputPath, outputContent.toString(), StandardCharsets.UTF_8);

            if (!quiet) {
                log.info("結果を出力ファイルに書き込みました: {}", outputFile);
            }
        }
    }

    /**
     * コマンドライン引数として直接提供された論理名を処理します。
     * <p>
     * このメソッドは論理名がコマンドラインの非オプション引数として渡される
     * シンプルなケースを処理します。各名前は指定された変換設定で個別に処理されます。
     * </p>
     * 
     * <p>
     * 引数として論理名が提供されていない場合、エラーがログに記録され、
     * 終了コードが1に設定されます。
     * </p>
     * 
     * @param args 非オプション引数として論理名を含むコマンドライン引数
     * @param tokenizerType すべての変換に使用するトークン化アルゴリズム
     * @param namingConvention すべての変換に適用する命名規則
     * @param enableFallback 未知語のローマ字変換を有効にするかどうか
     * @param verbose 出力に詳細なトークンマッピング情報を含むかどうか
     * @param quiet 物理名のみを出力するかどうか
     */
    private void processCommandLineArguments(ApplicationArguments args, TokenizerType tokenizerType,
                                             NamingConvention namingConvention, boolean enableFallback, boolean verbose, boolean quiet) {
        // 論理名の処理
        List<String> logicalNames = args.getNonOptionArgs();
        if (logicalNames.isEmpty()) {
            log.error("変換対象の論理名が指定されていません");
            exitCode = 1;
            return;
        }

        for (String logicalName : logicalNames) {
            processLogicalName(logicalName, tokenizerType, namingConvention, enableFallback, verbose, quiet);
        }
    }

    /**
     * アプリケーションの終了コードを返します。
     * <p>
     * このメソッドはアプリケーション終了時にSpring Bootによって呼び出され、
     * 終了コードを決定します。終了コードはCLI実行の成功または失敗状態を示します。
     * </p>
     * 
     * @return 実行成功時は0、エラー発生時は1
     * @see org.springframework.boot.ExitCodeGenerator
     */
    @Override
    public int getExitCode() {
        return exitCode;
    }
}
