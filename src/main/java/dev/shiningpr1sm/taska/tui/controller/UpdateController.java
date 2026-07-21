package dev.shiningpr1sm.taska.tui.controller;

import dev.shiningpr1sm.taska.update.SwingUpdatePrompt;
import dev.shiningpr1sm.taska.update.UpdateApplier;
import dev.shiningpr1sm.taska.update.UpdateManager;

import javax.swing.*;
import java.nio.file.Files;

public class UpdateController {

    public void checkForUpdatesBeforeLaunch() {
        String currentVersion = UpdateManager.getCurrentVersion();
        if ("dev".equals(currentVersion)) return;

        UpdateManager.ReleaseInfo release = UpdateManager.fetchLatestRelease();
        if (release == null) return;

        if (UpdateManager.compareVersions(release.version(), currentVersion) <= 0) return;

        String skipped = UpdateManager.loadSkippedVersion();
        if (release.version().equals(skipped)) return;

        SwingUpdatePrompt.Choice choice =
                SwingUpdatePrompt.show(currentVersion, release.version(), release.notesMarkdown());

        if (choice == SwingUpdatePrompt.Choice.KEEP_OLD) {
            UpdateManager.saveSkippedVersion(release.version());
            return;
        }

        try {
            java.nio.file.Path tempJar = Files.createTempFile("taska_update_", ".jar");
            UpdateManager.downloadRelease(release, tempJar);

            String downloadedVersion = UpdateManager.readJarVersion(tempJar);
            if (downloadedVersion == null || !downloadedVersion.equals(release.version())) {
                JOptionPane.showMessageDialog(null,
                        "The downloaded file failed the version check. The update has been canceled.",
                        "Update failed", JOptionPane.ERROR_MESSAGE);
                Files.deleteIfExists(tempJar);
                return;
            }

            UpdateApplier.restartWithNewJar(tempJar);
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null,
                    "The update could not be downloaded: " + e.getMessage(),
                    "Update failed", JOptionPane.ERROR_MESSAGE);
        }
    }
}
