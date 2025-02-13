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

export {uri, csrfToken};

/**
 * HTMLに埋め込んだコンテキストパスに基づきURI(パス)を解決する。
 * HTMLへの埋め込みは以下の要領。
 *   <meta name="context-root" th:content="@{/}" />
 */
const uri = ((r: Element | null) => {
    let root: string = r?.getAttribute("content") ?? ""
    if (root.endsWith("/")) {
        root = root.substring(0, root.length - 1)
    }
    return (path: string) => root + path
})(
    document.querySelector<Element>("meta[name='context-root']")
)

/**
 * HTMLに埋め込んだCSRF構成に基づきCSRF情報を保持する。
 * HTMLへの埋め込みは以下の要領。
 *   <meta name="csrf-header" th:content="${_csrf.headerName}" />
 *   <meta name="csrf-parameter" th:content="${_csrf.parameterName}" />
 *   <meta name="csrf-token" th:content="${_csrf.token}" />
 */
const csrfToken = ((h, p, t: Element | null) => ({
        header: (h?.getAttribute("content") ?? null),
        parameter: (p?.getAttribute("content") ?? null),
        token: (t?.getAttribute("content") ?? null),
    })
)(
    document.querySelector<Element>("meta[name='csrf-header']"),
    document.querySelector<Element>("meta[name='csrf-parameter']"),
    document.querySelector<Element>("meta[name='csrf-token']")
)
