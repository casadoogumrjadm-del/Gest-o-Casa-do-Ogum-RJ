package com.example.ui.screens

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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Dialpad
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.PessoaEntity
import com.example.ui.theme.GoldLight
import com.example.ui.theme.GoldMuted
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.IvoryText
import com.example.ui.theme.NavyCard
import com.example.ui.theme.NavyDeep
import com.example.ui.theme.NavyElevated
import com.example.ui.theme.NavyPrimary
import com.example.ui.theme.SilverSubtext
import com.example.ui.theme.StatusOverdueRed
import com.example.util.SecurityUtils

@Composable
fun LoginScreen(
    erroLogin: String?,
    onLogin: (String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    var cpf by rememberSaveable { mutableStateOf("") }
    var senha by rememberSaveable { mutableStateOf("") }
    var senhaVisivel by rememberSaveable { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(NavyDeep, NavyPrimary, NavyCard)
                )
            ),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 560.dp)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Logotipo Oficial em Destaque — CASA OGUM • DESDE 2016
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.5.dp, GoldPrimary.copy(alpha = 0.75f), RoundedCornerShape(22.dp)),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = NavyDeep)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 20.dp, horizontal = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.img_logo_oficial),
                            contentDescription = "Logotipo Oficial Casa do Ogum - RJ (Desde 2016)",
                            modifier = Modifier
                                .size(136.dp)
                                .clip(CircleShape)
                                .border(2.5.dp, GoldPrimary, CircleShape),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "CASA DO OGUM - RJ",
                            style = MaterialTheme.typography.headlineMedium,
                            color = GoldPrimary,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "Sistema Institucional de Gestão • Desde 2016",
                            style = MaterialTheme.typography.bodySmall,
                            color = GoldLight
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Card Principal de Login (CPF + Senha Numérica de 4 Dígitos)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, GoldMuted.copy(alpha = 0.45f), RoundedCornerShape(18.dp)),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = NavyCard.copy(alpha = 0.96f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = "Acesso Seguro",
                            tint = GoldPrimary,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Identificação de Acesso",
                            style = MaterialTheme.typography.titleLarge,
                            color = GoldLight
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Informe seu CPF e sua senha numérica de 4 números.",
                        style = MaterialTheme.typography.bodySmall,
                        color = SilverSubtext,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedTextField(
                        value = cpf,
                        onValueChange = { novo ->
                            cpf = novo.filter { it.isDigit() || it == '.' || it == '-' }.take(14)
                        },
                        label = { Text("CPF (somente números)") },
                        placeholder = { Text("Digite os 11 números do seu CPF") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "CPF",
                                tint = GoldPrimary
                            )
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Next
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GoldPrimary,
                            unfocusedBorderColor = GoldMuted,
                            focusedLabelColor = GoldPrimary,
                            unfocusedLabelColor = SilverSubtext
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("cpf_input")
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Campo de Senha de 4 Números (Teclado Exclusivamente Numérico)
                    OutlinedTextField(
                        value = senha,
                        onValueChange = { novo ->
                            senha = SecurityUtils.cleanPin4(novo)
                        },
                        label = { Text("Senha (4 números)") },
                        placeholder = { Text("•••• (apenas 4 números)") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "Senha de 4 números",
                                tint = GoldPrimary
                            )
                        },
                        trailingIcon = {
                            IconButton(onClick = { senhaVisivel = !senhaVisivel }) {
                                Icon(
                                    imageVector = if (senhaVisivel) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = "Alternar visibilidade da senha",
                                    tint = GoldLight
                                )
                            }
                        },
                        singleLine = true,
                        visualTransformation = if (senhaVisivel) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.NumberPassword,
                            imeAction = ImeAction.Done
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GoldPrimary,
                            unfocusedBorderColor = GoldMuted,
                            focusedLabelColor = GoldPrimary,
                            unfocusedLabelColor = SilverSubtext
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("password_input")
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Indicador Visual dos 4 Dígitos + Teclado Numérico Exclusivo Integrado
                    Pin4IndicatorRow(pin = senha, visible = senhaVisivel)

                    Spacer(modifier = Modifier.height(12.dp))

                    NumericPinKeypad(
                        onDigitClick = { digit ->
                            if (senha.length < 4) {
                                senha += digit
                            }
                        },
                        onBackspace = {
                            if (senha.isNotEmpty()) {
                                senha = senha.dropLast(1)
                            }
                        },
                        onClear = { senha = "" }
                    )

                    if (erroLogin != null) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Surface(
                            color = StatusOverdueRed.copy(alpha = 0.18f),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = erroLogin,
                                style = MaterialTheme.typography.bodySmall,
                                color = ColorErrorLight,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = { onLogin(cpf, senha) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = GoldPrimary,
                            contentColor = NavyDeep
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("login_button")
                    ) {
                        Text(
                            text = "ENTRAR NA CASA",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Rodapé Institucional de Segurança
            Surface(
                color = NavyElevated.copy(alpha = 0.55f),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, GoldMuted.copy(alpha = 0.3f), RoundedCornerShape(14.dp))
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Key,
                        contentDescription = null,
                        tint = GoldPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Acesso exclusivo para membros e administração da Casa do Ogum - RJ. No primeiro acesso, utilize a senha temporária de 4 números fornecida pela Administração.",
                        style = MaterialTheme.typography.bodySmall,
                        color = SilverSubtext
                    )
                }
            }
        }
    }
}

