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
