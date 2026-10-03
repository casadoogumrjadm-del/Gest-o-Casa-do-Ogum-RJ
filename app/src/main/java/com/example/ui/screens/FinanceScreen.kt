package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CompareArrows
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.TableView
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
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.PagamentoEntity
import com.example.data.PainelFinanceiroResumo
import com.example.data.PessoaEntity
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FinanceScreen(
    usuarioLogado: PessoaEntity,
    pessoas: List<PessoaEntity>,
    pagamentos: List<PagamentoEntity>,
    mesSelecionado: Int,
    anoSelecionado: Int,
    resumoMes: PainelFinanceiroResumo,
    onSelectMes: (Int, Int) -> Unit,
    onSalvarPagamento: (
        idExistente: Long,
        pessoaId: Long,
        competenciaMes: Int,
        ano: Int,
        valor: Double,
        dataPagamento: String,
        status: String,
        observacao: String,
        statusAnterior: String?
    ) -> Unit,
    onExportarFinanceiro: (formatoPdf: Boolean, mes: Int, ano: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val isAdmin = usuarioLogado.perfilAcesso == "ADMIN"
    val context = LocalContext.current
    val mapaPessoas = remember(pessoas) { pessoas.associateBy { it.id } }

    var filtroStatus by rememberSaveable { mutableStateOf("TODOS") }
    var pagamentoEmEdicao by remember { mutableStateOf<PagamentoEntity?>(null) }
    var exibirDialogLancamento by rememberSaveable { mutableStateOf(false) }

    // Se for Membro, mostra exclusivamente seus próprios pagamentos e mensalidades pendentes
    if (!isAdmin) {
        val meusPagamentos = pagamentos.filter { it.pessoaId == usuarioLogado.id }
        val pendentesCount = meusPagamentos.count { it.status != "PAGO" }
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "💰 Minhas Mensalidades e Pagamentos",
                    style = MaterialTheme.typography.headlineSmall,
                    color = GoldPrimary,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Surface(
                    color = NavyElevated,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Lock, contentDescription = null, tint = GoldPrimary, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "O controle e baixa de pagamentos são manuais e exclusivos do Administrador. Você possui $pendentesCount mensalidade(s) pendente(s)/em aberto.",
                            style = MaterialTheme.typography.bodySmall,
                            color = SilverSubtext
                        )
                    }
                }
            }

            items(meusPagamentos, key = { it.id }) { pag ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, GoldMuted.copy(alpha = 0.4f), RoundedCornerShape(14.dp)),
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
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "${DateAndCalendarUtils.monthName(pag.competenciaMes).uppercase()}/${pag.ano}",
                                style = MaterialTheme.typography.titleMedium,
                                color = GoldPrimary,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Valor: ${DateAndCalendarUtils.formatCurrency(pag.valor)}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = IvoryText
                            )
                            if (pag.dataPagamento.isNotBlank()) {
                                Text(
                                    text = "Data do pagamento: ${pag.dataPagamento}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = StatusPaidGreen
                                )
                            }
                            if (pag.observacao.isNotBlank()) {
                                Text(
                                    text = "Obs: ${pag.observacao}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = SilverSubtext
                                )
                            }
                        }
                        PaymentStatusBadge(status = pag.status)
                    }
                }
            }
            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
        return
    }

    // VISÃO DO ADMINISTRADOR — PAINEL FINANCEIRO COMPLETO
    val pagamentosDoMes = pagamentos.filter {
        it.competenciaMes == mesSelecionado && it.ano == anoSelecionado &&
            (filtroStatus == "TODOS" || it.status == filtroStatus)
    }

    // Cálculo comparativo com Setembro/2026 e Acumulado Anual 2026
    val arrecadadoSetembro = pagamentos.filter { it.competenciaMes == 9 && it.ano == anoSelecionado && it.status == "PAGO" }.sumOf { it.valor }
    val arrecadadoAnual = pagamentos.filter { it.ano == anoSelecionado && it.status == "PAGO" }.sumOf { it.valor }
    val previstoAnual = pagamentos.filter { it.ano == anoSelecionado }.sumOf { it.valor }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(4.dp))
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
                            text = "Painel Financeiro da Casa",
                            style = MaterialTheme.typography.headlineSmall,
                            color = GoldPrimary,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Controle manual exclusivo do Administrador",
                            style = MaterialTheme.typography.bodySmall,
                            color = SilverSubtext
                        )
                    }
                }
                Button(
                    onClick = {
                        pagamentoEmEdicao = null
                        exibirDialogLancamento = true
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = NavyDeep),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("add_payment_button")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Lançar", fontWeight = FontWeight.Bold)
                }
            }
        }

        // Seletor de Competência Mensal
        item {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(9 to "Setembro/2026", 10 to "Outubro/2026", 11 to "Novembro/2026", 12 to "Dezembro/2026").forEach { (m, label) ->
                    FilterChip(
                        selected = mesSelecionado == m,
                        onClick = { onSelectMes(m, 2026) },
                        label = { Text(label) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = GoldPrimary,
                            selectedLabelColor = NavyDeep
                        )
                    )
                }
            }
        }

        // Card Resumo Mensal (Exatamente como no exemplo: OUTUBRO/2026 Arrecadado, Pendente, Pagos, Em atraso)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.5.dp, GoldPrimary.copy(alpha = 0.7f), RoundedCornerShape(18.dp)),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = NavyCard)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "${DateAndCalendarUtils.monthName(mesSelecionado).uppercase()}/$anoSelecionado",
                        style = MaterialTheme.typography.titleLarge,
                        color = GoldPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        FinanceKpiBox("Total Previsto", DateAndCalendarUtils.formatCurrency(resumoMes.totalPrevisto), GoldLight)
                        FinanceKpiBox("Arrecadado", DateAndCalendarUtils.formatCurrency(resumoMes.totalRecebido), StatusPaidGreen)
                        FinanceKpiBox("Pendente", DateAndCalendarUtils.formatCurrency(resumoMes.totalPendente), StatusPendingYellow)
                    }
                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = GoldMuted.copy(alpha = 0.3f))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        FinanceKpiBox("Pagos", "${resumoMes.qtdPagos} pessoas", StatusPaidGreen)
                        FinanceKpiBox("Pendentes", "${resumoMes.qtdPendentes} pessoas", StatusPendingYellow)
                        FinanceKpiBox("Em atraso", "${resumoMes.qtdInadimplentes} pessoas", StatusOverdueRed)
                    }
                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = GoldMuted.copy(alpha = 0.3f))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CompareArrows, contentDescription = null, tint = GoldPrimary, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Comparativo Set/26: ${DateAndCalendarUtils.formatCurrency(arrecadadoSetembro)}  |  Anual 2026: ${DateAndCalendarUtils.formatCurrency(arrecadadoAnual)} / ${DateAndCalendarUtils.formatCurrency(previstoAnual)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = SilverSubtext
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = { onExportarFinanceiro(true, mesSelecionado, anoSelecionado) },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.PictureAsPdf, contentDescription = null, tint = GoldPrimary, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Exportar PDF", color = GoldLight, style = MaterialTheme.typography.labelMedium)
                        }
                        OutlinedButton(
                            onClick = { onExportarFinanceiro(false, mesSelecionado, anoSelecionado) },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.TableView, contentDescription = null, tint = GoldPrimary, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Exportar Excel", color = GoldLight, style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }
            }
        }

        // Filtro por Status
        item {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(
                    "TODOS" to "Todos",
                    "PAGO" to "🟢 Pagos",
                    "PENDENTE" to "🟡 Pendentes",
                    "EM_ATRASO" to "🔴 Em Atraso"
                ).forEach { (key, label) ->
                    FilterChip(
                        selected = filtroStatus == key,
                        onClick = { filtroStatus = key },
                        label = { Text(label) }
                    )
                }
            }
        }

        items(pagamentosDoMes, key = { it.id }) { pag ->
            val membro = mapaPessoas[pag.pessoaId]
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, GoldMuted.copy(alpha = 0.4f), RoundedCornerShape(14.dp))
                    .clickable {
                        pagamentoEmEdicao = pag
                        exibirDialogLancamento = true
                    },
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = NavyCard)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = membro?.orunko ?: "Membro #${pag.pessoaId}",
                                style = MaterialTheme.typography.titleMedium,
                                color = GoldPrimary,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Nome Civil: ${membro?.nome ?: "-"} • ${membro?.cargo ?: ""}",
                                style = MaterialTheme.typography.bodySmall,
                                color = SilverSubtext
                            )
                        }
                        PaymentStatusBadge(status = pag.status)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Valor: ${DateAndCalendarUtils.formatCurrency(pag.valor)} • Comp: %02d/%d".format(pag.competenciaMes, pag.ano),
                                style = MaterialTheme.typography.bodyMedium,
                                color = IvoryText,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = if (pag.dataPagamento.isNotBlank()) "Pago em: ${pag.dataPagamento}" else "Pagamento ainda não registrado",
                                style = MaterialTheme.typography.bodySmall,
                                color = SilverSubtext
                            )
                            if (pag.observacao.isNotBlank()) {
                                Text(
                                    text = "Obs: ${pag.observacao}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = GoldLight
                                )
                            }
                            Text(
                                text = "Lançado por: ${pag.adminResponsavel} em ${pag.dataHoraRegistro}",
                                style = MaterialTheme.typography.labelSmall,
                                color = SilverSubtext
                            )
                        }
                        OutlinedButton(
                            onClick = {
                                pagamentoEmEdicao = pag
                                exibirDialogLancamento = true
                            }
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = "Corrigir", tint = GoldPrimary, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Corrigir", color = GoldLight, style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(24.dp)) }
    }

    if (exibirDialogLancamento && isAdmin) {
        PagamentoFormDialog(
            pagamentoInicial = pagamentoEmEdicao,
            pessoas = pessoas,
            mesPadrao = mesSelecionado,
            anoPadrao = anoSelecionado,
            onDismiss = { exibirDialogLancamento = false },
            onConfirm = { id, pid, mes, ano, valor, dataPag, status, obs, statusAnterior ->
                onSalvarPagamento(id, pid, mes, ano, valor, dataPag, status, obs, statusAnterior)
                exibirDialogLancamento = false
            }
        )
    }
}

