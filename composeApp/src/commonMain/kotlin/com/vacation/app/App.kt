package com.vacation.app

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.vacation.core.designsystem.theme.VacationTheme
import com.vacation.feature.calendar.ui.CalendarRoute
import com.vacation.feature.calendar.ui.ImportRoute
import com.vacation.feature.calendar.ui.ManageApartmentsRoute

private enum class Screen(val label: String) {
    Calendar("Calendar"),
    Apartments("Apartments"),
    Import("Import"),
}

/** Root composable shared by Android, iOS and Desktop. Hosts a simple top nav over the screens. */
@Composable
fun App() {
    VacationTheme {
        Surface(Modifier.fillMaxSize()) {
            var screen by remember { mutableStateOf(Screen.Calendar) }
            Column(Modifier.fillMaxSize()) {
                NavBar(current = screen, onSelect = { screen = it })
                Box(Modifier.fillMaxSize().navigationBarsPadding()) {
                    when (screen) {
                        Screen.Calendar -> CalendarRoute()
                        Screen.Apartments -> ManageApartmentsRoute()
                        Screen.Import -> ImportRoute()
                    }
                }
            }
        }
    }
}

@Composable
private fun NavBar(current: Screen, onSelect: (Screen) -> Unit) {
    Surface(tonalElevation = 3.dp) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                // Keep the buttons clear of the status bar when drawing edge-to-edge (Android).
                // Zero on Desktop/iOS, so it is safe everywhere.
                .statusBarsPadding()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Screen.entries.forEach { entry ->
                TextButton(onClick = { onSelect(entry) }) {
                    Text(
                        entry.label,
                        color = if (entry == current) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}
