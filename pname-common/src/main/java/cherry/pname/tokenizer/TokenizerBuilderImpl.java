/*
 * Copyright 2017,2021 agwlvssainokuni
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

package cherry.pname.tokenizer;

import java.util.List;
import java.util.Map;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

@Component
public class TokenizerBuilderImpl implements TokenizerBuilder, ApplicationContextAware {

	private ApplicationContext appctx;

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) {
		this.appctx = applicationContext;
	}

	@Override
	public Tokenizer build(Map<String, List<String>> dict) {
		return appctx.getBean(Tokenizer.class, dict);
	}

}
