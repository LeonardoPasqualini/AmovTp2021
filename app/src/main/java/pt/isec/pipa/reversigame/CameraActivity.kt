package pt.isec.pipa.reversigame

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_camera.*
import pt.isec.pipa.reversigame.databinding.ActivityCameraBinding
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class CameraActivity : AppCompatActivity() {
    private var imageCapture: ImageCapture? = null

    private lateinit var outputDirectory: File
    private lateinit var cameraExecutor: ExecutorService
    lateinit var b : ActivityCameraBinding
    var imagePath: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        b = ActivityCameraBinding.inflate(layoutInflater)
        setContentView(b.root)

        // Pede permissão da camera
        if (allPermissionsGranted()) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(
                this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)
        }
        imagePath = getIntent().getStringExtra("imagePath")
        b.cameraCaptureButton.setOnClickListener { takePhoto() }
        //passa a diretoria que a imagem vai ser guardada
        outputDirectory = getOutputDirectory()

        cameraExecutor = Executors.newSingleThreadExecutor()
    }

    private fun takePhoto() {
        // pega uma refeencia ao obj ImageCapture responsável por capturar fotos
        val imageCapture = imageCapture ?: return

        // cira uma file que guarda a imagem
        // e coloca um timestamp para a imagem sempre ser única
        // escolhe qual o formato que quer guardar
        val photoFile = File(outputDirectory, imagePath)

        // Obj para especificar detalhes sobre a foto que foi capturada
        // - neste caso é em que ficheiro a imagem vai ser guardada
        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()
        val resultIntent = Intent()
        // Coloca o image capture listener para quando a foto tiver sido capturada
        imageCapture.takePicture(
            outputOptions, ContextCompat.getMainExecutor(this), object : ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) {
                    //caso ocorra algum erro
                    Log.e(TAG, "Photo capture failed: ${exc.message}", exc)
                    setResult(Activity.RESULT_CANCELED, resultIntent)
                }
                // Se não falhou então guarda a foto e apresenta um toast dizendo
                // que teve sucesso
                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    val savedUri = Uri.fromFile(photoFile)
                    val msg = "Photo capture succeeded: $savedUri"
                    Toast.makeText(baseContext, msg, Toast.LENGTH_SHORT).show()
                    Log.d(TAG, msg)
                    // retorna para a actividade que chamou a camera, a photoFile
                }
            })
        resultIntent.putExtra("path", Uri.fromFile(photoFile).toString())
        setResult(Activity.RESULT_OK, resultIntent)
        finish()
    }

    private fun startCamera() {
        // faz bind do lifecycle da camera para o lifecycle da nossa app
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        // retorna um Executor que roda na thread "principal"
        cameraProviderFuture.addListener(Runnable {
            // faz bind do lifecycle da camera para o lifecycle do processo da nossa app
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            // Mostra na escrã o que o sensor da camera pega
            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(viewFinder.surfaceProvider)
            }

            imageCapture = ImageCapture.Builder().build()

            // Seleciona a câmera traseira como default
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
            // limpa o que estiver em bind com a camera e liga com o cameraProvider
            try {
                // Unbind o que tiver na camera antes de fazer rebinding
                cameraProvider.unbindAll()

                // Bind cameraProvider na camera
                cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageCapture)

            } catch(exc: Exception) {
                Log.e(TAG, "Falha ao fazer o binding com o provider", exc)
            }

        }, ContextCompat.getMainExecutor(this))
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    private fun getOutputDirectory(): File {
        val mediaDir = externalMediaDirs.firstOrNull()?.let {
            File(it, resources.getString(R.string.app_name)).apply { mkdirs() } }
        return if (mediaDir != null && mediaDir.exists())
            mediaDir else filesDir
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults:
        IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                startCamera()
            } else {
                Toast.makeText(this,
                    "Permissoes nao disponibilizadas pelo utilizador.",
                    Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    companion object {
        private const val TAG = "PergilActivity"
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
    }
}