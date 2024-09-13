package com.example.dualscreen.activity

import android.app.Presentation
import android.content.Context
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Display
import android.widget.ImageView
import com.example.dualscreen.R
import java.io.File

class SecondScreenPresentation(
    context: Context,
    display: Display,
    private val imageDir: File
) : Presentation(context, display) {

    private lateinit var imageView: ImageView
    private val imageFiles = mutableListOf<File>()
    private var currentImageIndex = 0
    private val handler = Handler(Looper.getMainLooper())
    private val imageSwitcherRunnable = object : Runnable {
        override fun run() {
            if (imageFiles.isNotEmpty()) {
                val bitmap = BitmapFactory.decodeFile(imageFiles[currentImageIndex].absolutePath)
                imageView.setImageBitmap(bitmap)
                currentImageIndex = (currentImageIndex + 1) % imageFiles.size
                handler.postDelayed(this, 1000) // Đổi ảnh mỗi 5 giây
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_second)

        imageView = findViewById(R.id.imageView)

        // Kiểm tra tất cả các tệp hình ảnh trong thư mục
        imageDir.listFiles()?.filter { it.extension == "jpg" }?.let { files ->
            imageFiles.addAll(files)
            if (imageFiles.isEmpty()) {
                // Nếu không có hình ảnh, hiển thị hình ảnh mặc định từ drawable
                imageView.setImageResource(R.drawable.background_image_1)
            } else {
                // Bắt đầu chuyển đổi ảnh
                handler.post(imageSwitcherRunnable)
            }
        } ?: run {
            // Nếu không có tệp nào, hiển thị hình ảnh mặc định từ drawable
            imageView.setImageResource(R.drawable.background_image_1)
        }
    }

    override fun dismiss() {
        super.dismiss()
        handler.removeCallbacks(imageSwitcherRunnable)
    }
}
