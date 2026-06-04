package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.dp

@Composable
fun HomeNavIcon(selected: Boolean, modifier: Modifier = Modifier) {
    val gradientColors = if (selected) {
        listOf(Color(0xFF42A5F5), Color(0xFF1565C0))
    } else {
        listOf(
            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f),
            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.20f)
        )
    }
    val gradient = Brush.verticalGradient(colors = gradientColors)
    val surfaceColor = MaterialTheme.colorScheme.surface
    val roofPath = remember { Path() }
    
    Canvas(modifier = modifier.size(28.dp)) {
        val w = size.width
        val h = size.height
        
        // 1. Draw House Roof (Triangle)
        roofPath.run {
            reset()
            moveTo(w * 0.5f, h * 0.08f)
            lineTo(w * 0.08f, h * 0.44f)
            lineTo(w * 0.92f, h * 0.44f)
            close()
        }
        drawPath(path = roofPath, brush = gradient)
        
        // 2. Draw House Body (Rectangle)
        val bodyRect = Rect(
            left = w * 0.18f,
            top = h * 0.42f,
            right = w * 0.82f,
            bottom = h * 0.92f
        )
        drawRoundRect(
            brush = gradient,
            topLeft = Offset(bodyRect.left, bodyRect.top),
            size = Size(bodyRect.width, bodyRect.height),
            cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
        )
        
        // 3. Draw Arch Door Cutout (Reveals background surface color)
        // Draw the door cutout using the container's background surface color
        val doorWidth = bodyRect.width * 0.32f
        val doorHeight = bodyRect.height * 0.55f
        val doorLeft = w * 0.5f - doorWidth * 0.5f
        val doorTop = bodyRect.bottom - doorHeight
        
        drawRoundRect(
            color = surfaceColor,
            topLeft = Offset(doorLeft, doorTop),
            size = Size(doorWidth, doorHeight + 2f), // slight overlay to avoid stitching gaps at bottom
            cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
        )
    }
}

@Composable
fun SettingsNavIcon(selected: Boolean, modifier: Modifier = Modifier) {
    val gradientColors = if (selected) {
        listOf(Color(0xFF42A5F5), Color(0xFF1565C0))
    } else {
        listOf(
            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f),
            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.20f)
        )
    }
    val gradient = Brush.verticalGradient(colors = gradientColors)
    val surfaceColor = MaterialTheme.colorScheme.surface

    Canvas(modifier = modifier.size(28.dp)) {
        val w = size.width
        val h = size.height
        val cx = w / 2f
        val cy = h / 2f
        
        // Draw the outer gear teeth (4 rotated rounded rectangles)
        val toothWidth = w * 0.24f
        val toothHeight = h * 0.88f
        
        for (angle in listOf(0f, 45f, 90f, 135f)) {
            rotate(angle, pivot = Offset(cx, cy)) {
                drawRoundRect(
                    brush = gradient,
                    topLeft = Offset(cx - toothWidth / 2f, cy - toothHeight / 2f),
                    size = Size(toothWidth, toothHeight),
                    cornerRadius = CornerRadius(5.dp.toPx(), 5.dp.toPx())
                )
            }
        }
        
        // Draw the main gear body (inner circle)
        drawCircle(
            brush = gradient,
            radius = w * 0.32f,
            center = Offset(cx, cy)
        )
        
        // Draw the hub hole
        drawCircle(
            color = surfaceColor,
            radius = w * 0.14f,
            center = Offset(cx, cy)
        )
    }
}

@Composable
fun LibraryNavIcon(selected: Boolean, modifier: Modifier = Modifier) {
    val gradientColors = if (selected) {
        listOf(Color(0xFF4FC3F7), Color(0xFF0288D1))
    } else {
        listOf(
            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f),
            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.20f)
        )
    }
    val gradient = Brush.verticalGradient(colors = gradientColors)
    val gridColor = if (selected) Color.White.copy(alpha = 0.45f) else Color.White.copy(alpha = 0.20f)
    
    Canvas(modifier = modifier.size(28.dp)) {
        val w = size.width
        val h = size.height
        val cx = w / 2f
        val cy = h / 2f
        val r = w / 2f * 0.9f
        
        // Draw main sphere
        drawCircle(
            brush = gradient,
            radius = r,
            center = Offset(cx, cy)
        )
        
        // Draw vertical/horizontal grid lines (curved latitude and longitude)
        // Draw equator
        drawLine(
            color = gridColor,
            start = Offset(cx - r, cy),
            end = Offset(cx + r, cy),
            strokeWidth = 1.5.dp.toPx()
        )
        // Draw prime meridian
        drawLine(
            color = gridColor,
            start = Offset(cx, cy - r),
            end = Offset(cx, cy + r),
            strokeWidth = 1.5.dp.toPx()
        )
        
        // Draw longitude ellipses
        drawOval(
            color = gridColor,
            topLeft = Offset(cx - r * 0.5f, cy - r),
            size = Size(r, r * 2f),
            style = Stroke(width = 1.5.dp.toPx())
        )
        
        // Draw latitude curves
        drawLine(
            color = gridColor,
            start = Offset(cx - r * 0.8f, cy - r * 0.45f),
            end = Offset(cx + r * 0.8f, cy - r * 0.45f),
            strokeWidth = 1.2.dp.toPx()
        )
        drawLine(
            color = gridColor,
            start = Offset(cx - r * 0.8f, cy + r * 0.45f),
            end = Offset(cx + r * 0.8f, cy + r * 0.45f),
            strokeWidth = 1.2.dp.toPx()
        )
    }
}

