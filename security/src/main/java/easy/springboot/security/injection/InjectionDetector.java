/*
 * Copyright 2026 Babak Behzadi
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package easy.springboot.security.injection;

import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

@Component
public class InjectionDetector {

    private static final Pattern SQL_PATTERN = Pattern.compile(
            "(?i)(\\bunion\\s+(all\\s+)" +
                    "?select\\b|\\b(or|and)\\s+" +
                    "['\"]?\\w+['\"]?\\s*=\\s*" +
                    "['\"]?\\w+['\"]?|\\b(drop|truncate)" +
                    "\\s+table\\b|\\b(insert\\s+into|delete\\s+from|update\\s+\\w+\\s+set)" +
                    "\\b|--|/\\*|\\*/|;\\s*(select|insert|update|delete|drop)\\b)");
    private static final Pattern SCRIPT_PATTERN = Pattern.compile(
            "(?i)(<\\s*script\\b|javascript" +
                    "\\s*:|\\bon\\w+\\s*=|\\beval" +
                    "\\s*\\(|\\b(alert|prompt|confirm)\\s*\\(|document\\s*\\.\\s*(cookie|write))");
    private static final Pattern HTML_PATTERN = Pattern.compile("(?is)<\\s*/?\\s*[a-z][a-z0-9:-]*\\b[^>]*>");

    public boolean checkSqlInjection(String text) {
        return SQL_PATTERN.matcher(text).find();
    }

    public boolean checkScriptInjection(String text) {
        return SCRIPT_PATTERN.matcher(text).find();
    }

    public boolean checkHtmlInjection(String text) {
        return HTML_PATTERN.matcher(text).find();
    }

    public boolean checkInjection(String text) {
        return checkSqlInjection(text) || checkScriptInjection(text) || checkHtmlInjection(text);
    }

    public boolean checkInjection(byte[] bytes) {
        String text = new String(bytes);
        return checkInjection(text);
    }
}
