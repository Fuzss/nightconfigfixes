# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog].

## [v4.0.3-1.19.2] - 2023-03-03
### Changed
- Night Config Fixes now also handles individual config values being corrected: 
  - In Forge for those cases always the built-in default value for these options is used if the current value cannot be read
  - Now the default config in `defaultconfigs` is checked first if it contains an entry for the value being corrected
- `ModConfig` is no longer wrapped internally, changes to final fields are now applied using `Unsafe`

## [v4.0.2-1.19.2] - 2023-02-03
### Changed
- Mark the mod as incompatible with [ServerCore](https://www.curseforge.com/minecraft/mc-mods/servercore) on Fabric
- Night Config is now a required dependency on Fabric, if you don't have there is no point in having this mod anyway

## [v4.0.1-1.19.2] - 2023-01-20
### Added
- Implemented an optional alternative mode for applying the workaround to `com.electronwill.nightconfig.core.io.ParsingException: Not enough data available`
- Use it in case the default mode does not work reliably for you, the mode can be switched in the config file

## [v4.0.0-1.19.2] - 2023-01-19
- Initial release

[Keep a Changelog]: https://keepachangelog.com/en/1.0.0/
