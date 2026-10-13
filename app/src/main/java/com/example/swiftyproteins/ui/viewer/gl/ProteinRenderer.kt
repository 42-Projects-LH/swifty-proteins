package com.example.swiftyproteins.ui.viewer.gl

import android.graphics.Bitmap
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import android.os.Handler
import android.os.Looper
import com.example.swiftyproteins.data.model.Atom
import com.example.swiftyproteins.data.model.Molecule
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10
import kotlin.math.acos
import kotlin.math.max
import kotlin.math.sqrt

enum class RenderModel {
    BALL_AND_STICK,
    SPACE_FILLING,
    STICK,
    WIREFRAME
}

class ProteinRenderer : GLSurfaceView.Renderer {

    // Molecule Data
    @Volatile
    private var molecule: Molecule? = null
    private val centerOffset = FloatArray(3)
    private var boundingRadius = 5.0f

    // Camera & Transform Parameters
    @Volatile
    var cameraDistance = 20.0f
    private var initialDistance = 20.0f

    @Volatile
    var rotationAngleX = 25.0f

    @Volatile
    var rotationAngleY = 35.0f

    @Volatile
    var panX = 0.0f

    @Volatile
    var panY = 0.0f

    @Volatile
    var renderModel: RenderModel = RenderModel.BALL_AND_STICK

    @Volatile
    var selectedAtom: Atom? = null

    // Screen Dimensions
    @Volatile
    var surfaceWidth = 1
        private set

    @Volatile
    var surfaceHeight = 1
        private set

    // Matrices
    private val projectionMatrix = FloatArray(16)
    private val viewMatrix = FloatArray(16)
    private val modelMatrix = FloatArray(16)
    private val mvMatrix = FloatArray(16)
    private val mvpMatrix = FloatArray(16)
    private val tempMatrix = FloatArray(16)

    // Meshes & Shaders
    private var sphereMesh: SphereMesh? = null
    private var cylinderMesh: CylinderMesh? = null

    private var litProgram = 0
    private var uMVPMatrixHandle = 0
    private var uMVMatrixHandle = 0
    private var uLightPosHandle = 0
    private var uColorHandle = 0
    private var uShininessHandle = 0
    private var aPositionHandle = 0
    private var aNormalHandle = 0

    private var wireProgram = 0
    private var uWireMVPHandle = 0
    private var uWireColorHandle = 0
    private var aWirePositionHandle = 0

    // Directional light vector in eye space (from camera top-right)
    private val lightPositionEye = floatArrayOf(0.35f, 0.65f, 1.0f)

    // Screenshot capture state
    @Volatile
    private var pendingScreenshotCallback: ((Bitmap) -> Unit)? = null

    // Atom look-up map for bonds
    private val atomMap = HashMap<String, Atom>()

    // Wireframe dynamic vertex buffer
    private var wireframeBuffer: FloatBuffer? = null
    private var wireframeVertexCount = 0

    fun setMolecule(mol: Molecule) {
        this.molecule = mol
        atomMap.clear()
        for (atom in mol.atoms) {
            atomMap[atom.id] = atom
        }

        // Calculate centroid and bounding radius
        if (mol.atoms.isNotEmpty()) {
            var sumX = 0f
            var sumY = 0f
            var sumZ = 0f
            for (a in mol.atoms) {
                sumX += a.x
                sumY += a.y
                sumZ += a.z
            }
            centerOffset[0] = sumX / mol.atoms.size
            centerOffset[1] = sumY / mol.atoms.size
            centerOffset[2] = sumZ / mol.atoms.size

            var maxDistSq = 0f
            for (a in mol.atoms) {
                val dx = a.x - centerOffset[0]
                val dy = a.y - centerOffset[1]
                val dz = a.z - centerOffset[2]
                val distSq = dx * dx + dy * dy + dz * dz
                if (distSq > maxDistSq) maxDistSq = distSq
            }
            boundingRadius = max(sqrt(maxDistSq), 2.0f)
            initialDistance = boundingRadius * 3.2f
            cameraDistance = initialDistance
        }

        buildWireframeGeometry()
    }

