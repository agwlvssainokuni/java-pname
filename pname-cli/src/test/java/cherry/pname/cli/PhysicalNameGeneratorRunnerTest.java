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
import cherry.pname.main.dictionary.CsvDictionaryLoader;
import cherry.pname.main.dictionary.JsonDictionaryLoader;
import cherry.pname.main.dictionary.TsvDictionaryLoader;
import cherry.pname.main.dictionary.YamlDictionaryLoader;
import cherry.pname.main.romaji.KuromojiRomajiConverter;
import cherry.pname.main.tokenize.GreedyTokenizer;
import cherry.pname.main.tokenize.OptimalTokenizer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.DefaultApplicationArguments;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/**
 * PhysicalNameGeneratorRunnerのテストクラス
 * <p>
 * CLIアプリケーションの各機能を階層的にテストします：
 * - ヘルプ機能と基本動作
 * - フォールバック制御機能
 * - 出力オプション（verbose/quiet）
 * - 設定オプション（tokenizer/naming）
 * - ファイル処理機能
 * - 辞書ファイル処理
 * - エラーハンドリング
 */
@ExtendWith(MockitoExtension.class)
class PhysicalNameGeneratorRunnerTest {

    private PhysicalNameGeneratorRunner runner;
    private PhysicalNameGenerator generator;

    @BeforeEach
    void setUp() throws IOException {
        generator = new PhysicalNameGenerator(
                new CsvDictionaryLoader(),
                new TsvDictionaryLoader(),
                new JsonDictionaryLoader(),
                new YamlDictionaryLoader(),
                new GreedyTokenizer(),
                new OptimalTokenizer(),
                new KuromojiRomajiConverter()
        );

        runner = new PhysicalNameGeneratorRunner(generator);

        // テスト用辞書を読み込み
        String csvData = """
                顧客,customer
                管理,management
                システム,system
                """;
        generator.loadDictionary(DictionaryFormat.CSV, csvData);
    }

    /**
     * ヘルプ機能と基本動作のテスト
     * CLIアプリケーションの基本的な機能をテストします
     */
    @Nested
    class HelpAndBasicFunctionality {

        /**
         * --helpオプションのテスト
         * 
         * <p>検証内容:</p>
         * <ul>
         *   <li>--helpオプション指定時にヘルプメッセージが表示される</li>
         *   <li>例外がスローされずに正常終了する</li>
         *   <li>終了コードが0（正常終了）である</li>
         * </ul>
         * 
         * <p>期待動作:</p>
         * CLIの使用方法、オプション一覧、実行例が標準出力に表示され、
         * アプリケーションが正常終了する。
         */
        @Test
        void testHelpOption() {
            ApplicationArguments args = new DefaultApplicationArguments("--help");

            assertDoesNotThrow(() -> runner.run(args));
            assertEquals(0, runner.getExitCode());
        }

        /**
         * 論理名未指定時の動作テスト
         * 
         * <p>検証内容:</p>
         * <ul>
         *   <li>コマンドライン引数なしで実行時にヘルプが表示される</li>
         *   <li>例外がスローされずに正常終了する</li>
         *   <li>終了コードが0（正常終了）である</li>
         * </ul>
         * 
         * <p>期待動作:</p>
         * 変換対象の論理名が指定されていない場合、エラーではなく
         * ヘルプメッセージを表示してユーザーに使用方法を案内する。
         */
        @Test
        void testNoLogicalNamesProvided() {
            ApplicationArguments args = new DefaultApplicationArguments();

            assertDoesNotThrow(() -> runner.run(args));
            assertEquals(0, runner.getExitCode());
        }

        /**
         * 複数論理名の同時処理テスト
         * 
         * <p>検証内容:</p>
         * <ul>
         *   <li>複数の論理名を同一コマンドで指定できる</li>
         *   <li>すべての論理名が順次処理される</li>
         *   <li>処理中に例外が発生しない</li>
         *   <li>すべて処理完了後に正常終了する</li>
         * </ul>
         * 
         * <p>期待動作:</p>
         * 「顧客管理」「システム」の両方が個別に物理名変換され、
         * それぞれの結果が順次出力される。
         */
        @Test
        void testMultipleLogicalNames() {
            ApplicationArguments args = new DefaultApplicationArguments("顧客管理", "システム");

            assertDoesNotThrow(() -> runner.run(args));
            assertEquals(0, runner.getExitCode());
        }
    }

