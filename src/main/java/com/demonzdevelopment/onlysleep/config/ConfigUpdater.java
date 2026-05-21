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

    /**
     * Updates the destination YAML configuration file by merging default values and comments
     * from the plugin's jar resource without overwriting the user's customized values.
     *
     * @param plugin The JavaPlugin instance.
     * @param resourceName The name of the default YAML resource inside the jar.
     * @param destination The target file on disk to update.
     */
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
            // Load user's current configuration values
            FileConfiguration userConfig = YamlConfiguration.loadConfiguration(destination);

            // Read the default config from the jar line by line
            InputStream defaultStream = plugin.getResource(resourceName);
            if (defaultStream == null) return;

            List<String> newLines = new ArrayList<>();
            Stack<KeyInfo> keyStack = new Stack<>();

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(defaultStream, StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    // Check if it's a comment or empty line
                    String trimmed = line.trim();
                    if (trimmed.isEmpty() || trimmed.startsWith("#")) {
                        newLines.add(line);
                        continue;
                    }

                    // Check if it contains a key
                    int colonIndex = line.indexOf(':');
                    if (colonIndex == -1) {
                        newLines.add(line);
                        continue;
                    }

                    String key = line.substring(0, colonIndex).trim();
                    int indentation = getIndentation(line);

                    // Pop keys from the stack that have greater or equal indentation
                    while (!keyStack.isEmpty() && keyStack.peek().indentation >= indentation) {
                        keyStack.pop();
                    }

                    // Push the new key
                    keyStack.push(new KeyInfo(key, indentation));

                    // Build full path
                    String fullPath = getFullPath(keyStack);

                    // Check if there is a value after the colon
                    String valuePart = line.substring(colonIndex + 1).trim();
                    // Remove inline comments (only outside of quoted strings)
                    valuePart = stripInlineComment(valuePart);

                    if (!valuePart.isEmpty() && !valuePart.equals("{") && !valuePart.equals("[")) {
                        // It's a leaf node key-value pair!
                        if (userConfig.contains(fullPath)) {
                            // Substitute with user's value
                            Object userValue = userConfig.get(fullPath);
                            String serialized = serializeValue(userValue);
                            // Preserve leading whitespace
                            String leadingWhitespace = line.substring(0, line.indexOf(key));
                            newLines.add(leadingWhitespace + key + ": " + serialized);
                        } else {
                            // Keep default value
                            newLines.add(line);
                        }
                    } else {
                        // Section header or empty map/list
                        if (!valuePart.isEmpty() && userConfig.contains(fullPath)) {
                            // It's an empty map or list that the user might have customized
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

            // Write updated lines back to destination file
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

    /**
     * Strips an inline YAML comment (text after an unquoted {@code #}).
     * Respects single-quoted and double-quoted strings so that {@code #}
     * characters inside quotes are not treated as comments.
     */
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
                // Found an unquoted # — strip everything from here
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
                    // Escape double quotes and backslashes for YAML double-quoted string
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
