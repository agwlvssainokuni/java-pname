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

import { ln2pn } from "./pname-api";

class PnamePanel extends React.Component {

	constructor(props) {
		super(props);
		this.state = {
			text: "",
		};
		this.handleChange = this.handleChange.bind(this);
		this.handleClick = this.handleClick.bind(this);
	}

	handleChange(event) {
		this.setState({
			text: event.target.value,
		});
	}

	async handleClick(event) {
		let text = await ln2pn(event.target.value, this.state.text);
		this.setState({
			text: text,
		});
	}

	render() {
		return (
			<div>
				<div className="form-group">
					<textarea className="form-control" rows="20" cols="40" value={this.state.text} onChange={this.handleChange} />
				</div>
				<div className="form-group">
					{[
						["UPPER_SNAKE", "UPPER_SNAKE"],
						["-----"],
						["LOWER_SNAKE", "lower_snake"],
						["-----"],
						["UPPER_CAMEL", "UpperCamel"],
						["-----"],
						["LOWER_CAMEL", "lowerCamel"],
						["-----"],
						["UPPER_KEBAB", "UPPER-KEBAB"],
						["-----"],
						["LOWER_KEBAB", "lower-kebab"],
					].map((e) => {
						if (e.length === 2) {
							return <button className="btn btn-primary" value={e[0]} onClick={this.handleClick} >
								{e[1]}
							</button>;
						} else {
							return <span>{" "}</span>;
						}
					})}
				</div>
			</div >
		);
	}
}

window.onload = () => {
	ReactDOM.render(
		<PnamePanel />,
		document.querySelector("#pname-web")
	);
}
