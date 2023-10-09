package awesome.console.config;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.RoamingType;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.util.xmlb.XmlSerializerUtil;
import com.intellij.util.xmlb.annotations.Transient;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import org.jetbrains.annotations.NotNull;

/**
 * ref: https://plugins.jetbrains.com/docs/intellij/persisting-state-of-components.html
 */
@State(
        name = "Awesome Console Config",
        storages = {
                @Storage(value = "awesomeconsole.xml", roamingType = RoamingType.DISABLED)
        }
)
public class AwesomeConsoleStorage implements PersistentStateComponent<AwesomeConsoleStorage>, AwesomeConsoleDefaults {

    public static final Matcher DEFAULT_IGNORE_MATCHER = Pattern.compile(
            DEFAULT_IGNORE_PATTERN_TEXT,
            Pattern.UNICODE_CHARACTER_CLASS
    ).matcher("");

    public volatile boolean DEBUG_MODE = DEFAULT_DEBUG_MODE;

    public volatile boolean SPLIT_ON_LIMIT = DEFAULT_SPLIT_ON_LIMIT;

    public volatile boolean LIMIT_LINE_LENGTH = DEFAULT_LIMIT_LINE_LENGTH;

    public volatile int LINE_MAX_LENGTH = DEFAULT_LINE_MAX_LENGTH;

    public volatile boolean SEARCH_URLS = DEFAULT_SEARCH_URLS;

    @Transient
    public volatile Matcher ignoreMatcher = DEFAULT_IGNORE_MATCHER;

    private volatile boolean useIgnorePattern = DEFAULT_USE_IGNORE_PATTERN;

    private volatile String ignorePatternText = DEFAULT_IGNORE_PATTERN_TEXT;

    /**
     * Helpers
     */
    @NotNull
    public static AwesomeConsoleStorage getInstance() {
        return ApplicationManager.getApplication().getService(AwesomeConsoleStorage.class);
    }

    @Override
    public AwesomeConsoleStorage getState() {
        return this;
    }

    @Override
    public void loadState(@NotNull AwesomeConsoleStorage state) {
        XmlSerializerUtil.copyBean(state, this);
    }

    public boolean isUseIgnorePattern() {
        return useIgnorePattern;
    }

    public void setUseIgnorePattern(boolean useIgnorePattern) {
        this.useIgnorePattern = useIgnorePattern;
    }

    public String getIgnorePatternText() {
        return ignorePatternText;
    }

    public void setIgnorePatternText(String ignorePatternText) {
        if (!Objects.equals(this.ignorePatternText, ignorePatternText)) {
            try {
                ignoreMatcher = Pattern.compile(ignorePatternText, Pattern.UNICODE_CHARACTER_CLASS).matcher("");
            } catch (PatternSyntaxException e) {
                ignoreMatcher = DEFAULT_IGNORE_MATCHER;
            }
        }
        this.ignorePatternText = ignorePatternText;
    }
}
