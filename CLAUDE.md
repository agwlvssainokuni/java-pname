# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is a Java tool for converting logical names (Japanese) to physical names (alphanumeric). The project generates English identifiers from Japanese business terms for use in databases, APIs, and code.

## Project Structure

Multi-module Gradle project with three subprojects:

- **pname-main**: Core physical name generation logic
- **pname-cli**: Command-line interface for name conversion
- **pname-web**: Web application with REST API and React frontend

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
```

### Project Layout
```
java-pname/
├── pname-main/          # Core conversion logic
│   └── src/main/java/cherry/pname/main/
│       ├── tokenize/    # Word tokenization logic
│       ├── dictionary/  # Dictionary loading (CSV, TSV, JSON)
│       └── romaji/      # Japanese to romaji conversion
├── pname-cli/           # CLI interface
└── pname-web/           # Web API + React frontend
```

## Dependencies

- Spring Boot 3.5.4 (for pname-cli and pname-web)
- Apache Commons CSV 1.14.1
- Google Guava 33.4.8-jre
- Kuromoji IPADIC 0.9.0 (Japanese morphological analysis)
- ICU4J 76.1 (Unicode text processing and Japanese romanization)
- Jackson Databind (JSON processing)
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
- `DictionaryFormat` enum: Format specification (CSV, TSV, JSON)

**Japanese Romanization (cherry.pname.main.romaji)**:
- `RomajiConverter` interface: Common contract for Japanese to romaji conversion
- `KuromojiRomajiConverter` (@Component): Kuromoji morphological analysis + ICU4J romanization
- Handles hiragana, katakana, and kanji text conversion
- Provides detailed conversion results in tokenMappings as "(romaji: xy kanri)"

**Physical Name Generation**:
- `PhysicalNameGenerator` (@Component): Main service class
- `TokenizerType` enum: Tokenizer selection (GREEDY, OPTIMAL)
- `NamingConvention` enum: 6 naming conventions (CAMEL_CASE, PASCAL_CASE, SNAKE_CASE, KEBAB_CASE, SNAKE_CASE_UPPER, KEBAB_CASE_UPPER)
- `PhysicalNameResult` record: Generation results with tokenMappings showing conversion details

## License

Project is licensed under Apache License 2.0.