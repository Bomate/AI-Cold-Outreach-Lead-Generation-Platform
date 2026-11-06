package com.example.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.BusinessLead
import com.example.data.model.PsychologyAngle

@Composable
fun AddEditLeadDialog(
    leadToEdit: BusinessLead? = null,
    onDismiss: () -> Unit,
    onSave: (
        companyName: String,
        contactName: String,
        email: String,
        role: String,
        industry: String,
        companySize: String,
        painPoint: String,
        targetGoal: String,
        website: String,
        preferredAngle: PsychologyAngle
    ) -> Unit
) {
    var companyName by remember { mutableStateOf(leadToEdit?.companyName ?: "") }
    var contactName by remember { mutableStateOf(leadToEdit?.contactName ?: "") }
    var email by remember { mutableStateOf(leadToEdit?.email ?: "") }
    var role by remember { mutableStateOf(leadToEdit?.role ?: "Founder & CEO") }
    var industry by remember { mutableStateOf(leadToEdit?.industry ?: "B2B SaaS") }
    var companySize by remember { mutableStateOf(leadToEdit?.companySize ?: "25 employees") }
    var painPoint by remember { mutableStateOf(leadToEdit?.painPoint ?: "") }
    var targetGoal by remember { mutableStateOf(leadToEdit?.targetGoal ?: "") }
    var website by remember { mutableStateOf(leadToEdit?.website ?: "") }
    var preferredAngle by remember { mutableStateOf(leadToEdit?.preferredAngle ?: PsychologyAngle.SURVEY_HOOK) }

    var hasAttemptedSubmit by remember { mutableStateOf(false) }

    val isCompanyValid = companyName.isNotBlank()
    val isContactValid = contactName.isNotBlank()
    val isEmailValid = email.isNotBlank() && email.contains("@")
    val isPainPointValid = painPoint.isNotBlank()

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .padding(vertical = 24.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Header
                Text(
                    text = if (leadToEdit == null) "Target New Business" else "Edit Business Lead",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Capture business pain points and select the optimal psychological outreach strategy.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Company Name
                OutlinedTextField(
                    value = companyName,
                    onValueChange = { companyName = it },
                    label = { Text("Company Name *") },
                    placeholder = { Text("e.g. Acme Health Corp") },
                    leadingIcon = { Icon(Icons.Default.Business, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth().testTag("lead_company_input"),
                    singleLine = true,
                    isError = hasAttemptedSubmit && !isCompanyValid
                )

                // Decision Maker Name & Role
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = contactName,
                        onValueChange = { contactName = it },
                        label = { Text("Contact Name *") },
                        placeholder = { Text("e.g. Sarah Jenkins") },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                        modifier = Modifier.weight(1f).testTag("lead_name_input"),
                        singleLine = true,
                        isError = hasAttemptedSubmit && !isContactValid
                    )
                    OutlinedTextField(
                        value = role,
                        onValueChange = { role = it },
                        label = { Text("Role") },
                        placeholder = { Text("VP of Sales") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                // Business Email
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Business Email * (Will be auto-verified)") },
                    placeholder = { Text("sarah@acmehealth.com") },
                    leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth().testTag("lead_email_input"),
                    singleLine = true,
                    isError = hasAttemptedSubmit && !isEmailValid
                )

                // Industry & Company Size
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = industry,
                        onValueChange = { industry = it },
                        label = { Text("Industry") },
                        placeholder = { Text("B2B SaaS") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = companySize,
                        onValueChange = { companySize = it },
                        label = { Text("Company Size") },
                        placeholder = { Text("50 employees") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                // Specific Pain Point
                OutlinedTextField(
                    value = painPoint,
                    onValueChange = { painPoint = it },
                    label = { Text("Exact Business Pain Point *") },
                    placeholder = { Text("e.g. 45% onboarding drop-off due to manual email delays") },
                    leadingIcon = { Icon(Icons.Default.Warning, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth().testTag("lead_painpoint_input"),
                    minLines = 2,
                    isError = hasAttemptedSubmit && !isPainPointValid
                )

                // Desired Goal
                OutlinedTextField(
                    value = targetGoal,
                    onValueChange = { targetGoal = it },
                    label = { Text("Target Goal / Desired Outcome") },
                    placeholder = { Text("e.g. Accelerate pipeline activation by 35%") },
                    leadingIcon = { Icon(Icons.Default.TrendingUp, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                // Psychology Strategy Selection
                Text(
                    text = "Target Psychology Angle",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    PsychologyAngle.values().forEach { angle ->
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (preferredAngle == angle) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { preferredAngle = angle }
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = preferredAngle == angle,
                                    onClick = { preferredAngle = angle }
                                )
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = angle.title,
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = angle.psychologyHook,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Cancel")
                    }

                    Button(
                        onClick = {
                            hasAttemptedSubmit = true
                            if (isCompanyValid && isContactValid && isEmailValid && isPainPointValid) {
                                onSave(
                                    companyName.trim(),
                                    contactName.trim(),
                                    email.trim(),
                                    role.trim().ifEmpty { "Decision Maker" },
                                    industry.trim().ifEmpty { "General Business" },
                                    companySize.trim().ifEmpty { "10-50 employees" },
                                    painPoint.trim(),
                                    targetGoal.trim().ifEmpty { "Improve operational efficiency" },
                                    website.trim(),
                                    preferredAngle
                                )
                            }
                        },
                        modifier = Modifier.weight(1.4f).testTag("save_lead_button"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(if (leadToEdit == null) "Add & Verify" else "Save Changes")
                    }
                }
            }
        }
    }
}
