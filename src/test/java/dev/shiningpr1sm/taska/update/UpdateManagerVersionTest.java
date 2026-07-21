package dev.shiningpr1sm.taska.update;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UpdateManagerVersionTest {

    @Test
    void equalVersionsReturnZero() {
        assertEquals(0, UpdateManager.compareVersions("1.0.0", "1.0.0"));
    }

    @Test
    void firstMajorHigher() {
        assertTrue(UpdateManager.compareVersions("2.0.0", "1.0.0") > 0);
    }

    @Test
    void firstMajorLower() {
        assertTrue(UpdateManager.compareVersions("1.0.0", "2.0.0") < 0);
    }

    @Test
    void firstMinorHigher() {
        assertTrue(UpdateManager.compareVersions("1.2.0", "1.1.0") > 0);
    }

    @Test
    void firstPatchHigher() {
        assertTrue(UpdateManager.compareVersions("1.0.2", "1.0.1") > 0);
    }

    @Test
    void differentLengths() {
        assertTrue(UpdateManager.compareVersions("1.0.0", "1.0") == 0);
    }

    @Test
    void shorterFirstIsLower() {
        assertTrue(UpdateManager.compareVersions("1.0", "1.0.1") < 0);
    }

    @Test
    void handlesNonNumericSuffixes() {
        assertEquals(0, UpdateManager.compareVersions("1.0.0-beta", "1.0.0-beta"));
    }

    @Test
    void preReleaseSuffixesAreEqual() {
        assertEquals(0, UpdateManager.compareVersions("1.0.0-alpha", "1.0.0"));
    }

    @Test
    void singlePartVersions() {
        assertTrue(UpdateManager.compareVersions("2", "1") > 0);
    }
}