    private fun buildWireframeGeometry() {
        val mol = molecule ?: return
        val lineCoords = ArrayList<Float>()
        val cx = centerOffset[0]
        val cy = centerOffset[1]
        val cz = centerOffset[2]

        for (bond in mol.bonds) {
            val a1 = atomMap[bond.atom1Id]
            val a2 = atomMap[bond.atom2Id]
            if (a1 != null && a2 != null) {
                lineCoords.add(a1.x - cx)
                lineCoords.add(a1.y - cy)
                lineCoords.add(a1.z - cz)

                lineCoords.add(a2.x - cx)
                lineCoords.add(a2.y - cy)
                lineCoords.add(a2.z - cz)
            }
        }

        wireframeVertexCount = lineCoords.size / 3
        if (wireframeVertexCount > 0) {
            wireframeBuffer = ByteBuffer.allocateDirect(lineCoords.size * 4).run {
                order(ByteOrder.nativeOrder())
                asFloatBuffer().apply {
                    for (c in lineCoords) put(c)
                    position(0)
                }
            }
        }
    }

    fun resetCamera() {
        cameraDistance = initialDistance
        rotationAngleX = 25.0f
        rotationAngleY = 35.0f
        panX = 0.0f
        panY = 0.0f
    }

    fun requestScreenshot(callback: (Bitmap) -> Unit) {
        pendingScreenshotCallback = callback
    }

    fun pickAtom(screenX: Float, screenY: Float): Atom? {
        val mol = molecule ?: return null
        return RaycastPicker.pickAtom(
            screenX = screenX,
            screenY = screenY,
            viewWidth = surfaceWidth,
            viewHeight = surfaceHeight,
            viewMatrix = viewMatrix,
            projMatrix = projectionMatrix,
            modelMatrix = modelMatrix,
            atoms = mol.atoms,
            centerOffset = centerOffset,
            isSpaceFilling = (renderModel == RenderModel.SPACE_FILLING)
        )
    }

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        // Dark modern background (#10141D)
        GLES20.glClearColor(0.063f, 0.078f, 0.114f, 1.0f)
        GLES20.glEnable(GLES20.GL_DEPTH_TEST)
        GLES20.glDepthFunc(GLES20.GL_LEQUAL)
        GLES20.glEnable(GLES20.GL_CULL_FACE)
        GLES20.glCullFace(GLES20.GL_BACK)

        // Initialize meshes
        sphereMesh = SphereMesh(stacks = 20, slices = 20)
        cylinderMesh = CylinderMesh(segments = 16)

        // Compile lit shader
        val litVertexShader = """
            uniform mat4 u_MVPMatrix;
            uniform mat4 u_MVMatrix;
            uniform vec3 u_LightPos;
            
            attribute vec4 a_Position;
            attribute vec3 a_Normal;
            
            varying vec3 v_Normal;
            varying vec3 v_EyeVec;
            varying vec3 v_LightDir;
            
            void main() {
                vec4 eyePos = u_MVMatrix * a_Position;
                v_Normal = normalize((u_MVMatrix * vec4(a_Normal, 0.0)).xyz);
                v_LightDir = normalize(u_LightPos);
                v_EyeVec = normalize(-eyePos.xyz);
                gl_Position = u_MVPMatrix * a_Position;
            }
        """.trimIndent()

        val litFragmentShader = """
            precision mediump float;
            
            uniform vec4 u_Color;
            uniform float u_Shininess;
            
            varying vec3 v_Normal;
            varying vec3 v_EyeVec;
            varying vec3 v_LightDir;
            
            void main() {
                vec3 N = normalize(v_Normal);
                vec3 L = normalize(v_LightDir);
                vec3 V = normalize(v_EyeVec);
                vec3 H = normalize(L + V);
                
                float diffuse = max(dot(N, L), 0.0);
                float specular = 0.0;
                if (diffuse > 0.0) {
                    specular = pow(max(dot(N, H), 0.0), u_Shininess);
                }
                
                vec3 ambient = vec3(0.30, 0.30, 0.30) * u_Color.rgb;
                vec3 diffColor = diffuse * u_Color.rgb * 0.70;
                vec3 specColor = specular * vec3(0.60, 0.60, 0.60);
                
                gl_FragColor = vec4(ambient + diffColor + specColor, u_Color.a);
            }
        """.trimIndent()

        litProgram = ShaderHelper.createProgram(litVertexShader, litFragmentShader)
        uMVPMatrixHandle = GLES20.glGetUniformLocation(litProgram, "u_MVPMatrix")
        uMVMatrixHandle = GLES20.glGetUniformLocation(litProgram, "u_MVMatrix")
        uLightPosHandle = GLES20.glGetUniformLocation(litProgram, "u_LightPos")
        uColorHandle = GLES20.glGetUniformLocation(litProgram, "u_Color")
        uShininessHandle = GLES20.glGetUniformLocation(litProgram, "u_Shininess")
        aPositionHandle = GLES20.glGetAttribLocation(litProgram, "a_Position")
        aNormalHandle = GLES20.glGetAttribLocation(litProgram, "a_Normal")

