package com.jazibkhan.equalizer;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class UtilTest {
    private int minLevel = 10;
    private int maxLevel = 110;

    @Test
    public void eqLevelToProgress_0_Test() throws Exception {
        int actual = Util.eqLevelToProgress(10, minLevel, maxLevel);
        int expected = 0;

        assertEquals(expected, actual);
    }

    @Test
    public void eqLevelToProgress_50_Test() throws Exception {
        int actual = Util.eqLevelToProgress(60, minLevel, maxLevel);
        int expected = 50;

        assertEquals(expected, actual);
    }

    @Test
    public void eqLevelToProgress_100_Test() throws Exception {
        int actual = Util.eqLevelToProgress(110, minLevel, maxLevel);
        int expected = 100;

        assertEquals(expected, actual);
    }

    @Test
    public void progressToEqLevel_0_Test() throws Exception {
        int actual = Util.progressToEqLevel(0, minLevel, maxLevel);
        int expected = 10;

        assertEquals(expected, actual);
    }

    @Test
    public void progressToEqLevel_50_Test() throws Exception {
        int actual = Util.progressToEqLevel(50, minLevel, maxLevel);
        int expected = 60;

        assertEquals(expected, actual);
    }

    @Test
    public void progressToEqLevel_100_Test() throws Exception {
        int actual = Util.progressToEqLevel(100, minLevel, maxLevel);
        int expected = 110;

        assertEquals(expected, actual);
    }
}