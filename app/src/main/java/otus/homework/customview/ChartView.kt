package otus.homework.customview

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import android.os.Build
import android.os.Parcelable
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import kotlinx.parcelize.Parcelize
import otus.homework.customview.utils.px
import java.time.Duration
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import kotlin.math.min

class ChartView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : View(context, attrs) {

    private var data: Map<String, List<PurchaseInfo>> = emptyMap()

    val _height = 200.px
    val _width = 200.px

    private val path = Path()
    private val _strokeWidth = 10f

    // Отступ от края, необходимый, чтобы линия полностью помещалась в края вьюшки
    private val lineMargin = _strokeWidth / 2

    private val redPaint = Paint().apply {
        color = ContextCompat.getColor(context, R.color.red_1)
        strokeWidth = _strokeWidth
        style = Paint.Style.STROKE
    }

    private val orangePaint = Paint().apply {
        color = ContextCompat.getColor(context, R.color.orange_1)
        strokeWidth = _strokeWidth
        style = Paint.Style.STROKE
    }

    private val yellow1Paint = Paint().apply {
        color = ContextCompat.getColor(context, R.color.yellow_1)
        strokeWidth = _strokeWidth
        style = Paint.Style.STROKE
    }

    private val greenPaint = Paint().apply {
        color = ContextCompat.getColor(context, R.color.green_1)
        strokeWidth = _strokeWidth
        style = Paint.Style.STROKE
    }

    private val blue1Paint = Paint().apply {
        color = ContextCompat.getColor(context, R.color.blue_1)
        strokeWidth = _strokeWidth
        style = Paint.Style.STROKE
    }

    private val blue2Paint = Paint().apply {
        color = ContextCompat.getColor(context, R.color.blue_3)
        strokeWidth = _strokeWidth
        style = Paint.Style.STROKE
    }

    private val purple1Paint = Paint().apply {
        color = ContextCompat.getColor(context, R.color.purple_1)
        strokeWidth = _strokeWidth
        style = Paint.Style.STROKE
    }

    private val paintList = listOf(
        redPaint,
        orangePaint,
        yellow1Paint,
        greenPaint,
        blue1Paint,
        blue2Paint,
        purple1Paint
    )

    override fun onSaveInstanceState(): Parcelable? {
        val superState = super.onSaveInstanceState()
        return ChartState(superState, data)
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        val chartState = state as? ChartState
        super.onRestoreInstanceState(chartState?.superState ?: state)

        data = chartState?.data ?: emptyMap()
        invalidate()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onDraw(canvas: Canvas) {
        // Максимальная сумма трат
        val maxCount = data.map { it.value.maxOf { purchaseInfo -> purchaseInfo.amount } }.maxOrNull() ?: 0
        // Последний день, в который были совершены покупки
        val lastDateLong = data.map { it.value.maxOf { purchaseInfo -> purchaseInfo.time } }.maxOrNull()
        val lastDate = LocalDateTime.ofInstant(Instant.ofEpochSecond(lastDateLong!!), ZoneId.systemDefault())

        // Первый день, в который были совершены покупки
        val firstDateLong = data.map { it.value.minOf { purchaseInfo -> purchaseInfo.time } }.minOrNull()
        val firstDate = LocalDateTime.ofInstant(Instant.ofEpochSecond(firstDateLong!!), ZoneId.systemDefault())

        // Всего дней между первой и последней покупкой
        val daysCount = Duration.between(
            firstDate.toLocalDate().atStartOfDay(), lastDate.toLocalDate().atStartOfDay()
        ).toDays()
        // Ширина одного дня на графике
        val dayWidth = width / daysCount

        data.values.forEachIndexed { index, purchaseInfoList ->
            path.reset()
            path.moveTo(0f + lineMargin, height.toFloat() - lineMargin)

            purchaseInfoList.sortedBy { it.time }.forEachIndexed() { index, purchaseInfo ->
                // Вычисяем текущий день относительно первого дня покупок
                val purchaseDay = LocalDateTime.ofInstant(
                    Instant.ofEpochSecond(purchaseInfo.time),
                    ZoneId.systemDefault()
                ).toLocalDate().atStartOfDay()
                // День совершения окупки относительно первого дня покупок
                val purchaseDayByFirstDay = Duration.between(
                    firstDate.toLocalDate().atStartOfDay(), purchaseDay
                ).toDays()

                if (index == 0 && purchaseDayByFirstDay >= 1) {
                    // Если покупка совершена не в певый день, рисуем горизонтальную линию от нуля до дня покупки
                    val x = dayWidth * purchaseDayByFirstDay.toFloat() + lineMargin
                    val y = height.toFloat() - lineMargin
                    path.lineTo(x, y)
                }
                path.lineTo(
                    dayWidth * purchaseDayByFirstDay.toFloat() + lineMargin,
                    (height - ((height.toFloat() / maxCount!!.toFloat()) * purchaseInfo.amount)) - lineMargin
                )
            }
            val currentStrokePaint = paintList[index]
            currentStrokePaint.pathEffect = CornerPathEffect(50f)
            canvas.drawPath(path, currentStrokePaint)
        }
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

    fun setData(purchaseInfoList: Array<PurchaseInfo>) {
        data = purchaseInfoList.groupBy { it.category }
        invalidate()
    }
}

@Parcelize
class ChartState(private val superSavedState: Parcelable?, val data: Map<String, List<PurchaseInfo>>) :
    View.BaseSavedState(superSavedState), Parcelable