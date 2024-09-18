package otus.homework.customview

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.GsonBuilder


class MainActivity : AppCompatActivity() {

    private var pieChartView: PieChartView? = null
    private var chartView: ChartView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        pieChartView = findViewById(R.id.pieChart)
        chartView = findViewById(R.id.chart)
        val builder = GsonBuilder()
        val gson = builder.create()
        val chartData: Array<PurchaseInfo> = gson.fromJson(resources.openRawResource(R.raw.payload)
            .bufferedReader().use { it.readText() }, Array<PurchaseInfo>::class.java)
        pieChartView?.setData(chartData)
        chartView?.setData(chartData)
    }
}