package com.demonzdevelopment.onlysleep.config;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public final class ConfigUpdater {

    private ConfigUpdater() {}

    public static void update(JavaPlugin plugin, String resourceName, File destination) {
        if (!destination.exists()) {
            try {
                plugin.saveResource(resourceName, false);
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to save default resource " + resourceName + ": " + e.getMessage());
            }
            return;
        }

        try {

            FileConfiguration userConfig = YamlConfiguration.loadConfiguration(destination);

            InputStream defaultStream = plugin.getResource(resourceName);
            if (defaultStream == null) return;

            List<String> newLines = new ArrayList<>();
            Stack<KeyInfo> keyStack = new Stack<>();

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(defaultStream, StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {

                    String trimmed = line.trim();
                    if (trimmed.isEmpty() || trimmed.startsWith("#")) {
                        newLines.add(line);
                        continue;
                    }

                    int colonIndex = line.indexOf(':');
                    if (colonIndex == -1) {
                        newLines.add(line);
                        continue;
                    }

                    String key = line.substring(0, colonIndex).trim();
                    int indentation = getIndentation(line);

                    while (!keyStack.isEmpty() && keyStack.peek().indentation >= indentation) {
                        keyStack.pop();
                    }

                    keyStack.push(new KeyInfo(key, indentation));

                    String fullPath = getFullPath(keyStack);

                    String valuePart = line.substring(colonIndex + 1).trim();

                    valuePart = stripInlineComment(valuePart);

                    if (!valuePart.isEmpty() && !valuePart.equals("{") && !valuePart.equals("[")) {

                        if (userConfig.contains(fullPath)) {

                            Object userValue = userConfig.get(fullPath);
                            String serialized = serializeValue(userValue);

                            String leadingWhitespace = line.substring(0, line.indexOf(key));
                            newLines.add(leadingWhitespace + key + ": " + serialized);
                        } else {

                            newLines.add(line);
                        }
                    } else {

                        if (!valuePart.isEmpty() && userConfig.contains(fullPath)) {

                            Object userValue = userConfig.get(fullPath);
                            String serialized = serializeValue(userValue);
                            String leadingWhitespace = line.substring(0, line.indexOf(key));
                            newLines.add(leadingWhitespace + key + ": " + serialized);
                        } else {
                            newLines.add(line);
                        }
                    }
                }
            }

            try (PrintWriter writer = new PrintWriter(destination, "UTF-8")) {
                for (String newLine : newLines) {
                    writer.println(newLine);
                }
            }

        } catch (Exception e) {
            plugin.getLogger().warning("Failed to update config file " + resourceName + ": " + e.getMessage());
        }
    }

    private static int getIndentation(String line) {
        int count = 0;
        for (int i = 0; i < line.length(); i++) {
            if (line.charAt(i) == ' ') {
                count++;
            } else {
                break;
            }
        }
        return count;
    }

    private static String stripInlineComment(String value) {
        boolean inSingle = false;
        boolean inDouble = false;
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            if (c == '\'' && !inDouble) {
                inSingle = !inSingle;
            } else if (c == '"' && !inSingle) {
                inDouble = !inDouble;
            } else if (c == '#' && !inSingle && !inDouble) {

                return value.substring(0, i).trim();
            }
        }
        return value;
    }

    private static String getFullPath(Stack<KeyInfo> stack) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < stack.size(); i++) {
            if (i > 0) sb.append('.');
            sb.append(stack.get(i).key);
        }
        return sb.toString();
    }

    private static String serializeValue(Object value) {
        if (value instanceof List) {
            List<?> list = (List<?>) value;
            if (list.isEmpty()) {
                return "[]";
            }
            StringBuilder sb = new StringBuilder();
            sb.append("[");
            for (int i = 0; i < list.size(); i++) {
                if (i > 0) sb.append(", ");
                Object item = list.get(i);
                if (item == null) {
                    sb.append("null");
                } else if (item instanceof String) {

                    String escaped = item.toString()
                            .replace("\\", "\\\\")
                            .replace("\"", "\\\"");
                    sb.append("\"").append(escaped).append("\"");
                } else {
                    sb.append(item.toString());
                }
            }
            sb.append("]");
            return sb.toString();
        }

        YamlConfiguration temp = new YamlConfiguration();
        temp.set("temp", value);
        String saved = temp.saveToString();
        if (saved.startsWith("temp:")) {
            return saved.substring(5).trim();
        }
        return value.toString();
    }

    private static class KeyInfo {
        final String key;
        final int indentation;

        KeyInfo(String key, int indentation) {
            this.key = key;
            this.indentation = indentation;
        }
    }
}
