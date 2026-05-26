package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.database.BrandingEntity
import com.example.data.database.SongEntity
import com.example.playback.PlaybackManager
import kotlinx.coroutines.delay
import java.util.Random

// --- Helper Color Parser ---
fun parseColor(hex: String, fallback: Color = Color(0xFF00FFCC)): Color {
    return try {
        Color(android.graphics.Color.parseColor(hex))
    } catch (e: Exception) {
        fallback
    }
}

// ==========================================
// 1. INTRO SPLASH SCREEN
// ==========================================
@Composable
fun GlowingNeonText(
    text: String,
    fontSize: androidx.compose.ui.unit.TextUnit,
    accentColor: Color,
    alpha: Float,
    letterSpacing: androidx.compose.ui.unit.TextUnit = androidx.compose.ui.unit.TextUnit.Unspecified,
    fontWeight: FontWeight = FontWeight.Normal,
    fontFamily: FontFamily = FontFamily.Default
) {
    Box(contentAlignment = Alignment.Center) {
        // Broad ambient background glow
        Text(
            text = text,
            fontSize = fontSize,
            fontFamily = fontFamily,
            fontWeight = fontWeight,
            letterSpacing = letterSpacing,
            color = accentColor.copy(alpha = 0.25f * alpha),
            style = TextStyle(
                shadow = Shadow(
                    color = accentColor,
                    blurRadius = 45f * alpha
                )
            )
        )
        
        // Direct neon envelope glow
        Text(
            text = text,
            fontSize = fontSize,
            fontFamily = fontFamily,
            fontWeight = fontWeight,
            letterSpacing = letterSpacing,
            color = accentColor.copy(alpha = 0.85f * alpha),
            style = TextStyle(
                shadow = Shadow(
                    color = accentColor,
                    blurRadius = 15f * alpha
                )
            )
        )

        // Ultra high contrast center filament (realistic glass neon discharge inner tube)
        Text(
            text = text,
            fontSize = fontSize,
            fontFamily = fontFamily,
            fontWeight = fontWeight,
            letterSpacing = letterSpacing,
            color = Color.White.copy(alpha = 0.95f * alpha)
        )
    }
}

@Composable
fun AnimatedEqualizer(
    barCount: Int = 36,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    val heights = remember {
        mutableStateListOf<Float>().apply {
            if (isEmpty()) {
                addAll(List(barCount) { 0.15f })
            }
        }
    }
    
    LaunchedEffect(Unit) {
        val random = java.util.Random()
        while (true) {
            for (i in 0 until barCount) {
                val distFromCenter = Math.abs(i - barCount / 2f) / (barCount / 2f)
                val envelope = (1.0f - distFromCenter * 0.7f).coerceIn(0.1f, 1.0f)
                val target = envelope * (0.15f + random.nextFloat() * 0.85f)
                if (i < heights.size) {
                    heights[i] = heights[i] + (target - heights[i]) * 0.25f
                }
            }
            delay(35)
        }
    }

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val centerY = height * 0.55f
        val maxUpwardHeight = centerY - 10f
        val maxDownwardHeight = height - centerY - 10f
        
        val barWidth = width / (barCount + (barCount - 1) * 0.3f)
        val spacing = barWidth * 0.3f
        
        for (i in 0 until barCount) {
            val hRatio = if (i < heights.size) heights[i] else 0.15f
            val barH = hRatio * maxUpwardHeight
            
            val left = i * (barWidth + spacing)
            val top = centerY - barH
            
            // Main upward neon bar
            drawRoundRect(
                color = accentColor,
                topLeft = Offset(left, top),
                size = androidx.compose.ui.geometry.Size(barWidth, barH),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f, 4f)
            )
            
            // White neon core filament center line
            val innerWidth = barWidth * 0.35f
            val innerLeft = left + (barWidth - innerWidth) / 2f
            drawRoundRect(
                color = Color.White.copy(alpha = 0.8f),
                topLeft = Offset(innerLeft, top + 1f),
                size = androidx.compose.ui.geometry.Size(innerWidth, maxOf(1f, barH - 1f)),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(2f, 2f)
            )

            // Downward reflection bar with fade gradient
            val refH = hRatio * maxDownwardHeight * 0.5f
            if (refH > 1f) {
                drawRoundRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            accentColor.copy(alpha = 0.3f * hRatio),
                            accentColor.copy(alpha = 0.05f * hRatio),
                            Color.Transparent
                        ),
                        startY = centerY,
                        endY = centerY + refH
                    ),
                    topLeft = Offset(left, centerY),
                    size = androidx.compose.ui.geometry.Size(barWidth, refH),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f, 4f)
                )
            }
        }
    }
}