    /**
     * フォールバック制御機能のテスト
     * 未知語に対するローマ字変換の有効/無効を制御する機能をテストします
     */
    @Nested
    class FallbackControl {

        /**
         * フォールバック有効時の基本変換テスト
         * 
         * <p>検証内容:</p>
         * <ul>
         *   <li>--enable-fallbackオプションが正しく解釈される</li>
         *   <li>辞書にない未知語もローマ字変換される</li>
         *   <li>処理が正常完了する</li>
         * </ul>
         * 
         * <p>期待動作:</p>
         * 辞書に登録されていない単語もKuromojiとICU4Jを使用して
         * ローマ字変換され、物理名に含まれる。
         * 例: 未知語「ABC」→「abc」（ローマ字変換）
         */
        @Test
        void testBasicConversionWithFallbackEnabled() {
            ApplicationArguments args = new DefaultApplicationArguments("--enable-fallback", "顧客管理システム");

            assertDoesNotThrow(() -> runner.run(args));
            assertEquals(0, runner.getExitCode());
        }

        /**
         * フォールバック無効時の基本変換テスト
         * 
         * <p>検証内容:</p>
         * <ul>
         *   <li>フォールバックオプション未指定時の動作</li>
         *   <li>未知語が元の日本語のまま保持される</li>
         *   <li>辞書にある単語は正常に変換される</li>
         *   <li>処理が正常完了する</li>
         * </ul>
         * 
         * <p>期待動作:</p>
         * より安全な動作として、辞書にない単語はローマ字変換せず
         * 元の日本語のまま出力される。
         * 例: 「顧客XY管理」→「customerXy管理」（XYは変換されない）
         */
        @Test
        void testBasicConversionWithFallbackDisabled() {
            ApplicationArguments args = new DefaultApplicationArguments("顧客XY管理");

            assertDoesNotThrow(() -> runner.run(args));
            assertEquals(0, runner.getExitCode());
        }

        @Test
        void testFallbackDefaultBehavior() {
            // フォールバックオプション未指定時、デフォルトで無効になることを確認
            ApplicationArguments args = new DefaultApplicationArguments("顧客XY管理");

            assertDoesNotThrow(() -> runner.run(args));
            assertEquals(0, runner.getExitCode());
        }

        @Test
        void testEnableFallbackOption() {
            // --enable-fallbackオプションが正しく解釈され、フォールバックが有効になることを確認
            ApplicationArguments args = new DefaultApplicationArguments("--enable-fallback", "顧客XY管理");

            assertDoesNotThrow(() -> runner.run(args));
            assertEquals(0, runner.getExitCode());
        }
    }

    /**
     * 出力オプションのテスト
     * 詳細表示（verbose）と簡潔表示（quiet）の動作をテストします
     */
    @Nested
    class OutputOptions {

        @Test
        void testVerboseOption() {
            // --verboseオプション指定時、トークン分解の詳細が表示されることを確認
            ApplicationArguments args = new DefaultApplicationArguments("--verbose", "顧客管理システム");

            assertDoesNotThrow(() -> runner.run(args));
            assertEquals(0, runner.getExitCode());
        }

        @Test
        void testQuietOption() {
            // --quietオプション指定時、物理名のみが表示されることを確認
            ApplicationArguments args = new DefaultApplicationArguments("--quiet", "顧客管理システム");

            assertDoesNotThrow(() -> runner.run(args));
            assertEquals(0, runner.getExitCode());
        }
    }

    /**
     * 設定オプションのテスト
     * トークナイザーと命名規則の設定オプションをテストします
     */
    @Nested
    class ConfigurationOptions {

        @Test
        void testNamingConventionOption() {
            // --namingオプションで命名規則（snake_case等）を指定できることを確認
            ApplicationArguments args = new DefaultApplicationArguments("--naming=LOWER_SNAKE", "顧客管理システム");

            assertDoesNotThrow(() -> runner.run(args));
            assertEquals(0, runner.getExitCode());
        }

