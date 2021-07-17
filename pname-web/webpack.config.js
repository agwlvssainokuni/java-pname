module.exports = {
	output: {
		filename: "[name]",
		path: __dirname + "/src/main/resources/static/javascript"
	},
	entry: {
		"index.js": "./src/main/javascript/index.js",
		"indexreact.js": "./src/main/javascript/indexreact.js",
		"indexvue.js": "./src/main/javascript/indexvue.js"
	}
};
