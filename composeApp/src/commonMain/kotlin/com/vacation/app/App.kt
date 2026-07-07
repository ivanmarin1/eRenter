package com.vacation.app

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vacation.core.designsystem.theme.VacationTheme
import com.vacation.feature.calendar.ui.ApartmentBookingsRoute
import com.vacation.feature.calendar.ui.ApartmentCalendarRoute
import com.vacation.feature.calendar.ui.CalendarRoute
import com.vacation.feature.calendar.ui.ImportRoute

private enum class Screen(val label: String) {
    Calendar("Calendar"),
    Availability("Availability"),
    Apartments("Apartments"),
    Import("Import"),
}

/**
 * Root composable shared by Android, iOS and Desktop. Hosts the eRenter shell: a compact brand bar
 * with a light/dark toggle, the active screen, and a bottom navigation bar.
 */
@Composable
fun App() {
    var darkTheme by rememberSaveable { mutableStateOf<Boolean?>(null) }
    val systemDark = isSystemInDarkTheme()
    val isDark = darkTheme ?: systemDark

    VacationTheme(darkTheme = isDark) {
        Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            var screen by rememberSaveable { mutableStateOf(Screen.Calendar) }
            Column(Modifier.fillMaxSize()) {
                BrandBar(
                    isDark = isDark,
                    onToggleTheme = { darkTheme = !isDark },
                )
                Box(Modifier.weight(1f).fillMaxSize()) {
                    when (screen) {
                        Screen.Calendar -> CalendarRoute()
                        Screen.Availability -> ApartmentCalendarRoute()
                        Screen.Apartments -> ApartmentBookingsRoute()
                        Screen.Import -> ImportRoute()
                    }
                }
                BottomNav(current = screen, onSelect = { screen = it })
            }
        }
    }
}

@Composable
private fun BrandBar(isDark: Boolean, onToggleTheme: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 18.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Box(
                Modifier.size(9.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(MaterialTheme.colorScheme.primary),
            )
            Text(
                "eRenter",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onBackground,
            )
        }
        // ☀ / ☾ theme toggle — a rounded pill matching the design.
        Surface(
            onClick = onToggleTheme,
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        ) {
            Text(
                text = if (isDark) "☾ Dark" else "☀ Light",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
            )
        }
    }
}

@Composable
private fun BottomNav(current: Screen, onSelect: (Screen) -> Unit) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
    ) {
        val itemColors = NavigationBarItemDefaults.colors(
            selectedIconColor = MaterialTheme.colorScheme.primary,
            selectedTextColor = MaterialTheme.colorScheme.primary,
            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
            indicatorColor = Color.Transparent,
        )
        Screen.entries.forEach { entry ->
            val selected = entry == current
            val tint = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
            NavigationBarItem(
                selected = selected,
                onClick = { onSelect(entry) },
                colors = itemColors,
                icon = {
                    when (entry) {
                        Screen.Calendar -> CalendarIcon(tint)
                        Screen.Availability -> GridIcon(tint)
                        Screen.Apartments -> BuildingIcon(tint)
                        Screen.Import -> ImportIcon(tint)
                    }
                },
                label = { Text(entry.label, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold) },
            )
        }
    }
}
