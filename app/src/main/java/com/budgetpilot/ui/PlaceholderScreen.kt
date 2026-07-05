package com.budgetpilot.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.budgetpilot.R
import com.budgetpilot.core.designsystem.components.EmptyState
import com.budgetpilot.core.designsystem.theme.BudgetPilotTheme

/** Stand-in for a feature screen until its phase lands: screen name + [EmptyState]. */
@Composable
fun PlaceholderScreen(
    screenName: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = screenName,
            style = MaterialTheme.typography.headlineMedium,
        )
        EmptyState(
            icon = icon,
            title = stringResource(R.string.placeholder_title),
            description = stringResource(R.string.placeholder_description),
        )
    }
}

@PreviewLightDark
@Composable
private fun PlaceholderScreenPreview() {
    BudgetPilotTheme {
        Surface {
            PlaceholderScreen(
                screenName = "Home",
                icon = Icons.Outlined.Home,
            )
        }
    }
}
