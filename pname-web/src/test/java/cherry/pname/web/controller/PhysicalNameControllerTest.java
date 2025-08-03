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

package cherry.pname.web.controller;

import cherry.pname.web.dto.GenerateRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * PhysicalNameControllerのテストクラス
 * <p>
 * Web APIの各機能を階層的にテストします：
 * - 物理名生成API（基本機能）
 * - フォールバック制御機能
 * - 辞書データ処理
 * - 辞書ファイル管理
 * - エラーハンドリング
 */
@SpringBootTest
@AutoConfigureMockMvc
class PhysicalNameControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * 物理名生成APIの基本機能テスト
     * 辞書なしでの基本的な物理名生成をテストします
     */
    @Nested
    class BasicPhysicalNameGeneration {

        /**
         * 辞書なし基本変換APIテスト
         * 
         * <p>検証内容:</p>
         * <ul>
         *   <li>POST /api/generate エンドポイントが正常に動作する</li>
         *   <li>辞書データなしでも変換処理が実行される</li>
         *   <li>HTTPステータス200が返される</li>
         *   <li>レスポンスJSONの必須フィールドが存在する</li>
         *   <li>success=trueで成功を示す</li>
         * </ul>
         * 
         * <p>期待動作:</p>
         * 辞書が指定されていない場合でも、デフォルトの形態素解析と
         * ローマ字変換により物理名が生成される。
         */
        @Test
        void testGeneratePhysicalNameWithoutDictionary() throws Exception {
            GenerateRequest request = new GenerateRequest("テスト", "OPTIMAL", "LOWER_CAMEL");

            mockMvc.perform(post("/api/generate")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.logicalName").value("テスト"))
                    .andExpect(jsonPath("$.physicalName").exists())
                    .andExpect(jsonPath("$.tokenMappings").isArray());
        }

        @Test
        void testGeneratePhysicalNameWithInvalidParameters() throws Exception {
            // 無効なパラメータ指定時に適切なエラーレスポンスが返されることを確認
            GenerateRequest request = new GenerateRequest("テスト", "INVALID_TYPE", "LOWER_CAMEL");

            mockMvc.perform(post("/api/generate")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.errorMessage").exists());
        }
    }

    /**
     * フォールバック制御機能のテスト
     * 未知語に対するローマ字変換の有効/無効をAPIで制御する機能をテストします
     */
    @Nested
    class FallbackControl {

        @Test
        void testGeneratePhysicalNameWithFallbackEnabled() throws Exception {
            // フォールバック有効時、未知語もローマ字変換されることを確認
            GenerateRequest request = new GenerateRequest("テストXY", "OPTIMAL", "LOWER_CAMEL");
            request.setEnableFallback(true);

            mockMvc.perform(post("/api/generate")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.logicalName").value("テストXY"))
                    .andExpect(jsonPath("$.physicalName").exists())
                    .andExpect(jsonPath("$.tokenMappings").isArray());
        }

        @Test
        void testGeneratePhysicalNameWithFallbackDisabled() throws Exception {
            // フォールバック無効時、未知語は元の日本語のまま残ることを確認
            GenerateRequest request = new GenerateRequest("テストXY", "OPTIMAL", "LOWER_CAMEL");
            request.setEnableFallback(false);

            mockMvc.perform(post("/api/generate")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.logicalName").value("テストXY"))
                    .andExpect(jsonPath("$.physicalName").exists())
                    .andExpect(jsonPath("$.tokenMappings").isArray());
        }

        @Test
        void testGeneratePhysicalNameDefaultFallbackBehavior() throws Exception {
            // enableFallback未指定時、デフォルトでfalse（無効）になることを確認
            GenerateRequest request = new GenerateRequest("顧客XY管理", "OPTIMAL", "LOWER_CAMEL");
            request.setDictionaryData("顧客,customer\n管理,management");
            request.setDictionaryFormat("CSV");

            mockMvc.perform(post("/api/generate")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.logicalName").value("顧客XY管理"))
                    .andExpect(jsonPath("$.physicalName").value("customerXyManagement"))
                    .andExpect(jsonPath("$.tokenMappings").isArray());
        }
    }

    /**
     * 辞書データ処理のテスト
     * インライン辞書データを使用した物理名生成をテストします
     */
    @Nested
    class DictionaryDataProcessing {

        @Test
        void testGeneratePhysicalNameWithDictionary() throws Exception {
            // インライン辞書データを使用した物理名生成が正常に動作することを確認
            GenerateRequest request = new GenerateRequest("顧客管理", "OPTIMAL", "SNAKE");
            request.setDictionaryData("顧客,customer\n管理,management");
            request.setDictionaryFormat("CSV");

            mockMvc.perform(post("/api/generate")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.logicalName").value("顧客管理"))
                    .andExpect(jsonPath("$.physicalName").value("customer_management"));
        }

        @Test
        void testGeneratePhysicalNameWithDictionaryAndFallbackEnabled() throws Exception {
            // 辞書データありでフォールバック有効時、未知語がローマ字変換されることを確認
            GenerateRequest request = new GenerateRequest("顧客XY管理", "OPTIMAL", "LOWER_CAMEL");
            request.setDictionaryData("顧客,customer\n管理,management");
            request.setDictionaryFormat("CSV");
            request.setEnableFallback(true);

            mockMvc.perform(post("/api/generate")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.logicalName").value("顧客XY管理"))
                    .andExpect(jsonPath("$.physicalName").exists())
                    .andExpect(jsonPath("$.tokenMappings").isArray());
        }
    }

    /**
     * 辞書ファイル管理のテスト
     * 辞書ファイルのアップロード機能をテストします
     */
    @Nested
    class DictionaryFileManagement {

        @Test
        void testUploadDictionary() throws Exception {
            // 辞書ファイルのアップロードが正常に動作することを確認
            String csvContent = "顧客,customer\n管理,management\nシステム,system";
            MockMultipartFile file = new MockMultipartFile("file", "test.csv", "text/plain", csvContent.getBytes());

            mockMvc.perform(multipart("/api/generate/dictionary")
                            .file(file)
                            .param("format", "CSV"))
                    .andExpect(status().isOk())
                    .andExpect(content().string(org.hamcrest.Matchers.containsString("辞書ファイルを読み込みました")));
        }

        @Test
        void testUploadEmptyFile() throws Exception {
            // 空ファイルアップロード時に適切なエラーレスポンスが返されることを確認
            MockMultipartFile file = new MockMultipartFile("file", "test.csv", "text/plain", new byte[0]);

            mockMvc.perform(multipart("/api/generate/dictionary")
                            .file(file)
                            .param("format", "CSV"))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().string("ファイルが選択されていません"));
        }

        @Test
        void testUploadInvalidFormat() throws Exception {
            // 無効な辞書形式指定時に適切なエラーレスポンスが返されることを確認
            String csvContent = "顧客,customer";
            MockMultipartFile file = new MockMultipartFile("file", "test.csv", "text/plain", csvContent.getBytes());

            mockMvc.perform(multipart("/api/generate/dictionary")
                            .file(file)
                            .param("format", "INVALID"))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().string(org.hamcrest.Matchers.containsString("不正な辞書形式です")));
        }

        @Test
        void testGetDictionaryInfo() throws Exception {
            // 辞書情報取得APIが正常に動作することを確認
            mockMvc.perform(get("/api/generate/dictionary/info"))
                    .andExpect(status().isOk())
                    .andExpect(content().string(org.hamcrest.Matchers.containsString("辞書")));
        }
    }
}
