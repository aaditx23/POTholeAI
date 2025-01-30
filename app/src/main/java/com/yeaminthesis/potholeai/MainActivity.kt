package com.yeaminthesis.potholeai

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.camera.view.CameraController
import androidx.camera.view.LifecycleCameraController
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.yeaminthesis.potholeai.domain.BoundingBox
import com.yeaminthesis.potholeai.domain.Detector
import com.yeaminthesis.potholeai.ui.screens.CameraScreen
import com.yeaminthesis.potholeai.ui.screens.DetectionOverlay
import com.yeaminthesis.potholeai.ui.theme.POTholeAITheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity(), Detector.DetectorListener {

    private var boxList by mutableStateOf(emptyList<BoundingBox>())
    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        if (!hasCameraPermission()){
            ActivityCompat.requestPermissions(
                this, arrayOf(
                    Manifest.permission.CAMERA
                ), 0
            )
        }
        setContent {
            POTholeAITheme {


                val detector = Detector(
                    context = applicationContext,
                    modelPath = "best (3)_float32 (2).tflite",
                    labelPath = "labels.txt",
                    detectorListener = this
                )
                detector.setup()


                val controller = remember{
                    LifecycleCameraController(applicationContext).apply {
                        setEnabledUseCases(CameraController.IMAGE_ANALYSIS)
                        setImageAnalysisAnalyzer(
                            ContextCompat.getMainExecutor(applicationContext),
                            { imageProxy ->
                                // Convert imageProxy to bitmap if needed and run detection
                                val rotationDegrees = imageProxy.imageInfo.rotationDegrees
                                val bitmap = imageProxy.toBitmap()

                                // Optionally rotate the bitmap based on rotationDegrees
                                val rotatedBitmap = rotateBitmap(bitmap, rotationDegrees.toFloat())

                                detector.detect(rotatedBitmap)
                                imageProxy.close()
                            }
                        )
                    }
                }
                Scaffold(modifier = Modifier.fillMaxSize()) {  innerPadding ->
                    Box(modifier = Modifier.fillMaxSize()) {
                        var toggle by rememberSaveable { mutableStateOf(false) }
                        var text by rememberSaveable { mutableStateOf("Start") }
                          // Show the camera preview
                        CameraScreen(controller)
                        Button(
                            onClick = {
                                toggle = !toggle
                                if(toggle) text = "Stop"
                                else text = "Start"
                            },
                            modifier = Modifier
                                .padding(top = 50.dp, start = 20.dp)
                        ) {
                            Text(text)
                        }
                        if(toggle) {

                            DetectionOverlay(boxList,)
                        }  // Show bounding boxes and labels on top
                    }
                }
            }
        }
    }

    fun rotateBitmap(bitmap: Bitmap, rotationDegrees: Float): Bitmap {
        val matrix = android.graphics.Matrix().apply { postRotate(rotationDegrees) }
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

    private fun hasCameraPermission() = ContextCompat.checkSelfPermission(
        this, Manifest.permission.CAMERA
    ) == PackageManager.PERMISSION_GRANTED

    override fun onEmptyDetect() {
        boxList = emptyList()
    }

    override fun onDetect(boundingBoxes: List<BoundingBox>, inferenceTime: Long) {
        CoroutineScope(Dispatchers.IO).launch {
            boxList = boundingBoxes
        }
    }
}