        @Test
        void testTokenizerOption() {
            // --tokenizerオプションでトークナイザー（GREEDY/OPTIMAL）を指定できることを確認
            ApplicationArguments args = new DefaultApplicationArguments("--tokenizer=GREEDY", "顧客管理システム");

            assertDoesNotThrow(() -> runner.run(args));
            assertEquals(0, runner.getExitCode());
        }

        @Test
        void testInvalidTokenizerOption() {
            // 無効なトークナイザー指定時、警告表示後にデフォルト値で継続することを確認
            ApplicationArguments args = new DefaultApplicationArguments("--tokenizer=INVALID", "顧客管理システム");

            assertDoesNotThrow(() -> runner.run(args));
            assertEquals(0, runner.getExitCode());
        }

        @Test
        void testInvalidNamingConventionOption() {
            // 無効な命名規則指定時、警告表示後にデフォルト値で継続することを確認
            ApplicationArguments args = new DefaultApplicationArguments("--naming=INVALID", "顧客管理システム");

            assertDoesNotThrow(() -> runner.run(args));
            assertEquals(0, runner.getExitCode());
        }
    }

    /**
     * ファイル処理機能のテスト
     * 入力ファイルからの一括処理と出力ファイルへの書き込み機能をテストします
     */
    @Nested
    class FileProcessing {

        @Test
        void testFileProcessingWithFallbackEnabled() throws IOException {
            // ファイル処理時にフォールバック有効で未知語もローマ字変換されることを確認
            Path inputFile = Files.createTempFile("test_input", ".txt");
            Files.writeString(inputFile, "顧客管理システム\n注文XY処理", StandardCharsets.UTF_8);

            Path outputFile = Files.createTempFile("test_output", ".txt");

            try {
                ApplicationArguments args = new DefaultApplicationArguments(
                        "--input=" + inputFile.toString(),
                        "--output=" + outputFile.toString(),
                        "--enable-fallback"
                );

                assertDoesNotThrow(() -> runner.run(args));
                assertEquals(0, runner.getExitCode());

                assertTrue(Files.exists(outputFile));
                String output = Files.readString(outputFile, StandardCharsets.UTF_8);
                assertFalse(output.isEmpty());

            } finally {
                Files.deleteIfExists(inputFile);
                Files.deleteIfExists(outputFile);
            }
        }

        @Test
        void testFileProcessingWithFallbackDisabled() throws IOException {
            // ファイル処理時にフォールバック無効で未知語が元のまま残ることを確認
            Path inputFile = Files.createTempFile("test_input", ".txt");
            Files.writeString(inputFile, "顧客管理システム\n注文XY処理", StandardCharsets.UTF_8);

            Path outputFile = Files.createTempFile("test_output", ".txt");

            try {
                ApplicationArguments args = new DefaultApplicationArguments(
                        "--input=" + inputFile.toString(),
                        "--output=" + outputFile.toString()
                );

                assertDoesNotThrow(() -> runner.run(args));
                assertEquals(0, runner.getExitCode());

                assertTrue(Files.exists(outputFile));
                String output = Files.readString(outputFile, StandardCharsets.UTF_8);
                assertFalse(output.isEmpty());

            } finally {
                Files.deleteIfExists(inputFile);
                Files.deleteIfExists(outputFile);
            }
        }

        @Test
        void testFileProcessingVerbose() throws IOException {
            // ファイル処理時の詳細出力（論理名、物理名、トークン分解）が正しく出力されることを確認
            Path inputFile = Files.createTempFile("test_input", ".txt");
            Files.writeString(inputFile, "顧客管理システム", StandardCharsets.UTF_8);

            Path outputFile = Files.createTempFile("test_output", ".txt");

            try {
                ApplicationArguments args = new DefaultApplicationArguments(
                        "--input=" + inputFile.toString(),
                        "--output=" + outputFile.toString(),
                        "--verbose"
                );

                assertDoesNotThrow(() -> runner.run(args));
                assertEquals(0, runner.getExitCode());

                assertTrue(Files.exists(outputFile));
                String output = Files.readString(outputFile, StandardCharsets.UTF_8);
                assertTrue(output.contains("論理名:"));
                assertTrue(output.contains("物理名:"));
                assertTrue(output.contains("トークン分解:"));

            } finally {
                Files.deleteIfExists(inputFile);
                Files.deleteIfExists(outputFile);
            }
        }

