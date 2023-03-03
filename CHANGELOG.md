# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog].

## [v1.0.2-1.16.5] - 2023-03-03
### Changed
- Night Config Fixes now also handles individual config values being corrected:
    - In Forge for those cases always the built-in default value for these options is used if the current value cannot be read
    - Now the default config in `defaultconfigs` is checked first if it contains an entry for the value being corrected
- `ModConfig` is no longer wrapped internally, changes to final fields are now applied using `Unsafe`

## [v1.0.1-1.16.5] - 2023-02-27
### Fixed
- Fixed crash on start-up due to missing logging classes

## [v1.0.0-1.16.5] - 2023-02-27
- Ported from 1.19 version

[Keep a Changelog]: https://keepachangelog.com/en/1.0.0/