@Composable
fun IntroSplashScreen(
    branding: BrandingEntity,
    onFinished: () -> Unit
) {
    val accentColor = parseColor(branding.primaryColorHex)
    val glowColor = parseColor(branding.splashGlowHex)

    var startAnimation by remember { mutableStateOf(false) }
    
    // Scale & Alpha animations for luxury cinema appearance
    val alphaAnim by animateFloatAsState(
        targetValue = if (startAnimation) 1.0f else 0.0f,
        animationSpec = twinAlphaSpec(),
        label = "SplashAlpha"
    )

    val scaleAnim by animateFloatAsState(
        targetValue = if (startAnimation) 1.15f else 0.85f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessVeryLow
        ),
        label = "SplashScale"
    )

    // Pulse glow animation
    val infiniteTransition = rememberInfiniteTransition(label = "SplashGlowPulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "GlowPulse"
    )

    var entered by remember { mutableStateOf(false) }

    fun handleEnter() {
        if (!entered) {
            entered = true
            PlaybackManager.stop()
            onFinished()
        }
    }

    LaunchedEffect(Unit) {
        // Trigger intro jingle audio on startup
        PlaybackManager.playIntroJingle()
        
        delay(300) // Delay slightly for elegant onset
        startAnimation = true
        
        // Wait 7.5 seconds, then fade out and trigger finished redirect
        delay(7500)
        if (!entered) {
            startAnimation = false
            delay(500)
            handleEnter()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF030305)), // Authentic ultra deep space background
        contentAlignment = Alignment.Center
    ) {
        // Ambient backdrop neon radial flare
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(glowColor.copy(alpha = 0.2f * alphaAnim), Color.Transparent),
                        center = Offset.Unspecified,
                        radius = 1600f
                    )
                )
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 50.dp, horizontal = 24.dp)
                .scale(scaleAnim * pulseScale)
        ) {
            Spacer(modifier = Modifier.weight(0.1f))

            // Massive glowing headers exactly as layout
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                GlowingNeonText(
                    text = branding.logoText,
                    fontSize = 110.sp,
                    accentColor = accentColor,
                    alpha = alphaAnim,
                    fontWeight = FontWeight.ExtraBold,
                    fontFamily = FontFamily.SansSerif,
                    letterSpacing = 4.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                GlowingNeonText(
                    text = "SOUND & VISION",
                    fontSize = 24.sp,
                    accentColor = accentColor,
                    alpha = alphaAnim,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.SansSerif,
                    letterSpacing = 4.sp
                )
            }

            Spacer(modifier = Modifier.weight(0.15f))

            // Animated Spectral Equalizer with mirrored bottom reflection
            AnimatedEqualizer(
                barCount = 38,
                accentColor = accentColor,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .padding(horizontal = 8.dp)
            )

            Spacer(modifier = Modifier.weight(0.15f))

            // Neon Glow rounded ENTER Button
            Box(
                modifier = Modifier
                    .width(220.dp)
                    .height(56.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color.Black.copy(alpha = 0.4f))
                    .border(2.dp, accentColor.copy(alpha = alphaAnim), RoundedCornerShape(14.dp))
                    .clickable {
                        handleEnter()
                    }
                    .testTag("enter_button"),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "ENTER",
                    color = Color.White.copy(alpha = alphaAnim),
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    letterSpacing = 6.sp,
                    fontFamily = FontFamily.Monospace,
                    style = TextStyle(
                        shadow = Shadow(
                            color = accentColor,
                            blurRadius = 15f * alphaAnim
                        )
                    )
                )
            }

            Spacer(modifier = Modifier.weight(0.05f))
        }
    }
}

private fun twinAlphaSpec(): TweenSpec<Float> {
    return tween(durationMillis = 1200, easing = EaseInOutCubic)
}

