package com.example.healthcare

import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import okhttp3.*
import org.json.JSONObject
import java.io.IOException
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import android.content.Intent

class HomeActivity : AppCompatActivity() {

    private val client = OkHttpClient()

    // Barcode scanner launcher
    private val barcodeLauncher = registerForActivityResult(ScanContract()) { result ->
        if (result.contents != null) {
            fetchProduct(result.contents)
        } else {
            Toast.makeText(this, "Scan Cancelled", Toast.LENGTH_SHORT).show()
        }
    }
    data class ScoreResult(
        val score: Int,
        val category: String,
        val reasons: List<String>
    )

    fun calculateScoreDetailed(ingredients: String): ScoreResult {
        var score = 8
        val text = ingredients.lowercase()

        val reasons = mutableListOf<String>()

        val veryBad = listOf("high fructose", "corn syrup", "artificial sweetener")
        val bad = listOf("sugar", "palm oil", "hydrogenated", "emulsifier", "preservative", "flavour")
        val moderate = listOf("salt", "refined", "maida", "wheat flour")

        for (v in veryBad) {
            if (text.contains(v)) {
                score -= 2
                reasons.add("Contains $v")
            }
        }

        for (b in bad) {
            if (text.contains(b)) {
                score -= 1
                reasons.add("Contains $b")
            }
        }

        for (m in moderate) {
            if (text.contains(m)) {
                score -= 1
                reasons.add("Highly processed ($m)")
            }
        }

        // Special case: sugary drinks
        if (text.contains("sugar") && ingredients.length < 120) {
            score -= 2
            reasons.add("High liquid sugar")
        }

        val count = ingredients.split(",").size
        if (count > 10) {
            score -= 1
            reasons.add("Too many ingredients (highly processed)")
        }

        score = score.coerceIn(2, 10)

        val category = when {
            score >= 7 -> "Healthy 🟢"
            score >= 4 -> "Moderate 🟡"
            else -> "Unhealthy 🔴"
        }

        // If no issues found
        if (reasons.isEmpty()) {
            reasons.add("No major harmful ingredients detected")
        }

        return ScoreResult(score, category, reasons)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        val scanBtn = findViewById<Button>(R.id.scanBtn)

        scanBtn.setOnClickListener {
            val options = ScanOptions()
            options.setPrompt("Scan a product barcode")
            options.setBeepEnabled(true)
            options.setOrientationLocked(true)

            barcodeLauncher.launch(options)
        }
    }

    // API call to Open Food Facts
    private fun fetchProduct(barcode: String) {
        val url = "https://world.openfoodfacts.org/api/v0/product/$barcode.json"

        val request = Request.Builder().url(url).build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(this@HomeActivity, "API Failed", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val res = response.body?.string() ?: ""

                try {
                    val json = JSONObject(res)

                    // ✅ CHECK IF PRODUCT EXISTS
                    if (json.optInt("status") != 1) {
                        runOnUiThread {
                            Toast.makeText(this@HomeActivity, "Product not found", Toast.LENGTH_SHORT).show()
                        }
                        return
                    }

                    val product = json.getJSONObject("product")

                    val name = product.optString("product_name", "Unknown Product")
                    val ingredients = product.optString("ingredients_text", "")

                    val result = calculateScoreDetailed(ingredients)

                    runOnUiThread {
                        val intent = Intent(this@HomeActivity, ResultActivity::class.java)

                        intent.putExtra("name", name)
                        intent.putExtra("ingredients", ingredients)
                        intent.putExtra("score", result.score)
                        intent.putExtra("category", result.category)
                        intent.putExtra("reasons", result.reasons.joinToString("\n• ", prefix = "• "))

                        startActivity(intent)
                    }

                } catch (e: Exception) {
                    runOnUiThread {
                        Toast.makeText(this@HomeActivity, "Error parsing data", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
    }
}