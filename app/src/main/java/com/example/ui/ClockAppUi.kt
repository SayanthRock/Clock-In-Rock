package com.example.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material3.*
import com.example.ui.theme.ThemedText as Text
import androidx.compose.runtime.*
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.composed
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.database.Alarm
import com.example.data.database.WorldClock
import com.example.sound.SoundSynthesizer
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.*

@Composable
fun GlassSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val trackWidth = 52.dp
    val trackHeight = 28.dp
    val thumbSize = 22.dp
    
    val transition = updateTransition(targetState = checked, label = "GlassSwitchState")
    
    val thumbOffset by transition.animateDp(
        transitionSpec = { spring(stiffness = Spring.StiffnessMediumLow, dampingRatio = Spring.DampingRatioNoBouncy) },
        label = "ThumbOffset"
    ) { checkedState ->
        if (checkedState) 26.dp else 4.dp
    }
    
    val accentColor = NeonLime
    val trackBgColor by transition.animateColor(label = "TrackBg") { checkedState ->
        if (checkedState) accentColor.copy(alpha = 0.35f) else Color.White.copy(alpha = 0.08f)
    }
    
    val thumbBgColor by transition.animateColor(label = "ThumbBg") { checkedState ->
        if (checkedState) accentColor else Color.White.copy(alpha = 0.85f)
    }

    val borderStrokeColor by transition.animateColor(label = "BorderColor") { checkedState ->
        if (checkedState) accentColor.copy(alpha = 0.6f) else Color.White.copy(alpha = 0.18f)
    }

    Box(
        modifier = modifier
            .size(trackWidth, trackHeight)
            .clip(CircleShape)
            .background(trackBgColor)
            .border(1.dp, borderStrokeColor, CircleShape)
            .clickable { onCheckedChange(!checked) },
        contentAlignment = Alignment.CenterStart
    ) {
        Box(
            modifier = Modifier
                .offset(x = thumbOffset)
                .size(thumbSize)
                .clip(CircleShape)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            thumbBgColor,
                            thumbBgColor.copy(alpha = 0.8f)
                        )
                    )
                )
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClockAppUi(viewModel: ClockViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
    val is24Hour by viewModel.is24HourFormat.collectAsStateWithLifecycle()
    val clockTime by viewModel.clockTime.collectAsStateWithLifecycle()
    val triggeredAlarm by viewModel.triggeredAlarm.collectAsStateWithLifecycle()
    val timerTriggered by viewModel.timerTriggered.collectAsStateWithLifecycle()

    var showAddAlarmDialog by remember { mutableStateOf(false) }
    var showSplash by remember { mutableStateOf(true) }

    BoxWithConstraints(modifier = Modifier.fillMaxSize().background(ObsidianMain)) {
        val isTablet = maxWidth > 600.dp
        Box(
            modifier = Modifier
                .fillMaxSize()
                .then(if (isTablet) Modifier.widthIn(max = 600.dp).align(Alignment.Center) else Modifier)
        ) {
    Scaffold(
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .padding(horizontal = 16.dp, vertical = 24.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .liquidGlassCard(viewModel, cornerRadius = 32.dp, borderColor = NeonLime)
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val screens = listOf(
                        ClockViewModel.Screen.CLOCK to Icons.Default.Schedule,
                        ClockViewModel.Screen.ALARM to Icons.Default.Alarm,
                        ClockViewModel.Screen.STOPWATCH to Icons.Default.Timer,
                        ClockViewModel.Screen.TIMER to Icons.Default.HourglassEmpty,
                        ClockViewModel.Screen.WORLD_CLOCK to Icons.Default.Language,
                        ClockViewModel.Screen.ABOUT to Icons.Default.Info
                    )
                    screens.forEach { (screen, icon) ->
                        val isSelected = currentScreen == screen
                        val label = screen.name.substringBefore("_").lowercase().replaceFirstChar { it.uppercase() }
                        
                        val activeColor = NeonLime
                        val itemBg = if (isSelected) activeColor.copy(alpha = 0.12f) else Color.Transparent
                        
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .then(if (isSelected) Modifier.border(1.dp, activeColor.copy(alpha = 0.3f), RoundedCornerShape(16.dp)) else Modifier)
                                .background(itemBg)
                                .clickable { viewModel.navigateTo(screen) }
                                .padding(horizontal = 8.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = label,
                                tint = if (isSelected) NeonLime else TextMuted
                            )
                            val enableAnimations by viewModel.enableAnimations.collectAsStateWithLifecycle()
                            if (enableAnimations) {
                                AnimatedVisibility(visible = isSelected) {
                                    Text(
                                        text = label,
                                        fontSize = 10.sp,
                                        color = NeonLime,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(top = 4.dp)
                                    )
                                }
                            } else {
                                if (isSelected) {
                                    Text(
                                        text = label,
                                        fontSize = 10.sp,
                                        color = NeonLime,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(top = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        floatingActionButton = {
            if (currentScreen == ClockViewModel.Screen.ALARM) {
                FloatingActionButton(
                    onClick = { showAddAlarmDialog = true },
                    containerColor = NeonLime,
                    contentColor = ObsidianMain,
                    elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 6.dp),
                    modifier = Modifier.testTag("add_alarm_fab")
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Alarm", modifier = Modifier.size(28.dp))
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(ObsidianMain)
                .padding(innerPadding)
        ) {
            val transitionStyle by viewModel.transitionStyle.collectAsStateWithLifecycle()
            val enableAnimations by viewModel.enableAnimations.collectAsStateWithLifecycle()
            
            val screensList = remember {
                listOf(
                    ClockViewModel.Screen.CLOCK,
                    ClockViewModel.Screen.ALARM,
                    ClockViewModel.Screen.STOPWATCH,
                    ClockViewModel.Screen.TIMER,
                    ClockViewModel.Screen.WORLD_CLOCK,
                    ClockViewModel.Screen.ABOUT
                )
            }
            val pagerState = rememberPagerState(
                initialPage = screensList.indexOf(currentScreen).coerceAtLeast(0),
                pageCount = { screensList.size }
            )

            // Sync from currentScreen state (e.g. bottom bar clicks) to pager
            LaunchedEffect(currentScreen) {
                val targetPage = screensList.indexOf(currentScreen)
                if (targetPage != -1 && pagerState.currentPage != targetPage) {
                    if (enableAnimations) {
                        pagerState.animateScrollToPage(
                            page = targetPage,
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessLow
                            )
                        )
                    } else {
                        pagerState.scrollToPage(targetPage)
                    }
                }
            }

            // Sync from pager swipes to currentScreen state
            LaunchedEffect(pagerState.currentPage) {
                val swipedScreen = screensList.getOrNull(pagerState.currentPage)
                if (swipedScreen != null && currentScreen != swipedScreen) {
                    viewModel.navigateTo(swipedScreen)
                }
            }

            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize(),
                userScrollEnabled = true
            ) { page ->
                val screen = screensList[page]
                val pageOffset = ((pagerState.currentPage - page) + pagerState.currentPageOffsetFraction)
                val absOffset = Math.abs(pageOffset)
                val densityBase = LocalDensity.current.density
                
                val pageModifier = if (enableAnimations) {
                    when (transitionStyle) {
                        "Flip" -> Modifier.graphicsLayer {
                            rotationY = pageOffset * -180f
                            alpha = (1f - absOffset).coerceIn(0f, 1f)
                            cameraDistance = 8 * densityBase
                        }
                        "Cube" -> Modifier.graphicsLayer {
                            rotationY = pageOffset * -90f
                            alpha = (1f - absOffset).coerceIn(0f, 1f)
                            cameraDistance = 8 * densityBase
                        }
                        "Card" -> Modifier.graphicsLayer {
                            val scale = 0.8f + (1f - absOffset).coerceIn(0f, 1f) * 0.2f
                            scaleX = scale
                            scaleY = scale
                            alpha = (1f - absOffset).coerceIn(0f, 1f)
                        }
                        "Tilt" -> Modifier.graphicsLayer {
                            rotationX = pageOffset * -45f
                            rotationY = pageOffset * 45f
                            val scale = 0.5f + (1f - absOffset).coerceIn(0f, 1f) * 0.5f
                            scaleX = scale
                            scaleY = scale
                            alpha = (1f - absOffset).coerceIn(0f, 1f)
                            cameraDistance = 8 * densityBase
                        }
                        "Roll" -> Modifier.graphicsLayer {
                            rotationZ = pageOffset * -360f
                            val scale = (1f - absOffset).coerceIn(0f, 1f)
                            scaleX = scale
                            scaleY = scale
                            alpha = (1f - absOffset).coerceIn(0f, 1f)
                        }
                        "Slide" -> Modifier.graphicsLayer {
                            translationX = pageOffset * size.width
                        }
                        else -> Modifier.graphicsLayer {
                            alpha = (1f - absOffset).coerceIn(0f, 1f)
                        }
                    }
                } else Modifier

                Box(modifier = Modifier.fillMaxSize().then(pageModifier)) {
                    when (screen) {
                        ClockViewModel.Screen.CLOCK -> ClockScreen(viewModel, clockTime, is24Hour)
                        ClockViewModel.Screen.ALARM -> AlarmScreen(viewModel, is24Hour)
                        ClockViewModel.Screen.STOPWATCH -> StopwatchScreen(viewModel)
                        ClockViewModel.Screen.TIMER -> TimerScreen(viewModel)
                        ClockViewModel.Screen.WORLD_CLOCK -> WorldClockScreen(viewModel)
                        ClockViewModel.Screen.ABOUT -> AboutScreen(viewModel, onTriggerDiagnostics = { showSplash = true })
                    }
                }
            }

            // Real Time Triggered Alarm Overlaid Alert
            triggeredAlarm?.let { alarm ->
                AlarmTriggeredOverlay(
                    viewModel = viewModel,
                    alarm = alarm,
                    is24Hour = is24Hour,
                    onDismiss = { viewModel.dismissTriggeredAlarm() },
                    onSnooze = { viewModel.snoozeTriggeredAlarm() }
                )
            }

            // Countdown Timer Triggered Overlaid Alert
            if (timerTriggered) {
                TimerTriggeredOverlay(viewModel = viewModel) {
                    viewModel.dismissTimerAlarm()
                }
            }

            // New Alarm Designer Modal
            if (showAddAlarmDialog) {
                AddAlarmDialog(
                    is24Hour = is24Hour,
                    viewModel = viewModel,
                    onDismiss = { showAddAlarmDialog = false },
                    onSave = { alarm ->
                        viewModel.addAlarm(alarm)
                        showAddAlarmDialog = false
                    }
                )
            }
        }
    }
            
            // Dynamic Island Overlay (Android 16 Feature Request)
            DynamicIsland(viewModel = viewModel, modifier = Modifier.align(Alignment.TopCenter))

            AnimatedVisibility(
                visible = showSplash,
                enter = fadeIn(),
                exit = fadeOut(animationSpec = tween(600)) + scaleOut(targetScale = 0.88f, animationSpec = tween(600))
            ) {
                CyberpunkSplashScreen(onDismiss = { showSplash = false })
            }
        }
    }
}

// =========================================================================================
// 1. CLOCK COMPONENT SCREEN
// =========================================================================================
@Composable
fun AnalogClockCanvas(
    time: ZonedDateTime,
    enableAnimations: Boolean,
    modifier: Modifier = Modifier,
    digits: List<String> = listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12")
) {
    val neonLimeColor = NeonLime
    val neonTealColor = NeonTeal
    val neonAmberColor = NeonAmber
    val textWhiteColor = TextWhite
    val obsidianCardColor = ObsidianCard

    // For smooth sweep, let's create a float representing the sub-second component
    // that animates smoothly from 0f to 1000f every second.
    // If animations are disabled, this remains strictly 0f (battery saver mode).
    val animatedMilli = if (enableAnimations) {
        val infiniteTransition = rememberInfiniteTransition(label = "millisecond")
        infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 1000f,
            animationSpec = infiniteRepeatable(
                animation = tween(1000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "ms"
        ).value
    } else {
        0f
    }

    val secondFraction = time.second + (animatedMilli / 1000f)
    val minuteFraction = time.minute + (secondFraction / 60f)
    val hourFraction = (time.hour % 12) + (minuteFraction / 60f)

    Canvas(modifier = modifier) {
        val radius = size.minDimension / 2
        val center = androidx.compose.ui.geometry.Offset(size.width / 2, size.height / 2)

        // Outer rim
        drawCircle(
            color = neonLimeColor.copy(alpha = 0.2f),
            radius = radius,
            style = Stroke(width = 4.dp.toPx())
        )

        val paint = android.graphics.Paint().apply {
            color = android.graphics.Color.argb(
                (textWhiteColor.alpha * 255).toInt(),
                (textWhiteColor.red * 255).toInt(),
                (textWhiteColor.green * 255).toInt(),
                (textWhiteColor.blue * 255).toInt()
            )
            textSize = 11.dp.toPx()
            textAlign = android.graphics.Paint.Align.CENTER
            typeface = android.graphics.Typeface.create(android.graphics.Typeface.MONOSPACE, android.graphics.Typeface.BOLD)
            isAntiAlias = true
        }

        // Hour Ticks and Alphabet Label marks around the clock face
        for (i in 0 until 12) {
            val angle = Math.toRadians((i * 30 - 90).toDouble())
            val tickStart = androidx.compose.ui.geometry.Offset(
                x = center.x + (radius - 8.dp.toPx()) * Math.cos(angle).toFloat(),
                y = center.y + (radius - 8.dp.toPx()) * Math.sin(angle).toFloat()
            )
            val tickEnd = androidx.compose.ui.geometry.Offset(
                x = center.x + radius * Math.cos(angle).toFloat(),
                y = center.y + radius * Math.sin(angle).toFloat()
            )
            drawLine(
                color = if (i % 3 == 0) neonLimeColor else textWhiteColor.copy(alpha = 0.4f),
                start = tickStart,
                end = tickEnd,
                strokeWidth = if (i % 3 == 0) 2.5.dp.toPx() else 1.2.dp.toPx()
            )

            // Draw alphabet markings
            if (digits.size == 12) {
                val symbolIndex = if (i == 0) 11 else (i - 1)
                val symbol = digits[symbolIndex]
                val textRadius = radius - 18.dp.toPx()
                val textX = center.x + textRadius * Math.cos(angle).toFloat()
                val textY = center.y + textRadius * Math.sin(angle).toFloat() + 3.5.dp.toPx() // slight offset down for vertical centering
                
                // Highlight quarterly letters using NeonLime
                paint.color = if (i % 3 == 0) {
                    android.graphics.Color.argb(
                        (neonLimeColor.alpha * 255).toInt(),
                        (neonLimeColor.red * 255).toInt(),
                        (neonLimeColor.green * 255).toInt(),
                        (neonLimeColor.blue * 255).toInt()
                    )
                } else {
                    android.graphics.Color.argb(
                        (textWhiteColor.alpha * 180).toInt(),
                        (textWhiteColor.red * 255).toInt(),
                        (textWhiteColor.green * 255).toInt(),
                        (textWhiteColor.blue * 255).toInt()
                    )
                }

                drawContext.canvas.nativeCanvas.drawText(
                    symbol,
                    textX,
                    textY,
                    paint
                )
            }
        }

        // Calculate Hand angles
        val hrAngle = Math.toRadians((hourFraction * 30 - 90).toDouble())
        val minAngle = Math.toRadians((minuteFraction * 6 - 90).toDouble())
        val secAngle = Math.toRadians((secondFraction * 6 - 90).toDouble())

        // Hour Hand
        drawLine(
            color = neonLimeColor,
            start = center,
            end = androidx.compose.ui.geometry.Offset(
                x = center.x + (radius * 0.55f) * Math.cos(hrAngle).toFloat(),
                y = center.y + (radius * 0.55f) * Math.sin(hrAngle).toFloat()
            ),
            strokeWidth = 6.dp.toPx(),
            cap = StrokeCap.Round
        )

        // Minute Hand
        drawLine(
            color = textWhiteColor,
            start = center,
            end = androidx.compose.ui.geometry.Offset(
                x = center.x + (radius * 0.78f) * Math.cos(minAngle).toFloat(),
                y = center.y + (radius * 0.78f) * Math.sin(minAngle).toFloat()
            ),
            strokeWidth = 3.5.dp.toPx(),
            cap = StrokeCap.Round
        )

        // Second Hand
        drawLine(
            color = neonAmberColor,
            start = center,
            end = androidx.compose.ui.geometry.Offset(
                x = center.x + (radius * 0.88f) * Math.cos(secAngle).toFloat(),
                y = center.y + (radius * 0.88f) * Math.sin(secAngle).toFloat()
            ),
            strokeWidth = 1.5.dp.toPx(),
            cap = StrokeCap.Round
        )

        // Center hub
        drawCircle(
            color = obsidianCardColor,
            radius = 6.dp.toPx()
        )
        drawCircle(
            color = neonAmberColor,
            radius = 3.dp.toPx()
        )
    }
}

@Composable
fun EpochIndexClockView() {
    val epochTimeState = produceState(initialValue = Triple(0L, 0.0, 0.0)) {
        while (true) {
            val nowMs = System.currentTimeMillis()
            val jEpochStartMs = 631152000000L // January 1, 1990 00:00:00 UTC
            val diffMs = nowMs - jEpochStartMs
            val elapsedSecs = diffMs / 1000.0
            
            val uptimeMs = android.os.SystemClock.elapsedRealtime()
            
            value = Triple(diffMs, elapsedSecs, uptimeMs.toDouble())
            delay(40L)
        }
    }

    val (diffMs, elapsedSecs, uptimeMs) = epochTimeState.value

    // Format fields
    val totalSecsFormatted = String.format(Locale.US, "%,15.2f", elapsedSecs)
    
    // JEPOCH calculations
    val daysCount = diffMs / 86400000L
    val totalHours = diffMs / 3600000L
    val totalMinutes = diffMs / 60000L

    // Uptime conversion
    val upDays = (uptimeMs / (24 * 3600 * 1000)).toLong()
    val upHrs = ((uptimeMs % (24 * 3600 * 1000)) / (3600 * 1000)).toLong()
    val upMins = ((uptimeMs % (3600 * 1000)) / (60 * 1000)).toLong()
    val upSecs = ((uptimeMs % (60 * 1000)) / 1000.0)

    val uptimeFormatted = if (upDays > 0) {
        String.format(Locale.US, "%dd %02dh %02dm %04.1fs", upDays, upHrs, upMins, upSecs)
    } else {
        String.format(Locale.US, "%02dh %02dm %04.1fs", upHrs, upMins, upSecs)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp, horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Main Display Panel
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, NeonTeal.copy(alpha = 0.35f), RoundedCornerShape(24.dp)),
            colors = CardDefaults.cardColors(containerColor = ObsidianSurface.copy(alpha = 0.45f)),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "RUNNING ELAPSED TIME INDEX",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    color = NeonTeal,
                    letterSpacing = 1.8.sp
                )
                
                Spacer(modifier = Modifier.height(14.dp))
                
                // Massive glowing live running second.ms
                Text(
                    text = totalSecsFormatted,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    fontFamily = FontFamily.Monospace,
                    color = NeonLime,
                    letterSpacing = 0.5.sp,
                    modifier = Modifier.padding(vertical = 4.dp)
                )

                Text(
                    text = "ELAPSED SECONDS.MS (SINCE JAN 1, 1990)",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextMuted,
                    letterSpacing = 1.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Grid Layout for Stats: Daycount, Hours, Minutes, Uptime
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // JEPOCH Daycount card
            Card(
                modifier = Modifier
                    .weight(1f)
                    .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = ObsidianSurface.copy(alpha = 0.3f)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "JEPOCH DAYCOUNT",
                        fontSize = 8.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = NeonTeal,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = String.format(Locale.US, "%,d days", daysCount),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Black,
                        color = TextWhite,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            // Sys Uptime card
            Card(
                modifier = Modifier
                    .weight(1f)
                    .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = ObsidianSurface.copy(alpha = 0.3f)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "SYS UPTIME",
                        fontSize = 8.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = NeonTeal,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = uptimeFormatted,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = NeonAmber,
                        fontFamily = FontFamily.Monospace,
                        maxLines = 1
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Total Hours card
            Card(
                modifier = Modifier
                    .weight(1f)
                    .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = ObsidianSurface.copy(alpha = 0.3f)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "TOTAL HOURS",
                        fontSize = 8.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = NeonTeal,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = String.format(Locale.US, "%,d hrs", totalHours),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Black,
                        color = TextWhite,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            // Total Minutes card
            Card(
                modifier = Modifier
                    .weight(1f)
                    .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = ObsidianSurface.copy(alpha = 0.3f)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "TOTAL MINUTES",
                        fontSize = 8.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = NeonTeal,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = String.format(Locale.US, "%,d mins", totalMinutes),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        color = TextWhite,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // TIME WILL TELL - Automatic Greeting & Poetic Theme Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = 1.5.dp,
                    color = NeonTeal.copy(alpha = 0.35f),
                    shape = RoundedCornerShape(20.dp)
                ),
            colors = CardDefaults.cardColors(
                containerColor = ObsidianSurface.copy(alpha = 0.45f)
            ),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "TIME WILL TELL",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 2.sp
                    ),
                    color = NeonLime,
                    modifier = Modifier.padding(bottom = 6.dp)
                )

                val currentHour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
                val autoGreeting = when (currentHour) {
                    in 5..11 -> "GOOD MORNING IN THE MORNING"
                    in 12..16 -> "GOOD AFTERNOON"
                    in 17..20 -> "GOOD EVENING"
                    else -> "GOOD NIGHT AT NIGHT"
                }

                Text(
                    text = autoGreeting,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextWhite,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Text(
                    text = "Improvement: All problems will be solved.",
                    fontSize = 12.sp,
                    color = TextMuted,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun ClockScreen(viewModel: ClockViewModel, time: ZonedDateTime, is24Hour: Boolean) {
    val clockStyle by viewModel.clockStyle.collectAsStateWithLifecycle()
    val enableAnimations by viewModel.enableAnimations.collectAsStateWithLifecycle()
    val enableTactileFeedback by viewModel.enableTactileFeedback.collectAsStateWithLifecycle()
    val activeAlphabet by viewModel.activeAlphabet.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Good Morning / Good Night Greeting
        val greeting = when (time.hour) {
            in 5..11 -> "GOOD MORNING IN THE MORNING"
            in 12..16 -> "GOOD AFTERNOON"
            in 17..20 -> "GOOD EVENING"
            else -> "GOOD NIGHT AT NIGHT"
        }
        
        Text(
            text = greeting,
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 2.sp
            ),
            color = TextWhite,
            modifier = Modifier.padding(bottom = 6.dp)
        )

        // Aesthetic Clock Style Selector Tabs (Mode Selector)
        Row(
            modifier = Modifier
                .background(ObsidianCard, RoundedCornerShape(32.dp))
                .border(1.dp, NeonLime.copy(alpha = 0.15f), RoundedCornerShape(32.dp))
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            listOf("2026 Mode", "Digital", "Analog", "Epoch Index").forEach { style ->
                val selected = clockStyle == style
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(24.dp))
                        .background(if (selected) NeonLime else Color.Transparent)
                        .tactileClick(enabled = enableTactileFeedback) { viewModel.setClockStyle(style) }
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = style.uppercase(),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.5.sp,
                        color = if (selected) ObsidianMain else TextMuted
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Time format label indicator
        Text(
            text = if (is24Hour) "24-HOUR FORMAT" else "12-HOUR FORMAT",
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 1.sp
            ),
            color = TextMuted,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Body Display according to selected clock style
        when (clockStyle) {
            "Epoch Index" -> {
                EpochIndexClockView()
            }
            "Analog" -> {
                Box(
                    modifier = Modifier
                        .size(240.dp)
                        .padding(12.dp)
                        .background(ObsidianSurface, CircleShape)
                        .border(1.dp, NeonLime.copy(alpha = 0.1f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    AnalogClockCanvas(
                        time = time,
                        enableAnimations = enableAnimations,
                        modifier = Modifier.fillMaxSize().padding(16.dp),
                        digits = activeAlphabet.digits
                    )
                }
            }
            "Digital" -> {
                // Digital Display Card Container
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(2.dp, NeonLime.copy(alpha = 0.3f), RoundedCornerShape(24.dp)),
                    colors = CardDefaults.cardColors(containerColor = ObsidianSurface),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        val hours = if (is24Hour) {
                            String.format("%02d", time.hour)
                        } else {
                            val h = time.hour % 12
                            String.format("%02d", if (h == 0) 12 else h)
                        }
                        val minutes = String.format("%02d", time.minute)
                        val seconds = String.format("%02d", time.second)
                        val amPm = if (!is24Hour) (if (time.hour >= 12) "PM" else "AM") else ""

                        Row(
                            verticalAlignment = Alignment.Bottom,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            // Hour Box
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = hours,
                                    fontSize = 52.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    color = NeonLime
                                )
                                Text("HOURS", fontSize = 9.sp, color = TextMuted, fontWeight = FontWeight.Bold)
                            }

                            Text(
                                text = ":",
                                fontSize = 44.sp,
                                fontWeight = FontWeight.Bold,
                                color = NeonLime.copy(alpha = 0.6f),
                                modifier = Modifier.padding(start = 2.dp, end = 2.dp, bottom = 4.dp)
                            )

                            // Minute Box
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = minutes,
                                    fontSize = 52.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    color = NeonLime
                                )
                                Text("MINUTES", fontSize = 9.sp, color = TextMuted, fontWeight = FontWeight.Bold)
                            }

                            Text(
                                text = ":",
                                fontSize = 44.sp,
                                fontWeight = FontWeight.Bold,
                                color = NeonLime.copy(alpha = 0.6f),
                                modifier = Modifier.padding(start = 2.dp, end = 2.dp, bottom = 4.dp)
                            )

                            // Second Box
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = seconds,
                                    fontSize = 52.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    color = NeonTeal
                                )
                                Text("SECONDS", fontSize = 9.sp, color = TextMuted, fontWeight = FontWeight.Bold)
                            }

                            if (amPm.isNotEmpty()) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = amPm,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = NeonAmber,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )
                            }
                        }
                    }
                }
            }
            else -> {
                // "2026 Mode" - Ultimate Hybrid Design with dynamic progress trails
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Pulsing Glow Container
                    val scaleFactor = if (enableAnimations) {
                        val infiniteTransition = rememberInfiniteTransition(label = "pulseGlow")
                        infiniteTransition.animateFloat(
                            initialValue = 0.98f,
                            targetValue = 1.03f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(1500, easing = LinearOutSlowInEasing),
                                repeatMode = RepeatMode.Reverse
                            ),
                            label = "scale"
                        ).value
                    } else {
                        1.0f
                    }

                    Box(
                        modifier = Modifier
                            .size(190.dp)
                            .scale(scaleFactor)
                            .background(ObsidianSurface, CircleShape)
                            .border(width = 2.dp, color = NeonLime, shape = CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        AnalogClockCanvas(
                            time = time,
                            enableAnimations = enableAnimations,
                            modifier = Modifier.fillMaxSize().padding(14.dp),
                            digits = activeAlphabet.digits
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Embedded Modern Digital Box
                    val hours = if (is24Hour) {
                        String.format("%02d", time.hour)
                    } else {
                        val h = time.hour % 12
                        String.format("%02d", if (h == 0) 12 else h)
                    }
                    val minutes = String.format("%02d", time.minute)
                    val seconds = String.format("%02d", time.second)
                    val amPm = if (!is24Hour) (if (time.hour >= 12) "PM" else "AM") else ""

                    Row(
                        modifier = Modifier
                            .background(ObsidianSurface, RoundedCornerShape(16.dp))
                            .border(1.dp, NeonTeal.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "$hours:$minutes",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.ExtraBold,
                            fontFamily = FontFamily.Monospace,
                            color = NeonLime,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = seconds,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = NeonTeal
                        )
                        if (amPm.isNotEmpty()) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = amPm,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = NeonAmber
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Date Display
        val dtfDay = DateTimeFormatter.ofPattern("EEEE", Locale.getDefault())
        val dtfDateStr = DateTimeFormatter.ofPattern("MMMM dd, yyyy", Locale.getDefault())

        Text(
            text = time.format(dtfDay).uppercase(),
            fontSize = 24.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 2.sp,
            color = TextWhite
        )

        Spacer(modifier = Modifier.height(2.dp))

        Text(
            text = time.format(dtfDateStr).uppercase(),
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            letterSpacing = 1.sp,
            color = NeonTeal
        )

        Spacer(modifier = Modifier.height(28.dp))

        // Toggle Time Format Button
        val formatBtnInteraction = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
        Button(
            onClick = { viewModel.toggleTimeFormat() },
            interactionSource = formatBtnInteraction,
            colors = ButtonDefaults.buttonColors(
                containerColor = ObsidianCard,
                contentColor = NeonLime
            ),
            border = androidx.compose.foundation.BorderStroke(1.dp, NeonLime.copy(alpha = 0.4f)),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .height(44.dp)
                .tactilePress(enabled = enableTactileFeedback, interactionSource = formatBtnInteraction)
                .testTag("toggle_format_button")
        ) {
            Icon(Icons.Default.SwapHoriz, contentDescription = "Toggle Format")
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                "SWITCH TO " + if (is24Hour) "12-HOUR FORMAT" else "24-HOUR FORMAT",
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                fontSize = 11.sp
            )
        }
    }
}

// =========================================================================================
// 2. ALARM COMPONENT SCREEN
// =========================================================================================
@Composable
fun AlarmScreen(viewModel: ClockViewModel, is24Hour: Boolean) {
    val alarms by viewModel.alarms.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // App header bar
        Text(
            text = "MY ALARMS",
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            ),
            color = NeonLime,
            modifier = Modifier.padding(start = 8.dp, bottom = 16.dp, top = 8.dp)
        )

        // Core Feature Requirement: Alarm Presets Section
        AlarmPresetsSection(viewModel = viewModel)

        if (alarms.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.NotificationsOff,
                        contentDescription = "No Alarms",
                        modifier = Modifier.size(72.dp),
                        tint = TextMuted
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No Alarms set",
                        color = TextWhite,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Press the + Button to create a customized sound alarm",
                        color = TextMuted,
                        textAlign = TextAlign.Center,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(alarms, key = { it.id }) { alarm ->
                    val enableAnimations by viewModel.enableAnimations.collectAsStateWithLifecycle()
                    AlarmItemCard(
                        alarm = alarm,
                        is24Hour = is24Hour,
                        viewModel = viewModel,
                        onToggle = { viewModel.toggleAlarmActive(alarm) },
                        onDelete = { viewModel.deleteAlarm(alarm) },
                        modifier = if (enableAnimations) Modifier.animateItem() else Modifier
                    )
                }
            }
        }
    }
}

@Composable
fun AlarmPresetsSection(viewModel: ClockViewModel) {
    val presets by viewModel.alarmPresets.collectAsStateWithLifecycle()
    val alarms by viewModel.alarms.collectAsStateWithLifecycle()
    val playingPresetId by viewModel.playingPresetId.collectAsStateWithLifecycle()
    var showSaveDialog by remember { mutableStateOf(false) }
    var presetNameToSave by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "PRESET SCHEDULES",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = TextMuted,
                letterSpacing = 1.sp
            )
            
            TextButton(
                onClick = {
                    presetNameToSave = "Schedule ${presets.size + 1}"
                    showSaveDialog = true
                },
                colors = ButtonDefaults.textButtonColors(contentColor = NeonTeal),
                modifier = Modifier.testTag("save_current_preset_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Save,
                    contentDescription = "Save active",
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("SAVE ACTIVE SCHEDULE", fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
            }
        }

        if (presets.isEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp)
                    .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(12.dp)),
                colors = CardDefaults.cardColors(containerColor = ObsidianSurface.copy(alpha = 0.25f)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Presets Info",
                        tint = NeonTeal.copy(alpha = 0.8f),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Save your current list of alarms as a preset schedule to switch quickly later.",
                        fontSize = 12.sp,
                        color = TextMuted
                    )
                }
            }
        } else {
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(horizontal = 8.dp)
            ) {
                items(presets, key = { it.id }) { preset ->
                    val alarmCount = viewModel.getAlarmsCountInPreset(preset)
                    val isPresetSelected = preset.isActive
                    Card(
                        modifier = Modifier
                            .width(175.dp)
                            .border(
                                width = 1.5.dp,
                                color = if (isPresetSelected) NeonTeal else Color.White.copy(alpha = 0.12f),
                                shape = RoundedCornerShape(14.dp)
                            )
                            .clickable { viewModel.applyAlarmPreset(preset) }
                            .testTag("preset_card_${preset.id}"),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isPresetSelected) ObsidianSurface.copy(alpha = 0.45f) else ObsidianCard.copy(alpha = 0.4f)
                        ),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth().padding(end = 18.dp)
                                ) {
                                    if (isPresetSelected) {
                                        Box(
                                            modifier = Modifier
                                                .size(6.dp)
                                                .clip(CircleShape)
                                                .background(NeonTeal)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                    }
                                    Text(
                                        text = preset.name.uppercase(),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isPresetSelected) NeonTeal else TextWhite,
                                        maxLines = 1,
                                        letterSpacing = 0.5.sp
                                    )
                                }
                                
                                Spacer(modifier = Modifier.height(4.dp))
                                
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = if (alarmCount == 1) "1 active alarm" else "$alarmCount active alarms",
                                            fontSize = 9.sp,
                                            color = TextMuted,
                                            fontWeight = FontWeight.Medium
                                        )
                                        Text(
                                            text = "DIAGNOSTIC TEST",
                                            fontSize = 8.sp,
                                            color = if (playingPresetId == preset.id) NeonTeal else TextMuted.copy(alpha = 0.6f),
                                            fontWeight = FontWeight.Bold,
                                            letterSpacing = 0.5.sp
                                        )
                                    }

                                    val isTesting = playingPresetId == preset.id
                                    IconButton(
                                        onClick = { viewModel.testAlarmPreset(preset) },
                                        modifier = Modifier
                                            .size(28.dp)
                                            .background(
                                                if (isTesting) NeonTeal.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.05f),
                                                CircleShape
                                            )
                                            .testTag("preset_test_button_${preset.id}")
                                    ) {
                                        Icon(
                                            imageVector = if (isTesting) Icons.Default.Stop else Icons.Default.PlayArrow,
                                            contentDescription = "Test preset sound sample",
                                            tint = if (isTesting) NeonTeal else TextWhite,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }
                            }
                            
                            IconButton(
                                onClick = { viewModel.deleteAlarmPreset(preset) },
                                modifier = Modifier
                                    .size(28.dp)
                                    .align(Alignment.TopEnd)
                                    .padding(top = 4.dp, end = 4.dp)
                                    .testTag("preset_delete_${preset.id}")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Delete preset",
                                    tint = TextMuted.copy(alpha = 0.6f),
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showSaveDialog) {
        Dialog(onDismissRequest = { showSaveDialog = false }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(20.dp)),
                colors = CardDefaults.cardColors(containerColor = ObsidianCard.copy(alpha = 0.95f)),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "SAVE PRESET SCHEDULE",
                        fontWeight = FontWeight.Bold,
                        color = NeonTeal,
                        fontSize = 14.sp,
                        letterSpacing = 1.sp
                    )

                    Text(
                        text = "This will capture all ${alarms.size} current active alarms into a named preset so you can easily toggle back to it later.",
                        fontSize = 13.sp,
                        color = TextWhite
                    )

                    OutlinedTextField(
                        value = presetNameToSave,
                        onValueChange = { presetNameToSave = it },
                        label = { Text("Preset Schedule Name", color = TextMuted) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonTeal,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                            focusedLabelColor = NeonTeal,
                            unfocusedLabelColor = TextMuted,
                            focusedTextColor = TextWhite,
                            unfocusedTextColor = TextWhite
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("preset_name_input")
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(
                            onClick = { showSaveDialog = false }
                        ) {
                            Text("CANCEL", color = TextMuted, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (presetNameToSave.isNotBlank()) {
                                    viewModel.createPresetFromCurrentAlarms(presetNameToSave.trim())
                                    showSaveDialog = false
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = NeonTeal, contentColor = ObsidianMain),
                            modifier = Modifier.testTag("submit_preset_button")
                        ) {
                            Text("SAVE PRESET", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AlarmItemCard(
    alarm: Alarm,
    is24Hour: Boolean,
    viewModel: ClockViewModel,
    onToggle: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .liquidGlassCard(viewModel, cornerRadius = 16.dp, borderColor = if (alarm.isEnabled) NeonLime else Color.White),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (alarm.isEnabled) Icons.Default.NotificationsActive else Icons.Default.NotificationsNone,
                        contentDescription = "Alarm state",
                        tint = if (alarm.isEnabled) NeonLime else TextMuted,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = alarm.getFormattedTime(is24Hour),
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (alarm.isEnabled) TextWhite else TextMuted
                        )
                        if (alarm.label.isNotEmpty()) {
                            Text(
                                text = alarm.label.uppercase(),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (alarm.isEnabled) NeonTeal else TextMuted,
                                letterSpacing = 1.sp
                            )
                        }
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    GlassSwitch(
                        checked = alarm.isEnabled,
                        onCheckedChange = { onToggle() },
                        modifier = Modifier.testTag("alarm_switch_${alarm.id}")
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.testTag("alarm_delete_${alarm.id}")
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Delete alarm",
                            tint = TextWarning.copy(alpha = 0.8f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Sub info block regarding repeats & sound properties
            HorizontalDivider(color = ObsidianCard, thickness = 1.dp)
            
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Repeating days summary
                val daysList = listOf("M", "T", "W", "T", "F", "S", "S")
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    daysList.forEachIndexed { idx, day ->
                        val isDayRepeat = alarm.daysOfWeek[idx] == '1'
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(if (isDayRepeat) NeonLime.copy(alpha = 0.15f) else Color.Transparent)
                                .border(
                                    1.dp,
                                    if (isDayRepeat) NeonLime.copy(alpha = 0.5f) else ObsidianCard,
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = day,
                                fontSize = 10.sp,
                                fontWeight = if (isDayRepeat) FontWeight.Bold else FontWeight.Normal,
                                color = if (isDayRepeat) NeonLime else TextMuted
                            )
                        }
                    }
                }

                // Synth settings visual indicator
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        Icons.Default.MusicNote,
                        contentDescription = "Synth Alert Details",
                        tint = NeonAmber,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "${alarm.waveType} • ${alarm.frequency.toInt()}Hz",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextMuted
                    )
                }
            }
        }
    }
}

