package com.lighthousepark.intervalsgym.strength.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.lighthousepark.intervalsgym.core.debugContentDescription

@Composable
internal fun UnilateralSetSideRow(
    label: String,
    weightKg: String,
    reps: String,
    contentAlpha: Float,
    onWeightChange: (String) -> Unit,
    onRepsChange: (String) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .width(28.dp)
                .alpha(contentAlpha)
        )
        SetMetricField(
            value = weightKg,
            onValueChange = onWeightChange,
            unit = "kg",
            modifier = Modifier
                .weight(1f)
                .alpha(contentAlpha)
        )
        Text(
            text = "/",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.alpha(contentAlpha)
        )
        SetMetricField(
            value = reps,
            onValueChange = onRepsChange,
            unit = "회",
            modifier = Modifier
                .weight(1f)
                .alpha(contentAlpha)
        )
    }
}

@Composable
internal fun SetMetricField(
    value: String,
    unit: String,
    modifier: Modifier = Modifier,
    prefix: String? = null,
    testContentDescription: String? = null,
    onValueChange: (String) -> Unit,
) {
    var fieldValue by remember(value) {
        mutableStateOf(TextFieldValue(value, selection = TextRange(value.length)))
    }
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        prefix?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.SemiBold
            )
        }
        BasicTextField(
            value = fieldValue,
            onValueChange = { next ->
                if (next.text.all { it.isDigit() || it == '.' }) {
                    fieldValue = next.copy(selection = TextRange(next.text.length))
                    onValueChange(next.text)
                }
            },
            modifier = Modifier
                .weight(1f)
                .then(
                    testContentDescription?.let { Modifier.debugContentDescription(it) }
                        ?: Modifier
                ),
            singleLine = true,
            textStyle = MaterialTheme.typography.titleLarge.copy(
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.End,
                fontWeight = FontWeight.Bold
            ),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            decorationBox = { innerTextField ->
                if (value.isBlank()) {
                    Text(
                        text = "-",
                        modifier = Modifier.fillMaxWidth(),
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.End,
                        fontWeight = FontWeight.Bold
                    )
                }
                innerTextField()
            }
        )
        Text(
            text = unit,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.SemiBold
        )
    }
}
