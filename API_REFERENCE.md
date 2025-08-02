# Physical Name Generator API Reference

This document provides detailed reference for the Physical Name Generator REST API.

## Base URL

```
http://localhost:8080/api
```

## Authentication

No authentication is required for the current version of the API.

## Content Type

All API endpoints expect and return JSON data unless otherwise specified.

```
Content-Type: application/json
```

## API Endpoints

### 1. Generate Physical Name

Generates a physical name from a Japanese logical name.

**Endpoint:** `POST /generate`

**Description:** Converts a Japanese logical name into an alphanumeric physical name using specified tokenization and naming convention strategies.

#### Request

**Headers:**
```
Content-Type: application/json
```

**Body Parameters:**

| Parameter | Type | Required | Default | Description |
|-----------|------|----------|---------|-------------|
| `logicalName` | string | Yes | - | Japanese logical name to convert |
| `tokenizerType` | string | No | "OPTIMAL" | Tokenization algorithm ("GREEDY", "OPTIMAL") |
| `namingConvention` | string | No | "LOWER_CAMEL" | Output naming convention |
| `dictionaryData` | string | No | null | Dictionary data as text (CSV/TSV/JSON/YAML format) |
| `dictionaryFormat` | string | No | "CSV" | Dictionary format ("CSV", "TSV", "JSON", "YAML") |

**Naming Convention Options:**
- `CAMEL` - camelCase (e.g., customerManagement)
- `PASCAL` - PascalCase (e.g., CustomerManagement)
- `LOWER_CAMEL` - lowerCamelCase (e.g., customerManagement)
- `UPPER_CAMEL` - UpperCamelCase (e.g., CustomerManagement)
- `SNAKE` - snake_case (e.g., customer_management)
- `LOWER_SNAKE` - lower_snake_case (e.g., customer_management)
- `UPPER_SNAKE` - UPPER_SNAKE_CASE (e.g., CUSTOMER_MANAGEMENT)
- `KEBAB` - kebab-case (e.g., customer-management)
- `LOWER_KEBAB` - lower-kebab-case (e.g., customer-management)
- `UPPER_KEBAB` - UPPER-KEBAB-CASE (e.g., CUSTOMER-MANAGEMENT)

#### Response

**Success Response (200 OK):**

```json
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
```

**Error Response (400 Bad Request):**

```json
{
  "success": false,
  "logicalName": null,
  "physicalName": null,
  "tokenMappings": null,
  "errorMessage": "不正なパラメータです: Invalid naming convention"
}
```

**Error Response (500 Internal Server Error):**

```json
{
  "success": false,
  "logicalName": null,
  "physicalName": null,
  "tokenMappings": null,
  "errorMessage": "辞書の読み込みに失敗しました: Invalid dictionary format"
}
```

#### Example Requests

**Basic Request:**
```bash
curl -X POST http://localhost:8080/api/generate \
  -H "Content-Type: application/json" \
  -d '{
    "logicalName": "顧客管理"
  }'
```

**Request with Custom Settings:**
```bash
curl -X POST http://localhost:8080/api/generate \
  -H "Content-Type: application/json" \
  -d '{
    "logicalName": "顧客管理システム",
    "tokenizerType": "OPTIMAL",
    "namingConvention": "UPPER_SNAKE"
  }'
```

**Request with Inline Dictionary:**
```bash
curl -X POST http://localhost:8080/api/generate \
  -H "Content-Type: application/json" \
  -d '{
    "logicalName": "顧客管理",
    "tokenizerType": "OPTIMAL",
    "namingConvention": "LOWER_CAMEL",
    "dictionaryData": "顧客,customer\n管理,management\nシステム,system",
    "dictionaryFormat": "CSV"
  }'
```

### 2. Upload Dictionary File

Uploads and loads a dictionary file for use in subsequent generation requests.

**Endpoint:** `POST /generate/dictionary`

**Description:** Uploads a dictionary file in CSV, TSV, JSON, or YAML format and loads it into the generator for use in physical name generation.

#### Request

**Headers:**
```
Content-Type: multipart/form-data
```

**Form Parameters:**

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `file` | file | Yes | Dictionary file (CSV/TSV/JSON/YAML) |
| `format` | string | Yes | Dictionary format ("CSV", "TSV", "JSON", "YAML") |

#### Response

**Success Response (200 OK):**
```
辞書ファイルを読み込みました (150エントリ)
```

