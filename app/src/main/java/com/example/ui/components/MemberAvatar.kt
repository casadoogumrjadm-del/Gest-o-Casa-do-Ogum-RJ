package com.example.ui.components

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics. ImageDecoder
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.net.Uri
import android.os.Build
import android.util.Base64
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.GoldLight
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.NavyCard
import com.example.ui.theme.NavyDeep
import com.example.ui.theme.NavyElevated
import com.example.ui.theme.SilverSubtext
import com.example.ui.theme.StatusOverdueRed
import java.io.ByteArrayOutputStream
import kotlin.math.abs
import kotlin.math.min

object AvatarImageUtils {

    /**
     * Converte qualquer imagem selecionada da Galeria/Dispositivo (URI) para Base64 otimizado,
     * respeitando orientação EXIF e redimensionando com segurança mesmo para fotos de alta resolução.
     */
    fun uriToBase64Avatar(context: Context, uri: Uri, maxSizePx: Int = 256): String? {
        return try {
            val bitmap: Bitmap? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val source = ImageDecoder.createSource(context.contentResolver, uri)
                ImageDecoder.decodeBitmap(source) { decoder, info, _ ->
                    decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
                    decoder.isMutableRequired = true
                    val maxDim = maxOf(info.size.width, info.size.height)
                    if (maxDim > maxSizePx * 2) {
                        val sample = (maxDim / maxSizePx).coerceAtLeast(1)
                        decoder.setTargetSampleSize(sample)
                    }
                }
            } else {
                val boundsOptions = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                context.contentResolver.openInputStream(uri)?.use {
                    BitmapFactory.decodeStream(it, null, boundsOptions)
                }
                var sampleSize = 1
                val maxDim = maxOf(boundsOptions.outWidth, boundsOptions.outHeight)
                while (maxDim / sampleSize > maxSizePx * 2) {
                    sampleSize *= 2
                }
                val decodeOptions = BitmapFactory.Options().apply {
                    inSampleSize = sampleSize
                    inPreferredConfig = Bitmap.Config.ARGB_8888
                }
                context.contentResolver.openInputStream(uri)?.use {
                    BitmapFactory.decodeStream(it, null, decodeOptions)
                }
            }

            if (bitmap != null) {
                bitmapToBase64Avatar(bitmap, maxSizePx)
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Recorta um Bitmap em formato quadrado centralizado, redimensiona e codifica em JPEG Base64.
     */
    fun bitmapToBase64Avatar(original: Bitmap, maxSizePx: Int = 256): String? {
        return try {
            val safeBitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && original.config == Bitmap.Config.HARDWARE) {
                original.copy(Bitmap.Config.ARGB_8888, false)
            } else {
                original
            }
            val dimension = min(safeBitmap.width, safeBitmap.height).coerceAtLeast(1)
            val xOffset = (safeBitmap.width - dimension) / 2
            val yOffset = (safeBitmap.height - dimension) / 2
            val square = Bitmap.createBitmap(safeBitmap, xOffset, yOffset, dimension, dimension)
            val scaled = Bitmap.createScaledBitmap(square, maxSizePx, maxSizePx, true)

            val output = ByteArrayOutputStream()
            scaled.compress(Bitmap.CompressFormat.JPEG, 85, output)
            Base64.encodeToString(output.toByteArray(), Base64.NO_WRAP)
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Gera um Bitmap institucional para membros que ainda não possuem foto enviada,
     * garantindo que o componente Image sempre renderize um Bitmap nítido em cada item da lista.
     */
    fun createInstitutionalAvatarBitmap(
        nameOrOrunko: String,
        cargoOrOrixa: String = "Ogum",
        paletteIndex: Int = abs(nameOrOrunko.hashCode()) % 3
    ): Bitmap {
        val size = 240
        val bmp = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)

        val bgColors = listOf(
            intArrayOf(0xFF0B172A.toInt(), 0xFF1B355A.toInt()),
            intArrayOf(0xFF14243E.toInt(), 0xFF0A192F.toInt()),
            intArrayOf(0xFF1F2D46.toInt(), 0xFF091322.toInt())
        )[paletteIndex % 3]

        val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = android.graphics.LinearGradient(
                0f, 0f, size.toFloat(), size.toFloat(),
                bgColors[0], bgColors[1],
                android.graphics.Shader.TileMode.CLAMP
            )
        }
        canvas.drawRect(0f, 0f, size.toFloat(), size.toFloat(), bgPaint)

        // Anel dourado institucional
        val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            color = 0xFFD4AF37.toInt()
            strokeWidth = 6f
        }
        canvas.drawOval(RectF(10f, 10f, size - 10f, size - 10f), borderPaint)

        val innerBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            color = 0x66F3E5AB
            strokeWidth = 2f
        }
        canvas.drawOval(RectF(22f, 22f, size - 22f, size - 22f), innerBorderPaint)

