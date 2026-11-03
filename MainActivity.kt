package br.com.memoriamagica

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlin.random.Random

private val DeepPurple = Color(0xFF21064D)
private val Purple = Color(0xFF6424B8)
private val BrightPurple = Color(0xFF8B3DDB)
private val Pink = Color(0xFFFF4FA3)
private val Yellow = Color(0xFFFFD447)
private val Mint = Color(0xFF48E0A4)
private val CardBack = Color(0xFF7737C8)

private enum class Difficulty(val label: String, val pairs: Int, val columns: Int, val hint: String) {
    EASY("Fácil", 6, 3, "12 cartas · Para começar"),
    MEDIUM("Médio", 10, 4, "20 cartas · Mais desafio"),
    HARD("Difícil", 15, 5, "30 cartas · Teste sua memória")
}

private data class MemoryCard(val id: Int, val symbol: String, val pairId: Int)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = android.graphics.Color.rgb(33, 6, 77)
        window.navigationBarColor = android.graphics.Color.rgb(33, 6, 77)
        setContent { MemoryMagicApp(this) }
    }
}

@Composable
private fun MemoryMagicApp(context: Context) {
    val prefs = remember { context.getSharedPreferences("memoria_magica", Context.MODE_PRIVATE) }
    var screen by remember { mutableStateOf("home") }
    var difficulty by remember { mutableStateOf(Difficulty.EASY) }
    var cards by remember { mutableStateOf(emptyList<MemoryCard>()) }
    var opened by remember { mutableStateOf(emptyList<Int>()) }
    var matched by remember { mutableStateOf(setOf<Int>()) }
    var moves by remember { mutableIntStateOf(0) }
    var seconds by remember { mutableIntStateOf(0) }
    var locked by remember { mutableStateOf(false) }
    var bestMoves by remember { mutableIntStateOf(prefs.getInt("best_${difficulty.name}", 0)) }

    fun startGame(level: Difficulty) {
        difficulty = level
        val symbols = listOf("🐶", "🐱", "🦊", "🐸", "🐼", "🦁", "🐵", "🐰", "🐨", "🐯", "🐷", "🐙", "🦄", "🐝", "🦋")
        val chosen = symbols.take(level.pairs)
        cards = (chosen + chosen).mapIndexed { index, symbol ->
            MemoryCard(index, symbol, index % level.pairs)
        }.shuffled(Random(System.nanoTime()))
        opened = emptyList()
        matched = emptySet()
        moves = 0
        seconds = 0
        locked = false
        bestMoves = prefs.getInt("best_${level.name}", 0)
        screen = "game"
    }

    LaunchedEffect(screen, matched.size) {
        while (screen == "game" && matched.size < difficulty.pairs * 2) {
            delay(1000)
            seconds++
        }
    }

    LaunchedEffect(opened, locked) {
        if (opened.size == 2 && locked) {
            delay(850)
            opened = emptyList()
            locked = false
        }
    }

    val won = screen == "game" && matched.size == cards.size && cards.isNotEmpty()

    LaunchedEffect(won) {
        if (won) {
            val old = prefs.getInt("best_${difficulty.name}", 0)
            if (old == 0 || moves < old) {
                prefs.edit().putInt("best_${difficulty.name}", moves).apply()
                bestMoves = moves
            }
        }
    }

    MaterialTheme(colorScheme = darkColorScheme(
        primary = Yellow,
        secondary = Pink,
        background = DeepPurple,
        surface = Purple,
        onPrimary = DeepPurple,
        onBackground = Color.White,
        onSurface = Color.White
    )) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(listOf(Color(0xFF4B168B), DeepPurple, Color(0xFF170333))))
        ) {
            when (screen) {
                "home" -> HomeScreen(
                    best = Difficulty.entries.mapNotNull { level ->
                        prefs.getInt("best_${level.name}", 0).takeIf { it > 0 }?.let { "${level.label}: $it jogadas" }
                    },
                    onPlay = { screen = "levels" }
                )
                "levels" -> LevelScreen(onBack = { screen = "home" }, onChoose = { startGame(it) })
                else -> GameScreen(
                    difficulty = difficulty,
                    cards = cards,
                    opened = opened,
                    matched = matched,
                    moves = moves,
                    seconds = seconds,
                    bestMoves = bestMoves,
                    won = won,
                    onBack = { screen = "levels" },
                    onRestart = { startGame(difficulty) },
                    onCardClick = { index ->
                        if (!locked && !won && index !in matched && index !in opened && opened.size < 2) {
                            val next = opened + index
                            opened = next
                            if (next.size == 2) {
                                moves++
                                val first = cards[next[0]]
                                val second = cards[next[1]]
                                if (first.pairId == second.pairId) {
                                    matched = matched + next
                                    opened = emptyList()
                                } else {
                                    locked = true
                                }
                            }
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun HomeScreen(best: List<String>, onPlay: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("🌈", fontSize = 58.sp)
        Text("MEMÓRIA", color = Color.White, fontSize = 36.sp, fontWeight = FontWeight.Black, letterSpacing = 2.sp)
        Text("MÁGICA", color = Yellow, fontSize = 42.sp, fontWeight = FontWeight.Black, letterSpacing = 3.sp)
        Spacer(Modifier.height(12.dp))
        Text("Encontre os pares e treine sua memória!", color = Color(0xFFE9D5FF), textAlign = TextAlign.Center, fontSize = 16.sp)
        Spacer(Modifier.height(34.dp))
        Button(
            onClick = onPlay,
            modifier = Modifier.fillMaxWidth().height(62.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Yellow, contentColor = DeepPurple),
            shape = RoundedCornerShape(22.dp)
        ) { Text("JOGAR AGORA  ▶", fontSize = 19.sp, fontWeight = FontWeight.ExtraBold) }
        Spacer(Modifier.height(22.dp))
        if (best.isNotEmpty()) {
            Surface(color = Color(0x554B168B), shape = RoundedCornerShape(18.dp), border = BorderStroke(1.dp, Color(0x66FFFFFF))) {
                Column(Modifier.fillMaxWidth().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("🏆 SEUS RECORDES", color = Yellow, fontWeight = FontWeight.Bold)
                    best.forEach { Text(it, color = Color.White, modifier = Modifier.padding(top = 5.dp)) }
                }
            }
        }
        Spacer(Modifier.height(28.dp))
        Text("Feito para se divertir 💜", color = Color(0xFFCDB4F5), fontSize = 13.sp)
    }
}

@Composable
private fun LevelScreen(onBack: () -> Unit, onChoose: (Difficulty) -> Unit) {
    Column(Modifier.fillMaxSize().padding(22.dp), verticalArrangement = Arrangement.Center) {
        Text("← Voltar", color = Color.White, modifier = Modifier.clickable { onBack() }.padding(8.dp))
        Spacer(Modifier.height(20.dp))
        Text("ESCOLHA O NÍVEL", color = Color.White, fontSize = 27.sp, fontWeight = FontWeight.Black)
        Text("Prepare sua memória!", color = Color(0xFFE9D5FF), fontSize = 15.sp)
        Spacer(Modifier.height(24.dp))
        Difficulty.entries.forEach { level ->
            val tint = when (level) {
                Difficulty.EASY -> Mint
                Difficulty.MEDIUM -> Yellow
                Difficulty.HARD -> Pink
            }
            Surface(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp).clickable { onChoose(level) },
                color = Color(0xFF59269A),
                shape = RoundedCornerShape(22.dp),
                border = BorderStroke(2.dp, tint),
                shadowElevation = 5.dp
            ) {
                Row(Modifier.padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(when (level) { Difficulty.EASY -> "🌱"; Difficulty.MEDIUM -> "⚡"; Difficulty.HARD -> "🔥" }, fontSize = 32.sp)
                    Spacer(Modifier.width(14.dp))
                    Column(Modifier.weight(1f)) {
                        Text(level.label, color = tint, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                        Text(level.hint, color = Color.White, fontSize = 13.sp)
                    }
                    Text("➜", color = Color.White, fontSize = 24.sp)
                }
            }
        }
    }
}

@Composable
private fun GameScreen(
    difficulty: Difficulty,
    cards: List<MemoryCard>,
    opened: List<Int>,
    matched: Set<Int>,
    moves: Int,
    seconds: Int,
    bestMoves: Int,
    won: Boolean,
    onBack: () -> Unit,
    onRestart: () -> Unit,
    onCardClick: (Int) -> Unit
) {
    val timeText = "%02d:%02d".format(seconds / 60, seconds % 60)
    Column(Modifier.fillMaxSize().padding(horizontal = 14.dp, vertical = 10.dp)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            Text("← Sair", color = Color.White, modifier = Modifier.clickable { onBack() }.padding(8.dp))
            Text("MEMÓRIA MÁGICA", color = Yellow, fontWeight = FontWeight.Black, fontSize = 17.sp)
            Text("↻", color = Color.White, fontSize = 25.sp, modifier = Modifier.clickable { onRestart() }.padding(8.dp))
        }
        Text("${difficulty.label.uppercase()}  •  ${matched.size / 2}/${difficulty.pairs} pares", color = Color(0xFFE9D5FF), fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.CenterHorizontally).padding(vertical = 8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            StatPill("🎯 Jogadas", moves.toString(), Modifier.weight(1f))
            StatPill("⏱ Tempo", timeText, Modifier.weight(1f))
            StatPill("🏆 Recorde", if (bestMoves == 0) "—" else bestMoves.toString(), Modifier.weight(1f))
        }
        Spacer(Modifier.height(14.dp))
        LazyVerticalGrid(
            columns = GridCells.Fixed(difficulty.columns),
            modifier = Modifier.fillMaxWidth().weight(1f),
            contentPadding = PaddingValues(bottom = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            itemsIndexed(cards, key = { _, card -> card.id }) { index, card ->
                MemoryCardView(
                    symbol = card.symbol,
                    revealed = index in opened || index in matched,
                    found = index in matched,
                    enabled = !won,
                    onClick = { onCardClick(index) }
                )
            }
        }
        AnimatedVisibility(visible = won, enter = fadeIn(), exit = fadeOut()) {
            Surface(color = Color(0xFF6424B8), shape = RoundedCornerShape(22.dp), border = BorderStroke(2.dp, Yellow), modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("🏆 VOCÊ CONSEGUIU!", color = Yellow, fontSize = 22.sp, fontWeight = FontWeight.Black)
                    Text("Todos os pares em $moves jogadas • $timeText", color = Color.White, textAlign = TextAlign.Center)
                    Button(onClick = onRestart, colors = ButtonDefaults.buttonColors(containerColor = Yellow, contentColor = DeepPurple), modifier = Modifier.padding(top = 10.dp)) {
                        Text("JOGAR NOVAMENTE", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
        Text("Toque em duas cartas para encontrar o par 💜", color = Color(0xFFE9D5FF), fontSize = 12.sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth().padding(top = 8.dp))
    }
}

@Composable
private fun StatPill(label: String, value: String, modifier: Modifier = Modifier) {
    Surface(modifier, color = Color(0xFF5A2698), shape = RoundedCornerShape(14.dp), border = BorderStroke(1.dp, Color(0x66FFFFFF))) {
        Column(Modifier.padding(vertical = 8.dp, horizontal = 4.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(label, color = Color(0xFFE9D5FF), fontSize = 10.sp)
            Text(value, color = Yellow, fontSize = 17.sp, fontWeight = FontWeight.ExtraBold)
        }
    }
}

@Composable
private fun MemoryCardView(
    symbol: String,
    revealed: Boolean,
    found: Boolean,
    enabled: Boolean,
    onClick: () -> Unit
) {
    val rotation by animateFloatAsState(if (revealed) 180f else 0f, label = "flip")
    val frontColors = listOf(Pink, Color(0xFF14B8D4), Color(0xFFFF9F43), Mint, Color(0xFF9B72F2), Yellow)
    val cardColor = frontColors[(symbol.codePointAt(0) % frontColors.size)]
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(0.78f)
            .graphicsLayer { rotationY = rotation; cameraDistance = 12 * density }
            .clip(RoundedCornerShape(16.dp))
            .background(if (found) Color(0xFF20B879) else if (revealed) cardColor else CardBack)
            .clickable(enabled = enabled && !revealed && !found, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (revealed) {
            Text(symbol, fontSize = 29.sp, modifier = Modifier.graphicsLayer { rotationY = 180f })
        } else {
            Text("?", color = Color.White, fontSize = 30.sp, fontWeight = FontWeight.Black)
            Text("✦", color = Yellow, fontSize = 12.sp, modifier = Modifier.align(Alignment.TopEnd).padding(5.dp))
        }
    }
}