**Error Response (400 Bad Request):**
```
ファイルが選択されていません
```

or

```
不正な辞書形式です: INVALID_FORMAT
```

**Error Response (500 Internal Server Error):**
```
辞書の読み込みに失敗しました: Invalid file format
```

#### Example Request

```bash
curl -X POST http://localhost:8080/api/generate/dictionary \
  -F "file=@business_terms.csv" \
  -F "format=CSV"
```

### 3. Get Dictionary Information

Retrieves information about the currently loaded dictionary.

**Endpoint:** `GET /generate/dictionary/info`

**Description:** Returns information about whether a dictionary is loaded and how many entries it contains.

#### Request

No parameters required.

#### Response

**Success Response (200 OK):**

When dictionary is loaded:
```
辞書が読み込まれています (150エントリ)
```

When no dictionary is loaded:
```
辞書が読み込まれていません
```

#### Example Request

```bash
curl -X GET http://localhost:8080/api/generate/dictionary/info
```

## Dictionary Formats

### CSV Format

Comma-separated values with logical name and space-separated physical names:

```csv
顧客,customer client
注文,order
商品,product item goods
管理,management admin
システム,system
```

### TSV Format

Tab-separated values with logical name and space-separated physical names:

```tsv
顧客	customer client
注文	order
商品	product item goods
管理	management admin
システム	system
```

### JSON Format

JSON object with logical names as keys and arrays of physical names as values:

```json
{
  "顧客": ["customer", "client"],
  "注文": ["order"],
  "商品": ["product", "item", "goods"],
  "管理": ["management", "admin"],
  "システム": ["system"]
}
```

### YAML Format

YAML format with logical names as keys and arrays or single values as physical names. Supports comments and flexible syntax:

```yaml
# Dictionary for business terms
顧客:
  - customer
  - client
注文: order  # Single value
商品:
  - product
  - item
  - goods
管理:
  - management
  - admin
システム: system
```

**YAML Features:**
- Supports both single values (`注文: order`) and arrays (`顧客: [customer, client]`)
- Comments supported with `#`
- Human-readable indentation-based structure
- More flexible than JSON for manual editing

## Error Handling

The API uses standard HTTP status codes to indicate the success or failure of requests:

- `200 OK` - Request successful
- `400 Bad Request` - Invalid request parameters
- `500 Internal Server Error` - Server-side error

Error responses include a descriptive error message in the response body.

## Rate Limiting

Currently, no rate limiting is implemented. This may be added in future versions.

## Versioning

The current API version is v1. Future versions may be introduced with appropriate versioning schemes.

## Examples

### Complete Workflow Example

1. **Upload a dictionary:**
```bash
curl -X POST http://localhost:8080/api/generate/dictionary \
  -F "file=@business_dict.csv" \
  -F "format=CSV"
```

2. **Check dictionary status:**
```bash
curl -X GET http://localhost:8080/api/generate/dictionary/info
```

3. **Generate physical names:**
```bash
curl -X POST http://localhost:8080/api/generate \
  -H "Content-Type: application/json" \
  -d '{
    "logicalName": "顧客管理システム",
    "tokenizerType": "OPTIMAL",
    "namingConvention": "LOWER_SNAKE"
  }'
```

Expected response:
```json
{
  "success": true,
  "logicalName": "顧客管理システム",
  "physicalName": "customer_management_system",
  "tokenMappings": [
    "顧客=>customer",
    "管理=>management",
    "システム=>system"
  ],
  "errorMessage": null
}
```

### Batch Processing with Different Naming Conventions

```bash
# Generate camelCase
curl -X POST http://localhost:8080/api/generate \
  -H "Content-Type: application/json" \
  -d '{"logicalName": "注文処理", "namingConvention": "LOWER_CAMEL"}'

# Generate PascalCase  
curl -X POST http://localhost:8080/api/generate \
  -H "Content-Type: application/json" \
  -d '{"logicalName": "注文処理", "namingConvention": "UPPER_CAMEL"}'

# Generate snake_case
curl -X POST http://localhost:8080/api/generate \
  -H "Content-Type: application/json" \
  -d '{"logicalName": "注文処理", "namingConvention": "LOWER_SNAKE"}'

# Generate UPPER_SNAKE_CASE
curl -X POST http://localhost:8080/api/generate \
  -H "Content-Type: application/json" \
  -d '{"logicalName": "注文処理", "namingConvention": "UPPER_SNAKE"}'
```
