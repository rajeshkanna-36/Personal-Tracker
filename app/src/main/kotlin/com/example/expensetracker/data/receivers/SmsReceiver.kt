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

                // Improved regex to detect bank messages
                // Handles Rs, INR, ₹, amounts with commas, suffix currencies, and amounts directly after action keywords
                val amountRegex = "(?i)(?:rs\\.?|inr|₹|usd|\\$)\\s*([\\d,]+(?:\\.\\d+)?)|([\\d,]+(?:\\.\\d+)?)\\s*(?:inr|rs\\.?|rupees)|(?:debited|credited|spent|deducted|received|paid|payment of|txn of|transfer of|by|for)\\s+(?:rs\\.?|inr|₹)?\\s*([\\d,]+(?:\\.\\d+)?)".toRegex()
                val debitRegex = "(?i)(debited|spent|deducted|withdrawn|paid|payment|sent)".toRegex()
                val creditRegex = "(?i)(credited|added|received|deposited|refund)".toRegex()

                val amountMatch = amountRegex.find(body)
                val isDebit = debitRegex.containsMatchIn(body)
                val isCredit = creditRegex.containsMatchIn(body)

                if (amountMatch != null && (isDebit || isCredit)) {
                    // Extract just the digits and decimal point from the matched string
                    val amountStr = amountMatch.value.replace(Regex("[^\\d.]"), "")
                    // Clean up multiple decimals if they accidentally got merged
                    val cleanAmountStr = amountStr.trimEnd('.')
                    val amount = cleanAmountStr.toDoubleOrNull()
                    if (amount != null) {
                        val type = if (isDebit) "Expense" else "Income"
                        // Extract merchant or recipient name
                        val merchantRegex = "(?i)(?:credited to|paid to|sent to|at)\\s+([A-Za-z0-9\\s@&]+?)\\s*(?:via|on|ref|upi|\\.)".toRegex()
                        val upiRefRegex = "(?i)(?:upi ref no\\.?|ref no\\.?)\\s*(\\d+)".toRegex()

                        val merchantMatch = merchantRegex.find(body)
                        val upiRefMatch = upiRefRegex.find(body)

                        val extractedName = merchantMatch?.groupValues?.get(1)?.trim()
                        val upiRef = upiRefMatch?.groupValues?.get(1)?.trim()

                        val finalDescription = when {
                            extractedName != null && !extractedName.contains("your ac", ignoreCase = true) && !extractedName.contains("your a/c", ignoreCase = true) -> extractedName
                            upiRef != null -> "Bank Txn (Ref: $upiRef)"
                            else -> {
                                val shortBank = sender.takeLast(6).replace(Regex("[^A-Za-z]"), "")
                                if (shortBank.isNotBlank()) "Bank Txn ($shortBank)" else "Bank Transaction"
                            }
                        }

                        val expense = ExpenseEntity(
                            processId = null,
                            amount = amount,
                            description = finalDescription,
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
