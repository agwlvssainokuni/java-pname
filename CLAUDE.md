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
├── pname-cli/           # CLI interface
└── pname-web/           # Web API + React frontend
```

## Dependencies

- Spring Boot 3.5.4 (for pname-cli and pname-web)
- Apache Commons CSV 1.14.1
- Google Guava 33.4.8-jre
- JUnit 5 for testing

## Architecture Notes

- Package structure follows `cherry.pname.*` convention
- pname-main contains framework-agnostic conversion logic
- pname-cli and pname-web depend on pname-main
- Build configuration supports both executable applications (CLI/Web) and library (main)

## License

Project is licensed under Apache License 2.0.