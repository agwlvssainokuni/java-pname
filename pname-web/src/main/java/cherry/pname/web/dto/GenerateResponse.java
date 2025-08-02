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

import cherry.pname.main.PhysicalNameResult;

import java.util.List;

/**
 * 物理名生成レスポンスDTO
 */
public class GenerateResponse {
    
    private boolean success;
    private String logicalName;
    private String physicalName;
    private List<String> tokenMappings;
    private String errorMessage;

    public GenerateResponse() {
    }

    private GenerateResponse(boolean success, String logicalName, String physicalName, 
                            List<String> tokenMappings, String errorMessage) {
        this.success = success;
        this.logicalName = logicalName;
        this.physicalName = physicalName;
        this.tokenMappings = tokenMappings;
        this.errorMessage = errorMessage;
    }

    public static GenerateResponse fromResult(PhysicalNameResult result) {
        return new GenerateResponse(true, result.logicalName(), result.physicalName(), 
                                  result.tokenMappings(), null);
    }

    public static GenerateResponse error(String errorMessage) {
        return new GenerateResponse(false, null, null, null, errorMessage);
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getLogicalName() {
        return logicalName;
    }

    public void setLogicalName(String logicalName) {
        this.logicalName = logicalName;
    }

    public String getPhysicalName() {
        return physicalName;
    }

    public void setPhysicalName(String physicalName) {
        this.physicalName = physicalName;
    }

    public List<String> getTokenMappings() {
        return tokenMappings;
    }

    public void setTokenMappings(List<String> tokenMappings) {
        this.tokenMappings = tokenMappings;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}
