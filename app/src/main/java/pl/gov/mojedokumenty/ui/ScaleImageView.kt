package pl.gov.mojedokumenty.ui

import android.content.Context
import android.graphics.Matrix
import android.graphics.RectF
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import androidx.appcompat.widget.AppCompatImageView
import kotlin.math.min

/**
 * ImageView z powiększaniem „pinch” i przesuwaniem — podgląd stron dokumentu.
 * Zależność wyłącznie od SDK (bez zewnętrznych bibliotek zoomujących).
 */
class ScaleImageView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatImageView(context, attrs, defStyleAttr) {

    private val matrixValues = FloatArray(9)
    private val displayMatrix = Matrix()
    private var minScale = 1f
    private var maxScale = 8f

    private val scaleDetector = ScaleGestureDetector(context,
        object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
            override fun onScale(detector: ScaleGestureDetector): Boolean {
                scaleImage(detector.scaleFactor, detector.focusX, detector.focusY)
                return true
            }
        })

    private val gestureDetector = GestureDetector(context,
        object : GestureDetector.SimpleOnGestureListener() {
            override fun onScroll(e1: MotionEvent?, e2: MotionEvent, dx: Float, dy: Float): Boolean {
                if (imageScale() > minScale + 0.01f) {
                    translateBy(-dx, -dy)
                    invalidate()
                }
                return true
            }

            override fun onDoubleTap(e: MotionEvent): Boolean {
                if (imageScale() > minScale + 0.01f) {
                    resetZoom()
                } else {
                    scaleImage(2f, e.x, e.y)
                }
                return true
            }
        })

    init {
        scaleType = ScaleType.MATRIX
    }

    override fun setImageBitmap(bm: android.graphics.Bitmap?) {
        super.setImageBitmap(bm)
        fitToView()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        fitToView()
    }

    private fun fitToView() {
        val d = drawable ?: return
        val vw = width.toFloat().takeIf { it > 0 } ?: return
        val vh = height.toFloat().takeIf { it > 0 } ?: return
        val iw = d.bounds.width().toFloat()
        val ih = d.bounds.height().toFloat()
        minScale = min(vw / iw, vh / ih)
        maxScale = minScale * 8f
        displayMatrix.reset()
        displayMatrix.postScale(minScale, minScale)
        // wyśrodkuj
        val rect = RectF(0f, 0f, iw, ih)
        displayMatrix.mapRect(rect)
        displayMatrix.postTranslate((vw - rect.width()) / 2f, (vh - rect.height()) / 2f)
        imageMatrix = displayMatrix
    }

    private fun imageScale(): Float {
        imageMatrix.getValues(matrixValues)
        return matrixValues[Matrix.MSCALE_X]
    }

    private fun scaleImage(factor: Float, focusX: Float, focusY: Float) {
        val current = imageScale()
        val target = current * factor
        val clamped = when {
            target < minScale -> minScale / current
            target > maxScale -> maxScale / current
            else -> factor
        }
        displayMatrix.set(imageMatrix)
        displayMatrix.postScale(clamped, clamped, focusX, focusY)
        clampTranslation()
        imageMatrix = displayMatrix
    }

    private fun translateBy(dx: Float, dy: Float) {
        displayMatrix.set(imageMatrix)
        displayMatrix.postTranslate(dx, dy)
        clampTranslation()
        imageMatrix = displayMatrix
    }

    private fun clampTranslation() {
        val d = drawable ?: return
        val rect = RectF(0f, 0f, d.bounds.width().toFloat(), d.bounds.height().toFloat())
        displayMatrix.mapRect(rect)
        var tx = 0f
        var ty = 0f
        if (rect.width() <= width) tx = (width - rect.width()) / 2f - rect.left
        else {
            if (rect.left > 0) tx = -rect.left
            if (rect.right < width) tx = width - rect.right
        }
        if (rect.height() <= height) ty = (height - rect.height()) / 2f - rect.top
        else {
            if (rect.top > 0) ty = -rect.top
            if (rect.bottom < height) ty = height - rect.bottom
        }
        displayMatrix.postTranslate(tx, ty)
    }

    private fun resetZoom() {
        fitToView()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        // wyłącz intercept rodzica (ScrollView), żeby dało się przesuwać powiększony obraz
        parent?.requestDisallowInterceptTouchEvent(true)
        scaleDetector.onTouchEvent(event)
        gestureDetector.onTouchEvent(event)
        if (event.action == MotionEvent.ACTION_UP || event.action == MotionEvent.ACTION_CANCEL) {
            if (imageScale() <= minScale + 0.01f) fitToView()
        }
        return true
    }
}
