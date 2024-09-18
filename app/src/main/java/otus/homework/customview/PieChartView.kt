package otus.homework.customview

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.os.Parcelable
import android.util.AttributeSet
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import kotlinx.parcelize.Parcelize
import otus.homework.customview.utils.px
import kotlin.math.*


class PieChartView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : View(context, attrs) {

    val _height = 200.px
    val _width = 200.px
    private var data: List<Pair<Int, String>> = emptyList()

    private val red1Paint = Paint().apply {
        color = ContextCompat.getColor(context, R.color.red_1)
        style = Paint.Style.FILL
    }

    private val red2Paint = Paint().apply {
        color = ContextCompat.getColor(context, R.color.red_2)
        style = Paint.Style.FILL
    }

    private val orange1Paint = Paint().apply {
        color = ContextCompat.getColor(context, R.color.orange_1)
        style = Paint.Style.FILL
    }

    private val yellow1Paint = Paint().apply {
        color = ContextCompat.getColor(context, R.color.yellow_1)
        style = Paint.Style.FILL
    }

    private val green1Paint = Paint().apply {
        color = ContextCompat.getColor(context, R.color.green_1)
        style = Paint.Style.FILL
    }

    private val green2Paint = Paint().apply {
        color = ContextCompat.getColor(context, R.color.green_2)
        style = Paint.Style.FILL
    }

    private val blue1Paint = Paint().apply {
        color = ContextCompat.getColor(context, R.color.blue_1)
        style = Paint.Style.FILL
    }

    private val blue2Paint = Paint().apply {
        color = ContextCompat.getColor(context, R.color.blue_2)
        style = Paint.Style.FILL
    }

    private val blue3Paint = Paint().apply {
        color = ContextCompat.getColor(context, R.color.blue_3)
        style = Paint.Style.FILL
    }

    private val purple1Paint = Paint().apply {
        color = ContextCompat.getColor(context, R.color.purple_1)
        style = Paint.Style.FILL
    }

    private val paintList = listOf(
        red1Paint,
        red2Paint,
        orange1Paint,
        yellow1Paint,
        green1Paint,
        green2Paint,
        blue1Paint,
        blue2Paint,
        blue3Paint,
        purple1Paint
    )
    val sections: MutableList<CategoryInfo> = mutableListOf()

    private val eraserPaint = Paint().apply {
        color = resources.getColor(android.R.color.transparent)
        xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
    }

    private val rect = RectF()

    private val generalGestureDetector =
        GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
            override fun onDown(e: MotionEvent): Boolean {
                // Проверим, входит ли точка в круг
                val holeRadius = (width / 4).toFloat()
                // Расстояние до центра круга по x
                val xToCenter = abs((width / 2).toFloat() - e.x)
                // Расстояние до центра круга по y
                val yToCenter = abs((height / 2).toFloat() - e.y)
                if (xToCenter <= holeRadius && yToCenter <= holeRadius) {
                    return false
                }
                val centerX = (width / 2).toFloat()
                val centerY = (height / 2).toFloat()
                val circleRadius = (width / 2).toFloat()
                if ((centerX - e.x) * (centerX - e.x) + (centerY - e.y) * (centerY - e.y) > circleRadius * circleRadius) {
                    Log.d("PieChartView", "Координаты не входят в круг")
                    return false
                }
                // Находим длины сторон треугольника, образованного тремя очками: центом круга,
                // крайней правой точкой круга, точкой касания
                val a = circleRadius
                // Расстояние от точки касания до центра круга
                val b = sqrt((xToCenter * xToCenter) + (yToCenter * yToCenter))
                // Расстояние между точкой касания и крайней правой точкой круга
                val c =
                    sqrt(((width - e.x) * (width - e.x)) + (abs(centerY - e.y) * abs(centerY - e.y)))
                // Находим угол между отрезком, соединяющим центр круга и самую правую точку и
                // отрезком от точки касания до ценра круга
                var angle = acos((a * a + b * b - c * c) / (2 * a * b)) * (180 / Math.PI)
                if (e.y < centerY) angle = 360 - angle
                // Проверяем, к какой секции относится этот угол
                sections.forEach {
                    if (it.startAngle <= angle && it.endAngle > angle) {
                        Toast.makeText(context, it.name, Toast.LENGTH_SHORT).show()
                    }
                }
                Log.d("PieChartView", "a=$a, b=$b, c=$c, angle = $angle")
                return true
            }
        })

    init {
        setWillNotDraw(false)
        setLayerType(LAYER_TYPE_HARDWARE, null)
    }

    override fun onSaveInstanceState(): Parcelable? {
        val superState = super.onSaveInstanceState()
        return MyState(superState, data)
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        val myState = state as? MyState
        super.onRestoreInstanceState(myState?.superState ?: state)

        data = myState?.data ?: emptyList()
        invalidate()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val wMode = MeasureSpec.getMode(widthMeasureSpec)
        val hMode = MeasureSpec.getMode(heightMeasureSpec)
        val wSize = MeasureSpec.getSize(widthMeasureSpec)
        val hSize = MeasureSpec.getSize(heightMeasureSpec)

        val width = when (wMode) {
            MeasureSpec.EXACTLY -> wSize
            MeasureSpec.AT_MOST -> min(_width, wSize)
            MeasureSpec.UNSPECIFIED -> _width
            else -> _width
        }
        val height = when (hMode) {
            MeasureSpec.EXACTLY -> hSize
            MeasureSpec.AT_MOST -> min(_height, hSize)
            MeasureSpec.UNSPECIFIED -> _height
            else -> _height
        }
        setMeasuredDimension(width, height)
    }

    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas) {
        if (data.isEmpty()) return
        rect.set(0f, 0f, width.toFloat(), height.toFloat())
        val summaryAmount: Int = data.sumOf { it.first }
        var startAngle = 0f
        data.mapIndexed { index, category ->
            val angle = 360 / (summaryAmount.toFloat() / category.first.toFloat())
            CategoryInfo(category.second, category.first, startAngle, startAngle + angle).also {
                sections.add(it)
            }
            canvas.drawArc(rect, startAngle, angle, true, paintList[index])
            startAngle += angle
        }
        canvas.drawCircle(
            (width / 2).toFloat(),
            (height / 2).toFloat(),
            (height / 4).toFloat(),
            eraserPaint
        )
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        generalGestureDetector.onTouchEvent(event)
        return true
    }

    fun setData(purchaseInfoList: Array<PurchaseInfo>) {
        data = purchaseInfoList.groupBy { it.category }.map {
            it.value.sumOf { it.amount } to it.key
        }.sortedByDescending { it.first }.take(10)
        invalidate()
    }
}

/**
 * Класс с информацией о секции
 *
 * @param name название категории (Здоровье, Продукты...)
 * @param amount общая сумма покупок в этой категории
 * @param startAngle угол, с которого начинает отрисовываться секция
 * @param endAngle угол, которым заканчивает отрисовываться секция
 */
data class CategoryInfo(
    val name: String,
    val amount: Int,
    val startAngle: Float,
    val endAngle: Float
)

@Parcelize
class MyState(private val superSavedState: Parcelable?, val data: List<Pair<Int, String>>) :
    View.BaseSavedState(superSavedState), Parcelable
