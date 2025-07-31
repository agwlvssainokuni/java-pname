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
 * トークナイザーの種類を表すenum
 */
public enum TokenizerType {
    /**
     * 前方最長マッチ方式
     * 左から右へ順次、辞書で最も長くマッチする単語を選択する
     */
    GREEDY,

    /**
     * 最適分割選択方式
     * 考え得る分割パターンのうち最も適当な分け方を選択する
     * 評価基準：未知語長最小化 → 分割数最小化 → 辞書語数最大化 → 未知語数最小化
     */
    OPTIMAL
}