package awesome.console.util;

import com.intellij.execution.filters.HyperlinkInfo;
import com.intellij.execution.filters.LazyFileHyperlinkInfo;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

/**
 * @author anyesu
 */
public class HyperlinkUtils {

    public static HyperlinkInfo buildFileHyperlinkInfo(@NotNull Project project, @NotNull String filePath) {
        return buildFileHyperlinkInfo(project, filePath, 0);
    }

    public static HyperlinkInfo buildFileHyperlinkInfo(@NotNull Project project, @NotNull String filePath, int row) {
        return buildFileHyperlinkInfo(project, filePath, row, 0);
    }

    public static HyperlinkInfo buildFileHyperlinkInfo(@NotNull Project project, @NotNull String filePath, int row, int col) {
        row = row > 0 ? row - 1 : 0;
        col = col > 0 ? col - 1 : 0;
        return new LazyFileHyperlinkInfo(project, filePath, row, col) {
            @Override
            public void navigate(@NotNull Project project) {
                VirtualFile file = getVirtualFile();
                if (null == file || !file.isValid()) {
                    Messages.showErrorDialog(project, "Cannot find file " + StringUtil.trimMiddle(filePath, 150),
                                             "Cannot Open File");
                    return;
                }
                super.navigate(project);
            }
        };
    }
}
