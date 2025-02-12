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
// ENTRY

import {ln2pn} from "./pname-api";

$(function () {
    $(".pname-type").click(function (event) {
        const pnameType = $(this).val();
        const lnVal = $(".pname-ln").val();
        ln2pn(pnameType, lnVal).then((data) => {
            $(".pname-ln").val(data)
        });
    });
});
