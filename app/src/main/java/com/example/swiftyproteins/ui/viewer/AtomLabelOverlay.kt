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
        textSize = 28f
        typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
        textAlign = Paint.Align.CENTER
    }

    private val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
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

        Matrix.multiplyMM(mv, 0, r.viewMatrix, 0, r.modelMatrix, 0)
        Matrix.multiplyMM(mvp, 0, r.projectionMatrix, 0, mv, 0)

        val cx = centerOffset[0]
        val cy = centerOffset[1]
        val cz = centerOffset[2]

        val padding = 8f
        val radius = 12f

        for (atom in atoms) {
            // Skip hydrogens — too numerous, clutter the view
            if (atom.symbol.equals("H", ignoreCase = true)) continue

            scratch[0] = atom.x - cx
            scratch[1] = atom.y - cy
            scratch[2] = atom.z - cz
            scratch[3] = 1f

            Matrix.multiplyMV(clip, 0, mvp, 0, scratch, 0)
            if (clip[3] <= 0f) continue

            val sx = (clip[0] / clip[3] + 1f) / 2f * w
            val sy = (1f - clip[1] / clip[3]) / 2f * h

            if (sx < -40 || sx > w + 40 || sy < -40 || sy > h + 40) continue

            // Label floats above the atom sphere
            val labelY = sy - 36f

            val textW = textPaint.measureText(atom.symbol)
            val left   = sx - textW / 2f - padding
            val top    = labelY - textPaint.textSize - padding / 2f
            val right  = sx + textW / 2f + padding
            val bottom = labelY + padding / 2f

            // Background pill using CPK color with opacity
            val argb = atom.colorHex
            bgPaint.color = (0xCC shl 24) or (argb and 0x00FFFFFF)
            canvas.drawRoundRect(left, top, right, bottom, radius, radius, bgPaint)

            canvas.drawText(atom.symbol, sx, labelY, textPaint)
        }

        invalidate()
    }
}
