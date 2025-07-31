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

import java.util.Arrays;
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
        dictionary.put("顧客", Arrays.asList("customer", "client"));
        dictionary.put("注文", Arrays.asList("order"));
        dictionary.put("商品", Arrays.asList("product", "item"));
        dictionary.put("管理", Arrays.asList("management", "admin"));
        dictionary.put("システム", Arrays.asList("system"));
        dictionary.put("情報", Arrays.asList("information", "info"));
        dictionary.put("データ", Arrays.asList("data"));
        dictionary.put("マスタ", Arrays.asList("master"));
        
        // 単語の組み合わせテスト用
        dictionary.put("売上", Arrays.asList("sales", "revenue"));
        dictionary.put("明細", Arrays.asList("detail", "line"));
        dictionary.put("番号", Arrays.asList("number", "no"));
        dictionary.put("コード", Arrays.asList("code"));
        dictionary.put("名前", Arrays.asList("name"));
        dictionary.put("名称", Arrays.asList("name", "title"));
        
        // 部分マッチテスト用（より長い単語も登録）
        dictionary.put("顧客管理", Arrays.asList("customer_management", "crm"));
        dictionary.put("商品管理", Arrays.asList("product_management"));
        dictionary.put("注文管理", Arrays.asList("order_management"));
        
        // 日付・時間関連
        dictionary.put("日付", Arrays.asList("date"));
        dictionary.put("時刻", Arrays.asList("time"));
        dictionary.put("年月日", Arrays.asList("date"));
        
        // 数値関連
        dictionary.put("金額", Arrays.asList("amount", "price"));
        dictionary.put("数量", Arrays.asList("quantity", "qty"));
        
        return dictionary;
    }
}
