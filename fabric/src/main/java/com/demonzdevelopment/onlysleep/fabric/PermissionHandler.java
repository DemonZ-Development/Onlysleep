package com.demonzdevelopment.onlysleep.fabric;

import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;

public class PermissionHandler {

    private final OnlysleepMod mod;

    public PermissionHandler(OnlysleepMod mod) {
        this.mod = mod;
    }

    public boolean isExempt(ServerPlayer player) {
        return Permissions.check(player, "onlysleep.exempt", false);
    }

    public boolean canReload(CommandSourceStack source) {
        return Permissions.check(source, "onlysleep.reload", 2);
    }

    public boolean canViewInfo(CommandSourceStack source) {
        return Permissions.check(source, "onlysleep.info", 2);
    }

    public boolean canViewStatus(CommandSourceStack source) {
        return Permissions.check(source, "onlysleep.status", 2);
    }

    public boolean canCheckUpdates(CommandSourceStack source) {
        return Permissions.check(source, "onlysleep.update", 2);
    }

    public boolean shouldNotifyUpdates(ServerPlayer player) {
        return Permissions.check(player, "onlysleep.update", 2);
    }

    public boolean canConfig(CommandSourceStack source) {
        return Permissions.check(source, "onlysleep.config", 2) || Permissions.check(source, "onlysleep.reload", 2) || Permissions.check(source, "onlysleep.admin", 2);
    }

    public boolean canDump(CommandSourceStack source) {
        return Permissions.check(source, "onlysleep.dump", 2) || Permissions.check(source, "onlysleep.admin", 2) || canConfig(source);
    }

    public boolean canManageWorld(CommandSourceStack source) {
        return Permissions.check(source, "onlysleep.world", 2) || canConfig(source);
    }

    public boolean canManageGamemode(CommandSourceStack source) {
        return Permissions.check(source, "onlysleep.gamemode", 2) || canConfig(source);
    }
}
