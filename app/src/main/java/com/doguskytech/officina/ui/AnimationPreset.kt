package com.doguskytech.officina.ui

import androidx.compose.animation.ContentTransform
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith

data class AnimationPreset(
    val enter: ContentTransform,
    val pop: ContentTransform,
)

object Presets {

    val NONE = AnimationPreset(
        enter = EnterTransition.None togetherWith ExitTransition.None,
        pop   = EnterTransition.None togetherWith ExitTransition.None,
    )

    val FADE = AnimationPreset(
        enter = fadeIn(tween(300)) togetherWith fadeOut(tween(300)),
        pop   = fadeIn(tween(300)) togetherWith fadeOut(tween(300)),
    )

    val SLIDE_HORIZONTAL = AnimationPreset(
        enter = slideInHorizontally(tween(300)) { it } togetherWith slideOutHorizontally(tween(300)) { -it },
        pop   = slideInHorizontally(tween(300)) { -it } togetherWith slideOutHorizontally(tween(300)) { it },
    )

    val SLIDE_VERTICAL = AnimationPreset(
        enter = slideInVertically(tween(300)) { it } togetherWith fadeOut(tween(150)),
        pop   = fadeIn(tween(150)) togetherWith slideOutVertically(tween(300)) { it },
    )

    val SCALE_FADE = AnimationPreset(
        enter = (scaleIn(tween(300), initialScale = 0.92f) + fadeIn(tween(300))) togetherWith
                (scaleOut(tween(150), targetScale = 1.08f) + fadeOut(tween(150))),
        pop   = (scaleIn(tween(300), initialScale = 1.08f) + fadeIn(tween(300))) togetherWith
                (scaleOut(tween(150), targetScale = 0.92f) + fadeOut(tween(150))),
    )
}
