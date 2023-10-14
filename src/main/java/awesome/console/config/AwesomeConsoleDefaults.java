package awesome.console.config;

/**
 * @author anyesu
 */
public interface AwesomeConsoleDefaults {

    boolean DEFAULT_DEBUG_MODE = false;

    boolean DEFAULT_SPLIT_ON_LIMIT = false;

    boolean DEFAULT_LIMIT_LINE_LENGTH = true;

    int DEFAULT_LINE_MAX_LENGTH = 1024;

    boolean DEFAULT_SEARCH_URLS = true;

    boolean DEFAULT_SEARCH_FILES = true;

    boolean DEFAULT_SEARCH_CLASSES = true;

    boolean DEFAULT_USE_IGNORE_PATTERN = true;

    String DEFAULT_IGNORE_PATTERN_TEXT = "^(\"?)[.\\\\/]+\\1$|^node_modules/";

    boolean DEFAULT_FIX_CHOOSE_TARGET_FILE = true;

    boolean DEFAULT_USE_FILE_TYPES = true;

    String DEFAULT_FILE_TYPES = "bmp,gif,jpeg,jpg,png,webp,ttf";
}
