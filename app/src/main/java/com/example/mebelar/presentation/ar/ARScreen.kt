package com.example.mebelar.presentation.ar

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.os.Build
import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import android.provider.MediaStore
import android.util.Log
import android.view.MotionEvent
import android.view.PixelCopy
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.navigation.NavController
import com.google.android.filament.Engine
import com.google.ar.core.Anchor
import com.google.ar.core.Config
import com.google.ar.core.Frame
import com.google.ar.core.TrackingFailureReason
import io.github.sceneview.ar.ARScene
import io.github.sceneview.ar.arcore.createAnchorOrNull
import io.github.sceneview.ar.arcore.isValid
import io.github.sceneview.ar.getDescription
import io.github.sceneview.ar.node.AnchorNode
import io.github.sceneview.ar.rememberARCameraNode
import io.github.sceneview.loaders.MaterialLoader
import io.github.sceneview.loaders.ModelLoader
import io.github.sceneview.node.CubeNode
import io.github.sceneview.node.ModelNode
import io.github.sceneview.node.Node
import io.github.sceneview.rememberCollisionSystem
import io.github.sceneview.rememberEngine
import io.github.sceneview.rememberMaterialLoader
import io.github.sceneview.rememberModelLoader
import io.github.sceneview.rememberNodes
import io.github.sceneview.rememberOnGestureListener
import io.github.sceneview.rememberView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.nio.ByteBuffer
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.core.content.ContextCompat
import android.content.pm.PackageManager
import android.Manifest

@Composable
fun ARScreenInitializer(url: String, navController: NavController, modifier: Modifier) {
    val context = LocalContext.current
    val loadingState = remember { mutableStateOf(false) }
    val modelUrl = remember { mutableStateOf(url) }

    ARScreen(modelUrl.value, navController, modifier)
}

