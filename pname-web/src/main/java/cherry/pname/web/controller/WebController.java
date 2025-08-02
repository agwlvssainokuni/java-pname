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

package cherry.pname.web.controller;

import cherry.pname.main.DictionaryFormat;
import cherry.pname.main.NamingConvention;
import cherry.pname.main.PhysicalNameGenerator;
import cherry.pname.main.TokenizerType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Web画面用コントローラー
 */
@Controller
public class WebController {

    private final PhysicalNameGenerator generator;

    public WebController(PhysicalNameGenerator generator) {
        this.generator = generator;
    }

    /**
     * メイン画面を表示
     *
     * @param model モデル
     * @return テンプレート名
     */
    @GetMapping("/")
    public String index(Model model) {
        // 選択肢を設定
        model.addAttribute("tokenizerTypes", TokenizerType.values());
        model.addAttribute("namingConventions", NamingConvention.values());
        model.addAttribute("dictionaryFormats", DictionaryFormat.values());
        
        // 辞書情報を設定
        model.addAttribute("hasDictionary", generator.hasDictionary());
        model.addAttribute("dictionarySize", generator.getDictionarySize());
        
        return "index";
    }
}
