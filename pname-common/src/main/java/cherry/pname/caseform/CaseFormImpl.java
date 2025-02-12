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

package cherry.pname.caseform;

import cherry.pname.tokenizer.Token;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.Function;

@Component
public class CaseFormImpl implements CaseForm {

    @Override
    public String toLowerCamel(List<Token> list) {
        return toCamelCase(list, Character::toLowerCase);
    }

    @Override
    public String toUpperCamel(List<Token> list) {
        return toCamelCase(list, Character::toUpperCase);
    }

    @Override
    public String toLowerSnake(List<Token> list) {
        return toSnakeCase(list, Character::toLowerCase);
    }

    @Override
    public String toUpperSnake(List<Token> list) {
        return toSnakeCase(list, Character::toUpperCase);
    }

    @Override
    public String toLowerKebab(List<Token> list) {
        return toKebabCase(list, Character::toLowerCase);
    }

    @Override
    public String toUpperKebab(List<Token> list) {
        return toKebabCase(list, Character::toUpperCase);
    }

    private String toCamelCase(List<Token> list, Function<Character, Character> func) {

        var pname = list.stream().map(Token::pnm)
                .flatMap(List::stream)
                .filter(StringUtils::isNotBlank)
                .toList();

        var ch = new char[pname.stream().mapToInt(String::length).sum()];
        var index = 0;
        var first = true;
        for (var pn : pname) {
            for (var i = 0; i < pn.length(); i++) {
                if (i == 0) {
                    if (first) {
                        ch[index++] = func.apply(pn.charAt(i));
                        first = false;
                    } else {
                        ch[index++] = Character.toUpperCase(pn.charAt(i));
                    }
                } else {
                    ch[index++] = Character.toLowerCase(pn.charAt(i));
                }
            }
        }
        return new String(ch);
    }

    private String toSnakeCase(List<Token> list, Function<Character, Character> func) {
        return toSnakeKebab(list, func, '_');
    }

    private String toKebabCase(List<Token> list, Function<Character, Character> func) {
        return toSnakeKebab(list, func, '-');
    }

    private String toSnakeKebab(List<Token> list, Function<Character, Character> func, char delim) {

        var pname = list.stream().map(Token::pnm)
                .flatMap(List::stream)
                .filter(StringUtils::isNotBlank)
                .toList();

        var size = pname.stream().mapToInt(String::length).sum() + pname.size() - 1;
        if (size < 0) {
            return "";
        }
        var ch = new char[size];
        var index = 0;
        var first = true;
        for (var pn : pname) {
            if (first) {
                first = false;
            } else {
                ch[index++] = delim;
            }
            for (var i = 0; i < pn.length(); i++) {
                ch[index++] = func.apply(pn.charAt(i));
            }
        }
        return new String(ch);
    }

}
