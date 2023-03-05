# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog].

## [v3.0.2-1.18.2] - 2023-03-05
### Fixed
- Fixed an issue with mods expecting the original `ForgeConfigSpec` to be present when any `ModConfigEvent` is fired

## [v3.0.1-1.18.2] - 2023-03-03
### Changed
- Night Config Fixes now also handles individual config values being corrected:
    - In Forge for those cases always the built-in default value for these options is used if the current value cannot be read
    - Now the default config in `defaultconfigs` is checked first if it contains an entry for the value being corrected
- `ModConfig` is no longer wrapped internally, changes to final fields are now applied using `Unsafe`
### Fixed
- Fixed start-up crash on Minecraft 1.18 & 1.18.1

## [v3.0.0-1.18.2] - 2023-02-27
- Ported from 1.19 version

[Keep a Changelog]: https://keepachangelog.com/en/1.0.0/
