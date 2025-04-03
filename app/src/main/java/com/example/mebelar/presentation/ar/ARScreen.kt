package com.example.mebelar.presentation.ar

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.platform.LocalContext
import com.example.mebelar.ui.theme.MebelARTheme
import com.google.ar.core.Config
import com.google.ar.core.Plane
import com.google.ar.core.Pose
import com.google.ar.core.TrackingFailureReason
import com.google.ar.core.TrackingState
import dev.romainguy.kotlin.math.Float3
import io.github.sceneview.ar.ARScene
import io.github.sceneview.ar.node.AnchorNode
import io.github.sceneview.ar.rememberARCameraNode
import io.github.sceneview.node.ModelNode
import io.github.sceneview.rememberCollisionSystem
import io.github.sceneview.rememberEngine
import io.github.sceneview.rememberModelLoader
import io.github.sceneview.rememberNodes
import io.github.sceneview.rememberView

private const val kModelFile = "cursor.glb"
private const val kModelsFile = "cursor.glb"


class AR : ComponentActivity() {

    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MebelARTheme {
                ARScreen(kModelFile, kModelsFile)
            }
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun ARScreen(modelUrl: String, placedModelUrl: String) { // Добавляем URL для второй модели
    val context = LocalContext.current
    val displayMetrics = context.resources.displayMetrics
    val screenWidth = displayMetrics.widthPixels
    val screenHeight = displayMetrics.heightPixels

    val screenCenterX = (screenWidth / 2).toFloat()
    val screenCenterY = (screenHeight / 2).toFloat()

    Box(modifier = Modifier.fillMaxSize()) {
        val engine = rememberEngine()
        val modelLoader = rememberModelLoader(engine)
        val cameraNode = rememberARCameraNode(engine)
        val childNodes = rememberNodes()
        val view = rememberView(engine)
        val collisionSystem = rememberCollisionSystem(view)

        val trackingFailureReason = remember { mutableStateOf<TrackingFailureReason?>(null) }
        val modelNode = remember { mutableStateOf<ModelNode?>(null) } // Курсор
        val placedModelNode = remember { mutableStateOf<ModelNode?>(null) } // Модель по нажатию
        val isPlaced = remember { mutableStateOf<Boolean>(false) } // Флаг размещения
        var session = remember { mutableStateOf<com.google.ar.core.Session?>(null) }

        LaunchedEffect(modelUrl, placedModelUrl) { // Зависимости от modelUrl и placedModelUrl
            // Загружаем модель курсора
            modelLoader.loadModelInstance(modelUrl)?.let { modelInstance ->
                if (modelNode.value == null) { // Проверяем, не загружена ли уже модель
                    modelNode.value = ModelNode(
                        modelInstance = modelInstance,
                        autoAnimate = true,
                        scaleToUnits = 1.0f
                    ).apply {
                        isVisible = true // Курсор виден изначально
                    }
                    childNodes.add(modelNode.value!!) // Добавляем модель курсора в сцену
                }
            } ?: run {
                Log.e("ModelLoader", "Failed to load cursor model: $modelUrl") // Обработка ошибки загрузки
            }

            // Загружаем модель размещённого объекта
            modelLoader.loadModelInstance(placedModelUrl)?.let { modelInstance ->
                if (placedModelNode.value == null) { // Проверяем, не загружена ли уже модель
                    placedModelNode.value = ModelNode(
                        modelInstance = modelInstance,
                        autoAnimate = true,
                        scaleToUnits = 1.0f
                    ).apply {
                        isVisible = false // Изначально скрыта
                    }
                    childNodes.add(placedModelNode.value!!) // Добавляем модель размещённого объекта в сцену
                }
            } ?: run {
                Log.e("ModelLoader", "Failed to load placed model: $placedModelUrl") // Обработка ошибки загрузки
            }
        }


        ARScene(
            modifier = Modifier
                .fillMaxSize()
                .pointerInteropFilter { motionEvent ->
                    if (!isPlaced.value && motionEvent.action == MotionEvent.ACTION_DOWN) {
                        modelNode.value?.let { cursor ->
                            val cursorPosition = cursor.worldPosition
                            session.let { session ->
                                placedModelNode.value?.let { placedModel ->
                                    // Создаем Pose на основе текущей позиции курсора
                                    val pose = Pose(
                                        floatArrayOf(cursorPosition.x, cursorPosition.y, cursorPosition.z),
                                        floatArrayOf(0f, 0f, 0f, 1f) // Без вращения
                                    )
                                    val anchor = session.value!!.createAnchor(pose)
                                    val anchorNode = AnchorNode(engine, anchor).apply {
                                        childNodes.add(this) // Добавляем anchorNode в сцену
                                        placedModel.parent = this // Привязываем модель к anchorNode
                                        placedModel.worldPosition = cursorPosition // Устанавливаем позицию курсора
                                        placedModel.isVisible = true
                                    }

                                    // Скрываем курсор
                                    cursor.isVisible = false
                                    isPlaced.value = true
                                }
                            }
                            true // Событие обработано
                        } ?: false
                    } else {
                        false // Пропускаем событие
                    }
                },
            childNodes = childNodes,
            engine = engine,
            view = view,
            modelLoader = modelLoader,
            collisionSystem = collisionSystem,
            sessionConfiguration = { session, config ->
                config.depthMode = if (session.isDepthModeSupported(Config.DepthMode.AUTOMATIC)) {
                    Config.DepthMode.AUTOMATIC
                } else {
                    Config.DepthMode.DISABLED
                }
                config.instantPlacementMode = Config.InstantPlacementMode.LOCAL_Y_UP
                config.lightEstimationMode = Config.LightEstimationMode.ENVIRONMENTAL_HDR
            },
            cameraNode = cameraNode,
            onSessionUpdated = { session, updatedFrame ->
                // Перемещаем курсор только если модель еще не размещена
                if (!isPlaced.value) {
                    val hitResultList = updatedFrame.hitTest(screenCenterX, screenCenterY)

                    val hitResult = hitResultList.firstOrNull { hit ->
                        hit.trackable is Plane && (hit.trackable as Plane).type == Plane.Type.HORIZONTAL_UPWARD_FACING
                    }

                    hitResult?.let { hit ->
                        val plane = hit.trackable as Plane
                        if (plane.trackingState == TrackingState.TRACKING) {
                            val hitPose = hit.hitPose
                            modelNode.value?.apply {
                                val currentPosition = worldPosition
                                val targetPosition = Float3(hitPose.tx(), hitPose.ty(), hitPose.tz())
                                worldPosition = Float3(
                                    currentPosition.x + (targetPosition.x - currentPosition.x) * 0.1f,
                                    currentPosition.y + (targetPosition.y - currentPosition.y) * 0.1f,
                                    currentPosition.z + (targetPosition.z - currentPosition.z) * 0.1f
                                )
                            }
                        }
                    }
                }
            },
        )
    }
}

// Класс для результата пересечения
data class IntersectionResult(
    val point: FloatArray, // Точка пересечения в мировых координатах
    val distance: Float    // Расстояние от начала луча до точки пересечения
)

// Класс для луча
data class Ray(
    val origin: FloatArray,    // Начальная точка луча (x, y, z)
    val direction: FloatArray  // Направление луча (x, y, z), нормализованное
)

// Метод для выполнения теста пересечения луча с плоскостью
fun Plane.hitTest(ray: Ray): IntersectionResult? {
    // Получаем нормаль плоскости из centerPose (предполагаем, что это кватернион и нормаль вдоль Y)
    val planeNormal = floatArrayOf(0f, 1f, 0f) // Горизонтальная плоскость (нормаль вверх)
    val planePoint = floatArrayOf(
        centerPose.tx(),
        centerPose.ty(),
        centerPose.tz()
    ) // Точка на плоскости (центр)

    // Скалярное произведение нормали плоскости и направления луча
    val denominator = planeNormal[0] * ray.direction[0] +
            planeNormal[1] * ray.direction[1] +
            planeNormal[2] * ray.direction[2]

    // Если знаменатель близок к 0, луч параллелен плоскости — пересечения нет
    if (kotlin.math.abs(denominator) < 0.0001f) {
        return null
    }

    // Вектор от начала луча до точки на плоскости
    val w = floatArrayOf(
        planePoint[0] - ray.origin[0],
        planePoint[1] - ray.origin[1],
        planePoint[2] - ray.origin[2]
    )

    // Скалярное произведение w и нормали
    val numerator = w[0] * planeNormal[0] +
            w[1] * planeNormal[1] +
            w[2] * planeNormal[2]

    // Расстояние вдоль луча до точки пересечения
    val t = numerator / denominator

    // Если t < 0, пересечение находится позади начала луча — игнорируем
    if (t < 0f) {
        return null
    }

    // Вычисляем точку пересечения
    val intersectionPoint = floatArrayOf(
        ray.origin[0] + t * ray.direction[0],
        ray.origin[1] + t * ray.direction[1],
        ray.origin[2] + t * ray.direction[2]
    )

    // Проверяем, находится ли точка внутри границ плоскости (если плоскость конечная)
    if (this.type == Plane.Type.HORIZONTAL_UPWARD_FACING) {
        val extentX = this.extentX // Ширина плоскости по X
        val extentZ = this.extentZ // Ширина плоскости по Z
        val dx = intersectionPoint[0] - planePoint[0]
        val dz = intersectionPoint[2] - planePoint[2]
        if (kotlin.math.abs(dx) > extentX / 2 || kotlin.math.abs(dz) > extentZ / 2) {
            return null // Точка вне границ плоскости
        }
    }

    // Возвращаем результат
    return IntersectionResult(
        point = intersectionPoint,
        distance = t
    )
}