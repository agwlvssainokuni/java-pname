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
 * CsvDictionaryLoaderのテストクラス
 */
class CsvDictionaryLoaderTest {

    private CsvDictionaryLoader loader;

    @BeforeEach
    void setUp() {
        loader = new CsvDictionaryLoader();
    }

    @Test
    void testBasicCsvLoad() throws IOException {
        String csvData = """
                顧客,customer client
                注文,order
                商品,product item
                管理,management admin
                システム,system
                """;

        Map<String, List<String>> dictionary = loader.load(csvData);

        assertEquals(5, dictionary.size());
        assertEquals(List.of("customer", "client"), dictionary.get("顧客"));
        assertEquals(List.of("order"), dictionary.get("注文"));
        assertEquals(List.of("product", "item"), dictionary.get("商品"));
        assertEquals(List.of("management", "admin"), dictionary.get("管理"));
        assertEquals(List.of("system"), dictionary.get("システム"));
    }

    @Test
    void testSinglePhysicalName() throws IOException {
        String csvData = """
                データ,data
                情報,information
                """;

        Map<String, List<String>> dictionary = loader.load(csvData);

        assertEquals(2, dictionary.size());
        assertEquals(List.of("data"), dictionary.get("データ"));
        assertEquals(List.of("information"), dictionary.get("情報"));
    }

    @Test
    void testMultiplePhysicalNames() throws IOException {
        String csvData = """
                顧客管理,customer_management crm client_management
                売上明細,sales_detail revenue_line order_line
                """;

        Map<String, List<String>> dictionary = loader.load(csvData);

        assertEquals(2, dictionary.size());
        assertEquals(List.of("customer_management", "crm", "client_management"), dictionary.get("顧客管理"));
        assertEquals(List.of("sales_detail", "revenue_line", "order_line"), dictionary.get("売上明細"));
    }

    @Test
    void testEmptyLines() throws IOException {
        String csvData = """
                顧客,customer
                ,empty_logical
                商品,
                ,
                注文,order
                """;

        Map<String, List<String>> dictionary = loader.load(csvData);

        assertEquals(2, dictionary.size());
        assertEquals(List.of("customer"), dictionary.get("顧客"));
        assertEquals(List.of("order"), dictionary.get("注文"));
        assertFalse(dictionary.containsKey(""));
        assertFalse(dictionary.containsKey("商品"));
    }

    @Test
    void testWhitespaceHandling() throws IOException {
        String csvData = """
                  顧客  ,  customer   client  
                 注文 , order 
                """;

        Map<String, List<String>> dictionary = loader.load(csvData);

        assertEquals(2, dictionary.size());
        assertEquals(List.of("customer", "client"), dictionary.get("顧客"));
        assertEquals(List.of("order"), dictionary.get("注文"));
    }

    @Test
    void testIncompleteCsvRecords() throws IOException {
        String csvData = """
                顧客,customer
                注文
                商品,product item
                """;

        Map<String, List<String>> dictionary = loader.load(csvData);

        assertEquals(2, dictionary.size());
        assertEquals(List.of("customer"), dictionary.get("顧客"));
        assertEquals(List.of("product", "item"), dictionary.get("商品"));
        assertFalse(dictionary.containsKey("注文"));
    }

    @Test
    void testEmptySource() throws IOException {
        String csvData = "";

        Map<String, List<String>> dictionary = loader.load(csvData);

        assertTrue(dictionary.isEmpty());
    }

    @Test
    void testSpecialCharacters() throws IOException {
        String csvData = """
                "顧客,管理","customer_management"
                年月日,date
                """;

        Map<String, List<String>> dictionary = loader.load(csvData);

        assertEquals(2, dictionary.size());
        assertEquals(List.of("customer_management"), dictionary.get("顧客,管理"));
        assertEquals(List.of("date"), dictionary.get("年月日"));
    }
}
