package com.example.myapplication

import android.app.AlertDialog
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class EditRecordActivity : AppCompatActivity() {

    private lateinit var etFruitName: EditText
    private lateinit var etFruitPrice: EditText
    private lateinit var etFruitWeight: EditText
    private lateinit var etFruitBrand: EditText
    private lateinit var etFruitRemark: EditText
    private lateinit var btnConfirm: Button
    private lateinit var btnCancel: Button
    private lateinit var btnDelete: Button
    private lateinit var btnBack: ImageButton

    private lateinit var originalRecord: String
    private var recordIndex: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_record)

        // 获取传递的记录信息
        originalRecord = intent.getStringExtra("RECORD_DATA") ?: ""
        recordIndex = intent.getIntExtra("RECORD_INDEX", -1)

        initViews()
        setListeners()
        populateFields()
    }

    private fun initViews() {
        etFruitName = findViewById(R.id.et_fruit_name)
        etFruitPrice = findViewById(R.id.et_fruit_price)
        etFruitWeight = findViewById(R.id.et_fruit_weight)
        etFruitBrand = findViewById(R.id.et_fruit_brand)
        etFruitRemark = findViewById(R.id.et_fruit_remark)
        btnConfirm = findViewById(R.id.btn_confirm)
        btnCancel = findViewById(R.id.btn_cancel)
        btnDelete = findViewById(R.id.btn_delete)
        btnBack = findViewById(R.id.btn_back)
    }

    private fun setListeners() {
        btnConfirm.setOnClickListener {
            updateRecord()
        }

        btnCancel.setOnClickListener {
            finish()
        }

        btnDelete.setOnClickListener {
            showDeleteConfirmation()
        }

        btnBack.setOnClickListener {
            finish()
        }
    }

    private fun populateFields() {
        val parts = originalRecord.split(",")
        if (parts.size >= 7) {
            etFruitName.setText(parts[0])
            etFruitPrice.setText(parts[1])
            etFruitWeight.setText(parts[2])
            etFruitBrand.setText(parts[3])
            etFruitRemark.setText(parts[4])
        }
    }

    private fun updateRecord() {
        val name = etFruitName.text.toString().trim()
        val price = etFruitPrice.text.toString().trim()
        val weight = etFruitWeight.text.toString().trim()
        val brand = etFruitBrand.text.toString().trim()
        val remark = etFruitRemark.text.toString().trim()

        // 验证输入
        if (name.isEmpty()) {
            etFruitName.error = "请输入商品名称"
            return
        }

        if (price.isEmpty()) {
            etFruitPrice.error = "请输入价格"
            return
        }

        // 格式化价格
        val formattedPrice = if (price.startsWith("¥")) price else "¥$price"

        // 默认值处理
        val formattedWeight = if (weight.isEmpty()) "1.0kg" else if (!weight.endsWith("kg")) "${weight}kg" else weight
        val formattedBrand = if (brand.isEmpty()) "无" else brand
        val formattedRemark = if (remark.isEmpty()) "无" else remark

        // 获取当前时间作为更新时间
        val currentTime = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())

        // 读取原始记录获取创建时间
        val parts = originalRecord.split(",")
        val createTime = if (parts.size >= 7) parts[5] else currentTime

        // 构造更新后的记录
        val updatedRecord = "$name,$formattedPrice,$formattedWeight,$formattedBrand,$formattedRemark,$createTime,$currentTime"

        // 更新文件中的记录
        updateRecordInFile(updatedRecord)

        Toast.makeText(this, "记录更新成功", Toast.LENGTH_SHORT).show()
        finish()
    }

    private fun updateRecordInFile(updatedRecord: String) {
        val file = File(filesDir, "shop.txt")
        if (!file.exists()) return

        try {
            val lines = file.readLines().toMutableList()
            if (recordIndex >= 0 && recordIndex < lines.size) {
                lines[recordIndex] = updatedRecord
                FileWriter(file, false).use { writer -> // false表示覆盖模式
                    lines.forEach { line ->
                        writer.write("$line\n")
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "更新失败", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showDeleteConfirmation() {
        AlertDialog.Builder(this)
            .setTitle("确认删除")
            .setMessage("确定要删除这条记录吗？")
            .setPositiveButton("确定") { _, _ ->
                deleteRecord()
            }
            .setNegativeButton("取消", null)
            .show()
    }

    private fun deleteRecord() {
        val file = File(filesDir, "shop.txt")
        if (!file.exists()) return

        try {
            val lines = file.readLines().toMutableList()
            if (recordIndex >= 0 && recordIndex < lines.size) {
                lines.removeAt(recordIndex)
                FileWriter(file, false).use { writer -> // false表示覆盖模式
                    lines.forEach { line ->
                        writer.write("$line\n")
                    }
                }
                Toast.makeText(this, "记录删除成功", Toast.LENGTH_SHORT).show()
                finish()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "删除失败", Toast.LENGTH_SHORT).show()
        }
    }
}
