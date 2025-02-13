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

import {useState} from "react"
import {ln2pn} from "./pname-api"
import {Button, Container, Stack, TextField, Typography} from "@mui/material"

const App = () => {
    const [data, setData] = useState("")

    const handleClick = async (pnameType: string) => {
        const pn = await ln2pn(pnameType, data)
        setData(pn)
    }

    return <Container maxWidth={"lg"}>
        <Container>
            <Typography variant={"h4"} marginTop={1} marginBottom={2}>
                物理名生成
            </Typography>
        </Container>
        <Container>
            <TextField multiline={true} fullWidth={true} minRows={20}
                       label={"一行ずつ論理名を入力してください"} variant={"outlined"}
                       value={data} onChange={(e) => setData(e.target.value)}/>
        </Container>
        <Container>
            <Stack direction={"row"} useFlexGap={true} flexWrap={"wrap"}
                   spacing={1} marginTop={1} marginBottom={1}>
                {[
                    ["UPPER_SNAKE", "UPPER_SNAKE"],
                    ["LOWER_SNAKE", "lower_snake"],
                    ["UPPER_CAMEL", "UpperCamel"],
                    ["LOWER_CAMEL", "lowerCamel"],
                    ["UPPER_KEBAB", "UPPER-KEBAB"],
                    ["LOWER_KEBAB", "lower-kebab"],
                ].map((e) =>
                    <Button variant={"contained"} size={"medium"}
                            sx={{textTransform: 'none'}}
                            onClick={() => handleClick(e[0])}>
                        {e[1]}
                    </Button>
                )}
            </Stack>
        </Container>
        <Container>
            <Typography marginTop={1} marginBottom={1} textAlign={"center"}>
                Copyright &copy;, 2017,2025, agwlvssainokuni
            </Typography>
        </Container>
    </Container>
}

export default App
