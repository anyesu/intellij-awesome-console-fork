package awesome.console.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * @author anyesu
 */
public class RegexUtils {

    public static String matchGroup(final Matcher matcher, final String... groups) {
        for (String group : groups) {
            try {
                String match = matcher.group(group);
                if (null != match) {
                    return match;
                }
            } catch (IllegalArgumentException ignored) {
            }
        }
        return null;
    }

    public static boolean isValidRegex(final String pattern) {
        try {
            if (null != pattern) {
                Pattern.compile(pattern);
                return true;
            }
        } catch (PatternSyntaxException ignored) {
        }
        return false;
    }
}
