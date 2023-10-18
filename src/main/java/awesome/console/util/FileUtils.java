package awesome.console.util;

import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import java.io.File;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
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

    public static String resolveSymlink(@NotNull final String filePath, final boolean resolveSymlink) {
        if (resolveSymlink) {
            try {
                // to avoid DisposalException: Editor is already disposed
                // caused by `IDEA Resolve Symlinks` plugin
                return Paths.get(filePath).toRealPath().toString();
            } catch (Throwable ignored) {
            }
        }
        return filePath;
    }

    public static List<VirtualFile> resolveSymlinks(@NotNull List<VirtualFile> files, final boolean resolveSymlink) {
        if (resolveSymlink) {
            try {
                return files.parallelStream()
                            .map(it -> resolveSymlink(it.getPath(), true))
                            .distinct()
                            .map(it -> VfsUtil.findFile(Paths.get(it), false))
                            .filter(Objects::nonNull)
                            .collect(Collectors.toList());
            } catch (Throwable ignored) {
            }
        }
        return files;
    }
}
