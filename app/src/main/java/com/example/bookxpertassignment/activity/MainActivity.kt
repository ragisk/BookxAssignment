package com.example.bookxpertassignment.activity

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bookxpertassignment.AccountAdapter
import com.example.bookxpertassignment.AccountUtils
import com.example.bookxpertassignment.viewmodel.AccountViewModel
import com.example.bookxpertassignment.R
import com.example.bookxpertassignment.RetrofitClient
import com.example.bookxpertassignment.model.Account
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL

class MainActivity : ComponentActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var accountViewModel: AccountViewModel
    private lateinit var progressBar: ProgressBar
    private lateinit var pdfIV: ImageView
    private lateinit var galleryIV: ImageView
    private lateinit var cameraIV: ImageView
    private lateinit var noAccountAvailableTV: TextView
    private val REQUEST_CAMERA_PERMISSION = 100
    private val REQUEST_GALLERY_PERMISSION = 101
    private val REQUEST_IMAGE_CAPTURE = 1
    private val REQUEST_PICK_IMAGE = 2
    var isAccountsAdded=false
    private var photoUri: Uri? = null
    private var photoFile: File? = null

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initViews()
        recyclerView.layoutManager = LinearLayoutManager(this)

        accountViewModel.isLoading.observe(this) { loading ->
            progressBar.visibility = if (loading) View.VISIBLE else View.GONE
        }
        accountViewModel.accounts.observe(this) { accountList ->
            if (accountList.size==0){
                noAccountAvailableTV.visibility=View.VISIBLE
            }
            recyclerView.adapter = object :AccountAdapter(accountList, this){
                override fun delete(account: Account) {
                    accountViewModel.delete(account)
                }

                override fun update(account: Account) {
                    accountViewModel.update(account)
                }
            }
            isAccountsAdded=true
        }

        accountViewModel.getAllAccountsFromDB()

    }

    override fun onResume() {
        super.onResume()
        if (AccountUtils.isInternetAvailable(this) && !isAccountsAdded) {
            accountViewModel.getAllAccountsFromDB()
        }
    }

    fun initViews(){
        val factory = AccountViewModel.AccountViewModelFactory(this)
        accountViewModel = ViewModelProvider(this, factory)[AccountViewModel::class.java]
        recyclerView = findViewById(R.id.accountsRecyclerView)
        progressBar = findViewById(R.id.progressBar)
        pdfIV = findViewById(R.id.pdfIV)
        galleryIV = findViewById(R.id.galleryIV)
        cameraIV = findViewById(R.id.cameraIV)
        noAccountAvailableTV = findViewById(R.id.noAccountAvailableTV)
        pdfIV.setOnClickListener{
            openPdf()
        }
        galleryIV.setOnClickListener{
            if (checkGalleryPermission()) {
                openGallery()
            } else {
                requestGalleryPermission()
            }
        }
        cameraIV.setOnClickListener{
            if (checkCameraPermission()) {
                openCamera()
            } else {
                requestCameraPermission()
            }
        }
    }


    fun openPdf(){
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val pdfFile = File(cacheDir, "downloaded.pdf")
                if (!pdfFile.exists()) {
                    if (AccountUtils.isInternetAvailable(this@MainActivity)) {
                        downloadPdfFile(
                            AccountUtils.PDF_URL, // Or pass applicationContext
                        )
                    }else{
                        withContext(Dispatchers.Main) {
                            AccountUtils.showNoInternetDialog(this@MainActivity)
                        }
                        return@launch
                    }
                }
                withContext(Dispatchers.Main) {
                    val pdfUri = FileProvider.getUriForFile(
                        this@MainActivity,
                        "${applicationContext.packageName}.provider",
                        pdfFile
                    )

                    val intent = Intent(Intent.ACTION_VIEW)
                    intent.setDataAndType(pdfUri, "application/pdf")
                    intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION

                    startActivity(intent)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@MainActivity, "Download Failed: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }

    }


    suspend fun downloadPdfFile(urlStr: String): File {
        return withContext(Dispatchers.IO) {
            try {
                val url = URL(urlStr)
                val connection: HttpURLConnection = url.openConnection() as HttpURLConnection
                connection.connect()

                if (connection.responseCode != HttpURLConnection.HTTP_OK) {
                    throw Exception("Server returned HTTP ${connection.responseCode} ${connection.responseMessage}")
                }

                val inputStream: InputStream = connection.inputStream
                val file = File(this@MainActivity.cacheDir, "downloaded.pdf")

                val outputStream = FileOutputStream(file)

                val data = ByteArray(4096)
                var count: Int

                while (inputStream.read(data).also { count = it } != -1) {
                    outputStream.write(data, 0, count)
                }

                outputStream.flush()
                outputStream.close()
                inputStream.close()

                file
            } catch (e: Exception) {
                e.printStackTrace()
                throw e
            }
        }
    }

    private fun checkCameraPermission(): Boolean {
        val cameraPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
        return cameraPermission == PackageManager.PERMISSION_GRANTED
    }

    private fun checkGalleryPermission(): Boolean {
        val storagePermission = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
        return storagePermission == PackageManager.PERMISSION_GRANTED
    }

    private fun requestCameraPermission() {
        ActivityCompat.requestPermissions(this,
            arrayOf(Manifest.permission.CAMERA), REQUEST_CAMERA_PERMISSION)
    }

    private fun requestGalleryPermission() {
        val storagePermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_IMAGES
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }

        val permissionStatus = ContextCompat.checkSelfPermission(this, storagePermission)
        if (permissionStatus == PackageManager.PERMISSION_GRANTED) {
            openGallery()
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(storagePermission),
                REQUEST_GALLERY_PERMISSION
            )
        }


    }

    private fun openCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)

        photoUri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            createImageUri()
        } else {
            val file = createImageFile()
            FileProvider.getUriForFile(this, "$packageName.provider", file)
        }

        intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
        startActivityForResult(intent, REQUEST_IMAGE_CAPTURE)
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        intent.type = "image/*"
        startActivityForResult(intent, REQUEST_PICK_IMAGE)
    }

    private fun createImageUri(): Uri? {
        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "IMG_${System.currentTimeMillis()}.jpg")
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            put(MediaStore.Images.Media.RELATIVE_PATH, "DCIM/Camera")
        }

        return contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
    }

    private fun createImageFile(): File {
        val storageDir = getExternalFilesDir(null)
        val file = File.createTempFile("IMG_", ".jpg", storageDir)
        photoFile = file
        return file
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera()
            } else {
                Toast.makeText(this, "Camera permission is required!", Toast.LENGTH_SHORT).show()
            }
        }

        if (requestCode == REQUEST_GALLERY_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openGallery()
            } else {
                Toast.makeText(this, "Storage permission is required!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                REQUEST_IMAGE_CAPTURE -> {
                    photoUri?.let {
                        showImageDialog(it)
                    } ?: run {
                        Toast.makeText(this, "Image not found!", Toast.LENGTH_SHORT).show()
                    }
                }

                REQUEST_PICK_IMAGE -> {
                    val imageUri = data?.data
                    showImageDialog(imageUri!!)
                }
            }
        }
    }

    private fun showImageDialog(imageUri: Uri) {
        val dialog = Dialog(this) // If inside an Activity. If inside Fragment use requireContext()
        dialog.setContentView(R.layout.image_dialog)

        val imageView = dialog.findViewById<ImageView>(R.id.dialogImageView)
        val cancelIcon = dialog.findViewById<ImageView>(R.id.cancelIcon)

        imageView.setImageURI(imageUri)

        cancelIcon.setOnClickListener {
            dialog.dismiss()
        }

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.show()
    }
}

