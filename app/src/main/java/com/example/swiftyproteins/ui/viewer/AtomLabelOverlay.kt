package com.example.swiftyproteins.ui.viewer

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.opengl.Matrix
import android.util.AttributeSet
import android.view.View
import com.example.swiftyproteins.data.model.Atom
import com.example.swiftyproteins.ui.viewer.gl.ProteinRenderer

class AtomLabelOverlay @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : View(context, attrs) {

    var renderer: ProteinRenderer? = null
    var atoms: List<Atom> = emptyList()
    var centerOffset: FloatArray = FloatArray(3)
    var showLabels: Boolean = false
        set(value) { field = value; invalidate() }

    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textSize = 32f
        typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
        textAlign = Paint.Align.CENTER
        setShadowLayer(3f, 1f, 1f, Color.BLACK)
    }

    private val scratch = FloatArray(4)
    private val clip = FloatArray(4)
    private val mvp = FloatArray(16)
    private val mv = FloatArray(16)

    override fun onDraw(canvas: Canvas) {
        if (!showLabels || atoms.isEmpty()) return
        val r = renderer ?: return

        val w = width.toFloat()
        val h = height.toFloat()
        if (w <= 0 || h <= 0) return

        // Rebuild MVP from renderer state (same math as ProteinRenderer.onDrawFrame)
        val proj = r.projectionMatrix
        val view = r.viewMatrix
        val model = r.modelMatrix

        Matrix.multiplyMM(mv, 0, view, 0, model, 0)
        Matrix.multiplyMM(mvp, 0, proj, 0, mv, 0)

        val cx = centerOffset[0]
        val cy = centerOffset[1]
        val cz = centerOffset[2]

        for (atom in atoms) {
            scratch[0] = atom.x - cx
            scratch[1] = atom.y - cy
            scratch[2] = atom.z - cz
            scratch[3] = 1f

            Matrix.multiplyMV(clip, 0, mvp, 0, scratch, 0)

            if (clip[3] <= 0f) continue
            val ndcX = clip[0] / clip[3]
            val ndcY = clip[1] / clip[3]

            // Clip space to screen
            val sx = (ndcX + 1f) / 2f * w
            val sy = (1f - ndcY) / 2f * h

            // Only draw if on screen
            if (sx < -20 || sx > w + 20 || sy < -20 || sy > h + 20) continue

            canvas.drawText(atom.symbol, sx, sy - 20f, textPaint)
        }

        // Request next frame so labels follow rotation
        invalidate()
    }
}
