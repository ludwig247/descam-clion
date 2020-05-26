package com.DeSCAM.Check;

import com.intellij.codeInspection.*;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.StatusBar;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.google.common.base.Strings;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;


public class Linting {
    private static final Logger LOGGER = Logger.getInstance(Linting.class);

    private Linting() {
    }

    public static List<ProblemDescriptor> lint(@NotNull PsiFile file,
                                               @NotNull InspectionManager manager,
                                               @NotNull Document document) {
        String linterPath = Settings.get(Option.OPTION_KEY_CHECKER);
        String linterOptions = "";
        if (linterPath == null || linterPath.isEmpty()) {
            StatusBar.Info.set("Please set the path to descam_check.py first!", file.getProject());
            return Collections.emptyList();
        }

        final VirtualFile baseDir = file.getProject().getBaseDir();
        if (baseDir == null) {
            LOGGER.error("No valid base directory found!");
            return Collections.emptyList();
        }
        final String canonicalPath = baseDir.getCanonicalPath();
        if (Strings.isNullOrEmpty(canonicalPath)) {
            LOGGER.error("Failed to get canonical path!");
            return Collections.emptyList();
        }
        final List<String> args = buildCommandLineArgs(linterPath, linterOptions, file);
        return commenceLinting(file, manager, document, canonicalPath, args);
    }

    @NotNull
    private static List<String> buildCommandLineArgs(@NotNull String linterPath,
                                                     @NotNull String linterOptions,
                                                     @NotNull PsiFile file) {
        final String pythonPath = Settings.get(Option.OPTION_KEY_PYTHON);
        String cppFilePath = file.getVirtualFile().getCanonicalPath();
        if (CompilerHandler.isCygwinEnvironment()) {
            cppFilePath = CompilerHandler.toCygwinPath(cppFilePath);
        }
        final List<String> args = new ArrayList<>();
        if (CompilerHandler.isMinGWEnvironment()) {
            args.add(pythonPath);
            args.add(linterPath);
            Collections.addAll(args, linterOptions.split("\\s+"));
            Collections.addAll(args, cppFilePath);
        } else {
            args.add(CompilerHandler.getBashPath());
            args.add("-c");
            String joinedArgs;
            if (CompilerHandler.isCygwinEnvironment()) {
                joinedArgs = "\"\\\"" + pythonPath + "\\\" \\\"" + linterPath + "\\\" " + linterOptions + " ";
                joinedArgs += "\\\"" + cppFilePath + "\\\" ";
                joinedArgs += '\"';
            } else {
                joinedArgs = "\"" + pythonPath + "\" \"" + linterPath + "\" " + linterOptions + " ";
                joinedArgs += "\"" + cppFilePath + "\" ";
            }
            args.add(joinedArgs);
        }
        return args;
    }

    @NotNull
    private static List<ProblemDescriptor> commenceLinting(@NotNull PsiFile file,
                                                           @NotNull InspectionManager manager,
                                                           @NotNull Document document,
                                                           @NotNull String canonicalPath,
                                                           @NotNull List<String> args) {
        final File pwd = new File(canonicalPath);
        final List<ProblemDescriptor> problemDescriptors = new ArrayList<>();
        final ProcessBuilder pb = new ProcessBuilder(args);
        pb.directory(pwd);
        final Process proc;
        try {
            proc = pb.start();
        } catch (IOException e) {
            LOGGER.error("Failed to start linting the file: " + file.getVirtualFile().getCanonicalPath(), e);
            return Collections.emptyList();
        }

        final List<MessageObj> messagesList = parseJSONresult(proc.getInputStream(), file.getVirtualFile().getCanonicalPath());
        for (MessageObj msgObj : messagesList) {
            final ProblemDescriptor problemDescriptor = getProblemDescriptor(file, manager, document, msgObj);
            if (problemDescriptor == null) continue;
            problemDescriptors.add(problemDescriptor);
        }
        return problemDescriptors;
    }

