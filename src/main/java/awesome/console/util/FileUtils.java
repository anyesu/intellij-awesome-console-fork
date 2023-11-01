package awesome.console.util;

import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author anyesu
 */
public class FileUtils {

    public static boolean isUnixAbsolutePath(@NotNull String path) {
        return path.startsWith("/") || path.startsWith("\\");
    }

    public static boolean isUncPath(@NotNull String path) {
        return SystemUtils.isWindows() &&
                (path.startsWith("//") || path.startsWith("\\\\"));
    }

    /**
     * Detect a junction/reparse point
     * <p>
     *
     * @see <a href="https://stackoverflow.com/a/74801717">Cross platform way to detect a symbolic link / junction point</a>
     * @see sun.nio.fs.WindowsFileAttributes#isReparsePoint
     */
    public static boolean isReparsePoint(@NotNull Path path) {
        try {
            Object attribute = Files.getAttribute(path, "dos:attributes", LinkOption.NOFOLLOW_LINKS);
            if (attribute instanceof Integer) {
                // is junction or symlink
                return ((Integer) attribute & 0x400) != 0;
            }
        } catch (Throwable ignored) {
        }
        return false;
    }

    public static boolean isReparsePointOrSymlink(@NotNull String filePath) {
        try {
            Path path = Path.of(filePath);
            return Files.isSymbolicLink(path) || isReparsePoint(path);
        } catch (InvalidPathException e) {
            return false;
        }
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
        return !isUncPath(path) && (isReparsePointOrSymlink(path) || new File(path).exists());
    }

    @Nullable
    public static VirtualFile findFileByPath(@NotNull String path) {
        return LocalFileSystem.getInstance().refreshAndFindFileByPath(path);
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
