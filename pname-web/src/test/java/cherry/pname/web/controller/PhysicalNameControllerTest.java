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
 */
@SpringBootTest
@AutoConfigureMockMvc
class PhysicalNameControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

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
    void testGeneratePhysicalNameWithDictionary() throws Exception {
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
    void testGeneratePhysicalNameWithInvalidParameters() throws Exception {
        GenerateRequest request = new GenerateRequest("テスト", "INVALID_TYPE", "LOWER_CAMEL");

        mockMvc.perform(post("/api/generate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorMessage").exists());
    }

    @Test
    void testUploadDictionary() throws Exception {
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
        MockMultipartFile file = new MockMultipartFile("file", "test.csv", "text/plain", new byte[0]);

        mockMvc.perform(multipart("/api/generate/dictionary")
                        .file(file)
                        .param("format", "CSV"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("ファイルが選択されていません"));
    }

    @Test
    void testUploadInvalidFormat() throws Exception {
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
        mockMvc.perform(get("/api/generate/dictionary/info"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("辞書")));
    }
}
