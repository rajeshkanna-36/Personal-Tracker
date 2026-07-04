package com.example.expensetracker.ui.widget

import android.content.Context
import android.content.Intent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.provideContent
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.fillMaxSize
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.text.FontWeight
import androidx.glance.unit.ColorProvider
import androidx.glance.action.clickable
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.background
import androidx.glance.layout.padding
import com.example.expensetracker.MainActivity
import com.example.expensetracker.data.local.AppDatabase
import kotlinx.coroutines.flow.first
import java.text.NumberFormat
import java.util.Locale

class WalletWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val database = AppDatabase.getDatabase(context)
        val expenseDao = database.expenseDao()
        
        val sharedPrefs = context.getSharedPreferences("expense_tracker_prefs", Context.MODE_PRIVATE)
        val initialWalletBalance = sharedPrefs.getFloat("initial_wallet_balance", 0f).toDouble()
        val totalExpense = expenseDao.getTotalGeneralExpenses().first()
        val walletBalance = initialWalletBalance - totalExpense

        val formatter = NumberFormat.getCurrencyInstance(Locale("en", "IN")).apply {
            maximumFractionDigits = 0
        }

        provideContent {
            val bgColor = ColorProvider(Color(0xFF6750A4))
            val textColor = ColorProvider(Color.White)
            
            Box(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .background(bgColor)
                    .padding(16.dp)
                    .clickable(actionStartActivity(Intent(context, MainActivity::class.java)))
            ) {
                Column {
                    Text(
                        text = "My Wallet",
                        style = TextStyle(
                            color = textColor,
                            fontWeight = FontWeight.Normal
                        )
                    )
                    Text(
                        text = formatter.format(walletBalance),
                        style = TextStyle(
                            color = textColor,
                            fontWeight = FontWeight.Bold,
                            fontSize = 24.sp
                        )
                    )
                }
            }
        }
    }
}
