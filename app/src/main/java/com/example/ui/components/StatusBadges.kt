package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Dangerous
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.OutreachStatus
import com.example.data.model.PsychologyAngle
import com.example.data.model.VerificationStatus
import com.example.ui.theme.Amber100
import com.example.ui.theme.Amber600
import com.example.ui.theme.Cyan100
import com.example.ui.theme.Cyan600
import com.example.ui.theme.Emerald100
import com.example.ui.theme.Emerald600
import com.example.ui.theme.Indigo100
import com.example.ui.theme.Indigo600
import com.example.ui.theme.Rose100
import com.example.ui.theme.Rose600
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate700

@Composable
fun VerificationBadge(
    status: VerificationStatus,
    score: Int,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val (bgColor, textColor, icon) = when (status) {
        VerificationStatus.VALID -> Triple(Emerald100, Emerald600, Icons.Default.CheckCircle)
        VerificationStatus.RISKY -> Triple(Amber100, Amber600, Icons.Default.Warning)
        VerificationStatus.INVALID -> Triple(Rose100, Rose600, Icons.Default.Dangerous)
        VerificationStatus.UNVERIFIED -> Triple(Slate200, Slate700, Icons.Default.HourglassEmpty)
    }

    val label = when (status) {
        VerificationStatus.VALID -> "Verified ($score%)"
        VerificationStatus.RISKY -> "Risky ($score%)"
        VerificationStatus.INVALID -> "Invalid ($score%)"
        VerificationStatus.UNVERIFIED -> "Unverified"
    }

    Row(
        modifier = modifier
            .background(bgColor, RoundedCornerShape(12.dp))
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = textColor,
            modifier = Modifier.size(13.dp)
        )
        Text(
            text = label,
            color = textColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
fun OutreachStatusBadge(
    status: OutreachStatus,
    modifier: Modifier = Modifier
) {
    val (bgColor, textColor, label) = when (status) {
        OutreachStatus.DRAFT -> Triple(Slate200, Slate700, "Draft")
        OutreachStatus.SCHEDULED -> Triple(Indigo100, Indigo600, "Scheduled")
        OutreachStatus.SENT -> Triple(Indigo100, Indigo600, "Sent")
        OutreachStatus.OPENED -> Triple(Cyan100, Cyan600, "Opened")
        OutreachStatus.CLICKED -> Triple(Emerald100, Emerald600, "Clicked")
        OutreachStatus.REPLIED -> Triple(Emerald100, Emerald600, "Replied 🔥")
        OutreachStatus.BOUNCED -> Triple(Rose100, Rose600, "Bounced")
    }

    Box(
        modifier = modifier
            .background(bgColor, RoundedCornerShape(12.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = textColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun PsychologyAngleChip(
    angle: PsychologyAngle,
    selected: Boolean = false,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = MaterialTheme.colorScheme.background.red < 0.2f
    val bg = if (selected) {
        MaterialTheme.colorScheme.primary
    } else {
        if (isDark) MaterialTheme.colorScheme.surfaceVariant else Slate100
    }
    val contentColor = if (selected) Color.White else MaterialTheme.colorScheme.onSurface

    Row(
        modifier = modifier
            .background(bg, RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(
                    if (selected) Color.White else MaterialTheme.colorScheme.primary,
                    CircleShape
                )
        )
        Text(
            text = angle.title,
            color = contentColor,
            fontSize = 12.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
        )
    }
}
