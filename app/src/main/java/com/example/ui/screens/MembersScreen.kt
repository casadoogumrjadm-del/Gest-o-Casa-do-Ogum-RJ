package com.example.ui.screens

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
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.FirestoreConnectionState
import com.example.data.FirestoreStatusInfo
import com.example.data.PessoaEntity
import com.example.ui.components.AvatarImageUtils
import com.example.ui.components.MemberAvatar
import com.example.ui.components.MemberPhotoPickerDialog
import com.example.ui.theme.GoldLight
import com.example.ui.theme.GoldMuted
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.IvoryText
import com.example.ui.theme.NavyCard
import com.example.ui.theme.NavyDeep
import com.example.ui.theme.NavyElevated
import com.example.ui.theme.SilverSubtext
import com.example.ui.theme.StatusOverdueRed
import com.example.ui.theme.StatusPaidGreen
import com.example.ui.theme.StatusPendingYellow
import com.example.util.DateAndCalendarUtils
import com.example.util.SecurityUtils

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MembersScreen(
    usuarioLogado: PessoaEntity,
    pessoas: List<PessoaEntity>,
    firestoreStatus: FirestoreStatusInfo = FirestoreStatusInfo(),
    somenteMeusDados: Boolean = false,
    onCarregarMembrosFirestore: () -> Unit = {},
    onSincronizarFirestore: () -> Unit = {},
    onSalvarPessoa: (
        idExistente: Long,
        nome: String,
        orunko: String,
        cargo: String,
        cpf: String,
        telefone: String,
        endereco: String,
        orixa: String,
        entidades: String,
        dataNascimento: String,
        dataIniciacao: String,
        data1Ano: String,
        data3Anos: String,
        data7Anos: String,
        data14Anos: String,
        data21Anos: String,
        observacoes: String,
        telefonesContatos: String,
        perfilAcesso: String,
        fotoBase64: String
    ) -> Unit,
    onAtualizarFotoMembro: (PessoaEntity, String) -> Unit = { _, _ -> },
    onAlternarAtivo: (PessoaEntity) -> Unit,
    onResetarSenhaTemporaria: (PessoaEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    LaunchedEffect(Unit) {
        onCarregarMembrosFirestore()
    }

    val isAdmin = usuarioLogado.perfilAcesso == "ADMIN"
    var abaModo by rememberSaveable { mutableIntStateOf(0) } // 0 = Listagem de Membros, 1 = Cadastro de Novo Membro
    var busca by rememberSaveable { mutableStateOf("") }
    var filtroStatus by rememberSaveable { mutableStateOf("TODOS") } // TODOS, ATIVOS, INATIVOS
    var pessoaEmEdicao by remember { mutableStateOf<PessoaEntity?>(null) }
    var exibirModalCadastro by rememberSaveable { mutableStateOf(false) }
    var pessoaDetalheSelecionada by remember { mutableStateOf<PessoaEntity?>(null) }
    var pessoaParaFotoRapida by remember { mutableStateOf<PessoaEntity?>(null) }

    // Se for Membro ou abriu atalho "Meus Dados", destaca ficha pessoal oficial com foto
    if (!isAdmin || somenteMeusDados) {
        MemberPersonalDossierView(
            usuario = usuarioLogado,
            colegasAtivos = pessoas.filter { it.ativo },
            isAdmin = isAdmin,
            onAbrirSeletorFoto = {
                pessoaParaFotoRapida = usuarioLogado
            },
            onEditarProprioCadastroSeAdmin = {
                pessoaEmEdicao = usuarioLogado
                exibirModalCadastro = true
            },
            modifier = modifier
        )
        if (exibirModalCadastro && isAdmin) {
            PessoaFormDialog(
                pessoaInicial = pessoaEmEdicao,
                onDismiss = { exibirModalCadastro = false },
                onConfirm = { id, nome, orunko, cargo, cpf, tel, end, orixa, ent, nasc, inic, d1, d3, d7, d14, d21, obs, contatos, perfil, foto ->
                    onSalvarPessoa(id, nome, orunko, cargo, cpf, tel, end, orixa, ent, nasc, inic, d1, d3, d7, d14, d21, obs, contatos, perfil, foto)
                    exibirModalCadastro = false
                }
            )
        }
        pessoaParaFotoRapida?.let { alvo ->
            MemberPhotoPickerDialog(
                memberNameOrOrunko = alvo.orunko.ifBlank { alvo.nome },
                memberOrixaOrCargo = alvo.orixa.ifBlank { alvo.cargo },
                currentFotoBase64 = alvo.fotoBase64,
                onDismiss = { pessoaParaFotoRapida = null },
                onPhotoSelected = { novaFoto ->
                    onAtualizarFotoMembro(alvo, novaFoto)
                    pessoaParaFotoRapida = null
                }
            )
        }
        return
    }

    val filtradas = pessoas.filter { p ->
        val matchStatus = when (filtroStatus) {
            "ATIVOS" -> p.ativo
            "INATIVOS" -> !p.ativo
            else -> true
        }
        val q = busca.trim().lowercase()
        val matchQuery = q.isEmpty() ||
            p.orunko.lowercase().contains(q) ||
            p.nome.lowercase().contains(q) ||
            p.cargo.lowercase().contains(q) ||
            p.orixa.lowercase().contains(q) ||
            p.cpf.contains(q)
        matchStatus && matchQuery
    }

    Column(modifier = modifier.fillMaxSize()) {
        // Abas superiores para alternar rapidamente entre Listagem de Membros e Cadastro Completo
        TabRow(
            selectedTabIndex = abaModo,
            containerColor = NavyCard,
            contentColor = GoldPrimary
        ) {
            Tab(
                selected = abaModo == 0,
                onClick = { abaModo = 0 },
                icon = { Icon(Icons.Default.Groups, contentDescription = null) },
                text = { Text("Lista de Membros (${pessoas.size})", fontWeight = FontWeight.Bold) },
                modifier = Modifier.testTag("tab_member_list")
            )
            Tab(
                selected = abaModo == 1,
                onClick = {
                    pessoaEmEdicao = null
                    abaModo = 1
                },
                icon = { Icon(Icons.Default.PersonAdd, contentDescription = null) },
                text = { Text("Cadastrar Membro", fontWeight = FontWeight.Bold) },
                modifier = Modifier.testTag("tab_member_register")
            )
        }

        if (abaModo == 1) {
            InlineMemberRegistrationForm(
                onCancel = { abaModo = 0 },
                onSaveMember = { nome, orunko, cargo, cpf, tel, end, orixa, ent, nasc, inic, d1, d3, d7, d14, d21, obs, contatos, perfil, foto ->
                    onSalvarPessoa(0L, nome, orunko, cargo, cpf, tel, end, orixa, ent, nasc, inic, d1, d3, d7, d14, d21, obs, contatos, perfil, foto)
                    abaModo = 0
                }
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.img_logo_oficial),
                                contentDescription = "Brasão Oficial Casa do Ogum",
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .border(1.5.dp, GoldPrimary, CircleShape),
                                contentScale = ContentScale.Crop
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Gerenciamento de Membros",
                                    style = MaterialTheme.typography.headlineSmall,
                                    color = GoldPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${pessoas.count { it.ativo }} ativos • ${pessoas.count { !it.ativo }} inativos • Total: ${pessoas.size}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = SilverSubtext
                                )
                            }
                        }
                        Button(
                            onClick = {
                                pessoaEmEdicao = null
                                exibirModalCadastro = true
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = GoldPrimary,
                                contentColor = NavyDeep
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.testTag("add_member_button")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Cadastrar Pessoa", modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Novo", fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // Banner de Integração com Firebase Firestore (Coleção: membros)
                item {
                    Surface(
                        color = NavyElevated,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, GoldPrimary.copy(alpha = 0.45f), RoundedCornerShape(12.dp))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = if (firestoreStatus.state == FirestoreConnectionState.CONNECTED) {
                                        Icons.Default.CloudDone
                                    } else {
                                        Icons.Default.CloudSync
                                    },
                                    contentDescription = "Status Firestore",
                                    tint = GoldPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "Firebase Firestore • Coleção 'membros'",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = GoldLight,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = firestoreStatus.message,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = SilverSubtext
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            OutlinedButton(
                                onClick = onSincronizarFirestore
                            ) {
                                Text("Sincronizar", color = GoldPrimary, style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                }

                item {
                    OutlinedTextField(
                        value = busca,
                        onValueChange = { busca = it },
                        label = { Text("Buscar por Nome, Orunkó, Cargo, Orixá ou CPF") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = GoldPrimary) },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("search_member_input")
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("TODOS" to "Todos (${pessoas.size})", "ATIVOS" to "Ativos (${pessoas.count { it.ativo }})", "INATIVOS" to "Inativos (${pessoas.count { !it.ativo }})").forEach { (key, label) ->
                            FilterChip(
                                selected = filtroStatus == key,
                                onClick = { filtroStatus = key },
                                label = { Text(label) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = GoldPrimary,
                                    selectedLabelColor = NavyDeep
                                )
                            )
                        }
                    }
                }

                items(
                    items = filtradas,
                    key = { "${it.id}_${it.cpf}_${it.fotoBase64.hashCode()}" }
                ) { pessoa ->
                    AdminMemberCard(
                        pessoa = pessoa,
                        onViewFullDetails = { pessoaDetalheSelecionada = pessoa },
                        onQuickPhotoChange = { pessoaParaFotoRapida = pessoa },
                        onEdit = {
                            pessoaEmEdicao = pessoa
                            exibirModalCadastro = true
                        },
                        onToggleActive = { onAlternarAtivo(pessoa) },
                        onResetTempPassword = { onResetarSenhaTemporaria(pessoa) }
                    )
                }

                item { Spacer(modifier = Modifier.height(24.dp)) }
            }
        }
    }

    if (exibirModalCadastro) {
        PessoaFormDialog(
            pessoaInicial = pessoaEmEdicao,
            onDismiss = { exibirModalCadastro = false },
            onConfirm = { id, nome, orunko, cargo, cpf, tel, end, orixa, ent, nasc, inic, d1, d3, d7, d14, d21, obs, contatos, perfil, foto ->
                onSalvarPessoa(id, nome, orunko, cargo, cpf, tel, end, orixa, ent, nasc, inic, d1, d3, d7, d14, d21, obs, contatos, perfil, foto)
                exibirModalCadastro = false
            }
        )
    }

    pessoaParaFotoRapida?.let { alvo ->
        MemberPhotoPickerDialog(
            memberNameOrOrunko = alvo.orunko.ifBlank { alvo.nome },
            memberOrixaOrCargo = alvo.orixa.ifBlank { alvo.cargo },
            currentFotoBase64 = alvo.fotoBase64,
            onDismiss = { pessoaParaFotoRapida = null },
            onPhotoSelected = { novaFoto ->
                onAtualizarFotoMembro(alvo, novaFoto)
                pessoaParaFotoRapida = null
            }
        )
    }

    pessoaDetalheSelecionada?.let { p ->
        val pAtualizada = pessoas.firstOrNull { it.id == p.id } ?: p
        AlertDialog(
            onDismissRequest = { pessoaDetalheSelecionada = null },
            containerColor = NavyCard,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    MemberAvatar(
                        fotoBase64 = pAtualizada.fotoBase64,
                        nameOrOrunko = pAtualizada.orunko.ifBlank { pAtualizada.nome },
                        cargoOrOrixa = pAtualizada.cargo.ifBlank { pAtualizada.orixa },
                        size = 62.dp,
                        showEditBadge = true,
                        onClick = { pessoaParaFotoRapida = pAtualizada }
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = pAtualizada.nome,
                            style = MaterialTheme.typography.titleMedium,
                            color = GoldPrimary,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Orunkó: ${pAtualizada.orunko} • Cargo: ${pAtualizada.cargo}",
                            style = MaterialTheme.typography.bodySmall,
                            color = GoldLight
                        )
                    }
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    OutlinedButton(
                        onClick = { pessoaParaFotoRapida = pAtualizada },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.AddAPhoto, contentDescription = null, tint = GoldPrimary, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (pAtualizada.fotoBase64.isBlank()) "Cadastrar Foto de Perfil" else "Trocar Foto de Perfil",
                            color = GoldPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    DossierFieldRow("Nome Civil", pAtualizada.nome)
                    DossierFieldRow("Orunkó (Público)", pAtualizada.orunko)
                    DossierFieldRow("Cargo na Casa", pAtualizada.cargo)
                    DossierFieldRow("CPF (Login)", SecurityUtils.formatCpf(pAtualizada.cpf))
                    DossierFieldRow("Telefone", pAtualizada.telefone)
                    DossierFieldRow("Endereço", pAtualizada.endereco)
                    DossierFieldRow("Orixá", pAtualizada.orixa)
                    DossierFieldRow("Entidades", pAtualizada.entidades)
                    HorizontalDivider(color = GoldMuted.copy(alpha = 0.3f))
                    DossierFieldRow("Data de Nascimento", pAtualizada.dataNascimento)
                    DossierFieldRow("Data de Iniciação", pAtualizada.dataIniciacao)
                    DossierFieldRow("Obrigação 1 Ano", pAtualizada.data1Ano)
                    DossierFieldRow("Obrigação 3 Anos", pAtualizada.data3Anos)
                    DossierFieldRow("Obrigação 7 Anos", pAtualizada.data7Anos)
                    DossierFieldRow("Obrigação 14 Anos", pAtualizada.data14Anos)
                    DossierFieldRow("Obrigação 21 Anos", pAtualizada.data21Anos)
                    HorizontalDivider(color = GoldMuted.copy(alpha = 0.3f))
                    DossierFieldRow("Telefones de Contatos", pAtualizada.telefonesContatos)
                    DossierFieldRow("Observações", pAtualizada.observacoes)
                    HorizontalDivider(color = GoldMuted.copy(alpha = 0.3f))
                    DossierFieldRow("Data de Cadastro", pAtualizada.dataCadastro)
                    DossierFieldRow("Status", if (pAtualizada.ativo) "ATIVO" else "INATIVO")
                    DossierFieldRow("Perfil de Acesso", pAtualizada.perfilAcesso)
                    DossierFieldRow("Primeiro Acesso Pendente?", if (pAtualizada.primeiroAcesso) "SIM (Senha Temp: ${pAtualizada.senhaTemporariaDica})" else "NÃO (Senha pessoal ativa)")
                    DossierFieldRow("Último Acesso", pAtualizada.ultimoAcesso)
                }
            },
            confirmButton = {
                TextButton(onClick = { pessoaDetalheSelecionada = null }) {
                    Text("Fechar", color = GoldPrimary)
                }
            }
        )
    }
}

