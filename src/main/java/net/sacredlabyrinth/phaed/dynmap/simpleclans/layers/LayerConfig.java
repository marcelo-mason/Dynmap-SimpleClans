package net.sacredlabyrinth.phaed.dynmap.simpleclans.layers;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.util.NumberConversions;
import org.jetbrains.annotations.NotNull;

public class LayerConfig {

    private final @NotNull ConfigurationSection section;

    public LayerConfig(@NotNull ConfigurationSection section) {
        this.section = section;
    }

    public @NotNull ConfigurationSection getSection() {
        return section;
    }

    public @NotNull Integer getInt(@NotNull LayerField setting) {
        return section.getInt(setting.path, NumberConversions.toInt(setting.def));
    }

    public @NotNull String getString(@NotNull LayerField setting) {
        return section.getString(setting.path, String.valueOf(setting.def));
    }

    public @NotNull Boolean getBoolean(@NotNull LayerField setting) {
        return section.getBoolean(setting.path, (Boolean) setting.def);
    }


    /**
     * Represents the enum of <b>general</b> {@link Layer}'s fields
     */
    public enum LayerField {

        ENABLE("enable", true),
        PRIORITY("layer-priority", 1),
        LABEL("label", "Label"),
        HIDDEN("hide-by-default", false),
        MINZOOM("min-zoom", 0);

        private final String path;
        private final Object def;

        LayerField(String path, Object def) {
            this.path = path;
            this.def = def;
        }
    }
}
