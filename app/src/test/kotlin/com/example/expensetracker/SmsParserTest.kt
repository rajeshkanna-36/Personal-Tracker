package com.example.expensetracker

import org.junit.Test
import org.junit.Assert.*

class SmsParserTest {

    private fun parseSms(body: String, sender: String): Map<String, Any>? {
        val amountRegex = "(?i)(?:rs\\.?|inr|₹|usd|\\$)\\s*([\\d,]+(?:\\.\\d+)?)|([\\d,]+(?:\\.\\d+)?)\\s*(?:inr|rs\\.?|rupees)|(?:debited|credited|spent|deducted|received|paid|payment of|txn of|transfer of|by|for)\\s+(?:rs\\.?|inr|₹)?\\s*([\\d,]+(?:\\.\\d+)?)".toRegex()
        val debitRegex = "(?i)(debited|spent|deducted|withdrawn|paid|payment|sent)".toRegex()
        val creditRegex = "(?i)(credited|added|received|deposited|refund)".toRegex()

        val amountMatch = amountRegex.find(body)
        val isDebit = debitRegex.containsMatchIn(body)
        val isCredit = creditRegex.containsMatchIn(body)

        if (amountMatch != null && (isDebit || isCredit)) {
            val amountStrRaw = amountMatch.groupValues.drop(1).firstOrNull { it.isNotBlank() }
            val cleanAmountStr = amountStrRaw?.replace(",", "")?.trimEnd('.')
            val amount = cleanAmountStr?.toDoubleOrNull()
            if (amount != null) {
                val type = if (isDebit) "Expense" else "Income"
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

                return mapOf(
                    "amount" to amount,
                    "type" to type,
                    "description" to finalDescription
                )
            }
        }
        if (amountMatch == null) println("amountMatch is null for: $body")
        else println("amount failed to parse for: $body, amountMatch.value=${amountMatch.value}, groups=${amountMatch.groupValues}")
        if (!(isDebit || isCredit)) println("isDebit/isCredit is false for: $body")
        return null
    }

    @Test
    fun testSmsParsing() {
        val sms1 = parseSms("Your a/c XX1234 is debited for Rs.1500.00 on 04-07-26 and credited to Rajesh Store.", "VM-HDFC")
        println("SMS1 output: $sms1")
        assertNotNull(sms1)
        assertEquals(1500.0, sms1!!["amount"])
        assertEquals("Expense", sms1["type"])
        assertEquals("Rajesh Store", sms1["description"])
        println("SMS 1 Passed: \$sms1")

        val sms2 = parseSms("Rs 500 spent on Zomato via UPI Ref 123456789.", "AD-ICICI")
        assertNotNull(sms2)
        assertEquals(500.0, sms2!!["amount"])
        assertEquals("Expense", sms2["type"])
        println("SMS 2 Passed: \$sms2")

        val sms3 = parseSms("Credited Rs. 50,000.00 to a/c XX999 by Employer.", "SBI")
        assertNotNull(sms3)
        assertEquals(50000.0, sms3!!["amount"])
        assertEquals("Income", sms3["type"])
        println("SMS 3 Passed: \$sms3")

        val sms4 = parseSms("Payment of 1000 INR deducted for Electricity Bill.", "Axis")
        assertNotNull(sms4)
        assertEquals(1000.0, sms4!!["amount"])
        assertEquals("Expense", sms4["type"])
        println("SMS 4 Passed: \$sms4")

        val sms5 = parseSms("Hey, what's up?", "Mom")
        assertNull(sms5)
        println("SMS 5 (Non-bank) Passed: Rejected correctly")
    }
}
