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

import cherry.pname.main.*;
import cherry.pname.web.dto.GenerateRequest;
import cherry.pname.web.dto.GenerateResponse;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * 物理名生成のREST APIコントローラー
 */
@RestController
@RequestMapping("/api/generate")
@CrossOrigin(origins = "*")
public class PhysicalNameController {

    private final PhysicalNameGenerator generator;

    public PhysicalNameController(PhysicalNameGenerator generator) {
        this.generator = generator;
    }

    /**
     * 物理名生成API
     *
     * @param request 生成リクエスト
     * @return 生成結果
     */
    @PostMapping
    public ResponseEntity<GenerateResponse> generatePhysicalName(@RequestBody GenerateRequest request) {
        try {
            // 辞書データがある場合は読み込み
            if (request.getDictionaryData() != null && !request.getDictionaryData().trim().isEmpty()) {
                DictionaryFormat format = DictionaryFormat.valueOf(request.getDictionaryFormat().toUpperCase());
                generator.loadDictionary(format, request.getDictionaryData());
            }

            // 物理名生成
            TokenizerType tokenizerType = TokenizerType.valueOf(request.getTokenizerType().toUpperCase());
            NamingConvention namingConvention = NamingConvention.valueOf(request.getNamingConvention().toUpperCase());
            
            PhysicalNameResult result = generator.generatePhysicalName(
                    tokenizerType, namingConvention, request.getLogicalName());

            return ResponseEntity.ok(GenerateResponse.fromResult(result));
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(GenerateResponse.error("不正なパラメータです: " + e.getMessage()));
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(GenerateResponse.error("辞書の読み込みに失敗しました: " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(GenerateResponse.error("処理中にエラーが発生しました: " + e.getMessage()));
        }
    }

    /**
     * 辞書ファイルアップロードAPI
     *
     * @param file 辞書ファイル
     * @param format 辞書形式
     * @return アップロード結果
     */
    @PostMapping("/dictionary")
    public ResponseEntity<String> uploadDictionary(
            @RequestParam("file") MultipartFile file,
            @RequestParam("format") String format) {
        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body("ファイルが選択されていません");
            }

            String content = new String(file.getBytes(), StandardCharsets.UTF_8);
            DictionaryFormat dictionaryFormat = DictionaryFormat.valueOf(format.toUpperCase());
            
            generator.loadDictionary(dictionaryFormat, content);
            
            return ResponseEntity.ok("辞書ファイルを読み込みました (" + generator.getDictionarySize() + "エントリ)");
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("不正な辞書形式です: " + format);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("辞書の読み込みに失敗しました: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("処理中にエラーが発生しました: " + e.getMessage());
        }
    }

    /**
     * 辞書情報取得API
     *
     * @return 辞書情報
     */
    @GetMapping("/dictionary/info")
    public ResponseEntity<String> getDictionaryInfo() {
        if (generator.hasDictionary()) {
            return ResponseEntity.ok("辞書が読み込まれています (" + generator.getDictionarySize() + "エントリ)");
        } else {
            return ResponseEntity.ok("辞書が読み込まれていません");
        }
    }
}