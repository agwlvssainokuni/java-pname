/*
 * Copyright 2017 agwlvssainokuni
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

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.ExitCodeGenerator;
import org.springframework.stereotype.Component;

@Component
public class CmdHandler implements ApplicationRunner, ExitCodeGenerator {

	private Integer exitCode = null;

	@Override
	public synchronized void run(ApplicationArguments args) throws Exception {
		exitCode = 0;
		notifyAll();
	}

	@Override
	public synchronized int getExitCode() {
		while (true) {
			if (exitCode != null) {
				return exitCode.intValue();
			}
			try {
				wait();
			} catch (InterruptedException ex) {
				// NOTHING TO DO
			}
		}
	}

}
