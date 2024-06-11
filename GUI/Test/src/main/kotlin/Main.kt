import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import javax.sound.sampled.AudioInputStream
import javax.sound.sampled.AudioSystem
import javax.sound.sampled.Clip
import javax.swing.JFileChooser

@OptIn(ExperimentalAnimationApi::class)
@Composable
@Preview
fun App() {
    var screen by remember { mutableStateOf("live") }
    var importedFile by remember { mutableStateOf<File?>(null) }
    val scope = rememberCoroutineScope()

    Scaffold(
        bottomBar = {
            BottomAppBar(
                backgroundColor = Color.Gray,
                elevation = 8.dp,
                modifier = Modifier.clip(RoundedCornerShape(12.dp))
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    TabButton("Live", screen == "live") { screen = "live" }
                    TabButton("Analyze", screen == "analyze") { screen = "analyze" }
                }
            }
        }
    ) {
        AnimatedContent(
            targetState = screen,
            transitionSpec = {
                fadeIn(animationSpec = tween(300)) with fadeOut(animationSpec = tween(300))
            }
        ) { targetScreen ->
            when (targetScreen) {
                "live" -> LiveScreen()
                "analyze" -> AnalyzeScreen(importedFile) { file ->
                    scope.launch(Dispatchers.IO) {
                        importedFile = file
                    }
                }
            }
        }
    }
}

@Composable
fun TabButton(text: String, isSelected: Boolean, onClick: () -> Unit) {
    val transition = updateTransition(targetState = isSelected, label = "Tab Transition")
    val backgroundColor by transition.animateColor(label = "Background Color Transition") {
        if (it) Color.DarkGray else Color.Gray
    }
    val textColor by transition.animateColor(label = "Text Color Transition") {
        if (it) Color.White else Color.LightGray
    }
    val fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal

    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(backgroundColor = backgroundColor),
        shape = RoundedCornerShape(12.dp)
    ) {
        Text(text, color = textColor, fontWeight = fontWeight, fontSize = 16.sp)
    }
}

@Composable
fun LiveScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFEFEFEF)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxHeight(0.75f)
        ) {
            Button(onClick = { /* TODO: Pair action */ }, shape = RoundedCornerShape(12.dp)) {
                Text("Pair")
            }
            Spacer(modifier = Modifier.weight(1f))
            Button(onClick = { /* TODO: Start Live Recording action */ }, shape = RoundedCornerShape(12.dp)) {
                Text("Start Live Recording")
            }
        }
    }
}

@Composable
fun AnalyzeScreen(importedFile: File?, onFileImported: (File) -> Unit) {
    var isPlaying by remember { mutableStateOf(false) }
    var playButtonLabel by remember { mutableStateOf("Play") }
    var clip: Clip? = remember { null }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFEFEFEF))
    ) {
        if (importedFile != null) {
            Column(
                modifier = Modifier.align(Alignment.TopStart).padding(16.dp),
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.Top
            ) {
                Text("Imported File: ${importedFile.name}", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))
                AudioWaveform(importedFile)
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = {
                    if (isPlaying) {
                        clip?.stop()
                        playButtonLabel = "Play"
                    } else {
                        clip = playAudio(importedFile)
                        playButtonLabel = "Stop"
                    }
                    isPlaying = !isPlaying
                }, shape = RoundedCornerShape(12.dp)) {
                    Text(playButtonLabel)
                }
            }

            Box(
                modifier = Modifier.align(Alignment.TopEnd).padding(16.dp)
            ) {
                Button(onClick = {
                    val file = chooseFile()
                    if (file != null) {
                        onFileImported.invoke(file)
                    }
                }, shape = RoundedCornerShape(12.dp)) {
                    Text("Import")
                }
            }

            Box(
                modifier = Modifier.align(Alignment.BottomStart).padding(16.dp)
            ) {
                Button(onClick = { /* TODO: Split action */ }, shape = RoundedCornerShape(12.dp)) {
                    Text("Split")
                }
            }
        } else {
            Column(
                modifier = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Button(onClick = {
                    val file = chooseFile()
                    if (file != null) {
                        onFileImported.invoke(file)
                    }
                }, shape = RoundedCornerShape(12.dp)) {
                    Text("Import")
                }
            }
        }
    }
}

@Composable
fun AudioWaveform(file: File) {
    val waveform by remember { mutableStateOf(loadWaveform(file)) }

    Canvas(modifier = Modifier.fillMaxWidth().height(200.dp).padding(16.dp).clip(RoundedCornerShape(12.dp)).background(Color.White)) {
        val midY = size.height / 2
        val maxAmplitude = waveform.maxOrNull() ?: 1f

        for (i in waveform.indices) {
            val x = i * size.width / waveform.size
            val y = (waveform[i] / maxAmplitude) * midY

            drawLine(
                color = Color.Blue,
                start = androidx.compose.ui.geometry.Offset(x, midY - y),
                end = androidx.compose.ui.geometry.Offset(x, midY + y),
                strokeWidth = 1f
            )
        }
    }
}

fun loadWaveform(file: File): List<Float> {
    val audioInputStream = AudioSystem.getAudioInputStream(file)
    val bytes = audioInputStream.readBytes()
    val floats = bytes.map { it.toFloat() / 128f }
    return floats.chunked(1024).map { chunk -> chunk.average().toFloat() }
}

fun chooseFile(): File? {
    val fileChooser = JFileChooser()
    val result = fileChooser.showOpenDialog(null)
    return if (result == JFileChooser.APPROVE_OPTION) {
        fileChooser.selectedFile
    } else {
        null
    }
}

fun playAudio(file: File): Clip {
    val audioInputStream: AudioInputStream = AudioSystem.getAudioInputStream(file)
    val clip = AudioSystem.getClip()
    clip.open(audioInputStream)
    clip.start()
    return clip
}

fun main() = application {
    Window(onCloseRequest = ::exitApplication, title = "SimPL Stethoscope App") {
        App()
    }
}