        // Compile wireframe shader
        val wireVertexShader = """
            uniform mat4 u_MVPMatrix;
            attribute vec4 a_Position;
            void main() {
                gl_Position = u_MVPMatrix * a_Position;
            }
        """.trimIndent()

        val wireFragmentShader = """
            precision mediump float;
            uniform vec4 u_Color;
            void main() {
                gl_FragColor = u_Color;
            }
        """.trimIndent()

        wireProgram = ShaderHelper.createProgram(wireVertexShader, wireFragmentShader)
        uWireMVPHandle = GLES20.glGetUniformLocation(wireProgram, "u_MVPMatrix")
        uWireColorHandle = GLES20.glGetUniformLocation(wireProgram, "u_Color")
        aWirePositionHandle = GLES20.glGetAttribLocation(wireProgram, "a_Position")
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        surfaceWidth = width
        surfaceHeight = height
        GLES20.glViewport(0, 0, width, height)

        val aspect = width.toFloat() / max(height, 1).toFloat()
        Matrix.perspectiveM(projectionMatrix, 0, 45.0f, aspect, 0.5f, 200.0f)
    }

    override fun onDrawFrame(gl: GL10?) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)

        val mol = molecule ?: return

        // Setup View Matrix
        Matrix.setLookAtM(
            viewMatrix, 0,
            0f, 0f, cameraDistance,
            0f, 0f, 0f,
            0f, 1f, 0f
        )

        // Setup Base Model Matrix (Pan & Rotation)
        Matrix.setIdentityM(modelMatrix, 0)
        Matrix.translateM(modelMatrix, 0, panX, panY, 0f)
        Matrix.rotateM(modelMatrix, 0, rotationAngleX, 1f, 0f, 0f)
        Matrix.rotateM(modelMatrix, 0, rotationAngleY, 0f, 1f, 0f)

        when (renderModel) {
            RenderModel.BALL_AND_STICK -> {
                drawBonds(mol, bondRadius = 0.12f)
                drawAtoms(mol, useVdw = false)
            }
            RenderModel.SPACE_FILLING -> {
                drawAtoms(mol, useVdw = true)
            }
            RenderModel.STICK -> {
                drawBonds(mol, bondRadius = 0.22f)
                drawSmallAtomCaps(mol, capRadius = 0.22f)
            }
            RenderModel.WIREFRAME -> {
                drawWireframe()
                drawSmallAtomCaps(mol, capRadius = 0.15f)
            }
        }

        // Draw selection halo around selected atom if any
        selectedAtom?.let { sel ->
            drawAtomHighlight(sel)
        }

        // Handle screenshot capture if requested
        val cb = pendingScreenshotCallback
        if (cb != null) {
            pendingScreenshotCallback = null
            captureAndDispatchScreenshot(cb)
        }
    }

    private fun drawAtoms(mol: Molecule, useVdw: Boolean) {
        val sphere = sphereMesh ?: return
        val cx = centerOffset[0]
        val cy = centerOffset[1]
        val cz = centerOffset[2]

        GLES20.glUseProgram(litProgram)
        GLES20.glUniform3fv(uLightPosHandle, 1, lightPositionEye, 0)
        GLES20.glUniform1f(uShininessHandle, 32.0f)

        for (atom in mol.atoms) {
            val r = if (useVdw) atom.vdwRadius else atom.ballRadius
            val x = atom.x - cx
            val y = atom.y - cy
            val z = atom.z - cz

            // Combine model transform
            Matrix.multiplyMM(tempMatrix, 0, modelMatrix, 0, createTranslateScaleMatrix(x, y, z, r), 0)
            Matrix.multiplyMM(mvMatrix, 0, viewMatrix, 0, tempMatrix, 0)
            Matrix.multiplyMM(mvpMatrix, 0, projectionMatrix, 0, mvMatrix, 0)

            GLES20.glUniformMatrix4fv(uMVPMatrixHandle, 1, false, mvpMatrix, 0)
            GLES20.glUniformMatrix4fv(uMVMatrixHandle, 1, false, mvMatrix, 0)
            GLES20.glUniform4fv(uColorHandle, 1, atom.colorRgba, 0)

            sphere.draw(aPositionHandle, aNormalHandle)
        }
    }

    private fun drawSmallAtomCaps(mol: Molecule, capRadius: Float) {
        val sphere = sphereMesh ?: return
        val cx = centerOffset[0]
        val cy = centerOffset[1]
        val cz = centerOffset[2]

        GLES20.glUseProgram(litProgram)
        GLES20.glUniform3fv(uLightPosHandle, 1, lightPositionEye, 0)
        GLES20.glUniform1f(uShininessHandle, 20.0f)

        for (atom in mol.atoms) {
            val x = atom.x - cx
            val y = atom.y - cy
            val z = atom.z - cz

            Matrix.multiplyMM(tempMatrix, 0, modelMatrix, 0, createTranslateScaleMatrix(x, y, z, capRadius), 0)
            Matrix.multiplyMM(mvMatrix, 0, viewMatrix, 0, tempMatrix, 0)
            Matrix.multiplyMM(mvpMatrix, 0, projectionMatrix, 0, mvMatrix, 0)

            GLES20.glUniformMatrix4fv(uMVPMatrixHandle, 1, false, mvpMatrix, 0)
            GLES20.glUniformMatrix4fv(uMVMatrixHandle, 1, false, mvMatrix, 0)
            GLES20.glUniform4fv(uColorHandle, 1, atom.colorRgba, 0)

            sphere.draw(aPositionHandle, aNormalHandle)
        }
    }

    private fun drawBonds(mol: Molecule, bondRadius: Float) {
        val cylinder = cylinderMesh ?: return
        val cx = centerOffset[0]
        val cy = centerOffset[1]
        val cz = centerOffset[2]

        GLES20.glUseProgram(litProgram)
        GLES20.glUniform3fv(uLightPosHandle, 1, lightPositionEye, 0)
        GLES20.glUniform1f(uShininessHandle, 16.0f)

        // Default bond color: neutral metallic light gray
        val bondColor = floatArrayOf(0.72f, 0.72f, 0.76f, 1.0f)
        GLES20.glUniform4fv(uColorHandle, 1, bondColor, 0)

        val bondModelMat = FloatArray(16)

        for (bond in mol.bonds) {
            val a1 = atomMap[bond.atom1Id] ?: continue
            val a2 = atomMap[bond.atom2Id] ?: continue

            val p1x = a1.x - cx
            val p1y = a1.y - cy
            val p1z = a1.z - cz

            val p2x = a2.x - cx
            val p2y = a2.y - cy
            val p2z = a2.z - cz

            val dx = p2x - p1x
            val dy = p2y - p1y
            val dz = p2z - p1z
            val length = sqrt(dx * dx + dy * dy + dz * dz)
            if (length < 1e-4f) continue

            // Build cylinder transformation from A to B
            computeCylinderTransform(bondModelMat, p1x, p1y, p1z, dx, dy, dz, length, bondRadius)

            Matrix.multiplyMM(tempMatrix, 0, modelMatrix, 0, bondModelMat, 0)
            Matrix.multiplyMM(mvMatrix, 0, viewMatrix, 0, tempMatrix, 0)
            Matrix.multiplyMM(mvpMatrix, 0, projectionMatrix, 0, mvMatrix, 0)

            GLES20.glUniformMatrix4fv(uMVPMatrixHandle, 1, false, mvpMatrix, 0)
            GLES20.glUniformMatrix4fv(uMVMatrixHandle, 1, false, mvMatrix, 0)

            cylinder.draw(aPositionHandle, aNormalHandle)
        }
    }

    private fun drawWireframe() {
        val buffer = wireframeBuffer ?: return
        if (wireframeVertexCount <= 0) return

        GLES20.glUseProgram(wireProgram)
        Matrix.multiplyMM(mvMatrix, 0, viewMatrix, 0, modelMatrix, 0)
        Matrix.multiplyMM(mvpMatrix, 0, projectionMatrix, 0, mvMatrix, 0)

        GLES20.glUniformMatrix4fv(uWireMVPHandle, 1, false, mvpMatrix, 0)
        val wireColor = floatArrayOf(0.01f, 0.94f, 1.0f, 0.9f) // Cyan wire
        GLES20.glUniform4fv(uWireColorHandle, 1, wireColor, 0)

        GLES20.glLineWidth(3.0f)
        GLES20.glEnableVertexAttribArray(aWirePositionHandle)
        GLES20.glVertexAttribPointer(aWirePositionHandle, 3, GLES20.GL_FLOAT, false, 0, buffer)

        GLES20.glDrawArrays(GLES20.GL_LINES, 0, wireframeVertexCount)

        GLES20.glDisableVertexAttribArray(aWirePositionHandle)
    }

    private fun drawAtomHighlight(atom: Atom) {
        val sphere = sphereMesh ?: return
        val cx = centerOffset[0]
        val cy = centerOffset[1]
        val cz = centerOffset[2]

        val highlightRadius = (if (renderModel == RenderModel.SPACE_FILLING) atom.vdwRadius else atom.ballRadius) * 1.35f
        val x = atom.x - cx
        val y = atom.y - cy
        val z = atom.z - cz

        GLES20.glEnable(GLES20.GL_BLEND)
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE)
        GLES20.glDisable(GLES20.GL_CULL_FACE)

        GLES20.glUseProgram(litProgram)
        Matrix.multiplyMM(tempMatrix, 0, modelMatrix, 0, createTranslateScaleMatrix(x, y, z, highlightRadius), 0)
        Matrix.multiplyMM(mvMatrix, 0, viewMatrix, 0, tempMatrix, 0)
        Matrix.multiplyMM(mvpMatrix, 0, projectionMatrix, 0, mvMatrix, 0)

        GLES20.glUniformMatrix4fv(uMVPMatrixHandle, 1, false, mvpMatrix, 0)
        GLES20.glUniformMatrix4fv(uMVMatrixHandle, 1, false, mvMatrix, 0)
        GLES20.glUniform3fv(uLightPosHandle, 1, lightPositionEye, 0)
        GLES20.glUniform1f(uShininessHandle, 64.0f)

        // Pulsing / glowing cyan highlight
        val glowColor = floatArrayOf(0.01f, 0.94f, 1.0f, 0.45f)
        GLES20.glUniform4fv(uColorHandle, 1, glowColor, 0)

        sphere.draw(aPositionHandle, aNormalHandle)

        GLES20.glDisable(GLES20.GL_BLEND)
        GLES20.glEnable(GLES20.GL_CULL_FACE)
    }

    private fun createTranslateScaleMatrix(x: Float, y: Float, z: Float, s: Float): FloatArray {
        val mat = FloatArray(16)
        Matrix.setIdentityM(mat, 0)
        Matrix.translateM(mat, 0, x, y, z)
        Matrix.scaleM(mat, 0, s, s, s)
        return mat
    }

    private fun computeCylinderTransform(
        result: FloatArray,
        p1x: Float, p1y: Float, p1z: Float,
        dx: Float, dy: Float, dz: Float,
        length: Float, radius: Float
    ) {
        Matrix.setIdentityM(result, 0)
        Matrix.translateM(result, 0, p1x, p1y, p1z)

        // Direction unit vector
        val vx = dx / length
        val vy = dy / length
        val vz = dz / length

        // Default cylinder is along Z-axis (0, 0, 1)
        val axisX = -vy
        val axisY = vx
        val axisLen = sqrt(axisX * axisX + axisY * axisY)

        val dot = vz.coerceIn(-1.0f, 1.0f)
        val angleDeg = acos(dot) * (180.0f / Math.PI.toFloat())

        if (axisLen > 1e-5f) {
            Matrix.rotateM(result, 0, angleDeg, axisX / axisLen, axisY / axisLen, 0.0f)
        } else if (vz < 0.0f) {
            Matrix.rotateM(result, 0, 180.0f, 1.0f, 0.0f, 0.0f)
        }

        Matrix.scaleM(result, 0, radius, radius, length)
    }

    private fun captureAndDispatchScreenshot(callback: (Bitmap) -> Unit) {
        val w = surfaceWidth
        val h = surfaceHeight
        if (w <= 0 || h <= 0) return

        val buffer = ByteBuffer.allocateDirect(w * h * 4).order(ByteOrder.nativeOrder())
        GLES20.glReadPixels(0, 0, w, h, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, buffer)

        val bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val pixelArray = IntArray(w * h)
        buffer.asIntBuffer().get(pixelArray)

        // In OpenGL, pixel rows are read bottom-to-top and colors are in RGBA byte order
        val flippedPixels = IntArray(w * h)
        for (row in 0 until h) {
            val srcRow = (h - 1 - row) * w
            val dstRow = row * w
            for (col in 0 until w) {
                val pixel = pixelArray[srcRow + col]
                // RGBA byte order in IntBuffer -> ARGB
                val r = (pixel and 0xFF)
                val g = (pixel shr 8) and 0xFF
                val b = (pixel shr 16) and 0xFF
                val a = (pixel shr 24) and 0xFF
                flippedPixels[dstRow + col] = (a shl 24) or (r shl 16) or (g shl 8) or b
            }
        }
        bitmap.setPixels(flippedPixels, 0, w, 0, 0, w, h)

        Handler(Looper.getMainLooper()).post {
            callback(bitmap)
        }
    }
}
