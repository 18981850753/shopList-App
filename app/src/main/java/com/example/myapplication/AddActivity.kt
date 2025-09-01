package com.example.myapplication

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AddActivity : AppCompatActivity() {

    private lateinit var etFruitName: EditText
    private lateinit var etFruitPrice: EditText
    private lateinit var etFruitWeight: EditText
    private lateinit var etFruitBrand: EditText
    private lateinit var etFruitRemark: EditText
    private lateinit var btnConfirm: Button
    private lateinit var btnCancel: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add)

        initViews()
        setListeners()

        // 自动填充商品名称（如果从首页操作按钮跳转而来）
        val name = intent.getStringExtra("NAME")
        if (!name.isNullOrEmpty()) {
            etFruitName.setText(name)
        }
    }


    private fun initViews() {
        etFruitName = findViewById(R.id.et_fruit_name)
        etFruitPrice = findViewById(R.id.et_fruit_price)
        etFruitWeight = findViewById(R.id.et_fruit_weight)
        etFruitBrand = findViewById(R.id.et_fruit_brand)
        etFruitRemark = findViewById(R.id.et_fruit_remark)
        btnConfirm = findViewById(R.id.btn_confirm)
        btnCancel = findViewById(R.id.btn_cancel)
    }

    private fun setListeners() {
        btnConfirm.setOnClickListener {
            addFruitRecord()
        }

        btnCancel.setOnClickListener {
            // 取消操作，直接返回主页面
            finish()
        }
    }

    private fun addFruitRecord() {
        val name = etFruitName.text.toString().trim()
        val price = etFruitPrice.text.toString().trim()
        val weight = etFruitWeight.text.toString().trim()
        val brand = etFruitBrand.text.toString().trim()
        val remark = etFruitRemark.text.toString().trim()

        // 验证必填字段
        var isValid = true

        if (name.isEmpty()) {
            etFruitName.error = "请输入商品名称"
            isValid = false
        }

        if (price.isEmpty()) {
            etFruitPrice.error = "请输入价格"
            isValid = false
        }

        if (weight.isEmpty()) {
            etFruitWeight.error = "请输入规格"
            isValid = false
        }

        // 如果必填字段未通过验证，则不继续执行
        if (!isValid) {
            return
        }

        // 格式化价格
        val formattedPrice = if (price.startsWith("¥")) price else "¥$price"

        // 默认值处理
        val formattedWeight = if (weight.isEmpty()) "1.0kg" else if (!weight.endsWith("kg")) "${weight}kg" else weight
        val formattedBrand = if (brand.isEmpty()) "无" else brand
        val formattedRemark = if (remark.isEmpty()) "无" else remark

        // 获取当前时间作为创建时间和更新时间
        val currentTime = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())

        // 保存到文件
        saveFruitRecordToFile(name, formattedPrice, formattedWeight, formattedBrand, formattedRemark, currentTime)

        // 返回主页面
        finish()
    }


    private fun saveFruitRecordToFile(name: String, price: String, weight: String, brand: String, remark: String, time: String) {
        try {
            val file = File(filesDir, "shop.txt")
            FileWriter(file, true).use { writer -> // true表示追加模式
                writer.write("$name,$price,$weight,$brand,$remark,$time,$time\n")
            }
            Toast.makeText(this, "商品记录添加成功", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "保存失败", Toast.LENGTH_SHORT).show()
        }
    }
}
