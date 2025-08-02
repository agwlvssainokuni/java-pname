# Physical Name Generator (java-pname)

A Java tool for converting Japanese logical names to alphanumeric physical names. Generates identifiers for use in databases, APIs, and code.

## Overview

This project generates alphanumeric identifiers from Japanese business terms using dictionary-based conversion and romanization fallback. It supports multiple tokenization strategies and naming conventions to meet various development needs.

## Features

- **Multi-format Dictionary Support**: CSV, TSV, JSON dictionary formats
- **Advanced Tokenization**: Greedy longest-match and optimal selection algorithms
- **Japanese Text Processing**: Morphological analysis and romanization using Kuromoji and ICU4J
- **Multiple Naming Conventions**: 10 types including camelCase, PascalCase, snake_case, UPPER_SNAKE_CASE, kebab-case, UPPER-KEBAB-CASE, etc.
- **CLI Interface**: Command-line tool with batch processing capabilities
- **Web Interface**: REST API + intuitive Web UI with dictionary upload support
- **REST API**: Comprehensive HTTP API for integration with other systems
- **File Processing**: Input/output file support for large-scale operations

## Quick Start

### Prerequisites

- Java 21 or later
- Gradle (included via wrapper)

### Build

```bash
# Clone repository
git clone <repository-url>
cd java-pname

# Build all modules
./gradlew build

# Run tests
./gradlew test
```

### CLI Usage

#### Basic Usage

```bash
# Convert a single logical name
./gradlew :pname-cli:bootRun --args="--dictionary=dict.csv 顧客管理システム"

# Convert multiple names with specific options
./gradlew :pname-cli:bootRun --args="--dictionary=dict.csv --tokenizer=OPTIMAL --naming=LOWER_SNAKE 顧客管理 注文処理"
```

### Web UI Usage

```bash
# Start the web application
./gradlew :pname-web:bootRun

# Access http://localhost:8080 in your browser
# - Dictionary file upload
# - Real-time physical name generation
# - Token decomposition display
```

### REST API Usage

The web application also provides a REST API for programmatic access. See [API_REFERENCE.md](API_REFERENCE.md) for detailed documentation.

**Interactive API Documentation:**
- **Swagger UI**: http://localhost:8080/swagger-ui/index.html (when web app is running)
- **OpenAPI JSON**: http://localhost:8080/v3/api-docs

**Quick API Example:**
```bash
# Generate physical name via API
curl -X POST http://localhost:8080/api/generate \
  -H "Content-Type: application/json" \
  -d '{
    "logicalName": "顧客管理システム",
    "namingConvention": "LOWER_SNAKE"
  }'
```

#### Batch Processing

```bash
# File processing with input/output files
./gradlew :pname-cli:bootRun --args="--dictionary=dict.csv --input=input.txt --output=output.txt"

# Verbose output showing tokenization details
./gradlew :pname-cli:bootRun --args="--dictionary=dict.csv --input=input.txt --verbose"
```

#### Command Line Options

| Option | Description | Default |
|--------|-------------|---------|
| `--help` | Show help message | - |
| `--dictionary=<file>` | Dictionary file path | - |
| `--format=<format>` | Dictionary format (CSV, TSV, JSON) | CSV |
| `--tokenizer=<type>` | Tokenizer type (GREEDY, OPTIMAL) | OPTIMAL |
| `--naming=<convention>` | Naming convention (CAMEL, PASCAL, LOWER_CAMEL, UPPER_CAMEL, SNAKE, LOWER_SNAKE, UPPER_SNAKE, KEBAB, LOWER_KEBAB, UPPER_KEBAB) | LOWER_CAMEL |
| `--input=<file>` | Input file containing logical names | - |
| `--output=<file>` | Output file for results | - |
| `--verbose` | Show detailed conversion information | false |
| `--quiet` | Show only physical names | false |

## Dictionary Formats

### CSV Format

```csv
顧客,customer client
注文,order
商品,product item
```

### TSV Format

```tsv
顧客	customer client
注文	order
商品	product item
```

### JSON Format

```json
{
  "顧客": ["customer", "client"],
  "注文": ["order"],
  "商品": ["product", "item"]
}
```

## Usage Examples

### Input File Processing

Create input file `logical_names.txt`:
```
顧客管理システム
注文処理機能
商品マスタ管理
```

Execute batch conversion:
```bash
./gradlew :pname-cli:bootRun --args="--dictionary=business_dict.csv --input=logical_names.txt --output=physical_names.txt --naming=LOWER_SNAKE"
```

Output (`physical_names.txt`):
```
顧客管理システム -> customer_management_system
注文処理機能 -> order_processing_function
商品マスタ管理 -> product_master_management
```

### Verbose Output

```bash
./gradlew :pname-cli:bootRun --args="--dictionary=dict.csv --verbose 顧客管理"
```

Output:
```
論理名: 顧客管理
物理名: customerManagement
トークン分解:
  顧客=>customer
  管理=>management
```

## Architecture

### Multi-Module Structure

- **pname-main**: Core conversion logic and Spring components
- **pname-cli**: Command-line interface (implemented)
- **pname-web**: Web API and Thymeleaf frontend (implemented)

### Key Components

- **Tokenization**: Multiple algorithms for optimal word segmentation
- **Dictionary Loading**: Support for various dictionary formats
- **Romanization**: Japanese text conversion using Kuromoji and ICU4J
- **Physical Name Generation**: Configurable naming convention conversion

## Development

### Project Structure

```
java-pname/
├── pname-main/          # Core logic
│   ├── tokenize/        # Tokenization algorithms
│   ├── dictionary/      # Dictionary loaders
│   └── romaji/          # Japanese romanization
├── pname-cli/           # CLI interface
└── pname-web/           # Web interface (implemented)
```

### Running Tests

```bash
# All tests
./gradlew test

# Specific modules
./gradlew :pname-main:test
./gradlew :pname-cli:test
./gradlew :pname-web:test
```

### Dependencies

- Spring Boot 3.5.4
- Kuromoji IPADIC 0.9.0 (Japanese morphological analysis)
- ICU4J 76.1 (Unicode processing and romanization)
- Apache Commons CSV 1.14.1
- Jackson Databind (JSON processing)
- Google Guava 33.4.8-jre

## License

This project is licensed under the Apache License 2.0 - see the [LICENSE](LICENSE) file for details.

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests for new features
5. Run the test suite
6. Submit a pull request

## Roadmap

- [x] Core tokenization and romanization
- [x] Dictionary loading (CSV, TSV, JSON)
- [x] CLI interface with batch processing
- [x] Web API with REST endpoints
- [x] Thymeleaf + Bootstrap-based web interface
- [x] Dictionary file upload functionality
- [x] Real-time physical name generation and token decomposition display
- [x] Comprehensive API documentation
- [ ] Performance optimization
- [ ] Additional dictionary formats
- [x] OpenAPI/Swagger integration