// ==========================================
// 2. HOME SCREEN
// ==========================================
@Composable
fun HomeScreen(
    songs: List<SongEntity>,
    latestSongs: List<SongEntity>,
    branding: BrandingEntity,
    onNavigateToLibrary: () -> Unit,
    onNavigateToExperience: () -> Unit,
    onOpenAdmin: () -> Unit
) {
    val accentColor = parseColor(branding.primaryColorHex)
    var headerTapCount by remember { mutableStateOf(0) }
    val featuredSong = songs.firstOrNull { it.isFeatured } ?: songs.firstOrNull()

    // Splash Header Tap Gestures to launch Admin Secret login pathway
    LaunchedEffect(headerTapCount) {
        if (headerTapCount > 0) {
            delay(3000)
            headerTapCount = 0 // Reset tap counters
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0C0C0F)),
        contentPadding = PaddingValues(bottom = 110.dp)
    ) {
        // App Premium Header
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 20.dp)
                    .windowInsetsPadding(WindowInsets.statusBars),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    modifier = Modifier.clickable(
                        interactionSource = null,
                        indication = null
                    ) {
                        headerTapCount++
                        if (headerTapCount >= 3) {
                            headerTapCount = 0
                            onOpenAdmin()
                        }
                    }
                ) {
                    Text(
                        text = branding.appTitle,
                        fontFamily = FontFamily.SansSerif,
                        fontWeight = FontWeight.Black,
                        fontSize = 24.sp,
                        color = Color.White
                    )
                    Text(
                        text = "Premium Cinematic Soundscapes",
                        fontFamily = FontFamily.SansSerif,
                        fontWeight = FontWeight.Light,
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }

                // Decorative Action Node indicating standalone status
                Icon(
                    imageVector = Icons.Default.CloudQueue,
                    contentDescription = "Online Offline Status",
                    tint = accentColor,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        // FEATURED SONG BANNER
        featuredSong?.let { song ->
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                ) {
                    Text(
                        text = "FEATURED TONIGHT",
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        letterSpacing = 2.sp,
                        color = accentColor,
                        modifier = Modifier.padding(start = 4.dp, bottom = 10.dp)
                    )

                    val itemColor = parseColor(song.coverColorHex)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(itemColor.copy(alpha = 0.85f), Color(0xFF1E1E24))
                                )
                            )
                            .clickable { PlaybackManager.playSong(song) }
                            .testTag("featured_banner")
                    ) {
                        if (!song.coverUrl.isNullOrBlank()) {
                            AsyncImage(
                                model = song.coverUrl,
                                contentDescription = "Featured Cover",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.verticalGradient(
                                            colors = listOf(Color.Black.copy(alpha = 0.2f), Color.Black.copy(alpha = 0.75f))
                                        )
                                    )
                            )
                        } else {
                            // Glossy geometric background highlights
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                drawCircle(
                                    color = accentColor.copy(alpha = 0.15f),
                                    radius = 220f,
                                    center = Offset(size.width - 150f, size.height - 50f)
                                )
                            }
                        }

                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(24.dp),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color.Black.copy(alpha = 0.4f))
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = "CINEMATIC SPEC",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.Bottom,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = song.title,
                                        fontWeight = FontWeight.Black,
                                        fontSize = 26.sp,
                                        color = Color.White,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = song.artist,
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 14.sp,
                                        color = Color.LightGray
                                    )
                                }

                                FloatingActionButton(
                                    onClick = { PlaybackManager.playSong(song) },
                                    containerColor = accentColor,
                                    contentColor = Color.Black,
                                    shape = CircleShape,
                                    modifier = Modifier
                                        .size(48.dp)
                                        .testTag("play_featured_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PlayArrow,
                                        contentDescription = "Play"
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // LATEST RELEASES (Horizontal scroll)
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "NEW RELEASES",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        letterSpacing = 2.sp,
                        color = Color.White
                    )
                    Text(
                        text = "See All",
                        fontSize = 12.sp,
                        color = accentColor,
                        modifier = Modifier.clickable { onNavigateToLibrary() }
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                if (latestSongs.isEmpty()) {
                    Text(
                        text = "No content added yet.",
                        color = Color.Gray,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )
                } else {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 20.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(latestSongs) { song ->
                            LatestReleaseCard(song = song, accentColor = accentColor)
                        }
                    }
                }
            }
        }

        // ALBUMS (ALBEN) ROW
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp)
            ) {
                Text(
                    text = "FEATURED ALBEN (ALBUMS)",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    letterSpacing = 2.sp,
                    color = Color.White,
                    modifier = Modifier.padding(start = 24.dp, end = 24.dp, bottom = 14.dp)
                )

                val albumSongs = songs.filter { it.releaseType?.equals("Album", true) == true }
                if (albumSongs.isEmpty()) {
                    Text(
                        text = "Keine Alben gelistet.",
                        color = Color.Gray,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )
                } else {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 20.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(albumSongs) { song ->
                            LatestReleaseCard(song = song, accentColor = accentColor)
                        }
                    }
                }
            }
        }

        // SINGLES (SINGLES) ROW
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp)
            ) {
                Text(
                    text = "NEUE SINGLES & RELEASES",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    letterSpacing = 2.sp,
                    color = Color.White,
                    modifier = Modifier.padding(start = 24.dp, end = 24.dp, bottom = 14.dp)
                )

                val singleSongs = songs.filter { it.releaseType?.equals("Single", true) == true || it.releaseType.isNullOrBlank() }
                if (singleSongs.isEmpty()) {
                    Text(
                        text = "Keine Singles gelistet.",
                        color = Color.Gray,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )
                } else {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 20.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(singleSongs) { song ->
                            LatestReleaseCard(song = song, accentColor = accentColor)
                        }
                    }
                }
            }
        }

        // AMBIENT & TRANQUILITY ROWS
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp)
            ) {
                Text(
                    text = "AMBIENT SPACE RESONANCE",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    letterSpacing = 2.sp,
                    color = Color.White,
                    modifier = Modifier.padding(start = 24.dp, end = 24.dp, bottom = 14.dp)
                )

                val ambientSongs = songs.filter { it.category.equals("Ambient", true) }
                if (ambientSongs.isEmpty()) {
                    Text(
                        text = "No ambient tracks listed.",
                        color = Color.Gray,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )
                } else {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 20.dp),
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        items(ambientSongs) { song ->
                            SmallMusicCard(song = song, accentColor = accentColor)
                        }
                    }
                }
            }
        }

        // CYBER PULSE BEATS ROW
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp)
            ) {
                Text(
                    text = "CYBER ENERGETIC PULSE",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    letterSpacing = 2.sp,
                    color = Color.White,
                    modifier = Modifier.padding(start = 24.dp, end = 24.dp, bottom = 14.dp)
                )

                val pulseSongs = songs.filter { it.category.equals("Pulse", true) }
                if (pulseSongs.isEmpty()) {
                    Text(
                        text = "No neon pulses available.",
                        color = Color.Gray,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )
                } else {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 20.dp),
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        items(pulseSongs) { song ->
                            SmallMusicCard(song = song, accentColor = accentColor)
                        }
                    }
                }
            }
        }

        // CONTINUE LISTENING & OFFLINE WEBAPP STATUS
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 10.dp)
            ) {
                Text(
                    text = "CONTINUE WEB LISTENING",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    letterSpacing = 2.sp,
                    color = Color.White,
                    modifier = Modifier.padding(start = 4.dp, bottom = 12.dp)
                )

                // Quick restore block
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFF141419))
                        .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(16.dp))
                        .padding(16.dp)
                ) {
                    val fallbackSong = songs.lastOrNull()
                    if (fallbackSong != null) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(45.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(parseColor(fallbackSong.coverColorHex))
                                )
                                Spacer(modifier = Modifier.width(14.dp))
                                Column {
                                    Text(
                                        text = fallbackSong.title,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = Color.White
                                    )
                                    Text(
                                        text = fallbackSong.artist + " • Continue",
                                        fontSize = 12.sp,
                                        color = Color.Gray
                                    )
                                }
                            }

                            FilledIconButton(
                                onClick = { PlaybackManager.playSong(fallbackSong) },
                                colors = IconButtonDefaults.filledIconButtonColors(
                                    containerColor = Color.White.copy(alpha = 0.08f)
                                ),
                                modifier = Modifier.size(40.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = "Resume",
                                    tint = accentColor
                                )
                            }
                        }
                    } else {
                        Text("No items loaded.", color = Color.Gray, fontSize = 12.sp)
                    }
                }
            }
        }

        // INSTALL APP EXPLAINER PROMPT
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(Color(0xFF161C19), Color(0xFF0F1210))
                        )
                    )
                    .border(1.dp, accentColor.copy(alpha = 0.15f), RoundedCornerShape(16.dp))
                    .padding(20.dp)
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.SystemUpdate,
                            contentDescription = "Installer",
                            tint = accentColor,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "NATIVE SYSTEM RUNTIME",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = accentColor,
                            letterSpacing = 1.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "This premium application is running as a high-performance native client with full local persistence, instant synth loading, zero buffer times, and responsive system audio layouts.",
                        color = Color.LightGray,
                        fontSize = 12.sp,
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))
                    
                    // Instructions for adding PWA or pinning standalone shortcut
                    Text(
                        text = "✔ Standalone App Install: INSTALLED\n✔ UI and Audio Engine Cache: OFFLINE PERSISTED\n💡 Tip: Long press the title header to enter Hidden Master Backend.",
                        color = Color.Gray,
                        fontSize = 11.sp,
                        lineHeight = 16.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }
    }
}