        val initials = nameOrOrunko.trim()
            .split("\\s+".toRegex())
            .filter { it.isNotEmpty() }
            .take(2)
            .joinToString("") { it.first().uppercase() }
            .ifEmpty { "CO" }

        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFFF3E5AB.toInt()
            textSize = 78f
            typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }
        val yPos = (size / 2f) - ((textPaint.descent() + textPaint.ascent()) / 2f) - 8f
        canvas.drawText(initials, size / 2f, yPos, textPaint)

        val subPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFFD4AF37.toInt()
            textSize = 21f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }
        val subtitle = cargoOrOrixa.take(14).uppercase()
        canvas.drawText(subtitle, size / 2f, size - 38f, subPaint)

        return bmp
    }

    fun generateInstitutionalAvatarBase64(
        nameOrOrunko: String,
        cargoOrOrixa: String = "Ogum",
        paletteIndex: Int = abs(nameOrOrunko.hashCode()) % 3
    ): String {
        val bmp = createInstitutionalAvatarBitmap(nameOrOrunko, cargoOrOrixa, paletteIndex)
        val output = ByteArrayOutputStream()
        bmp.compress(Bitmap.CompressFormat.JPEG, 88, output)
        return Base64.encodeToString(output.toByteArray(), Base64.NO_WRAP)
    }

    /**
     * Decodifica Base64 (com ou sem prefixo data:image/...;base64,) em ImageBitmap.
     */
    fun decodeBase64ToImageBitmap(base64Str: String): ImageBitmap? {
        if (base64Str.isBlank()) return null
        return try {
            val cleanBase64 = base64Str
                .substringAfter("base64,", base64Str)
                .trim()
            val bytes = Base64.decode(cleanBase64, Base64.DEFAULT)
            val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size) ?: return null
            bitmap.asImageBitmap()
        } catch (e: Exception) {
            null
        }
    }
}

/**
 * Componente unificado de Foto de Perfil do Membro.
 * Garante que a imagem de perfil seja sempre renderizada como um ImageBitmap circular
 * com moldura dourada para cada item da lista.
 */
