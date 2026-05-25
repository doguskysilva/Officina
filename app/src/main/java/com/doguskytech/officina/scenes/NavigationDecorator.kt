package com.doguskytech.officina.scenes

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.scene.Scene
import androidx.navigation3.scene.SceneDecoratorStrategy
import androidx.navigation3.scene.SceneDecoratorStrategyScope
import androidx.window.core.layout.WindowSizeClass
import androidx.window.core.layout.WindowSizeClass.Companion.WIDTH_DP_MEDIUM_LOWER_BOUND
import com.doguskytech.officina.navigation.AppSettings
import com.doguskytech.officina.navigation.ProjectList
import com.doguskytech.officina.navigation.TaskList

// --------------------------------------------------------------------------
// Definição das abas — centralizada aqui para o decorator e o resto do app
// --------------------------------------------------------------------------
data class TabItem(
    val route: NavKey,
    val label: String,
    val icon: ImageVector,
)

val appTabs = listOf(
    TabItem(ProjectList, "Projetos", Icons.Default.Build),
    TabItem(TaskList, "Tarefas", Icons.Default.List),
    TabItem(AppSettings, "Ajustes", Icons.Default.Settings),
)

// --------------------------------------------------------------------------
// SceneDecoratorStrategy — envolve qualquer Scene com NavigationBar/Rail.
// NÃO é chamado para OverlayScene (Dialog, BottomSheet).
// --------------------------------------------------------------------------
class NavDecoratorStrategy<T : Any>(
    private val windowSizeClass: WindowSizeClass,
    private val selectedTab: NavKey,
    private val onTabSelected: (NavKey) -> Unit,
) : SceneDecoratorStrategy<T> {

    override fun SceneDecoratorStrategyScope<T>.decorateScene(scene: Scene<T>): Scene<T> =
        NavDecoratorScene(scene, windowSizeClass, selectedTab, onTabSelected)
}

// --------------------------------------------------------------------------
// DecoratorScene — a Scene envolvida com a UI de navegação
// --------------------------------------------------------------------------
class NavDecoratorScene<T : Any>(
    private val scene: Scene<T>,
    private val windowSizeClass: WindowSizeClass,
    private val selectedTab: NavKey,
    private val onTabSelected: (NavKey) -> Unit,
) : Scene<T> {

    // Regra crítica: derived key a partir da classe E key da scene interna.
    // Se só copiarmos scene.key, NavDisplay não detecta mudança de classe de Scene
    // (ex: SinglePane → ListDetail) e as animações param de funcionar.
    override val key = scene::class to scene.key

    // As três propriedades abaixo sempre vêm da scene interna —
    // o decorator não muda quais entries estão visíveis nem a lógica de back.
    override val entries = scene.entries
    override val previousEntries = scene.previousEntries
    override val metadata = scene.metadata

    override val content: @Composable () -> Unit = {
        val isCompact = !windowSizeClass.isWidthAtLeastBreakpoint(WIDTH_DP_MEDIUM_LOWER_BOUND)

        if (isCompact) {
            // Phone — NavigationBar na base
            Scaffold(
                bottomBar = {
                    NavigationBar {
                        appTabs.forEach { tab ->
                            NavigationBarItem(
                                selected = selectedTab == tab.route,
                                onClick = { onTabSelected(tab.route) },
                                icon = { Icon(tab.icon, contentDescription = tab.label) },
                                label = { Text(tab.label) }
                            )
                        }
                    }
                }
            ) { padding ->
                Box(modifier = Modifier.padding(padding)) {
                    scene.content()
                }
            }
        } else {
            // Tablet — NavigationRail na lateral
            Row(modifier = Modifier.fillMaxSize()) {
                NavigationRail {
                    appTabs.forEach { tab ->
                        NavigationRailItem(
                            selected = selectedTab == tab.route,
                            onClick = { onTabSelected(tab.route) },
                            icon = { Icon(tab.icon, contentDescription = tab.label) },
                            label = { Text(tab.label) }
                        )
                    }
                }
                Box(modifier = Modifier.weight(1f)) {
                    scene.content()
                }
            }
        }
    }
}

// Factory composable — lê WindowSizeClass e cria o decorator com remember.
// É recriado apenas quando windowSizeClass ou selectedTab muda.
@Composable
fun <T : Any> rememberNavDecoratorStrategy(
    selectedTab: NavKey,
    onTabSelected: (NavKey) -> Unit,
): NavDecoratorStrategy<T> {
    val windowSizeClass = currentWindowAdaptiveInfo().windowSizeClass
    return remember(windowSizeClass, selectedTab) {
        NavDecoratorStrategy(windowSizeClass, selectedTab, onTabSelected)
    }
}
