package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DetailActivity : AppCompatActivity() {

    private lateinit var tableLayout: TableLayout
    private lateinit var btnBack: ImageButton
    private lateinit var btnSortPrice: ImageButton
    private lateinit var btnSortTime: ImageButton
    private lateinit var btnSortUnitPrice: ImageButton  // 新增单价排序按钮
    private lateinit var name: String
    private var recordList = mutableListOf<Record>()
    private var priceSortOrder = SortOrder.ASC // 价格排序顺序
    private var timeSortOrder = SortOrder.ASC  // 时间排序顺序
    private var unitPriceSortOrder = SortOrder.ASC  // 单价排序顺序

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)

        // 获取传递的商品名称
        name = intent.getStringExtra("NAME") ?: ""

        initViews()
        setListeners()
        loadFruitRecords()
        displayRecords()
    }

    private fun initViews() {
        tableLayout = findViewById(R.id.table_layout)
        btnBack = findViewById(R.id.btn_back)
        btnSortPrice = findViewById(R.id.btn_sort_price)
        btnSortTime = findViewById(R.id.btn_sort_time)
        btnSortUnitPrice = findViewById(R.id.btn_sort_unit_price)  // 初始化单价排序按钮
    }

    private fun setListeners() {
        btnBack.setOnClickListener {
            // 返回上一界面
            finish()
        }

        btnSortPrice.setOnClickListener {
            sortRecordsByPrice()
        }

        btnSortTime.setOnClickListener {
            sortRecordsByCreateTime()
        }

        // 添加单价排序按钮监听器
        btnSortUnitPrice.setOnClickListener {
            sortRecordsByUnitPrice()
        }
    }

    private fun loadFruitRecords() {
        recordList.clear()
        val file = File(filesDir, "shop.txt")
        if (!file.exists()) return

        try {
            BufferedReader(FileReader(file)).use { reader ->
                var line: String?
                var index = 0
                while (reader.readLine().also { line = it } != null) {
                    val parts = line?.split(",")
                    if (parts != null && parts.size >= 7 && parts[0] == name) {
                        recordList.add(Record(parts[0], parts[1], parts[2], parts[3], parts[4], parts[5], parts[6], index))
                    }
                    index++
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun displayRecords() {
        tableLayout.removeViews(1, tableLayout.childCount - 1) // 保留表头

        recordList.forEach { record ->
            val tableRow = TableRow(this).apply {
                setBackgroundResource(R.drawable.item_border)
                setPadding(4, 8, 4, 8) // 增加垂直内边距
                minimumHeight = 60 // 设置最小高度
            }

            // 名称（添加点击事件进入编辑界面）
            val nameText = TextView(this).apply {
                text = record.name
                textSize = 12f
                gravity = android.view.Gravity.CENTER
                setPadding(4, 8, 4, 8) // 增加垂直内边距
                layoutParams = TableRow.LayoutParams(80, TableRow.LayoutParams.WRAP_CONTENT)
                setOnClickListener {
                    val intent = Intent(this@DetailActivity, EditRecordActivity::class.java)
                    intent.putExtra("RECORD_DATA", "${record.name},${record.price},${record.weight},${record.brand},${record.remark},${record.createTime},${record.updateTime}")
                    intent.putExtra("RECORD_INDEX", record.index)
                    startActivity(intent)
                }
            }
            tableRow.addView(nameText)

            // 价格
            val priceText = TextView(this).apply {
                text = record.price
                textSize = 12f
                gravity = android.view.Gravity.CENTER
                setPadding(4, 8, 4, 8) // 增加垂直内边距
                layoutParams = TableRow.LayoutParams(80, TableRow.LayoutParams.WRAP_CONTENT)
            }
            tableRow.addView(priceText)

            // 规格
            val weightText = TextView(this).apply {
                text = record.weight
                textSize = 12f
                gravity = android.view.Gravity.CENTER
                setPadding(4, 8, 4, 8) // 增加垂直内边距
                layoutParams = TableRow.LayoutParams(80, TableRow.LayoutParams.WRAP_CONTENT)
            }
            tableRow.addView(weightText)

            // 单价（新增列）
            val unitPriceText = TextView(this).apply {
                // 计算单价 = 价格/规格
                val unitPrice = calculateUnitPrice(record.price, record.weight)
                text = if (unitPrice > 0) "¥${String.format("%.2f", unitPrice)}" else "N/A"
                textSize = 12f
                gravity = android.view.Gravity.CENTER
                setPadding(4, 8, 4, 8) // 增加垂直内边距
                layoutParams = TableRow.LayoutParams(80, TableRow.LayoutParams.WRAP_CONTENT)
            }
            tableRow.addView(unitPriceText)

            // 品牌
            val brandText = TextView(this).apply {
                text = record.brand
                textSize = 12f
                gravity = android.view.Gravity.CENTER
                setPadding(4, 8, 4, 8) // 增加垂直内边距
                layoutParams = TableRow.LayoutParams(80, TableRow.LayoutParams.WRAP_CONTENT)
            }
            tableRow.addView(brandText)

            // 备注
            val remarkText = TextView(this).apply {
                text = record.remark
                textSize = 12f
                gravity = android.view.Gravity.CENTER
                setPadding(4, 8, 4, 8) // 增加垂直内边距
                layoutParams = TableRow.LayoutParams(80, TableRow.LayoutParams.WRAP_CONTENT)
            }
            tableRow.addView(remarkText)

            // 创建时间
            val createTimeText = TextView(this).apply {
                text = record.createTime
                textSize = 12f
                gravity = android.view.Gravity.CENTER
                setPadding(4, 8, 4, 8) // 增加垂直内边距
                layoutParams = TableRow.LayoutParams(120, TableRow.LayoutParams.WRAP_CONTENT)
            }
            tableRow.addView(createTimeText)

            // 更新时间
            val updateTimeText = TextView(this).apply {
                text = record.updateTime
                textSize = 12f
                gravity = android.view.Gravity.CENTER
                setPadding(4, 8, 4, 8) // 增加垂直内边距
                layoutParams = TableRow.LayoutParams(120, TableRow.LayoutParams.WRAP_CONTENT)
            }
            tableRow.addView(updateTimeText)

            tableLayout.addView(tableRow)
        }
    }

    // 计算单价的方法
    private fun calculateUnitPrice(price: String, weight: String): Double {
        return try {
            // 提取价格（去除¥符号）
            val priceValue = price.replace("¥", "").toDouble()
            // 提取规格（去除kg单位）
            val weightValue = weight.replace("kg", "").toDouble()
            // 计算单价
            if (weightValue > 0) priceValue / weightValue else 0.0
        } catch (e: NumberFormatException) {
            0.0
        }
    }

    private fun sortRecordsByPrice() {
        when (priceSortOrder) {
            SortOrder.ASC -> {
                recordList.sortWith(compareBy { record ->
                    try {
                        record.price.replace("¥", "").toDouble()
                    } catch (e: NumberFormatException) {
                        0.0
                    }
                })
                priceSortOrder = SortOrder.DESC
            }
            SortOrder.DESC -> {
                recordList.sortWith(compareByDescending { record ->
                    try {
                        record.price.replace("¥", "").toDouble()
                    } catch (e: NumberFormatException) {
                        0.0
                    }
                })
                priceSortOrder = SortOrder.ASC
            }
        }
        displayRecords()
    }

    // 新增单价排序方法
    private fun sortRecordsByUnitPrice() {
        when (unitPriceSortOrder) {
            SortOrder.ASC -> {
                recordList.sortWith(compareBy { record ->
                    calculateUnitPrice(record.price, record.weight)
                })
                unitPriceSortOrder = SortOrder.DESC
            }
            SortOrder.DESC -> {
                recordList.sortWith(compareByDescending { record ->
                    calculateUnitPrice(record.price, record.weight)
                })
                unitPriceSortOrder = SortOrder.ASC
            }
        }
        displayRecords()
    }

    private fun sortRecordsByCreateTime() {
        when (timeSortOrder) {
            SortOrder.ASC -> {
                recordList.sortWith(compareBy { record ->
                    try {
                        SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).parse(record.createTime)
                    } catch (e: Exception) {
                        Date(0)
                    }
                })
                timeSortOrder = SortOrder.DESC
            }
            SortOrder.DESC -> {
                recordList.sortWith(compareByDescending { record ->
                    try {
                        SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).parse(record.createTime)
                    } catch (e: Exception) {
                        Date(0)
                    }
                })
                timeSortOrder = SortOrder.ASC
            }
        }
        displayRecords()
    }

    override fun onResume() {
        super.onResume()
        loadFruitRecords()
        displayRecords()
    }

    data class Record(
        val name: String,
        val price: String,
        val weight: String,
        val brand: String,
        val remark: String,
        val createTime: String,
        val updateTime: String,
        val index: Int
    )

    enum class SortOrder {
        ASC, DESC
    }
}
