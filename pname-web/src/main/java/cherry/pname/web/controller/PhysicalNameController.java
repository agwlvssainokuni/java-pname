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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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
@Tag(name = "Physical Name Generation", description = "Core functionality for converting Japanese logical names to physical names")
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
    @Operation(
        summary = "Generate Physical Name",
        description = "Converts a Japanese logical name into an alphanumeric physical name using specified tokenization and naming convention strategies. The API supports both pre-loaded dictionaries (via file upload) and inline dictionary data provided directly in the request."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Physical name generated successfully",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = GenerateResponse.class),
                examples = @ExampleObject(
                    name = "success",
                    summary = "Successful generation",
                    value = """
                    {
                        "success": true,
                        "logicalName": "顧客管理システム",
                        "physicalName": "customerManagementSystem",
                        "tokenMappings": [
                            "顧客=>customer",
                            "管理=>management",
                            "システム=>system"
                        ],
                        "errorMessage": null
                    }
                    """
                )
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid request parameters",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = GenerateResponse.class),
                examples = @ExampleObject(
                    name = "invalid_parameter",
                    summary = "Invalid parameter error",
                    value = """
                    {
                        "success": false,
                        "logicalName": null,
                        "physicalName": null,
                        "tokenMappings": null,
                        "errorMessage": "不正なパラメータです: Invalid naming convention"
                    }
                    """
                )
            )
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Internal server error",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = GenerateResponse.class),
                examples = @ExampleObject(
                    name = "server_error",
                    summary = "Server error",
                    value = """
                    {
                        "success": false,
                        "logicalName": null,
                        "physicalName": null,
                        "tokenMappings": null,
                        "errorMessage": "辞書の読み込みに失敗しました: Invalid dictionary format"
                    }
                    """
                )
            )
        )
    })
    @PostMapping
    public ResponseEntity<GenerateResponse> generatePhysicalName(
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Request containing logical name and conversion parameters",
            required = true,
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = GenerateRequest.class),
                examples = {
                    @ExampleObject(
                        name = "basic",
                        summary = "Basic conversion",
                        value = """
                        {
                            "logicalName": "顧客管理",
                            "tokenizerType": "OPTIMAL",
                            "namingConvention": "LOWER_CAMEL"
                        }
                        """
                    ),
                    @ExampleObject(
                        name = "with_dictionary",
                        summary = "With inline dictionary",
                        value = """
                        {
                            "logicalName": "顧客管理システム",
                            "tokenizerType": "OPTIMAL",
                            "namingConvention": "UPPER_SNAKE",
                            "dictionaryData": "顧客,customer\\n管理,management\\nシステム,system",
                            "dictionaryFormat": "CSV"
                        }
                        """
                    ),
                    @ExampleObject(
                        name = "snake_case",
                        summary = "Snake case output",
                        value = """
                        {
                            "logicalName": "注文処理機能",
                            "tokenizerType": "GREEDY",
                            "namingConvention": "LOWER_SNAKE"
                        }
                        """
                    )
                }
            )
        )
        @RequestBody GenerateRequest request) {
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
                    tokenizerType, namingConvention, request.getLogicalName(), request.isEnableFallback());

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
    @Operation(
        summary = "Upload Dictionary File",
        description = "Uploads and loads a dictionary file for use in subsequent generation requests. The dictionary will be stored in memory and used for all future generation requests until a new dictionary is uploaded or the server is restarted. Supported formats: CSV, TSV, JSON, YAML",
        tags = {"Dictionary Management"}
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Dictionary uploaded successfully",
            content = @Content(
                mediaType = MediaType.TEXT_PLAIN_VALUE,
                schema = @Schema(type = "string"),
                examples = @ExampleObject(
                    name = "success",
                    summary = "Upload successful",
                    value = "辞書ファイルを読み込みました (150エントリ)"
                )
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid request",
            content = @Content(
                mediaType = MediaType.TEXT_PLAIN_VALUE,
                schema = @Schema(type = "string"),
                examples = {
                    @ExampleObject(
                        name = "no_file",
                        summary = "No file selected",
                        value = "ファイルが選択されていません"
                    ),
                    @ExampleObject(
                        name = "invalid_format",
                        summary = "Invalid format",
                        value = "不正な辞書形式です: INVALID_FORMAT"
                    )
                }
            )
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Dictionary processing error",
            content = @Content(
                mediaType = MediaType.TEXT_PLAIN_VALUE,
                schema = @Schema(type = "string"),
                examples = @ExampleObject(
                    name = "processing_error",
                    summary = "Processing error",
                    value = "辞書の読み込みに失敗しました: Invalid file format"
                )
            )
        )
    })
    @PostMapping(value = "/dictionary", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> uploadDictionary(
            @Parameter(
                description = "Dictionary file in CSV, TSV, JSON, or YAML format",
                required = true,
                content = @Content(mediaType = MediaType.APPLICATION_OCTET_STREAM_VALUE)
            )
            @RequestParam("file") MultipartFile file,
            @Parameter(
                description = "Dictionary file format",
                required = true,
                schema = @Schema(type = "string", allowableValues = {"CSV", "TSV", "JSON", "YAML"})
            )
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
    @Operation(
        summary = "Get Dictionary Information",
        description = "Returns information about the currently loaded dictionary, including whether a dictionary is loaded and the number of entries it contains.",
        tags = {"Dictionary Management"}
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Dictionary information retrieved successfully",
            content = @Content(
                mediaType = MediaType.TEXT_PLAIN_VALUE,
                schema = @Schema(type = "string"),
                examples = {
                    @ExampleObject(
                        name = "with_dictionary",
                        summary = "Dictionary loaded",
                        value = "辞書が読み込まれています (150エントリ)"
                    ),
                    @ExampleObject(
                        name = "without_dictionary",
                        summary = "No dictionary loaded",
                        value = "辞書が読み込まれていません"
                    )
                }
            )
        )
    })
    @GetMapping("/dictionary/info")
    public ResponseEntity<String> getDictionaryInfo() {
        if (generator.hasDictionary()) {
            return ResponseEntity.ok("辞書が読み込まれています (" + generator.getDictionarySize() + "エントリ)");
        } else {
            return ResponseEntity.ok("辞書が読み込まれていません");
        }
    }
}
