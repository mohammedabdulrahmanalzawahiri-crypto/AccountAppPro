package com.abdelrahman.accountpromax.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File

@Serializable
data class BackupTransaction(
    val name: String,
    val amount: Double,
    val type: String,
    val date: String,
    val desc: String
)

object BackupManager {
    fun exportJson(context: Context, rows: List<BackupTransaction>): File {
        val file = File(context.cacheDir, "backup_account.json")
        file.writeText(Json.encodeToString(rows))
        return file
    }

    fun exportExcel(context: Context, rows: List<BackupTransaction>): File {
        val file = File(context.cacheDir, "backup_account.xlsx")
        val wb = XSSFWorkbook()
        val sheet = wb.createSheet("transactions")
        rows.forEachIndexed { i, r ->
            val row = sheet.createRow(i)
            row.createCell(0).setCellValue(r.name)
            row.createCell(1).setCellValue(r.amount)
            row.createCell(2).setCellValue(r.type)
            row.createCell(3).setCellValue(r.date)
            row.createCell(4).setCellValue(r.desc)
        }
        file.outputStream().use { wb.write(it) }
        wb.close()
        return file
    }

    fun exportWord(context: Context, rows: List<BackupTransaction>): File {
        val file = File(context.cacheDir, "backup_account.doc")
        file.writeText(rows.joinToString("\n") { "${it.date} | ${it.name} | ${it.type} | ${it.amount} | ${it.desc}" })
        return file
    }

    fun shareFile(context: Context, file: File, target: String? = null) {
        val uri: Uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "*/*"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            if (!target.isNullOrEmpty()) `package` = target
        }
        if (!target.isNullOrEmpty()) {
            val resolved = context.packageManager.resolveActivity(intent, 0)
            if (resolved != null) {
                context.startActivity(intent)
                return
            }
        }
        context.startActivity(Intent.createChooser(intent, "مشاركة النسخة الاحتياطية"))
    }

    fun importJson(context: Context, uri: Uri): List<BackupTransaction> {
        val text = context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }.orEmpty()
        if (text.isBlank()) return emptyList()
        return Json.decodeFromString(text)
    }

    fun importExcel(context: Context, uri: Uri): List<BackupTransaction> {
        val input = context.contentResolver.openInputStream(uri) ?: return emptyList()
        input.use { stream ->
            val wb = XSSFWorkbook(stream)
            val sheet = wb.getSheetAt(0)
            val rows = mutableListOf<BackupTransaction>()
            for (i in 0..sheet.lastRowNum) {
                val r = sheet.getRow(i) ?: continue
                rows.add(
                    BackupTransaction(
                        name = r.getCell(0)?.toString().orEmpty(),
                        amount = r.getCell(1)?.numericCellValue ?: 0.0,
                        type = r.getCell(2)?.toString().orEmpty(),
                        date = r.getCell(3)?.toString().orEmpty(),
                        desc = r.getCell(4)?.toString().orEmpty()
                    )
                )
            }
            wb.close()
            return rows
        }
    }
}
