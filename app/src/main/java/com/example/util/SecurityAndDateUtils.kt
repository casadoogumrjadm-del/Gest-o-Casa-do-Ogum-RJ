package com.example.util

import java.security.MessageDigest
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.random.Random

object SecurityUtils {
    private const val SALT = "CASA_DO_OGUM_RJ_SALT_2026"

    fun hashPassword(plainPassword: String): String {
        val input = "$SALT:${plainPassword.trim()}"
        val bytes = MessageDigest.getInstance("SHA-256").digest(input.toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }

    fun verifyPassword(plainPassword: String, storedHash: String): Boolean {
        return hashPassword(plainPassword) == storedHash
    }

    fun generateTemporaryPassword(): String {
        val code = Random.nextInt(1000, 9999)
        return "%04d".format(code)
    }

    fun cleanPin4(input: String): String {
        return input.filter { it.isDigit() }.take(4)
    }

    fun isValidPin4(input: String): Boolean {
        val digits = input.trim()
        return digits.length == 4 && digits.all { it.isDigit() }
    }

    fun cleanCpf(input: String): String {
        return input.filter { it.isDigit() }.take(11)
    }

    fun formatCpf(cpf: String): String {
        val digits = cleanCpf(cpf)
        if (digits.length != 11) return cpf
        return "${digits.substring(0, 3)}.${digits.substring(3, 6)}.${digits.substring(6, 9)}-${digits.substring(9, 11)}"
    }

    fun maskCpf(cpf: String): String {
        val digits = cleanCpf(cpf)
        if (digits.length != 11) return cpf
        return "***.${digits.substring(3, 6)}.${digits.substring(6, 9)}-**"
    }
}

data class ParsedDate(
    val day: Int,
    val month: Int,
    val year: Int
)

object DateAndCalendarUtils {
    val monthNames = listOf(
        "Janeiro", "Fevereiro", "Março", "Abril", "Maio", "Junho",
        "Julho", "Agosto", "Setembro", "Outubro", "Novembro", "Dezembro"
    )

    fun monthName(month: Int): String {
        return monthNames.getOrElse((month - 1).coerceIn(0, 11)) { "Outubro" }
    }

    fun parseDate(dateStr: String): ParsedDate? {
        val parts = dateStr.trim().split("/")
        if (parts.size != 3) return null
        val d = parts[0].toIntOrNull() ?: return null
        val m = parts[1].toIntOrNull() ?: return null
        val y = parts[2].toIntOrNull() ?: return null
        if (d !in 1..31 || m !in 1..12 || y < 1900) return null
        return ParsedDate(d, m, y)
    }

    fun formatDayMonth(dateStr: String): String {
        val parsed = parseDate(dateStr) ?: return dateStr
        return "%02d/%02d".format(parsed.day, parsed.month)
    }

    fun calculateYearsInYear(dateStr: String, targetYear: Int = 2026): Int? {
        val parsed = parseDate(dateStr) ?: return null
        val diff = targetYear - parsed.year
        return if (diff >= 0) diff else null
    }

    /**
     * Calcula automaticamente as datas de obrigação (1, 3, 7, 14 e 21 anos)
     * a partir da Data de Iniciação caso não tenham sido preenchidas manualmente.
     */
    fun computeObligationDate(initiationDate: String, yearsToAdd: Int): String {
        val parsed = parseDate(initiationDate) ?: return ""
        return "%02d/%02d/%04d".format(parsed.day, parsed.month, parsed.year + yearsToAdd)
    }

    fun currentDateTimeFormatted(): String {
        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("pt", "BR"))
        return sdf.format(Date())
    }

    fun currentDateFormatted(): String {
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale("pt", "BR"))
        return sdf.format(Date())
    }

    fun formatCurrency(value: Double): String {
        val format = NumberFormat.getCurrencyInstance(Locale("pt", "BR"))
        return format.format(value)
    }

    fun daysUntilNextOccurrence(dateStr: String, currentDay: Int = 3, currentMonth: Int = 10): Int {
        val parsed = parseDate(dateStr) ?: return 999
        val calNow = Calendar.getInstance().apply {
            set(2026, currentMonth - 1, currentDay, 0, 0, 0)
        }
        val calTarget = Calendar.getInstance().apply {
            set(2026, parsed.month - 1, parsed.day, 0, 0, 0)
        }
        if (calTarget.before(calNow)) {
            calTarget.add(Calendar.YEAR, 1)
        }
        val diffMillis = calTarget.timeInMillis - calNow.timeInMillis
        return (diffMillis / (1000 * 60 * 60 * 24)).toInt()
    }
}
