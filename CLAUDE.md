# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is a Java tool for converting logical names (Japanese) to physical names (alphanumeric). The project generates English identifiers from Japanese business terms for use in databases, APIs, and code.

## Project Structure

Multi-module Gradle project with three subprojects:

- **pname-main**: Core physical name generation logic
- **pname-cli**: Command-line interface for name conversion
- **pname-web**: Web application with REST API and Thymeleaf frontend

## Development Setup

### Build System
- Gradle with Wrapper (use `./gradlew` commands)
- Java 21 with Spring Boot 3.5.4
- Multi-project build configuration

### Common Commands
```bash
# Build all projects
./gradlew build

# Run tests
./gradlew test

# Run specific subproject tests
./gradlew :pname-main:test
./gradlew :pname-cli:test
./gradlew :pname-web:test

# Run CLI application
./gradlew :pname-cli:bootRun --args="--help"
./gradlew :pname-cli:bootRun --args="--dictionary=dict.csv 顧客管理システム"
./gradlew :pname-cli:bootRun --args="--dictionary=dict.yaml --format=YAML --naming=LOWER_SNAKE 顧客管理システム"

# Run Web application
./gradlew :pname-web:bootRun
# Access http://localhost:8080
```

### Project Layout
```
java-pname/
├── pname-main/          # Core conversion logic
│   └── src/main/java/cherry/pname/main/
│       ├── tokenize/    # Word tokenization logic
│       ├── dictionary/  # Dictionary loading (CSV, TSV, JSON, YAML)
│       └── romaji/      # Japanese to romaji conversion
├── pname-cli/           # CLI interface (implemented)
│   └── src/main/java/cherry/pname/cli/
│       ├── Main.java                        # Spring Boot entry point
│       └── PhysicalNameGeneratorRunner.java # CLI processing logic
└── pname-web/           # Web API + Thymeleaf frontend (implemented)
    └── src/main/java/cherry/pname/web/
        ├── Main.java                            # Spring Boot Web entry point
        ├── controller/                          # Web controllers
        │   ├── PhysicalNameController.java      # REST API endpoints
        │   └── WebController.java               # Web UI controller
        └── dto/                                 # Data transfer objects
```

## Dependencies

- Spring Boot 3.5.4 (for pname-cli and pname-web)
- Apache Commons CSV 1.14.1
- Google Guava 33.4.8-jre
- Kuromoji IPADIC 0.9.0 (Japanese morphological analysis)
- ICU4J 76.1 (Unicode text processing and Japanese romanization)
- Jackson Databind (JSON processing)
- Jackson YAML (YAML processing)
- SpringDoc OpenAPI (API documentation)
- JUnit 5 for testing

## Architecture Notes

- Package structure follows `cherry.pname.*` convention
- pname-main contains framework-agnostic conversion logic with Spring components
- pname-cli and pname-web depend on pname-main
- Build configuration supports both executable applications (CLI/Web) and library (main)

### Core Components

**Tokenization (cherry.pname.main.tokenize)**:
- `Tokenizer` interface: Common contract for word tokenization
- `Token` record: Result format (word, physicalNames, isUnknown)
- `GreedyTokenizer` (@Component("greedyTokenizer")): Forward longest-match algorithm
- `OptimalTokenizer` (@Component("optimalTokenizer")): Dynamic programming with evaluation criteria
  - Priority: unknown word length minimization → token count minimization → dictionary word maximization → unknown word count minimization
- Thread-safe implementation with method-local memoization

**Dictionary Loading (cherry.pname.main.dictionary)**:
- `DictionaryLoader` interface: Common contract for dictionary loading
- `CsvDictionaryLoader` (@Component("csvDictionaryLoader")): CSV format support
- `TsvDictionaryLoader` (@Component("tsvDictionaryLoader")): TSV format support  
- `JsonDictionaryLoader` (@Component("jsonDictionaryLoader")): JSON format support
- `YamlDictionaryLoader` (@Component("yamlDictionaryLoader")): YAML format support with single/array value handling
- `DictionaryFormat` enum: Format specification (CSV, TSV, JSON, YAML)

**Japanese Romanization (cherry.pname.main.romaji)**:
- `RomajiConverter` interface: Common contract for Japanese to romaji conversion
- `KuromojiRomajiConverter` (@Component): Kuromoji morphological analysis + ICU4J romanization
- Handles hiragana, katakana, and kanji text conversion
- Provides detailed conversion results in tokenMappings as "(romaji: xy kanri)"

**Physical Name Generation**:
- `PhysicalNameGenerator` (@Component): Main service class
- `TokenizerType` enum: Tokenizer selection (GREEDY, OPTIMAL)
- `NamingConvention` enum: 10 naming conventions (CAMEL, PASCAL, LOWER_CAMEL, UPPER_CAMEL, SNAKE, LOWER_SNAKE, UPPER_SNAKE, KEBAB, LOWER_KEBAB, UPPER_KEBAB)
- `PhysicalNameResult` record: Generation results with tokenMappings showing conversion details

**CLI Interface (cherry.pname.cli)**:
- `Main` class: Spring Boot application entry point with component scanning
- `PhysicalNameGeneratorRunner` (@Component): ApplicationRunner implementing CLI logic
- Command-line argument processing with comprehensive option support
- File-based batch processing capabilities
- Multiple output formats (verbose, normal, quiet)
- Error handling with appropriate exit codes

**Web Interface (cherry.pname.web)**:
- `Main` class: Spring Boot Web application entry point
- `PhysicalNameController` (@RestController): REST API endpoints with OpenAPI annotations
  - POST /api/generate: Physical name generation
  - POST /api/generate/dictionary: Dictionary file upload (supports CSV, TSV, JSON, YAML)
  - GET /api/generate/dictionary/info: Dictionary information
- `WebController` (@Controller): Web UI controller for main page
- `GenerateRequest/Response`: DTOs for API communication
- `OpenApiConfig`: SpringDoc configuration for automatic API documentation
- Thymeleaf + Bootstrap frontend with file upload and real-time generation
- OpenAPI 3.0 specification with Swagger UI integration
- Comprehensive error handling and validation

**API Documentation**:
- Interactive Swagger UI: http://localhost:8080/swagger-ui/index.html
- OpenAPI JSON specification: http://localhost:8080/v3/api-docs
- Static openapi.yaml file with complete API specification
- Comprehensive API_REFERENCE.md documentation

## License

Project is licensed under Apache License 2.0.
