package com.example.expensetracker.data.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import android.util.Log
import com.example.expensetracker.ExpenseTrackerApplication
import com.example.expensetracker.data.local.ExpenseEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SmsReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Telephony.Sms.Intents.SMS_RECEIVED_ACTION) {
            val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
            if (messages.isEmpty()) return

            val application = context.applicationContext as? ExpenseTrackerApplication
            if (application == null) {
                Log.e("SmsReceiver", "Application context is not ExpenseTrackerApplication")
                return
            }

            val repository = application.container.expenseRepository
            val backupManager = application.container.backupManager

            for (sms in messages) {
                val body = sms.displayMessageBody ?: continue
                val sender = sms.displayOriginatingAddress ?: "Unknown Sender"

                // Extremely simplified regex to detect bank messages
                // Looks for "debited" or "credited" and "Rs" or "INR" followed by an amount
                val amountRegex = "(?i)(?:rs\\.?|inr)\\s*(\\d+(?:\\.\\d+)?)".toRegex()
                val debitRegex = "(?i)(debited|spent|deducted)".toRegex()
                val creditRegex = "(?i)(credited|added|received)".toRegex()

                val amountMatch = amountRegex.find(body)
                val isDebit = debitRegex.containsMatchIn(body)
                val isCredit = creditRegex.containsMatchIn(body)

                if (amountMatch != null && (isDebit || isCredit)) {
                    val amount = amountMatch.groupValues[1].toDoubleOrNull()
                    if (amount != null) {
                        val type = if (isDebit) "Expense" else "Income"
                        val descriptionSnippet = body.take(40).replace("\n", " ") + "..."

                        val expense = ExpenseEntity(
                            processId = null,
                            amount = amount,
                            description = "SMS: $descriptionSnippet",
                            date = System.currentTimeMillis(),
                            category = "General",
                            type = type,
                            receiptUri = null,
                            quantity = null,
                            unit = null
                        )

                        CoroutineScope(Dispatchers.IO).launch {
                            try {
                                repository.insertExpense(expense)
                                backupManager.triggerAutoBackup()
                                Log.d("SmsReceiver", "Successfully parsed and saved SMS expense: $amount $type")
                            } catch (e: Exception) {
                                Log.e("SmsReceiver", "Error saving SMS expense", e)
                            }
                        }
                    }
                }
            }
        }
    }
}