@Composable
private fun FinanceKpiBox(label: String, value: String, color: Color) {
    Column {
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = SilverSubtext)
        Text(text = value, style = MaterialTheme.typography.titleMedium, color = color, fontWeight = FontWeight.Bold)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PagamentoFormDialog(
    pagamentoInicial: PagamentoEntity?,
    pessoas: List<PessoaEntity>,
    mesPadrao: Int,
    anoPadrao: Int,
    onDismiss: () -> Unit,
    onConfirm: (
        idExistente: Long,
        pessoaId: Long,
        competenciaMes: Int,
        ano: Int,
        valor: Double,
        dataPagamento: String,
        status: String,
        observacao: String,
        statusAnterior: String?
    ) -> Unit
) {
    var pessoaIdSelecionada by rememberSaveable {
        mutableLongStateOf(pagamentoInicial?.pessoaId ?: (pessoas.firstOrNull()?.id ?: 1L))
    }
    var competenciaMes by rememberSaveable { mutableIntStateOf(pagamentoInicial?.competenciaMes ?: mesPadrao) }
    var ano by rememberSaveable { mutableIntStateOf(pagamentoInicial?.ano ?: anoPadrao) }
    var valorTexto by rememberSaveable { mutableStateOf((pagamentoInicial?.valor ?: 150.0).toString()) }
    var status by rememberSaveable { mutableStateOf(pagamentoInicial?.status ?: "PAGO") }
    var dataPagamento by rememberSaveable {
        mutableStateOf(pagamentoInicial?.dataPagamento?.ifBlank { DateAndCalendarUtils.currentDateFormatted() } ?: DateAndCalendarUtils.currentDateFormatted())
    }
    var observacao by rememberSaveable { mutableStateOf(pagamentoInicial?.observacao ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = NavyCard,
        title = {
            Text(
                text = if (pagamentoInicial == null) "Registrar Pagamento Manual" else "Corrigir Lançamento Financeiro",
                style = MaterialTheme.typography.titleLarge,
                color = GoldPrimary,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Selecione o Membro (Orunkó):", style = MaterialTheme.typography.labelMedium, color = GoldLight)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    pessoas.forEach { p ->
                        FilterChip(
                            selected = pessoaIdSelecionada == p.id,
                            onClick = { pessoaIdSelecionada = p.id },
                            label = { Text(p.orunko) }
                        )
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = competenciaMes.toString(),
                        onValueChange = { competenciaMes = it.toIntOrNull()?.coerceIn(1, 12) ?: competenciaMes },
                        label = { Text("Mês (1-12)") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = ano.toString(),
                        onValueChange = { ano = it.toIntOrNull() ?: ano },
                        label = { Text("Ano") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                }
                OutlinedTextField(
                    value = valorTexto,
                    onValueChange = { valorTexto = it },
                    label = { Text("Valor (R$)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )
                Text("Status do Pagamento:", style = MaterialTheme.typography.labelMedium, color = GoldLight)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("PAGO" to "🟢 PAGO", "PENDENTE" to "🟡 PENDENTE", "EM_ATRASO" to "🔴 EM ATRASO").forEach { (k, label) ->
                        FilterChip(
                            selected = status == k,
                            onClick = { status = k },
                            label = { Text(label) }
                        )
                    }
                }
                if (status == "PAGO") {
                    OutlinedTextField(
                        value = dataPagamento,
                        onValueChange = { dataPagamento = it },
                        label = { Text("Data do Pagamento (DD/MM/AAAA)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                OutlinedTextField(
                    value = observacao,
                    onValueChange = { observacao = it },
                    label = { Text("Observação / Justificativa") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val v = valorTexto.replace(",", ".").toDoubleOrNull() ?: 150.0
                    onConfirm(
                        pagamentoInicial?.id ?: 0L,
                        pessoaIdSelecionada,
                        competenciaMes,
                        ano,
                        v,
                        dataPagamento,
                        status,
                        observacao,
                        pagamentoInicial?.status
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = NavyDeep)
            ) {
                Text("Salvar e Registrar no Histórico", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = SilverSubtext)
            }
        }
    )
}
