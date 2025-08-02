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

package cherry.pname.main.dictionary;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JsonDictionaryLoaderのテストクラス
 */
class JsonDictionaryLoaderTest {

    private JsonDictionaryLoader loader;

    @BeforeEach
    void setUp() {
        loader = new JsonDictionaryLoader();
    }

    @Test
    void testBasicJsonLoad() throws IOException {
        String jsonData = """
                {
                  "顧客": ["customer", "client"],
                  "注文": ["order"],
                  "商品": ["product", "item"],
                  "管理": ["management", "admin"],
                  "システム": ["system"]
                }
                """;

        Map<String, List<String>> dictionary = loader.load(jsonData);

        assertEquals(5, dictionary.size());
        assertEquals(List.of("customer", "client"), dictionary.get("顧客"));
        assertEquals(List.of("order"), dictionary.get("注文"));
        assertEquals(List.of("product", "item"), dictionary.get("商品"));
        assertEquals(List.of("management", "admin"), dictionary.get("管理"));
        assertEquals(List.of("system"), dictionary.get("システム"));
    }

    @Test
    void testSinglePhysicalName() throws IOException {
        String jsonData = """
                {
                  "データ": ["data"],
                  "情報": ["information"]
                }
                """;

        Map<String, List<String>> dictionary = loader.load(jsonData);

        assertEquals(2, dictionary.size());
        assertEquals(List.of("data"), dictionary.get("データ"));
        assertEquals(List.of("information"), dictionary.get("情報"));
    }

    @Test
    void testMultiplePhysicalNames() throws IOException {
        String jsonData = """
                {
                  "顧客管理": ["customer_management", "crm", "client_management"],
                  "売上明細": ["sales_detail", "revenue_line", "order_line"]
                }
                """;

        Map<String, List<String>> dictionary = loader.load(jsonData);

        assertEquals(2, dictionary.size());
        assertEquals(List.of("customer_management", "crm", "client_management"), dictionary.get("顧客管理"));
        assertEquals(List.of("sales_detail", "revenue_line", "order_line"), dictionary.get("売上明細"));
    }

    @Test
    void testEmptyAndNullValues() throws IOException {
        String jsonData = """
                {
                  "顧客": ["customer"],
                  "": ["empty_key"],
                  "商品": [],
                  "注文": ["order"],
                  "管理": ["", "management", " ", "admin"],
                  "システム": null
                }
                """;

        Map<String, List<String>> dictionary = loader.load(jsonData);

        assertEquals(3, dictionary.size());
        assertEquals(List.of("customer"), dictionary.get("顧客"));
        assertEquals(List.of("order"), dictionary.get("注文"));
        assertEquals(List.of("management", "admin"), dictionary.get("管理"));
        assertFalse(dictionary.containsKey(""));
        assertFalse(dictionary.containsKey("商品"));
        assertFalse(dictionary.containsKey("システム"));
    }

    @Test
    void testWhitespaceHandling() throws IOException {
        String jsonData = """
                {
                  "  顧客  ": ["  customer  ", "  client  "],
                  " 注文 ": [" order "]
                }
                """;

        Map<String, List<String>> dictionary = loader.load(jsonData);

        assertEquals(2, dictionary.size());
        assertEquals(List.of("customer", "client"), dictionary.get("顧客"));
        assertEquals(List.of("order"), dictionary.get("注文"));
    }

    @Test
    void testEmptySource() throws IOException {
        String jsonData = "";

        Map<String, List<String>> dictionary = loader.load(jsonData);

        assertTrue(dictionary.isEmpty());
    }

    @Test
    void testNullSource() throws IOException {
        Map<String, List<String>> dictionary = loader.load(null);

        assertTrue(dictionary.isEmpty());
    }

    @Test
    void testEmptyJsonObject() throws IOException {
        String jsonData = "{}";

        Map<String, List<String>> dictionary = loader.load(jsonData);

        assertTrue(dictionary.isEmpty());
    }

    @Test
    void testInvalidJson() {
        String jsonData = """
                {
                  "顧客": ["customer",
                  "注文": "order"
                """;

        assertThrows(IOException.class, () -> loader.load(jsonData));
    }

    @Test
    void testCompactJson() throws IOException {
        String jsonData = """
                {"顧客":["customer","client"],"注文":["order"],"商品":["product","item"]}
                """;

        Map<String, List<String>> dictionary = loader.load(jsonData);

        assertEquals(3, dictionary.size());
        assertEquals(List.of("customer", "client"), dictionary.get("顧客"));
        assertEquals(List.of("order"), dictionary.get("注文"));
        assertEquals(List.of("product", "item"), dictionary.get("商品"));
    }

    @Test
    void testUnicodeCharacters() throws IOException {
        String jsonData = """
                {
                  "年月日": ["date"],
                  "金額": ["amount", "price"],
                  "数量": ["quantity", "qty"]
                }
                """;

        Map<String, List<String>> dictionary = loader.load(jsonData);

        assertEquals(3, dictionary.size());
        assertEquals(List.of("date"), dictionary.get("年月日"));
        assertEquals(List.of("amount", "price"), dictionary.get("金額"));
        assertEquals(List.of("quantity", "qty"), dictionary.get("数量"));
    }
}
