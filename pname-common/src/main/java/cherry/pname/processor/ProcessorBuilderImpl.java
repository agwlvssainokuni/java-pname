/*
 * Copyright 2017,2020 agwlvssainokuni
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

package cherry.pname.processor;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import cherry.pname.caseform.CaseForm;
import cherry.pname.tokenizer.Token;
import cherry.pname.tokenizer.TokenizerBuilder;

@Component
public class ProcessorBuilderImpl implements ProcessorBuilder, ApplicationContextAware {

	private ApplicationContext appctx;

	@Autowired
	private TokenizerBuilder tokenizerBuilder;

	@Autowired
	private CaseForm caseform;

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) {
		this.appctx = applicationContext;
	}

	@Override
	public Processor build(Map<String, List<String>> dict, PnameType pnameType) {
		return appctx.getBean(Processor.class, tokenizerBuilder.build(dict), getPnameFunc(pnameType));
	}

	private Function<List<Token>, String> getPnameFunc(PnameType pnameType) {
		if (pnameType == PnameType.UPPER_SNAKE) {
			return caseform::toUpperSnake;
		} else if (pnameType == PnameType.LOWER_SNAKE) {
			return caseform::toLowerSnake;
		} else if (pnameType == PnameType.UPPER_CAMEL) {
			return caseform::toUpperCamel;
		} else if (pnameType == PnameType.LOWER_CAMEL) {
			return caseform::toLowerCamel;
		} else if (pnameType == PnameType.UPPER_KEBAB) {
			return caseform::toUpperKebab;
		} else if (pnameType == PnameType.LOWER_KEBAB) {
			return caseform::toLowerKebab;
		} else {
			return caseform::toUpperSnake;
		}
	}

}
