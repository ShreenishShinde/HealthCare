package com.example.healthcare

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import android.widget.LinearLayout
import android.widget.Button
import android.view.View
class ResultActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_result)

        val name = intent.getStringExtra("name")
        val ingredients = intent.getStringExtra("ingredients")
        val score = intent.getIntExtra("score", 0)
        val category = intent.getStringExtra("category")

        val productName = findViewById<TextView>(R.id.productName)
        val scoreText = findViewById<TextView>(R.id.scoreText)
        val categoryText = findViewById<TextView>(R.id.categoryText)
        val ingredientsText = findViewById<TextView>(R.id.ingredientsText)

        productName.text = name
        scoreText.text = "$score/10"
        categoryText.text = category
        ingredientsText.text = ingredients

        val rootLayout = findViewById<View>(android.R.id.content)

        when {
            score >= 7 -> {
                scoreText.setTextColor(getColor(android.R.color.holo_green_dark))
                categoryText.setTextColor(getColor(android.R.color.holo_green_dark))
            }
            score >= 4 -> {
                scoreText.setTextColor(getColor(android.R.color.holo_orange_dark))
                categoryText.setTextColor(getColor(android.R.color.holo_orange_dark))
            }
            else -> {
                scoreText.setTextColor(getColor(android.R.color.holo_red_dark))
                categoryText.setTextColor(getColor(android.R.color.holo_red_dark))
            }
        }
        val scanAgainBtn = findViewById<Button>(R.id.scanAgainBtn)
        val reasonsText = findViewById<TextView>(R.id.reasonsText)

        val reasons = intent.getStringExtra("reasons")
        reasonsText.text = reasons
        scanAgainBtn.setOnClickListener {
            finish() // goes back to scanner
        }
    }
}