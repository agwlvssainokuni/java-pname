/*
 * Copyright 2017,2025 agwlvssainokuni
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

package cherry.pname.web;

import cherry.pname.processor.PnameType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;
import java.util.List;

@RequestMapping("/pname")
public interface PnameController {

    @RequestMapping()
    List<Result> generate(@RequestParam() String ln, @RequestParam(required = false) PnameType type);

    @RequestMapping(params = "tsv", produces = "text/tab-separated-values; charset=UTF-8")
    String generateTsv(@RequestParam() String ln, @RequestParam(required = false) PnameType type);

    @RequestMapping(params = {"dicttext"})
    int uploadDictText(@RequestParam("dicttext") String dicttext) throws IOException;

    @RequestMapping(params = {"dictreload"})
    int reloadDict() throws IOException;

    record Result(
            String ln,
            String pn,
            List<String> desc
    ) {
    }

}
