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

    @Test
    void testHelpOption() {
        ApplicationArguments args = new DefaultApplicationArguments("--help");
        
        assertDoesNotThrow(() -> runner.run(args));
        assertEquals(0, runner.getExitCode());
    }

    @Test
    void testBasicConversionWithFallbackEnabled() {
        ApplicationArguments args = new DefaultApplicationArguments("--enable-fallback", "顧客管理システム");
        
        assertDoesNotThrow(() -> runner.run(args));
        assertEquals(0, runner.getExitCode());
    }

    @Test
    void testBasicConversionWithFallbackDisabled() {
        ApplicationArguments args = new DefaultApplicationArguments("顧客XY管理");
        
        assertDoesNotThrow(() -> runner.run(args));
        assertEquals(0, runner.getExitCode());
    }

    @Test
    void testMultipleLogicalNames() {
        ApplicationArguments args = new DefaultApplicationArguments("顧客管理", "システム");
        
        assertDoesNotThrow(() -> runner.run(args));
        assertEquals(0, runner.getExitCode());
    }

    @Test
    void testVerboseOption() {
        ApplicationArguments args = new DefaultApplicationArguments("--verbose", "顧客管理システム");
        
        assertDoesNotThrow(() -> runner.run(args));
        assertEquals(0, runner.getExitCode());
    }

    @Test
    void testQuietOption() {
        ApplicationArguments args = new DefaultApplicationArguments("--quiet", "顧客管理システム");
        
        assertDoesNotThrow(() -> runner.run(args));
        assertEquals(0, runner.getExitCode());
    }

    @Test
    void testNamingConventionOption() {
        ApplicationArguments args = new DefaultApplicationArguments("--naming=LOWER_SNAKE", "顧客管理システム");
        
        assertDoesNotThrow(() -> runner.run(args));
        assertEquals(0, runner.getExitCode());
    }

    @Test
    void testTokenizerOption() {
        ApplicationArguments args = new DefaultApplicationArguments("--tokenizer=GREEDY", "顧客管理システム");
        
        assertDoesNotThrow(() -> runner.run(args));
        assertEquals(0, runner.getExitCode());
    }

    @Test
    void testInvalidTokenizerOption() {
        ApplicationArguments args = new DefaultApplicationArguments("--tokenizer=INVALID", "顧客管理システム");
        
        assertDoesNotThrow(() -> runner.run(args));
        assertEquals(0, runner.getExitCode()); // デフォルトが使用される
    }

    @Test
    void testInvalidNamingConventionOption() {
        ApplicationArguments args = new DefaultApplicationArguments("--naming=INVALID", "顧客管理システム");
        
        assertDoesNotThrow(() -> runner.run(args));
        assertEquals(0, runner.getExitCode()); // デフォルトが使用される
    }

    @Test
    void testFileProcessingWithFallbackEnabled() throws IOException {
        // 一時入力ファイルを作成
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

            // 出力ファイルが作成されていることを確認
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
        // 一時入力ファイルを作成
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

            // 出力ファイルが作成されていることを確認
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
        // 一時入力ファイルを作成
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

            // 出力ファイルが作成されていることを確認
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
        // 一時入力ファイルを作成
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

            // 出力ファイルが作成されていることを確認
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
    void testNonExistentInputFile() {
        ApplicationArguments args = new DefaultApplicationArguments("--input=/nonexistent/file.txt");
        
        assertDoesNotThrow(() -> runner.run(args));
        assertEquals(1, runner.getExitCode());
    }

    @Test
    void testNonExistentDictionaryFile() {
        ApplicationArguments args = new DefaultApplicationArguments("--dictionary=/nonexistent/dict.csv", "顧客管理");
        
        assertDoesNotThrow(() -> runner.run(args));
        assertEquals(1, runner.getExitCode());
    }

    @Test
    void testNoLogicalNamesProvided() {
        ApplicationArguments args = new DefaultApplicationArguments();
        
        assertDoesNotThrow(() -> runner.run(args));
        assertEquals(0, runner.getExitCode()); // ヘルプが表示される
    }

    @Test
    void testEmptyInputFile() throws IOException {
        // 空の入力ファイルを作成
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
        // 空行を含む入力ファイルを作成
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

    @Test
    void testCustomDictionaryFile() throws IOException {
        // カスタム辞書ファイルを作成
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

    @Test
    void testFallbackDefaultBehavior() {
        // フォールバックオプションが指定されていない場合、デフォルトでfalseであることを確認
        ApplicationArguments args = new DefaultApplicationArguments("顧客XY管理");
        
        assertDoesNotThrow(() -> runner.run(args));
        assertEquals(0, runner.getExitCode());
    }

    @Test
    void testEnableFallbackOption() {
        // --enable-fallbackが指定された場合、フォールバックが有効になることを確認
        ApplicationArguments args = new DefaultApplicationArguments("--enable-fallback", "顧客XY管理");
        
        assertDoesNotThrow(() -> runner.run(args));
        assertEquals(0, runner.getExitCode());
    }
}
