package dev.shiningpr1sm.taska.tui;

public final class VersionInfo {

    private VersionInfo() {}

    public static String getVersion() {
        String version = VersionInfo.class.getPackage().getImplementationVersion();
        return version != null ? version : "dev";
    }
}