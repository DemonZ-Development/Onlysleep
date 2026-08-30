package com.demonzdevelopment.onlysleep.command;

import com.demonzdevelopment.onlysleep.Onlysleep;
import com.demonzdevelopment.onlysleep.config.ConfigManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class OnlysleepCommandTest {

    private Onlysleep plugin;
    private ConfigManager config;
    private CommandSender sender;
    private OnlysleepCommand command;

    @BeforeEach
    void setUp() {
        plugin = mock(Onlysleep.class);
        config = mock(ConfigManager.class);
        sender = mock(CommandSender.class);
        command = new OnlysleepCommand(plugin, config);

        when(plugin.getLogger()).thenReturn(Logger.getLogger("OnlysleepCommandTest"));
        when(config.getMessage(anyString())).thenAnswer(invocation -> invocation.getArgument(0));
        when(config.getMessage(anyString(), anyMap())).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void worldPermissionCanListWorlds() {
        FileConfiguration fileConfig = mock(FileConfiguration.class);
        when(sender.hasPermission("onlysleep.world")).thenReturn(true);
        when(config.getConfig()).thenReturn(fileConfig);
        when(fileConfig.getStringList("disabled-worlds")).thenReturn(List.of("world_nether"));

        command.onCommand(sender, mock(Command.class), "onlysleep", new String[]{"world", "list"});

        verify(sender).sendMessage("§eDisabled worlds: world_nether");
    }

    @Test
    void gamemodePermissionCanListGamemodes() {
        FileConfiguration fileConfig = mock(FileConfiguration.class);
        when(sender.hasPermission("onlysleep.gamemode")).thenReturn(true);
        when(config.getConfig()).thenReturn(fileConfig);
        when(fileConfig.getStringList("disabled-gamemodes")).thenReturn(List.of("CREATIVE"));

        command.onCommand(sender, mock(Command.class), "onlysleep", new String[]{"gamemode", "list"});

        verify(sender).sendMessage("§eDisabled gamemodes: CREATIVE");
    }

    @Test
    void invalidMorningTimeIsRejected() {
        when(sender.hasPermission("onlysleep.config")).thenReturn(true);

        command.onCommand(sender, mock(Command.class), "onlysleep", new String[]{"set", "morningtime", "24000"});

        verify(config, never()).setMorningTime(24000);
        verify(sender).sendMessage("§cmorningtime must be between 0 and 23999");
    }

    @Test
    void helpRendersCommandPlaceholder() {
        command.onCommand(sender, mock(Command.class), "os", new String[]{"help"});

        ArgumentCaptor<String> messages = ArgumentCaptor.forClass(String.class);
        verify(sender, org.mockito.Mockito.atLeastOnce()).sendMessage(messages.capture());
        assertTrue(messages.getAllValues().stream().anyMatch(message -> message.contains("/onlysleep set <option> <value>")));
        assertTrue(messages.getAllValues().stream().noneMatch(message -> message.contains("%cmd%")));
    }
}
