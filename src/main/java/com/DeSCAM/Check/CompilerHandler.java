package com.DeSCAM.Check;

import java.io.File;
import java.util.List;

import com.jetbrains.cidr.cpp.toolchains.CPPToolchains;
import com.jetbrains.cidr.cpp.toolchains.Cygwin;
import com.jetbrains.cidr.cpp.toolchains.MinGW;

public class CompilerHandler {
    public static boolean isCygwinEnvironment() {
        final List<CPPToolchains.Toolchain> toolchainList = CPPToolchains.getInstance().getToolchains();
        for (CPPToolchains.Toolchain toolchain : toolchainList) {
            final Cygwin cygwin = toolchain.getCygwin();
            if (cygwin != null && (cygwin.isCygwin() || cygwin.isCygwin64())) {
                return true;
            }
        }
        return false;
    }

    public static String toCygwinPath(String path) {
        final List<CPPToolchains.Toolchain> toolchainList = CPPToolchains.getInstance().getToolchains();
        for (CPPToolchains.Toolchain toolchain : toolchainList) {
            final Cygwin cygwin = toolchain.getCygwin();
            if (cygwin != null) {
                return Cygwin.toCygwinPath(path, cygwin);
            }
        }
        return path;
    }

    public static String getCygwinRoot() {
        final List<CPPToolchains.Toolchain> toolchainList = CPPToolchains.getInstance().getToolchains();
        for (CPPToolchains.Toolchain toolchain : toolchainList) {
            final Cygwin cygwin = toolchain.getCygwin();
            if (cygwin != null) {
                return cygwin.getHomePath();
            }
        }
        return null;
    }

    public static String getBashPath() {
        if (isCygwinEnvironment()) {
            return getCygwinRoot() + "\\bin\\bash.exe";
        }
        String[] paths = {"/bin/bash", "/usr/bin/bash"};
        for (String path : paths) {
            if (new File(path).exists()) {
                return path;
            }
        }
        return paths[0];
    }

    public static boolean isMinGWEnvironment() {
        final List<CPPToolchains.Toolchain> toolchainList = CPPToolchains.getInstance().getToolchains();
        for (CPPToolchains.Toolchain toolchain : toolchainList) {
            final MinGW minGW = toolchain.getMinGW();
            if (minGW != null && (minGW.isMinGW() || minGW.isMinGW64())) {
                return true;
            }
        }
        return false;
    }
}

