package com.example.dualscreen.activity

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.hardware.display.DisplayManager
import android.os.Bundle
import android.util.Log
import android.view.Display
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.dualscreen.R
import com.example.dualscreen.`interface`.DogApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL

class MainActivity : AppCompatActivity() {
    private lateinit var displayManager: DisplayManager
    private var secondScreenPresentation: SecondScreenPresentation? = null
    private val imageDir by lazy { File(filesDir, "images") }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        displayManager = getSystemService(Context.DISPLAY_SERVICE) as DisplayManager

        // Tạo thư mục lưu ảnh nếu chưa tồn tại
        if (!imageDir.exists()) {
            imageDir.mkdirs()
        }

        val button = findViewById<Button>(R.id.btn_load_images)
        button.setOnClickListener {
            loadAndSaveImage()
        }

        // Tự động hiển thị trên màn hình thứ hai nếu có
        showOnSecondScreen()
    }

    private fun loadAndSaveImage() {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://dog.ceo/api/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val dogApi = retrofit.create(DogApi::class.java)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = dogApi.getRandomDogImages()

                if (response.isSuccessful) {
                    val imageUrls = response.body()?.message
                    if (!imageUrls.isNullOrEmpty()) {
                        // Xóa tất cả ảnh cũ
                        clearOldImages()

                        // Lưu tất cả ảnh mới
                        for (imageUrl in imageUrls) {
                            saveImageFromUrl(imageUrl)
                        }
                    } else {
                        Log.e("MainActivity", "Image URLs are empty")
                    }
                } else {
                    Log.e("MainActivity", "API call failed: ${response.errorBody()?.string()}")
                }
            } catch (e: Exception) {
                Log.e("MainActivity", "Error loading images", e)
            }
        }
    }

    private fun clearOldImages() {
        imageDir.listFiles()?.forEach { file ->
            file.delete()
        }
    }

    private suspend fun saveImageFromUrl(imageUrl: String) {
        withContext(Dispatchers.IO) {
            try {
                val url = URL(imageUrl)
                val connection: HttpURLConnection = url.openConnection() as HttpURLConnection
                connection.doInput = true
                connection.connect()
                val inputStream: InputStream = connection.inputStream
                val bitmap = BitmapFactory.decodeStream(inputStream)

                val file = File(imageDir, "image_${System.currentTimeMillis()}.jpg")
                FileOutputStream(file).use { outputStream ->
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
                }

                Log.d("MainActivity", "Image saved to ${file.absolutePath}")

                withContext(Dispatchers.Main) {
                    // Cập nhật màn hình thứ hai sau khi lưu thành công
                    showOnSecondScreen()
                }
            } catch (e: Exception) {
                Log.e("MainActivity", "Error saving image", e)
            }
        }
    }

    private fun showOnSecondScreen() {
        val displays: Array<Display> = displayManager.displays

        if (displays.size > 1) {
            val secondDisplay = displays[1]

            secondScreenPresentation = SecondScreenPresentation(this, secondDisplay, imageDir)
            secondScreenPresentation?.show()
        } else {
            Log.d("MainActivity", "No secondary display found.")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        secondScreenPresentation?.dismiss()
    }
}
