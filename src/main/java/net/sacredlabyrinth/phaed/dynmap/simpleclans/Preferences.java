package net.sacredlabyrinth.phaed.dynmap.simpleclans;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.sacredlabyrinth.phaed.simpleclans.Clan;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.file.Files;

public class Preferences {
    private static final File prefFile = new File(DynmapSimpleClans.getInstance().getDataFolder(), "preferences.json");
    private static JsonArray preferences;
    private final Clan clan;

    public Preferences(@NotNull Clan clan) {
        this.clan = clan;
        if (preferences == null) {
            loadPreferences();
        }
    }

    public static void loadPreferences() {
        try {
            if (!prefFile.exists()) {
                prefFile.createNewFile();
            }
            preferences = new Gson().fromJson(new InputStreamReader(Files.newInputStream(prefFile.toPath())), JsonArray.class);
        } catch (Exception ex) {
            DynmapSimpleClans.getInstance().getLogger().severe("An error happened while reading the preferences file");
            ex.printStackTrace();
        }

        if (preferences == null) {
            preferences = new JsonArray();
        }
    }

    private static void savePreferences() {
        try {
            Writer w = new OutputStreamWriter(Files.newOutputStream(prefFile.toPath()));
            new Gson().toJson(preferences, w);
            w.close();
        } catch (Exception ex) {
            DynmapSimpleClans.getInstance().getLogger().severe("An error happened while saving the preferences file");
            ex.printStackTrace();
        }
    }

    @Nullable
    public String getClanHomeIcon() {
        if (!preferences.isJsonNull()) {
            JsonArray ja = preferences.getAsJsonArray();
            for (JsonElement je : ja) {
                JsonObject asJsonObject = je.getAsJsonObject();
                if (asJsonObject.get("tag").getAsString().equalsIgnoreCase(clan.getTag())) {
                    JsonElement icon = asJsonObject.get("clanhome-icon");
                    if (!icon.isJsonNull()) {
                        return icon.getAsString();
                    }
                }
            }
        }

        return null;
    }

    public void setClanHomeIcon(String icon) {
        if (!preferences.isJsonNull()) {
            JsonArray ja = preferences.getAsJsonArray();
            boolean contains = false;
            for (JsonElement je : ja) {
                JsonObject asJsonObject = je.getAsJsonObject();
                if (asJsonObject.get("tag").getAsString().equalsIgnoreCase(clan.getTag())) {
                    asJsonObject.addProperty("clanhome-icon", icon);
                    contains = true;
                }
            }
            if (!contains) {
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("tag", clan.getTag());
                jsonObject.addProperty("clanhome-icon", icon);
                ja.add(jsonObject);
            }
        }

        savePreferences();
    }
}
