package awesome.console.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * @author anyesu
 */
public class RegexUtils {

    public static String tryMatchGroup(final Matcher matcher, final String group) {
        return tryMatchGroup(matcher, group, 5);
    }

    public static String tryMatchGroup(final Matcher matcher, final String group, final int retries) {
        for (int i = 0; i <= retries; i++) {
            String match = matchGroup(matcher, group + (i > 0 ? i : ""));
            if (null != match) {
                return match;
            }
        }
        return null;
    }

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
