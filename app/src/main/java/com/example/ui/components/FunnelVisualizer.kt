package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.OutreachAnalytics
import com.example.ui.theme.Cyan500
import com.example.ui.theme.Emerald500
import com.example.ui.theme.Indigo500
import com.example.ui.theme.Indigo600
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate700

@Composable
fun FunnelVisualizer(
    analytics: OutreachAnalytics,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Outreach Pipeline Funnel",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Psychology-driven conversion drop-off",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Text(
                    text = "${analytics.totalLeads} Total Leads",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }

            val maxVal = maxOf(analytics.totalLeads, 1).toFloat()

            FunnelStepRow(
                label = "1. Targeted Leads",
                count = analytics.totalLeads,
                fraction = analytics.totalLeads / maxVal,
                color = Indigo600,
                percentageNote = "100%"
            )

            FunnelStepRow(
                label = "2. Verified Inboxes",
                count = analytics.verifiedLeads,
                fraction = analytics.verifiedLeads / maxVal,
                color = Indigo500,
                percentageNote = "${analytics.verificationRate.toInt()}% deliverable"
            )

            FunnelStepRow(
                label = "3. Dispatched Emails",
                count = analytics.totalSent,
                fraction = analytics.totalSent / maxVal,
                color = Cyan500,
                percentageNote = if (analytics.totalLeads > 0) "${((analytics.totalSent.toFloat() / analytics.totalLeads) * 100).toInt()}% contacted" else "0%"
            )

            FunnelStepRow(
                label = "4. Opened (Subject Hook)",
                count = analytics.totalOpened,
                fraction = analytics.totalOpened / maxVal,
                color = Cyan500,
                percentageNote = "${analytics.openRate.toInt()}% open rate"
            )

            FunnelStepRow(
                label = "5. Clicked (Survey/Diagnostic)",
                count = analytics.totalClicked,
                fraction = analytics.totalClicked / maxVal,
                color = Emerald500,
                percentageNote = "${analytics.clickThroughRate.toInt()}% CTR"
            )

            FunnelStepRow(
                label = "6. Positive Replies",
                count = analytics.totalReplied,
                fraction = analytics.totalReplied / maxVal,
                color = Emerald500,
                percentageNote = "${analytics.responseRate.toInt()}% reply rate"
            )
        }
    }
}

@Composable
private fun FunnelStepRow(
    label: String,
    count: Int,
    fraction: Float,
    color: Color,
    percentageNote: String
) {
    val animatedProgress by animateFloatAsState(targetValue = fraction.coerceIn(0.04f, 1f), label = "funnel_bar")

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "$count",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "($percentageNote)",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 11.sp
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(10.dp)
                .clip(RoundedCornerShape(5.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(animatedProgress)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(5.dp))
                    .background(color)
            )
        }
    }
}
