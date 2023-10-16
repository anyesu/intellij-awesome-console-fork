package awesome.console.config;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.RoamingType;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.util.xmlb.XmlSerializerUtil;
import com.intellij.util.xmlb.annotations.Transient;
import java.util.Collections;
import java.util.Objects;
import java.util.Set;
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

    public volatile boolean DEBUG_MODE = DEFAULT_DEBUG_MODE;

    public volatile boolean SPLIT_ON_LIMIT = DEFAULT_SPLIT_ON_LIMIT;

    public volatile boolean LIMIT_LINE_LENGTH = DEFAULT_LIMIT_LINE_LENGTH;

    public volatile int LINE_MAX_LENGTH = DEFAULT_LINE_MAX_LENGTH;

    public volatile boolean searchUrls = DEFAULT_SEARCH_URLS;

    public volatile boolean searchFiles = DEFAULT_SEARCH_FILES;

    public volatile boolean searchClasses = DEFAULT_SEARCH_CLASSES;

    public volatile boolean useIgnorePattern = DEFAULT_USE_IGNORE_PATTERN;

    @NotNull
    @Transient
    public volatile Pattern ignorePattern = DEFAULT_IGNORE_PATTERN;

    public volatile boolean fixChooseTargetFile = DEFAULT_FIX_CHOOSE_TARGET_FILE;

    public volatile boolean useFileTypes = DEFAULT_USE_FILE_TYPES;

    @NotNull
    @Transient
    public volatile Set<String> fileTypeSet = Collections.emptySet();

    private volatile String ignorePatternText = DEFAULT_IGNORE_PATTERN_TEXT;

    private volatile String fileTypes;

    public AwesomeConsoleStorage() {
        setFileTypes(DEFAULT_FILE_TYPES);
    }

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

    public String getIgnorePatternText() {
        return ignorePatternText;
    }

    public void setIgnorePatternText(String ignorePatternText) {
        if (!Objects.equals(this.ignorePatternText, ignorePatternText)) {
            try {
                this.ignorePattern = Pattern.compile(ignorePatternText, Pattern.UNICODE_CHARACTER_CLASS);
                this.ignorePatternText = ignorePatternText;
            } catch (PatternSyntaxException e) {
                this.ignorePattern = DEFAULT_IGNORE_PATTERN;
                this.ignorePatternText = DEFAULT_IGNORE_PATTERN_TEXT;
            }
        }
    }

    public String getFileTypes() {
        return fileTypes;
    }

    public void setFileTypes(String fileTypes) {
        this.fileTypeSet = Set.of(fileTypes.toLowerCase().split(","));
        this.fileTypes = fileTypes;
    }
}
