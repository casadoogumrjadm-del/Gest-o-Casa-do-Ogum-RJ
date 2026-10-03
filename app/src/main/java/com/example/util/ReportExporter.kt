package com.example.util

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import com.example.R
import java.io.File
import java.io.FileOutputStream

data class ExportResult(
    val success: Boolean,
    val filePath: String,
    val fileName: String,
    val previewContent: String,
    val message: String
)

object ReportExporter {

    fun exportToExcelCsv(
        context: Context,
        reportTitle: String,
        headers: List<String>,
        rows: List<List<String>>
    ): ExportResult {
        return try {
            val sanitizedTitle = reportTitle.lowercase()
                .replace("[^a-z0-9]+".toRegex(), "_")
                .trim('_')
            val fileName = "casa_do_ogum_${sanitizedTitle}_2026.csv"
            val dir = File(context.cacheDir, "relatorios").apply { mkdirs() }
            val file = File(dir, fileName)

            val sb = StringBuilder()
            // UTF-8 BOM for Microsoft Excel compatibility in Portuguese
            sb.append("\uFEFF")
            sb.append("CASA DO OGUM - RJ | $reportTitle\n")
            sb.append("Gerado em: ${DateAndCalendarUtils.currentDateTimeFormatted()}\n\n")
            sb.append(headers.joinToString(";") { it.replace(";", ",") }).append("\n")
            rows.forEach { row ->
                sb.append(row.joinToString(";") { it.replace(";", ",") }).append("\n")
            }

            file.writeText(sb.toString(), Charsets.UTF_8)

            ExportResult(
                success = true,
                filePath = file.absolutePath,
                fileName = fileName,
                previewContent = sb.toString().removePrefix("\uFEFF"),
                message = "Planilha Excel/CSV '$fileName' gerada com sucesso (${rows.size} registros)."
            )
        } catch (e: Exception) {
            ExportResult(
                success = false,
                filePath = "",
                fileName = "",
                previewContent = "",
                message = "Erro ao exportar planilha: ${e.localizedMessage}"
            )
        }
    }

    fun exportToPdf(
        context: Context,
        reportTitle: String,
        subtitle: String,
        summaryMetrics: List<Pair<String, String>>,
        headers: List<String>,
        rows: List<List<String>>
    ): ExportResult {
        return try {
            val sanitizedTitle = reportTitle.lowercase()
                .replace("[^a-z0-9]+".toRegex(), "_")
                .trim('_')
            val fileName = "casa_do_ogum_${sanitizedTitle}_2026.pdf"
            val dir = File(context.cacheDir, "relatorios").apply { mkdirs() }
            val file = File(dir, fileName)

            val pdfDocument = PdfDocument()
            val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 standard
            val page = pdfDocument.startPage(pageInfo)
            val canvas: Canvas = page.canvas

            val headerPaint = Paint().apply {
                color = Color.parseColor("#0B172A")
                style = Paint.Style.FILL
            }
            canvas.drawRect(0f, 0f, 595f, 96f, headerPaint)

            // Desenha o Brasão Oficial da Casa do Ogum no cabeçalho do PDF
            try {
                val logoBitmap: Bitmap? = BitmapFactory.decodeResource(context.resources, R.drawable.img_logo_oficial)
                if (logoBitmap != null) {
                    val destRect = Rect(24, 14, 88, 78)
                    canvas.drawBitmap(logoBitmap, null, destRect, null)
                }
            } catch (_: Exception) {
            }

            val goldPaint = Paint().apply {
                color = Color.parseColor("#D4AF37")
                textSize = 18f
                typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
                isAntiAlias = true
            }
            val whitePaint = Paint().apply {
                color = Color.WHITE
                textSize = 11f
                isAntiAlias = true
            }
            val bodyBoldPaint = Paint().apply {
                color = Color.parseColor("#0B172A")
                textSize = 10f
                typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
                isAntiAlias = true
            }
            val bodyPaint = Paint().apply {
                color = Color.parseColor("#1E293B")
                textSize = 9.5f
                isAntiAlias = true
            }

            canvas.drawText("CASA DO OGUM - RJ • DESDE 2016", 100f, 38f, goldPaint)
            canvas.drawText(reportTitle, 100f, 60f, whitePaint)
            canvas.drawText("$subtitle | Emitido em ${DateAndCalendarUtils.currentDateTimeFormatted()}", 100f, 78f, whitePaint)

            var y = 120f
            // Summary metrics box
            summaryMetrics.chunked(3).forEach { chunk ->
                var x = 32f
                chunk.forEach { (label, value) ->
                    canvas.drawText("$label: $value", x, y, bodyBoldPaint)
                    x += 180f
                }
                y += 20f
            }

            y += 12f
            val colWidth = (530f / headers.size.coerceAtLeast(1))
            headers.forEachIndexed { index, h ->
                canvas.drawText(h.take(20), 32f + index * colWidth, y, bodyBoldPaint)
            }
            y += 8f
            canvas.drawLine(32f, y, 562f, y, goldPaint)
            y += 16f

            rows.take(32).forEach { row ->
                row.forEachIndexed { index, cell ->
                    canvas.drawText(cell.take(22), 32f + index * colWidth, y, bodyPaint)
                }
                y += 18f
            }

            pdfDocument.finishPage(page)
            FileOutputStream(file).use { out ->
                pdfDocument.writeTo(out)
            }
            pdfDocument.close()

            val previewBuilder = StringBuilder()
            previewBuilder.append("DOCUMENTO PDF INSTITUCIONAL — CASA DO OGUM - RJ\n")
            previewBuilder.append("Título: $reportTitle ($subtitle)\n")
            previewBuilder.append("────────────────────────────────────────────\n")
            summaryMetrics.forEach { (k, v) -> previewBuilder.append("• $k: $v\n") }
            previewBuilder.append("────────────────────────────────────────────\n")
            previewBuilder.append(headers.joinToString(" | ")).append("\n")
            rows.forEach { row ->
                previewBuilder.append(row.joinToString(" | ")).append("\n")
            }

            ExportResult(
                success = true,
                filePath = file.absolutePath,
                fileName = fileName,
                previewContent = previewBuilder.toString(),
                message = "Relatório PDF '$fileName' gerado com sucesso (${rows.size} registros)."
            )
        } catch (e: Exception) {
            ExportResult(
                success = false,
                filePath = "",
                fileName = "",
                previewContent = "",
                message = "Erro ao gerar PDF: ${e.localizedMessage}"
            )
        }
    }

    fun shareTextReport(context: Context, title: String, content: String) {
        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_SUBJECT, "CASA DO OGUM - RJ: $title")
            putExtra(Intent.EXTRA_TEXT, content)
            type = "text/plain"
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        val chooser = Intent.createChooser(sendIntent, "Compartilhar Relatório").apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(chooser)
    }
}
