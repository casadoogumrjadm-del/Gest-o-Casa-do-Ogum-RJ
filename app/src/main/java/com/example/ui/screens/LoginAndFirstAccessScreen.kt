package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
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
    onOpenWebBrowser: () -> Unit = {},
    onExportWebHtml: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var cpf by rememberSaveable { mutableStateOf("") }
    var senha by rememberSaveable { mutableStateOf("") }
    var senhaVisivel by rememberSaveable { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current

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
                .padding(horizontal = 20.dp, vertical = 24.dp),
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
                        .padding(vertical = 22.dp, horizontal = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.img_logo_oficial),
                            contentDescription = "Logotipo Oficial Casa do Ogum - RJ (Desde 2016)",
                            modifier = Modifier
                                .size(144.dp)
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

            Spacer(modifier = Modifier.height(18.dp))

            // Card Principal de Login (CPF + Senha Numérica de 4 Dígitos pelo Teclado do Aparelho)
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
                        text = "Toque nos campos abaixo para digitar seu CPF e sua senha de 4 números usando o teclado numérico do seu aparelho.",
                        style = MaterialTheme.typography.bodySmall,
                        color = SilverSubtext,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

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

                    // Campo de Senha de 4 Números (Abre o Teclado Numérico do Próprio Aparelho)
                    OutlinedTextField(
                        value = senha,
                        onValueChange = { novo ->
                            senha = SecurityUtils.cleanPin4(novo)
                        },
                        label = { Text("Senha (4 números)") },
                        placeholder = { Text("Digite os 4 números da senha") },
                        supportingText = {
                            Text(
                                text = "${senha.length}/4 dígitos numéricos",
                                color = SilverSubtext
                            )
                        },
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
                        keyboardActions = KeyboardActions(
                            onDone = {
                                focusManager.clearFocus()
                                onLogin(cpf, senha)
                            }
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
                        onClick = {
                            focusManager.clearFocus()
                            onLogin(cpf, senha)
                        },
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

            Spacer(modifier = Modifier.height(12.dp))

            Surface(
                color = NavyCard,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, GoldPrimary.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "🌐 Versão para Navegador (Chrome • Safari • Edge • IE)",
                        style = MaterialTheme.typography.labelLarge,
                        color = GoldPrimary,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = onOpenWebBrowser,
                            colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = NavyDeep),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("open_web_mode_login_button")
                        ) {
                            Text("Abrir Modo Web", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                        }
                        OutlinedButton(
                            onClick = onExportWebHtml,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("export_web_html_login_button")
                        ) {
                            Text("Baixar .HTML", style = MaterialTheme.typography.labelSmall, color = GoldLight)
                        }
                    }
                }
            }
        }
    }
}

private val ColorErrorLight = androidx.compose.ui.graphics.Color(0xFFFFB4AB)

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
    var senhaVisivel by rememberSaveable { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current

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
                    text = "Defina agora sua nova senha pessoal contendo exatamente 4 números usando o teclado numérico do seu aparelho. A senha temporária deixará de valer imediatamente.",
                    style = MaterialTheme.typography.bodySmall,
                    color = SilverSubtext,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = novaSenha,
                    onValueChange = {
                        novaSenha = SecurityUtils.cleanPin4(it)
                    },
                    label = { Text("Nova Senha (4 números)") },
                    placeholder = { Text("Digite 4 números") },
                    singleLine = true,
                    visualTransformation = if (senhaVisivel) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { senhaVisivel = !senhaVisivel }) {
                            Icon(
                                imageVector = if (senhaVisivel) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = "Alternar visibilidade",
                                tint = GoldLight
                            )
                        }
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.NumberPassword,
                        imeAction = ImeAction.Next
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GoldPrimary,
                        unfocusedBorderColor = GoldMuted
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("first_access_new_password")
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = confirmarSenha,
                    onValueChange = {
                        confirmarSenha = SecurityUtils.cleanPin4(it)
                    },
                    label = { Text("Confirmar Nova Senha (4 números)") },
                    placeholder = { Text("Repita os 4 números") },
                    singleLine = true,
                    visualTransformation = if (senhaVisivel) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.NumberPassword,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            focusManager.clearFocus()
                            onConfirmNewPassword(novaSenha, confirmarSenha)
                        }
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GoldPrimary,
                        unfocusedBorderColor = GoldMuted
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("first_access_confirm_password")
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

                Spacer(modifier = Modifier.height(18.dp))

                Button(
                    onClick = {
                        focusManager.clearFocus()
                        onConfirmNewPassword(novaSenha, confirmarSenha)
                    },
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
