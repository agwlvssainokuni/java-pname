/*
 * Copyright 2017,2018 agwlvssainokuni
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

import static java.text.MessageFormat.format;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import cherry.pname.tokenizer.Token;
import cherry.pname.tokenizer.Tokenizer;

@Component
@Lazy(true)
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class ProcessorImpl implements Processor {

	private final Logger logger = LoggerFactory.getLogger(getClass());

	private final Tokenizer tokenizer;

	private final Function<List<Token>, String> pnfunc;

	@Autowired
	public ProcessorImpl(Tokenizer tokenizer, Function<List<Token>, String> pnfunc) {
		super();
		this.tokenizer = tokenizer;
		this.pnfunc = pnfunc;
	}

	@Override
	public Result process(String lname) {
		List<Token> token = tokenizer.tokenize(lname);
		String pname = pnfunc.apply(token);
		List<String> desc = token.stream().map(this::getDesc).collect(Collectors.toList());

		if (logger.isInfoEnabled()) {
			logger.info("論理名: {}", lname);
			for (Token tk : token) {
				if (tk.isOk()) {
					logger.info("  単語: {} => {}", tk.getWord(), tk.getName());
				} else {
					logger.info("  未知: {}", tk.getWord());
				}
			}
			logger.info("物理名: {}", pname);
		}

		return new Result(lname, pname, desc);
	}

	private String getDesc(Token token) {
		if (token.isOk()) {
			return format("{0}=>{1}", token.getWord(), token.getName());
		} else {
			return format("{0}=*", token.getWord(), token.getName());
		}
	}

}
