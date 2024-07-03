package com.cheenx.demo.scankitdemo

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Shader
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import android.view.animation.LinearInterpolator
import androidx.core.graphics.drawable.toBitmapOrNull


/**
 * @auther: Qinjian Xuan
 * @date  : 2024/7/2 .
 * <P>
 * Description:
 * <P>
 */
class ScanView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : View(context, attrs) {

    private val frameColor: Int
    private val frameCornerColor: Int
    private val lineColor: Int
    private val lineDrawable: Drawable?
    private val transparentLineColor: Int
    private val frameWidth: Int
    private val lineMoveDuration: Int
    private val framePaint: Paint
    private val frameCornerPaint: Paint
    private val linePaint: Paint

    private var width = 0
    private var height = 0

    private var animator: ValueAnimator? = null
    private var lineHeight = 0f
    private var running = false


    init {
        val a = context.obtainStyledAttributes(attrs, R.styleable.ScanView)
        frameColor = a.getColor(R.styleable.ScanView_frame_color, Color.WHITE)
        frameCornerColor = a.getColor(R.styleable.ScanView_frame_corner_color, Color.WHITE)
        frameWidth = a.getDimension(R.styleable.ScanView_frame_line_width, dp2Px(10f)).toInt()
        lineColor = a.getColor(R.styleable.ScanView_scan_line_color, Color.WHITE)
        lineDrawable = a.getDrawable(R.styleable.ScanView_scan_line_drawable)
        lineMoveDuration = a.getInt(R.styleable.ScanView_line_move_duration, 3000)
        a.recycle()

        framePaint = Paint(Paint.ANTI_ALIAS_FLAG);
        framePaint.style = Paint.Style.STROKE
        framePaint.strokeWidth = (frameWidth / 7.0).toFloat()
        framePaint.setColor(frameColor)

        frameCornerPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        frameCornerPaint.style = Paint.Style.FILL
        frameCornerPaint.strokeWidth = (frameWidth * 1.6).toFloat()
        frameCornerPaint.setColor(frameCornerColor)

        linePaint = Paint(Paint.ANTI_ALIAS_FLAG)
        linePaint.style = Paint.Style.FILL
        linePaint.strokeWidth = (frameWidth / 5.0).toFloat()

        val red: Int = lineColor shr 16 and 0xFF
        val green: Int = lineColor shr 8 and 0xFF
        val blue: Int = lineColor and 0xFF
        val transparentColor = 0x00000000
        transparentLineColor =
            transparentColor and -0x1000000 or (red shl 16) or (green shl 8) or blue


    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        width = measuredWidth
        height = measuredHeight
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        drawFrame(canvas)
        drawFrameCorner(canvas)
        drawScanLine(canvas)
    }

    private fun drawFrame(canvas: Canvas) {
        val left = (frameWidth / 2.1).toFloat()
        val right = (width - (frameWidth / 2.1)).toFloat()
        val top = left
        val bottom = (height - (frameWidth / 2.1)).toFloat()

        canvas.drawRect(left, top, right, bottom, framePaint)
    }

    private fun drawFrameCorner(canvas: Canvas) {
        val length = (height / 6.0).toFloat()
        canvas.drawLine(0f, 0f, length, 0f, frameCornerPaint)
        canvas.drawLine(0f, 0f, 0f, length, frameCornerPaint)
        canvas.drawLine(width - length, 0f, width.toFloat(), 0f, frameCornerPaint)
        canvas.drawLine(width.toFloat(), 0f, width.toFloat(), length, frameCornerPaint)
        canvas.drawLine(
            width.toFloat(),
            height - length,
            width.toFloat(),
            height.toFloat(),
            frameCornerPaint
        )
        canvas.drawLine(
            width - length,
            height.toFloat(),
            width.toFloat(),
            height.toFloat(),
            frameCornerPaint
        )
        canvas.drawLine(
            0f,
            height.toFloat(),
            length,
            height.toFloat(),
            frameCornerPaint
        )
        canvas.drawLine(
            0f,
            height - length,
            0f,
            height.toFloat(),
            frameCornerPaint
        )
    }

    private fun drawScanLine(canvas: Canvas) {

        if (lineHeight < 0.1 * height) return

        if (lineDrawable == null) {
            linePaint.shader = LinearGradient(
                0f, 0f, width.toFloat(), 0f, intArrayOf(
                    transparentLineColor, lineColor, transparentLineColor
                ), null,
                Shader.TileMode.CLAMP
            )
            canvas.drawLine(
                frameWidth.toFloat(),
                lineHeight,
                width.toFloat() - frameWidth,
                lineHeight,
                linePaint
            )
        } else {
            val bitmap = lineDrawable.toBitmapOrNull(width = width)
            if (bitmap != null) {
                canvas.drawBitmap(bitmap, 0f, lineHeight, linePaint)
            }
        }
    }

    fun startScan() {
        if (!isRunning()) {
            post {
                animator = ValueAnimator.ofFloat(0.05f * height, 0.85f * height);
                animator?.setDuration(lineMoveDuration.toLong()) // 动画持续时间为2秒
                animator?.interpolator = LinearInterpolator()
                animator?.repeatCount = -1
                animator?.repeatMode = ValueAnimator.RESTART
                animator?.addUpdateListener { ani ->
                    lineHeight = ani.animatedValue as? Float ?: 0f
                    invalidate()
                }
                animator?.start()

                running = true
            }
        }
    }

    fun stop() {
        animator?.removeAllUpdateListeners()
        animator?.cancel()
        animator = null
        running = false
    }

    fun isRunning() = running

    private fun dp2Px(dpValue: Float): Float {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dpValue,
            getResources().getDisplayMetrics()
        )
    }


}