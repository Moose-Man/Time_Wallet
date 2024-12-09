package com.example.time_wallet_3.view.Achievements

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.time_wallet_3.viewmodel.viewmodel
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.navigation.NavHostController
import com.example.time_wallet_3.model.Achievements
import com.example.time_wallet_3.view.TimeLogs.HeaderSection

@Composable
fun AchievementsScreen(viewModel: viewmodel, navController: NavHostController) {
    val achievements = listOf(
        Achievements(id = 1, points = 50),
        Achievements(id = 2, points = 100),
        Achievements(id = 3, points = 200),
        Achievements(id = 4, points = 400),
        Achievements(id = 5, points = 650),
        Achievements(id = 6, points = 1000),
        Achievements(id = 7, points = 1500),
        Achievements(id = 8, points = 2000),
        Achievements(id = 9, points = 3000),
        Achievements(id = 10, points = 5000),
        Achievements(id = 11, points = 8000),
        Achievements(id = 12, points = 10000),
        Achievements(id = 13, points = 13000),
        Achievements(id = 14, points = 15000),
        Achievements(id = 15,points = 20000)
    )

    val totalPoints by viewModel.totalPoints.collectAsState(initial = 0)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // Header Section
        HeaderSection(viewModel = viewModel, navController = navController)

        // Content below the header
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 56.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Achievements",
                    style = MaterialTheme.typography.headlineSmall
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Achievements grid
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(achievements) { achievement ->
                        AchievementCard(
                            achievement = achievement,
                            totalPoints = totalPoints
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AchievementCard(achievement: Achievements, totalPoints: Int) {
    val isCleared = totalPoints >= achievement.points

    Card(
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isCleared) Color.Red else Color.LightGray
        )
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Text(
                text = if (isCleared) {
                    "${achievement.points} Points\nCleared!"
                } else {
                    "${achievement.points} Points"
                },
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White,
                textAlign = TextAlign.Center
            )
        }
    }
}




