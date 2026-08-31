package com.example.data.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import android.telephony.TelephonyManager
import com.example.util.NetworkRoutingMode
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import java.net.Inet4Address
import java.net.NetworkInterface

data class NetworkInfoState(
    val isConnected: Boolean = false,
    val isWifiAvailable: Boolean = false,
    val isCellularAvailable: Boolean = false,
    val type: NetworkType = NetworkType.NONE,
    val typeName: String = "No Connection",
    val operatorOrSsid: String = "Offline",
    val linkSpeedMbps: Int = 0,
    val frequencyMhz: Int = 0,
    val signalStrengthLevel: Int = 0, // 0 to 4
    val rssiDbm: Int = -60, // dBm level
    val signalPercent: Int = 0, // 0 to 100%
    val localIpAddress: String = "Unknown",
    val isMetered: Boolean = false,
    val activeRoutingMode: NetworkRoutingMode = NetworkRoutingMode.AUTO_ALL
) {
    val frequencyBand: String
        get() = when {
            frequencyMhz in 2400..2499 -> "2.4 GHz"
            frequencyMhz in 4900..5900 -> "5 GHz"
            frequencyMhz > 5900 -> "6 GHz (Wi-Fi 6E/7)"
            else -> if (type == NetworkType.CELLULAR) "5G / LTE" else ""
        }

    val channel: Int
        get() = when {
            frequencyMhz in 2412..2484 -> (frequencyMhz - 2407) / 5
            frequencyMhz in 5170..5825 -> (frequencyMhz - 5000) / 5
            frequencyMhz in 5955..7115 -> (frequencyMhz - 5950) / 5
            else -> 0
        }

    val standardProtocol: String
        get() = when {
            frequencyMhz > 5900 -> "Wi-Fi 6E / 7 (802.11ax/be)"
            frequencyMhz in 4900..5900 -> "Wi-Fi 5 / 6 (802.11ac/ax)"
            frequencyMhz in 2400..2499 -> "Wi-Fi 4 (802.11n)"
            type == NetworkType.CELLULAR -> "Cellular 5G NR / LTE-A"
            type == NetworkType.ETHERNET -> "Gigabit Ethernet (802.3ab)"
            else -> "Standard Protocol"
        }
}

enum class NetworkType {
    WIFI,
    CELLULAR,
    ETHERNET,
    VPN,
    NONE
}

class NetworkMonitor(private val context: Context) {
    private val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
    private val wifiManager =
        context.applicationContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager
    private val telephonyManager =
        context.getSystemService(Context.TELEPHONY_SERVICE) as? TelephonyManager

    @Volatile
    var wifiNetwork: Network? = null
        private set

    @Volatile
    var cellularNetwork: Network? = null
        private set

    fun getNetworkForMode(mode: NetworkRoutingMode): Network? {
        return when (mode) {
            NetworkRoutingMode.WIFI_ONLY -> wifiNetwork ?: connectivityManager?.activeNetwork
            NetworkRoutingMode.CELLULAR_ONLY -> cellularNetwork ?: connectivityManager?.activeNetwork
            NetworkRoutingMode.AUTO_ALL -> null // null means system default
        }
    }

    fun isModeAvailable(mode: NetworkRoutingMode): Boolean {
        return when (mode) {
            NetworkRoutingMode.AUTO_ALL -> (wifiNetwork != null || cellularNetwork != null || connectivityManager?.activeNetwork != null)
            NetworkRoutingMode.WIFI_ONLY -> wifiNetwork != null
            NetworkRoutingMode.CELLULAR_ONLY -> cellularNetwork != null
        }
    }

    fun observeNetwork(): Flow<NetworkInfoState> = callbackFlow {
        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                updateTrackedNetworks()
                trySend(getCurrentNetworkInfo())
            }

            override fun onLost(network: Network) {
                if (network == wifiNetwork) wifiNetwork = null
                if (network == cellularNetwork) cellularNetwork = null
                updateTrackedNetworks()
                trySend(getCurrentNetworkInfo())
            }

