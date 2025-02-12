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

package cherry.pname.processor;

import cherry.pname.caseform.CaseForm;
import cherry.pname.tokenizer.Token;
import cherry.pname.tokenizer.TokenizerBuilder;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

@Component
public class ProcessorBuilderImpl implements ProcessorBuilder {

    private final ApplicationContext appctx;

    private final TokenizerBuilder tokenizerBuilder;

    private final CaseForm caseform;

    public ProcessorBuilderImpl(
            ApplicationContext appctx,
            TokenizerBuilder tokenizerBuilder,
            CaseForm caseform
    ) {
        this.appctx = appctx;
        this.tokenizerBuilder = tokenizerBuilder;
        this.caseform = caseform;
    }

    @Override
    public Processor build(Map<String, List<String>> dict, PnameType pnameType) {
        return appctx.getBean(
                Processor.class,
                tokenizerBuilder.build(dict),
                getPnameFunc(pnameType)
        );
    }

    private Function<List<Token>, String> getPnameFunc(PnameType pnameType) {
        return switch (pnameType) {
            case UPPER_SNAKE -> caseform::toUpperSnake;
            case LOWER_SNAKE -> caseform::toLowerSnake;
            case UPPER_CAMEL -> caseform::toUpperCamel;
            case LOWER_CAMEL -> caseform::toLowerCamel;
            case UPPER_KEBAB -> caseform::toUpperKebab;
            case LOWER_KEBAB -> caseform::toLowerKebab;
        };
    }

}
