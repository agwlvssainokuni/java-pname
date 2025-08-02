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

package cherry.pname.main;

/**
 * 物理名の命名規則を表すenum
 */
public enum NamingConvention {
    /**
     * lowerCamelCase形式 (例: customerManagement)
     */
    LOWER_CAMEL,

    /**
     * UpperCamelCase（PascalCase）形式 (例: CustomerManagement)
     */
    UPPER_CAMEL,

    /**
     * camelCase形式 (例: customerManagement)
     */
    CAMEL,

    /**
     * PascalCase形式 (例: CustomerManagement)
     */
    PASCAL,

    /**
     * lower_snake_case形式 (例: customer_management)
     */
    LOWER_SNAKE,

    /**
     * UPPER_SNAKE_CASE形式 (例: CUSTOMER_MANAGEMENT)
     */
    UPPER_SNAKE,

    /**
     * lower-kebab-case形式 (例: customer-management)
     */
    LOWER_KEBAB,

    /**
     * UPPER-KEBAB-CASE形式 (例: CUSTOMER-MANAGEMENT)
     */
    UPPER_KEBAB
}
