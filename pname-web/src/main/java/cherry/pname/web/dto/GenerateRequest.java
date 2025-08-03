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

package cherry.pname.web.dto;

/**
 * 物理名生成リクエストDTO
 */
public class GenerateRequest {
    
    private String logicalName;
    private String tokenizerType = "OPTIMAL";
    private String namingConvention = "LOWER_CAMEL";
    private String dictionaryData;
    private String dictionaryFormat = "CSV";
    private boolean enableFallback = true;

    public GenerateRequest() {
    }

    public GenerateRequest(String logicalName, String tokenizerType, String namingConvention) {
        this.logicalName = logicalName;
        this.tokenizerType = tokenizerType;
        this.namingConvention = namingConvention;
    }

    public String getLogicalName() {
        return logicalName;
    }

    public void setLogicalName(String logicalName) {
        this.logicalName = logicalName;
    }

    public String getTokenizerType() {
        return tokenizerType;
    }

    public void setTokenizerType(String tokenizerType) {
        this.tokenizerType = tokenizerType;
    }

    public String getNamingConvention() {
        return namingConvention;
    }

    public void setNamingConvention(String namingConvention) {
        this.namingConvention = namingConvention;
    }

    public String getDictionaryData() {
        return dictionaryData;
    }

    public void setDictionaryData(String dictionaryData) {
        this.dictionaryData = dictionaryData;
    }

    public String getDictionaryFormat() {
        return dictionaryFormat;
    }

    public void setDictionaryFormat(String dictionaryFormat) {
        this.dictionaryFormat = dictionaryFormat;
    }

    public boolean isEnableFallback() {
        return enableFallback;
    }

    public void setEnableFallback(boolean enableFallback) {
        this.enableFallback = enableFallback;
    }
}
