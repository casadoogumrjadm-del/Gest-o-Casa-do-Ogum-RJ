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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Announcement
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AssignmentTurnedIn
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TableView
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.unit.dp
import com.example.data.ComunicadoEntity
import com.example.data.HistoricoAdminEntity
import com.example.data.PagamentoEntity
import com.example.data.PessoaEntity
import com.example.data.TarefaEntity
import com.example.ui.theme.GoldLight
import com.example.ui.theme.GoldMuted
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.IvoryText
import com.example.ui.theme.NavyCard
import com.example.ui.theme.NavyDeep
import com.example.ui.theme.NavyElevated
import com.example.ui.theme.SilverSubtext
import com.example.ui.theme.StatusInProgressBlue
import com.example.ui.theme.StatusOverdueRed
import com.example.ui.theme.StatusPaidGreen
import com.example.ui.theme.StatusPendingYellow
import com.example.util.DateAndCalendarUtils

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ManagementAndReportsScreen(
    usuarioLogado: PessoaEntity,
    abaInicial: Int = 0, // 0 = Comunicados, 1 = Tarefas, 2 = Relatórios, 3 = Auditoria
    pessoas: List<PessoaEntity>,
    pagamentos: List<PagamentoEntity>,
    comunicados: List<ComunicadoEntity>,
    tarefas: List<TarefaEntity>,
    historico: List<HistoricoAdminEntity>,
    onSalvarComunicado: (id: Long, titulo: String, texto: String, publico: String, destaque: Boolean) -> Unit,
    onAlternarStatusComunicado: (ComunicadoEntity) -> Unit,
    onSalvarTarefa: (id: Long, descricao: String, responsavel: PessoaEntity, prazo: String, prioridade: String, status: String, obs: String) -> Unit,
    onAlterarStatusTarefa: (TarefaEntity, String) -> Unit,
    onExportarFinanceiro: (formatoPdf: Boolean, mes: Int, ano: Int) -> Unit,
    onExportarCadastral: (formatoPdf: Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val isAdmin = usuarioLogado.perfilAcesso == "ADMIN"
    var abaAtual by rememberSaveable(abaInicial) { mutableIntStateOf(if (!isAdmin && abaInicial > 1) 0 else abaInicial) }

    var exibirModalComunicado by rememberSaveable { mutableStateOf(false) }
    var exibirModalTarefa by rememberSaveable { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxSize()) {
        TabRow(
            selectedTabIndex = abaAtual,
            containerColor = NavyCard,
            contentColor = GoldPrimary
        ) {
            Tab(
                selected = abaAtual == 0,
                onClick = { abaAtual = 0 },
                text = { Text("📢 Avisos", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold) },
                modifier = Modifier.testTag("tab_comunicados")
            )
            Tab(
                selected = abaAtual == 1,
                onClick = { abaAtual = 1 },
                text = { Text("✅ Tarefas", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold) },
                modifier = Modifier.testTag("tab_tarefas")
            )
            if (isAdmin) {
                Tab(
                    selected = abaAtual == 2,
                    onClick = { abaAtual = 2 },
                    text = { Text("📊 Relatórios", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold) },
                    modifier = Modifier.testTag("tab_relatorios")
                )
                Tab(
                    selected = abaAtual == 3,
                    onClick = { abaAtual = 3 },
                    text = { Text("🔐 Auditoria", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold) },
                    modifier = Modifier.testTag("tab_auditoria")
                )
            }
        }

        when (abaAtual) {
            0 -> {
                // 📢 COMUNICADOS
                val visiveis = if (isAdmin) comunicados else comunicados.filter { it.ativo }
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
                                    text = "📢 Comunicados da Casa",
                                    style = MaterialTheme.typography.headlineSmall,
                                    color = GoldPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Mural oficial de avisos e orientações",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = SilverSubtext
                                )
                            }
                            if (isAdmin) {
                                Button(
                                    onClick = { exibirModalComunicado = true },
                                    colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = NavyDeep),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.testTag("add_comunicado_button")
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Novo Aviso", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    items(visiveis, key = { it.id }) { com ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(
                                    width = if (com.destaque) 1.5.dp else 1.dp,
                                    color = if (com.destaque) GoldPrimary else GoldMuted.copy(alpha = 0.4f),
                                    shape = RoundedCornerShape(16.dp)
                                ),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (com.destaque) NavyElevated else NavyCard
                            )
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        if (com.destaque) {
                                            Surface(
                                                color = GoldPrimary.copy(alpha = 0.22f),
                                                shape = RoundedCornerShape(50)
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Icon(Icons.Default.Star, contentDescription = null, tint = GoldPrimary, modifier = Modifier.size(14.dp))
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text(
                                                        text = "⭐ DESTAQUE",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = GoldPrimary,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                            }
                                            Spacer(modifier = Modifier.width(8.dp))
                                        }
                                        Text(
                                            text = "Público: ${com.publicoAlvo}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = GoldLight
                                        )
                                    }
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = com.data,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = SilverSubtext
                                        )
                                        if (isAdmin) {
                                            IconButton(onClick = { onAlternarStatusComunicado(com) }) {
                                                Icon(
                                                    imageVector = if (com.ativo) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                                    contentDescription = "Alternar status do comunicado",
                                                    tint = if (com.ativo) StatusPaidGreen else StatusOverdueRed
                                                )
                                            }
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = com.titulo,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = GoldPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = com.texto,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = IvoryText
                                )
                            }
                        }
                    }
                    item { Spacer(modifier = Modifier.height(24.dp)) }
                }
            }

            1 -> {
                // ✅ TAREFAS E RESPONSABILIDADES
                val listaTarefas = if (isAdmin) tarefas else tarefas.filter { it.responsavelPessoaId == usuarioLogado.id }
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
                                    text = if (isAdmin) "✅ Controle Geral de Tarefas" else "✅ Minhas Tarefas e Funções",
                                    style = MaterialTheme.typography.headlineSmall,
                                    color = GoldPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Responsabilidades identificadas por Orunkó",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = SilverSubtext
                                )
                            }
                            if (isAdmin) {
                                Button(
                                    onClick = { exibirModalTarefa = true },
                                    colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = NavyDeep),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.testTag("add_tarefa_button")
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Nova Tarefa", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    items(listaTarefas, key = { it.id }) { tar ->
                        val corPrioridade = when (tar.prioridade) {
                            "Urgente" -> StatusOverdueRed
                            "Alta" -> StatusPendingYellow
                            "Normal" -> GoldPrimary
                            else -> SilverSubtext
                        }
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, corPrioridade.copy(alpha = 0.5f), RoundedCornerShape(14.dp)),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = NavyCard)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        color = corPrioridade.copy(alpha = 0.18f),
                                        shape = RoundedCornerShape(50)
                                    ) {
                                        Text(
                                            text = "Prioridade: ${tar.prioridade}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = corPrioridade,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                                        )
                                    }
                                    TaskStatusChip(status = tar.status)
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = tar.descricao,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = IvoryText,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Responsável (Orunkó): ${tar.responsavelOrunko}  •  Prazo: ${tar.prazo}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = GoldLight
                                )
                                if (tar.observacoes.isNotBlank()) {
                                    Text(
                                        text = "Observações: ${tar.observacoes}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = SilverSubtext
                                    )
                                }
                                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = GoldMuted.copy(alpha = 0.25f))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    listOf(
                                        "PENDENTE" to "⚪ Pendente",
                                        "EM_ANDAMENTO" to "🔵 Em andamento",
                                        "CONCLUIDA" to "🟢 Concluída"
                                    ).forEach { (stKey, stLabel) ->
                                        FilterChip(
                                            selected = tar.status == stKey,
                                            onClick = { onAlterarStatusTarefa(tar, stKey) },
                                            label = { Text(stLabel, style = MaterialTheme.typography.labelSmall) }
                                        )
                                    }
                                }
                            }
                        }
                    }
                    item { Spacer(modifier = Modifier.height(24.dp)) }
                }
            }

            2 -> {
                // 📊 RELATÓRIOS (FINANCEIROS + CADASTRAIS + EXPORTAÇÃO PDF & EXCEL)
                val ativosCount = pessoas.count { it.ativo }
                val inativosCount = pessoas.count { !it.ativo }
                val porCargo = pessoas.groupingBy { it.cargo }.eachCount()
                val porOrixa = pessoas.groupingBy { it.orixa }.eachCount()
                val pagamentosOut = pagamentos.filter { it.competenciaMes == 10 && it.ano == 2026 }
                val totalArrecadadoOut = pagamentosOut.filter { it.status == "PAGO" }.sumOf { it.valor }
                val totalPrevistoOut = pagamentosOut.sumOf { it.valor }
                val totalPendenteOut = pagamentosOut.filter { it.status != "PAGO" }.sumOf { it.valor }
                val totalArrecadadoAnual = pagamentos.filter { it.ano == 2026 && it.status == "PAGO" }.sumOf { it.valor }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    item {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "📊 Central de Relatórios e Exportação",
                            style = MaterialTheme.typography.headlineSmall,
                            color = GoldPrimary,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Relatórios Financeiros e Cadastrais com exportação para PDF e Excel",
                            style = MaterialTheme.typography.bodySmall,
                            color = SilverSubtext
                        )
                    }

                    // Relatórios Financeiros
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, GoldPrimary.copy(alpha = 0.6f), RoundedCornerShape(16.dp)),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = NavyCard)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "1. Relatórios Financeiros",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = GoldPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                ReportStatLine("Total Previsto (Outubro/2026)", DateAndCalendarUtils.formatCurrency(totalPrevistoOut))
                                ReportStatLine("Total Arrecadado (Outubro/2026)", DateAndCalendarUtils.formatCurrency(totalArrecadadoOut))
                                ReportStatLine("Pendências e Inadimplência", DateAndCalendarUtils.formatCurrency(totalPendenteOut))
                                ReportStatLine("Pagamentos Realizados (Out/2026)", "${pagamentosOut.count { it.status == "PAGO" }} membros")
                                ReportStatLine("Em Atraso / Inadimplentes", "${pagamentosOut.count { it.status == "EM_ATRASO" }} membros")
                                ReportStatLine("Acumulado Anual Recebido (2026)", DateAndCalendarUtils.formatCurrency(totalArrecadadoAnual))

                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "Exportar Relatório Financeiro Mensal (Outubro/2026):",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = GoldLight
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Button(
                                        onClick = { onExportarFinanceiro(true, 10, 2026) },
                                        colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = NavyDeep),
                                        modifier = Modifier.weight(1f).testTag("export_fin_pdf_button")
                                    ) {
                                        Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("PDF Mensal", fontWeight = FontWeight.Bold)
                                    }
                                    OutlinedButton(
                                        onClick = { onExportarFinanceiro(false, 10, 2026) },
                                        modifier = Modifier.weight(1f).testTag("export_fin_excel_button")
                                    ) {
                                        Icon(Icons.Default.TableView, contentDescription = null, tint = GoldPrimary, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Excel Mensal", color = GoldLight)
                                    }
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    OutlinedButton(
                                        onClick = { onExportarFinanceiro(true, 0, 2026) },
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("PDF Anual 2026", color = GoldLight, style = MaterialTheme.typography.labelMedium)
                                    }
                                    OutlinedButton(
                                        onClick = { onExportarFinanceiro(false, 0, 2026) },
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("Excel Anual 2026", color = GoldLight, style = MaterialTheme.typography.labelMedium)
                                    }
                                }
                            }
                        }
                    }

                    // Relatórios Cadastrais
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, GoldPrimary.copy(alpha = 0.6f), RoundedCornerShape(16.dp)),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = NavyCard)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "2. Relatórios Cadastrais da Casa",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = GoldPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                ReportStatLine("Total de Pessoas Cadastradas", "${pessoas.size}")
                                ReportStatLine("Membros Ativos", "$ativosCount")
                                ReportStatLine("Membros Inativos", "$inativosCount")

                                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = GoldMuted.copy(alpha = 0.3f))
                                Text("Pessoas por Cargo:", style = MaterialTheme.typography.labelMedium, color = GoldLight)
                                porCargo.forEach { (cargo, qtd) ->
                                    ReportStatLine("• $cargo", "$qtd pessoa(s)")
                                }

                                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = GoldMuted.copy(alpha = 0.3f))
                                Text("Pessoas por Orixá:", style = MaterialTheme.typography.labelMedium, color = GoldLight)
                                porOrixa.forEach { (orixa, qtd) ->
                                    ReportStatLine("• $orixa", "$qtd pessoa(s)")
                                }

                                Spacer(modifier = Modifier.height(12.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Button(
                                        onClick = { onExportarCadastral(true) },
                                        colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = NavyDeep),
                                        modifier = Modifier.weight(1f).testTag("export_cad_pdf_button")
                                    ) {
                                        Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Exportar PDF", fontWeight = FontWeight.Bold)
                                    }
                                    OutlinedButton(
                                        onClick = { onExportarCadastral(false) },
                                        modifier = Modifier.weight(1f).testTag("export_cad_excel_button")
                                    ) {
                                        Icon(Icons.Default.TableView, contentDescription = null, tint = GoldPrimary, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Exportar Excel", color = GoldLight)
                                    }
                                }
                            }
                        }
                    }

                    item { Spacer(modifier = Modifier.height(24.dp)) }
                }
            }

            3 -> {
                // 🔐 HISTÓRICO ADMINISTRATIVO (AUDITORIA)
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    item {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "🔐 Histórico Administrativo",
                            style = MaterialTheme.typography.headlineSmall,
                            color = GoldPrimary,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Auditoria completa das ações realizadas na Casa (${historico.size} registros)",
                            style = MaterialTheme.typography.bodySmall,
                            color = SilverSubtext
                        )
                    }

                    items(historico, key = { it.id }) { log ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, GoldMuted.copy(alpha = 0.35f), RoundedCornerShape(12.dp)),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = NavyCard)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        color = GoldPrimary.copy(alpha = 0.18f),
                                        shape = RoundedCornerShape(50)
                                    ) {
                                        Text(
                                            text = log.categoria,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = GoldPrimary,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                        )
                                    }
                                    Text(
                                        text = log.dataHora,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = SilverSubtext
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = log.acao,
                                    style = MaterialTheme.typography.titleSmall,
                                    color = GoldLight,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = log.detalhes,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = IvoryText
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Responsável: ${log.adminNome}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = SilverSubtext
                                )
                            }
                        }
                    }

                    item { Spacer(modifier = Modifier.height(24.dp)) }
                }
            }
        }
    }

    if (exibirModalComunicado && isAdmin) {
        ComunicadoFormDialog(
            onDismiss = { exibirModalComunicado = false },
            onConfirm = { titulo, texto, publico, destaque ->
                onSalvarComunicado(0L, titulo, texto, publico, destaque)
                exibirModalComunicado = false
            }
        )
    }

    if (exibirModalTarefa && isAdmin) {
        TarefaFormDialog(
            pessoas = pessoas.filter { it.ativo },
            onDismiss = { exibirModalTarefa = false },
            onConfirm = { desc, resp, prazo, prio, status, obs ->
                onSalvarTarefa(0L, desc, resp, prazo, prio, status, obs)
                exibirModalTarefa = false
            }
        )
    }
}