@Composable
private fun InlineMemberRegistrationForm(
    onCancel: () -> Unit,
    onSaveMember: (
        nome: String,
        orunko: String,
        cargo: String,
        cpf: String,
        telefone: String,
        endereco: String,
        orixa: String,
        entidades: String,
        dataNascimento: String,
        dataIniciacao: String,
        data1Ano: String,
        data3Anos: String,
        data7Anos: String,
        data14Anos: String,
        data21Anos: String,
        observacoes: String,
        telefonesContatos: String,
        perfilAcesso: String,
        fotoBase64: String
    ) -> Unit
) {
    val context = LocalContext.current
    var nome by rememberSaveable { mutableStateOf("") }
    var orunko by rememberSaveable { mutableStateOf("") }
    var cargo by rememberSaveable { mutableStateOf("Yaô") }
    var cpf by rememberSaveable { mutableStateOf("") }
    var telefone by rememberSaveable { mutableStateOf("") }
    var endereco by rememberSaveable { mutableStateOf("") }
    var orixa by rememberSaveable { mutableStateOf("Ogum") }
    var entidades by rememberSaveable { mutableStateOf("") }
    var dataNascimento by rememberSaveable { mutableStateOf("10/10/1995") }
    var dataIniciacao by rememberSaveable { mutableStateOf("10/10/2025") }
    var data1Ano by rememberSaveable { mutableStateOf("") }
    var data3Anos by rememberSaveable { mutableStateOf("") }
    var data7Anos by rememberSaveable { mutableStateOf("") }
    var data14Anos by rememberSaveable { mutableStateOf("") }
    var data21Anos by rememberSaveable { mutableStateOf("") }
    var telefonesContatos by rememberSaveable { mutableStateOf("") }
    var observacoes by rememberSaveable { mutableStateOf("") }
    var perfilAcesso by rememberSaveable { mutableStateOf("MEMBRO") }
    var fotoBase64 by rememberSaveable { mutableStateOf("") }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            AvatarImageUtils.uriToBase64Avatar(context, uri)?.let { encoded ->
                fotoBase64 = encoded
            }
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        if (bitmap != null) {
            AvatarImageUtils.bitmapToBase64Avatar(bitmap)?.let { encoded ->
                fotoBase64 = encoded
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.5.dp, GoldPrimary.copy(alpha = 0.7f), RoundedCornerShape(18.dp)),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = NavyCard)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "FOTO DE PERFIL DO MEMBRO",
                    style = MaterialTheme.typography.titleMedium,
                    color = GoldPrimary,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                MemberAvatar(
                    fotoBase64 = fotoBase64,
                    nameOrOrunko = orunko.ifBlank { nome.ifBlank { "Novo Membro" } },
                    cargoOrOrixa = cargo.ifBlank { orixa },
                    size = 96.dp,
                    showEditBadge = true,
                    onClick = {
                        photoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    }
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "A foto selecionada será exibida na lista de membros, na Tela Principal e nos calendários.",
                    style = MaterialTheme.typography.bodySmall,
                    color = SilverSubtext
                )
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = NavyDeep),
                        modifier = Modifier.weight(1f).testTag("inline_pick_photo_button")
                    ) {
                        Icon(Icons.Default.PhotoLibrary, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Escolher da Galeria", fontWeight = FontWeight.Bold)
                    }
                    OutlinedButton(
                        onClick = { cameraLauncher.launch(null) },
                        modifier = Modifier.weight(1f).testTag("inline_camera_photo_button")
                    ) {
                        Icon(Icons.Default.CameraAlt, contentDescription = null, tint = GoldPrimary, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Tirar Foto", color = GoldLight)
                    }
                }
            }
        }

        OutlinedTextField(
            value = nome,
            onValueChange = { nome = it },
            label = { Text("NOME CIVIL COMPLETO *") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = orunko,
            onValueChange = { orunko = it },
            label = { Text("ORUNKÓ (Exibido nos Calendários) *") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = cargo,
                onValueChange = { cargo = it },
                label = { Text("CARGO") },
                singleLine = true,
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = orixa,
                onValueChange = { orixa = it },
                label = { Text("ORIXÁ") },
                singleLine = true,
                modifier = Modifier.weight(1f)
            )
        }
        OutlinedTextField(
            value = cpf,
            onValueChange = { cpf = it },
            label = { Text("CPF (11 dígitos - chave de login) *") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = telefone,
            onValueChange = { telefone = it },
            label = { Text("TELEFONE") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = endereco,
            onValueChange = { endereco = it },
            label = { Text("ENDEREÇO COMPLETO") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = entidades,
            onValueChange = { entidades = it },
            label = { Text("ENTIDADES") },
            modifier = Modifier.fillMaxWidth()
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = dataNascimento,
                onValueChange = { dataNascimento = it },
                label = { Text("DATA NASC. (DD/MM/AAAA)") },
                singleLine = true,
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = dataIniciacao,
                onValueChange = { dataIniciacao = it },
                label = { Text("INICIAÇÃO (DD/MM/AAAA)") },
                singleLine = true,
                modifier = Modifier.weight(1f)
            )
        }

        OutlinedButton(
            onClick = {
                data1Ano = DateAndCalendarUtils.computeObligationDate(dataIniciacao, 1)
                data3Anos = DateAndCalendarUtils.computeObligationDate(dataIniciacao, 3)
                data7Anos = DateAndCalendarUtils.computeObligationDate(dataIniciacao, 7)
                data14Anos = DateAndCalendarUtils.computeObligationDate(dataIniciacao, 14)
                data21Anos = DateAndCalendarUtils.computeObligationDate(dataIniciacao, 21)
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.AutoFixHigh, contentDescription = null, tint = GoldPrimary, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Calcular Datas de 1, 3, 7, 14 e 21 Anos Automaticamente", color = GoldLight, style = MaterialTheme.typography.labelSmall)
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = data1Ano,
                onValueChange = { data1Ano = it },
                label = { Text("DATA 1 ANO") },
                singleLine = true,
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = data3Anos,
                onValueChange = { data3Anos = it },
                label = { Text("DATA 3 ANOS") },
                singleLine = true,
                modifier = Modifier.weight(1f)
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = data7Anos,
                onValueChange = { data7Anos = it },
                label = { Text("DATA 7 ANOS") },
                singleLine = true,
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = data14Anos,
                onValueChange = { data14Anos = it },
                label = { Text("DATA 14 ANOS") },
                singleLine = true,
                modifier = Modifier.weight(1f)
            )
        }
        OutlinedTextField(
            value = data21Anos,
            onValueChange = { data21Anos = it },
            label = { Text("DATA 21 ANOS") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = telefonesContatos,
            onValueChange = { telefonesContatos = it },
            label = { Text("TELEFONES DE CONTATOS / EMERGÊNCIA") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = observacoes,
            onValueChange = { observacoes = it },
            label = { Text("OBSERVAÇÕES") },
            modifier = Modifier.fillMaxWidth()
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedButton(
                onClick = onCancel,
                modifier = Modifier.weight(1f)
            ) {
                Text("Voltar à Lista", color = SilverSubtext)
            }
            Button(
                onClick = {
                    onSaveMember(
                        nome,
                        orunko,
                        cargo,
                        cpf,
                        telefone,
                        endereco,
                        orixa,
                        entidades,
                        dataNascimento,
                        dataIniciacao,
                        data1Ano,
                        data3Anos,
                        data7Anos,
                        data14Anos,
                        data21Anos,
                        observacoes,
                        telefonesContatos,
                        perfilAcesso,
                        fotoBase64
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = NavyDeep),
                modifier = Modifier.weight(1f).testTag("inline_save_member_button")
            ) {
                Text("Salvar Membro", fontWeight = FontWeight.Bold)
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun AdminMemberCard(
    pessoa: PessoaEntity,
    onViewFullDetails: () -> Unit,
    onQuickPhotoChange: () -> Unit,
    onEdit: () -> Unit,
    onToggleActive: () -> Unit,
    onResetTempPassword: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                1.dp,
                if (pessoa.ativo) GoldMuted.copy(alpha = 0.45f) else StatusOverdueRed.copy(alpha = 0.5f),
                RoundedCornerShape(16.dp)
            )
            .clickable { onViewFullDetails() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = NavyCard)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Foto do Perfil do Membro renderizada para cada item da lista
                Box {
                    MemberAvatar(
                        fotoBase64 = pessoa.fotoBase64,
                        nameOrOrunko = pessoa.orunko.ifBlank { pessoa.nome },
                        cargoOrOrixa = pessoa.cargo.ifBlank { pessoa.orixa },
                        size = 66.dp,
                        showEditBadge = true,
                        onClick = onQuickPhotoChange
                    )
                    // Indicador visual de status Ativo/Inativo no canto superior esquerdo do avatar
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .size(14.dp)
                            .clip(CircleShape)
                            .background(if (pessoa.ativo) StatusPaidGreen else StatusOverdueRed)
                            .border(1.5.dp, NavyDeep, CircleShape)
                    )
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = pessoa.nome,
                            style = MaterialTheme.typography.titleMedium,
                            color = GoldPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = "Cargo: ${pessoa.cargo}  •  Orunkó: ${pessoa.orunko}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = GoldLight,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Orixá: ${pessoa.orixa} • CPF: ${SecurityUtils.formatCpf(pessoa.cpf)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = SilverSubtext
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            color = if (pessoa.ativo) StatusPaidGreen.copy(alpha = 0.18f) else StatusOverdueRed.copy(alpha = 0.18f),
                            shape = RoundedCornerShape(50)
                        ) {
                            Text(
                                text = if (pessoa.ativo) "ATIVO" else "INATIVO",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (pessoa.ativo) StatusPaidGreen else StatusOverdueRed,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                        if (pessoa.perfilAcesso == "ADMIN") {
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                color = GoldPrimary.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(50)
                            ) {
                                Text(
                                    text = "👑 ADMIN",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = GoldLight,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Nasc: ${pessoa.dataNascimento}  |  Iniciação: ${pessoa.dataIniciacao}  |  7 Anos: ${pessoa.data7Anos}  |  21 Anos: ${pessoa.data21Anos}",
                style = MaterialTheme.typography.bodySmall,
                color = IvoryText
            )

            if (pessoa.primeiroAcesso && pessoa.senhaTemporariaDica.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Surface(
                    color = StatusPendingYellow.copy(alpha = 0.16f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "🔑 1º Acesso pendente — Senha Temporária (4 números): ${pessoa.senhaTemporariaDica}",
                        style = MaterialTheme.typography.labelMedium,
                        color = StatusPendingYellow,
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 10.dp),
                color = GoldMuted.copy(alpha = 0.25f)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                OutlinedButton(
                    onClick = onQuickPhotoChange,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.AddAPhoto, contentDescription = "Foto", tint = GoldPrimary, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Foto", color = GoldPrimary, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                }
                OutlinedButton(
                    onClick = onEdit,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Edit, contentDescription = "Editar", tint = GoldPrimary, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Editar", color = GoldLight, style = MaterialTheme.typography.labelSmall)
                }
                OutlinedButton(
                    onClick = onResetTempPassword,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Key, contentDescription = "Senha Temp", tint = GoldPrimary, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Senha", color = GoldLight, style = MaterialTheme.typography.labelSmall)
                }
                OutlinedButton(
                    onClick = onToggleActive,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = if (pessoa.ativo) Icons.Default.Block else Icons.Default.CheckCircle,
                        contentDescription = "Alternar status",
                        tint = if (pessoa.ativo) StatusOverdueRed else StatusPaidGreen,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (pessoa.ativo) "Inativar" else "Ativar",
                        color = if (pessoa.ativo) StatusOverdueRed else StatusPaidGreen,
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        }
    }
}

@Composable
private fun MemberPersonalDossierView(
    usuario: PessoaEntity,
    colegasAtivos: List<PessoaEntity>,
    isAdmin: Boolean,
    onAbrirSeletorFoto: () -> Unit,
    onEditarProprioCadastroSeAdmin: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(4.dp))
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, GoldPrimary.copy(alpha = 0.6f), RoundedCornerShape(18.dp)),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = NavyCard)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            MemberAvatar(
                                fotoBase64 = usuario.fotoBase64,
                                nameOrOrunko = usuario.orunko,
                                cargoOrOrixa = usuario.cargo.ifBlank { usuario.orixa },
                                size = 72.dp,
                                showEditBadge = true,
                                onClick = onAbrirSeletorFoto
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Meus Dados Oficiais",
                                    style = MaterialTheme.typography.headlineSmall,
                                    color = GoldPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${usuario.orunko} • ${usuario.cargo}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = GoldLight
                                )
                                TextButton(
                                    onClick = onAbrirSeletorFoto
                                ) {
                                    Icon(Icons.Default.AddAPhoto, contentDescription = null, tint = GoldPrimary, modifier = Modifier.size(15.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (usuario.fotoBase64.isBlank()) "Adicionar Foto de Perfil" else "Alterar Foto de Perfil",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = GoldPrimary,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                        if (isAdmin) {
                            OutlinedButton(onClick = onEditarProprioCadastroSeAdmin) {
                                Icon(Icons.Default.Edit, contentDescription = null, tint = GoldPrimary, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Editar", color = GoldPrimary)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Surface(
                        color = NavyElevated,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = GoldPrimary, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Regra de Segurança: O membro consulta seus dados oficiais, mas alterações cadastrais são exclusivas da Administração da Casa.",
                                style = MaterialTheme.typography.bodySmall,
                                color = SilverSubtext
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    DossierFieldRow("Nome Civil", usuario.nome)
                    DossierFieldRow("Orunkó", usuario.orunko)
                    DossierFieldRow("Cargo na Casa", usuario.cargo)
                    DossierFieldRow("CPF de Acesso", SecurityUtils.formatCpf(usuario.cpf))
                    DossierFieldRow("Telefone", usuario.telefone)
                    DossierFieldRow("Endereço", usuario.endereco)
                    DossierFieldRow("Orixá", usuario.orixa)
                    DossierFieldRow("Entidades", usuario.entidades)
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = GoldMuted.copy(alpha = 0.3f))
                    Text(
                        text = "DATAS LITÚRGICAS E OBRIGAÇÕES (ODÚN KODÚN)",
                        style = MaterialTheme.typography.labelMedium,
                        color = GoldPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    DossierFieldRow("Data de Nascimento", usuario.dataNascimento)
                    DossierFieldRow("Data de Iniciação", usuario.dataIniciacao)
                    DossierFieldRow("Data 1 Ano", usuario.data1Ano)
                    DossierFieldRow("Data 3 Anos", usuario.data3Anos)
                    DossierFieldRow("Data 7 Anos", usuario.data7Anos)
                    DossierFieldRow("Data 14 Anos", usuario.data14Anos)
                    DossierFieldRow("Data 21 Anos", usuario.data21Anos)
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = GoldMuted.copy(alpha = 0.3f))
                    DossierFieldRow("Telefones de Contatos", usuario.telefonesContatos)
                    DossierFieldRow("Observações", usuario.observacoes)
                    DossierFieldRow("Último Acesso", usuario.ultimoAcesso)
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Irmãos da Casa (Identificação Exclusiva por Orunkó)",
                style = MaterialTheme.typography.titleMedium,
                color = GoldLight,
                fontWeight = FontWeight.Bold
            )
            OrunkoPrivacyBanner()
        }

        items(
            items = colegasAtivos,
            key = { "${it.id}_${it.fotoBase64.hashCode()}" }
        ) { irmao ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, GoldMuted.copy(alpha = 0.3f), RoundedCornerShape(12.dp)),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = NavyCard)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        MemberAvatar(
                            fotoBase64 = irmao.fotoBase64,
                            nameOrOrunko = irmao.orunko,
                            cargoOrOrixa = irmao.cargo.ifBlank { irmao.orixa },
                            size = 48.dp
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = irmao.orunko, // Nunca exibe nome civil de terceiros!
                                style = MaterialTheme.typography.titleMedium,
                                color = GoldPrimary,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${irmao.cargo} • Orixá: ${irmao.orixa}",
                                style = MaterialTheme.typography.bodySmall,
                                color = SilverSubtext
                            )
                        }
                    }
                    Text(
                        text = "Inic.: ${DateAndCalendarUtils.formatDayMonth(irmao.dataIniciacao)}",
                        style = MaterialTheme.typography.labelMedium,
                        color = GoldLight
                    )
                }
            }
        }

        item { Spacer(modifier = Modifier.height(24.dp)) }
    }
}

@Composable
private fun DossierFieldRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "$label:",
            style = MaterialTheme.typography.bodySmall,
            color = SilverSubtext,
            modifier = Modifier.weight(0.42f)
        )
        Text(
            text = value.ifBlank { "—" },
            style = MaterialTheme.typography.bodySmall,
            color = IvoryText,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.weight(0.58f)
        )
    }
}

@Composable
private fun PessoaFormDialog(
    pessoaInicial: PessoaEntity?,
    onDismiss: () -> Unit,
    onConfirm: (
        idExistente: Long,
        nome: String,
        orunko: String,
        cargo: String,
        cpf: String,
        telefone: String,
        endereco: String,
        orixa: String,
        entidades: String,
        dataNascimento: String,
        dataIniciacao: String,
        data1Ano: String,
        data3Anos: String,
        data7Anos: String,
        data14Anos: String,
        data21Anos: String,
        observacoes: String,
        telefonesContatos: String,
        perfilAcesso: String,
        fotoBase64: String
    ) -> Unit
) {
    val context = LocalContext.current
    var nome by rememberSaveable { mutableStateOf(pessoaInicial?.nome ?: "") }
    var orunko by rememberSaveable { mutableStateOf(pessoaInicial?.orunko ?: "") }
    var cargo by rememberSaveable { mutableStateOf(pessoaInicial?.cargo ?: "Yaô") }
    var cpf by rememberSaveable { mutableStateOf(pessoaInicial?.cpf ?: "") }
    var telefone by rememberSaveable { mutableStateOf(pessoaInicial?.telefone ?: "") }
    var endereco by rememberSaveable { mutableStateOf(pessoaInicial?.endereco ?: "") }
    var orixa by rememberSaveable { mutableStateOf(pessoaInicial?.orixa ?: "Ogum") }
    var entidades by rememberSaveable { mutableStateOf(pessoaInicial?.entidades ?: "") }
    var dataNascimento by rememberSaveable { mutableStateOf(pessoaInicial?.dataNascimento ?: "10/10/1995") }
    var dataIniciacao by rememberSaveable { mutableStateOf(pessoaInicial?.dataIniciacao ?: "10/10/2025") }
    var data1Ano by rememberSaveable { mutableStateOf(pessoaInicial?.data1Ano ?: "") }
    var data3Anos by rememberSaveable { mutableStateOf(pessoaInicial?.data3Anos ?: "") }
    var data7Anos by rememberSaveable { mutableStateOf(pessoaInicial?.data7Anos ?: "") }
    var data14Anos by rememberSaveable { mutableStateOf(pessoaInicial?.data14Anos ?: "") }
    var data21Anos by rememberSaveable { mutableStateOf(pessoaInicial?.data21Anos ?: "") }
    var telefonesContatos by rememberSaveable { mutableStateOf(pessoaInicial?.telefonesContatos ?: "") }
    var observacoes by rememberSaveable { mutableStateOf(pessoaInicial?.observacoes ?: "") }
    var perfilAcesso by rememberSaveable { mutableStateOf(pessoaInicial?.perfilAcesso ?: "MEMBRO") }
    var fotoBase64 by rememberSaveable { mutableStateOf(pessoaInicial?.fotoBase64 ?: "") }

    // Android Photo Picker oficial (Galeria — zero permissões invasivas)
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            val encoded = AvatarImageUtils.uriToBase64Avatar(context, uri)
            if (encoded != null) {
                fotoBase64 = encoded
            }
        }
    }

    // Captura direta pela Câmera
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        if (bitmap != null) {
            val encoded = AvatarImageUtils.bitmapToBase64Avatar(bitmap)
            if (encoded != null) {
                fotoBase64 = encoded
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = NavyCard,
        title = {
            Text(
                text = if (pessoaInicial == null) "Novo Cadastro de Membro" else "Editar Cadastro — ${pessoaInicial.orunko}",
                style = MaterialTheme.typography.titleLarge,
                color = GoldPrimary,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // SEÇÃO EM DESTAQUE: FOTO DE PERFIL DO MEMBRO
                Surface(
                    color = NavyElevated,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.5.dp, GoldPrimary.copy(alpha = 0.7f), RoundedCornerShape(16.dp))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        MemberAvatar(
                            fotoBase64 = fotoBase64,
                            nameOrOrunko = orunko.ifBlank { nome.ifBlank { "M" } },
                            cargoOrOrixa = cargo.ifBlank { orixa },
                            size = 82.dp,
                            showEditBadge = true,
                            onClick = {
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "FOTO DE PERFIL DO MEMBRO",
                            style = MaterialTheme.typography.titleSmall,
                            color = GoldPrimary,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Exibida na Tela Principal, lista de membros, mensalidades, tarefas e calendários ao lado do nome/Orunkó.",
                            style = MaterialTheme.typography.bodySmall,
                            color = SilverSubtext
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    photoPickerLauncher.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = NavyDeep),
                                modifier = Modifier.weight(1f).testTag("pick_member_photo_button")
                            ) {
                                Icon(Icons.Default.PhotoLibrary, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (fotoBase64.isBlank()) "Galeria" else "Trocar",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            OutlinedButton(
                                onClick = { cameraLauncher.launch(null) },
                                modifier = Modifier.weight(1f).testTag("camera_member_photo_button")
                            ) {
                                Icon(Icons.Default.CameraAlt, contentDescription = null, tint = GoldPrimary, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Câmera", color = GoldLight, style = MaterialTheme.typography.labelSmall)
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    fotoBase64 = AvatarImageUtils.generateInstitutionalAvatarBase64(
                                        nameOrOrunko = orunko.ifBlank { nome.ifBlank { "Casa Ogum" } },
                                        cargoOrOrixa = orixa.ifBlank { cargo }
                                    )
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = GoldPrimary, modifier = Modifier.size(15.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Gerar Selo com Iniciais", color = GoldLight, style = MaterialTheme.typography.labelSmall)
                            }
                            if (fotoBase64.isNotBlank()) {
                                OutlinedButton(
                                    onClick = { fotoBase64 = "" }
                                ) {
                                    Icon(Icons.Default.DeleteOutline, contentDescription = "Remover foto", tint = StatusOverdueRed, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                }

                Text(
                    text = "Ao cadastrar uma nova pessoa, o sistema salva no Firestore e gera automaticamente o acesso por CPF e senha temporária de 4 números.",
                    style = MaterialTheme.typography.bodySmall,
                    color = SilverSubtext
                )
                OutlinedTextField(
                    value = nome,
                    onValueChange = { nome = it },
                    label = { Text("NOME CIVIL COMPLETO *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("input_nome_civil")
                )
                OutlinedTextField(
                    value = orunko,
                    onValueChange = { orunko = it },
                    label = { Text("ORUNKÓ (Exibido nos Calendários) *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("input_orunko")
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = cargo,
                        onValueChange = { cargo = it },
                        label = { Text("CARGO") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = orixa,
                        onValueChange = { orixa = it },
                        label = { Text("ORIXÁ") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }
                OutlinedTextField(
                    value = cpf,
                    onValueChange = { cpf = it },
                    label = { Text("CPF (11 dígitos - chave de login) *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("input_cpf_cadastro")
                )
                OutlinedTextField(
                    value = telefone,
                    onValueChange = { telefone = it },
                    label = { Text("TELEFONE") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = endereco,
                    onValueChange = { endereco = it },
                    label = { Text("ENDEREÇO COMPLETO") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = entidades,
                    onValueChange = { entidades = it },
                    label = { Text("ENTIDADES") },
                    modifier = Modifier.fillMaxWidth()
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = dataNascimento,
                        onValueChange = { dataNascimento = it },
                        label = { Text("DATA NASC. (DD/MM/AAAA)") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = dataIniciacao,
                        onValueChange = { dataIniciacao = it },
                        label = { Text("INICIAÇÃO (DD/MM/AAAA)") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                OutlinedButton(
                    onClick = {
                        data1Ano = DateAndCalendarUtils.computeObligationDate(dataIniciacao, 1)
                        data3Anos = DateAndCalendarUtils.computeObligationDate(dataIniciacao, 3)
                        data7Anos = DateAndCalendarUtils.computeObligationDate(dataIniciacao, 7)
                        data14Anos = DateAndCalendarUtils.computeObligationDate(dataIniciacao, 14)
                        data21Anos = DateAndCalendarUtils.computeObligationDate(dataIniciacao, 21)
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.AutoFixHigh, contentDescription = null, tint = GoldPrimary, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Calcular Datas de 1, 3, 7, 14 e 21 Anos Automaticamente", color = GoldLight, style = MaterialTheme.typography.labelSmall)
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = data1Ano,
                        onValueChange = { data1Ano = it },
                        label = { Text("DATA 1 ANO") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = data3Anos,
                        onValueChange = { data3Anos = it },
                        label = { Text("DATA 3 ANOS") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = data7Anos,
                        onValueChange = { data7Anos = it },
                        label = { Text("DATA 7 ANOS") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = data14Anos,
                        onValueChange = { data14Anos = it },
                        label = { Text("DATA 14 ANOS") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }
                OutlinedTextField(
                    value = data21Anos,
                    onValueChange = { data21Anos = it },
                    label = { Text("DATA 21 ANOS") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = telefonesContatos,
                    onValueChange = { telefonesContatos = it },
                    label = { Text("TELEFONES DE CONTATOS / EMERGÊNCIA") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = observacoes,
                    onValueChange = { observacoes = it },
                    label = { Text("OBSERVAÇÕES") },
                    modifier = Modifier.fillMaxWidth()
                )

                Text(
                    text = "Perfil de Acesso:",
                    style = MaterialTheme.typography.labelMedium,
                    color = GoldLight
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("MEMBRO" to "👤 Membro", "ADMIN" to "👑 Administrador").forEach { (key, label) ->
                        FilterChip(
                            selected = perfilAcesso == key,
                            onClick = { perfilAcesso = key },
                            label = { Text(label) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(
                        pessoaInicial?.id ?: 0L,
                        nome,
                        orunko,
                        cargo,
                        cpf,
                        telefone,
                        endereco,
                        orixa,
                        entidades,
                        dataNascimento,
                        dataIniciacao,
                        data1Ano,
                        data3Anos,
                        data7Anos,
                        data14Anos,
                        data21Anos,
                        observacoes,
                        telefonesContatos,
                        perfilAcesso,
                        fotoBase64
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = NavyDeep),
                modifier = Modifier.testTag("save_member_dialog_button")
            ) {
                Text("Salvar no Firestore", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = SilverSubtext)
            }
        }
    )
}
