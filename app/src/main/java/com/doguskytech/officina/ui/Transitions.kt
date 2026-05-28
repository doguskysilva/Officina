package com.doguskytech.officina.ui

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut

val itemEnterTransition: EnterTransition = fadeIn() + scaleIn(initialScale = 0.85f)
val itemExitTransition: ExitTransition  = fadeOut() + scaleOut(targetScale = 0.85f)
