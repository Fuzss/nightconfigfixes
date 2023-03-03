# Night Config Fixes

A Minecraft mod. Downloads can be found on [CurseForge](https://www.curseforge.com/members/fuzs_/projects) and [Modrinth](https://modrinth.com/user/Fuzs).

![](https://raw.githubusercontent.com/Fuzss/modresources/main/pages/data/nightconfigfixes/banner.png)

## About the project
Night Config Fixes implements a small number of workarounds for relatively common issues concerning the [Night Config](https://github.com/TheElectronWill/night-config) library, and it's implementation in Minecraft Forge's config system.

There have been daily reports mentioning some of these issue for the Better Minecraft mod packs, which feels common enough to provide a dedicated workaround mod.

The Fabric version only includes the first workaround for mods using Night Config on Fabric, both other workarounds are specific to Minecraft Forge's config system and therefore only exist in the Forge version of this mod.

## Workarounds

### Workaround 1: A fix for `ParsingException: Not enough data available`

> recreateConfigsWhenParsingFails = true

If your game has ever crashed with the following exception, this workaround is just for you and the main reason why Night Config Fixes was made in the first place:
> Caused by: com.electronwill.nightconfig.core.io.ParsingException: Not enough data available

Sometimes and very randomly (also only reported on Windows systems), existing config files just loose all of their data and go completely blank. This is when the exception mentioned above is thrown, as Night Config is unable to read the file.

With this workaround enabled, instead of the game crashing, the invalid blank file is simply deleted and a new file with default values is created in its place. No settings from the previous file can be restored, unfortunately.

**Note:**  
When enabling this workaround in a mod pack which ships some already configured configs, make sure to place those configs in the `defaultconfigs` directory, not just in `config`, so that when restoring a faulty config the desired default values from `defaultconfigs` are used instead of the built-in values.

### Workaround 2: Apply default config values from `defaultconfigs`

> correctConfigValuesFromDefaultConfig = true

When only individual options in a config are invalid, like an option is missing or contains a set value that cannot be parsed, Forge corrects those individual options by restoring them to their default values in the config file. You can observe Forge doing this in the console when the following message is printed:

> [net.minecraftforge.fml.config.ConfigFileTypeHandler/CONFIG]: Configuration file CONFIG_PATH is not correct. Correcting

The problem with that is, Forge uses the built-in default value defined by the mod providing the config, but ignores any value from a possibly present default config in `defaultconfigs` which a mod pack might ship.

This workaround changes this behavior and checks if an entry in a config in `defaultconfigs` exists first before falling back to correcting to the built-in default value.

**Example:**  
A config contains an option which requires an integer value.  
The default value for this option defined by the mod the config is from is 3.  
The default value for this option defined by the current mod pack via the config placed in `defaultconfigs` is 5 though.  
When the user now accidentially enters a value such as 10.5, Forge corrects the input back to the default 3 (since 10.5 is a double, not an integer and therefore invalid).  
With this workaround enabled the value will instead be corrected to 5.

### Workaround 3: Global server configs

> forceGlobalServerConfigs = false

Changes Forge's server config type to generate in the global `config` directory, instead of on a local basis per world in `saves/WORLD_NAME/serverconfig`.

This design decision by Forge simply causes too much confusion and frustration among users, so this mod felt like a good enough opportunity to include a fix for that.
