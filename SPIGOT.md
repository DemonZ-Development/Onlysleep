[center]
[size=5][color=#00d875]Onlysleep[/color][/size]
[size=4][i]One player sleeps. Everyone wakes up.[/i][/size][/center]

[HR]

[size=4][color=#ffaa00]What is Onlysleep?[/color][/size]
Onlysleep lets your server skip the night without waiting for every player to find a bed. One sleeper is enough by default, but you can set any threshold from 0 to 100 percent.

Sleep counts are kept per world. A player sleeping in one world will not skip the night in another, and rain and thunder can be cleared independently.

[HR]

[size=4][color=#ffaa00]Features[/color][/size]
[list]
[*][b]One-Player Sleep[/b] — Default mode, or any percentage (0-100%)
[*][b]Per-World Sleep[/b] — Each world tracks separately
[*][b]Weather Skip[/b] — Clear rain and thunder independently
[*][b]Visual Feedback[/b] — Boss bar, action bar, progress bar, titles
[*][b]Sound Effects[/b] — Configurable on bed-enter and night-skip
[*][b]Smart Filtering[/b] — AFK (built-in, EssentialsX, CMI), spectators, creative, flying, exempt permissions, disabled gamemodes
[*][b]Gamerule Management[/b] — Auto-manages playersSleepingPercentage
[*][b]PlaceholderAPI[/b] — 12+ placeholders
[*][b]Folia Support[/b] — Full regionized scheduler support
[*][b]Update Checker[/b] — Automatic update notifications
[*][b]bStats[/b] — Anonymous usage statistics (opt-out available)
[/list]

[HR]

[size=4][color=#ffaa00]Commands[/color][/size]
[list]
[*][b]/onlysleep[/b] — Show help
[*][b]/onlysleep reload[/b] — Reload config ([i]requires onlysleep.reload[/i])
[*][b]/onlysleep info[/b] — Plugin information
[*][b]/onlysleep status[/b] — Detailed status overview
[*][b]/onlysleep update[/b] — Check for updates
[*][b]/onlysleep set|get|toggle[/b] — Manage configuration in game
[*][b]/onlysleep world|gamemode[/b] — Manage excluded worlds and game modes
[*][b]/onlysleep dump [paste][/b] — Create a diagnostic dump
[*][b]Aliases:[/b] /os, /sleep
[/list]

[HR]

[size=4][color=#ffaa00]Installation[/color][/size]
[list=1]
[*]Download the JAR
[*]Place it in your [b]plugins/[/b] folder
[*]Restart your server
[*]Edit [b]plugins/Onlysleep/config.yml[/b] if you want to tweak anything
[*]Run [b]/onlysleep reload[/b] to apply changes
[/list]

[HR]

[size=4][color=#ffaa00]Requirements[/color][/size]
[list]
[*]Java 25+
[*]Minecraft 26.2
[*]Works standalone — [b]no dependencies required[/b] (PlaceholderAPI is optional)
[/list]

[HR]

[size=4][color=#ffaa00]Permissions[/color][/size]
[list]
[*][b]onlysleep.*[/b] — All permissions (OP)
[*][b]onlysleep.command[/b] — Use commands (Everyone)
[*][b]onlysleep.info[/b] — View plugin info (OP)
[*][b]onlysleep.reload[/b] — Reload config (OP)
[*][b]onlysleep.status[/b] — View status (OP)
[*][b]onlysleep.exempt[/b] — Excluded from sleep (None — operators sleep by default)
[*][b]onlysleep.update[/b] — Update alerts (OP)
[/list]

[HR]

[size=4][color=#ffaa00]Configuration[/color][/size]
The default configuration is ready to use. The main setting is `sleep-percentage` in `config.yml`:
[list]
[*][b]0[/b] = one player sleeps, everyone wakes up
[*][b]50[/b] = half the server needs to be in bed
[*][b]100[/b] = everyone has to sleep
[/list]

Set `per-world-sleep: false` if you want global counting across all worlds.

[HR]

[size=4][color=#ffaa00]bStats[/color][/size]
[img]https://bstats.org/signatures/bukkit/OnlySleep.svg[/img]

[url=https://bstats.org/plugin/bukkit/OnlySleep/31415]View live statistics[/url] | [url=https://modrinth.com/plugin/onlysleep]Download on Modrinth[/url] | [url=https://github.com/DemonZ-Development/Onlysleep/issues]Report Issues[/url]

[HR]

[size=4][color=#ffaa00]Links[/color][/size]
[list]
[*][url=https://demonzdevelopment.online]Website[/url]
[*][url=https://github.com/DemonZ-Development]GitHub[/url]
[*][url=https://modrinth.com/organization/DemonZDevelopment]Modrinth[/url]
[*][url=https://hangar.papermc.io/DemonzDevelopment/Onlysleep]Hangar[/url]
[*][url=https://x.com/DemonZ_Dev]Twitter / X[/url]
[*][url=https://www.youtube.com/@DemonzDevelopment]YouTube[/url]
[*][url=https://www.instagram.com/demonzdevelopement]Instagram[/url]
[*][url=https://discord.gg/qkvkEaPryF]Discord[/url]
[*][url=https://www.reddit.com/r/DemonZDevelopment/]Reddit[/url]
[/list]

[HR]

[center][size=4][color=#ffaa00]Sponsored By[/color][/size]
[url='https://nexeu.zip'][img]https://whodoesntloveavatars.s3.fra.databucket.eu/assets/promo.png[/img][/url]
[size=3]Server hosting for this project is sponsored by [url='https://nexeu.zip'][b]Nexeu Hosting[/b][/url].[/size][/center]

[HR]

[center][size=2][color=#888888]Made by Demonz Development | [email]demonzdevelopment@gmail.com[/email][/color][/size][/center]
