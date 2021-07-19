import _regeneratorRuntime from "babel-runtime/regenerator";

var _createClass = function () { function defineProperties(target, props) { for (var i = 0; i < props.length; i++) { var descriptor = props[i]; descriptor.enumerable = descriptor.enumerable || false; descriptor.configurable = true; if ("value" in descriptor) descriptor.writable = true; Object.defineProperty(target, descriptor.key, descriptor); } } return function (Constructor, protoProps, staticProps) { if (protoProps) defineProperties(Constructor.prototype, protoProps); if (staticProps) defineProperties(Constructor, staticProps); return Constructor; }; }();

function _asyncToGenerator(fn) { return function () { var gen = fn.apply(this, arguments); return new Promise(function (resolve, reject) { function step(key, arg) { try { var info = gen[key](arg); var value = info.value; } catch (error) { reject(error); return; } if (info.done) { resolve(value); } else { return Promise.resolve(value).then(function (value) { step("next", value); }, function (err) { step("throw", err); }); } } return step("next"); }); }; }

function _classCallCheck(instance, Constructor) { if (!(instance instanceof Constructor)) { throw new TypeError("Cannot call a class as a function"); } }

function _possibleConstructorReturn(self, call) { if (!self) { throw new ReferenceError("this hasn't been initialised - super() hasn't been called"); } return call && (typeof call === "object" || typeof call === "function") ? call : self; }

function _inherits(subClass, superClass) { if (typeof superClass !== "function" && superClass !== null) { throw new TypeError("Super expression must either be null or a function, not " + typeof superClass); } subClass.prototype = Object.create(superClass && superClass.prototype, { constructor: { value: subClass, enumerable: false, writable: true, configurable: true } }); if (superClass) Object.setPrototypeOf ? Object.setPrototypeOf(subClass, superClass) : subClass.__proto__ = superClass; }

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

import { ln2pn } from "./pname-api";

var PnamePanel = function (_React$Component) {
	_inherits(PnamePanel, _React$Component);

	function PnamePanel(props) {
		_classCallCheck(this, PnamePanel);

		var _this = _possibleConstructorReturn(this, (PnamePanel.__proto__ || Object.getPrototypeOf(PnamePanel)).call(this, props));

		_this.state = {
			text: ""
		};
		_this.handleChange = _this.handleChange.bind(_this);
		_this.handleClick = _this.handleClick.bind(_this);
		return _this;
	}

	_createClass(PnamePanel, [{
		key: "handleChange",
		value: function handleChange(event) {
			this.setState({
				text: event.target.value
			});
		}
	}, {
		key: "handleClick",
		value: function () {
			var _ref = _asyncToGenerator( /*#__PURE__*/_regeneratorRuntime.mark(function _callee(event) {
				var text;
				return _regeneratorRuntime.wrap(function _callee$(_context) {
					while (1) {
						switch (_context.prev = _context.next) {
							case 0:
								_context.next = 2;
								return ln2pn(event.target.value, this.state.text);

							case 2:
								text = _context.sent;

								this.setState({
									text: text
								});

							case 4:
							case "end":
								return _context.stop();
						}
					}
				}, _callee, this);
			}));

			function handleClick(_x) {
				return _ref.apply(this, arguments);
			}

			return handleClick;
		}()
	}, {
		key: "render",
		value: function render() {
			var _this2 = this;

			return React.createElement(
				React.Fragment,
				null,
				React.createElement(
					"div",
					{ className: "form-group" },
					React.createElement("textarea", { className: "form-control", rows: "20", cols: "40", value: this.state.text, onChange: this.handleChange })
				),
				React.createElement(
					"div",
					{ className: "form-group" },
					[["UPPER_SNAKE", "UPPER_SNAKE"], ["LOWER_SNAKE", "lower_snake"], ["UPPER_CAMEL", "UpperCamel"], ["LOWER_CAMEL", "lowerCamel"], ["UPPER_KEBAB", "UPPER-KEBAB"], ["LOWER_KEBAB", "lower-kebab"]].map(function (e) {
						return React.createElement(
							"button",
							{ className: "btn btn-primary", value: e[0], onClick: _this2.handleClick },
							e[1]
						);
					}).flatMap(function (btn, i) {
						return i === 0 ? [btn] : [" ", btn];
					})
				)
			);
		}
	}]);

	return PnamePanel;
}(React.Component);

window.onload = function () {
	ReactDOM.render(React.createElement(PnamePanel, null), document.querySelector("#pname-web"));
};