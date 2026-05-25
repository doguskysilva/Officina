package com.doguskytech.officina.scenes

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.scene.OverlayScene
import androidx.navigation3.scene.Scene
import androidx.navigation3.scene.SceneStrategy
import androidx.navigation3.scene.SceneStrategyScope

private const val BOTTOM_SHEET_KEY = "com.doguskytech.officina.BottomSheet"

// --------------------------------------------------------------------------
// SceneStrategy — identifica entries marcados com bottomSheet() e os envolve
// num ModalBottomSheet. É OverlayScene, logo o decorator não é aplicado.
// --------------------------------------------------------------------------
class BottomSheetSceneStrategy<T : Any> : SceneStrategy<T> {

    companion object {
        fun bottomSheet(): Map<String, Any> = mapOf(BOTTOM_SHEET_KEY to Unit)
    }

    override fun SceneStrategyScope<T>.calculateScene(
        entries: List<NavEntry<T>>,
    ): Scene<T>? {
        val topEntry = entries.lastOrNull() ?: return null
        if (!topEntry.metadata.containsKey(BOTTOM_SHEET_KEY)) return null
        val underlaidEntries = entries.dropLast(1)
        return BottomSheetScene(
            sheetEntry = topEntry,
            previousEntries = underlaidEntries,
            overlaidEntries = underlaidEntries,
            // onBack vem do SceneStrategyScope — é o mesmo callback passado ao NavDisplay.
            // Cobre: swipe down, toque fora e botão voltar do sistema.
            onDismiss = onBack,
        )
    }
}

// --------------------------------------------------------------------------
// OverlayScene — renderiza sobre a scene anterior sem que o decorator seja
// aplicado (sem NavigationBar/Rail dentro do sheet).
// overlaidEntries = entries visíveis sob o sheet (NavDisplay os renderiza antes).
// --------------------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
class BottomSheetScene<T : Any>(
    private val sheetEntry: NavEntry<T>,
    override val previousEntries: List<NavEntry<T>>,
    override val overlaidEntries: List<NavEntry<T>>,
    private val onDismiss: () -> Unit,
) : OverlayScene<T> {

    // contentKey é a chave pública do NavEntry — usada por NavDisplay para animar.
    override val key: Any = sheetEntry.contentKey
    override val entries: List<NavEntry<T>> = listOf(sheetEntry)

    override val content: @Composable () -> Unit = {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

        ModalBottomSheet(
            onDismissRequest = onDismiss,
            sheetState = sheetState,
        ) {
            // Content() é o método público de NavEntry que renderiza o composable do entry.
            sheetEntry.Content()
        }
    }
}

// --------------------------------------------------------------------------
// Factory composable — sem parâmetros, pois onBack vem do SceneStrategyScope.
// --------------------------------------------------------------------------
@Composable
fun <T : Any> rememberBottomSheetSceneStrategy(): BottomSheetSceneStrategy<T> {
    return remember { BottomSheetSceneStrategy() }
}
