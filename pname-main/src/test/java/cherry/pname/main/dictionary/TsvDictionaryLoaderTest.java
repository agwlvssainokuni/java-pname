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
 * TsvDictionaryLoaderのテストクラス
 */
class TsvDictionaryLoaderTest {

    private TsvDictionaryLoader loader;

    @BeforeEach
    void setUp() {
        loader = new TsvDictionaryLoader();
    }

    @Test
    void testBasicTsvLoad() throws IOException {
        String tsvData = """
                顧客\tcustomer client
                注文\torder
                商品\tproduct item
                管理\tmanagement admin
                システム\tsystem
                """;

        Map<String, List<String>> dictionary = loader.load(tsvData);

        assertEquals(5, dictionary.size());
        assertEquals(List.of("customer", "client"), dictionary.get("顧客"));
        assertEquals(List.of("order"), dictionary.get("注文"));
        assertEquals(List.of("product", "item"), dictionary.get("商品"));
        assertEquals(List.of("management", "admin"), dictionary.get("管理"));
        assertEquals(List.of("system"), dictionary.get("システム"));
    }

    @Test
    void testSinglePhysicalName() throws IOException {
        String tsvData = """
                データ\tdata
                情報\tinformation
                """;

        Map<String, List<String>> dictionary = loader.load(tsvData);

        assertEquals(2, dictionary.size());
        assertEquals(List.of("data"), dictionary.get("データ"));
        assertEquals(List.of("information"), dictionary.get("情報"));
    }

    @Test
    void testMultiplePhysicalNames() throws IOException {
        String tsvData = """
                顧客管理\tcustomer_management crm client_management
                売上明細\tsales_detail revenue_line order_line
                """;

        Map<String, List<String>> dictionary = loader.load(tsvData);

        assertEquals(2, dictionary.size());
        assertEquals(List.of("customer_management", "crm", "client_management"), dictionary.get("顧客管理"));
        assertEquals(List.of("sales_detail", "revenue_line", "order_line"), dictionary.get("売上明細"));
    }

    @Test
    void testEmptyLines() throws IOException {
        String tsvData = """
                顧客\tcustomer
                \tempty_logical
                商品\t
                \t
                注文\torder
                """;

        Map<String, List<String>> dictionary = loader.load(tsvData);

        assertEquals(2, dictionary.size());
        assertEquals(List.of("customer"), dictionary.get("顧客"));
        assertEquals(List.of("order"), dictionary.get("注文"));
        assertFalse(dictionary.containsKey(""));
        assertFalse(dictionary.containsKey("商品"));
    }

    @Test
    void testWhitespaceHandling() throws IOException {
        String tsvData = """
                  顧客  \t  customer   client  
                 注文 \t order 
                """;

        Map<String, List<String>> dictionary = loader.load(tsvData);

        assertEquals(2, dictionary.size());
        assertEquals(List.of("customer", "client"), dictionary.get("顧客"));
        assertEquals(List.of("order"), dictionary.get("注文"));
    }

    @Test
    void testIncompleteTsvRecords() throws IOException {
        String tsvData = """
                顧客\tcustomer
                注文
                商品\tproduct item
                """;

        Map<String, List<String>> dictionary = loader.load(tsvData);

        assertEquals(2, dictionary.size());
        assertEquals(List.of("customer"), dictionary.get("顧客"));
        assertEquals(List.of("product", "item"), dictionary.get("商品"));
        assertFalse(dictionary.containsKey("注文"));
    }

    @Test
    void testEmptySource() throws IOException {
        String tsvData = "";

        Map<String, List<String>> dictionary = loader.load(tsvData);

        assertTrue(dictionary.isEmpty());
    }

    @Test
    void testTabsInData() throws IOException {
        String tsvData = """
                年月日\tdate
                金額\tamount price
                """;

        Map<String, List<String>> dictionary = loader.load(tsvData);

        assertEquals(2, dictionary.size());
        assertEquals(List.of("date"), dictionary.get("年月日"));
        assertEquals(List.of("amount", "price"), dictionary.get("金額"));
    }
}
