package com.yeaminthesis.potholeai.domain

import android.graphics.RectF

data class Classification(
    val name: String,
    val score: Float,
    val boundingBox: BoundingBox
)
