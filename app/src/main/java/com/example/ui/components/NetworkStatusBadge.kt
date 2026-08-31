package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CellTower
import androidx.compose.material.icons.filled.Lan
import androidx.compose.material.icons.filled.SignalCellularConnectedNoInternet0Bar
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.network.NetworkInfoState
import com.example.data.network.NetworkType
import com.example.ui.theme.AndroidGreen
import com.example.ui.theme.HologramCyan
import com.example.ui.theme.LaserRose
import com.example.ui.theme.NeonLime
import com.example.ui.theme.ObsidianBorder
import com.example.ui.theme.ObsidianDark
import com.example.ui.theme.QuantumAmber
import com.example.ui.theme.TextDisabled
import com.example.ui.theme.TextMediumEmphasis
import com.example.util.NetworkRoutingMode

@Composable
fun NetworkStatusBadge(
    networkInfo: NetworkInfoState,
    routingMode: NetworkRoutingMode,
    modifier: Modifier = Modifier
) {
    val icon: ImageVector = when (networkInfo.type) {
        NetworkType.WIFI -> Icons.Default.Wifi
        NetworkType.CELLULAR -> Icons.Default.CellTower
        NetworkType.ETHERNET -> Icons.Default.Lan
        NetworkType.VPN -> Icons.Default.VpnKey
        NetworkType.NONE -> Icons.Default.SignalCellularConnectedNoInternet0Bar
    }

    val statusColor = when {
        !networkInfo.isConnected -> LaserRose
        networkInfo.type == NetworkType.WIFI -> AndroidGreen
        networkInfo.type == NetworkType.CELLULAR -> HologramCyan
        else -> QuantumAmber
    }

    LiquidGlassCard(
        modifier = modifier
            .fillMaxWidth()
            .testTag("network_status_badge"),
        shape = RoundedCornerShape(18.dp),
        accentColor = statusColor
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Network Name and Icon
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(statusColor.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = networkInfo.typeName,
                            tint = statusColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .clip(CircleShape)
                                    .background(if (networkInfo.isConnected) NeonLime else LaserRose)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = networkInfo.operatorOrSsid,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                        Text(
                            text = if (networkInfo.isConnected) {
                                "${networkInfo.typeName} ${if (networkInfo.frequencyBand.isNotEmpty()) "• ${networkInfo.frequencyBand}" else ""}"
                            } else "Offline / No Active Link",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextMediumEmphasis,
                            fontSize = 11.5.sp
                        )
                    }
                }

                // Routing Mode Tag
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(ObsidianDark)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = routingMode.label,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = when (routingMode) {
                            NetworkRoutingMode.WIFI_ONLY -> AndroidGreen
                            NetworkRoutingMode.CELLULAR_ONLY -> HologramCyan
                            NetworkRoutingMode.AUTO_ALL -> NeonLime
                        },
                        fontSize = 11.sp
                    )
                }
            }

            if (networkInfo.isConnected) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(ObsidianDark.copy(alpha = 0.6f))
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "IP: ${networkInfo.localIpAddress}",
                        style = MaterialTheme.typography.labelSmall,
                        fontFamily = FontFamily.Monospace,
                        color = TextMediumEmphasis,
                        fontSize = 11.sp
                    )
                    if (networkInfo.linkSpeedMbps > 0) {
                        Text(
                            text = "Link: ~${networkInfo.linkSpeedMbps} Mbps",
                            style = MaterialTheme.typography.labelSmall,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.SemiBold,
                            color = statusColor,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }
    }
}
