package com.example.expensetracker.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.ui.graphics.vector.ImageVector

data class HabitIconItem(
    val key: String,
    val icon: ImageVector,
    val label: String
)

val habitIconsList = listOf(
    HabitIconItem("running", Icons.Rounded.DirectionsRun, "Running"),
    HabitIconItem("walking", Icons.Rounded.DirectionsWalk, "Walking"),
    HabitIconItem("gym", Icons.Rounded.FitnessCenter, "Gym"),
    HabitIconItem("cycling", Icons.Rounded.DirectionsBike, "Cycling"),
    HabitIconItem("swimming", Icons.Rounded.Pool, "Swimming"),
    HabitIconItem("yoga", Icons.Rounded.SelfImprovement, "Yoga"),
    HabitIconItem("meditation", Icons.Rounded.Spa, "Meditation"),
    HabitIconItem("reading", Icons.Rounded.MenuBook, "Reading"),
    HabitIconItem("writing", Icons.Rounded.Edit, "Writing"),
    HabitIconItem("water", Icons.Rounded.WaterDrop, "Water"),
    HabitIconItem("sleep", Icons.Rounded.Bedtime, "Sleep"),
    HabitIconItem("food", Icons.Rounded.Restaurant, "Food"),
    HabitIconItem("medicine", Icons.Rounded.Medication, "Medicine"),
    HabitIconItem("heart", Icons.Rounded.Favorite, "Heart"),
    HabitIconItem("music", Icons.Rounded.MusicNote, "Music"),
    HabitIconItem("study", Icons.Rounded.School, "Study"),
    HabitIconItem("code", Icons.Rounded.Code, "Code"),
    HabitIconItem("target", Icons.Rounded.TrackChanges, "Target"),
    HabitIconItem("star", Icons.Rounded.Star, "Star"),
    HabitIconItem("brush", Icons.Rounded.Brush, "Art"),
    HabitIconItem("nature", Icons.Rounded.Park, "Nature"),
    HabitIconItem("pet", Icons.Rounded.Pets, "Pet Care"),
    HabitIconItem("work", Icons.Rounded.Work, "Work"),
    HabitIconItem("timer", Icons.Rounded.Timer, "Timer")
)

val habitIconsMap: Map<String, ImageVector> = habitIconsList.associate { it.key to it.icon }

fun getHabitIcon(key: String): ImageVector {
    return habitIconsMap[key] ?: Icons.Rounded.Star
}
