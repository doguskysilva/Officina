package com.doguskytech.officina.ui.preview

import android.content.res.Configuration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.Wallpapers

// ── Dispositivos ──────────────────────────────────────────────────────────────

@Preview(name = "Phone", device = "spec:width=411dp,height=891dp")
@Preview(name = "Phone Landscape", device = "spec:width=891dp,height=411dp,orientation=landscape")
@Preview(name = "Tablet", device = "spec:width=1280dp,height=800dp,dpi=240")
@Preview(name = "Foldable (unfolded)", device = "spec:width=673dp,height=841dp")
annotation class DevicePreviews

// ── Tema ──────────────────────────────────────────────────────────────────────

@Preview(name = "Light")
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
annotation class LightDarkPreviews

@Preview(name = "Dynamic — Red", wallpaper = Wallpapers.RED_DOMINATED_EXAMPLE)
@Preview(name = "Dynamic — Blue", wallpaper = Wallpapers.BLUE_DOMINATED_EXAMPLE)
@Preview(name = "Dynamic — Green", wallpaper = Wallpapers.GREEN_DOMINATED_EXAMPLE)
@Preview(name = "Dynamic — Yellow", wallpaper = Wallpapers.YELLOW_DOMINATED_EXAMPLE)
annotation class DynamicColorPreviews

// ── Acessibilidade ────────────────────────────────────────────────────────────

@Preview(name = "Font 85%", fontScale = 0.85f)
@Preview(name = "Font 100%", fontScale = 1.0f)
@Preview(name = "Font 150%", fontScale = 1.5f)
@Preview(name = "Font 200%", fontScale = 2.0f)
annotation class FontScalePreviews

// ── Combinadas ────────────────────────────────────────────────────────────────

/** Phone + tablet, light + dark. Cobre os casos mais comuns de uma vez. */
@Preview(name = "Phone · Light", device = "spec:width=411dp,height=891dp")
@Preview(name = "Phone · Dark", device = "spec:width=411dp,height=891dp", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Preview(name = "Tablet · Light", device = "spec:width=1280dp,height=800dp,dpi=240")
@Preview(name = "Tablet · Dark", device = "spec:width=1280dp,height=800dp,dpi=240", uiMode = Configuration.UI_MODE_NIGHT_YES)
annotation class OfficinaPreviews
