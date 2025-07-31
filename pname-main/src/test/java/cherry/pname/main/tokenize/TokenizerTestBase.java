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

package cherry.pname.main.tokenize;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * トークナイザーテスト用のベースクラス
 * テスト用辞書データを提供する
 */
public abstract class TokenizerTestBase {

    /**
     * テスト用の辞書データを作成
     * ビジネス用語を中心とした日本語→英語の辞書
     */
    protected Map<String, List<String>> createTestDictionary() {
        Map<String, List<String>> dictionary = new HashMap<>();

        // 基本的なビジネス用語
        dictionary.put("顧客", List.of("customer", "client"));
        dictionary.put("注文", List.of("order"));
        dictionary.put("商品", List.of("product", "item"));
        dictionary.put("管理", List.of("management", "admin"));
        dictionary.put("システム", List.of("system"));
        dictionary.put("情報", List.of("information", "info"));
        dictionary.put("データ", List.of("data"));
        dictionary.put("マスタ", List.of("master"));

        // 単語の組み合わせテスト用
        dictionary.put("売上", List.of("sales", "revenue"));
        dictionary.put("明細", List.of("detail", "line"));
        dictionary.put("番号", List.of("number", "no"));
        dictionary.put("コード", List.of("code"));
        dictionary.put("名前", List.of("name"));
        dictionary.put("名称", List.of("name", "title"));

        // 部分マッチテスト用（より長い単語も登録）
        dictionary.put("顧客管理", List.of("customer_management", "crm"));
        dictionary.put("商品管理", List.of("product_management"));
        dictionary.put("注文管理", List.of("order_management"));

        // 日付・時間関連
        dictionary.put("日付", List.of("date"));
        dictionary.put("時刻", List.of("time"));
        dictionary.put("年月日", List.of("date"));

        // 数値関連
        dictionary.put("金額", List.of("amount", "price"));
        dictionary.put("数量", List.of("quantity", "qty"));

        return dictionary;
    }
}