@Composable
fun ARScreen(modelUrl: String, navController: NavController, modifier: Modifier) {
    Box(modifier = modifier.fillMaxSize()) {
        val context = LocalContext.current
        val engine = rememberEngine()
        val modelLoader = rememberModelLoader(engine)
        val materialLoader = rememberModelLoader(engine)
        val cameraNode = rememberARCameraNode(engine)
        val childNodes = rememberNodes()
        val view = rememberView(engine)
        val collisionSystem = rememberCollisionSystem(view)

        val selectedAnchorNode = remember { mutableStateOf<AnchorNode?>(null) }
        val isFirstModelAdded = remember { mutableStateOf(false) }
        val isDuplicateModeEnabled = remember { mutableStateOf(false) }
        val currentModelBuffer = remember { mutableStateOf<ByteBuffer?>(null) }
        var planeRenderer by remember { mutableStateOf(true) }
        var trackingFailureReason by remember { mutableStateOf<TrackingFailureReason?>(null) }
        var frame by remember { mutableStateOf<Frame?>(null) }
        var isUiVisible by remember { mutableStateOf(true) }
        var sceneViewRef by remember { mutableStateOf<io.github.sceneview.SceneView?>(null) }
        var capturedBitmap by remember { mutableStateOf<Bitmap?>(null) }

        ARScene(
            modifier = Modifier
                .fillMaxSize()
                .onGloballyPositioned { coordinates ->
                    coordinates.parentLayoutCoordinates?.let { parent ->
                        val view = parent as? io.github.sceneview.SceneView
                        sceneViewRef = view
                        Log.d("ARScreen", "sceneViewRef initialized: ${view != null}")
                    }
                },
            childNodes = childNodes,
            engine = engine,
            view = view,
            modelLoader = modelLoader,
            collisionSystem = collisionSystem,
            sessionConfiguration = { session, config ->
                config.depthMode =
                    if (session.isDepthModeSupported(Config.DepthMode.AUTOMATIC))
                        Config.DepthMode.AUTOMATIC
                    else Config.DepthMode.DISABLED
                config.instantPlacementMode = Config.InstantPlacementMode.LOCAL_Y_UP
                config.lightEstimationMode = Config.LightEstimationMode.ENVIRONMENTAL_HDR
            },
            cameraNode = cameraNode,
            planeRenderer = planeRenderer,
            onTrackingFailureChanged = { trackingFailureReason = it },
            onSessionUpdated = { _, updatedFrame ->
                frame = updatedFrame
                Log.d("ARScreen", "Frame updated: ${updatedFrame.timestamp}")
            },
            onGestureListener = rememberOnGestureListener(
                onSingleTapConfirmed = { motionEvent: MotionEvent, node: Node? ->
                    if (node != null) {
                        selectedAnchorNode.value = node.findAnchorNode()
                        true
                    } else {
                        val canAddModel = !isFirstModelAdded.value || isDuplicateModeEnabled.value
                        if (canAddModel) {
                            val hitResults = frame?.hitTest(motionEvent.x, motionEvent.y)
                            hitResults?.firstOrNull {
                                it.isValid(depthPoint = false, point = false)
                            }?.createAnchorOrNull()?.let { anchor ->
                                planeRenderer = false
                                childNodes += createAnchorNode(
                                    engine = engine,
                                    modelLoader = modelLoader,
                                    anchor = anchor,
                                    modelUrl = modelUrl,
                                    context = context,
                                    isDuplicate = isDuplicateModeEnabled.value,
                                    currentModelBuffer = currentModelBuffer
                                )
                                isFirstModelAdded.value = true
                                isDuplicateModeEnabled.value = false
                            }
                        }
                        selectedAnchorNode.value = null
                        false
                    }
                }
            )
        )

        // Отображение захваченного скриншота (для теста)
        if (capturedBitmap != null) {
            Image(
                bitmap = capturedBitmap!!.asImageBitmap(),
                contentDescription = "Captured screenshot",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .align(Alignment.TopCenter)
                    .zIndex(2f)
            )
        }

        // Текст ошибки отслеживания
        if (isUiVisible) {
            Text(
                modifier = Modifier
                    .systemBarsPadding()
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .padding(top = 16.dp, start = 32.dp, end = 32.dp)
                    .zIndex(1f),
                textAlign = TextAlign.Center,
                fontSize = 28.sp,
                color = Color.White,
                text = trackingFailureReason?.getDescription(LocalContext.current) ?: ""
            )
        }

        // Кнопка возврата (слева сверху)
        if (isUiVisible) {
            IconButton(
                onClick = { navController.navigateUp() },
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(16.dp)
                    .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(8.dp))
                    .zIndex(1f)
            ) {
                Icon(
                    Icons.Default.ArrowBack,
                    contentDescription = "Назад",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        // Кнопка переключения отображения поверхностей (справа сверху)
        if (isUiVisible) {
            IconButton(
                onClick = { planeRenderer = !planeRenderer },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
                    .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(8.dp))
                    .zIndex(1f)
            ) {
                Icon(
                    if (planeRenderer) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                    contentDescription = if (planeRenderer) "Скрыть поверхности" else "Показать поверхности",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        // Панель управления (внизу, выше нижней навигации)
        if (isUiVisible && selectedAnchorNode.value != null) {
            ControlPanel(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 80.dp)
                    .zIndex(1f),
                onRotateLeft = {
                    selectedAnchorNode.value?.getFirstModelNode()?.applyAccumulatedRotationY(-90f)
                },
                onRotateRight = {
                    selectedAnchorNode.value?.getFirstModelNode()?.applyAccumulatedRotationY(90f)
                },
                onDuplicate = {
                    isDuplicateModeEnabled.value = true
                    Log.d("ARScreen", "Duplicate mode enabled")
                },
                onDelete = {
                    val anchorNode = selectedAnchorNode.value?.findAnchorNode()
                    if (anchorNode != null) {
                        childNodes -= anchorNode
                        selectedAnchorNode.value = null
                        isDuplicateModeEnabled.value = false
                        if (childNodes.isEmpty()) {
                            isFirstModelAdded.value = false
                            currentModelBuffer.value = null
                        }
                    }
                }
            )
        }

        // Кнопка скриншота (в центре снизу)
        if (isUiVisible) {
            IconButton(
                onClick = {
                    isUiVisible = false
                    CoroutineScope(Dispatchers.Main).launch {
                        kotlinx.coroutines.delay(1000) // Увеличенная задержка для рендеринга
                        if (sceneViewRef == null || !sceneViewRef!!.isAttachedToWindow) {
                            Log.e("ARScreen", "SceneView is not attached or initialized")
                            isUiVisible = true
                            return@launch
                        }
                        Log.d("ARScreen", "Attempting to capture screenshot")
                        captureSceneView(sceneViewRef!!, context) { bitmap: Bitmap? ->
                            bitmap?.let {
                                capturedBitmap = it // Отобразить для проверки
                                saveBitmapToGallery(context, it)
                                Log.d("ARScreen", "Capture successful: ${bitmap.width}x${bitmap.height}")
                            } ?: Log.e("ARScreen", "Failed to capture bitmap")
                            isUiVisible = true
                        }
                    }
                },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 48.dp)
                    .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(8.dp))
                    .zIndex(1f)
            ) {
                Icon(
                    Icons.Default.Camera,
                    contentDescription = "Сделать скриншот",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

private fun captureSceneView(
    sceneView: io.github.sceneview.SceneView,
    context: Context,
    onResult: (Bitmap?) -> Unit
) {
    Log.d("ARScreen", "Starting captureSceneView")
    val width = sceneView.width
    val height = sceneView.height
    Log.d("ARScreen", "SceneView dimensions: $width x $height")
    if (width <= 0 || height <= 0) {
        Log.e("ARScreen", "Invalid SceneView dimensions: $width x $height")
        onResult(null)
        return
    }

    val surfaceView = sceneView as? android.view.SurfaceView
    if (surfaceView == null) {
        Log.e("ARScreen", "SceneView is not a SurfaceView")
        onResult(null)
        return
    }

    if (!surfaceView.holder.surface.isValid) {
        Log.e("ARScreen", "Surface is not valid")
        onResult(null)
        return
    }

    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val handlerThread = HandlerThread("PixelCopier")
    handlerThread.start()

    try {
        PixelCopy.request(surfaceView, bitmap, { copyResult ->
            Log.d("ARScreen", "PixelCopy result: $copyResult")
            when (copyResult) {
                PixelCopy.SUCCESS -> {
                    Log.d("ARScreen", "PixelCopy successful")
                    onResult(bitmap)
                }
                else -> {
                    Log.e("ARScreen", "PixelCopy failed with code: $copyResult")
                    onResult(null)
                }
            }
            handlerThread.quitSafely()
        }, Handler(handlerThread.looper))
    } catch (e: Exception) {
        Log.e("ARScreen", "PixelCopy exception: ${e.message}", e)
        onResult(null)
        handlerThread.quitSafely()
    }
}

// Сохранение скриншота в галерею
private fun saveBitmapToGallery(context: Context, bitmap: Bitmap) {
    Log.d("ARScreen", "Starting saveBitmapToGallery with bitmap: ${bitmap.width}x${bitmap.height}")

    // Проверка разрешения на запись для Android 9 и ниже
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            Log.e("ARScreen", "Storage permission not granted")
            return
        }
    }

    val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
    val displayName = "AR_Screenshot_$timeStamp.jpg"

    val contentValues = ContentValues().apply {
        put(MediaStore.MediaColumns.DISPLAY_NAME, displayName)
        put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            put(MediaStore.MediaColumns.RELATIVE_PATH, "Pictures/AR_Screenshots")
            put(MediaStore.MediaColumns.IS_PENDING, 1)
        }
    }

    val resolver = context.contentResolver
    val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
    if (uri == null) {
        Log.e("ARScreen", "Failed to insert into MediaStore: uri is null")
        return
    }

    try {
        resolver.openOutputStream(uri)?.use { outputStream ->
            val success = bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
            Log.d("ARScreen", "Bitmap compress result: $success")
        } ?: run {
            Log.e("ARScreen", "Failed to open OutputStream")
            return
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            contentValues.clear()
            contentValues.put(MediaStore.MediaColumns.IS_PENDING, 0)
            resolver.update(uri, contentValues, null, null)
        }
        Log.d("ARScreen", "Screenshot saved to gallery: $displayName")
    } catch (e: Exception) {
        Log.e("ARScreen", "Error saving screenshot: ${e.message}", e)
    }
}

@Composable
fun ControlPanel(
    modifier: Modifier = Modifier,
    onRotateLeft: () -> Unit,
    onRotateRight: () -> Unit,
    onDuplicate: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onRotateLeft) {
                Icon(Icons.Default.RotateLeft, contentDescription = "Повернуть влево")
            }
            IconButton(onClick = onRotateRight) {
                Icon(Icons.Default.RotateRight, contentDescription = "Повернуть вправо")
            }
            IconButton(onClick = onDuplicate) {
                Icon(Icons.Default.ContentCopy, contentDescription = "Дублировать")
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Удалить")
            }
        }
    }
}

