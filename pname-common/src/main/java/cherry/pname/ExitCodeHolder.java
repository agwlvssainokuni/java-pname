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

package cherry.pname;

import org.springframework.boot.ExitCodeGenerator;
import org.springframework.stereotype.Component;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class ExitCodeHolder implements ExitCodeGenerator {

    private final AtomicInteger exitCode = new AtomicInteger(0);
    private final CountDownLatch latch = new CountDownLatch(1);

    public void setExitCode(int code) {
        exitCode.set(code);
        latch.countDown();
    }

    @Override
    public int getExitCode() {
        try {
            latch.await();
        } catch (InterruptedException ex) {
            // NOTHING TO DO
        }
        return exitCode.get();
    }

}