@Composable
private fun Pin4IndicatorRow(
    pin: String,
    visible: Boolean
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (i in 0 until 4) {
            val char = pin.getOrNull(i)
            val isFilled = char != null
            Surface(
                color = if (isFilled) GoldPrimary.copy(alpha = 0.22f) else NavyDeep,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .size(width = 52.dp, height = 52.dp)
                    .border(
                        width = if (isFilled) 2.dp else 1.dp,
                        color = if (isFilled) GoldPrimary else GoldMuted.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(12.dp)
                    )
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = when {
                            char == null -> "–"
                            visible -> char.toString()
                            else -> "●"
                        },
                        style = MaterialTheme.typography.headlineSmall,
                        color = if (isFilled) GoldPrimary else SilverSubtext,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun NumericPinKeypad(
    onDigitClick: (String) -> Unit,
    onBackspace: () -> Unit,
    onClear: () -> Unit
) {
    val rows = listOf(
        listOf("1", "2", "3"),
        listOf("4", "5", "6"),
        listOf("7", "8", "9")
    )
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(NavyDeep.copy(alpha = 0.55f), RoundedCornerShape(14.dp))
            .border(1.dp, GoldMuted.copy(alpha = 0.35f), RoundedCornerShape(14.dp))
            .padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Default.Dialpad,
                contentDescription = null,
                tint = GoldPrimary,
                modifier = Modifier.size(15.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "Teclado Numérico de Senha (4 Números)",
                style = MaterialTheme.typography.labelSmall,
                color = GoldLight
            )
        }
        rows.forEach { rowDigits ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                rowDigits.forEach { d ->
                    NumericKeyButton(
                        label = d,
                        onClick = { onDigitClick(d) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .clickable { onClear() }
                    .border(1.dp, GoldMuted.copy(alpha = 0.4f), RoundedCornerShape(10.dp)),
                color = NavyElevated,
                shape = RoundedCornerShape(10.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = "Limpar",
                        style = MaterialTheme.typography.labelMedium,
                        color = SilverSubtext,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
            NumericKeyButton(
                label = "0",
                onClick = { onDigitClick("0") },
                modifier = Modifier.weight(1f)
            )
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .clickable { onBackspace() }
                    .border(1.dp, GoldMuted.copy(alpha = 0.4f), RoundedCornerShape(10.dp)),
                color = NavyElevated,
                shape = RoundedCornerShape(10.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Backspace,
                        contentDescription = "Apagar dígito",
                        tint = GoldLight,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun NumericKeyButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .height(48.dp)
            .clip(RoundedCornerShape(10.dp))
            .clickable { onClick() }
            .border(1.dp, GoldPrimary.copy(alpha = 0.45f), RoundedCornerShape(10.dp))
            .testTag("key_$label"),
        color = NavyCard,
        shape = RoundedCornerShape(10.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = label,
                style = MaterialTheme.typography.titleLarge,
                color = IvoryText,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

private val ColorErrorLight = androidx.compose.ui.graphics.Color(0xFFFFB4AB)

@Composable
private fun DemoCredentialRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    badge: String,
    orunko: String,
    cpf: String,
    senha: String,
    tag: String,
    onSelect: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .clickable { onSelect() }
            .padding(vertical = 6.dp, horizontal = 8.dp)
            .testTag(tag),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = badge,
                tint = GoldPrimary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = "$badge — $orunko",
                    style = MaterialTheme.typography.labelLarge,
                    color = GoldPrimary
                )
                Text(
                    text = "CPF: $cpf  •  Senha (4 números): $senha",
                    style = MaterialTheme.typography.bodySmall,
                    color = SilverSubtext
                )
            }
        }
        Text(
            text = "Entrar →",
            style = MaterialTheme.typography.labelMedium,
            color = GoldLight,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun FirstAccessPasswordChangeScreen(
    usuario: PessoaEntity,
    erro: String?,
    onConfirmNewPassword: (String, String) -> Unit,
    onCancelLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    var novaSenha by rememberSaveable { mutableStateOf("") }
    var confirmarSenha by rememberSaveable { mutableStateOf("") }
    var campoAtivo by rememberSaveable { mutableIntStateOf(0) } // 0 = novaSenha, 1 = confirmarSenha

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(NavyDeep, NavyPrimary))),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 520.dp)
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
                .border(1.5.dp, GoldPrimary, RoundedCornerShape(20.dp)),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = NavyCard)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(22.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_logo_oficial),
                    contentDescription = "Logotipo Oficial Casa do Ogum - RJ",
                    modifier = Modifier
                        .size(84.dp)
                        .clip(CircleShape)
                        .border(2.dp, GoldPrimary, CircleShape),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "PRIMEIRO ACESSO",
                    style = MaterialTheme.typography.headlineSmall,
                    color = GoldPrimary,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Bem-vindo(a), ${usuario.orunko}!",
                    style = MaterialTheme.typography.titleMedium,
                    color = GoldLight
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Defina agora sua nova senha pessoal contendo exatamente 4 números. A senha temporária deixará de valer imediatamente.",
                    style = MaterialTheme.typography.bodySmall,
                    color = SilverSubtext,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = novaSenha,
                    onValueChange = {
                        campoAtivo = 0
                        novaSenha = SecurityUtils.cleanPin4(it)
                    },
                    label = { Text("Nova Senha (4 números)") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.NumberPassword,
                        imeAction = ImeAction.Next
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GoldPrimary,
                        unfocusedBorderColor = if (campoAtivo == 0) GoldPrimary else GoldMuted
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { campoAtivo = 0 }
                        .testTag("first_access_new_password")
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = confirmarSenha,
                    onValueChange = {
                        campoAtivo = 1
                        confirmarSenha = SecurityUtils.cleanPin4(it)
                    },
                    label = { Text("Confirmar Nova Senha (4 números)") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.NumberPassword,
                        imeAction = ImeAction.Done
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GoldPrimary,
                        unfocusedBorderColor = if (campoAtivo == 1) GoldPrimary else GoldMuted
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { campoAtivo = 1 }
                        .testTag("first_access_confirm_password")
                )

                Spacer(modifier = Modifier.height(12.dp))

                NumericPinKeypad(
                    onDigitClick = { d ->
                        if (campoAtivo == 0) {
                            if (novaSenha.length < 4) {
                                novaSenha += d
                                if (novaSenha.length == 4) campoAtivo = 1
                            }
                        } else {
                            if (confirmarSenha.length < 4) {
                                confirmarSenha += d
                            }
                        }
                    },
                    onBackspace = {
                        if (campoAtivo == 1 && confirmarSenha.isNotEmpty()) {
                            confirmarSenha = confirmarSenha.dropLast(1)
                        } else if (campoAtivo == 1 && confirmarSenha.isEmpty()) {
                            campoAtivo = 0
                            if (novaSenha.isNotEmpty()) novaSenha = novaSenha.dropLast(1)
                        } else if (novaSenha.isNotEmpty()) {
                            novaSenha = novaSenha.dropLast(1)
                        }
                    },
                    onClear = {
                        novaSenha = ""
                        confirmarSenha = ""
                        campoAtivo = 0
                    }
                )

                if (erro != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = erro,
                        style = MaterialTheme.typography.bodySmall,
                        color = ColorErrorLight,
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { onConfirmNewPassword(novaSenha, confirmarSenha) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GoldPrimary,
                        contentColor = NavyDeep
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("confirm_first_access_button")
                ) {
                    Text(
                        text = "SALVAR SENHA DE 4 NÚMEROS E ENTRAR",
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedButton(
                    onClick = onCancelLogout,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Voltar para o Login", color = SilverSubtext)
                }
            }
        }
    }
}
