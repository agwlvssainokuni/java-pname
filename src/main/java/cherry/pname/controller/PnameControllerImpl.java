/*
 * Copyright 2017 agwlvssainokuni
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

package cherry.pname.controller;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import cherry.pname.caseform.CaseForm;
import cherry.pname.dict.DictLoader;
import cherry.pname.processor.Processor;
import cherry.pname.tokenizer.TokenizerBuilder;

import com.google.common.collect.Maps;

@RestController
public class PnameControllerImpl implements PnameController {

	@Autowired
	private DictLoader loader;

	@Autowired
	private TokenizerBuilder builder;

	@Autowired
	private CaseForm caseform;

	@Autowired
	private Processor processor;

	private Map<String, List<String>> dictMap = Maps.newHashMap();

	@Override
	public boolean register(String dict) {
		try (Reader r = new StringReader(dict)) {
			dictMap = loader.load(r, false, " ", true);
			return true;
		} catch (IOException ex) {
			throw new IllegalArgumentException(ex);
		}
	}

	@Override
	public List<Result> generate(String ln) {
		return processor.process(builder.build(dictMap), caseform::toLowerCamel, ln, true).stream()
				.map(r -> new Result(r.getLname(), r.getPname(), r.getDesc())).collect(Collectors.toList());
	}

}
