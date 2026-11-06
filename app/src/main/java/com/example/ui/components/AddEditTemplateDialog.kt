package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.QuestionAnswer
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Title
import androidx.compose.material.icons.filled.Update
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.OutreachTemplate
import com.example.data.model.PsychologyAngle
import com.example.ui.theme.Cyan100
import com.example.ui.theme.Cyan600
import com.example.ui.theme.Emerald600

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddEditTemplateDialog(
    templateToEdit: OutreachTemplate? = null,
    onDismiss: () -> Unit,
    onSave: (
        title: String,
        category: String,
        psychologyAngle: PsychologyAngle,
        subjectLineTemplate: String,
        subjectAlternativeTemplate: String,
        bodyTemplate: String,
        surveyQuestionTemplate: String,
        followUpNudgeTemplate: String,
        psychologyInsight: String
    ) -> Unit
) {
    var title by remember { mutableStateOf(templateToEdit?.title ?: "") }
    var category by remember { mutableStateOf(templateToEdit?.category ?: "Custom") }
    var psychologyAngle by remember { mutableStateOf(templateToEdit?.psychologyAngle ?: PsychologyAngle.SURVEY_HOOK) }
    var subjectLineTemplate by remember { mutableStateOf(templateToEdit?.subjectLineTemplate ?: "") }
    var subjectAlternativeTemplate by remember { mutableStateOf(templateToEdit?.subjectAlternativeTemplate ?: "") }
    var bodyTemplate by remember { mutableStateOf(templateToEdit?.bodyTemplate ?: "") }
    var surveyQuestionTemplate by remember { mutableStateOf(templateToEdit?.surveyQuestionTemplate ?: "") }
    var followUpNudgeTemplate by remember { mutableStateOf(templateToEdit?.followUpNudgeTemplate ?: "") }
    var psychologyInsight by remember { mutableStateOf(templateToEdit?.psychologyInsight ?: psychologyAngle.psychologyHook) }

    val isEdit = templateToEdit != null

    // Variable insertion helper tags
    val variableTags = listOf(
        "{{contactName}}",
        "{{firstName}}",
        "{{companyName}}",
        "{{painPoint}}",
        "{{targetGoal}}",
        "{{industry}}",
        "{{role}}",
        "{{website}}"
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp, vertical = 20.dp)
                .clip(RoundedCornerShape(24.dp))
                .testTag("add_edit_template_dialog"),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (isEdit) "Edit Outreach Template" else "New Psychology Template",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Craft reusable cold outreach sequences with dynamic tags",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Scrollable Form
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Title and Category
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = title,
                            onValueChange = { title = it },
                            label = { Text("Template Name") },
                            placeholder = { Text("e.g. 10-Sec Micro Survey") },
                            leadingIcon = { Icon(Icons.Default.Title, contentDescription = null) },
                            modifier = Modifier
                                .weight(1.3f)
                                .testTag("template_title_input"),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = category,
                            onValueChange = { category = it },
                            label = { Text("Category") },
                            placeholder = { Text("Micro-Surveys") },
                            leadingIcon = { Icon(Icons.Default.Category, contentDescription = null) },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("template_category_input"),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )
                    }

                    // Psychology Framework Selector
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Psychology, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.size(6.dp))
                                Text("Psychological Angle", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                            }

                            PsychologyAngle.values().forEach { angle ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            psychologyAngle = angle
                                            if (psychologyInsight.isBlank() || psychologyInsight == psychologyAngle.psychologyHook) {
                                                psychologyInsight = angle.psychologyHook
                                            }
                                        }
                                        .padding(vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(
                                        selected = psychologyAngle == angle,
                                        onClick = {
                                            psychologyAngle = angle
                                            if (psychologyInsight.isBlank() || psychologyInsight == psychologyAngle.psychologyHook) {
                                                psychologyInsight = angle.psychologyHook
                                            }
                                        }
                                    )
                                    Column(modifier = Modifier.padding(start = 4.dp)) {
                                        Text(text = angle.title, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                                        Text(text = angle.subtitle, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            }
                        }
                    }

                    // Variable Helper Bar
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Cyan100.copy(alpha = 0.4f))
                    ) {
                        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Add, contentDescription = null, tint = Cyan600, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.size(4.dp))
                                Text("Insert Dynamic Placeholder Tag:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = Cyan600)
                            }
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                variableTags.forEach { tag ->
                                    FilterChip(
                                        selected = false,
                                        onClick = {
                                            // Append to bodyTemplate
                                            bodyTemplate = if (bodyTemplate.endsWith(" ") || bodyTemplate.isEmpty()) "$bodyTemplate$tag " else "$bodyTemplate $tag "
                                        },
                                        label = { Text(tag, fontSize = 11.sp, fontWeight = FontWeight.Medium) },
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Subject Lines
                    OutlinedTextField(
                        value = subjectLineTemplate,
                        onValueChange = { subjectLineTemplate = it },
                        label = { Text("Primary Subject Line Template") },
                        placeholder = { Text("quick question on {{companyName}}'s {{painPoint}}") },
                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("template_subject_input"),
                        shape = RoundedCornerShape(12.dp)
                    )

                    OutlinedTextField(
                        value = subjectAlternativeTemplate,
                        onValueChange = { subjectAlternativeTemplate = it },
                        label = { Text("A/B Alternative Subject Line Template") },
                        placeholder = { Text("{{contactName}}, thought on {{targetGoal}}?") },
                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("template_alt_subject_input"),
                        shape = RoundedCornerShape(12.dp)
                    )

                    // Body
                    OutlinedTextField(
                        value = bodyTemplate,
                        onValueChange = { bodyTemplate = it },
                        label = { Text("Email Body Template") },
                        placeholder = { Text("Hi {{contactName}},\n\nNoticed {{companyName}}'s focus in {{industry}}...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .testTag("template_body_input"),
                        shape = RoundedCornerShape(12.dp)
                    )

                    // Survey Hook & Follow-up
                    OutlinedTextField(
                        value = surveyQuestionTemplate,
                        onValueChange = { surveyQuestionTemplate = it },
                        label = { Text("Diagnostic Survey Question Template") },
                        placeholder = { Text("Between {{painPoint}} and bandwidth, which is the main bottleneck?") },
                        leadingIcon = { Icon(Icons.Default.QuestionAnswer, contentDescription = null) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("template_survey_input"),
                        shape = RoundedCornerShape(12.dp)
                    )

                    OutlinedTextField(
                        value = followUpNudgeTemplate,
                        onValueChange = { followUpNudgeTemplate = it },
                        label = { Text("Day +3 Follow-Up Nudge Template") },
                        placeholder = { Text("Hi {{contactName}}, circling back on my note regarding {{targetGoal}}...") },
                        leadingIcon = { Icon(Icons.Default.Update, contentDescription = null) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("template_nudge_input"),
                        shape = RoundedCornerShape(12.dp)
                    )

                    // Psychology Insight
                    OutlinedTextField(
                        value = psychologyInsight,
                        onValueChange = { psychologyInsight = it },
                        label = { Text("Conversion & Psychology Rationale") },
                        placeholder = { Text("Why this email framework converts...") },
                        leadingIcon = { Icon(Icons.Default.Lightbulb, contentDescription = null) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                            .testTag("template_insight_input"),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Bottom Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancel")
                    }

                    Button(
                        onClick = {
                            if (title.isNotBlank() && bodyTemplate.isNotBlank()) {
                                onSave(
                                    title,
                                    category,
                                    psychologyAngle,
                                    subjectLineTemplate.ifBlank { "quick question for {{contactName}}" },
                                    subjectAlternativeTemplate.ifBlank { "thought for {{companyName}}" },
                                    bodyTemplate,
                                    surveyQuestionTemplate,
                                    followUpNudgeTemplate,
                                    psychologyInsight
                                )
                            }
                        },
                        enabled = title.isNotBlank() && bodyTemplate.isNotBlank(),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1.3f)
                            .testTag("btn_save_template")
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.size(6.dp))
                        Text(if (isEdit) "Update Template" else "Save Template")
                    }
                }
            }
        }
    }
}