    private static List<MessageObj> parseJSONresult(InputStream inStream, String file) {
        final List<MessageObj> messagesList = new ArrayList<>();
        JSONParser parser = new JSONParser();
        try {
            JSONArray messagesArray = (JSONArray) parser.parse(new InputStreamReader(inStream));
            if (messagesArray != null) {
                for (Object object : messagesArray) {
                    try {
                        JSONObject jsonMessage = (JSONObject) object;
                        String fileDir = (String) jsonMessage.get("file");
                        String message = (String) jsonMessage.get("message");
                        if (message == null || fileDir == null || !fileDir.equals(file)) continue;

                        String severityLevel = (String) jsonMessage.get("severity");
                        if (severityLevel == null) severityLevel = "Error";

                        String violationType = (String) jsonMessage.get("violation");
                        if (violationType == null) violationType = "SystemC-PPA Compliance";

                        JSONArray lineArrays = (JSONArray) jsonMessage.get("line");
                        if (lineArrays == null) continue;
                        JSONArray rowArray = (JSONArray) lineArrays.get(0);
                        JSONArray columnArray = (JSONArray) lineArrays.get(1);
                        if (rowArray == null || columnArray == null) continue;
                        int rowStartNum = (int) (long) rowArray.get(0);
                        int rowEndNum = (int) (long) rowArray.get(1);
                        int colStartNum = (int) (long) columnArray.get(0);
                        int colEndNum = (int) (long) columnArray.get(1);

                        MessageObj msgObj = new MessageObj(fileDir, message, severityLevel, violationType, rowStartNum, rowEndNum, colStartNum, colEndNum);
                        messagesList.add(msgObj);
                    } catch (ClassCastException e){
                    }
                }
            }
        } catch (ParseException | IOException e) {
            LOGGER.error("Failed to lint file: " + file, e);
            //e.printStackTrace();
            return messagesList;
        }
        return messagesList;
    }

    @Nullable
    private static ProblemDescriptor getProblemDescriptor(@NotNull PsiFile file,
                                                          @NotNull InspectionManager manager,
                                                          @NotNull Document document,
                                                          @NotNull MessageObj msgObj) {
        int lineCount = document.getLineCount();
        if (lineCount == 0) {
            return null;
        }
        ProblemHighlightType HighlightType = ProblemHighlightType.ERROR;
        if (msgObj.getSeverityLevel().equals("Warning")) {
            HighlightType = ProblemHighlightType.WARNING;
        }
        int rowStart = (msgObj.getRowStartNum() >= lineCount) ? (lineCount - 1) : msgObj.getRowStartNum();
        rowStart = (rowStart > 0) ? (rowStart - 1) : 0;
        int rowEnd = (msgObj.getRowEndNum() >= lineCount) ? (lineCount - 1) : msgObj.getRowEndNum();
        rowEnd = (rowEnd > 0) ? (rowEnd - 1) : 0;

        final String lintMessage = msgObj.getViolationType() + ": " + msgObj.getMessage();
        int lineStartOffset = document.getLineStartOffset(rowStart);
        int lineEndOffset = document.getLineEndOffset(rowEnd);

        if (msgObj.getRowStartNum() == msgObj.getRowEndNum() && msgObj.getColumnStartNum() == msgObj.getColumnEndNum()) {
            // Do not highlight empty whitespace prepended to lines.
            final String text = document.getImmutableCharSequence().subSequence(
                    lineStartOffset, lineEndOffset).toString();
            lineStartOffset += text.length() -
                    text.replaceAll("^\\s+", "").length();
        }else if (msgObj.getColumnStartNum() == msgObj.getColumnEndNum()){
            final String text = document.getImmutableCharSequence().subSequence(
                    lineStartOffset, document.getLineEndOffset(rowStart)).toString();
            lineStartOffset += text.length() -
                    text.replaceAll("^\\s+", "").length();
        }else {
            lineEndOffset = document.getLineStartOffset(rowEnd);
            lineStartOffset += (msgObj.getColumnStartNum()>0?(msgObj.getColumnStartNum()-1):0);
            lineEndOffset += (msgObj.getColumnEndNum()>0?(msgObj.getColumnEndNum()-1):0);
        }

        return manager.createProblemDescriptor(
                file,
                TextRange.create(lineStartOffset, lineEndOffset),
                lintMessage,
                HighlightType,
                true
        );
    }
}