// ==========================================
// CARD COMPONENT SUB ELEMENTS
// ==========================================
@Composable
fun LatestReleaseCard(song: SongEntity, accentColor: Color) {
    val coverColor = parseColor(song.coverColorHex)
    Column(
        modifier = Modifier
            .width(140.dp)
            .clickable { PlaybackManager.playSong(song) }
            .testTag("latest_release_card_${song.id}")
    ) {
        Box(
            modifier = Modifier
                .size(140.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(coverColor)
                .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(14.dp)),
            contentAlignment = Alignment.Center
        ) {
            if (!song.coverUrl.isNullOrBlank()) {
                AsyncImage(
                    model = song.coverUrl,
                    contentDescription = song.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                // Elegant subtle graphic decoration inside cover
                Icon(
                    imageVector = Icons.Outlined.GraphicEq,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.15f),
                    modifier = Modifier.size(48.dp)
                )
            }

            // Play over node on hover or focus
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.25f)),
                contentAlignment = Alignment.BottomEnd
            ) {
                Box(
                    modifier = Modifier
                        .padding(12.dp)
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(accentColor),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Play",
                        tint = Color.Black,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = song.title,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            color = Color.White,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = song.artist,
            fontSize = 11.sp,
            color = Color.Gray,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun SmallMusicCard(song: SongEntity, accentColor: Color) {
    val coverColor = parseColor(song.coverColorHex)
    Row(
        modifier = Modifier
            .width(220.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF141419))
            .clickable { PlaybackManager.playSong(song) }
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(coverColor),
            contentAlignment = Alignment.Center
        ) {
            if (!song.coverUrl.isNullOrBlank()) {
                AsyncImage(
                    model = song.coverUrl,
                    contentDescription = song.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Icon(
                    imageVector = Icons.Default.MusicNote,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.2f),
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = song.title,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = song.artist,
                fontSize = 10.sp,
                color = Color.Gray,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Icon(
            imageVector = Icons.Default.PlayCircleFilled,
            contentDescription = "Play",
            tint = accentColor,
            modifier = Modifier.size(24.dp)
        )
    }
}

// ==========================================
// 3. LIBRARY SCREEN
// ==========================================
@Composable
fun LibraryScreen(
    songs: List<SongEntity>,
    branding: BrandingEntity
) {
    val accentColor = parseColor(branding.primaryColorHex)
    var selectedCategory by remember { mutableStateOf("All") }
    var searchQuery by remember { mutableStateOf("") }

    val categories = listOf("All", "Ambient", "Pulse", "Cinematic", "Chill")

    val filteredSongs = songs.filter { song ->
        val matchesCategory = selectedCategory == "All" || song.category.equals(selectedCategory, true)
        val matchesSearch = song.title.contains(searchQuery, true) || song.artist.contains(searchQuery, true)
        matchesCategory && matchesSearch
    }

    val currentPlayingSong by PlaybackManager.currentSong.collectAsState()
    val isPlayingState by PlaybackManager.isPlaying.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0C0C0F))
            .windowInsetsPadding(WindowInsets.statusBars)
    ) {
        // Headline
        Text(
            text = "MUSIC LIBRARY",
            fontWeight = FontWeight.Black,
            fontSize = 28.sp,
            color = Color.White,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)
        )

        // Custom Search Field
        TextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search songs or artists...", color = Color.Gray) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray) },
            singleLine = true,
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color(0xFF141419),
                unfocusedContainerColor = Color(0xFF141419),
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.LightGray,
                focusedIndicatorColor = accentColor,
                unfocusedIndicatorColor = Color.Transparent
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 6.dp)
        )

        // Horizontal Category Tabs
        LazyRow(
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(categories) { cat ->
                val isSelected = selectedCategory == cat
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(30.dp))
                        .background(if (isSelected) accentColor else Color(0xFF141419))
                        .clickable { selectedCategory = cat }
                        .padding(horizontal = 18.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = cat,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = if (isSelected) Color.Black else Color.White
                    )
                }
            }
        }

        // Song Scroll List
        if (filteredSongs.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.MusicNote,
                        contentDescription = "Empty",
                        tint = Color.Gray,
                        modifier = Modifier.size(54.dp)
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "No songs matching requirements found.",
                        color = Color.Gray,
                        fontSize = 13.sp
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(bottom = 120.dp, start = 20.dp, end = 20.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredSongs) { song ->
                    val isCurrent = currentPlayingSong?.id == song.id
                    val cover = parseColor(song.coverColorHex)

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isCurrent) Color(0xFF161E1A) else Color(0xFF141419))
                            .border(
                                width = 1.dp,
                                color = if (isCurrent) accentColor.copy(alpha = 0.3f) else Color.Transparent,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clickable { PlaybackManager.playSong(song) }
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Artwork Cover
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(cover),
                            contentAlignment = Alignment.Center
                        ) {
                            if (!song.coverUrl.isNullOrBlank()) {
                                AsyncImage(
                                    model = song.coverUrl,
                                    contentDescription = song.title,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                            if (isCurrent && isPlayingState) {
                                // Mini reactive animated wave representation
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(Color.Black.copy(alpha = 0.4f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.GraphicEq,
                                        contentDescription = "Playing Animation",
                                        tint = accentColor,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            } else {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(Color.Black.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PlayCircle,
                                        contentDescription = null,
                                        tint = Color.White.copy(alpha = 0.6f),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = song.title,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = if (isCurrent) accentColor else Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = song.artist + " • " + song.duration,
                                fontSize = 12.sp,
                                color = Color.Gray,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        IconButton(
                            onClick = { PlaybackManager.playSong(song) },
                            modifier = Modifier.testTag("play_song_${song.id}")
                        ) {
                            Icon(
                                imageVector = if (isCurrent && isPlayingState) Icons.Default.PauseCircle else Icons.Default.PlayArrow,
                                contentDescription = "Play Control",
                                tint = if (isCurrent) accentColor else Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// 4. SOUND & VISION EXPERIMENTAL PAGE
// ==========================================
@Composable
fun CinematicScreen(
    branding: BrandingEntity
) {
    val accentColor = parseColor(branding.primaryColorHex)
    val appGlow = parseColor(branding.splashGlowHex)
    val currentSong by PlaybackManager.currentSong.collectAsState()
    val isPlaying by PlaybackManager.isPlaying.collectAsState()
    val visualizerData by PlaybackManager.visualizerFlow.collectAsState()

    val random = remember { Random() }
    
    // Smooth infinite particle timer state
    var triggerTick by remember { mutableStateOf(0L) }
    LaunchedEffect(isPlaying) {
        if (isPlaying) {
            while (true) {
                delay(16) // tick roughly at 60 fps
                triggerTick++
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF030304))
    ) {
        // IMMERSIVE VISUALS CANVAS
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height
            val centerX = width / 2f
            val centerY = height / 2f

            // Grab instant average pulse sizing based on first few visualizer bands
            val avgAmplitude = visualizerData.take(6).average().toFloat().coerceIn(0.05f, 1.0f)

            // Draw glowing background radial gradient aligned with the pulse
            drawCircle(
                color = appGlow.copy(alpha = 0.08f * avgAmplitude),
                radius = 350f + avgAmplitude * 150f,
                center = Offset(centerX, centerY)
            )

            // Draw drifting cosmic particulate nodes reacting to melody frequencies
            for (p in 0 until 35) {
                val seed = p * 12345L
                val speedCoeff = 0.015f + (p % 5) * 0.01f
                val phaseX = (triggerTick.toDouble() * speedCoeff + seed) % width
                val startY = (seed % height.toLong()).toFloat()
                
                // Pulse size based on index syncing to individual visualizer bands
                val bandIdx = p % visualizerData.size
                val livePulsingRadius = 4f + visualizerData[bandIdx] * 12f

                drawCircle(
                    color = accentColor.copy(alpha = 0.18f + (visualizerData[bandIdx] * 0.35f)),
                    radius = livePulsingRadius,
                    center = Offset(phaseX.toFloat(), startY)
                )
            }

            // Draw Centering Quantum Ring of Sound
            // Changes size, shape, and brush density corresponding to average audio amplitudes!
            drawCircle(
                color = Color.White.copy(alpha = 0.04f + avgAmplitude * 0.06f),
                radius = 180f + avgAmplitude * 75f,
                center = Offset(centerX, centerY),
                style = Stroke(width = 24f)
            )

            drawCircle(
                color = accentColor.copy(alpha = 0.61f),
                radius = 190f + avgAmplitude * 85f,
                center = Offset(centerX, centerY),
                style = Stroke(width = 3f + avgAmplitude * 12f)
            )

            // Overlapping geometric orbits to simulate deep space vision
            drawCircle(
                color = Color.White.copy(alpha = 0.12f),
                radius = 120f + avgAmplitude * 40f,
                center = Offset(centerX, centerY),
                style = Stroke(width = 1f)
            )
        }

        // Full Screen Text Overlay Info
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp)
                .windowInsetsPadding(WindowInsets.statusBars),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(30.dp))
                    .background(Color.White.copy(alpha = 0.06f))
                    .padding(horizontal = 20.dp, vertical = 8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.BlurOn,
                        contentDescription = "Sync",
                        tint = accentColor,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "SYNCHRONIZED KINETIC ORBIT",
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        color = Color.White,
                        letterSpacing = 2.sp
                    )
                }
            }

            // Center details
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                if (currentSong != null) {
                    Text(
                        text = currentSong!!.title.uppercase(),
                        fontWeight = FontWeight.Black,
                        fontSize = 32.sp,
                        color = Color.White,
                        letterSpacing = 4.sp,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "ARTIST: ${currentSong!!.artist.uppercase()}",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        color = accentColor,
                        letterSpacing = 2.sp
                    )
                } else {
                    Text(
                        text = "NO SONG PRELOADED",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 24.sp,
                        color = Color.Gray,
                        letterSpacing = 3.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "SELECT A WORK FROM MUSIC LIBRARY",
                        fontSize = 11.sp,
                        color = Color.DarkGray,
                        letterSpacing = 1.sp
                    )
                }
            }

            // Simple media controls overlaid transparently
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                IconButton(
                    onClick = { PlaybackManager.previousTrack() },
                    modifier = Modifier.size(54.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.SkipPrevious,
                        contentDescription = "Prev",
                        tint = Color.White,
                        modifier = Modifier.size(34.dp)
                    )
                }

                IconButton(
                    onClick = { PlaybackManager.togglePlayPause() },
                    modifier = Modifier
                        .size(72.dp)
                        .background(Color.White.copy(alpha = 0.08f), CircleShape)
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = "PlayPause",
                        tint = accentColor,
                        modifier = Modifier.size(42.dp)
                    )
                }

                IconButton(
                    onClick = { PlaybackManager.nextTrack() },
                    modifier = Modifier.size(54.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.SkipNext,
                        contentDescription = "Next",
                        tint = Color.White,
                        modifier = Modifier.size(34.dp)
                    )
                }
            }
        }
    }
}