@Composable
fun BookmarksNavIcon(selected: Boolean, modifier: Modifier = Modifier) {
    val mainColors = if (selected) {
        listOf(Color(0xFF2979FF), Color(0xFF1565C0))
    } else {
        listOf(
            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f),
            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.20f)
        )
    }
    
    val foldColors = if (selected) {
        listOf(Color(0xFF82B1FF), Color(0xFF2979FF))
    } else {
        listOf(
            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f),
            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.30f)
        )
    }
    
    val mainBrush = Brush.verticalGradient(colors = mainColors)
    val foldBrush = Brush.verticalGradient(colors = foldColors)
    val ribbonPath = remember { Path() }
    val foldPath = remember { Path() }
    
    Canvas(modifier = modifier.size(28.dp)) {
        val w = size.width
        val h = size.height
        
        // 1. Main Ribbon body with bottom V-cutout
        ribbonPath.run {
            reset()
            moveTo(w * 0.32f, h * 0.1f)
            lineTo(w * 0.82f, h * 0.1f)
            lineTo(w * 0.82f, h * 0.88f)
            lineTo(w * 0.57f, h * 0.72f) // Inverted V center peak
            lineTo(w * 0.32f, h * 0.88f)
            close()
        }
        drawPath(path = ribbonPath, brush = mainBrush)
        
        // 2. Beautiful 3D fold on the left
        foldPath.run {
            reset()
            moveTo(w * 0.32f, h * 0.1f)
            lineTo(w * 0.18f, h * 0.1f)
            quadraticTo(w * 0.15f, h * 0.35f, w * 0.18f, h * 0.6f)
            lineTo(w * 0.32f, h * 0.6f)
            lineTo(w * 0.32f, h * 0.1f)
            close()
        }
        drawPath(path = foldPath, brush = foldBrush)
    }
}

@Composable
fun BibleNavIcon(selected: Boolean, modifier: Modifier = Modifier) {
    val activeGradientColors = listOf(Color(0xFF00E5FF), Color(0xFF00BFA5))
    val bookBrush = Brush.verticalGradient(
        if (selected) activeGradientColors else listOf(
            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f),
            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.20f)
        )
    )
    
    val crossColor = if (selected) Color.White else MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)
    val pageColor = if (selected) Color(0xFFB2DFDB) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f)
    val pagesPath = remember { Path() }

    Canvas(modifier = modifier.size(28.dp)) {
        val w = size.width
        val h = size.height
        
        // 1. Draw Bible Leather Cover (Main rounded rectangle)
        val coverRect = Rect(
            left = w * 0.15f,
            top = h * 0.08f,
            right = w * 0.85f,
            bottom = h * 0.85f
        )
        drawRoundRect(
            brush = bookBrush,
            topLeft = Offset(coverRect.left, coverRect.top),
            size = Size(coverRect.width, coverRect.height),
            cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
        )
        
        // 2. Draw Book Pages Spine/Bottom effect (white slice at the bottom of the book)
        pagesPath.run {
            reset()
            moveTo(w * 0.20f, h * 0.85f)
            lineTo(w * 0.81f, h * 0.85f)
            quadraticTo(w * 0.81f, h * 0.93f, w * 0.76f, h * 0.93f)
            lineTo(w * 0.27f, h * 0.93f)
            quadraticTo(w * 0.20f, h * 0.93f, w * 0.20f, h * 0.85f)
            close()
        }
        drawPath(path = pagesPath, color = pageColor)
        
        // 3. Central White Cross
        val cx = w * 0.5f
        val cy = h * 0.44f
        val beamThick = w * 0.09f
        val crossLengthV = h * 0.32f
        val crossLengthH = w * 0.24f
        
        // Draw vertical beam
        drawRoundRect(
            color = crossColor,
            topLeft = Offset(cx - beamThick / 2f, cy - crossLengthV / 2f),
            size = Size(beamThick, crossLengthV),
            cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
        )
        
        // Draw horizontal beam
        val hBeamY = cy - crossLengthV * 0.12f
        drawRoundRect(
            color = crossColor,
            topLeft = Offset(cx - crossLengthH / 2f, hBeamY - beamThick / 2f),
            size = Size(crossLengthH, beamThick),
            cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
        )
    }
}
