/*
 * Copyright 2021,2025 agwlvssainokuni
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
// ENTRY

import React, {useState} from "react";
import ReactDOM from "react-dom/client";
import {ln2pn} from "./pname-api";

function PnamePanel(props) {
    let [data, setData] = useState("");

    const handleClick = async (event) => {
        let pn = await ln2pn(event.target.value, data);
        setData(pn);
    }

    return (<React.Fragment>
        <div className="form-group">
			<textarea className="form-control" rows="20" cols="40"
                      value={data} onChange={(event) => setData(event.target.value)}/>
        </div>
        <div className="form-group">
            {[
                ["UPPER_SNAKE", "UPPER_SNAKE"],
                ["LOWER_SNAKE", "lower_snake"],
                ["UPPER_CAMEL", "UpperCamel"],
                ["LOWER_CAMEL", "lowerCamel"],
                ["UPPER_KEBAB", "UPPER-KEBAB"],
                ["LOWER_KEBAB", "lower-kebab"],
            ].map((e) =>
                <button className="btn btn-primary" value={e[0]} onClick={handleClick}>
                    {e[1]}
                </button>
            ).flatMap((btn, i) =>
                i === 0 ? [btn] : [" ", btn]
            )}
        </div>
    </React.Fragment>);
}

window.onload = () => {
    ReactDOM.createRoot(document.querySelector("#pname-web")).render(
        <PnamePanel/>
    );
}
