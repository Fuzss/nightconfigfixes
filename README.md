# Night Config Fixes

A Minecraft mod. Downloads can be found on [CurseForge](https://www.curseforge.com/members/fuzs_/projects) and [Modrinth](https://modrinth.com/user/Fuzs).

![](https://i.imgur.com/4htIK3r.png)

## ABOUT THE PROJECT
Night Config Fixes exists mostly for one purpose, which is to implement a workaround for a rather common issue related to [Night Config](https://github.com/TheElectronWill/night-config) config loading (the library behind Forge's config system, also used by many mods on Fabric): 

```
Caused by: com.electronwill.nightconfig.core.io.ParsingException: Not enough data available
	at com.electronwill.nightconfig.core.io.ParsingException.notEnoughData(ParsingException.java:22) ~[core-3.6.4.jar%237!/:?] {}
	at com.electronwill.nightconfig.core.io.ReaderInput.directReadChar(ReaderInput.java:36) ~[core-3.6.4.jar%237!/:?] {}
	at com.electronwill.nightconfig.core.io.AbstractInput.readChar(AbstractInput.java:49) ~[core-3.6.4.jar%237!/:?] {}
	at com.electronwill.nightconfig.core.io.AbstractInput.readCharsUntil(AbstractInput.java:123) ~[core-3.6.4.jar%237!/:?] {}
	at com.electronwill.nightconfig.toml.TableParser.parseKey(TableParser.java:166) ~[toml-3.6.4.jar%238!/:?] {}
	at com.electronwill.nightconfig.toml.TableParser.parseDottedKey(TableParser.java:145) ~[toml-3.6.4.jar%238!/:?] {}
	at com.electronwill.nightconfig.toml.TableParser.parseNormal(TableParser.java:55) ~[toml-3.6.4.jar%238!/:?] {}
	at com.electronwill.nightconfig.toml.TomlParser.parse(TomlParser.java:44) ~[toml-3.6.4.jar%238!/:?] {}
	at com.electronwill.nightconfig.toml.TomlParser.parse(TomlParser.java:37) ~[toml-3.6.4.jar%238!/:?] {}
	at com.electronwill.nightconfig.core.io.ConfigParser.parse(ConfigParser.java:113) ~[core-3.6.4.jar%237!/:?] {}
	at com.electronwill.nightconfig.core.io.ConfigParser.parse(ConfigParser.java:219) ~[core-3.6.4.jar%237!/:?] {}
	at com.electronwill.nightconfig.core.io.ConfigParser.parse(ConfigParser.java:202) ~[core-3.6.4.jar%237!/:?] {}
	at com.electronwill.nightconfig.core.file.WriteSyncFileConfig.load(WriteSyncFileConfig.java:73) ~[core-3.6.4.jar%237!/:?] {}
	at com.electronwill.nightconfig.core.file.AutosaveCommentedFileConfig.load(AutosaveCommentedFileConfig.java:85) ~[core-3.6.4.jar%237!/:?] {}
	... 10 more
```

There have been daily reports mentioning this issue for the Better Minecraft mod packs, which feels common enough to provide a dedicated workaround mod.

## The issue
The issue is not fully understood at the time, so here is what's been gathered so far:
The issue seems to be exclusive to Windows and is related to config file contents being written to the config file every time the file is read. This can lead to the config file going blank. The next time Night Config tries to read the now blank file, the `com.electronwill.nightconfig.core.io.ParsingException: Not enough data available` is thrown. These assumptions are based on [this](https://github.com/MinecraftForge/MinecraftForge/issues/9122).

## The workaround
First of all, let's be clear: Night Config Fixes does not implement an actual 'fix' for the mentioned issue, it's just a simple workaround.

The workaround basically just deletes the config file that caused the `ParsingException`, so it can immediately be recreated from Night Config's `FileNotFoundAction`. There is no content to preserve from the original file as it's already blank. So deleting the file is the most viable thing to do.

## Regarding mod packs
This mod is specifically designed for mod packs, as naturally containing a lot of mods there is a big chance of this issue occurring.

Note, that since the original config contents are lost, it is important in the case of the mod pack providing pre-configured configs that they are placed in Forge's `defaultconfigs` directory, and not in the `config` directory. This way, when recreating faulty configs the pre-configured files will simply be copied from `defaultconfigs`. This goes for all types of configs (`CLIENT`, `COMMON` and `SERVER`), as all three types support the `defaultconfigs` directory.

So to make it short: If your mod pack ships pre-configured configs, place them in the `defaultconfigs` directory, not in `config`.

## The implementation
Generally, the implementation should be straight forward: Just hook into Night Config where the exception is thrown and where the `FileNotFoundAction` for creating a new file is still available. This is also exactly what's done on Fabric via Mixin.

The advantage of this approach is, that all mods using Night Config's config system in any capacity benefit from the workaround, so Night Config Fixes is not bound to a specific config system implementation.

On Forge though, this approach is unfortunately not possible: Forge does not load ASM transformations for classes outside the main Minecraft Forge jar, so not even changes to classes in the Forge Loader jar are possible before they are loaded by the classloader (neither via Mixin nor Forge's native JavaScript based core mod system).
So instead, a reflection based approach seems the best option. It's actually quite simple: The `ConfigFileTypeHandler` instance used globally by all `ModConfig` instances is replaced with a custom version that is able to handle the problematic `ParsingException`.

The huge disadvantage of this approach is though, that only Forge mods using Forge's config system are affected. Other mods on Forge, that use Night Config for their own custom config system may still throw an unhandled `ParsingException`.

## Additional changes
The Forge version of Night Config Fixes also includes another change: It makes Forge's server config type generate in the global `.minecraft/config/` directory, instead of on a local basis per world. This design decision by Forge simply causes too much confusion and frustration among users, so this mod felt like a good enough opportunity to include a fix for that, too.

This feature is optional though, it can be enabled in Night Config Fixes' own config file.
