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
public class AwesomeConsoleStorage implements PersistentStateComponent<AwesomeConsoleStorage> {

    public static final Matcher DEFAULT_IGNORE_MATCHER = Pattern.compile(
            AwesomeConsoleConfigForm.DEFAULT_IGNORE_PATTERN_TEXT,
            Pattern.UNICODE_CHARACTER_CLASS
    ).matcher("");

    public boolean DEBUG_MODE = AwesomeConsoleConfigForm.DEFAULT_DEBUG_MODE;

    public boolean SPLIT_ON_LIMIT = false;

    public boolean LIMIT_LINE_LENGTH = true;

    public int LINE_MAX_LENGTH = 1024;

    public boolean SEARCH_URLS = true;

    @Transient
    public volatile Matcher ignoreMatcher = DEFAULT_IGNORE_MATCHER;

    private volatile boolean useIgnorePattern = AwesomeConsoleConfigForm.DEFAULT_USE_IGNORE_PATTERN;

    private volatile String ignorePatternText = AwesomeConsoleConfigForm.DEFAULT_IGNORE_PATTERN_TEXT;

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
