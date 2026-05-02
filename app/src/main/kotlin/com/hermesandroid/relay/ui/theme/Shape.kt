package com.hermesandroid.relay.ui.theme

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

val HermesShapes = Shapes(
    small = RoundedCornerShape(8.dp),      // chips, badges
    medium = RoundedCornerShape(12.dp),    // cards, bubbles, banners
    large = RoundedCornerShape(16.dp),     // sheets, dialogs
)

// Extra sizes for one-off uses
val ShapeSmall = RoundedCornerShape(4.dp)
val ShapeMedium = RoundedCornerShape(12.dp)
val ShapeLarge = RoundedCornerShape(16.dp)
val ShapeXLarge = RoundedCornerShape(20.dp)
val ShapeCircle = CircleShape
