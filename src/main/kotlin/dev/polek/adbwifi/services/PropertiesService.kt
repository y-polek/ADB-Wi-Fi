package dev.polek.adbwifi.services

interface PropertiesService {
    var isLogVisible: Boolean
    var adbLocation: String

    var adbLocationListener: ((location: String, isValid: Boolean) -> Unit)?
}