            override fun onCapabilitiesChanged(
                network: Network,
                networkCapabilities: NetworkCapabilities
            ) {
                if (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                    wifiNetwork = network
                }
                if (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                    cellularNetwork = network
                }
                trySend(getCurrentNetworkInfo())
            }
        }

        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()

        connectivityManager?.registerNetworkCallback(request, callback)
        updateTrackedNetworks()
        trySend(getCurrentNetworkInfo())

        awaitClose {
            try {
                connectivityManager?.unregisterNetworkCallback(callback)
            } catch (_: Exception) {}
        }
    }

    private fun updateTrackedNetworks() {
        val cm = connectivityManager ?: return
        val allNetworks = cm.allNetworks
        var foundWifi: Network? = null
        var foundCellular: Network? = null

        for (net in allNetworks) {
            val caps = cm.getNetworkCapabilities(net) ?: continue
            if (caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)) {
                if (caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                    foundWifi = net
                }
                if (caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                    foundCellular = net
                }
            }
        }
        wifiNetwork = foundWifi
        cellularNetwork = foundCellular
    }

    fun getCurrentNetworkInfo(): NetworkInfoState {
        val cm = connectivityManager ?: return NetworkInfoState()
        updateTrackedNetworks()
        val activeNetwork = cm.activeNetwork
        val capabilities = if (activeNetwork != null) cm.getNetworkCapabilities(activeNetwork) else null

        val isConnected = capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
        val isMetered = capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_METERED) == false
        val ip = getLocalIpAddress()

        val isWifiAvail = wifiNetwork != null
        val isCellularAvail = cellularNetwork != null

        if (capabilities == null) {
            return NetworkInfoState(
                isConnected = false,
                isWifiAvailable = isWifiAvail,
                isCellularAvailable = isCellularAvail,
                type = NetworkType.NONE,
                typeName = "No Connection",
                operatorOrSsid = "Offline",
                localIpAddress = ip
            )
        }

        return when {
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> {
                var ssid = "Wi-Fi Network"
                var linkSpeed = capabilities.linkDownstreamBandwidthKbps / 1000
                var freq = 0
                var signal = 4
                var rssiDbm = -55

                try {
                    val wifiInfo: WifiInfo? = wifiManager?.connectionInfo
                    if (wifiInfo != null) {
                        val rawSsid = wifiInfo.ssid
                        if (rawSsid.isNotEmpty() && rawSsid != "<unknown ssid>") {
                            ssid = rawSsid.replace("\"", "")
                        }
                        if (wifiInfo.linkSpeed > 0) {
                            linkSpeed = wifiInfo.linkSpeed
                        }
                        freq = wifiInfo.frequency
                        if (wifiInfo.rssi != -127 && wifiInfo.rssi != 0) {
                            rssiDbm = wifiInfo.rssi
                        }
                        signal = WifiManager.calculateSignalLevel(rssiDbm, 5)
                    }
                } catch (_: Exception) {}

                // Convert dBm to 0-100% (-100 dBm = 0%, -50 dBm = 100%)
                val signalPct = ((rssiDbm + 100) * 2).coerceIn(5, 100)

                NetworkInfoState(
                    isConnected = isConnected,
                    isWifiAvailable = true,
                    isCellularAvailable = isCellularAvail,
                    type = NetworkType.WIFI,
                    typeName = "Wi-Fi",
                    operatorOrSsid = ssid,
                    linkSpeedMbps = linkSpeed,
                    frequencyMhz = freq,
                    signalStrengthLevel = signal,
                    rssiDbm = rssiDbm,
                    signalPercent = signalPct,
                    localIpAddress = ip,
                    isMetered = isMetered
                )
            }
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> {
                val operatorName = telephonyManager?.networkOperatorName?.takeIf { it.isNotBlank() }
                    ?: telephonyManager?.simOperatorName?.takeIf { it.isNotBlank() }
                    ?: "Cellular Provider"

                val downstreamMbps = capabilities.linkDownstreamBandwidthKbps / 1000
                val defaultCellularDbm = -75
                val cellPct = ((defaultCellularDbm + 110) * 1.8).toInt().coerceIn(10, 95)

                NetworkInfoState(
                    isConnected = isConnected,
                    isWifiAvailable = isWifiAvail,
                    isCellularAvailable = true,
                    type = NetworkType.CELLULAR,
                    typeName = "Mobile Data",
                    operatorOrSsid = operatorName,
                    linkSpeedMbps = downstreamMbps,
                    frequencyMhz = 0,
                    signalStrengthLevel = 4,
                    rssiDbm = defaultCellularDbm,
                    signalPercent = cellPct,
                    localIpAddress = ip,
                    isMetered = true
                )
            }
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> {
                NetworkInfoState(
                    isConnected = isConnected,
                    isWifiAvailable = isWifiAvail,
                    isCellularAvailable = isCellularAvail,
                    type = NetworkType.ETHERNET,
                    typeName = "Ethernet",
                    operatorOrSsid = "Wired Connection",
                    linkSpeedMbps = (capabilities.linkDownstreamBandwidthKbps / 1000).coerceAtLeast(1000),
                    signalStrengthLevel = 4,
                    rssiDbm = -30,
                    signalPercent = 100,
                    localIpAddress = ip,
                    isMetered = false
                )
            }
            else -> {
                NetworkInfoState(
                    isConnected = isConnected,
                    isWifiAvailable = isWifiAvail,
                    isCellularAvailable = isCellularAvail,
                    type = NetworkType.VPN,
                    typeName = "Connected (Other)",
                    operatorOrSsid = "Network Adapter",
                    localIpAddress = ip,
                    isMetered = isMetered
                )
            }
        }
    }

    private fun getLocalIpAddress(): String {
        try {
            val interfaces = NetworkInterface.getNetworkInterfaces() ?: return "Unknown"
            while (interfaces.hasMoreElements()) {
                val networkInterface = interfaces.nextElement()
                val addresses = networkInterface.inetAddresses
                while (addresses.hasMoreElements()) {
                    val address = addresses.nextElement()
                    if (!address.isLoopbackAddress && address is Inet4Address) {
                        return address.hostAddress ?: "Unknown"
                    }
                }
            }
        } catch (_: Exception) {}
        return "Unknown"
    }
}
