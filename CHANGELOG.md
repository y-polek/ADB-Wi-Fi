<!-- Keep a Changelog guide -> https://keepachangelog.com -->

# ADB Wi-Fi Changelog

## [1.2.2]
### Added
- Added support of IntelliJ IDEA 2021.1

## [1.2.1]
### Added
- Added "Connect Device" toolbar action which allows connecting devices by IP address ([Issue #7](https://github.com/y-polek/ADB-Wi-Fi/issues/7))
- Device list contains multiple entries (one for each IP) if the device has multiple IP addresses to make it possible to choose which IP to connect to ([Issue #4](https://github.com/y-polek/ADB-Wi-Fi/issues/4))
- "Restart ADB" toolbar action disconnects all devices before killing ADB server.
  It might be helpful in case if device is in "unauthorized" state after being connected.
- Added implementation of error handler ("Report to Developer" button).

## [1.2.0]
### Added
- [scrcpy](https://github.com/Genymobile/scrcpy) integration

## [1.1.1]
### Fixed
- Fixed obtaining of IP Address when Wi-Fi and mobile network are turned on simultaneously ([Issue #2](https://github.com/y-polek/ADB-Wi-Fi/issues/2)).

## [1.1.0]
### Added
- List of previously connected devices
- Improved "ADB Location" settings

## [1.0.0]
### Added
- Initial release

## [Unreleased]
### Added

### Changed

### Removed

### Fixed

### Security

