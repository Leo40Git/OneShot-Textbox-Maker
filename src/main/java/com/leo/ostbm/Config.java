package com.leo.ostbm;

import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

public class Config {

    public static final long BUILD = 1;
    public static final String KEY_CONFIG_BUILD = "config_build";
    public static final String KEY_SKIP_UPDATE_CHECK = "skip_update_check";
    public static final String KEY_HIDE_SOLSTICE_FACES = "hide_solstice_faces";
    public static final String KEY_COPY_FACEPICS = "copy_facepics";
    private static Preferences config;

    private Config() {
    }

    public static void init() {
        if (config == null)
            config = Preferences.userNodeForPackage(Main.class);
        config.putLong(KEY_CONFIG_BUILD, BUILD);
    }

    public static void wipe() {
        try {
            config.removeNode();
            config = null;
        } catch (final BackingStoreException e) {
            Main.LOGGER.error("Error while wiping configuration!", e);
        }
    }

    public static String get(final String key, final String def) {
        return config.get(key, def);
    }

    public static void set(final String key, final String value) {
        config.put(key, value);
    }

    public static boolean getBoolean(final String key, final boolean def) {
        return config.getBoolean(key, def);
    }

    public static void setBoolean(final String key, final boolean value) {
        config.putBoolean(key, value);
    }

}
