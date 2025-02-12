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

import cherry.pname.tokenizer.Token;
import cherry.pname.tokenizer.Tokenizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.Function;

import static java.text.MessageFormat.format;

@Component
@Lazy(true)
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class ProcessorImpl implements Processor {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final Tokenizer tokenizer;

    private final Function<List<Token>, String> pnfunc;

    public ProcessorImpl(
            Tokenizer tokenizer,
            Function<List<Token>, String> pnfunc
    ) {
        this.tokenizer = tokenizer;
        this.pnfunc = pnfunc;
    }

    @Override
    public Result process(String lname) {
        var token = tokenizer.tokenize(lname);
        var pname = pnfunc.apply(token);
        var desc = token.stream().map(this::getDesc).toList();

        if (logger.isInfoEnabled()) {
            logger.info("論理名: {}", lname);
            for (Token tk : token) {
                if (tk.ok()) {
                    logger.info("  単語: {} => {}", tk.lnm(), tk.pnm());
                } else {
                    logger.info("  未知: {}", tk.lnm());
                }
            }
            logger.info("物理名: {}", pname);
        }

        return new Result(lname, pname, desc);
    }

    private String getDesc(Token token) {
        if (token.ok()) {
            return format("{0}=>{1}", token.lnm(), token.pnm());
        } else {
            return format("{0}=*", token.lnm(), token.pnm());
        }
    }

}
