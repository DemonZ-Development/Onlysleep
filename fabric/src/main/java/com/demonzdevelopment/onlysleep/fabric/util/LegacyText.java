package com.demonzdevelopment.onlysleep.fabric.util;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public final class LegacyText {

    private static final Set<ChatFormatting> COLORS = Set.of(
        ChatFormatting.BLACK, ChatFormatting.DARK_BLUE, ChatFormatting.DARK_GREEN,
        ChatFormatting.DARK_AQUA, ChatFormatting.DARK_RED, ChatFormatting.DARK_PURPLE,
        ChatFormatting.GOLD, ChatFormatting.GRAY, ChatFormatting.DARK_GRAY,
        ChatFormatting.BLUE, ChatFormatting.GREEN, ChatFormatting.AQUA,
        ChatFormatting.RED, ChatFormatting.LIGHT_PURPLE, ChatFormatting.YELLOW,
        ChatFormatting.WHITE);

    private LegacyText() {}

    public static MutableComponent of(String input) {
        MutableComponent root = Component.empty();
        StringBuilder segment = new StringBuilder();
        List<ChatFormatting> active = new ArrayList<>();

        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            if ((c == '&' || c == '\u00a7') && i + 1 < input.length()) {
                ChatFormatting format = ChatFormatting.getByCode(Character.toLowerCase(input.charAt(i + 1)));
                if (format != null) {
                    if (!segment.isEmpty()) {
                        root.append(Component.literal(segment.toString()).withStyle(toStyle(active)));
                        segment.setLength(0);
                    }
                    i++;
                    if (format == ChatFormatting.RESET) {
                        active.clear();
                    } else if (COLORS.contains(format)) {
                        active.removeIf(COLORS::contains);
                        active.removeIf(f -> !COLORS.contains(f));
                        active.add(format);
                    } else {
                        active.add(format);
                    }
                    continue;
                }
            }
            segment.append(c);
        }

        if (!segment.isEmpty()) {
            root.append(Component.literal(segment.toString()).withStyle(toStyle(active)));
        }
        return root;
    }

    private static Style toStyle(List<ChatFormatting> formats) {
        if (formats.isEmpty()) return Style.EMPTY;
        return Style.EMPTY.applyFormats(formats.toArray(new ChatFormatting[0]));
    }
}
