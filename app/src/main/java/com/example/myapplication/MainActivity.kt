package com.example.myapplication

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.io.FileWriter

// 添加 PriceInfo 数据类
data class PriceInfo(
    val avgPrice: Double,
    val maxPrice: Double,
    val minPrice: Double
)

class MainActivity : AppCompatActivity() {

    private lateinit var containerRecords: LinearLayout
    private lateinit var btnAdd: Button
    private val fruitNames = mutableSetOf<String>() // 存储不重复的商品名称

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initViews()
        setListeners()

        // 初始化数据文件（如果不存在）
        initDataFile()

        // 从文件加载数据
        loadDataFromFile()

        // 动态加载列表
        loadList()
    }

    private fun initViews() {
        containerRecords = findViewById(R.id.container_records)
        btnAdd = findViewById(R.id.btn_add)
    }

    private fun setListeners() {
        btnAdd.setOnClickListener {
            // 跳转到新增记录页面
            val intent = Intent(this, AddActivity::class.java)
            startActivity(intent)
        }
    }

    private fun initDataFile() {
        // 使用应用内部私有目录存储文件，更安全
        val file = File(filesDir, "shop.txt")
        if (!file.exists()) {
            try {
                FileWriter(file).use { writer ->
                    // 创建空文件，不写入任何内容
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun loadDataFromFile() {
        fruitNames.clear()
        // 使用应用内部私有目录读取文件
        val file = File(filesDir, "shop.txt")

        if (file.exists()) {
            try {
                BufferedReader(FileReader(file)).use { reader ->
                    var line: String?
                    while (reader.readLine().also { line = it } != null) {
                        val parts = line?.split(",")
                        // 更新为检查7个字段
                        if (parts != null && parts.size >= 7) {
                            fruitNames.add(parts[0])
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                // 如果读取失败，使用默认数据
                loadDefaultFruitData()
            }
        } else {
            // 如果文件不存在，使用默认数据
            loadDefaultFruitData()
        }
    }

    private fun loadDefaultFruitData() {
        fruitNames.clear()
    }

    // 添加计算商品价格信息的方法
    private fun calculatePriceInfo(fruitName: String): PriceInfo {
        val file = File(filesDir, "shop.txt")
        if (!file.exists()) {
            return PriceInfo(0.0, 0.0, 0.0)
        }

        val prices = mutableListOf<Double>()

        try {
            BufferedReader(FileReader(file)).use { reader ->
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    val parts = line?.split(",")
                    if (parts != null && parts.size >= 7 && parts[0] == fruitName) {
                        try {
                            // 提取价格（去除¥符号）
                            val priceStr = parts[1].replace("¥", "")
                            val price = priceStr.toDouble()

                            // 提取规格（去除kg单位）
                            val weightStr = parts[2].replace("kg", "")
                            val weight = weightStr.toDouble()

                            // 计算单价（价格/规格）
                            val unitPrice = price / weight
                            prices.add(unitPrice)
                        } catch (e: NumberFormatException) {
                            // 如果解析失败，跳过该记录
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        if (prices.isEmpty()) {
            return PriceInfo(0.0, 0.0, 0.0)
        }

        return PriceInfo(
            avgPrice = prices.average(),
            maxPrice = prices.maxOrNull() ?: 0.0,
            minPrice = prices.minOrNull() ?: 0.0
        )
    }

    // 添加编辑商品名称的方法
    private fun editName(oldName: String) {
        val editText = android.widget.EditText(this)
        editText.setText(oldName)

        AlertDialog.Builder(this)
            .setTitle("编辑商品名称")
            .setView(editText)
            .setPositiveButton("确定") { _, _ ->
                val newName = editText.text.toString().trim()
                if (newName.isNotEmpty() && newName != oldName) {
                    updateName(oldName, newName)
                }
            }
            .setNegativeButton("取消", null)
            .show()
    }

    // 更新商品名称的方法
    private fun updateName(oldName: String, newName: String) {
        val file = File(filesDir, "shop.txt")
        if (!file.exists()) return

        try {
            val lines = file.readLines().toMutableList()
            var updated = false

            for (i in lines.indices) {
                val parts = lines[i].split(",")
                if (parts.size >= 7 && parts[0] == oldName) {
                    // 更新商品名称
                    val updatedLine = "$newName,${parts[1]},${parts[2]},${parts[3]},${parts[4]},${parts[5]},${parts[6]}"
                    lines[i] = updatedLine
                    updated = true
                }
            }

            if (updated) {
                FileWriter(file, false).use { writer ->
                    lines.forEach { line ->
                        writer.write("$line\n")
                    }
                }
                Toast.makeText(this, "商品名称更新成功", Toast.LENGTH_SHORT).show()
                // 重新加载数据
                loadDataFromFile()
                loadList()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "更新失败", Toast.LENGTH_SHORT).show()
        }
    }

    // 添加删除商品的方法
    private fun delete(name: String) {
        AlertDialog.Builder(this)
            .setTitle("确认删除")
            .setMessage("确定要删除商品 \"$name\" 及其所有记录吗？")
            .setPositiveButton("确定") { _, _ ->
                deleteRecords(name)
            }
            .setNegativeButton("取消", null)
            .show()
    }

    // 删除商品记录的方法
    private fun deleteRecords(name: String) {
        val file = File(filesDir, "shop.txt")
        if (!file.exists()) return

        try {
            val lines = file.readLines().toMutableList()
            // 保留不匹配的商品记录
            val filteredLines = lines.filter { line ->
                val parts = line.split(",")
                !(parts.size >= 7 && parts[0] == name)
            }

            FileWriter(file, false).use { writer ->
                filteredLines.forEach { line ->
                    writer.write("$line\n")
                }
            }
            Toast.makeText(this, "商品 \"$name\" 删除成功", Toast.LENGTH_SHORT).show()
            // 重新加载数据
            loadDataFromFile()
            loadList()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "删除失败", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadList() {
        containerRecords.removeAllViews()

        fruitNames.forEach { name ->
            val itemLayout = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                setPadding(16, 16, 16, 16)
                setBackgroundResource(R.drawable.item_border)
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(16, 8, 16, 8)
                }
            }

            // 左侧信息区域
            val infoLayout = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                layoutParams = LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1.0f
                )
            }

            // 商品名称文本
            val fruitNameTextView = TextView(this).apply {
                text = name
                textSize = 18f
                setOnClickListener {
                    // 跳转到商品详情页面
                    val intent = Intent(this@MainActivity, DetailActivity::class.java)
                    intent.putExtra("NAME", name)
                    startActivity(intent)
                }
            }

            // 计算并显示价格信息
            val priceInfo = calculatePriceInfo(name)
            val priceInfoTextView = TextView(this).apply {
                text = "平均: ¥${String.format("%.2f", priceInfo.avgPrice)}/kg " +
                        "最高: ¥${String.format("%.2f", priceInfo.maxPrice)}/kg " +
                        "最低: ¥${String.format("%.2f", priceInfo.minPrice)}/kg"
                textSize = 14f
                setTextColor(android.graphics.Color.GRAY)
            }

            infoLayout.addView(fruitNameTextView)
            infoLayout.addView(priceInfoTextView)

            // 右侧操作按钮
            val moreButton = android.widget.ImageButton(this).apply {
                setImageResource(R.drawable.ic_more) // 使用更多操作图标
                setBackgroundResource(android.R.color.transparent)
                setPadding(8, 8, 8, 8)
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )

                setOnClickListener {
                    // 显示操作菜单
                    val popupMenu = androidx.appcompat.widget.PopupMenu(this@MainActivity, this)
                    popupMenu.menuInflater.inflate(R.menu.item_menu, popupMenu.menu)

                    popupMenu.setOnMenuItemClickListener { menuItem ->
                        when (menuItem.itemId) {
                            R.id.action_add -> {
                                val intent = Intent(this@MainActivity, AddActivity::class.java)
                                intent.putExtra("NAME", name)
                                startActivity(intent)
                                true
                            }
                            R.id.action_edit -> {
                                editName(name)
                                true
                            }
                            R.id.action_delete -> {
                                delete(name)
                                true
                            }
                            else -> false
                        }
                    }
                    popupMenu.show()
                }

            }

            itemLayout.addView(infoLayout)
            itemLayout.addView(moreButton)
            containerRecords.addView(itemLayout)
        }
    }

    override fun onResume() {
        super.onResume()
        // 重新加载数据，确保新增的商品能显示
        loadDataFromFile()
        loadList()
    }
}