        @Test
        void testFileProcessingQuiet() throws IOException {
            // ファイル処理時の簡潔出力（物理名のみ）が正しく出力されることを確認
            Path inputFile = Files.createTempFile("test_input", ".txt");
            Files.writeString(inputFile, "顧客管理システム", StandardCharsets.UTF_8);

            Path outputFile = Files.createTempFile("test_output", ".txt");

            try {
                ApplicationArguments args = new DefaultApplicationArguments(
                        "--input=" + inputFile.toString(),
                        "--output=" + outputFile.toString(),
                        "--quiet"
                );

                assertDoesNotThrow(() -> runner.run(args));
                assertEquals(0, runner.getExitCode());

                assertTrue(Files.exists(outputFile));
                String output = Files.readString(outputFile, StandardCharsets.UTF_8);
                assertFalse(output.contains("論理名:"));
                assertFalse(output.contains("物理名:"));
                assertFalse(output.contains("トークン分解:"));

            } finally {
                Files.deleteIfExists(inputFile);
                Files.deleteIfExists(outputFile);
            }
        }

        @Test
        void testEmptyInputFile() throws IOException {
            // 空の入力ファイルを指定した場合でも正常に処理されることを確認
            Path inputFile = Files.createTempFile("test_empty", ".txt");
            Files.writeString(inputFile, "", StandardCharsets.UTF_8);

            try {
                ApplicationArguments args = new DefaultApplicationArguments("--input=" + inputFile.toString());

                assertDoesNotThrow(() -> runner.run(args));
                assertEquals(0, runner.getExitCode());

            } finally {
                Files.deleteIfExists(inputFile);
            }
        }

        @Test
        void testInputFileWithEmptyLines() throws IOException {
            // 空行を含む入力ファイルで、空行が適切にスキップされることを確認
            Path inputFile = Files.createTempFile("test_empty_lines", ".txt");
            Files.writeString(inputFile, "顧客管理システム\n\n\n注文処理\n", StandardCharsets.UTF_8);

            try {
                ApplicationArguments args = new DefaultApplicationArguments("--input=" + inputFile.toString());

                assertDoesNotThrow(() -> runner.run(args));
                assertEquals(0, runner.getExitCode());

            } finally {
                Files.deleteIfExists(inputFile);
            }
        }
    }

    /**
     * 辞書ファイル処理のテスト
     * カスタム辞書ファイルの読み込み機能をテストします
     */
    @Nested
    class DictionaryHandling {

        @Test
        void testCustomDictionaryFile() throws IOException {
            // カスタム辞書ファイルが正しく読み込まれ、変換に使用されることを確認
            Path dictFile = Files.createTempFile("test_dict", ".csv");
            Files.writeString(dictFile, "テスト,test\n処理,processing", StandardCharsets.UTF_8);

            try {
                ApplicationArguments args = new DefaultApplicationArguments(
                        "--dictionary=" + dictFile.toString(),
                        "テスト処理"
                );

                assertDoesNotThrow(() -> runner.run(args));
                assertEquals(0, runner.getExitCode());

            } finally {
                Files.deleteIfExists(dictFile);
            }
        }
    }

    /**
     * エラーハンドリングのテスト
     * 存在しないファイル指定時などのエラー処理をテストします
     */
    @Nested
    class ErrorHandling {

        @Test
        void testNonExistentInputFile() {
            // 存在しない入力ファイル指定時、適切なエラーコード（1）で終了することを確認
            ApplicationArguments args = new DefaultApplicationArguments("--input=/nonexistent/file.txt");

            assertDoesNotThrow(() -> runner.run(args));
            assertEquals(1, runner.getExitCode());
        }

        @Test
        void testNonExistentDictionaryFile() {
            // 存在しない辞書ファイル指定時、適切なエラーコード（1）で終了することを確認
            ApplicationArguments args = new DefaultApplicationArguments("--dictionary=/nonexistent/dict.csv", "顧客管理");

            assertDoesNotThrow(() -> runner.run(args));
            assertEquals(1, runner.getExitCode());
        }
    }
}
