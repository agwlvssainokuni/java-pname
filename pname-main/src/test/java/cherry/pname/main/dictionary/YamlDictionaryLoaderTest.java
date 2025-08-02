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

class YamlDictionaryLoaderTest {

    private YamlDictionaryLoader loader;

    @BeforeEach
    void setUp() {
        loader = new YamlDictionaryLoader();
    }

    @Test
    void load_validYaml_returnsDictionary() throws IOException {
        String yamlContent = """
                顧客:
                  - customer
                  - client
                注文:
                  - order
                商品:
                  - product
                  - item
                """;

        Map<String, List<String>> result = loader.load(yamlContent);

        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals(List.of("customer", "client"), result.get("顧客"));
        assertEquals(List.of("order"), result.get("注文"));
        assertEquals(List.of("product", "item"), result.get("商品"));
    }

    @Test
    void load_singleValueYaml_returnsDictionary() throws IOException {
        String yamlContent = """
                顧客: customer
                注文: order
                """;

        Map<String, List<String>> result = loader.load(yamlContent);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(List.of("customer"), result.get("顧客"));
        assertEquals(List.of("order"), result.get("注文"));
    }

    @Test
    void load_mixedFormatYaml_returnsDictionary() throws IOException {
        String yamlContent = """
                顧客:
                  - customer
                  - client
                注文: order
                商品:
                  - product
                """;

        Map<String, List<String>> result = loader.load(yamlContent);

        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals(List.of("customer", "client"), result.get("顧客"));
        assertEquals(List.of("order"), result.get("注文"));
        assertEquals(List.of("product"), result.get("商品"));
    }

    @Test
    void load_yamlWithWhitespace_trimmedValues() throws IOException {
        String yamlContent = """
                " 顧客 ":
                  - " customer "
                  - " client "
                " 注文 ": " order "
                """;

        Map<String, List<String>> result = loader.load(yamlContent);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(List.of("customer", "client"), result.get("顧客"));
        assertEquals(List.of("order"), result.get("注文"));
    }

    @Test
    void load_yamlWithEmptyValues_filteredOut() throws IOException {
        String yamlContent = """
                顧客:
                  - customer
                  - ""
                  - client
                注文:
                  - ""
                商品: ""
                """;

        Map<String, List<String>> result = loader.load(yamlContent);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(List.of("customer", "client"), result.get("顧客"));
        assertFalse(result.containsKey("注文"));
        assertFalse(result.containsKey("商品"));
    }

    @Test
    void load_emptyString_returnsEmptyMap() throws IOException {
        Map<String, List<String>> result = loader.load("");
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void load_nullString_returnsEmptyMap() throws IOException {
        Map<String, List<String>> result = loader.load(null);
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void load_whitespaceOnlyString_returnsEmptyMap() throws IOException {
        Map<String, List<String>> result = loader.load("   \n\t   ");
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void load_invalidYaml_throwsIOException() {
        String invalidYaml = """
                顧客:
                  - customer
                 - invalid indentation
                """;

        IOException exception = assertThrows(IOException.class, () -> loader.load(invalidYaml));
        assertTrue(exception.getMessage().contains("Failed to parse YAML dictionary"));
    }

    @Test
    void load_yamlWithComments_ignoresComments() throws IOException {
        String yamlContent = """
                # This is a comment
                顧客: # inline comment
                  - customer
                  - client
                # Another comment
                注文:
                  - order
                """;

        Map<String, List<String>> result = loader.load(yamlContent);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(List.of("customer", "client"), result.get("顧客"));
        assertEquals(List.of("order"), result.get("注文"));
    }

    @Test
    void load_complexYamlStructure_extractsCorrectly() throws IOException {
        String yamlContent = """
                システム:
                  - system
                  - sys
                データベース:
                  - database
                  - db
                  - data_base
                ユーザー:
                  - user
                """;

        Map<String, List<String>> result = loader.load(yamlContent);

        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals(List.of("system", "sys"), result.get("システム"));
        assertEquals(List.of("database", "db", "data_base"), result.get("データベース"));
        assertEquals(List.of("user"), result.get("ユーザー"));
    }
}