// ==========================================
// 5. FIXED BOTTOM SPOTIFY-LIKE PLAYER
// ==========================================
@Composable
fun FixedBottomPlayer(
    branding: BrandingEntity,
    onExpandedClick: () -> Unit
) {
    val accentColor = parseColor(branding.primaryColorHex)
    val currentSong by PlaybackManager.currentSong.collectAsState()
    val isPlaying by PlaybackManager.isPlaying.collectAsState()
    val progress by PlaybackManager.progress.collectAsState()

    AnimatedVisibility(
        visible = currentSong != null,
        enter = fadeIn(animationSpec = tween(400)),
        exit = fadeOut(animationSpec = tween(400))
    ) {
        currentSong?.let { song ->
            val coverColor = parseColor(song.coverColorHex)

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF141419).copy(alpha = 0.96f))
                    .border(1.dp, Color.White.copy(alpha = 0.07f), RoundedCornerShape(16.dp))
                    .clickable { onExpandedClick() }
                    .testTag("fixed_bottom_player")
            ) {
                Column {
                    // Small progress slide indicator at top
                    LinearProgressIndicator(
                        progress = { progress },
                        color = accentColor,
                        trackColor = Color.White.copy(alpha = 0.08f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(2.5.dp)
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            // Circular pulsing cover indicators with image load helper
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(coverColor),
                                contentAlignment = Alignment.Center
                            ) {
                                if (!song.coverUrl.isNullOrBlank()) {
                                    AsyncImage(
                                        model = song.coverUrl,
                                        contentDescription = song.title,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.MusicNote,
                                        contentDescription = null,
                                        tint = Color.White.copy(alpha = 0.3f),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            Column {
                                Text(
                                    text = song.title,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = Color.White,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = song.artist,
                                    fontSize = 11.sp,
                                    color = Color.Gray,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        // Playback Action Segment
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = { PlaybackManager.previousTrack() }) {
                                Icon(
                                    imageVector = Icons.Default.SkipPrevious,
                                    contentDescription = "Prev",
                                    tint = Color.White,
                                    modifier = Modifier.size(22.dp)
                                )
                            }

                            IconButton(
                                onClick = { PlaybackManager.togglePlayPause() },
                                modifier = Modifier.testTag("fixed_play_pause")
                            ) {
                                Icon(
                                    imageVector = if (isPlaying) Icons.Default.PauseCircle else Icons.Default.PlayCircle,
                                    contentDescription = "PlayPause",
                                    tint = accentColor,
                                    modifier = Modifier.size(34.dp)
                                )
                            }

                            IconButton(onClick = { PlaybackManager.nextTrack() }) {
                                Icon(
                                    imageVector = Icons.Default.SkipNext,
                                    contentDescription = "Next",
                                    tint = Color.White,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// 6. DETAILED EXPANDED PLAYER SCREEN
// ==========================================
@Composable
fun FullPlayerSheet(
    branding: BrandingEntity,
    onDismiss: () -> Unit
) {
    val accentColor = parseColor(branding.primaryColorHex)
    val appGlow = parseColor(branding.splashGlowHex)
    val currentSong by PlaybackManager.currentSong.collectAsState()
    val isPlaying by PlaybackManager.isPlaying.collectAsState()
    val progress by PlaybackManager.progress.collectAsState()
    val currentTimeStr by PlaybackManager.currentTimeString.collectAsState()
    val visualizerData by PlaybackManager.visualizerFlow.collectAsState()

    Dialog(
        onDismissRequest = { onDismiss() },
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        currentSong?.let { song ->
            val coverColor = parseColor(song.coverColorHex)

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF07070A))
                    .windowInsetsPadding(WindowInsets.safeDrawing)
                    .testTag("full_player_sheet")
            ) {
                // Background artistic gradient aura matching the song cover
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(coverColor.copy(alpha = 0.28f), Color.Transparent)
                            )
                        )
                )

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.SpaceBetween,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Title Bar Controls
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { onDismiss() }) {
                            Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Close", tint = Color.White)
                        }
                        Text(
                            text = "NOWSTREAMING",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 2.sp,
                            color = Color.LightGray,
                            fontFamily = FontFamily.Monospace
                        )
                        IconButton(onClick = { /* Save or share dialog */ }) {
                            Icon(Icons.Default.MoreHoriz, contentDescription = "More", tint = Color.White)
                        }
                    }

                    // Rotating/Glow Cover Concept Card with full resolution imageUrl support
                    Box(
                        modifier = Modifier
                            .size(240.dp)
                            .clip(RoundedCornerShape(24.dp))
                            .background(coverColor)
                            .border(2.dp, accentColor.copy(alpha = 0.35f), RoundedCornerShape(24.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (!song.coverUrl.isNullOrBlank()) {
                            AsyncImage(
                                model = song.coverUrl,
                                contentDescription = song.title,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.GraphicEq,
                                contentDescription = null,
                                tint = Color.White.copy(alpha = 0.12f),
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(36.dp)
                            )
                        }
                    }

                    // Music info
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = song.title,
                            fontWeight = FontWeight.Black,
                            fontSize = 26.sp,
                            color = Color.White,
                            textAlign = TextAlign.Center,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = song.artist,
                            color = accentColor,
                            fontWeight = FontWeight.Medium,
                            fontSize = 14.sp
                        )
                    }

                    // AUDIO SPECTRAL VISUALIZER (Modern Pop wave bands reacting in realtime)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        visualizerData.forEachIndexed { index, heightMultiplier ->
                            val smoothedHeight = 4.dp + (46 * heightMultiplier).dp
                            Box(
                                modifier = Modifier
                                    .width(6.dp)
                                    .height(smoothedHeight)
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(
                                        Brush.verticalGradient(
                                            listOf(accentColor, accentColor.copy(alpha = 0.2f))
                                        )
                                    )
                            )
                        }
                    }

                    // Duration Seek Indicator
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Slider(
                            value = progress,
                            onValueChange = { /* Simulated jump restricted for auto loop preservation */ },
                            colors = SliderDefaults.colors(
                                thumbColor = accentColor,
                                activeTrackColor = accentColor,
                                inactiveTrackColor = Color.White.copy(alpha = 0.08f)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = currentTimeStr, color = Color.Gray, fontSize = 11.sp)
                            Text(text = song.duration, color = Color.Gray, fontSize = 11.sp)
                        }
                    }

                    // Action Controls Block
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { /* Shuffle toggle */ }) {
                            Icon(Icons.Default.Shuffle, contentDescription = "Shuffle", tint = Color.Gray)
                        }

                        IconButton(onClick = { PlaybackManager.previousTrack() }) {
                            Icon(Icons.Default.SkipPrevious, contentDescription = "Prev", tint = Color.White, modifier = Modifier.size(34.dp))
                        }

                        IconButton(
                            onClick = { PlaybackManager.togglePlayPause() },
                            modifier = Modifier
                                .size(64.dp)
                                .background(accentColor, CircleShape)
                        ) {
                            Icon(
                                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = "Play/Pause",
                                tint = Color.Black,
                                modifier = Modifier.size(34.dp)
                            )
                        }

                        IconButton(onClick = { PlaybackManager.nextTrack() }) {
                            Icon(Icons.Default.SkipNext, contentDescription = "Next", tint = Color.White, modifier = Modifier.size(34.dp))
                        }

                        IconButton(onClick = { /* Repeat toggle */ }) {
                            Icon(Icons.Default.Repeat, contentDescription = "Repeat", tint = Color.Gray)
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// 7. MASTER ADMIN BACKEND PANEL (With login password protection)
// ==========================================
@Composable
fun AdminBackendPanel(
    viewModel: com.example.ui.viewmodel.MusicViewModel,
    branding: BrandingEntity,
    songs: List<SongEntity>,
    onClose: () -> Unit
) {
    var password by remember { mutableStateOf("") }
    var isAuthenticated by remember { mutableStateOf(false) }
    var authError by remember { mutableStateOf(false) }

    // Forms states for dynamic custom song addition
    var songTitle by remember { mutableStateOf("") }
    var songArtist by remember { mutableStateOf("") }
    var songDuration by remember { mutableStateOf("3:00") }
    var songCategory by remember { mutableStateOf("Ambient") }
    var synthPreset by remember { mutableStateOf("Cosmonaut") }
    var coverColorHex by remember { mutableStateOf("#FF8A00FF") }
    var coverUrl by remember { mutableStateOf("") }
    var releaseType by remember { mutableStateOf("Single") }

    // Forms states for branding customizations
    var appTitleText by remember { mutableStateOf(branding.appTitle) }
    var primaryColorVal by remember { mutableStateOf(branding.primaryColorHex) }
    var splashGlowVal by remember { mutableStateOf(branding.splashGlowHex) }
    var logoTextVal by remember { mutableStateOf(branding.logoText) }
    
    val context = LocalContext.current
    val accentColor = parseColor(branding.primaryColorHex)

    if (!isAuthenticated) {
        // Simple elegant locked gateway overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF08080C))
                .padding(24.dp)
                .windowInsetsPadding(WindowInsets.safeDrawing),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.width(320.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "Locked",
                    tint = accentColor,
                    modifier = Modifier.size(48.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "MASTER BACKEND CODE",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    letterSpacing = 2.sp,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(24.dp))

                TextField(
                    value = password,
                    onValueChange = { password = it; authError = false },
                    placeholder = { Text("Enter master password...", color = Color.Gray) },
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFF141419),
                        unfocusedContainerColor = Color(0xFF141419),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.LightGray,
                        focusedIndicatorColor = accentColor
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("admin_secret_field")
                )

                if (authError) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "Incorrect password. access denied.", color = Color.Red, fontSize = 11.sp)
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        if (password == "admin123") {
                            isAuthenticated = true
                        } else {
                            authError = true
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = accentColor),
                    modifier = Modifier.fillMaxWidth().testTag("submit_password_button")
                ) {
                    Text(text = "UNLOCK ACCESS", color = Color.Black, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(12.dp))

                TextButton(onClick = { onClose() }) {
                    Text(text = "CANCEL GO HOME", color = Color.Gray, fontSize = 12.sp)
                }
            }
        }
    } else {
        // Authenticated Admin Settings Dashboard
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF0F0F12))
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(20.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "ADMIN CONTROL PANEL",
                    fontWeight = FontWeight.Black,
                    fontSize = 20.sp,
                    color = Color.White
                )

                IconButton(onClick = { onClose() }) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Exit", tint = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Section: App branding modifications
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF18181F))
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "GLOBAL APPFACE SETTINGS",
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            letterSpacing = 1.sp,
                            color = accentColor,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        OutlinedTextField(
                            value = appTitleText,
                            onValueChange = { appTitleText = it },
                            label = { Text("App Title") },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.LightGray
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = logoTextVal,
                            onValueChange = { logoTextVal = it },
                            label = { Text("Splash Logo Text") },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.LightGray
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = primaryColorVal,
                            onValueChange = { primaryColorVal = it },
                            label = { Text("Accent Color Hex Code") },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.LightGray
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = splashGlowVal,
                            onValueChange = { splashGlowVal = it },
                            label = { Text("Splash Glow Hex Code") },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.LightGray
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Button(
                            onClick = {
                                viewModel.updateBranding(
                                    appTitle = appTitleText,
                                    primaryColorHex = primaryColorVal,
                                    splashGlowHex = splashGlowVal,
                                    logoText = logoTextVal
                                )
                                Toast.makeText(context, "Global branding applied!", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = accentColor),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(text = "APPLY NEW SKIN", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // Section: Custom manual song insertion form
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF18181F))
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "UPLOAD / ADD NEW MUSIC WORK",
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            letterSpacing = 1.sp,
                            color = accentColor,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        OutlinedTextField(
                            value = songTitle,
                            onValueChange = { songTitle = it },
                            label = { Text("Work Title") },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.LightGray
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = songArtist,
                            onValueChange = { songArtist = it },
                            label = { Text("Artist / Sound Oracle") },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.LightGray
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = songDuration,
                            onValueChange = { songDuration = it },
                            label = { Text("Length (e.g. 3:20)") },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.LightGray
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = songCategory,
                            onValueChange = { songCategory = it },
                            label = { Text("Category (Ambient, Pulse, Cinematic, Chill)") },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.LightGray
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = synthPreset,
                            onValueChange = { synthPreset = it },
                            label = { Text("Synth Oscillator Preset (Cosmonaut, NeonPulse, DeepVoyage, MutedRain)") },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.LightGray
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = coverColorHex,
                            onValueChange = { coverColorHex = it },
                            label = { Text("Cover Gradient Color Hex desc") },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.LightGray
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Release Type Tab / Selector
                        Text(
                            text = "RELEASE TYPE (ALBUM VS SINGLE)",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = accentColor,
                            modifier = Modifier.padding(top = 10.dp, bottom = 6.dp)
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            listOf("Single", "Album").forEach { type ->
                                val isTypeSelected = releaseType.equals(type, true)
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isTypeSelected) accentColor else Color(0xFF24242C))
                                        .clickable { releaseType = type }
                                        .padding(vertical = 10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = type,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = if (isTypeSelected) Color.Black else Color.White
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = coverUrl,
                            onValueChange = { coverUrl = it },
                            label = { Text("Cover Image URL (Optional HTTP Link)") },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.LightGray
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Cool autofill image presets
                        Text(
                            text = "PREFAST PRESET COVER IMAGES (CLICK TO APPLY)",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Gray,
                            modifier = Modifier.padding(top = 10.dp, bottom = 6.dp)
                        )

                        val presets = listOf(
                            "Synthwave" to "https://images.unsplash.com/photo-1614613535308-eb5fbd3d2c17?q=80&w=400&auto=format&fit=crop",
                            "Laser" to "https://images.unsplash.com/photo-1514525253161-7a46d19cd819?q=80&w=400&auto=format&fit=crop",
                            "Mic Glow" to "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?q=80&w=400&auto=format&fit=crop",
                            "Synth Deck" to "https://images.unsplash.com/photo-1508700115892-45ecd05ae2ad?q=80&w=400&auto=format&fit=crop",
                            "DJ Mixer" to "https://images.unsplash.com/photo-1470225620780-dba8ba36b745?q=80&w=400&auto=format&fit=crop",
                            "Pink Classic" to "https://images.unsplash.com/photo-1487180142328-054b783fc471?q=80&w=400&auto=format&fit=crop"
                        )

                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                        ) {
                            items(presets) { (label, url) ->
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (coverUrl == url) accentColor else Color(0xFF24242C))
                                        .clickable {
                                            coverUrl = url
                                            if (label == "Synthwave") coverColorHex = "#FFFF007F"
                                            if (label == "Laser") coverColorHex = "#FF8A00FF"
                                            if (label == "Mic Glow") coverColorHex = "#FF1E3A8A"
                                            if (label == "Synth Deck") coverColorHex = "#FF1D4ED8"
                                            if (label == "DJ Mixer") coverColorHex = "#FF0D9488"
                                            if (label == "Pink Classic") coverColorHex = "#FFFF0080"
                                        }
                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = label,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (coverUrl == url) Color.Black else Color.White
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Button(
                            onClick = {
                                if (songTitle.isNotBlank() && songArtist.isNotBlank()) {
                                    viewModel.addSong(
                                        title = songTitle,
                                        artist = songArtist,
                                        duration = songDuration,
                                        category = songCategory,
                                        coverColorHex = coverColorHex,
                                        synthPreset = synthPreset,
                                        coverUrl = coverUrl,
                                        releaseType = releaseType,
                                        isLatest = true
                                    )
                                    Toast.makeText(context, "Added $songTitle successfully!", Toast.LENGTH_SHORT).show()
                                    // Reset input values
                                    songTitle = ""
                                    songArtist = ""
                                    coverUrl = ""
                                    releaseType = "Single"
                                } else {
                                    Toast.makeText(context, "Please complete Title and Artist details", Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = accentColor),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(text = "PERSIST & COMPOSE TO LIBRARY", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // Section: Existing Database Song Lists manipulation
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF18181F))
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "REMOVE EXISTING TRACKS",
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            letterSpacing = 1.sp,
                            color = accentColor,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        songs.forEach { song ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(text = song.title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Text(text = song.artist, color = Color.Gray, fontSize = 11.sp)
                                }

                                IconButton(
                                    onClick = {
                                        viewModel.deleteSong(song.id)
                                        Toast.makeText(context, "Deleted ${song.title}", Toast.LENGTH_SHORT).show()
                                    }
                                ) {
                                    Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
