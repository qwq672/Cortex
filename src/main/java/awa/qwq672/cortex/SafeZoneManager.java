package awa.qwq672.cortex;

import com.google.gson.*;
import net.minecraft.entity.Entity;
import net.minecraft.server.network.ServerPlayerEntity;
import java.io.*;
import java.nio.file.*;
import java.util.*;

public class SafeZoneManager {
    public enum ZoneShape { SPHERE, CYLINDER, CUBOID }
    public enum HeightMode { FULL, ABOVE, BELOW }

    private static final Path CONFIG_PATH = Paths.get("config/cortex/safezones.json");
    private static final List<SafeZone> zones = new ArrayList<>();

    public static void load() {
        if (!Files.exists(CONFIG_PATH)) return;
        try (Reader reader = Files.newBufferedReader(CONFIG_PATH)) {
            JsonObject obj = JsonParser.parseReader(reader).getAsJsonObject();
            JsonArray arr = obj.getAsJsonArray("zones");
            zones.clear();
            for (JsonElement e : arr) {
                JsonObject zoneObj = e.getAsJsonObject();
                String name = zoneObj.get("name").getAsString();
                double cx = zoneObj.get("cx").getAsDouble();
                double cy = zoneObj.get("cy").getAsDouble();
                double cz = zoneObj.get("cz").getAsDouble();
                double radius = zoneObj.has("radius") ? zoneObj.get("radius").getAsDouble() : 5.0;
                double sizeX = zoneObj.has("sizeX") ? zoneObj.get("sizeX").getAsDouble() : 5.0;
                double sizeY = zoneObj.has("sizeY") ? zoneObj.get("sizeY").getAsDouble() : 5.0;
                double sizeZ = zoneObj.has("sizeZ") ? zoneObj.get("sizeZ").getAsDouble() : 5.0;
                ZoneShape shape = zoneObj.has("shape") ? ZoneShape.valueOf(zoneObj.get("shape").getAsString()) : ZoneShape.SPHERE;
                HeightMode heightMode = zoneObj.has("heightMode") ? HeightMode.valueOf(zoneObj.get("heightMode").getAsString()) : HeightMode.FULL;
                zones.add(new SafeZone(name, shape, heightMode, cx, cy, cz, radius, sizeX, sizeY, sizeZ));
            }
        } catch (Exception ex) {
            System.err.println("[Cortex] Failed to load safezones: " + ex.getMessage());
        }
    }

    public static void save() {
        JsonObject root = new JsonObject();
        JsonArray arr = new JsonArray();
        for (SafeZone zone : zones) {
            JsonObject obj = new JsonObject();
            obj.addProperty("name", zone.name);
            obj.addProperty("shape", zone.shape.name());
            obj.addProperty("heightMode", zone.heightMode.name());
            obj.addProperty("cx", zone.cx);
            obj.addProperty("cy", zone.cy);
            obj.addProperty("cz", zone.cz);
            obj.addProperty("radius", zone.radius);
            obj.addProperty("sizeX", zone.sizeX);
            obj.addProperty("sizeY", zone.sizeY);
            obj.addProperty("sizeZ", zone.sizeZ);
            arr.add(obj);
        }
        root.add("zones", arr);
        try {
            Files.createDirectories(CONFIG_PATH.getParent());
            try (Writer writer = Files.newBufferedWriter(CONFIG_PATH)) {
                new GsonBuilder().setPrettyPrinting().create().toJson(root, writer);
            }
        } catch (IOException ex) {
            System.err.println("[Cortex] Failed to save safezones: " + ex.getMessage());
        }
    }

    public static void addZone(String name, ZoneShape shape, HeightMode heightMode,
                               double cx, double cy, double cz,
                               double radius, double sizeX, double sizeY, double sizeZ) {
        zones.add(new SafeZone(name, shape, heightMode, cx, cy, cz, radius, sizeX, sizeY, sizeZ));
        save();
    }

    public static void removeZone(String name) {
        zones.removeIf(zone -> zone.name.equals(name));
        save();
    }

    public static List<String> getZoneNames() {
        return zones.stream().map(zone -> zone.name).toList();
    }

    public static SafeZone getZone(String name) {
        return zones.stream().filter(z -> z.name.equals(name)).findFirst().orElse(null);
    }

    public static boolean isInSafeZone(Entity player) {
        double px = player.getX(), py = player.getY(), pz = player.getZ();
        for (SafeZone zone : zones) {
            if (zone.contains(px, py, pz)) return true;
        }
        return false;
    }

    public static class SafeZone {
        public final String name;
        public final ZoneShape shape;
        public final HeightMode heightMode;
        public final double cx, cy, cz;
        public final double radius;
        public final double sizeX, sizeY, sizeZ;

        SafeZone(String name, ZoneShape shape, HeightMode heightMode,
                 double cx, double cy, double cz,
                 double radius, double sizeX, double sizeY, double sizeZ) {
            this.name = name;
            this.shape = shape;
            this.heightMode = heightMode;
            this.cx = cx;
            this.cy = cy;
            this.cz = cz;
            this.radius = radius;
            this.sizeX = sizeX;
            this.sizeY = sizeY;
            this.sizeZ = sizeZ;
        }

        boolean contains(double x, double y, double z) {
            switch (shape) {
                case SPHERE:
                    return checkSphere(x, y, z);
                case CYLINDER:
                    return checkCylinder(x, y, z);
                case CUBOID:
                    return checkCuboid(x, y, z);
                default:
                    return false;
            }
        }

        private boolean checkSphere(double x, double y, double z) {
            double dx = x - cx;
            double dz = z - cz;
            double hDistSq = dx * dx + dz * dz;
            if (hDistSq > radius * radius) return false;
            switch (heightMode) {
                case FULL: {
                    double dy = y - cy;
                    return dx * dx + dy * dy + dz * dz <= radius * radius;
                }
                case ABOVE:
                    return y >= cy;
                case BELOW:
                    return y <= cy;
                default:
                    return false;
            }
        }

        private boolean checkCylinder(double x, double y, double z) {
            double dx = x - cx;
            double dz = z - cz;
            if (dx * dx + dz * dz > radius * radius) return false;
            switch (heightMode) {
                case FULL:
                    return y >= cy - radius && y <= cy + radius;
                case ABOVE:
                    return y >= cy;
                case BELOW:
                    return y <= cy;
                default:
                    return false;
            }
        }

        private boolean checkCuboid(double x, double y, double z) {
            double halfX = sizeX / 2.0;
            double halfZ = sizeZ / 2.0;
            if (x < cx - halfX || x > cx + halfX) return false;
            if (z < cz - halfZ || z > cz + halfZ) return false;
            switch (heightMode) {
                case FULL: {
                    double halfY = sizeY / 2.0;
                    return y >= cy - halfY && y <= cy + halfY;
                }
                case ABOVE:
                    return y >= cy;
                case BELOW:
                    return y <= cy;
                default:
                    return false;
            }
        }

        public String getDescription() {
            switch (shape) {
                case SPHERE:
                    return String.format("§7球形 r=%.1f %s", radius, heightModeDesc());
                case CYLINDER:
                    return String.format("§7圆柱 r=%.1f %s", radius, heightModeDesc());
                case CUBOID:
                    return String.format("§7长方体 %.1fx%.1fx%.1f %s", sizeX, sizeY, sizeZ, heightModeDesc());
                default:
                    return "";
            }
        }

        private String heightModeDesc() {
            switch (heightMode) {
                case FULL: return "全范围";
                case ABOVE: return "仅上方";
                case BELOW: return "仅下方";
                default: return "";
            }
        }
    }
}