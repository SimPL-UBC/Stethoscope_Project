import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.background
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.ApplicationScope
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

@Composable
@Preview
fun App() {
    var screen by remember { mutableStateOf("live") }

    Scaffold(
        bottomBar = {
            BottomAppBar(
                backgroundColor = Color.Gray
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    Button(onClick = { screen = "live" }) {
                        Text("Live")
                    }
                    Button(onClick = { screen = "analyze" }) {
                        Text("Analyze")
                    }
                }
            }
        }
    ) {
        when (screen) {
            "live" -> LiveScreen()
            "analyze" -> AnalyzeScreen()
        }
    }
}

@Composable
fun LiveScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.LightGray),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxHeight(0.75f)
        ) {
            Button(onClick = { /* TODO: Pair action */ }) {
                Text("Pair")
            }
            Spacer(modifier = Modifier.weight(1f))
            Button(onClick = { /* TODO: Start Live Recording action */ }) {
                Text("Start Live Recording")
            }
        }
    }
}

@Composable
fun AnalyzeScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.LightGray),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Button(onClick = { /* TODO: Import action */ }) {
                Text("Import")
            }
            Spacer(modifier = Modifier.height(20.dp))
            Button(onClick = { /* TODO: Split action */ }) {
                Text("Split")
            }
        }
    }
}

fun main() = application {
    Window(onCloseRequest = ::exitApplication, title = "Kotlin GUI App") {
        App()
    }
}
