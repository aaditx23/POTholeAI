package com.yeaminthesis.potholeai.components

import android.Manifest
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.util.Base64
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.canhub.cropper.CropImageContract
import com.canhub.cropper.CropImageContractOptions
import com.canhub.cropper.CropImageOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.InputStream

@Composable
fun ImagePicker(onImagePicked: (image: Bitmap) -> Unit){
    var selectedImageBitmap by remember {
        mutableStateOf<Bitmap?>(null)
    }

    var showGallery by remember {
        mutableStateOf(false)
    }
    val context = LocalContext.current
    var hasImagePermission by remember{ mutableStateOf(false) }
    val permission =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) Manifest.permission.READ_MEDIA_IMAGES
        else Manifest.permission.READ_EXTERNAL_STORAGE
    hasImagePermission = permissionLauncher(
        context = context,
        permission = permission
    )

    val cropLauncher = rememberLauncherForActivityResult(contract = CropImageContract()) { result ->
        if (result.isSuccessful){
            val selectedImageUri = result.uriContent
            val bitmap = createBitmapFromUri(context, selectedImageUri!!)
            selectedImageBitmap = bitmap
            onImagePicked(bitmap)
        } else {
            Toast.makeText(context, "An Error occurred while cropping", Toast.LENGTH_SHORT).show()
            println("${result.error}")
        }
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        val cropOptions = CropImageContractOptions(uri, CropImageOptions().apply {
            fixAspectRatio = true
            aspectRatioX = 1
            aspectRatioY = 1
            outputRequestWidth = 150
            outputRequestHeight = 150
            outputCompressQuality = 100
            outputCompressFormat = Bitmap.CompressFormat.JPEG
        })
        cropLauncher.launch(cropOptions)
    }

    if (hasImagePermission){
        if(!showGallery){
            showGallery = true
            println("LAUNCHING")
            launcher.launch(("image/*"))
        }
    }
}

private fun createBitmapFromUri(context: Context, uri: Uri): Bitmap {
    val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
    return BitmapFactory.decodeStream(inputStream)
}

//fun bitmapToString(bitmap: Bitmap): String {
//    val outputStream = ByteArrayOutputStream()
//    bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
//    val byteArray = outputStream.toByteArray()
//    return Base64.encodeToString(byteArray, Base64.DEFAULT)
//}

suspend fun bitmapToString(bitmap: Bitmap, maxSizeKB: Int = 100): String = withContext(Dispatchers.IO) {
    var compressedBitmap = bitmap
    var quality = 100
    var outputStream = ByteArrayOutputStream()

    // Compress and check size
    do {
        outputStream.reset() // Reset the stream to clear previous data
        compressedBitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
        val byteArray = outputStream.toByteArray()
        val sizeKB = byteArray.size / 1024
        println("Size of image $sizeKB")
        quality -= 10 // Reduce quality for further compression if needed

    } while (sizeKB > maxSizeKB && quality > 0)

    Base64.encodeToString(outputStream.toByteArray(), Base64.DEFAULT)
}

fun stringToBitmap(encodedString: String): Bitmap? {
    return try {
        // Decode the Base64 string to a byte array
        val decodedBytes = Base64.decode(encodedString, Base64.DEFAULT)
        // Convert the byte array to a Bitmap
        val byteArray = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
        byteArray
    } catch (e: IllegalArgumentException) {
        e.printStackTrace()
        null
    }
}
fun drawableToBitmap(drawable: Drawable): Bitmap {
    if (drawable is BitmapDrawable) {
        return drawable.bitmap
    }

    val bitmap = Bitmap.createBitmap(
        drawable.intrinsicWidth,
        drawable.intrinsicHeight,
        Bitmap.Config.ARGB_8888
    )

    val canvas = Canvas(bitmap)
    drawable.setBounds(0, 0, canvas.width, canvas.height)
    drawable.draw(canvas)

    return bitmap
}

