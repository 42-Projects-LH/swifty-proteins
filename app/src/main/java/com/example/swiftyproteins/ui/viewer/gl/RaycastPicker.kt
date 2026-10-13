package com.example.swiftyproteins.ui.viewer.gl

import android.opengl.Matrix
import com.example.swiftyproteins.data.model.Atom
import kotlin.math.sqrt

/**
 * High-precision 3D raycasting picker for atom selection on screen touch.
 */
object RaycastPicker {

    fun pickAtom(
        screenX: Float,
        screenY: Float,
        viewWidth: Int,
        viewHeight: Int,
        viewMatrix: FloatArray,
        projMatrix: FloatArray,
        modelMatrix: FloatArray,
        atoms: List<Atom>,
        centerOffset: FloatArray,
        isSpaceFilling: Boolean
    ): Atom? {
        if (viewWidth <= 0 || viewHeight <= 0 || atoms.isEmpty()) return null

        // 1. Compute Full MVP Matrix
        val mvMatrix = FloatArray(16)
        val mvpMatrix = FloatArray(16)
        val invMvpMatrix = FloatArray(16)

        Matrix.multiplyMM(mvMatrix, 0, viewMatrix, 0, modelMatrix, 0)
        Matrix.multiplyMM(mvpMatrix, 0, projMatrix, 0, mvMatrix, 0)

        // 2. Invert MVP Matrix
        val success = Matrix.invertM(invMvpMatrix, 0, mvpMatrix, 0)
        if (!success) return null

        // 3. Screen coordinates to NDC [-1, 1]
        val ndcX = (2.0f * screenX) / viewWidth.toFloat() - 1.0f
        val ndcY = 1.0f - (2.0f * screenY) / viewHeight.toFloat()

        // 4. Unproject Near Point (-1) and Far Point (+1)
        val nearClip = floatArrayOf(ndcX, ndcY, -1.0f, 1.0f)
        val farClip = floatArrayOf(ndcX, ndcY, 1.0f, 1.0f)

        val nearWorld = FloatArray(4)
        val farWorld = FloatArray(4)

        Matrix.multiplyMV(nearWorld, 0, invMvpMatrix, 0, nearClip, 0)
        Matrix.multiplyMV(farWorld, 0, invMvpMatrix, 0, farClip, 0)

        if (nearWorld[3] == 0f || farWorld[3] == 0f) return null

        val rayOriginX = nearWorld[0] / nearWorld[3]
        val rayOriginY = nearWorld[1] / nearWorld[3]
        val rayOriginZ = nearWorld[2] / nearWorld[3]

        val farX = farWorld[0] / farWorld[3]
        val farY = farWorld[1] / farWorld[3]
        val farZ = farWorld[2] / farWorld[3]

        var dirX = farX - rayOriginX
        var dirY = farY - rayOriginY
        var dirZ = farZ - rayOriginZ

        val len = sqrt(dirX * dirX + dirY * dirY + dirZ * dirZ)
        if (len == 0f) return null
        dirX /= len
        dirY /= len
        dirZ /= len

        // 5. Test Ray-Sphere Intersection against all atoms
        var closestDistance = Float.MAX_VALUE
        var selectedAtom: Atom? = null

        val cx = centerOffset[0]
        val cy = centerOffset[1]
        val cz = centerOffset[2]

        for (atom in atoms) {
            // Atom position centered at origin
            val atomX = atom.x - cx
            val atomY = atom.y - cy
            val atomZ = atom.z - cz

            // Visual radius + touch padding for easier mobile interaction
            val visualRadius = if (isSpaceFilling) atom.vdwRadius else atom.ballRadius
            val pickRadius = visualRadius + 0.25f // 0.25A touch tolerance

            val deltaX = rayOriginX - atomX
            val deltaY = rayOriginY - atomY
            val deltaZ = rayOriginZ - atomZ

            // a = 1.0 because dir is normalized
            val b = 2.0f * (dirX * deltaX + dirY * deltaY + dirZ * deltaZ)
            val c = (deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ) - (pickRadius * pickRadius)

            val discriminant = b * b - 4.0f * c
            if (discriminant >= 0) {
                val t = (-b - sqrt(discriminant)) / 2.0f
                if (t in 0.0f..<closestDistance) {
                    closestDistance = t
                    selectedAtom = atom
                }
            }
        }

        return selectedAtom
    }
}
