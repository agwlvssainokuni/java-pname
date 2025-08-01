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

import java.util.List;

/**
 * 物理名生成の結果を表すrecord
 *
 * @param logicalName   元の日本語名
 * @param physicalName  生成した物理名
 * @param tokenMappings トークンの変換を「辞書キー=>辞書値」で表した文字列のリスト
 */
public record PhysicalNameResult(
        String logicalName,
        String physicalName,
        List<String> tokenMappings
) {
}