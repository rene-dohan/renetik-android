package renetik.android.getpicture

import android.Manifest.permission.CAMERA
import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.content.Intent
import android.content.Intent.createChooser
import android.net.Uri
import android.os.Environment.getExternalStorageDirectory
import android.provider.MediaStore.ACTION_IMAGE_CAPTURE
import android.provider.MediaStore.EXTRA_OUTPUT
import android.view.View
import androidx.core.content.FileProvider.getUriForFile
import renetik.android.base.CSApplicationObject.application
import renetik.android.controller.base.CSViewController
import renetik.android.controller.extensions.dialog
import renetik.android.controller.extensions.requestPermissions
import renetik.android.controller.extensions.snackBarWarn
import renetik.android.controller.extensions.startActivityForResult
import renetik.android.imaging.extensions.resizeImage
import renetik.android.java.common.catchAllError
import renetik.android.java.extensions.collections.list
import renetik.android.java.extensions.createDatedFile
import renetik.android.java.extensions.later
import renetik.android.task.CSBackgroundHandlerObject.background
import java.io.File

class CSGetPictureController<T : View>(
    parent: CSViewController<T>, val title: String, private val folder: File,
    private val onImageReady: (File) -> Unit) : CSViewController<T>(parent) {

    constructor(parent: CSViewController<T>,
                title: String,
                imagesDirName: String,
                onImageReady: (File) -> Unit) :
            this(parent,
                title,
                File(File(application.externalFilesDir, "Pictures"), imagesDirName),
                onImageReady)

    //TODO: Migrate to Context#getExternalFilesDir(String)
    private val cacheImagesDir = File(getExternalStorageDirectory(),
        "Android/data/renetik.android.getpicture/files/ImageCache")
    var selectPhoto = true
    var takePhoto = true

    init {
        folder.mkdirs()
    }

    fun show() {
        requestPermissions(list(CAMERA, WRITE_EXTERNAL_STORAGE), { showAfterPermissionsGranted() },
            onNotGranted = { snackBarWarn("Some permissions not granted for taking photos") })
    }

    private fun showAfterPermissionsGranted() {
        if (!selectPhoto) onTakePhoto()
        else if (!takePhoto) onSelectPhoto()
        else dialog(title).showChoice("Album", { onSelectPhoto() }, "Camera", { onTakePhoto() })
    }

    private fun onImageSelected(input: Uri, onDone: (() -> Unit)? = null) = catchAllError {
        background {
            catchAllError {
                val outputImage = folder.createDatedFile("jpg")
                outputImage.outputStream().use { output -> input.resizeImage(1024, 768, output) }
                later { onImageReady(outputImage) }
            }
            later { onDone?.invoke() }
        }
    }

    private fun onSelectPhoto() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        startActivityForResult(createChooser(intent, "Select Picture")) { result ->
            catchAllError { onImageSelected(result!!.data!!) }
        }
    }

    private fun onTakePhoto() {
        val intent = Intent(ACTION_IMAGE_CAPTURE)
        val cacheImage = cacheImagesDir.createDatedFile("jpg")
        val authority = "${applicationContext.packageName}.renetik.android.getpicture.fileprovider"
        val uri = getUriForFile(this, authority, cacheImage)
        startActivityForResult(intent.putExtra(EXTRA_OUTPUT, uri)) {
            onImageSelected(uri) { cacheImage.delete() }
        }
    }
}