import _regeneratorRuntime from "babel-runtime/regenerator";

var _slicedToArray = function () { function sliceIterator(arr, i) { var _arr = []; var _n = true; var _d = false; var _e = undefined; try { for (var _i = arr[Symbol.iterator](), _s; !(_n = (_s = _i.next()).done); _n = true) { _arr.push(_s.value); if (i && _arr.length === i) break; } } catch (err) { _d = true; _e = err; } finally { try { if (!_n && _i["return"]) _i["return"](); } finally { if (_d) throw _e; } } return _arr; } return function (arr, i) { if (Array.isArray(arr)) { return arr; } else if (Symbol.iterator in Object(arr)) { return sliceIterator(arr, i); } else { throw new TypeError("Invalid attempt to destructure non-iterable instance"); } }; }();

function _asyncToGenerator(fn) { return function () { var gen = fn.apply(this, arguments); return new Promise(function (resolve, reject) { function step(key, arg) { try { var info = gen[key](arg); var value = info.value; } catch (error) { reject(error); return; } if (info.done) { resolve(value); } else { return Promise.resolve(value).then(function (value) { step("next", value); }, function (err) { step("throw", err); }); } } return step("next"); }); }; }

/*
 * Copyright 2021 agwlvssainokuni
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

import React, { useState } from "react";
import ReactDOM from "react-dom";
import { ln2pn } from "./pname-api";

function PnamePanel(props) {
	var _this = this;

	var _useState = useState(""),
	    _useState2 = _slicedToArray(_useState, 2),
	    data = _useState2[0],
	    setData = _useState2[1];

	var handleClick = function () {
		var _ref = _asyncToGenerator( /*#__PURE__*/_regeneratorRuntime.mark(function _callee(event) {
			var pn;
			return _regeneratorRuntime.wrap(function _callee$(_context) {
				while (1) {
					switch (_context.prev = _context.next) {
						case 0:
							_context.next = 2;
							return ln2pn(event.target.value, data);

						case 2:
							pn = _context.sent;

							setData(pn);

						case 4:
						case "end":
							return _context.stop();
					}
				}
			}, _callee, _this);
		}));

		return function handleClick(_x) {
			return _ref.apply(this, arguments);
		};
	}();

	return React.createElement(
		React.Fragment,
		null,
		React.createElement(
			"div",
			{ className: "form-group" },
			React.createElement("textarea", { className: "form-control", rows: "20", cols: "40",
				value: data, onChange: function onChange(event) {
					return setData(event.target.value);
				} })
		),
		React.createElement(
			"div",
			{ className: "form-group" },
			[["UPPER_SNAKE", "UPPER_SNAKE"], ["LOWER_SNAKE", "lower_snake"], ["UPPER_CAMEL", "UpperCamel"], ["LOWER_CAMEL", "lowerCamel"], ["UPPER_KEBAB", "UPPER-KEBAB"], ["LOWER_KEBAB", "lower-kebab"]].map(function (e) {
				return React.createElement(
					"button",
					{ className: "btn btn-primary", value: e[0], onClick: handleClick },
					e[1]
				);
			}).flatMap(function (btn, i) {
				return i === 0 ? [btn] : [" ", btn];
			})
		)
	);
}

window.onload = function () {
	ReactDOM.render(React.createElement(PnamePanel, null), document.querySelector("#pname-web"));
};