import android.annotation.SuppressLint
import android.os.Bundle
import android.view.MotionEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController
import com.example.mebelar.presentation.activity.AppNavigation
import com.example.mebelar.presentation.activity.BottomNavigationBar
import com.example.mebelar.presentation.activity.TopBarNavigation
import com.example.mebelar.ui.theme.MebelARTheme
import io.github.sceneview.ar.ARScene
import io.github.sceneview.ar.arcore.ARSession
import io.github.sceneview.ar.node.AnchorNode
import io.github.sceneview.math.Position
import io.github.sceneview.math.Scale
import io.github.sceneview.node.ModelNode
import io.github.sceneview.node.Node
import io.github.sceneview.rememberEngine
import io.github.sceneview.rememberModelLoader

class AR : ComponentActivity() {
    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MebelARTheme {
                ARScreen()
            }
        }
    }
}

@Composable
fun ARScreen() {
    val isRayVisible = remember { mutableStateOf(true) }
    val engine = rememberEngine()
    val modelLoader = rememberModelLoader(engine)

    // Модель основного объекта
    val modelInstance = remember {
        modelLoader.createModelInstance("models/your_model.glb")
    }

    // Модель для указателя (например, маленький куб)
    val rayModelInstance = remember {
        modelLoader.createModelInstance("models/cube.glb") // Нужна модель куба
    }

    Box(modifier = Modifier.fillMaxSize()) {
        ARScene(
            modifier = Modifier.fillMaxSize(),
            engine = engine,
            modelLoader = modelLoader,
            planeRenderer = true,
            onCreate = { arSceneView ->
                // Инициализация при создании сцены
            },


        )
    }
}