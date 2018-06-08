package com.jazibkhan.equalizer;

import android.widget.Toast;

public class Util {
    private boolean isMinMaxSet = false;
    private int minLevel;
    private int maxLevel;
    private final RuntimeException ERR_MIN_MAX = new RuntimeException("Min and Max levels not yet set.");


    private static Util instance;

    private Util() { }

    public static Util instance() {
        if (instance == null) {
            instance = new Util();
        }
        return instance;
    }

    public void init(int minLevel, int maxLevel) {
        this.minLevel = minLevel;
        this.maxLevel = maxLevel;
        this.isMinMaxSet = true;
    }

    public int eqLevelToProgress(int level) {
        if (!isMinMaxSet) {
            throw ERR_MIN_MAX;
        }
        int new_level = 100 * (level - minLevel) / (maxLevel - minLevel);
        return new_level;
    }

    public short progressToEqLevel(int level) {
        if (!isMinMaxSet) {
            throw ERR_MIN_MAX;
        }
        short new_level = (short) ( minLevel + (maxLevel - minLevel) * level / 100 );
        return new_level;
    }

    public int getMinLevel() {
        if (!isMinMaxSet) {
            throw ERR_MIN_MAX;
        }
        return minLevel;
    }

    public int getMaxLevel() {
        if (!isMinMaxSet) {
            throw ERR_MIN_MAX;
        }
        return maxLevel;
    }
}
