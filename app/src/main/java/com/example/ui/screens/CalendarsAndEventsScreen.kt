package com.example.ui.screens

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.AniversarianteItem
import com.example.data.EventoEntity
import com.example.data.OdunKodunItem
import com.example.data.PessoaEntity
import com.example.ui.components.MemberAvatar
import com.example.ui.theme.GoldLight
import com.example.ui.theme.GoldMuted
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.IvoryText
import com.example.ui.theme.NavyCard
import com.example.ui.theme.NavyDeep
import com.example.ui.theme.NavyElevated
import com.example.ui.theme.SilverSubtext
import com.example.ui.theme.StatusOverdueRed
import com.example.util.DateAndCalendarUtils

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CalendarsAndEventsScreen(
    usuarioLogado: PessoaEntity,
    pessoas: List<PessoaEntity> = emptyList(),
    eventos: List<EventoEntity>,
    aniversariantes: List<AniversarianteItem>,
    odunKodunList: List<OdunKodunItem>,
    mesSelecionado: Int,
    onSelectMes: (Int) -> Unit,
    onSalvarEvento: (
        idExistente: Long,
        nome: String,
        data: String,
        horario: String,
        local: String,
        descricao: String,
        valorContribuicao: Double,
        responsaveis: String,
        participantes: String,
        observacoes: String
    ) -> Unit,
    onExcluirEvento: (EventoEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val isAdmin = usuarioLogado.perfilAcesso == "ADMIN"
    var abaSelecionada by rememberSaveable { mutableIntStateOf(0) } // 0 = Eventos, 1 = Aniversariantes, 2 = Odún Kodún
    var modoOrdenacaoOdun by rememberSaveable { mutableStateOf("MES") } // "MES", "ANUAL", "PROXIMO"
    var eventoEmEdicao by remember { mutableStateOf<EventoEntity?>(null) }
    var exibirModalEvento by rememberSaveable { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxSize()) {
        TabRow(
            selectedTabIndex = abaSelecionada,
            containerColor = NavyCard,
            contentColor = GoldPrimary
        ) {
            Tab(
                selected = abaSelecionada == 0,
                onClick = { abaSelecionada = 0 },
                text = { Text("📅 Eventos", fontWeight = FontWeight.Bold) },
                modifier = Modifier.testTag("tab_eventos")
            )
            Tab(
                selected = abaSelecionada == 1,
                onClick = { abaSelecionada = 1 },
                text = { Text("🎂 Aniversários", fontWeight = FontWeight.Bold) },
                modifier = Modifier.testTag("tab_aniversariantes")
            )
            Tab(
                selected = abaSelecionada == 2,
                onClick = { abaSelecionada = 2 },
                text = { Text("🕯️ Odún Kodún", fontWeight = FontWeight.Bold) },
                modifier = Modifier.testTag("tab_odun_kodun")
            )
        }

        when (abaSelecionada) {
            0 -> {
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
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Eventos e Festividades",
                                    style = MaterialTheme.typography.headlineSmall,
                                    color = GoldPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Calendário oficial da Casa do Ogum - RJ",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = SilverSubtext
                                )
                            }
                            if (isAdmin) {
                                Button(
                                    onClick = {
                                        eventoEmEdicao = null
                                        exibirModalEvento = true
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = NavyDeep),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.testTag("add_event_button")
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Novo Evento", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    items(eventos, key = { it.id }) { ev ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, GoldPrimary.copy(alpha = 0.55f), RoundedCornerShape(16.dp)),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = NavyCard)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(Icons.Default.Event, contentDescription = null, tint = GoldPrimary)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Text(
                                                text = ev.nome,
                                                style = MaterialTheme.typography.titleMedium,
                                                color = GoldPrimary,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = "${ev.data} às ${ev.horario}",
                                                style = MaterialTheme.typography.labelLarge,
                                                color = GoldLight
                                            )
                                        }
                                    }
                                    if (isAdmin) {
                                        Row {
                                            IconButton(onClick = {
                                                eventoEmEdicao = ev
                                                exibirModalEvento = true
                                            }) {
                                                Icon(Icons.Default.Edit, contentDescription = "Editar Evento", tint = GoldLight)
                                            }
                                            IconButton(onClick = { onExcluirEvento(ev) }) {
                                                Icon(Icons.Default.Delete, contentDescription = "Remover Evento", tint = StatusOverdueRed)
                                            }
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.LocationOn, contentDescription = null, tint = GoldMuted, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(text = ev.local, style = MaterialTheme.typography.bodySmall, color = IvoryText)
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(text = ev.descricao, style = MaterialTheme.typography.bodyMedium, color = IvoryText)
                                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = GoldMuted.copy(alpha = 0.25f))
                                Text(
                                    text = "Contribuição: ${if (ev.valorContribuicao > 0) DateAndCalendarUtils.formatCurrency(ev.valorContribuicao) else "Isento / Comunitário"}",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = GoldLight
                                )
                                val respMembros = pessoas.filter { p ->
                                    ev.responsaveis.contains(p.orunko, ignoreCase = true)
                                }
                                if (respMembros.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Responsáveis (Orunkó):",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = GoldLight
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    FlowRow(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        respMembros.forEach { rm ->
                                            Surface(
                                                color = NavyElevated,
                                                shape = RoundedCornerShape(50),
                                                modifier = Modifier.border(1.dp, GoldMuted.copy(alpha = 0.4f), RoundedCornerShape(50))
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    MemberAvatar(
                                                        fotoBase64 = rm.fotoBase64,
                                                        nameOrOrunko = rm.orunko,
                                                        size = 24.dp
                                                    )
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Text(
                                                        text = rm.orunko,
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = GoldPrimary,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                            }
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                } else {
                                    Text(
                                        text = "Responsáveis (Orunkó): ${ev.responsaveis}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = SilverSubtext
                                    )
                                }
                                Text(
                                    text = "Participantes: ${ev.participantes}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = SilverSubtext
                                )
                                if (ev.observacoes.isNotBlank()) {
                                    Text(
                                        text = "Obs: ${ev.observacoes}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = GoldPrimary
                                    )
                                }
                            }
                        }
                    }
                    item { Spacer(modifier = Modifier.height(24.dp)) }
                }
            }

            1 -> {
                // 🎂 CALENDÁRIO DE ANIVERSARIANTES (SOMENTE ORUNKÓ!)
                val filtrados = if (mesSelecionado == 0) {
                    aniversariantes
                } else {
                    aniversariantes.filter { it.mes == mesSelecionado }
                }
                val tituloMes = if (mesSelecionado == 0) "ANUAL (TODOS OS MESES)" else DateAndCalendarUtils.monthName(mesSelecionado).uppercase()

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "🎂 ANIVERSARIANTES — $tituloMes",
                            style = MaterialTheme.typography.headlineSmall,
                            color = GoldPrimary,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        OrunkoPrivacyBanner()
                        Spacer(modifier = Modifier.height(8.dp))
                        MonthFilterChips(
                            mesSelecionado = mesSelecionado,
                            onSelectMes = onSelectMes
                        )
                    }

                    if (filtrados.isEmpty()) {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = NavyCard)
                            ) {
                                Text(
                                    text = "Nenhum aniversariante cadastrado neste mês. Selecione 'Visão Anual' ou 'Outubro' para ver todos.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = SilverSubtext,
                                    modifier = Modifier.padding(16.dp)
                                )
                            }
                        }
                    } else {
                        items(filtrados, key = { it.pessoaId }) { item ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, GoldPrimary.copy(alpha = 0.45f), RoundedCornerShape(14.dp)),
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = NavyCard)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        MemberAvatar(
                                            fotoBase64 = item.fotoBase64,
                                            nameOrOrunko = item.orunko,
                                            size = 46.dp
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Surface(
                                            color = GoldPrimary.copy(alpha = 0.2f),
                                            shape = RoundedCornerShape(10.dp),
                                            modifier = Modifier.border(1.dp, GoldPrimary, RoundedCornerShape(10.dp))
                                        ) {
                                            Text(
                                                text = item.diaMesFormatado,
                                                style = MaterialTheme.typography.titleMedium,
                                                color = GoldPrimary,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Text(
                                                text = "${item.diaMesFormatado} — ${item.orunko}",
                                                style = MaterialTheme.typography.titleMedium,
                                                color = IvoryText,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = "${item.cargo} • Orixá: ${item.orixa}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = SilverSubtext
                                            )
                                        }
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        Icon(Icons.Default.Cake, contentDescription = null, tint = GoldPrimary, modifier = Modifier.size(18.dp))
                                        if (item.idadeNoAno != null) {
                                            Text(
                                                text = "${item.idadeNoAno} anos em 2026",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = GoldLight
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                    item { Spacer(modifier = Modifier.height(24.dp)) }
                }
            }

            2 -> {
                // 🕯️ CALENDÁRIO DE ODÚN KODÚN (SOMENTE ORUNKÓ!)
                val filtrados = when (modoOrdenacaoOdun) {
                    "ANUAL" -> odunKodunList
                    "PROXIMO" -> odunKodunList.sortedBy { it.diasAteProximo }
                    else -> if (mesSelecionado == 0) odunKodunList else odunKodunList.filter { it.mes == mesSelecionado }
                }
                val subtitulo = when (modoOrdenacaoOdun) {
                    "ANUAL" -> "VISÃO ANUAL COMPLETA (2026)"
                    "PROXIMO" -> "ORDENADO PELOS PRÓXIMOS ODÚN KODÚN"
                    else -> if (mesSelecionado == 0) "TODOS OS MESES" else DateAndCalendarUtils.monthName(mesSelecionado).uppercase()
                }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "🕯️ ODÚN KODÚN — $subtitulo",
                            style = MaterialTheme.typography.headlineSmall,
                            color = GoldPrimary,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        OrunkoPrivacyBanner()
                        Spacer(modifier = Modifier.height(8.dp))
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FilterChip(
                                selected = modoOrdenacaoOdun == "MES",
                                onClick = { modoOrdenacaoOdun = "MES" },
                                label = { Text("Por Mês") }
                            )
                            FilterChip(
                                selected = modoOrdenacaoOdun == "ANUAL",
                                onClick = { modoOrdenacaoOdun = "ANUAL" },
                                label = { Text("Visão Anual") }
                            )
                            FilterChip(
                                selected = modoOrdenacaoOdun == "PROXIMO",
                                onClick = { modoOrdenacaoOdun = "PROXIMO" },
                                label = { Text("Próximos Odún Kodún") }
                            )
                        }
                        if (modoOrdenacaoOdun == "MES") {
                            Spacer(modifier = Modifier.height(6.dp))
                            MonthFilterChips(
                                mesSelecionado = mesSelecionado,
                                onSelectMes = onSelectMes
                            )
                        }
                    }

                    items(filtrados, key = { it.pessoaId }) { odun ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(
                                    1.5.dp,
                                    if (odun.marcoObrigacao != null) GoldPrimary else GoldMuted.copy(alpha = 0.45f),
                                    RoundedCornerShape(14.dp)
                                ),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (odun.marcoObrigacao != null) NavyElevated else NavyCard
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    MemberAvatar(
                                        fotoBase64 = odun.fotoBase64,
                                        nameOrOrunko = odun.orunko,
                                        size = 46.dp
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Surface(
                                        color = GoldPrimary.copy(alpha = 0.22f),
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.border(1.dp, GoldPrimary, RoundedCornerShape(10.dp))
                                    ) {
                                        Text(
                                            text = odun.diaMesFormatado,
                                            style = MaterialTheme.typography.titleMedium,
                                            color = GoldPrimary,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = "${odun.diaMesFormatado} — ${odun.orunko} — ${odun.anosCompletos} anos",
                                            style = MaterialTheme.typography.titleMedium,
                                            color = GoldLight,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "${odun.cargo} • Orixá: ${odun.orixa} • Iniciação: ${odun.dataIniciacao}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = SilverSubtext
                                        )
                                        if (odun.marcoObrigacao != null) {
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Surface(
                                                color = GoldPrimary.copy(alpha = 0.2f),
                                                shape = RoundedCornerShape(6.dp)
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.AutoAwesome,
                                                        contentDescription = null,
                                                        tint = GoldPrimary,
                                                        modifier = Modifier.size(14.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text(
                                                        text = odun.marcoObrigacao,
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = GoldPrimary,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                                Icon(
                                    imageVector = Icons.Default.LocalFireDepartment,
                                    contentDescription = "Odún Kodún",
                                    tint = GoldPrimary
                                )
                            }
                        }
                    }
                    item { Spacer(modifier = Modifier.height(24.dp)) }
                }
            }
        }
    }

    if (exibirModalEvento && isAdmin) {
        EventoFormDialog(
            eventoInicial = eventoEmEdicao,
            onDismiss = { exibirModalEvento = false },
            onConfirm = { id, nome, data, horario, local, desc, valor, resp, part, obs ->
                onSalvarEvento(id, nome, data, horario, local, desc, valor, resp, part, obs)
                exibirModalEvento = false
            }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun MonthFilterChips(
    mesSelecionado: Int,
    onSelectMes: (Int) -> Unit
) {
    val opcoes = listOf(
        0 to "Anual (Todos)",
        10 to "Outubro",
        11 to "Novembro",
        12 to "Dezembro",
        4 to "Abril",
        5 to "Maio"
    )
    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        opcoes.forEach { (m, label) ->
            FilterChip(
                selected = mesSelecionado == m,
                onClick = { onSelectMes(m) },
                label = { Text(label) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = GoldPrimary,
                    selectedLabelColor = NavyDeep
                )
            )
        }
    }
}

@Composable
private fun EventoFormDialog(
    eventoInicial: EventoEntity?,
    onDismiss: () -> Unit,
    onConfirm: (
        idExistente: Long,
        nome: String,
        data: String,
        horario: String,
        local: String,
        descricao: String,
        valorContribuicao: Double,
        responsaveis: String,
        participantes: String,
        observacoes: String
    ) -> Unit
) {
    var nome by rememberSaveable { mutableStateOf(eventoInicial?.nome ?: "") }
    var data by rememberSaveable { mutableStateOf(eventoInicial?.data ?: "25/10/2026") }
    var horario by rememberSaveable { mutableStateOf(eventoInicial?.horario ?: "18:30") }
    var local by rememberSaveable { mutableStateOf(eventoInicial?.local ?: "Barracão Principal — Casa do Ogum - RJ") }
    var descricao by rememberSaveable { mutableStateOf(eventoInicial?.descricao ?: "") }
    var valorTexto by rememberSaveable { mutableStateOf((eventoInicial?.valorContribuicao ?: 0.0).toString()) }
    var responsaveis by rememberSaveable { mutableStateOf(eventoInicial?.responsaveis ?: "Ogunjobi, Ogunlade") }
    var participantes by rememberSaveable { mutableStateOf(eventoInicial?.participantes ?: "Todos os filhos da Casa") }
    var observacoes by rememberSaveable { mutableStateOf(eventoInicial?.observacoes ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = NavyCard,
        title = {
            Text(
                text = if (eventoInicial == null) "Cadastrar Novo Evento" else "Editar Evento",
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
                OutlinedTextField(
                    value = nome,
                    onValueChange = { nome = it },
                    label = { Text("Nome do Evento *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = data,
                        onValueChange = { data = it },
                        label = { Text("Data (DD/MM/AAAA)") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = horario,
                        onValueChange = { horario = it },
                        label = { Text("Horário (HH:mm)") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }
                OutlinedTextField(
                    value = local,
                    onValueChange = { local = it },
                    label = { Text("Local") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = descricao,
                    onValueChange = { descricao = it },
                    label = { Text("Descrição") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = valorTexto,
                    onValueChange = { valorTexto = it },
                    label = { Text("Valor / Contribuição (R$)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = responsaveis,
                    onValueChange = { responsaveis = it },
                    label = { Text("Responsáveis (Orunkó)") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = participantes,
                    onValueChange = { participantes = it },
                    label = { Text("Participantes") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = observacoes,
                    onValueChange = { observacoes = it },
                    label = { Text("Observações") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val v = valorTexto.replace(",", ".").toDoubleOrNull() ?: 0.0
                    onConfirm(
                        eventoInicial?.id ?: 0L,
                        nome,
                        data,
                        horario,
                        local,
                        descricao,
                        v,
                        responsaveis,
                        participantes,
                        observacoes
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = NavyDeep)
            ) {
                Text("Salvar Evento", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = SilverSubtext)
            }
        }
    )
}
