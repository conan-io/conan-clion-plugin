<!-- Keep a Changelog guide -> https://keepachangelog.com -->

# Conan CLion plugin changelog

## [Unreleased]

## [2.0.3] - 2023-11-13

### Changed

- Fix selection of Conan executable when named "conan.exe"
- Fix plugin compatibility with 2023.3

## [2.0.2] - 2023-09-07

### Bugfix

- Fixed when only the Conan executable path was changed, it was not updated
- Fixed adding multiple CONAN_COMMAND

## [2.0.1] - 2023-08-31

### Changed

- Use relative (instead of absolute) path to pass the conan_provider.cmake to CMake

### Bugfix

- Fixed crash due to library data race condition when first using the plugin

## [2.0.0] - 2023-08-30

### Added

- Add description to plugin.xml
- Bump minimun required CLion version to 223 (2022.3) to ensure the correct CMake version
- New Conan CLion plugin compatible with Conan 2.X

## [2.0.0-beta.1] - 2023-08-24

### Added

- New Conan CLion plugin compatible with Conan 2.X

[Unreleased]: https://github.com/conan-io/conan-clion-plugin//compare/v2.0.3...HEAD
[2.0.3]: https://github.com/conan-io/conan-clion-plugin//compare/v2.0.2...v2.0.3
[2.0.2]: https://github.com/conan-io/conan-clion-plugin//compare/v2.0.1...v2.0.2
[2.0.1]: https://github.com/conan-io/conan-clion-plugin//compare/v2.0.0...v2.0.1
[2.0.0]: https://github.com/conan-io/conan-clion-plugin//compare/v2.0.0-beta.1...v2.0.0
[2.0.0-beta.1]: https://github.com/conan-io/conan-clion-plugin//commits/v2.0.0-beta.1
