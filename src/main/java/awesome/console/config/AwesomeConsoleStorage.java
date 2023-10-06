package awesome.console.config;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.RoamingType;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.util.xmlb.XmlSerializerUtil;
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

    public boolean SPLIT_ON_LIMIT = false;

    public boolean LIMIT_LINE_LENGTH = true;

    public int LINE_MAX_LENGTH = 1024;

    public boolean SEARCH_URLS = true;

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
}
