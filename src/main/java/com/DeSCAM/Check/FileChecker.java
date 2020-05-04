package com.DeSCAM.Check;

import java.util.List;
import com.intellij.psi.PsiFile;
import com.intellij.codeInspection.LocalInspectionTool;
import com.jetbrains.cidr.lang.OCLanguageKind;
import com.jetbrains.cidr.lang.psi.impl.OCFileImpl;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.InspectionManager;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.editor.Document;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

// Lint the current open SystemC file and return an array of problem descriptors "lint messages"
public class FileChecker extends LocalInspectionTool {
    @Nullable
    @Override
    public ProblemDescriptor[] checkFile(@NotNull PsiFile file, @NotNull InspectionManager manager, boolean isOnTheFly) {
        if (!isCpp(file)) {
            return new ProblemDescriptor[0];
        }
        final Document document = FileDocumentManager.getInstance().getDocument(file.getVirtualFile());
        if (document == null) {
            return new ProblemDescriptor[0];
        }
        final List<ProblemDescriptor> descriptors = Linting.lint(file, manager, document);
        return descriptors.toArray(new ProblemDescriptor[descriptors.size()]);
    }

    public static boolean isCpp(PsiFile file) {
        if (!(file instanceof OCFileImpl)) {
            return false;
        }
        OCLanguageKind ocLanguageKind = ((OCFileImpl) file).getKind();
        return ocLanguageKind.isCpp();
    }
}