@Composable
fun MemberAvatar(
    fotoBase64: String,
    nameOrOrunko: String,
    size: Dp = 48.dp,
    modifier: Modifier = Modifier,
    cargoOrOrixa: String = "Casa do Ogum",
    showEditBadge: Boolean = false,
    onClick: (() -> Unit)? = null
) {
    val renderedBitmap: ImageBitmap = remember(fotoBase64, nameOrOrunko, cargoOrOrixa) {
        AvatarImageUtils.decodeBase64ToImageBitmap(fotoBase64)
            ?: AvatarImageUtils.createInstitutionalAvatarBitmap(
                nameOrOrunko = nameOrOrunko.ifBlank { "Membro" },
                cargoOrOrixa = cargoOrOrixa
            ).asImageBitmap()
    }

    Box(
        modifier = modifier
            .size(size)
            .then(
                if (onClick != null) {
                    Modifier.clickable { onClick() }
                } else {
                    Modifier
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(CircleShape)
                .background(
                    Brush.linearGradient(
                        colors = listOf(NavyElevated, NavyDeep)
                    )
                )
                .border(1.5.dp, GoldPrimary, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Image(
                bitmap = renderedBitmap,
                contentDescription = "Foto de perfil de ${nameOrOrunko.ifBlank { "Membro" }}",
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
        }

        if (showEditBadge) {
            val badgeSize = (size * 0.34f).coerceIn(18.dp, 28.dp)
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(badgeSize)
                    .clip(CircleShape)
                    .background(GoldPrimary)
                    .border(1.dp, NavyDeep, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CameraAlt,
                    contentDescription = "Alterar foto de $nameOrOrunko",
                    tint = NavyDeep,
                    modifier = Modifier.size(badgeSize * 0.6f)
                )
            }
        }
    }
}

@Composable
fun MemberPhotoPickerDialog(
    memberNameOrOrunko: String,
    memberOrixaOrCargo: String,
    currentFotoBase64: String,
    onDismiss: () -> Unit,
    onPhotoSelected: (String) -> Unit
) {
    val context = LocalContext.current
    var previewBase64 by rememberSaveable { mutableStateOf(currentFotoBase64) }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            AvatarImageUtils.uriToBase64Avatar(context, uri)?.let { encoded ->
                previewBase64 = encoded
            }
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        if (bitmap != null) {
            AvatarImageUtils.bitmapToBase64Avatar(bitmap)?.let { encoded ->
                previewBase64 = encoded
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = NavyCard,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.AddAPhoto, contentDescription = null, tint = GoldPrimary)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Foto de Perfil — $memberNameOrOrunko",
                    style = MaterialTheme.typography.titleMedium,
                    color = GoldPrimary,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MemberAvatar(
                    fotoBase64 = previewBase64,
                    nameOrOrunko = memberNameOrOrunko,
                    cargoOrOrixa = memberOrixaOrCargo,
                    size = 96.dp,
                    showEditBadge = true,
                    onClick = {
                        galleryLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    }
                )
                Text(
                    text = "Escolha uma foto da galeria do aparelho, tire uma foto com a câmera ou gere um selo personalizado:",
                    style = MaterialTheme.typography.bodySmall,
                    color = SilverSubtext
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            galleryLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = NavyDeep),
                        modifier = Modifier.weight(1f).testTag("dialog_pick_gallery_button")
                    ) {
                        Icon(Icons.Default.PhotoLibrary, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Galeria", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = { cameraLauncher.launch(null) },
                        modifier = Modifier.weight(1f).testTag("dialog_take_camera_button")
                    ) {
                        Icon(Icons.Default.CameraAlt, contentDescription = null, tint = GoldPrimary, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Câmera", color = GoldLight, style = MaterialTheme.typography.labelMedium)
                    }
                }

                OutlinedButton(
                    onClick = {
                        previewBase64 = AvatarImageUtils.generateInstitutionalAvatarBase64(
                            nameOrOrunko = memberNameOrOrunko,
                            cargoOrOrixa = memberOrixaOrCargo
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = GoldPrimary, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Gerar Brasão de Perfil com Iniciais", color = GoldLight, style = MaterialTheme.typography.labelMedium)
                }

                if (previewBase64.isNotBlank()) {
                    TextButton(
                        onClick = { previewBase64 = "" }
                    ) {
                        Icon(Icons.Default.DeleteOutline, contentDescription = null, tint = StatusOverdueRed, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Remover Foto Personalizada", color = StatusOverdueRed, style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onPhotoSelected(previewBase64)
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = NavyDeep),
                modifier = Modifier.testTag("confirm_photo_dialog_button")
            ) {
                Text("Salvar Foto", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = SilverSubtext)
            }
        }
    )
}
