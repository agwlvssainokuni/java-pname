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

package cherry.pname.web.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * OpenAPI設定クラス
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI physicalNameGeneratorOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Physical Name Generator API")
                        .description("""
                                A Java tool for converting Japanese logical names to alphanumeric physical names.
                                Generates identifiers for use in databases, APIs, and code using dictionary-based
                                conversion and romanization fallback.
                                
                                ## Features
                                - Multi-format dictionary support (CSV, TSV, JSON, YAML)
                                - Advanced tokenization algorithms (Greedy, Optimal)
                                - Japanese text processing with morphological analysis
                                - 10 different naming conventions
                                - Real-time physical name generation
                                - Interactive API documentation with Swagger UI
                                - Comprehensive error handling and validation
                                
                                ## Dictionary Formats
                                - CSV: Comma-separated values format
                                - TSV: Tab-separated values format  
                                - JSON: JavaScript Object Notation
                                - YAML: Human-readable structured data format
                                """)
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Physical Name Generator")
                                .url("https://github.com/example/java-pname"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0.html")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8080/api")
                                .description("Local development server"),
                        new Server()
                                .url("https://api.example.com/api")
                                .description("Production server")
                ));
    }
}
