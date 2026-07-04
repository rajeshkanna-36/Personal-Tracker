package com.example.expensetracker.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation3.runtime.NavKey
import com.example.expensetracker.ui.viewmodel.ExpenseViewModel
import com.example.expensetracker.ui.viewmodel.HabitViewModel
import com.example.expensetracker.ui.viewmodel.MemoryViewModel
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.haze
import dev.chrisbanes.haze.hazeChild
import dev.chrisbanes.haze.HazeStyle

@Composable
fun MainScreen(
    expenseViewModel: ExpenseViewModel,
    habitViewModel: HabitViewModel,
    memoryViewModel: MemoryViewModel,
    isDarkTheme: Boolean = true,
    onThemeToggle: () -> Unit = {},
    onNavigate: (NavKey) -> Unit
) {
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    val hazeState = remember { HazeState() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Main Content Layer
        Box(
            modifier = Modifier
                .fillMaxSize()
                .haze(state = hazeState)
                .padding(top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding() + 6.dp)
        ) {
            AnimatedContent(
                targetState = selectedTab,
                transitionSpec = {
                    val slideDirection = if (targetState > initialState) 1 else -1
                    (slideInHorizontally(
                        animationSpec = tween(400),
                        initialOffsetX = { fullWidth -> slideDirection * fullWidth }
                    ) + fadeIn(animationSpec = tween(400))).togetherWith(
                        slideOutHorizontally(
                            animationSpec = tween(400),
                            targetOffsetX = { fullWidth -> -slideDirection * fullWidth }
                        ) + fadeOut(animationSpec = tween(400))
                    )
                },
                label = "tab_animation"
            ) { targetTab ->
                when (targetTab) {
                    0 -> MonthlyExpensesScreen(
                        viewModel = expenseViewModel,
                        isDarkTheme = isDarkTheme,
                        onThemeToggle = onThemeToggle,
                        onNavigate = onNavigate
                    )
                    1 -> DashboardScreen(
                        viewModel = expenseViewModel,
                        isDarkTheme = isDarkTheme,
                        onThemeToggle = onThemeToggle,
                        onNavigate = onNavigate
                    )
                    2 -> HabitTrackerScreen(
                        viewModel = habitViewModel,
                        isDarkTheme = isDarkTheme,
                        onThemeToggle = onThemeToggle,
                        onNavigate = onNavigate
                    )
                    3 -> MemoryScreen(
                        viewModel = memoryViewModel,
                        isDarkTheme = isDarkTheme,
                        onThemeToggle = onThemeToggle
                    )
                }
            }
        }

        // Floating Navbar Layer
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp, vertical = 20.dp),
            contentAlignment = Alignment.Center
        ) {
            // Apple Liquid Glass floating pill
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.95f)
                    .shadow(
                        elevation = 12.dp, // Approximation for 0 6px 6px and 0 0 20px shadows
                        shape = CircleShape,
                        spotColor = Color.Black.copy(alpha = 0.2f),
                        ambientColor = Color.Black.copy(alpha = 0.1f)
                    )
                    .clip(CircleShape)
                    .hazeChild(
                        state = hazeState,
                        shape = CircleShape,
                        style = HazeStyle(
                            tint = Color.Transparent, 
                            blurRadius = 12.dp,
                            noiseFactor = 0f
                        )
                    )
                    .background(
                        color = Color.Transparent
                    )
                    .border(
                        width = 1.dp,
                        color = Color.White.copy(alpha = 0.2f),
                        shape = CircleShape
                    )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp)
                        .padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    PremiumIosNavItem(
                        icon = if (selectedTab == 0) Icons.Filled.CalendarMonth else Icons.Outlined.CalendarMonth,
                        label = "Monthly",
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 }
                    )
                    PremiumIosNavItem(
                        icon = if (selectedTab == 1) Icons.Filled.Folder else Icons.Outlined.Folder,
                        label = "Projects",
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 }
                    )
                    PremiumIosNavItem(
                        icon = if (selectedTab == 2) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        label = "Habits",
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 }
                    )
                    PremiumIosNavItem(
                        icon = if (selectedTab == 3) Icons.Filled.Lock else Icons.Outlined.Lock,
                        label = "Vault",
                        selected = selectedTab == 3,
                        onClick = { selectedTab = 3 }
                    )
                }
            }
        }
    }
}

@Composable
fun PremiumIosNavItem(
    icon: ImageVector,
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    // Scale animation for bounce effect on press
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.85f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "scale"
    )
    
    // Smooth color transition
    val activeColor = MaterialTheme.colorScheme.primary
    val inactiveColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
    
    val tintColor by animateColorAsState(
        targetValue = if (selected) activeColor else inactiveColor,
        animationSpec = tween(durationMillis = 200),
        label = "color"
    )

    // Animated background pill for selected state
    val backgroundColor by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f) else Color.Transparent,
        animationSpec = tween(durationMillis = 200),
        label = "background"
    )

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50)) // fully rounded pill
            .background(backgroundColor)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 24.dp, vertical = 12.dp)
            .scale(scale),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = tintColor,
            modifier = Modifier.size(28.dp) // slightly larger icons since there is no text
        )
    }
}
