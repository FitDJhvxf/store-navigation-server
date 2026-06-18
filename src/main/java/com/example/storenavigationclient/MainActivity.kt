package com.example.storenavigationclient

import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanIntentResult
import com.journeyapps.barcodescanner.ScanOptions
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

// Модель данных (обязательно должна совпадать с JSON-ответом)
data class Product(
    val name: String,
    val price: Double,
    val shelfId: Int,
    val instruction: String? // Знак вопроса разрешает приходить null
)

interface ApiService {
    @GET("search")
    fun getProductByBarcode(@Query("barcode") barcode: String): Call<Product>
}

class MainActivity : AppCompatActivity() {
    private lateinit var tvResult: TextView
    private lateinit var btnScan: Button
    private lateinit var apiService: ApiService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tvResult = findViewById(R.id.tvResult)
        btnScan = findViewById(R.id.btnScan)

        val retrofit = Retrofit.Builder()
            .baseUrl("http://10.83.4.114:8080/") // IP твоего сервера
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        apiService = retrofit.create(ApiService::class.java)

        btnScan.setOnClickListener {
            val options = ScanOptions()
            options.setDesiredBarcodeFormats(ScanOptions.ALL_CODE_TYPES)
            barcodeLauncher.launch(options)
        }
    }

    private val barcodeLauncher = registerForActivityResult(ScanContract()) { result: ScanIntentResult ->
        if (result.contents != null) {
            fetchProduct(result.contents)
        }
    }

    private fun fetchProduct(barcode: String) {
        tvResult.text = "🔍 Идет поиск..."
        tvResult.setTextColor(Color.BLACK)

        apiService.getProductByBarcode(barcode).enqueue(object : Callback<Product> {
            override fun onResponse(call: Call<Product>, response: Response<Product>) {
                if (response.isSuccessful && response.body() != null) {
                    val p = response.body()!!

                    // Если instruction пустая, пишем заглушку
                    val routeText = if (p.instruction.isNullOrBlank()) "Инструкция не задана в БД" else p.instruction

                    tvResult.text = """
                        ✅ ТОВАР НАЙДЕН
                        
                        🏷 НАЗВАНИЕ: ${p.name.uppercase()}
                        💰 ЦЕНА: ${p.price} руб.
                        📍 СЕКЦИЯ: №${p.shelfId}
                        
                        🚀 МАРШРУТ:
                        $routeText
                    """.trimIndent()
                } else {
                    tvResult.setTextColor(Color.RED)
                    tvResult.text = "❌ Товар с кодом $barcode не найден."
                }
            }

            override fun onFailure(call: Call<Product>, t: Throwable) {
                tvResult.setTextColor(Color.RED)
                tvResult.text = "⚠️ Ошибка связи с сервером! Проверьте IP и порт 8080."
            }
        })
    }
}