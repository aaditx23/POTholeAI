package com.yeaminthesis.potholeai.ui.screens

import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.unit.dp
import com.yeaminthesis.potholeai.domain.BoundingBox

//
//@Composable
//fun DetectionOverlay(
//    classifications: List<BoundingBox>,  // List of detected classifications with bounding boxes
//    modifier: Modifier = Modifier,
//    imageWidth: Int,  // Width of the original image
//    imageHeight: Int   // Height of the original image
//) {
//    Text(text = "OVERLAY", color = Color.Red)
//
//    Canvas(modifier = modifier.fillMaxSize()) {
//        // Get the size of the canvas
//        val canvasWidth = size.width
//        val canvasHeight = size.height
//
//        classifications.forEach { bbox ->
//            val boundingBox = bbox
//
//            // Convert normalized coordinates to pixels
//            val topLeft = Offset(
//                boundingBox.x1 * canvasWidth,
//                (1 - boundingBox.y1) * canvasHeight // Invert y-axis for correct placement
//            )
//            val size = Size(boundingBox.w * canvasWidth, boundingBox.h * canvasHeight)
//
//            // Debugging output
//            println("Bounding Box - x1: ${boundingBox.x1}, y1: ${boundingBox.y1}, w: ${boundingBox.w}, h: ${boundingBox.h}")
//
//            // Draw the bounding box
//            drawRect(
//                color = Color.Red,
//                topLeft = topLeft,
//                size = size,
//                style = Stroke(width = 4.dp.toPx())  // Make it a stroked rectangle
//            )
//
//            // Prepare the label text
//            val labelText = "${bbox.clsName} (${bbox.cnf})"
//            val textPaint = Paint().apply {
//                color = android.graphics.Color.WHITE
//                textSize = 40f
//            }
//            val textBounds = android.graphics.Rect()
//            textPaint.getTextBounds(labelText, 0, labelText.length, textBounds)
//
//            // Draw the label background (optional)
//            drawRect(
//                color = Color.Black,
//                topLeft = Offset(topLeft.x, topLeft.y - textBounds.height() - 8),  // Position above bounding box
//                size = Size(textBounds.width().toFloat() + 16, textBounds.height().toFloat() + 8)  // Add padding
//            )
//
//            // Draw the label text
//            drawContext.canvas.nativeCanvas.drawText(
//                labelText,
//                topLeft.x + 8,  // Add padding to x-coordinate
//                topLeft.y - 8,  // Position label above bounding box
//                textPaint
//            )
//        }
//    }
//}

@Composable
fun DetectionOverlay(
    boundingBoxes: List<BoundingBox>,  // List of detected bounding boxes
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.fillMaxSize()) {
        val canvasWidth = size.width
        val canvasHeight = size.height

        println("Canvas size - width: $canvasWidth, height: $canvasHeight")

        boundingBoxes.forEach { box ->
            // Convert normalized coordinates to pixels
            val left = box.x1 * canvasWidth
            val top = box.y1 * canvasHeight
            val right = box.x2 * canvasWidth
            val bottom = box.y2 * canvasHeight

            // Debugging output
            println("Bounding Box - left: $left, top: $top, right: $right, bottom: $bottom")

            // Ensure bounding boxes are within canvas bounds
            if (left < 0 || top < 0 || right > canvasWidth || bottom > canvasHeight) {
                println("Bounding Box out of bounds")
                return@forEach
            }

            // Draw the bounding box
            drawRect(
                color = Color.Red,
                topLeft = Offset(left, top),
                size = Size(right - left, bottom - top),
                style = Stroke(width = 4.dp.toPx())
            )

            // Prepare the label text
            val labelText = "${box.clsName} (${String.format("%.2f", box.cnf)})"
            val textPaint = Paint().apply {
                color = android.graphics.Color.WHITE
                textSize = 40f
                isAntiAlias = true
                typeface = Typeface.DEFAULT_BOLD
            }

            // Calculate text bounds
            val textBounds = android.graphics.Rect()
            textPaint.getTextBounds(labelText, 0, labelText.length, textBounds)
            val textWidth = textBounds.width()
            val textHeight = textBounds.height()

            // Draw text background for better visibility
            drawRect(
                color = Color.Black.copy(alpha = 0.7f),
                topLeft = Offset(left, top - textHeight - 16),
                size = Size(textWidth.toFloat() + 16, textHeight.toFloat() + 16)
            )

            // Draw the label text
            drawContext.canvas.nativeCanvas.drawText(
                labelText,
                left + 8,
                top - 8,
                textPaint
            )
        }
    }
}