// =========================================================================================
// 3. STOPWATCH COMPONENT SCREEN
// =========================================================================================
@Composable
fun StopwatchScreen(viewModel: ClockViewModel) {
    val elapsedMs by viewModel.stopwatchElapsedTimeMs.collectAsStateWithLifecycle()
    val running by viewModel.stopwatchRunning.collectAsStateWithLifecycle()
    val laps by viewModel.stopwatchLaps.collectAsStateWithLifecycle()

    // Formatter structure (Minutes : Seconds : Millis)
    val minutesVal = (elapsedMs / 60000)
    val secondsVal = (elapsedMs / 1000) % 60
    val millisVal = (elapsedMs / 10) % 100

    val timeString = String.format("%02d:%02d", minutesVal, secondsVal)
    val milliString = String.format(".%02d", millisVal)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "STOPWATCH",
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            ),
            color = NeonLime,
            modifier = Modifier.padding(vertical = 12.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Large high-precision timer indicator
        Box(
            modifier = Modifier
                .size(240.dp)
                .clip(CircleShape)
                .background(ObsidianSurface)
                .border(2.dp, NeonLime.copy(alpha = 0.3f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            // Draw a subtle animated ticking circle using Canvas
            val secondsRotationPattern = (elapsedMs % 60000) / 60000f * 360f
            val neonLimeColor = NeonLime
            val neonTealColor = NeonTeal
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawArc(
                    brush = Brush.sweepGradient(listOf(neonLimeColor, neonTealColor, neonLimeColor)),
                    startAngle = 270f,
                    sweepAngle = secondsRotationPattern,
                    useCenter = false,
                    style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = timeString,
                        fontSize = 46.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = TextWhite
                    )
                    Text(
                        text = milliString,
                        fontSize = 24.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = NeonTeal
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "MINS : SECS . CS",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextMuted,
                    letterSpacing = 1.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Controlling actions
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Lap / Reset button
            Button(
                onClick = {
                    if (running) viewModel.lapStopwatch() else viewModel.resetStopwatch()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = ObsidianCard,
                    contentColor = TextWhite
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 8.dp)
                    .testTag("stopwatch_secondary_button")
            ) {
                Icon(
                    imageVector = if (running) Icons.Default.Flag else Icons.Default.Refresh,
                    contentDescription = if (running) "Lap" else "Reset"
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (running) "LAP" else "RESET", fontWeight = FontWeight.Bold)
            }

            // Power Play/Pause trigger
            Button(
                onClick = {
                    if (running) viewModel.pauseStopwatch() else viewModel.startStopwatch()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (running) TextWarning else NeonLime,
                    contentColor = ObsidianMain
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 8.dp)
                    .testTag("stopwatch_primary_button")
            ) {
                Icon(
                    imageVector = if (running) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (running) "Pause" else "Start"
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (running) "PAUSE" else "START", fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Laps listings
        Text(
            text = "LAPS",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = TextMuted,
            letterSpacing = 2.sp,
            modifier = Modifier
                .align(Alignment.Start)
                .padding(start = 8.dp, bottom = 4.dp)
        )

        HorizontalDivider(color = ObsidianCard, thickness = 1.dp)

        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            items(count = laps.size, key = { laps.size - it }) { index ->
                val enableAnimations by viewModel.enableAnimations.collectAsStateWithLifecycle()
                val lapTimeMs = laps[index]
                val lapMin = (lapTimeMs / 60000)
                val lapSec = (lapTimeMs / 1000) % 60
                val lapMil = (lapTimeMs / 10) % 100

                val inverseIndex = laps.size - index

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .liquidGlassCard(viewModel, cornerRadius = 12.dp, borderColor = NeonLime.copy(alpha = 0.5f))
                        .padding(vertical = 8.dp, horizontal = 12.dp)
                        .then(if (enableAnimations) Modifier.animateItem() else Modifier)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "LAP $inverseIndex",
                            fontWeight = FontWeight.Bold,
                            color = NeonTeal,
                            fontSize = 13.sp
                        )
                        Text(
                            text = String.format("%02d:%02d.%02d", lapMin, lapSec, lapMil),
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.SemiBold,
                            color = TextWhite,
                            fontSize = 15.sp
                        )
                    }
                }
            }
        }
    }
}

// =========================================================================================
// 4. TIMER COMPONENT SCREEN
// =========================================================================================
@Composable
fun TimerScreen(viewModel: ClockViewModel) {
    val totalSecs by viewModel.timerDurationSecs.collectAsStateWithLifecycle()
    val timeLeftSecs by viewModel.timerTimeLeftSecs.collectAsStateWithLifecycle()
    val running by viewModel.timerRunning.collectAsStateWithLifecycle()
    val enableTactileFeedback by viewModel.enableTactileFeedback.collectAsStateWithLifecycle()

    var inputHours by remember { mutableStateOf(0) }
    var inputMinutes by remember { mutableStateOf(0) }
    var inputSeconds by remember { mutableStateOf(0) }

    // Toggle panel for sound customizations
    var showSoundCustomizer by remember { mutableStateOf(false) }

    val waveType by viewModel.globalWaveType.collectAsStateWithLifecycle()
    val frequency by viewModel.globalFrequency.collectAsStateWithLifecycle()
    val pulseSpeed by viewModel.globalPulseSpeed.collectAsStateWithLifecycle()
    val vibratoDepth by viewModel.globalVibratoDepth.collectAsStateWithLifecycle()
    val vibratoSpeed by viewModel.globalVibratoSpeed.collectAsStateWithLifecycle()
    val isPreviewPlaying by viewModel.isPreviewPlaying.collectAsStateWithLifecycle()

    val totalInputSeconds = inputHours * 3600 + inputMinutes * 60 + inputSeconds

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "COUNTDOWN TIMER",
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            ),
            color = NeonLime,
            modifier = Modifier.padding(vertical = 12.dp)
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                Spacer(modifier = Modifier.height(16.dp))

                if (totalSecs == 0 || (timeLeftSecs <= 0 && !running)) {
                    // 1. Selector wheel layout when TIMER IS READY TO SETUP
                    Text(
                        "SET DURATION",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextMuted,
                        letterSpacing = 1.sp,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        // Hour Selector Card
                        TimerUnitPicker("HOURS", inputHours, 0, 23, enableTactileFeedback = enableTactileFeedback, viewModel = viewModel) { inputHours = it }
                        // Minute Selector Card
                        TimerUnitPicker("MINS", inputMinutes, 0, 59, enableTactileFeedback = enableTactileFeedback, viewModel = viewModel) { inputMinutes = it }
                        // Second Selector Card
                        TimerUnitPicker("SECS", inputSeconds, 0, 59, enableTactileFeedback = enableTactileFeedback, viewModel = viewModel) { inputSeconds = it }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Preset buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(60 to "1 MIN", 300 to "5 MIN", 900 to "15 MIN", 1800 to "30 MIN").forEach { (sec, label) ->
                            val presetInteraction = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
                            Button(
                                onClick = {
                                    inputHours = sec / 3600
                                    inputMinutes = (sec % 3600) / 60
                                    inputSeconds = sec % 60
                                },
                                interactionSource = presetInteraction,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = ObsidianCard,
                                    contentColor = NeonTeal
                                ),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .tactilePress(enabled = enableTactileFeedback, interactionSource = presetInteraction)
                            ) {
                                Text(label, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    // Launch setup button
                    val startTimerInteraction = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
                    Button(
                        onClick = {
                            if (totalInputSeconds > 0) {
                                viewModel.setTimerDuration(totalInputSeconds)
                                viewModel.startTimer()
                            }
                        },
                        interactionSource = startTimerInteraction,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = NeonLime,
                            contentColor = ObsidianMain
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .tactilePress(enabled = enableTactileFeedback, interactionSource = startTimerInteraction)
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = "Start Timer")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("START TIMER", fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                    }

                } else {
                    // 2. Active countdown display
                    val displayHours = timeLeftSecs / 3600
                    val displayMins = (timeLeftSecs % 3600) / 60
                    val displaySecs = timeLeftSecs % 60

                    val activeTimeString = String.format("%02d:%02d:%02d", displayHours, displayMins, displaySecs)
                    val percent = if (totalSecs > 0) timeLeftSecs.toFloat() / totalSecs.toFloat() else 0f

                    val animatedPercent by animateFloatAsState(
                        targetValue = percent,
                        animationSpec = if (running) {
                            tween(durationMillis = 1000, easing = LinearEasing)
                        } else {
                            spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMedium)
                        },
                        label = "timerProgress"
                    )

                    val obsidianCardColor = ObsidianCard
                    val neonTealColor = NeonTeal
                    val neonLimeColor = NeonLime
                    val textWhiteColor = TextWhite
                    Box(
                        modifier = Modifier.size(220.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val center = androidx.compose.ui.geometry.Offset(size.width / 2, size.height / 2)
                            val radiusPx = size.minDimension / 2

                            // Secondary background ring
                            drawCircle(
                                color = obsidianCardColor,
                                radius = radiusPx,
                                style = Stroke(width = 6.dp.toPx())
                            )
                            // Animated neon countdown progress draw
                            drawArc(
                                brush = Brush.sweepGradient(listOf(neonTealColor, neonLimeColor, neonTealColor)),
                                startAngle = 270f,
                                sweepAngle = -360f * animatedPercent,
                                useCenter = false,
                                style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round)
                            )
                            
                            // Glowing indicator thumb dot at the end of the progress arc
                            val angleRad = (Math.toRadians(270.0 - 360.0 * animatedPercent)).toFloat()
                            val thumbX = center.x + kotlin.math.cos(angleRad) * radiusPx
                            val thumbY = center.y + kotlin.math.sin(angleRad) * radiusPx
                            
                            // Glowing halo
                            drawCircle(
                                color = neonTealColor.copy(alpha = 0.4f),
                                radius = 10.dp.toPx(),
                                center = androidx.compose.ui.geometry.Offset(thumbX, thumbY)
                            )
                            // Bright center point
                            drawCircle(
                                color = textWhiteColor,
                                radius = 4.dp.toPx(),
                                center = androidx.compose.ui.geometry.Offset(thumbX, thumbY)
                            )
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = activeTimeString,
                                fontSize = 42.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = TextWhite
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${(animatedPercent * 100).toInt()}% REMAINING",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextMuted
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Reset button
                        val resetInteraction = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
                        Button(
                            onClick = { viewModel.resetTimer() },
                            interactionSource = resetInteraction,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = ObsidianCard,
                                contentColor = TextWhite
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .tactilePress(enabled = enableTactileFeedback, interactionSource = resetInteraction)
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = "Reset")
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("RESET", fontWeight = FontWeight.Bold)
                        }

                        // Play/Pause button
                        val playPauseInteraction = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
                        Button(
                            onClick = {
                                if (running) viewModel.pauseTimer() else viewModel.startTimer()
                            },
                            interactionSource = playPauseInteraction,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (running) TextWarning else NeonLime,
                                contentColor = ObsidianMain
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .tactilePress(enabled = enableTactileFeedback, interactionSource = playPauseInteraction)
                        ) {
                            Icon(
                                imageVector = if (running) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = if (running) "Pause" else "Resume"
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(if (running) "PAUSE" else "RESUME", fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // 3. Sound customizations expansion panel
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = ObsidianSurface),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showSoundCustomizer = !showSoundCustomizer },
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Tune, contentDescription = "Tuning Settings", tint = NeonLime)
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        "CUSTOMIZE TIMER & ALARM SOUND",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = TextWhite,
                                        letterSpacing = 0.5.sp
                                    )
                                    Text(
                                        "Tune waveforms, pitch, tempo, vibrato sliders",
                                        fontSize = 11.sp,
                                        color = TextMuted
                                    )
                                }
                            }
                            Icon(
                                imageVector = if (showSoundCustomizer) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                contentDescription = "Toggle Customizer",
                                tint = NeonLime
                            )
                        }

                        if (showSoundCustomizer) {
                            Spacer(modifier = Modifier.height(16.dp))

                            // Custom synth sliders panel
                            val currentVol by viewModel.globalVolume.collectAsStateWithLifecycle()
                            SoundPresetSliders(
                                waveType = waveType,
                                onWaveTypeChange = { viewModel.saveGlobalSoundSettings(it, frequency, pulseSpeed, vibratoDepth, vibratoSpeed, currentVol) },
                                frequency = frequency,
                                onFrequencyChange = { viewModel.saveGlobalSoundSettings(waveType, it, pulseSpeed, vibratoDepth, vibratoSpeed, currentVol) },
                                pulseSpeed = pulseSpeed,
                                onPulseSpeedChange = { viewModel.saveGlobalSoundSettings(waveType, frequency, it, vibratoDepth, vibratoSpeed, currentVol) },
                                vibratoDepth = vibratoDepth,
                                onVibratoDepthChange = { viewModel.saveGlobalSoundSettings(waveType, frequency, pulseSpeed, it, vibratoSpeed, currentVol) },
                                vibratoSpeed = vibratoSpeed,
                                onVibratoSpeedChange = { viewModel.saveGlobalSoundSettings(waveType, frequency, pulseSpeed, vibratoDepth, it, currentVol) },
                                isPreviewPlaying = isPreviewPlaying,
                                onTogglePreview = { viewModel.playGlobalSoundPreview() }
                            )
                        }
                    }
                }

                // MULTIPLE CONCURRENT TIMERS TITLE
                Spacer(modifier = Modifier.height(24.dp))
                
                Text(
                    text = "MULTIPLE BACKGROUND TIMERS",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    ),
                    color = NeonLime,
                    modifier = Modifier.padding(bottom = 6.dp)
                )
                
                Text(
                    text = "Configure and monitor multiple custom timers running in the background simultaneously.",
                    fontSize = 11.sp,
                    color = TextMuted,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Creator panel for custom multiple timers
                var customTimerLabel by remember { mutableStateOf("") }
                var timerHoursInput by remember { mutableStateOf(0) }
                var timerMinsInput by remember { mutableStateOf(0) }
                var timerSecsInput by remember { mutableStateOf(10) } // default 10 seconds for immediate test satisfaction!

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .liquidGlassCard(viewModel, cornerRadius = 16.dp, borderColor = NeonLime),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "CREATE NEW TIMER",
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = NeonLime,
                            letterSpacing = 0.5.sp
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        // Label input field
                        OutlinedTextField(
                            value = customTimerLabel,
                            onValueChange = { customTimerLabel = it },
                            placeholder = { Text("Timer Name (e.g. Pasta, Workout)", color = TextMuted, fontSize = 13.sp) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = NeonLime,
                                unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                                focusedTextColor = TextWhite,
                                unfocusedTextColor = TextWhite
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Duration:", fontSize = 13.sp, color = TextWhite)
                            
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("Hrs", fontSize = 10.sp, color = TextMuted)
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        IconButton(onClick = { if (timerHoursInput > 0) timerHoursInput-- }) {
                                            Icon(Icons.Default.Remove, "Less hours", tint = TextWhite, modifier = Modifier.size(16.dp))
                                        }
                                        Text("$timerHoursInput", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        IconButton(onClick = { if (timerHoursInput < 23) timerHoursInput++ }) {
                                            Icon(Icons.Default.Add, "More hours", tint = TextWhite, modifier = Modifier.size(16.dp))
                                        }
                                    }
                                }

                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("Mins", fontSize = 10.sp, color = TextMuted)
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        IconButton(onClick = { if (timerMinsInput > 0) timerMinsInput-- }) {
                                            Icon(Icons.Default.Remove, "Less minutes", tint = TextWhite, modifier = Modifier.size(16.dp))
                                        }
                                        Text("$timerMinsInput", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        IconButton(onClick = { if (timerMinsInput < 59) timerMinsInput++ }) {
                                            Icon(Icons.Default.Add, "More minutes", tint = TextWhite, modifier = Modifier.size(16.dp))
                                        }
                                    }
                                }

                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("Secs", fontSize = 10.sp, color = TextMuted)
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        IconButton(onClick = { if (timerSecsInput > 0) timerSecsInput-- }) {
                                            Icon(Icons.Default.Remove, "Less seconds", tint = TextWhite, modifier = Modifier.size(16.dp))
                                        }
                                        Text("$timerSecsInput", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        IconButton(onClick = { if (timerSecsInput < 59) timerSecsInput++ }) {
                                            Icon(Icons.Default.Add, "More seconds", tint = TextWhite, modifier = Modifier.size(16.dp))
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = {
                                val totalSecs = timerHoursInput * 3600 + timerMinsInput * 60 + timerSecsInput
                                if (totalSecs > 0) {
                                    viewModel.addCustomTimer(customTimerLabel, totalSecs)
                                    customTimerLabel = ""
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = NeonLime,
                                contentColor = ObsidianMain
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.AddAlarm, contentDescription = "Add Custom Clock")
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("ADD TO CONCURRENT LIST", fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // List of timers
                val customTimers by viewModel.customTimersList.collectAsStateWithLifecycle()
                
                if (customTimers.isEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = ObsidianSurface.copy(alpha = 0.5f))
                    ) {
                        Box(modifier = Modifier.padding(24.dp).fillMaxWidth(), contentAlignment = Alignment.Center) {
                            Text("No multi-timers active. Create one above!", color = TextMuted, fontSize = 12.sp)
                        }
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        customTimers.forEach { cTimer ->
                            val cHours = cTimer.timeLeftSecs / 3600
                            val cMins = (cTimer.timeLeftSecs % 3600) / 60
                            val cSecs = cTimer.timeLeftSecs % 60
                            val formattedTime = String.format("%02d:%02d:%02d", cHours, cMins, cSecs)
                            
                            val isRunning = cTimer.isRunning
                            val isTriggered = cTimer.isTriggered

                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .liquidGlassCard(viewModel, cornerRadius = 12.dp, borderColor = if (isTriggered) NeonAmber else if (isRunning) NeonTeal else Color.White),
                                colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(
                                                text = cTimer.label.uppercase(),
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isTriggered) NeonAmber else if (isRunning) NeonTeal else TextWhite
                                            )
                                            Text(
                                                text = "Original duration: ${cTimer.durationSecs}s",
                                                fontSize = 10.sp,
                                                color = TextMuted
                                            )
                                        }

                                        // Status
                                        Box(
                                            modifier = Modifier
                                                .background(
                                                    color = if (isTriggered) NeonAmber.copy(alpha = 0.2f) else if (isRunning) NeonTeal.copy(alpha = 0.2f) else ObsidianCard,
                                                    shape = RoundedCornerShape(12.dp)
                                                )
                                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                        ) {
                                            Text(
                                                text = if (isTriggered) "TRIGGERED" else if (isRunning) "RUNNING" else "PAUSED",
                                                color = if (isTriggered) NeonAmber else if (isRunning) NeonTeal else TextMuted,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        // Large Timer clock display
                                        Text(
                                            text = formattedTime,
                                            fontSize = 28.sp,
                                            fontFamily = FontFamily.Monospace,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isTriggered) NeonAmber else TextWhite
                                        )

                                        // Control actions Row
                                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                            if (isTriggered) {
                                                Button(
                                                    onClick = { viewModel.dismissTriggeredCustomTimer(cTimer.id) },
                                                    colors = ButtonDefaults.buttonColors(containerColor = NeonAmber, contentColor = ObsidianMain),
                                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                                                    shape = RoundedCornerShape(6.dp)
                                                ) {
                                                    Text("DISMISS", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                                }
                                            } else {
                                                // Start / Pause
                                                IconButton(
                                                    onClick = {
                                                        if (isRunning) viewModel.pauseCustomTimer(cTimer.id) else viewModel.startCustomTimer(cTimer.id)
                                                    }
                                                ) {
                                                    Icon(
                                                        imageVector = if (isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                                                        contentDescription = "Toggle Timer State",
                                                        tint = if (isRunning) TextWarning else NeonLime,
                                                        modifier = Modifier.size(20.dp)
                                                    )
                                                }
                                                // Reset
                                                IconButton(
                                                    onClick = { viewModel.resetCustomTimer(cTimer.id) }
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Refresh,
                                                        contentDescription = "Reset Timer",
                                                        tint = TextWhite,
                                                        modifier = Modifier.size(20.dp)
                                                    )
                                                }
                                            }

                                            // Delete / Remove
                                            IconButton(
                                                onClick = { viewModel.removeCustomTimer(cTimer.id) }
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Delete,
                                                    contentDescription = "Delete Timer",
                                                    tint = TextWarning,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TimerUnitPicker(
    label: String,
    currentValue: Int,
    min: Int,
    max: Int,
    enableTactileFeedback: Boolean = true,
    viewModel: ClockViewModel? = null,
    onValueChange: (Int) -> Unit
) {
    val enableGlassEffect = if (viewModel != null) {
        viewModel.enableGlassEffect.collectAsStateWithLifecycle().value
    } else {
        false
    }

    Card(
        modifier = Modifier
            .width(80.dp)
            .then(
                if (viewModel != null) {
                    Modifier.liquidGlassCard(viewModel, cornerRadius = 12.dp, borderColor = NeonLime)
                } else {
                    Modifier.border(1.dp, ObsidianCard, RoundedCornerShape(12.dp))
                }
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (enableGlassEffect) Color.Transparent else ObsidianSurface
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = TextMuted,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            val upInteraction = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
            IconButton(
                onClick = { if (currentValue < max) onValueChange(currentValue + 1) else onValueChange(min) },
                interactionSource = upInteraction,
                modifier = Modifier
                    .size(24.dp)
                    .tactilePress(enabled = enableTactileFeedback, interactionSource = upInteraction)
            ) {
                Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Increment", tint = NeonLime)
            }
            Text(
                text = String.format("%02d", currentValue),
                fontSize = 28.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                color = TextWhite
            )
            val downInteraction = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
            IconButton(
                onClick = { if (currentValue > min) onValueChange(currentValue - 1) else onValueChange(max) },
                interactionSource = downInteraction,
                modifier = Modifier
                    .size(24.dp)
                    .tactilePress(enabled = enableTactileFeedback, interactionSource = downInteraction)
            ) {
                Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Decrement", tint = NeonLime)
            }
        }
    }
}

// Slider helper class for customizable audio synthesis properties
@Composable
fun SoundPresetSliders(
    waveType: String,
    onWaveTypeChange: (String) -> Unit,
    frequency: Float,
    onFrequencyChange: (Float) -> Unit,
    pulseSpeed: Long,
    onPulseSpeedChange: (Long) -> Unit,
    vibratoDepth: Float,
    onVibratoDepthChange: (Float) -> Unit,
    vibratoSpeed: Float,
    onVibratoSpeedChange: (Float) -> Unit,
    isPreviewPlaying: Boolean,
    onTogglePreview: () -> Unit
) {
    // SoundPreset Helper definition for quick adjustments
    val soundPresets = listOf(
        com.example.sound.SoundPresetData("🚨 Siren", "SINE", 800f, 300L, 0.40f, 6.0f),
        com.example.sound.SoundPresetData("🛸 Cosmic", "SINE", 300f, 1000L, 0.15f, 1.5f),
        com.example.sound.SoundPresetData("👾 Laser", "SQUARE", 1200f, 150L, 0.35f, 14.0f),
        com.example.sound.SoundPresetData("⏰ Classic", "SQUARE", 600f, 400L, 0.00f, 1.0f),
        com.example.sound.SoundPresetData("🌀 Alien", "TRIANGLE", 1500f, 80L, 0.60f, 18.0f),
        com.example.sound.SoundPresetData("⚡ Beep!", "SAWTOOTH", 950f, 200L, 0.10f, 4.0f)
    )

    Column(modifier = Modifier.fillMaxWidth()) {
        HorizontalDivider(color = ObsidianCard, thickness = 1.dp)
        Spacer(modifier = Modifier.height(12.dp))

        // Programmatic Presets section
        Text("PROGRAMMATIC SOUND PRESETS", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = NeonLime, letterSpacing = 0.5.sp)
        Spacer(modifier = Modifier.height(6.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            soundPresets.forEach { preset ->
                val isSelected = waveType.equals(preset.wave, ignoreCase = true) &&
                        Math.abs(frequency - preset.freq) < 5f &&
                        pulseSpeed == preset.pulse &&
                        Math.abs(vibratoDepth - preset.vibDepth) < 0.05f &&
                        Math.abs(vibratoSpeed - preset.vibSpeed) < 0.5f

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isSelected) NeonLime else ObsidianCard)
                        .border(1.dp, if (isSelected) NeonLime else ObsidianCard.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
                        .clickable {
                            onWaveTypeChange(preset.wave)
                            onFrequencyChange(preset.freq)
                            onPulseSpeedChange(preset.pulse)
                            onVibratoDepthChange(preset.vibDepth)
                            onVibratoSpeedChange(preset.vibSpeed)
                            // We should also let it be saved by the ViewModel, but we will pass a save callback.
                        }
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = preset.name,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) ObsidianMain else TextWhite
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Waveform selection button blocks
        Text("WAVEFORM TYPE", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextMuted)
        Spacer(modifier = Modifier.height(6.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            listOf("SINE", "SQUARE", "TRIANGLE", "SAWTOOTH").forEach { wave ->
                val selected = waveType.equals(wave, ignoreCase = true)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (selected) NeonLime else ObsidianCard)
                        .border(1.dp, if (selected) NeonLime else ObsidianCard, RoundedCornerShape(8.dp))
                        .clickable { onWaveTypeChange(wave) }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        wave,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (selected) ObsidianMain else TextWhite
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // PITCH FREQUENCY SLIDER (Hz)
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("PITCH FREQUENCY", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextMuted)
            Text("${frequency.toInt()} Hz", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = NeonLime)
        }
        Slider(
            value = frequency,
            onValueChange = onFrequencyChange,
            valueRange = 150f..2200f,
            colors = SliderDefaults.colors(thumbColor = NeonLime, activeTrackColor = NeonLime)
        )

        // SYNC BEEP TEMPO/SPEED SLIDER (ms)
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("BEEP TEMPO SPEED", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextMuted)
            Text("${pulseSpeed} ms", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = NeonTeal)
        }
        Slider(
            value = pulseSpeed.toFloat(),
            onValueChange = { onPulseSpeedChange(it.toLong()) },
            valueRange = 60f..1500f,
            colors = SliderDefaults.colors(thumbColor = NeonTeal, activeTrackColor = NeonTeal)
        )

        // VIBRATO LFO DEPTH SLIDER
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("VIBRATO MOD DEPTH", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextMuted)
            Text("${(vibratoDepth * 100).toInt()}%", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = NeonAmber)
        }
        Slider(
            value = vibratoDepth,
            onValueChange = onVibratoDepthChange,
            valueRange = 0f..0.8f,
            colors = SliderDefaults.colors(thumbColor = NeonAmber, activeTrackColor = NeonAmber)
        )

        // VIBRATO SPEED RATE (Hz)
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("VIBRATO MOD RATE LFO", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextMuted)
            Text("${String.format("%.1f", vibratoSpeed)} Hz", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextWhite)
        }
        Slider(
            value = vibratoSpeed,
            onValueChange = onVibratoSpeedChange,
            valueRange = 0.5f..20f,
            colors = SliderDefaults.colors(thumbColor = TextWhite, activeTrackColor = TextWhite)
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Realtime Sound preview listening button
        Button(
            onClick = onTogglePreview,
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isPreviewPlaying) TextWarning else ObsidianCard,
                contentColor = if (isPreviewPlaying) ObsidianMain else NeonLime
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = if (isPreviewPlaying) Icons.AutoMirrored.Filled.VolumeOff else Icons.AutoMirrored.Filled.VolumeUp,
                contentDescription = null
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                if (isPreviewPlaying) "STOP SOUND PREVIEW" else "TEST CUSTOMIZED SOUND PREVIEW",
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp
            )
        }
    }
}


// =========================================================================================
// 5. WORLD CLOCK SCREEN (Timezone Database Search & Add)
// =========================================================================================
@Composable
fun WorldClockScreen(viewModel: ClockViewModel) {
    val trackedClocks by viewModel.trackedWorldClocks.collectAsStateWithLifecycle()
    val query by viewModel.worldClockSearchQuery.collectAsStateWithLifecycle()

    var activeSearchMode by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Section Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "WORLD TIMEZONE CLOCKS",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    ),
                    color = NeonLime,
                    modifier = Modifier.padding(vertical = 12.dp)
                )

                IconButton(
                    onClick = { activeSearchMode = !activeSearchMode },
                    modifier = Modifier.testTag("search_world_clock_toggle")
                ) {
                    Icon(
                        imageVector = if (activeSearchMode) Icons.Default.Close else Icons.Default.Search,
                        contentDescription = "Search world",
                        tint = NeonLime
                    )
                }
            }

            // List of tracked items
            if (trackedClocks.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.Language,
                            contentDescription = "No Clocks",
                            modifier = Modifier.size(64.dp),
                            tint = ObsidianCard
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            "No Tracked World Clocks",
                            color = TextWhite,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "Tap the Search Icon to add locations",
                            fontSize = 13.sp,
                            color = TextMuted
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(trackedClocks, key = { it.timezoneId + "_" + it.cityName }) { clock ->
                        val enableAnimations by viewModel.enableAnimations.collectAsStateWithLifecycle()
                        WorldClockCard(
                            clock = clock,
                            viewModel = viewModel,
                            modifier = if (enableAnimations) Modifier.animateItem() else Modifier
                        )
                    }
                }
            }
        }

        // FLOATING SEARCH OVERLAY with Glassmorphism, Dimming Effect & Animated Border Focus Glow
        if (activeSearchMode) {
            // Dim background (Blur effect mask)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.65f))
                    .clickable { 
                        activeSearchMode = false 
                        viewModel.searchWorldCities("") 
                    }
            )

            var isSearchFocused by remember { mutableStateOf(false) }
            val focusTransition = rememberInfiniteTransition(label = "SearchFocusGlow")
            
            val glowingColor by focusTransition.animateColor(
                initialValue = NeonTeal.copy(alpha = 0.35f),
                targetValue = NeonTeal.copy(alpha = 0.85f),
                animationSpec = infiniteRepeatable(
                    animation = tween(1200, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "GlowColor"
            )
            
            val glowingWidth by animateDpAsState(
                targetValue = if (isSearchFocused) 2.5.dp else 1.dp,
                label = "GlowWidth"
            )

            // Floating glassy modal
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 72.dp)
                    .align(Alignment.TopCenter)
                    .clickable(enabled = false) { },
                colors = CardDefaults.cardColors(containerColor = ObsidianCard.copy(alpha = 0.92f)),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.15f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "TRACK WORLD CITIES",
                            fontWeight = FontWeight.Bold,
                            color = NeonTeal,
                            fontSize = 12.sp,
                            letterSpacing = 1.sp
                        )
                        IconButton(onClick = { 
                            activeSearchMode = false 
                            viewModel.searchWorldCities("")
                        }) {
                            Icon(Icons.Default.Close, contentDescription = "Close search", tint = TextMuted)
                        }
                    }

                    // Floating Search Bar with Animated Focus Glow!
                    OutlinedTextField(
                        value = query,
                        onValueChange = { viewModel.searchWorldCities(it) },
                        placeholder = { Text("Search city (e.g. London, Tokyo)", color = TextMuted) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent,
                            focusedTextColor = TextWhite,
                            unfocusedTextColor = TextWhite,
                            focusedContainerColor = ObsidianSurface.copy(alpha = 0.4f),
                            unfocusedContainerColor = ObsidianSurface.copy(alpha = 0.2f)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .onFocusChanged { isSearchFocused = it.isFocused }
                            .border(
                                width = glowingWidth,
                                color = if (isSearchFocused) glowingColor else Color.White.copy(alpha = 0.12f),
                                shape = RoundedCornerShape(14.dp)
                            )
                            .testTag("world_clock_search_input")
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Results lists with Blur/Glass container
                    val filteredList = if (query.isEmpty()) {
                        viewModel.availableWorldCities
                    } else {
                        viewModel.availableWorldCities.filter {
                            it.cityName.contains(query, ignoreCase = true) || it.countryName.contains(query, ignoreCase = true)
                        }
                    }

                    if (filteredList.isEmpty()) {
                        Text(
                            "No cities found matching '$query'",
                            color = TextMuted,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(vertical = 12.dp, horizontal = 4.dp)
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 240.dp)
                                .background(ObsidianSurface.copy(alpha = 0.35f), RoundedCornerShape(16.dp))
                                .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(16.dp))
                        ) {
                            LazyColumn {
                                items(filteredList) { city ->
                                    val alreadyTracked = trackedClocks.any { it.timezoneId == city.timezoneId && it.cityName == city.cityName }
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable(enabled = !alreadyTracked) {
                                                viewModel.addTrackedZone(city)
                                            }
                                            .padding(14.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(city.cityName, fontWeight = FontWeight.Bold, color = TextWhite, fontSize = 14.sp)
                                            Text(city.countryName + " (${city.timezoneId})", fontSize = 11.sp, color = TextMuted)
                                        }
                                        if (alreadyTracked) {
                                            Text("TRACKING", fontSize = 10.sp, color = NeonLime, fontWeight = FontWeight.Bold)
                                        } else {
                                            Icon(Icons.Default.AddCircleOutline, contentDescription = "Add Clocks", tint = NeonTeal)
                                        }
                                    }
                                    HorizontalDivider(color = Color.White.copy(alpha = 0.06f), thickness = 0.5.dp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun WorldClockCard(clock: WorldClock, viewModel: ClockViewModel, modifier: Modifier = Modifier) {
    val currentTime = viewModel.getTrackedClockTime(clock.timezoneId)
    val currentDate = viewModel.getTrackedClockDate(clock.timezoneId)
    val clockTimeState by viewModel.clockTime.collectAsStateWithLifecycle()

    Card(
        modifier = modifier
            .fillMaxWidth()
            .liquidGlassCard(viewModel, cornerRadius = 16.dp, borderColor = NeonTeal),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = clock.cityName.uppercase(),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = NeonTeal
                )
                Text(
                    text = clock.countryName.uppercase(),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextMuted,
                    letterSpacing = 0.5.sp
                )
                val zone = java.time.ZoneId.of(clock.timezoneId)
                val offsetStr = zone.rules.getOffset(java.time.Instant.now()).id.let { if (it == "Z") "+00:00" else it }
                Text(
                    text = "GMT$offsetStr",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = NeonAmber,
                    modifier = Modifier.padding(top = 2.dp)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = currentDate,
                    fontSize = 12.sp,
                    color = TextWhite.copy(alpha = 0.8f)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = currentTime,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    fontFamily = FontFamily.Monospace,
                    color = NeonLime
                )
            }

            val obsidianCardColor = ObsidianCard
            val textWhiteColor = TextWhite
            val neonTealColor = NeonTeal
            val neonAmberColor = NeonAmber
            val neonLimeColor = NeonLime
            // Analog Clock Canvas
            Box(
                modifier = Modifier.size(70.dp).padding(end = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val radius = size.minDimension / 2
                    val center = androidx.compose.ui.geometry.Offset(size.width / 2, size.height / 2)
                    
                    // Clock face
                    drawCircle(
                        color = obsidianCardColor,
                        radius = radius,
                        style = Stroke(width = 2.dp.toPx())
                    )
                    
                    // Current hour/minute based on the tracked timezone
                    val zone = java.time.ZoneId.of(clock.timezoneId)
                    val localTime = java.time.ZonedDateTime.ofInstant(clockTimeState.toInstant(), zone)
                    val h = localTime.hour % 12
                    val m = localTime.minute
                    val s = localTime.second
                    
                    val hrAngle = Math.toRadians((h * 30 + m * 0.5 - 90).toDouble())
                    val minAngle = Math.toRadians((m * 6 + s * 0.1 - 90).toDouble())
                    val secAngle = Math.toRadians((s * 6 - 90).toDouble())
                    
                    // Hour Hand
                    drawLine(
                        color = textWhiteColor,
                        start = center,
                        end = androidx.compose.ui.geometry.Offset(
                            x = center.x + (radius * 0.5f) * Math.cos(hrAngle).toFloat(),
                            y = center.y + (radius * 0.5f) * Math.sin(hrAngle).toFloat()
                        ),
                        strokeWidth = 3.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                    // Minute Hand
                    drawLine(
                        color = neonTealColor,
                        start = center,
                        end = androidx.compose.ui.geometry.Offset(
                            x = center.x + (radius * 0.75f) * Math.cos(minAngle).toFloat(),
                            y = center.y + (radius * 0.75f) * Math.sin(minAngle).toFloat()
                        ),
                        strokeWidth = 2.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                    // Second Hand
                    drawLine(
                        color = neonAmberColor,
                        start = center,
                        end = androidx.compose.ui.geometry.Offset(
                            x = center.x + (radius * 0.85f) * Math.cos(secAngle).toFloat(),
                            y = center.y + (radius * 0.85f) * Math.sin(secAngle).toFloat()
                        ),
                        strokeWidth = 1.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                    // Center pin
                    drawCircle(color = neonLimeColor, radius = 2.dp.toPx())
                }
            }

            IconButton(
                onClick = { viewModel.removeTrackedZone(clock.timezoneId) },
                modifier = Modifier.testTag("remove_world_clock_${clock.timezoneId}")
            ) {
                Icon(
                    Icons.Default.RemoveCircleOutline,
                    contentDescription = "Remove from tracked list",
                    tint = TextWarning.copy(alpha = 0.7f)
                )
            }
        }
    }
}


// =========================================================================================
// 6. ABOUT COMPONENT SCREEN (Developer and APK Build Info)
// =========================================================================================
@Composable
fun AboutScreen(viewModel: ClockViewModel, onTriggerDiagnostics: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "ABOUT SYSTEM",
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            ),
            color = NeonLime,
            modifier = Modifier.padding(vertical = 12.dp)
        )

        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                Spacer(modifier = Modifier.height(16.dp))

                // Brand Emblem (Features the new real adaptive chronometer logo!)
                Box(
                    modifier = Modifier
                        .size(110.dp)
                        .clip(CircleShape)
                        .background(ObsidianCard.copy(alpha = 0.5f))
                        .border(1.5.dp, NeonLime, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = com.example.R.drawable.ic_launcher_foreground),
                        contentDescription = "Clock In Rock Logo",
                        modifier = Modifier
                            .size(90.dp)
                            .scale(1.15f)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Clock In rock",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = TextWhite
                )
                Text(
                    text = "v1.0.0 Stable Release",
                    fontSize = 12.sp,
                    color = TextMuted,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Developer Biography Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .liquidGlassCard(viewModel, cornerRadius = 16.dp, borderColor = NeonLime),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "DEVELOPER PROFILE",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = NeonTeal,
                            letterSpacing = 1.5.sp
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "I am a developer.",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextWhite
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "apk website Will build",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium,
                            color = NeonAmber
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Box(
                            modifier = Modifier
                                .background(ObsidianCard, RoundedCornerShape(8.dp))
                                .padding(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "Made by @sayanthrock",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = NeonLime
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // SYSTEM UPDATES & OTA MANAGEMENT CARD
                val isChecking by viewModel.isCheckingForUpdates.collectAsStateWithLifecycle()
                val updateInfo by viewModel.updateAvailable.collectAsStateWithLifecycle()
                val otaErr by viewModel.otaError.collectAsStateWithLifecycle()
                var isNightlyStream by remember { mutableStateOf(false) }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .liquidGlassCard(viewModel, cornerRadius = 16.dp, borderColor = NeonAmber),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp)
                    ) {
                        Text(
                            text = "SYSTEM OTA UPDATE",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = NeonAmber,
                            letterSpacing = 1.5.sp,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        Text(
                            text = "Compare current deployment against distribution targets. Regular and experimental channels supported.",
                            fontSize = 11.sp,
                            color = TextMuted,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )

                        // Stream Channel toggle
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(ObsidianCard, RoundedCornerShape(8.dp))
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Nightly Stream Updates",
                                fontSize = 12.sp,
                                color = TextWhite,
                                fontWeight = FontWeight.Medium
                            )
                            Switch(
                                checked = isNightlyStream,
                                onCheckedChange = { isNightlyStream = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = NeonAmber,
                                    checkedTrackColor = NeonAmber.copy(alpha = 0.4f),
                                    uncheckedThumbColor = TextMuted,
                                    uncheckedTrackColor = ObsidianCard
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = { viewModel.checkForUpdates(isNightlyStream) },
                            enabled = !isChecking,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = NeonAmber,
                                contentColor = ObsidianMain,
                                disabledContainerColor = NeonAmber.copy(alpha = 0.3f)
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            if (isChecking) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    color = ObsidianMain,
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text("QUERYING CHANNELS...", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            } else {
                                Icon(Icons.Default.Download, contentDescription = "Check update")
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("CHECK FOR UPDATES", fontWeight = FontWeight.Bold)
                            }
                        }

                        // Status messages
                        otaErr?.let { errMsg ->
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = errMsg,
                                color = NeonLime,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.align(Alignment.CenterHorizontally)
                            )
                        }

                        updateInfo?.let { update ->
                            Spacer(modifier = Modifier.height(16.dp))
                            HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            Box(
                                modifier = Modifier
                                    .background(NeonTeal.copy(alpha = 0.1f), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "UPDATE FOUND: v${update.versionName}",
                                    color = NeonTeal,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "CHANGELOG:",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextWhite
                            )
                            Text(
                                text = update.changelog,
                                fontSize = 12.sp,
                                color = TextWhite.copy(alpha = 0.85f),
                                modifier = Modifier.padding(top = 2.dp, bottom = 12.dp)
                            )

                            val context = androidx.compose.ui.platform.LocalContext.current
                            Button(
                                onClick = {
                                    try {
                                        val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(update.apkUrl))
                                        context.startActivity(intent)
                                    } catch (e: Exception) {
                                        // Failover link navigation
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = NeonTeal, contentColor = ObsidianMain),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.Launch, contentDescription = "Launch install")
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("DOWNLOAD & INSTALL APK", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Interactive Diagnostic Calibration Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                        .liquidGlassCard(viewModel, cornerRadius = 16.dp, borderColor = NeonTeal)
                        .clickable { onTriggerDiagnostics() },
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Construction,
                                contentDescription = "Calibrate Chrono system",
                                tint = NeonTeal,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(14.dp))
                            Column {
                                Text(
                                    text = "CHRONO CALIBRATION",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = NeonTeal,
                                    letterSpacing = 1.sp
                                )
                                Text(
                                    text = "Run structural index diagnostics",
                                    fontSize = 13.sp,
                                    color = TextWhite,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Run",
                            tint = NeonTeal,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                
                // Theme Mode settings with 3-state support
                val themeModeEx by viewModel.themeMode.collectAsStateWithLifecycle()
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, ObsidianCard, RoundedCornerShape(16.dp)),
                    colors = CardDefaults.cardColors(containerColor = ObsidianSurface),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 16.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = when (themeModeEx) {
                                    "Light" -> Icons.Default.LightMode
                                    "Dark" -> Icons.Default.DarkMode
                                    "Auto (Time-based)" -> Icons.Default.AutoAwesome
                                    else -> Icons.Default.Settings
                                },
                                contentDescription = "Theme Mode",
                                tint = NeonLime
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "THEME MODE",
                                fontWeight = FontWeight.Bold,
                                color = TextWhite,
                                fontSize = 12.sp,
                                letterSpacing = 1.0.sp
                             )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                listOf("Light", "Dark").forEach { mode ->
                                    FilterChip(
                                        selected = themeModeEx == mode,
                                        onClick = { viewModel.setThemeMode(mode) },
                                        label = { 
                                            Text(
                                                text = mode, 
                                                fontSize = 11.sp, 
                                                fontWeight = FontWeight.SemiBold,
                                                modifier = Modifier.fillMaxWidth(),
                                                textAlign = TextAlign.Center
                                            ) 
                                        },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = NeonLime,
                                            selectedLabelColor = ObsidianMain,
                                            labelColor = TextWhite
                                        ),
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                listOf("System Default", "Auto (Time-based)").forEach { mode ->
                                    FilterChip(
                                        selected = themeModeEx == mode,
                                        onClick = { viewModel.setThemeMode(mode) },
                                        label = { 
                                            Text(
                                                text = mode, 
                                                fontSize = 11.sp, 
                                                fontWeight = FontWeight.SemiBold,
                                                modifier = Modifier.fillMaxWidth(),
                                                textAlign = TextAlign.Center
                                            ) 
                                        },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = NeonLime,
                                            selectedLabelColor = ObsidianMain,
                                            labelColor = TextWhite
                                        ),
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }

                        if (themeModeEx == "Auto (Time-based)") {
                            Spacer(modifier = Modifier.height(10.dp))
                            val currentHour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
                            val isDarkNow = currentHour !in 5..16
                            Text(
                                text = "Auto theme is currently active. With the current hour being $currentHour:00, " +
                                       "it is in ${if (isDarkNow) "Dark" else "Light"} Mode to align with your " +
                                       "${if (currentHour in 5..11) "Good Morning" else if (currentHour in 12..16) "Good Afternoon" else if (currentHour in 17..20) "Good Evening" else "Good Night"} greeting.",
                                fontSize = 11.sp,
                                color = NeonTeal,
                                fontWeight = FontWeight.Medium,
                                letterSpacing = 0.2.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Display UI Settings Menu
                val displaySize by viewModel.displaySize.collectAsStateWithLifecycle()
                val fontFamily by viewModel.fontFamilyStr.collectAsStateWithLifecycle()
                val fontWeight by viewModel.fontWeightStr.collectAsStateWithLifecycle()

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, ObsidianCard, RoundedCornerShape(16.dp)),
                    colors = CardDefaults.cardColors(containerColor = ObsidianSurface),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "DISPLAY PREFERENCES",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = NeonTeal,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        // Display Size Segmented Control
                        Text("DISPLAY SIZE", fontSize = 10.sp, color = TextMuted, fontWeight = FontWeight.Bold)
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf("Small", "Standard", "Large").forEach { size ->
                                FilterChip(
                                    selected = displaySize == size,
                                    onClick = { viewModel.setDisplaySize(size) },
                                    label = { Text(size, fontSize = 12.sp, fontWeight = FontWeight.SemiBold) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = NeonLime,
                                        selectedLabelColor = ObsidianMain,
                                        labelColor = TextWhite
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Font Family Control
                        Text("FONT FAMILY", fontSize = 10.sp, color = TextMuted, fontWeight = FontWeight.Bold)
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf("Sans-Serif", "Serif", "Monospace").forEach { family ->
                                FilterChip(
                                    selected = fontFamily == family,
                                    onClick = { viewModel.setFontFamily(family) },
                                    label = { Text(family, fontSize = 12.sp, fontWeight = FontWeight.SemiBold) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = NeonTeal,
                                        selectedLabelColor = ObsidianMain,
                                        labelColor = TextWhite
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Font Weight Control
                        Text("FONT WEIGHT", fontSize = 10.sp, color = TextMuted, fontWeight = FontWeight.Bold)
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf("Normal", "SemiBold", "Bold").forEach { weight ->
                                FilterChip(
                                    selected = fontWeight == weight,
                                    onClick = { viewModel.setFontWeight(weight) },
                                    label = { Text(weight, fontSize = 12.sp, fontWeight = FontWeight.SemiBold) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = NeonAmber,
                                        selectedLabelColor = ObsidianMain,
                                        labelColor = TextWhite
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Color Preset Control
                        val colorProfile by viewModel.colorProfile.collectAsStateWithLifecycle()
                        Text("COLOR PRESET", fontSize = 10.sp, color = TextMuted, fontWeight = FontWeight.Bold)
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp).horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(
                                "Midnight Black", "Ocean Blue", "Royal Purple",
                                "Emerald Green", "Crimson Red", "Sunset Orange",
                                "Rose Pink", "Neon Cyber", "Galaxy Gradient", "Rock Theme"
                            ).forEach { profile ->
                                FilterChip(
                                    selected = colorProfile == profile,
                                    onClick = { viewModel.setColorProfile(profile) },
                                    label = { Text(profile, fontSize = 12.sp, fontWeight = FontWeight.SemiBold) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = NeonTeal,
                                        selectedLabelColor = ObsidianMain,
                                        labelColor = TextWhite
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Transition Style Control
                        val transitionStyle by viewModel.transitionStyle.collectAsStateWithLifecycle()
                        Text("TRANSITION STYLE", fontSize = 10.sp, color = TextMuted, fontWeight = FontWeight.Bold)
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp).horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf("Roll", "Cube", "Flip", "Card", "Tilt", "Slide", "Fade").forEach { style ->
                                FilterChip(
                                    selected = transitionStyle == style,
                                    onClick = { viewModel.setTransitionStyle(style) },
                                    label = { Text(style, fontSize = 12.sp, fontWeight = FontWeight.SemiBold) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = NeonAmber,
                                        selectedLabelColor = ObsidianMain,
                                        labelColor = TextWhite
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Animation Toggle
                        val enableAnimations by viewModel.enableAnimations.collectAsStateWithLifecycle()
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = "Animations Enable Toggle",
                                    tint = NeonLime
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text("Enable Animations", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            }
                            GlassSwitch(
                                checked = enableAnimations,
                                onCheckedChange = { viewModel.setEnableAnimations(it) }
                            )
                        }

                        // Visual Haptic Toggle
                        val enableTactileFeedback by viewModel.enableTactileFeedback.collectAsStateWithLifecycle()
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.TouchApp,
                                    contentDescription = "Visual Haptics Toggle",
                                    tint = NeonTeal
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text("Visual Haptic Feedback", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                    Text("Tactile shake and bounciness on touch", fontSize = 11.sp, color = TextMuted)
                                }
                            }
                            GlassSwitch(
                                checked = enableTactileFeedback,
                                onCheckedChange = { viewModel.setEnableTactileFeedback(it) }
                            )
                        }

                        // Universal Monochrome Style Toggle
                        val enableMonochrome by viewModel.enableMonochrome.collectAsStateWithLifecycle()
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Palette,
                                    contentDescription = "Monochrome Toggle",
                                    tint = NeonLime
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text("Unified Monochrome Colors", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                    Text("Make all accent colors look the same (Sleek Silver & Slate)", fontSize = 11.sp, color = TextMuted)
                                }
                            }
                            GlassSwitch(
                                checked = enableMonochrome,
                                onCheckedChange = { viewModel.setEnableMonochrome(it) }
                            )
                        }

                        // Material You Dynamic Colors Toggle
                        val enableDynamicColor by viewModel.enableDynamicColor.collectAsStateWithLifecycle()
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Palette,
                                    contentDescription = "Dynamic Colors Toggle",
                                    tint = NeonTeal
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text("Dynamic Material You Colors", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                    Text("Matches wallpaper color palette (Android 12+)", fontSize = 11.sp, color = TextMuted)
                                }
                            }
                            GlassSwitch(
                                checked = enableDynamicColor,
                                onCheckedChange = { viewModel.setEnableDynamicColor(it) }
                            )
                        }

                        // AMOLED Mode Toggle
                        val enableAmoledMode by viewModel.enableAmoledMode.collectAsStateWithLifecycle()
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Contrast,
                                    contentDescription = "AMOLED Mode Toggle",
                                    tint = NeonAmber
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text("AMOLED Pure Black Mode", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                    Text("Pure dark background for contrast and energy savings", fontSize = 11.sp, color = TextMuted)
                                }
                            }
                            GlassSwitch(
                                checked = enableAmoledMode,
                                onCheckedChange = { viewModel.setEnableAmoledMode(it) }
                            )
                        }

                        // LIQUID GLASS & BLUR EFFECTS CONTROL CARD
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("LIQUID GLASS & BLUR EFFECTS", fontSize = 10.sp, color = TextMuted, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))

                        val enableGlassEffect by viewModel.enableGlassEffect.collectAsStateWithLifecycle()
                        val glassBlurStrength by viewModel.glassBlurStrength.collectAsStateWithLifecycle()
                        val glassTransparency by viewModel.glassTransparency.collectAsStateWithLifecycle()
                        val glassBorderThickness by viewModel.glassBorderThickness.collectAsStateWithLifecycle()

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = ObsidianSurface),
                            border = BorderStroke(1.dp, NeonTeal.copy(alpha = 0.2f))
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("Liquid Glass Theme Active", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                                        Text("Render premium translucent glass textures", fontSize = 11.sp, color = TextMuted)
                                    }
                                    GlassSwitch(
                                        checked = enableGlassEffect,
                                        onCheckedChange = { viewModel.setEnableGlassEffect(it) }
                                    )
                                }

                                if (enableGlassEffect) {
                                    Spacer(modifier = Modifier.height(16.dp))
                                    HorizontalDivider(color = NeonTeal.copy(alpha = 0.1f))
                                    Spacer(modifier = Modifier.height(16.dp))

                                    // BLUR RADIUS STRENGTH
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("BLUR STRENGTH (RADIUS)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextMuted)
                                        Text("${glassBlurStrength.toInt()} dp", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = NeonLime)
                                    }
                                    Slider(
                                        value = glassBlurStrength,
                                        onValueChange = { viewModel.setGlassBlurStrength(it) },
                                        valueRange = 0f..40f,
                                        colors = SliderDefaults.colors(thumbColor = NeonLime, activeTrackColor = NeonLime)
                                    )
                                    Text(
                                        "Controls the background softness profile behind components.",
                                        fontSize = 10.sp,
                                        color = TextMuted,
                                        modifier = Modifier.padding(bottom = 12.dp)
                                    )

                                    // GLASS TRANSPARENCY
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("GLASS OPACITY / TRANSPARENCY", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextMuted)
                                        Text("${(glassTransparency * 100).toInt()}%", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = NeonTeal)
                                    }
                                    Slider(
                                        value = glassTransparency,
                                        onValueChange = { viewModel.setGlassTransparency(it) },
                                        valueRange = 0.02f..0.80f,
                                        colors = SliderDefaults.colors(thumbColor = NeonTeal, activeTrackColor = NeonTeal)
                                    )
                                    Text(
                                        "Controls physical material transparency of the panels.",
                                        fontSize = 10.sp,
                                        color = TextMuted,
                                        modifier = Modifier.padding(bottom = 12.dp)
                                    )

                                    // BORDER THICKNESS
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("SPECULAR BORDER THICKNESS", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextMuted)
                                        Text("${String.format("%.1f", glassBorderThickness)} dp", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = NeonAmber)
                                    }
                                    Slider(
                                        value = glassBorderThickness,
                                        onValueChange = { viewModel.setGlassBorderThickness(it) },
                                        valueRange = 0.2f..5.0f,
                                        colors = SliderDefaults.colors(thumbColor = NeonAmber, activeTrackColor = NeonAmber)
                                    )
                                    Text(
                                        "Adjusts the highlight edge thickness for the liquid refraction.",
                                        fontSize = 10.sp,
                                        color = TextMuted
                                    )
                                    
                                    Spacer(modifier = Modifier.height(16.dp))

                                    // GLASS PREVIEW LAYER
                                    Text("DYNAMIC GLASS PREVIEW", fontSize = 11.sp, color = TextMuted, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(60.dp)
                                            .background(
                                                brush = androidx.compose.ui.graphics.Brush.sweepGradient(
                                                    colors = listOf(NeonLime, NeonTeal, NeonAmber, NeonLime)
                                                ),
                                                shape = RoundedCornerShape(12.dp)
                                            )
                                            .padding(6.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .liquidGlassCard(viewModel, cornerRadius = 8.dp, borderColor = Color.White),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = "Liquid Glass Refraction Active",
                                                color = TextWhite,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                letterSpacing = 0.5.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        Text("CLOCK NUMERAL ALPHABET", fontSize = 10.sp, color = TextMuted, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))

                        val allAlphabets by viewModel.allAlphabets.collectAsStateWithLifecycle()
                        val selectedIndex by viewModel.selectedAlphabetIndex.collectAsStateWithLifecycle()

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = ObsidianSurface),
                            border = BorderStroke(1.dp, NeonTeal.copy(alpha = 0.15f))
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("Select Dial Numerals Alphabet", fontSize = 12.sp, color = TextMuted, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(8.dp))

                                // Horizontally scrollable list of alphabets
                                Row(
                                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    allAlphabets.forEachIndexed { idx, alpha ->
                                        FilterChip(
                                            selected = selectedIndex == idx,
                                            onClick = { viewModel.selectAlphabetIndex(idx) },
                                            label = { Text(alpha.name, fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                                            colors = FilterChipDefaults.filterChipColors(
                                                selectedContainerColor = NeonTeal,
                                                selectedLabelColor = ObsidianMain,
                                                labelColor = TextWhite
                                            )
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                // Preview of the active alphabet
                                if (selectedIndex in allAlphabets.indices) {
                                    val active = allAlphabets[selectedIndex]
                                    Text("Preview (1 to 12):", fontSize = 11.sp, color = TextMuted)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        active.digits.forEach { digit ->
                                            Text(
                                                text = digit,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = NeonLime,
                                                fontFamily = FontFamily.Monospace
                                            )
                                        }
                                    }

                                    // Option to delete custom alphabet
                                    if (selectedIndex >= viewModel.defaultAlphabets.size) {
                                        Spacer(modifier = Modifier.height(12.dp))
                                        val interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
                                        Button(
                                            onClick = { viewModel.removeCustomAlphabet(active.name) },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = TextWarning.copy(alpha = 0.2f),
                                                contentColor = TextWarning
                                            ),
                                            shape = RoundedCornerShape(8.dp),
                                            interactionSource = interactionSource,
                                            modifier = Modifier.align(Alignment.CenterHorizontally).height(32.dp).tactilePress(enabled = enableTactileFeedback, interactionSource = interactionSource),
                                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp)
                                        ) {
                                            Icon(Icons.Default.Delete, contentDescription = "Delete Alphabet", modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Delete Custom Alphabet", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(16.dp))
                                Divider(color = NeonTeal.copy(alpha = 0.1f))
                                Spacer(modifier = Modifier.height(12.dp))

                                // Section to ADD a new custom alphabet
                                Text("Add Custom Alphabet", fontSize = 12.sp, color = TextWhite, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(8.dp))

                                var newAlphaName by remember { mutableStateOf("") }
                                var newAlphaSymbols by remember { mutableStateOf("") }
                                var addAlphabetError by remember { mutableStateOf<String?>(null) }

                                OutlinedTextField(
                                    value = newAlphaName,
                                    onValueChange = { newAlphaName = it },
                                    label = { Text("Alphabet Name (e.g. Emoji, Alien)", fontSize = 11.sp) },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = NeonTeal,
                                        unfocusedBorderColor = TextMuted.copy(alpha = 0.3f),
                                        focusedLabelColor = NeonTeal,
                                        cursorColor = NeonTeal
                                    ),
                                    textStyle = LocalTextStyle.current.copy(fontSize = 12.sp),
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                OutlinedTextField(
                                    value = newAlphaSymbols,
                                    onValueChange = { newAlphaSymbols = it },
                                    label = { Text("12 Symbols (separated by commas or spaces)", fontSize = 11.sp) },
                                    placeholder = { Text("A, B, C, D, E, F, G, H, I, J, K, L", fontSize = 11.sp, color = TextMuted) },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = NeonTeal,
                                        unfocusedBorderColor = TextMuted.copy(alpha = 0.3f),
                                        focusedLabelColor = NeonTeal,
                                        cursorColor = NeonTeal
                                    ),
                                    textStyle = LocalTextStyle.current.copy(fontSize = 12.sp),
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true
                                )

                                if (addAlphabetError != null) {
                                    Text(
                                        text = addAlphabetError ?: "",
                                        color = TextWarning,
                                        fontSize = 11.sp,
                                        modifier = Modifier.padding(top = 4.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                val saveInteract = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
                                Button(
                                    onClick = {
                                        // Parse and validate symbols
                                        val rawSymbols = if (newAlphaSymbols.contains(",")) {
                                            newAlphaSymbols.split(",")
                                        } else {
                                            newAlphaSymbols.trim().split("\\s+".toRegex())
                                        }.map { it.trim() }.filter { it.isNotEmpty() }

                                        when {
                                            newAlphaName.isBlank() -> {
                                                addAlphabetError = "Please enter a valid alphabet name"
                                            }
                                            rawSymbols.size < 12 -> {
                                                addAlphabetError = "Please provide exactly 12 symbols (found: ${rawSymbols.size})"
                                            }
                                            else -> {
                                                viewModel.addCustomAlphabet(newAlphaName, rawSymbols)
                                                // Select the newly added alphabet
                                                viewModel.selectAlphabetIndex(allAlphabets.size)
                                                newAlphaName = ""
                                                newAlphaSymbols = ""
                                                addAlphabetError = null
                                            }
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = NeonLime,
                                        contentColor = ObsidianMain
                                    ),
                                    modifier = Modifier.fillMaxWidth().height(40.dp).tactilePress(enabled = enableTactileFeedback, interactionSource = saveInteract),
                                    shape = RoundedCornerShape(8.dp),
                                    interactionSource = saveInteract
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = "Add New Alphabet", modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Add New Alphabet", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        val primaryTz by viewModel.primaryTimezone.collectAsStateWithLifecycle()
                        val detectedTz by viewModel.detectedTimezone.collectAsStateWithLifecycle()
                        var expandTz by remember { mutableStateOf(false) }

                        // Primary Timezone Settings
                        Text("PRIMARY CLOCK TIMEZONE", fontSize = 10.sp, color = TextMuted, fontWeight = FontWeight.Bold)
                        Box(modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                            OutlinedButton(
                                onClick = { expandTz = true },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = TextWhite),
                                border = BorderStroke(1.dp, ObsidianCard)
                            ) {
                                Text(
                                    if (primaryTz == "System Default") {
                                        if (detectedTz != null) "Automatic ($detectedTz)" else "Automatic (System Default)"
                                    } else {
                                        viewModel.availableWorldCities.find { it.timezoneId == primaryTz }?.cityName ?: primaryTz
                                    }
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Icon(Icons.Default.ArrowDropDown, contentDescription = "Select Timezone")
                            }

                            DropdownMenu(
                                expanded = expandTz,
                                onDismissRequest = { expandTz = false },
                                modifier = Modifier.fillMaxWidth(0.8f).background(ObsidianSurface)
                            ) {
                                DropdownMenuItem(
                                    text = { 
                                        Text(
                                            if (detectedTz != null) "Automatic ($detectedTz)" else "Automatic (System Default)", 
                                            color = TextWhite
                                        ) 
                                    },
                                    onClick = { 
                                        viewModel.setPrimaryTimezone("System Default")
                                        expandTz = false
                                    }
                                )
                                viewModel.availableWorldCities.forEach { city ->
                                    DropdownMenuItem(
                                        text = { Text("${city.cityName}, ${city.countryName} (${city.timezoneId})", color = TextWhite) },
                                        onClick = { 
                                            viewModel.setPrimaryTimezone(city.timezoneId)
                                            expandTz = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Technical explanation card of modular synthesizers
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, ObsidianCard, RoundedCornerShape(16.dp)),
                    colors = CardDefaults.cardColors(containerColor = ObsidianSurface),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "PROGRAMMATIC SYNTH PLAYER",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextMuted,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Instead of relying on heavy default MP3 files, Clock In rock synthesizes and plays standard sine/square patterns on-the-fly dynamically. You can adjust frequency, speed and pitch levels across individual alarms! Built using native AudioTrack and Coroutines.",
                            fontSize = 12.sp,
                            color = TextWhite.copy(alpha = 0.8f),
                            lineHeight = 18.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}


// =========================================================================================
// 7. REAL-TIME EVENT OVERLAYS (ALARM FIRED OVERLAY)
// =========================================================================================
@Composable
fun AlarmTriggeredOverlay(
    viewModel: ClockViewModel,
    alarm: Alarm,
    is24Hour: Boolean,
    onDismiss: () -> Unit,
    onSnooze: () -> Unit
) {
    val enableAnimations by viewModel.enableAnimations.collectAsStateWithLifecycle()
    val borderScale = if (enableAnimations) {
        val infiniteTransition = rememberInfiniteTransition(label = "pulsing")
        infiniteTransition.animateFloat(
            initialValue = 0.8f,
            targetValue = 1.3f,
            animationSpec = infiniteRepeatable(
                animation = tween(1000, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "scaling"
        ).value
    } else 1f

    Dialog(onDismissRequest = {}) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(2.dp, NeonLime, RoundedCornerShape(24.dp)),
            colors = CardDefaults.cardColors(containerColor = ObsidianMain),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Glow Pulsing Icon
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .scale(borderScale)
                        .border(3.dp, NeonLime, CircleShape)
                        .background(NeonLime.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.NotificationsActive,
                        contentDescription = "Alarm trigger active",
                        tint = NeonLime,
                        modifier = Modifier.size(48.dp)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = alarm.label.uppercase(),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = NeonTeal,
                    letterSpacing = 2.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = alarm.getFormattedTime(is24Hour),
                    fontSize = 44.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextWhite
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Sound generated dynamically • ${alarm.waveType} wave @ ${alarm.frequency.toInt()}Hz",
                    fontSize = 11.sp,
                    color = TextMuted,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(32.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Snooze action
                    Button(
                        onClick = onSnooze,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ObsidianCard,
                            contentColor = NeonAmber
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("SNOOZE (+5m)", fontWeight = FontWeight.Bold)
                    }

                    // Dismiss Alarm
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = NeonLime,
                            contentColor = ObsidianMain
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("DISMISS", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun TimerTriggeredOverlay(viewModel: ClockViewModel, onDismiss: () -> Unit) {
    val enableAnimations by viewModel.enableAnimations.collectAsStateWithLifecycle()
    val borderScale = if (enableAnimations) {
        val infiniteTransition = rememberInfiniteTransition(label = "pulsing")
        infiniteTransition.animateFloat(
            initialValue = 0.8f,
            targetValue = 1.3f,
            animationSpec = infiniteRepeatable(
                animation = tween(900, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "scaling"
        ).value
    } else 1f

    Dialog(onDismissRequest = {}) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(2.dp, NeonTeal, RoundedCornerShape(24.dp)),
            colors = CardDefaults.cardColors(containerColor = ObsidianMain),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Pulsing Timer Icon
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .scale(borderScale)
                        .border(3.dp, NeonTeal, CircleShape)
                        .background(NeonTeal.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.HourglassDisabled,
                        contentDescription = "Timer finished!",
                        tint = NeonTeal,
                        modifier = Modifier.size(48.dp)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "TIMER COMPLETED",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = NeonLime,
                    letterSpacing = 1.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "00:00:00",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextWhite
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Your countdown timer is done!",
                    fontSize = 14.sp,
                    color = TextMuted
                )

                Spacer(modifier = Modifier.height(28.dp))

                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = NeonLime,
                        contentColor = ObsidianMain
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("DISMISS TIMER ALERT", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}


// =========================================================================================
// 8. ALARM DESIGNER dialog popup
// =========================================================================================
@Composable
fun AddAlarmDialog(
    is24Hour: Boolean,
    viewModel: ClockViewModel,
    onDismiss: () -> Unit,
    onSave: (Alarm) -> Unit
) {
    var alarmHour by remember { mutableStateOf(Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) }
    var alarmMinute by remember { mutableStateOf(Calendar.getInstance().get(Calendar.MINUTE)) }
    var alarmLabel by remember { mutableStateOf("Alarm") }
    var vibrationEnabled by remember { mutableStateOf(true) }

    // Sound variables inside the alarm designer
    var waveType by remember { mutableStateOf("SINE") }
    var frequency by remember { mutableStateOf(600f) }
    var pulseSpeed by remember { mutableStateOf(400L) }
    var vibratoDepth by remember { mutableStateOf(0.15f) }
    var vibratoSpeed by remember { mutableStateOf(6.0f) }

    val daysRepeatState = remember { mutableStateListOf(false, false, false, false, false, false, false) }

    val tempSynthesizer: SoundSynthesizer = remember { SoundSynthesizer() }
    var isTestPlaying by remember { mutableStateOf(false) }

    var isLabelFocused by remember { mutableStateOf(false) }

    // Cleanup resources upon dialog exit
    DisposableEffect(Unit) {
        onDispose {
            tempSynthesizer.stop()
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
                .liquidGlassCard(viewModel, cornerRadius = 24.dp, borderColor = NeonLime),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
            shape = RoundedCornerShape(24.dp)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "CREATE NEW ALARM",
                            fontWeight = FontWeight.Bold,
                            color = NeonLime,
                            fontSize = 16.sp,
                            letterSpacing = 1.sp
                        )
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = TextMuted)
                        }
                    }
                }

                // 2. Select Time (Hour / Minute Clickers)
                item {
                    Text("SET ALARM TIME", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextMuted)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (is24Hour) {
                            TimerUnitPicker("HOUR (0-23)", alarmHour, 0, 23, viewModel = viewModel) { alarmHour = it }
                        } else {
                            var displayHour = if (alarmHour == 0) 12 else if (alarmHour > 12) alarmHour - 12 else alarmHour
                            val isPmCurrent = alarmHour >= 12

                            TimerUnitPicker("HOUR (1-12)", displayHour, 1, 12, viewModel = viewModel) { newHour ->
                                displayHour = newHour
                                alarmHour = if (isPmCurrent) {
                                    if (newHour == 12) 12 else newHour + 12
                                } else {
                                    if (newHour == 12) 0 else newHour
                                }
                            }
                        }

                        Text(
                            ":",
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            color = NeonLime,
                            modifier = Modifier.padding(horizontal = 12.dp)
                        )
                        TimerUnitPicker("MINUTE (0-59)", alarmMinute, 0, 59, viewModel = viewModel) { alarmMinute = it }

                        if (!is24Hour) {
                            Spacer(modifier = Modifier.width(16.dp))
                            val isPmCurrent = alarmHour >= 12
                            
                            Button(
                                onClick = { 
                                    alarmHour = if (!isPmCurrent) {
                                        if (alarmHour < 12) alarmHour + 12 else alarmHour
                                    } else {
                                        if (alarmHour >= 12) alarmHour - 12 else alarmHour
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isPmCurrent) NeonTeal else ObsidianSurface,
                                    contentColor = if (isPmCurrent) ObsidianMain else TextWhite
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                    text = if (isPmCurrent) "PM" else "AM",
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                // 3. Label text field
                item {
                    val labelFocusTransition = rememberInfiniteTransition(label = "LabelFocusGlow")
                    val glowingLabelColor by labelFocusTransition.animateColor(
                        initialValue = NeonTeal.copy(alpha = 0.35f),
                        targetValue = NeonTeal.copy(alpha = 0.85f),
                        animationSpec = infiniteRepeatable(
                            animation = tween(1200, easing = FastOutSlowInEasing),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "LabelGlowColor"
                    )
                    val glowingLabelWidth by animateDpAsState(
                        targetValue = if (isLabelFocused) 2.5.dp else 1.dp,
                        label = "LabelGlowWidth"
                    )

                    OutlinedTextField(
                        value = alarmLabel,
                        onValueChange = { alarmLabel = it },
                        label = { Text("Alarm Label Name", color = TextMuted) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextWhite,
                            unfocusedTextColor = TextWhite,
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent,
                            focusedContainerColor = ObsidianSurface.copy(alpha = 0.4f),
                            unfocusedContainerColor = ObsidianSurface.copy(alpha = 0.2f)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .onFocusChanged { isLabelFocused = it.isFocused }
                            .border(
                                width = glowingLabelWidth,
                                color = if (isLabelFocused) glowingLabelColor else Color.White.copy(alpha = 0.12f),
                                shape = RoundedCornerShape(12.dp)
                            )
                    )
                }

                // 4. Repeat days week selector
                item {
                    Text("REPEAT DAYS", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextMuted)
                    Spacer(modifier = Modifier.height(6.dp))
                    val daysList = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        daysList.forEachIndexed { index, day ->
                            val isSelected = daysRepeatState[index]
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(if (isSelected) NeonLime else ObsidianCard)
                                    .clickable { daysRepeatState[index] = !daysRepeatState[index] },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = day.first().toString(),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) ObsidianMain else TextWhite
                                )
                            }
                        }
                    }
                }

                // 5. Sound properties sliders inside designer
                item {
                    Text("TUNE PROGRAMMATIC ALARM SOUND", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = NeonLime)
                    Spacer(modifier = Modifier.height(8.dp))
                    SoundPresetSliders(
                        waveType = waveType,
                        onWaveTypeChange = { waveType = it },
                        frequency = frequency,
                        onFrequencyChange = { frequency = it },
                        pulseSpeed = pulseSpeed,
                        onPulseSpeedChange = { pulseSpeed = it },
                        vibratoDepth = vibratoDepth,
                        onVibratoDepthChange = { vibratoDepth = it },
                        vibratoSpeed = vibratoSpeed,
                        onVibratoSpeedChange = { vibratoSpeed = it },
                        isPreviewPlaying = isTestPlaying,
                        onTogglePreview = {
                            if (isTestPlaying) {
                                tempSynthesizer.stop()
                                isTestPlaying = false
                            } else {
                                isTestPlaying = true
                                tempSynthesizer.playPreview(
                                    freq = frequency,
                                    wave = waveType,
                                    pulseSpeed = pulseSpeed,
                                    vibDepth = vibratoDepth,
                                    vibSpeed = vibratoSpeed,
                                    durationMs = 2500L,
                                    volume = 0.5f,
                                    onDone = {
                                        isTestPlaying = false
                                    }
                                )
                            }
                        }
                    )
                }

                // Vibration settings switch
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Enable Alarm Vibration", fontSize = 13.sp, color = TextWhite)
                        GlassSwitch(
                            checked = vibrationEnabled,
                            onCheckedChange = { vibrationEnabled = it }
                        )
                    }
                }

                // Action Actions Save/Dismiss
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = onDismiss,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = ObsidianCard,
                                contentColor = TextWhite
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("CANCEL", fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = {
                                val binaryDays = daysRepeatState.joinToString("") { if (it) "1" else "0" }
                                val alarmObj = Alarm(
                                    hour = alarmHour,
                                    minute = alarmMinute,
                                    label = alarmLabel.ifEmpty { "Alarm" },
                                    isEnabled = true,
                                    daysOfWeek = binaryDays,
                                    waveType = waveType,
                                    frequency = frequency,
                                    pulseSpeedMs = pulseSpeed,
                                    vibratoDepth = vibratoDepth,
                                    vibratoSpeed = vibratoSpeed,
                                    isVibrationEnabled = vibrationEnabled
                                )
                                onSave(alarmObj)
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = NeonLime,
                                contentColor = ObsidianMain
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("SAVE", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

// =========================================================================================
// 10. DYNAMIC ISLAND (Android 16 style overlay)
// =========================================================================================
@Composable
fun DynamicIsland(viewModel: ClockViewModel, modifier: Modifier = Modifier) {
    val timerRunning by viewModel.timerRunning.collectAsStateWithLifecycle()
    val stopwatchRunning by viewModel.stopwatchRunning.collectAsStateWithLifecycle()
    val timerRemaining by viewModel.timerTimeLeftSecs.collectAsStateWithLifecycle()
    val stopwatchElapsedMs by viewModel.stopwatchElapsedTimeMs.collectAsStateWithLifecycle()
    val enableAnimations by viewModel.enableAnimations.collectAsStateWithLifecycle()
    
    val isVisible = timerRunning || stopwatchRunning
    val duration = if (enableAnimations) 400 else 0
    
    // Animate size of the island
    val islandHeight by animateDpAsState(targetValue = if (isVisible) 48.dp else 0.dp, label = "islandHeight", animationSpec = tween(duration))
    val islandWidth by animateDpAsState(targetValue = if (isVisible) 160.dp else 0.dp, label = "islandWidth", animationSpec = tween(duration))
    val alpha by animateFloatAsState(targetValue = if (isVisible) 1f else 0f, label = "islandAlpha", animationSpec = tween(duration))
    
    if (islandHeight > 0.dp) {
        Box(
            modifier = modifier
                .padding(top = 16.dp)
                .size(width = islandWidth, height = islandHeight)
                .background(Color.Black, shape = RoundedCornerShape(24.dp))
                .border(1.dp, NeonLime.copy(alpha = 0.3f), RoundedCornerShape(24.dp))
                .alpha(alpha),
            contentAlignment = Alignment.Center
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp).fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (timerRunning) {
                    Icon(
                        imageVector = Icons.Default.HourglassEmpty,
                        contentDescription = "Timer",
                        tint = NeonLime,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = String.format("%02d:%02d", timerRemaining / 60, timerRemaining % 60),
                        color = NeonLime,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                } else if (stopwatchRunning) {
                    Icon(
                        imageVector = Icons.Default.Timer,
                        contentDescription = "Stopwatch",
                        tint = NeonAmber,
                        modifier = Modifier.size(16.dp)
                    )
                    val s = (stopwatchElapsedMs / 1000) % 60
                    val m = (stopwatchElapsedMs / 60000) % 60
                    Text(
                        text = String.format("%02d:%02d", m, s),
                        color = NeonAmber,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}

@Composable
fun CyberpunkSplashScreen(onDismiss: () -> Unit) {
    var progress by remember { mutableFloatStateOf(0f) }
    var statusText by remember { mutableStateOf("INITIALIZING CHRONO CORE...") }
    val rotationTransition = rememberInfiniteTransition(label = "SplashRotation")
    
    // Smooth spin core rotation
    val spinAngle1 by rotationTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing)
        ),
        label = "coreSpin"
    )
    
    val spinAngle2 by rotationTransition.animateFloat(
        initialValue = 360f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing)
        ),
        label = "coreSpinReverse"
    )

    // Launch progress countdown simulating the futuristic diagnostics check!
    LaunchedEffect(Unit) {
        val phases = listOf(
            0.0f to "CONSTRUCTING CYBER CHRONO CORES...",
            0.15f to "SYNCHRONIZING ATOMIC ATTEMPTS...",
            0.35f to "MAPPING RELATIONAL EPOCHS...",
            0.55f to "CALIBRATING UTC INDEX ARRAYS...",
            0.75f to "STABILIZING OBSIDIAN POWER GRID...",
            0.9f to "ENGAGING NEON EMITTING CHANNELS...",
            1.0f to "CHRONOLOGY ACTIVE. ONLINE."
        )
        
        var currentPhaseIndex = 0
        while (progress < 1.0f) {
            delay(35L)
            progress += 0.02f
            if (progress > 1.0f) progress = 1.0f
            
            if (currentPhaseIndex < phases.size - 1 && progress >= phases[currentPhaseIndex + 1].first) {
                currentPhaseIndex++
                statusText = phases[currentPhaseIndex].second
            }
        }
        delay(600L) // Beautiful dramatic pause at 100%
        onDismiss()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ObsidianMainStatic)
            .clickable(enabled = false) { }, // Prevent click throughs
        contentAlignment = Alignment.Center
    ) {
        // Subtle cyber canvas grid drawings in the background
        Canvas(modifier = Modifier.fillMaxSize()) {
            val cellS = 50f
            val dotRadius = 2f
            for (x in 0..size.width.toInt() step cellS.toInt()) {
                for (y in 0..size.height.toInt() step cellS.toInt()) {
                    drawCircle(
                        color = Color.White.copy(alpha = 0.05f),
                        radius = dotRadius,
                        center = androidx.compose.ui.geometry.Offset(x.toFloat(), y.toFloat())
                    )
                }
            }
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            // Rotating double-ring high-tech emblem containing the custom new logo
            Box(
                modifier = Modifier
                    .size(175.dp),
                contentAlignment = Alignment.Center
            ) {
                // outer spinning neon ring
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawArc(
                        color = NeonLimeStatic.copy(alpha = 0.25f),
                        startAngle = spinAngle1,
                        sweepAngle = 260f,
                        useCenter = false,
                        style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
                    )
                }

                // inner spinning amber ring
                Canvas(modifier = Modifier.size(145.dp)) {
                    drawArc(
                        color = NeonAmberStatic.copy(alpha = 0.35f),
                        startAngle = spinAngle2,
                        sweepAngle = 180f,
                        useCenter = false,
                        style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                    )
                }

                // Inner core with logo image
                Card(
                    modifier = Modifier
                        .size(115.dp)
                        .border(1.dp, Color.White.copy(alpha = 0.15f), CircleShape),
                    shape = CircleShape,
                    colors = CardDefaults.cardColors(containerColor = ObsidianCardStatic)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = com.example.R.drawable.ic_launcher_foreground),
                            contentDescription = "Chrono Core System Logo",
                            modifier = Modifier
                                .size(96.dp)
                                .scale(1.2f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(36.dp))

            // Subtitles
            Text(
                text = "CLOCK IN ROCK",
                fontSize = 24.sp,
                fontWeight = FontWeight.Black,
                color = TextWhiteStatic,
                letterSpacing = 4.sp
            )

            Text(
                text = "CYBER TIME INDEX // v1.0.0",
                fontSize = 10.sp,
                color = NeonLimeStatic,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp,
                modifier = Modifier.padding(top = 4.dp, bottom = 28.dp)
            )

            // Custom glowing progress track
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .height(6.dp)
                    .background(Color.White.copy(alpha = 0.08f), RoundedCornerShape(3.dp))
                    .border(0.5.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(3.dp))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progress)
                        .fillMaxHeight()
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(NeonLimeStatic, NeonTealStatic.copy(alpha = 0.7f))
                            ),
                            shape = RoundedCornerShape(3.dp)
                        )
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Numeric progress count
            Text(
                text = "${(progress * 100).toInt()}% READY",
                fontFamily = FontFamily.Monospace,
                fontSize = 12.sp,
                fontWeight = FontWeight.Black,
                color = NeonLimeStatic
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Moving log status
            Text(
                text = statusText,
                fontFamily = FontFamily.Monospace,
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextMutedStatic,
                letterSpacing = 0.5.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.height(18.dp)
            )
        }
    }
}

// Visual tactile (haptic-like) feedback modifiers
fun Modifier.tactilePress(
    enabled: Boolean = true,
    interactionSource: androidx.compose.foundation.interaction.InteractionSource
): Modifier = composed {
    val isPressed = if (enabled) {
        val pressedState by interactionSource.collectIsPressedAsState()
        pressedState
    } else {
        false
    }
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.88f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "tactileScale"
    )
    
    val translationX = remember { Animatable(0f) }
    LaunchedEffect(isPressed) {
        if (isPressed) {
            translationX.animateTo(
                targetValue = -6f,
                animationSpec = spring(stiffness = Spring.StiffnessHigh)
            )
            translationX.animateTo(
                targetValue = 6f,
                animationSpec = spring(dampingRatio = Spring.DampingRatioHighBouncy, stiffness = Spring.StiffnessHigh)
            )
            translationX.animateTo(0f)
        }
    }
    
    if (enabled) {
        this.graphicsLayer {
            scaleX = scale
            scaleY = scale
            this.translationX = translationX.value
        }
    } else {
        this
    }
}

fun Modifier.tactileClick(
    enabled: Boolean = true,
    interactionSource: androidx.compose.foundation.interaction.MutableInteractionSource? = null,
    onClick: () -> Unit
): Modifier = composed {
    val actualInteractionSource = interactionSource ?: remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
    val isPressed = if (enabled) {
        val pressedState by actualInteractionSource.collectIsPressedAsState()
        pressedState
    } else {
        false
    }
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.88f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "tactileScale"
    )
    
    val translationX = remember { Animatable(0f) }
    LaunchedEffect(isPressed) {
        if (isPressed) {
            translationX.animateTo(
                targetValue = -6f,
                animationSpec = spring(stiffness = Spring.StiffnessHigh)
            )
            translationX.animateTo(
                targetValue = 6f,
                animationSpec = spring(dampingRatio = Spring.DampingRatioHighBouncy, stiffness = Spring.StiffnessHigh)
            )
            translationX.animateTo(0f)
        }
    }
    
    val visualModifier = if (enabled) {
        this.graphicsLayer {
            scaleX = scale
            scaleY = scale
            this.translationX = translationX.value
        }
    } else {
        this
    }
    
    visualModifier.clickable(
        interactionSource = actualInteractionSource,
        indication = androidx.compose.foundation.LocalIndication.current,
        onClick = onClick
    )
}

@Composable
fun Modifier.liquidGlassCard(
    viewModel: ClockViewModel,
    cornerRadius: androidx.compose.ui.unit.Dp = 16.dp,
    borderColor: Color = Color.White
): Modifier {
    val enableGlassEffect by viewModel.enableGlassEffect.collectAsStateWithLifecycle()
    val glassBlurStrength by viewModel.glassBlurStrength.collectAsStateWithLifecycle()
    val glassTransparency by viewModel.glassTransparency.collectAsStateWithLifecycle()
    val glassBorderThickness by viewModel.glassBorderThickness.collectAsStateWithLifecycle()

    if (!enableGlassEffect) {
        return this.border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(cornerRadius))
    }

    // Apply background blur if supported (and blur strength > 0)
    val blurModifier = if (glassBlurStrength > 0f) {
        Modifier.blur(glassBlurStrength.dp)
    } else {
        Modifier
    }

    return this
        .then(blurModifier)
        .clip(RoundedCornerShape(cornerRadius))
        .background(
            brush = androidx.compose.ui.graphics.Brush.linearGradient(
                colors = listOf(
                    Color.White.copy(alpha = glassTransparency * 1.5f),
                    Color.White.copy(alpha = glassTransparency * 0.4f)
                ),
                start = androidx.compose.ui.geometry.Offset(0f, 0f),
                end = androidx.compose.ui.geometry.Offset.Infinite
            )
        )
        .border(
            width = glassBorderThickness.dp,
            brush = androidx.compose.ui.graphics.Brush.linearGradient(
                colors = listOf(
                    borderColor.copy(alpha = (glassTransparency * 2.8f).coerceAtMost(1f)),
                    borderColor.copy(alpha = (glassTransparency * 0.7f).coerceAtMost(1f))
                ),
                start = androidx.compose.ui.geometry.Offset(0f, 0f),
                end = androidx.compose.ui.geometry.Offset.Infinite
            ),
            shape = RoundedCornerShape(cornerRadius)
        )
}

