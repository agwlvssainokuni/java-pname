/*
 * Copyright 2017,2019 agwlvssainokuni
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

package cherry.pname.cmd;

import java.nio.charset.Charset;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.io.Resource;

import cherry.pname.processor.PnameType;

@Configuration
@PropertySource({ "classpath:pname-cmd.properties" })
@ConfigurationProperties
public class CmdConfig {

	private Charset charset;

	private Resource dict;

	private String delim;

	private boolean tsvout;

	private boolean desc;

	private PnameType type;

	@Override
	public String toString() {
		return ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
	}

	public Charset getCharset() {
		return charset;
	}

	public void setCharset(Charset charset) {
		this.charset = charset;
	}

	public Resource getDict() {
		return dict;
	}

	public void setDict(Resource dict) {
		this.dict = dict;
	}

	public String getDelim() {
		return delim;
	}

	public void setDelim(String delim) {
		this.delim = delim;
	}

	public boolean isTsvout() {
		return tsvout;
	}

	public void setTsvout(boolean tsvout) {
		this.tsvout = tsvout;
	}

	public boolean isDesc() {
		return desc;
	}

	public void setDesc(boolean desc) {
		this.desc = desc;
	}

	public PnameType getType() {
		return type;
	}

	public void setType(PnameType type) {
		this.type = type;
	}

}
