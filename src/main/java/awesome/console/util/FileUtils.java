package awesome.console.util;

import java.io.File;
import org.jetbrains.annotations.NotNull;

/**
 * @author anyesu
 */
public class FileUtils {

    public static boolean isUncPath(@NotNull String path) {
        return SystemUtils.isWindows() &&
                (path.startsWith("//") || path.startsWith("\\\\"));
    }

    /**
     * Tests whether the file or directory denoted by this abstract pathname
     * exists.
     *
     * @return <code>true</code> if and only if the pathname is not a UNC path and the file or directory denoted
     * by this abstract pathname exists; <code>false</code> otherwise
     */
    public static boolean quickExists(@NotNull String path) {
        // Finding the UNC path will access the network,
        // which takes a long time and causes the UI to freeze.
        // ref: https://stackoverflow.com/a/48554407
        return !isUncPath(path) && new File(path).exists();
    }
}
