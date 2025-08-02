# Physical Name Generator (java-pname)

日本語の論理名から英数字の物理名を生成するJavaツールです。データベース、API、コードで使用する識別子を生成します。

## 概要

このプロジェクトは、辞書ベースの変換とローマ字変換フォールバックを使用して、日本語のビジネス用語から英数字の識別子を生成します。様々な開発ニーズに対応するため、複数のトークン化戦略と命名規則をサポートしています。

## 機能

- **多形式辞書サポート**: CSV、TSV、JSON辞書形式
- **高度なトークン化**: 貪欲最長一致および最適選択アルゴリズム
- **日本語テキスト処理**: KuromojiとICU4Jを使用した形態素解析とローマ字変換
- **複数の命名規則**: camelCase、PascalCase、snake_case、SNAKE_CASE_UPPER、kebab-case、KEBAB-CASE_UPPER等10種類
- **CLI インターフェース**: バッチ処理機能付きコマンドラインツール
- **Web インターフェース**: REST API + 直感的なWebUI（辞書アップロード対応）
- **REST API**: 他システムとの連携用包括的HTTP API
- **ファイル処理**: 大規模操作用の入出力ファイルサポート

## クイックスタート

### 前提条件

- Java 21以降
- Gradle（ラッパー経由で含まれています）

### ビルド

```bash
# リポジトリをクローン
git clone <repository-url>
cd java-pname

# 全モジュールをビルド
./gradlew build

# テストを実行
./gradlew test
```

### CLI使用方法

#### 基本的な使用方法

```bash
# 単一の論理名を変換
./gradlew :pname-cli:bootRun --args="--dictionary=dict.csv 顧客管理システム"

# 特定のオプションで複数の名前を変換
./gradlew :pname-cli:bootRun --args="--dictionary=dict.csv --tokenizer=OPTIMAL --naming=LOWER_SNAKE 顧客管理 注文処理"
```

### Web UI使用方法

```bash
# Webアプリケーションを起動
./gradlew :pname-web:bootRun

# ブラウザで http://localhost:8080 にアクセス
# - 辞書ファイルのアップロード
# - リアルタイム物理名生成
# - トークン分解結果の表示
```

### REST API使用方法

Webアプリケーションはプログラムからのアクセス用にREST APIも提供しています。詳細は[API_REFERENCE.md](API_REFERENCE.md)を参照してください。

**インタラクティブAPIドキュメント:**
- **Swagger UI**: http://localhost:8080/swagger-ui/index.html (Webアプリ実行時)
- **OpenAPI JSON**: http://localhost:8080/v3/api-docs

**API使用例:**
```bash
# API経由で物理名生成
curl -X POST http://localhost:8080/api/generate \
  -H "Content-Type: application/json" \
  -d '{
    "logicalName": "顧客管理システム",
    "namingConvention": "LOWER_SNAKE"
  }'
```

#### バッチ処理

```bash
# 入出力ファイルでファイル処理
./gradlew :pname-cli:bootRun --args="--dictionary=dict.csv --input=input.txt --output=output.txt"

# トークン化の詳細を表示する詳細出力
./gradlew :pname-cli:bootRun --args="--dictionary=dict.csv --input=input.txt --verbose"
```

#### コマンドラインオプション

| オプション | 説明 | デフォルト |
|-----------|------|-----------|
| `--help` | ヘルプメッセージを表示 | - |
| `--dictionary=<file>` | 辞書ファイルパス | - |
| `--format=<format>` | 辞書形式 (CSV, TSV, JSON) | CSV |
| `--tokenizer=<type>` | トークナイザータイプ (GREEDY, OPTIMAL) | OPTIMAL |
| `--naming=<convention>` | 命名規則 (CAMEL, PASCAL, LOWER_CAMEL, UPPER_CAMEL, SNAKE, LOWER_SNAKE, UPPER_SNAKE, KEBAB, LOWER_KEBAB, UPPER_KEBAB) | LOWER_CAMEL |
| `--input=<file>` | 論理名を含む入力ファイル | - |
| `--output=<file>` | 結果用出力ファイル | - |
| `--verbose` | 詳細な変換情報を表示 | false |
| `--quiet` | 物理名のみを表示 | false |

