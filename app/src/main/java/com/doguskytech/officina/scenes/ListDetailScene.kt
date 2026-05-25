package com.doguskytech.officina.scenes

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.VerticalDivider
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavMetadataKey
import androidx.navigation3.runtime.contains
import androidx.navigation3.runtime.metadata
import androidx.navigation3.scene.Scene
import androidx.navigation3.scene.SceneStrategy
import androidx.navigation3.scene.SceneStrategyScope
import androidx.window.core.layout.WindowSizeClass
import androidx.window.core.layout.WindowSizeClass.Companion.WIDTH_DP_MEDIUM_LOWER_BOUND

// Diz ao detalhe se deve mostrar botão de voltar.
// No tablet (dois painéis), o botão voltar no detalhe não faz sentido.
val LocalShowBackButton = compositionLocalOf { true }

// --------------------------------------------------------------------------
// Scene — responsável pelo LAYOUT (como os entries são exibidos)
// --------------------------------------------------------------------------
class ListDetailScene<T : Any>(
    override val key: Any,
    override val previousEntries: List<NavEntry<T>>,
    val listEntry: NavEntry<T>,
    val detailEntry: NavEntry<T>,
) : Scene<T> {

    override val entries: List<NavEntry<T>> = listOf(listEntry, detailEntry)

    override val content: @Composable () -> Unit = {
        Row(modifier = Modifier.fillMaxSize()) {

            Box(modifier = Modifier.weight(0.4f)) {
                listEntry.Content()
            }

            VerticalDivider(modifier = Modifier.fillMaxHeight())

            // AnimatedContent anima a troca de item sem recriar a Scene inteira
            AnimatedContent(
                targetState = detailEntry,
                contentKey = { entry -> entry.contentKey },
                transitionSpec = {
                    slideInHorizontally { it } togetherWith slideOutHorizontally { -it }
                },
                modifier = Modifier.weight(0.6f),
                label = "detail_pane"
            ) { entry ->
                entry.Content()
            }
        }
    }
}

// --------------------------------------------------------------------------
// SceneStrategy — decide SE e COMO criar a ListDetailScene.
// As keys ficam na Strategy: ela é quem define os critérios de seleção,
// então é ela que deve guardar os marcadores de metadata.
// --------------------------------------------------------------------------
class ListDetailSceneStrategy<T : Any>(
    private val windowSizeClass: WindowSizeClass
) : SceneStrategy<T> {

    // Keys aninhadas na Strategy — padrão dos docs oficiais
    object ListKey : NavMetadataKey<Boolean>
    object DetailKey : NavMetadataKey<Boolean>

    companion object {
        fun listPane() = metadata { put(ListKey, true) }
        fun detailPane() = metadata { put(DetailKey, true) }
    }

    override fun SceneStrategyScope<T>.calculateScene(
        entries: List<NavEntry<T>>
    ): Scene<T>? {

        // Janela estreita → retorna null, SinglePaneSceneStrategy assume
        if (!windowSizeClass.isWidthAtLeastBreakpoint(WIDTH_DP_MEDIUM_LOWER_BOUND)) {
            return null
        }

        val detailEntry = entries.lastOrNull()
            ?.takeIf { it.metadata.contains(DetailKey) }
            ?: return null

        val listEntry = entries.findLast { it.metadata.contains(ListKey) }
            ?: return null

        // Usa o contentKey da lista como key da Scene:
        // trocar o item selecionado anima só o painel direito, não a Scene inteira
        return ListDetailScene(
            key = listEntry.contentKey,
            previousEntries = entries.dropLast(1),
            listEntry = listEntry,
            detailEntry = detailEntry,
        )
    }
}

@Composable
fun <T : Any> rememberListDetailSceneStrategy(): ListDetailSceneStrategy<T> {
    val windowSizeClass = currentWindowAdaptiveInfo().windowSizeClass
    return remember(windowSizeClass) { ListDetailSceneStrategy(windowSizeClass) }
}
