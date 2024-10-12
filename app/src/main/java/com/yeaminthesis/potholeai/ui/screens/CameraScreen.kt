package com.yeaminthesis.potholeai.ui.screens

import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView

@Composable
fun CameraScreen(
    controller: LifecycleCameraController
){
    val lifeCycleOwner = LocalLifecycleOwner.current

    AndroidView(
        factory = {
            PreviewView(it).apply {
                this.controller = controller
                controller.bindToLifecycle(lifeCycleOwner)
            }
        },
        modifier = Modifier
            .fillMaxSize()
    )

}