## 辞書形式

### CSV形式

```csv
顧客,customer client
注文,order
商品,product item
```

### TSV形式

```tsv
顧客	customer client
注文	order
商品	product item
```

### JSON形式

```json
{
  "顧客": ["customer", "client"],
  "注文": ["order"],
  "商品": ["product", "item"]
}
```

## 使用例

### 入力ファイル処理

入力ファイル `logical_names.txt` を作成:
```
顧客管理システム
注文処理機能
商品マスタ管理
```

バッチ変換を実行:
```bash
./gradlew :pname-cli:bootRun --args="--dictionary=business_dict.csv --input=logical_names.txt --output=physical_names.txt --naming=LOWER_SNAKE"
```

出力 (`physical_names.txt`):
```
顧客管理システム -> customer_management_system
注文処理機能 -> order_processing_function
商品マスタ管理 -> product_master_management
```

### 詳細出力

```bash
./gradlew :pname-cli:bootRun --args="--dictionary=dict.csv --verbose 顧客管理"
```

出力:
```
論理名: 顧客管理
物理名: customerManagement
トークン分解:
  顧客=>customer
  管理=>management
```

## アーキテクチャ

### マルチモジュール構造

- **pname-main**: コア変換ロジックとSpringコンポーネント
- **pname-cli**: コマンドラインインターフェース（実装済み）
- **pname-web**: Web APIとThymeleafフロントエンド（実装済み）

### 主要コンポーネント

- **トークン化**: 最適な単語分割のための複数アルゴリズム
- **辞書読み込み**: 様々な辞書形式のサポート
- **ローマ字化**: KuromojiとICU4Jを使用した日本語テキスト変換
- **物理名生成**: 設定可能な命名規則変換

## 開発

### プロジェクト構造

```
java-pname/
├── pname-main/          # コアロジック
│   ├── tokenize/        # トークン化アルゴリズム
│   ├── dictionary/      # 辞書ローダー
│   └── romaji/          # 日本語ローマ字変換
├── pname-cli/           # CLIインターフェース
└── pname-web/           # Webインターフェース（実装済み）
```

### テストの実行

```bash
# 全テスト
./gradlew test

# 特定のモジュール
./gradlew :pname-main:test
./gradlew :pname-cli:test
./gradlew :pname-web:test
```

### 依存関係

- Spring Boot 3.5.4
- Kuromoji IPADIC 0.9.0（日本語形態素解析）
- ICU4J 76.1（Unicode処理とローマ字変換）
- Apache Commons CSV 1.14.1
- Jackson Databind（JSON処理）
- Google Guava 33.4.8-jre

## ライセンス

このプロジェクトはApache License 2.0の下でライセンスされています - 詳細は[LICENSE](LICENSE)ファイルを参照してください。

## 貢献

1. リポジトリをフォーク
2. フィーチャーブランチを作成
3. 変更を行う
4. 新機能にテストを追加
5. テストスイートを実行
6. プルリクエストを提出

## ロードマップ

- [x] コアトークン化とローマ字変換
- [x] 辞書読み込み（CSV、TSV、JSON）
- [x] バッチ処理機能付きCLIインターフェース
- [x] RESTエンドポイント付きWeb API
- [x] Thymeleaf + Bootstrapベースのwebインターフェース
- [x] 辞書ファイルアップロード機能
- [x] リアルタイム物理名生成とトークン分解表示
- [x] 包括的なAPIドキュメント
- [ ] パフォーマンス最適化
- [ ] 追加辞書形式
- [x] OpenAPI/Swagger統合