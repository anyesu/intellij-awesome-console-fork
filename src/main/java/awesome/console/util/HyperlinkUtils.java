package awesome.console.util;

import awesome.console.config.AwesomeConsoleStorage;
import com.intellij.execution.filters.HyperlinkInfo;
import com.intellij.execution.filters.HyperlinkInfoBase;
import com.intellij.execution.filters.HyperlinkInfoFactory;
import com.intellij.execution.filters.LazyFileHyperlinkInfo;
import com.intellij.openapi.editor.LogicalPosition;
import com.intellij.openapi.editor.colors.CodeInsightColors;
import com.intellij.openapi.editor.colors.EditorColorsManager;
import com.intellij.openapi.editor.markup.EffectType;
import com.intellij.openapi.editor.markup.TextAttributes;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.PathUtil;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
        try {
            // Fix the problem of IDE and external programs opening some non-text files at the same time
            final String ext = PathUtil.getFileExtension(filePath);
            AwesomeConsoleStorage config = AwesomeConsoleStorage.getInstance();
            if (null != ext && config.useFileTypes && config.fileTypeSet.contains(ext.toLowerCase())) {
                VirtualFile virtualFile = LocalFileSystem.getInstance().refreshAndFindFileByPath(filePath);
                if (null != virtualFile) {
                    return createMultipleFilesHyperlinkInfo(List.of(virtualFile), row, col, project, false);
                }
            }
        } catch (Throwable ignored) {
        }
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

    @NotNull
    public static HyperlinkInfo createMultipleFilesHyperlinkInfo(@NotNull List<? extends VirtualFile> files,
                                                                 int row, int col,
                                                                 @NotNull Project project, boolean useFix) {
        // ref: https://github.com/JetBrains/intellij-community/blob/212.5080/platform/platform-impl/src/com/intellij/ide/util/GotoLineNumberDialog.java#L53-L55
        final int row2 = row > 0 ? row - 1 : 0;
        final int col2 = col > 0 ? col - 1 : 0;
        return createMultipleFilesHyperlinkInfo(
                files, row, project, useFix,
                (_project, psiFile, editor, originalEditor) ->
                        editor.getCaretModel().moveToLogicalPosition(new LogicalPosition(row2, col2))
        );
    }

    @NotNull
    public static HyperlinkInfo createMultipleFilesHyperlinkInfo(@NotNull List<? extends VirtualFile> files,
                                                                 int line,
                                                                 @NotNull Project project,
                                                                 boolean useFix,
                                                                 HyperlinkInfoFactory.@Nullable HyperlinkHandler action) {
        line = line > 0 ? line - 1 : 0;
        HyperlinkInfo linkInfo = HyperlinkInfoFactory.getInstance().createMultipleFilesHyperlinkInfo(files, line, project, action);
        if (useFix && linkInfo instanceof HyperlinkInfoBase) {
            return new MultipleFilesHyperlinkInfoWrapper((HyperlinkInfoBase) linkInfo);
        }
        return linkInfo;
    }

    public static TextAttributes createIgnoreStyle() {
        try {
            TextAttributes attr = EditorColorsManager.getInstance().getGlobalScheme().getAttributes(CodeInsightColors.INACTIVE_HYPERLINK_ATTRIBUTES).clone();
            attr.setEffectType(EffectType.SEARCH_MATCH);
            return attr;
        } catch (Throwable e) {
            return null;
        }
    }
}