@Composable
private fun ReportStatLine(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.bodySmall, color = SilverSubtext)
        Text(text = value, style = MaterialTheme.typography.bodySmall, color = IvoryText, fontWeight = FontWeight.Bold)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ComunicadoFormDialog(
    onDismiss: () -> Unit,
    onConfirm: (titulo: String, texto: String, publico: String, destaque: Boolean) -> Unit
) {
    var titulo by rememberSaveable { mutableStateOf("") }
    var texto by rememberSaveable { mutableStateOf("") }
    var publico by rememberSaveable { mutableStateOf("Todos") }
    var destaque by rememberSaveable { mutableStateOf(true) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = NavyCard,
        title = {
            Text("Publicar Comunicado", style = MaterialTheme.typography.titleLarge, color = GoldPrimary, fontWeight = FontWeight.Bold)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = titulo,
                    onValueChange = { titulo = it },
                    label = { Text("Título do Comunicado *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = texto,
                    onValueChange = { texto = it },
                    label = { Text("Texto Completo *") },
                    modifier = Modifier.fillMaxWidth()
                )
                Text("Público-Alvo:", style = MaterialTheme.typography.labelMedium, color = GoldLight)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("Todos", "Iniciados", "Ogãs e Ekedis", "Diretoria").forEach { pub ->
                        FilterChip(
                            selected = publico == pub,
                            onClick = { publico = pub },
                            label = { Text(pub) }
                        )
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = destaque, onCheckedChange = { destaque = it })
                    Text("Marcar como ⭐ DESTAQUE no topo da tela dos membros", style = MaterialTheme.typography.bodySmall, color = GoldLight)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(titulo, texto, publico, destaque) },
                colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = NavyDeep)
            ) {
                Text("Publicar", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = SilverSubtext)
            }
        }
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TarefaFormDialog(
    pessoas: List<PessoaEntity>,
    onDismiss: () -> Unit,
    onConfirm: (descricao: String, responsavel: PessoaEntity, prazo: String, prioridade: String, status: String, obs: String) -> Unit
) {
    var descricao by rememberSaveable { mutableStateOf("") }
    var responsavelSelecionado by remember { mutableStateOf(pessoas.firstOrNull()) }
    var prazo by rememberSaveable { mutableStateOf("15/10/2026") }
    var prioridade by rememberSaveable { mutableStateOf("Alta") }
    var status by rememberSaveable { mutableStateOf("PENDENTE") }
    var obs by rememberSaveable { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = NavyCard,
        title = {
            Text("Criar Nova Tarefa", style = MaterialTheme.typography.titleLarge, color = GoldPrimary, fontWeight = FontWeight.Bold)
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = descricao,
                    onValueChange = { descricao = it },
                    label = { Text("Descrição da Tarefa *") },
                    modifier = Modifier.fillMaxWidth()
                )
                Text("Responsável (Orunkó):", style = MaterialTheme.typography.labelMedium, color = GoldLight)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    pessoas.forEach { p ->
                        FilterChip(
                            selected = responsavelSelecionado?.id == p.id,
                            onClick = { responsavelSelecionado = p },
                            label = { Text(p.orunko) }
                        )
                    }
                }
                OutlinedTextField(
                    value = prazo,
                    onValueChange = { prazo = it },
                    label = { Text("Prazo (DD/MM/AAAA)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Text("Prioridade:", style = MaterialTheme.typography.labelMedium, color = GoldLight)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("Baixa", "Normal", "Alta", "Urgente").forEach { prio ->
                        FilterChip(
                            selected = prioridade == prio,
                            onClick = { prioridade = prio },
                            label = { Text(prio) }
                        )
                    }
                }
                OutlinedTextField(
                    value = obs,
                    onValueChange = { obs = it },
                    label = { Text("Observações") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    responsavelSelecionado?.let { resp ->
                        onConfirm(descricao, resp, prazo, prioridade, status, obs)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = NavyDeep)
            ) {
                Text("Salvar Tarefa", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = SilverSubtext)
            }
        }
    )
}