fun Node.getFirstModelNode(): ModelNode? =
    this.childNodes.filterIsInstance<ModelNode>().firstOrNull()

fun Node.findAnchorNode(): AnchorNode? {
    var current: Node? = this
    while (current != null && current !is AnchorNode) {
        current = current.parent
    }
    return current as? AnchorNode
}

fun ModelNode.applyAccumulatedRotationY(deltaDegrees: Float) {
    val newRotation = this.rotation.copy(
        y = this.rotation.y + deltaDegrees
    )
    this.rotation = newRotation
}

fun createAnchorNode(
    engine: Engine,
    modelLoader: ModelLoader,
    anchor: Anchor,
    modelUrl: String,
    context: Context,
    isDuplicate: Boolean,
    currentModelBuffer: MutableState<ByteBuffer?>
): AnchorNode {
    val anchorNode = AnchorNode(engine = engine, anchor = anchor)

    Log.d("ARScreen", "Attempting to load model from URL: $modelUrl, isDuplicate: $isDuplicate")

    CoroutineScope(Dispatchers.IO).launch {
        try {
            val buffer = if (isDuplicate && currentModelBuffer.value != null) {
                currentModelBuffer.value!!.rewind()
            } else {
                val connection = URL(modelUrl).openConnection() as HttpURLConnection
                connection.requestMethod = "HEAD"
                val responseCode = connection.responseCode
                if (responseCode !in 200..299) {
                    withContext(Dispatchers.Main) {
                        Log.e("ARScreen", "URL is not accessible. HTTP response code: $responseCode")
                    }
                    return@launch
                }
                withContext(Dispatchers.Main) {
                    Log.d("ARScreen", "URL is accessible: $modelUrl")
                }

                val byteArrayOutputStream = ByteArrayOutputStream()
                val inputStream = URL(modelUrl).openStream()
                inputStream.copyTo(byteArrayOutputStream)
                inputStream.close()
                val byteArray = byteArrayOutputStream.toByteArray()
                ByteBuffer.allocateDirect(byteArray.size).put(byteArray).rewind()
            }

            withContext(Dispatchers.Main) {
                try {
                    val modelInstance = modelLoader.createModelInstance(buffer)
                    if (modelInstance == null) {
                        Log.e("ARScreen", "Model instance is null for URL: $modelUrl")
                        return@withContext
                    }

                    Log.d("ARScreen", "Model instance created successfully from URL: $modelUrl")

                    if (!isDuplicate) {
                        currentModelBuffer.value = buffer as ByteBuffer?
                    }

                    val modelNode = ModelNode(modelInstance = modelInstance).apply {
                        isEditable = true
                        editableScaleRange = 1f..1f
                    }

                    val boundingBoxNode = CubeNode(
                        engine,
                        size = modelNode.extents,
                        center = modelNode.center,
                    ).apply {
                        isVisible = false
                    }

                    modelNode.addChildNode(boundingBoxNode)
                    anchorNode.addChildNode(modelNode)

                    listOf(modelNode, anchorNode).forEach {
                        it.onEditingChanged = { editingTransforms ->
                            boundingBoxNode.isVisible = editingTransforms.isNotEmpty()
                        }
                    }
                } catch (e: Exception) {
                    Log.e("ARScreen", "Error creating model instance from $modelUrl: ${e.message}", e)
                }
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                Log.e("ARScreen", "Error downloading or processing model from $modelUrl: ${e.message}", e)
            }
        }
    }

    return anchorNode
}