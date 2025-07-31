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

import java.util.List;

/**
 * トークナイザーの結果を表すレコード
 * 
 * @param word 分割された単語（日本語）
 * @param physicalNames 対応する物理名のリスト（英語）
 * @param isUnknown 未知語フラグ（辞書にない単語の場合true）
 */
public record Token(
    String word,
    List<String> physicalNames,
    boolean isUnknown
) {
}