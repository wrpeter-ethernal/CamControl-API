package dev.peter.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.WorldSavePath;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CinematicStorage {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static Map<String, List<Keyframe>> savedCinematics = new HashMap<>();

    public static void save(MinecraftServer server, String name, List<Keyframe> keyframes) {
        savedCinematics.put(name, keyframes);
        persist(server);
    }

    public static List<Keyframe> load(MinecraftServer server, String name) {
        refresh(server);
        return savedCinematics.get(name);
    }

    public static void delete(MinecraftServer server, String name) {
        savedCinematics.remove(name);
        persist(server);
    }

    public static Map<String, List<Keyframe>> getAll(MinecraftServer server) {
        refresh(server);
        return savedCinematics;
    }

    private static void persist(MinecraftServer server) {
        File file = getStorageFile(server);
        try (FileWriter writer = new FileWriter(file)) {
            GSON.toJson(savedCinematics, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void refresh(MinecraftServer server) {
        File file = getStorageFile(server);
        if (!file.exists()) {
            savedCinematics = new HashMap<>();
            return;
        }

        try (FileReader reader = new FileReader(file)) {
            Type type = new TypeToken<Map<String, List<Keyframe>>>() {}.getType();
            savedCinematics = GSON.fromJson(reader, type);
            if (savedCinematics == null) savedCinematics = new HashMap<>();
        } catch (IOException e) {
            e.printStackTrace();
            savedCinematics = new HashMap<>();
        }
    }

    public static void saveSession(MinecraftServer server, List<Keyframe> keyframes) {
        File file = getSessionFile(server);
        try (FileWriter writer = new FileWriter(file)) {
            GSON.toJson(keyframes, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static List<Keyframe> loadSession(MinecraftServer server) {
        File file = getSessionFile(server);
        if (!file.exists()) return null;
        try (FileReader reader = new FileReader(file)) {
            Type type = new TypeToken<List<Keyframe>>() {}.getType();
            return GSON.fromJson(reader, type);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static File getSessionFile(MinecraftServer server) {
        Path worldDir = server.getSavePath(WorldSavePath.ROOT);
        File storageDir = new File(worldDir.toFile(), "camcontrol");
        if (!storageDir.exists()) storageDir.mkdirs();
        return new File(storageDir, "session.json");
    }

    private static File getStorageFile(MinecraftServer server) {
        Path worldDir = server.getSavePath(WorldSavePath.ROOT);
        File storageDir = new File(worldDir.toFile(), "camcontrol");
        if (!storageDir.exists()) storageDir.mkdirs();
        return new File(storageDir, "cinematics.json");
    }
}
