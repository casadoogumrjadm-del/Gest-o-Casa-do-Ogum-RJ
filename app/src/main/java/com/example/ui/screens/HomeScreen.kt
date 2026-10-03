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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Announcement
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.AssignmentTurnedIn
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.PersonPin
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.AniversarianteItem
import com.example.data.ComunicadoEntity
import com.example.data.EventoEntity
import com.example.data.OdunKodunItem
import com.example.data.PagamentoEntity
import com.example.data.PainelFinanceiroResumo
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
import com.example.viewmodel.AppDestination
import com.example.viewmodel.SubScreen

@Composable
fun HomeScreen(
    usuario: PessoaEntity,
    pagamentos: List<PagamentoEntity>,
    resumoFinanceiroOutubro: PainelFinanceiroResumo,
    eventos: List<EventoEntity>,
    aniversariantes: List<AniversarianteItem>,
    odunKodunList: List<OdunKodunItem>,
    comunicados: List<ComunicadoEntity>,
    tarefas: List<TarefaEntity>,
    onNavigateToDestination: (AppDestination) -> Unit,
    onOpenSubScreen: (SubScreen) -> Unit,
    onSelectCalendarMonth: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val isAdmin = usuario.perfilAcesso == "ADMIN"
    val meuPagamentoAtual = pagamentos.firstOrNull {
        it.pessoaId == usuario.id && it.competenciaMes == 10 && it.ano == 2026
    } ?: pagamentos.firstOrNull { it.pessoaId == usuario.id }

    val proximoEvento = eventos.firstOrNull()
    val aniversariantesOutubro = aniversariantes.filter { it.mes == 10 }.ifEmpty { aniversariantes.take(3) }
    val odunOutubro = odunKodunList.filter { it.mes == 10 }.ifEmpty { odunKodunList.take(3) }
    val comunicadosAtivos = comunicados.filter { it.ativo }
    val comunicadosDestaque = comunicadosAtivos.filter { it.destaque }
    val minhasTarefas = if (isAdmin) tarefas else tarefas.filter { it.responsavelPessoaId == usuario.id }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(4.dp))
            // Banner Institucional de Boas-Vindas (sempre saudando pelo Orunkó!)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, GoldPrimary.copy(alpha = 0.65f), RoundedCornerShape(20.dp)),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = NavyCard)
            ) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    Image(
                        painter = painterResource(id = R.drawable.img_hero_crest),
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(145.dp),
                        contentScale = ContentScale.Crop
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(145.dp)
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(
                                        NavyDeep.copy(alpha = 0.92f),
                                        NavyDeep.copy(alpha = 0.72f)
                                    )
                                )
                            )
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.img_logo_oficial),
                            contentDescription = "Logotipo Oficial Casa do Ogum - RJ",
                            modifier = Modifier
                                .size(74.dp)
                                .clip(CircleShape)
                                .border(2.dp, GoldPrimary, CircleShape),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Surface(
                                color = GoldPrimary.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(50),
                                modifier = Modifier.border(1.dp, GoldPrimary, RoundedCornerShape(50))
                            ) {
                                Text(
                                    text = if (isAdmin) "👑 PERFIL ADMINISTRADOR" else "👤 ÁREA DO MEMBRO",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = GoldLight,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Olá, ${usuario.orunko}!",
                                style = MaterialTheme.typography.headlineMedium,
                                color = GoldPrimary,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${usuario.cargo} • Orixá: ${usuario.orixa}",
                                style = MaterialTheme.typography.bodySmall,
                                color = IvoryText
                            )
                        }
                    }
                }
            }
        }

        // ⭐ COMUNICADO EM DESTAQUE NO TOPO
        if (comunicadosDestaque.isNotEmpty()) {
            item {
                val dest = comunicadosDestaque.first()
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.5.dp, GoldPrimary, RoundedCornerShape(16.dp))
                        .clickable { onOpenSubScreen(SubScreen.COMUNICADOS) }
                        .testTag("card_comunicado_destaque"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = NavyElevated)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = "Destaque",
                                tint = GoldPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "⭐ COMUNICADO EM DESTAQUE",
                                style = MaterialTheme.typography.labelMedium,
                                color = GoldPrimary,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.weight(1f))
                            Text(
                                text = dest.data,
                                style = MaterialTheme.typography.labelSmall,
                                color = SilverSubtext
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = dest.titulo,
                            style = MaterialTheme.typography.titleMedium,
                            color = GoldLight,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = dest.texto,
                            style = MaterialTheme.typography.bodySmall,
                            color = IvoryText
                        )
                    }
                }
            }
        }

        // Atalhos Rápidos de Sub-módulos (Meus Dados, Comunicados, Tarefas, Relatórios, Auditoria)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                QuickActionPill(
                    icon = Icons.Default.PersonPin,
                    label = "Meus Dados",
                    onClick = { onOpenSubScreen(SubScreen.MEUS_DADOS) },
                    modifier = Modifier.weight(1f),
                    tag = "quick_meus_dados"
                )
                QuickActionPill(
                    icon = Icons.AutoMirrored.Filled.Announcement,
                    label = "Comunicados",
                    onClick = { onOpenSubScreen(SubScreen.COMUNICADOS) },
                    modifier = Modifier.weight(1f),
                    tag = "quick_comunicados"
                )
                QuickActionPill(
                    icon = Icons.Default.AssignmentTurnedIn,
                    label = "Tarefas",
                    onClick = { onOpenSubScreen(SubScreen.TAREFAS) },
                    modifier = Modifier.weight(1f),
                    tag = "quick_tarefas"
                )
                if (isAdmin) {
                    QuickActionPill(
                        icon = Icons.Default.PieChart,
                        label = "Relatórios",
                        onClick = { onOpenSubScreen(SubScreen.RELATORIOS) },
                        modifier = Modifier.weight(1f),
                        tag = "quick_relatorios"
                    )
                }
            }
        }

        // CARD 1: 💰 MINHA MENSALIDADE + RESUMO FINANCEIRO (SE ADMIN)
        item {
            DashboardSectionCard(
                icon = Icons.Default.AccountBalanceWallet,
                title = "💰 Minha Mensalidade",
                subtitle = "Competência Outubro/2026",
                actionLabel = "Ver Financeiro",
                onClick = { onNavigateToDestination(AppDestination.FINANCEIRO) },
                tag = "home_card_mensalidade"
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (meuPagamentoAtual != null) {
                                DateAndCalendarUtils.formatCurrency(meuPagamentoAtual.valor)
                            } else {
                                "R$ 150,00"
                            },
                            style = MaterialTheme.typography.headlineSmall,
                            color = GoldLight,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (meuPagamentoAtual?.dataPagamento?.isNotBlank() == true) {
                                "Pago em ${meuPagamentoAtual.dataPagamento}"
                            } else {
                                "Vencimento dia 10/10/2026"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = SilverSubtext
                        )
                    }
                    PaymentStatusBadge(status = meuPagamentoAtual?.status ?: "PENDENTE")
                }

                if (isAdmin) {
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 10.dp),
                        color = GoldMuted.copy(alpha = 0.3f)
                    )
                    Text(
                        text = "PAINEL DA CASA — OUTUBRO/2026",
                        style = MaterialTheme.typography.labelSmall,
                        color = GoldPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        MiniMetric("Arrecadado", DateAndCalendarUtils.formatCurrency(resumoFinanceiroOutubro.totalRecebido), StatusPaidGreen)
                        MiniMetric("Pendente", DateAndCalendarUtils.formatCurrency(resumoFinanceiroOutubro.totalPendente), StatusPendingYellow)
                        MiniMetric("Pagos", "${resumoFinanceiroOutubro.qtdPagos} pessoas", GoldLight)
                        MiniMetric("Em atraso", "${resumoFinanceiroOutubro.qtdInadimplentes} pessoas", StatusOverdueRed)
                    }
                }
            }
        }

        // CARD 2: 📅 PRÓXIMO EVENTO
        item {
            DashboardSectionCard(
                icon = Icons.Default.Event,
                title = "📅 Próximo Evento",
                subtitle = "Calendário de Festividades e Obrigações",
                actionLabel = "Ver Eventos",
                onClick = { onNavigateToDestination(AppDestination.CALENDARIOS) },
                tag = "home_card_evento"
            ) {
                if (proximoEvento != null) {
                    Text(
                        text = proximoEvento.nome,
                        style = MaterialTheme.typography.titleMedium,
                        color = GoldPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Data: ${proximoEvento.data} às ${proximoEvento.horario} • ${proximoEvento.local}",
                        style = MaterialTheme.typography.bodySmall,
                        color = IvoryText
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Responsáveis (Orunkó): ${proximoEvento.responsaveis}",
                        style = MaterialTheme.typography.bodySmall,
                        color = SilverSubtext
                    )
                } else {
                    Text(
                        text = "Nenhum evento agendado no momento.",
                        style = MaterialTheme.typography.bodySmall,
                        color = SilverSubtext
                    )
                }
            }
        }

        // CARD 3: 🎂 PRÓXIMOS ANIVERSARIANTES (REGRA DO ORUNKÓ)
        item {
            DashboardSectionCard(
                icon = Icons.Default.Cake,
                title = "🎂 Próximos Aniversariantes",
                subtitle = "Outubro — Exibição exclusiva por Orunkó",
                actionLabel = "Ver Calendário",
                onClick = {
                    onSelectCalendarMonth(10)
                    onNavigateToDestination(AppDestination.CALENDARIOS)
                },
                tag = "home_card_aniversariantes"
            ) {
                OrunkoPrivacyBanner()
                Spacer(modifier = Modifier.height(8.dp))
                aniversariantesOutubro.forEach { item ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                color = GoldPrimary.copy(alpha = 0.18f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = item.diaMesFormatado,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = GoldPrimary,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = item.orunko,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = IvoryText,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "${item.cargo} • ${item.orixa}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = SilverSubtext
                                )
                            }
                        }
                        if (item.idadeNoAno != null) {
                            Text(
                                text = "${item.idadeNoAno} anos",
                                style = MaterialTheme.typography.labelMedium,
                                color = GoldLight
                            )
                        }
                    }
                }
            }
        }

        // CARD 4: 🕯️ PRÓXIMOS ODÚN KODÚN (REGRA DO ORUNKÓ)
        item {
            DashboardSectionCard(
                icon = Icons.Default.LocalFireDepartment,
                title = "🕯️ Próximos Odún Kodún",
                subtitle = "Anos completos de Iniciação — Outubro/2026",
                actionLabel = "Ver Odún Kodún",
                onClick = {
                    onSelectCalendarMonth(10)
                    onNavigateToDestination(AppDestination.CALENDARIOS)
                },
                tag = "home_card_odun_kodun"
            ) {
                odunOutubro.forEach { odun ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 5.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                color = NavyElevated,
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.border(1.dp, GoldPrimary.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                            ) {
                                Text(
                                    text = odun.diaMesFormatado,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = GoldLight,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "${odun.orunko} — ${odun.anosCompletos} anos",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = GoldPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = odun.marcoObrigacao ?: "Iniciação em ${odun.dataIniciacao} (${odun.orixa})",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = SilverSubtext
                                )
                            }
                        }
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = GoldPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        // CARD 5: 📢 COMUNICADOS RECENTES
        item {
            DashboardSectionCard(
                icon = Icons.AutoMirrored.Filled.Announcement,
                title = "📢 Comunicados da Casa",
                subtitle = "${comunicadosAtivos.size} comunicados ativos",
                actionLabel = "Abrir Mural",
                onClick = { onOpenSubScreen(SubScreen.COMUNICADOS) },
                tag = "home_card_comunicados"
            ) {
                comunicadosAtivos.take(2).forEach { com ->
                    Column(modifier = Modifier.padding(vertical = 4.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (com.destaque) {
                                Text(
                                    text = "⭐ ",
                                    style = MaterialTheme.typography.labelMedium
                                )
                            }
                            Text(
                                text = com.titulo,
                                style = MaterialTheme.typography.bodyMedium,
                                color = GoldLight,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Text(
                            text = "${com.data} • Público: ${com.publicoAlvo}",
                            style = MaterialTheme.typography.bodySmall,
                            color = SilverSubtext
                        )
                    }
                }
            }
        }

        // CARD 6: ✅ MINHAS TAREFAS E RESPONSABILIDADES
        item {
            DashboardSectionCard(
                icon = Icons.Default.AssignmentTurnedIn,
                title = if (isAdmin) "✅ Tarefas e Responsabilidades" else "✅ Minhas Tarefas",
                subtitle = "${minhasTarefas.count { it.status != "CONCLUIDA" }} em aberto",
                actionLabel = "Gerenciar",
                onClick = { onOpenSubScreen(SubScreen.TAREFAS) },
                tag = "home_card_tarefas"
            ) {
                if (minhasTarefas.isEmpty()) {
                    Text(
                        text = "Nenhuma tarefa pendente para seu Orunkó.",
                        style = MaterialTheme.typography.bodySmall,
                        color = SilverSubtext
                    )
                } else {
                    minhasTarefas.take(3).forEach { tar ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = tar.descricao,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = IvoryText,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = "Resp: ${tar.responsavelOrunko} • Prazo: ${tar.prazo} • Prioridade: ${tar.prioridade}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = SilverSubtext
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            TaskStatusChip(status = tar.status)
                        }
                    }
                }
            }
        }

        if (isAdmin) {
            item {
                DashboardSectionCard(
                    icon = Icons.Default.History,
                    title = "🔐 Histórico Administrativo",
                    subtitle = "Registro de auditoria e segurança da Casa",
                    actionLabel = "Ver Auditoria",
                    onClick = { onOpenSubScreen(SubScreen.HISTORICO_AUDITORIA) },
                    tag = "home_card_historico"
                ) {
                    Text(
                        text = "Todas as alterações de cadastros, lançamentos de mensalidades e senhas temporárias são auditadas automaticamente.",
                        style = MaterialTheme.typography.bodySmall,
                        color = SilverSubtext
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
fun QuickActionPill(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    tag: String = ""
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .border(1.dp, GoldMuted.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
            .testTag(tag),
        color = NavyCard,
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = GoldPrimary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = GoldLight,
                maxLines = 1
            )
        }
    }
}

@Composable
fun DashboardSectionCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    actionLabel: String,
    onClick: () -> Unit,
    tag: String,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, GoldMuted.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .testTag(tag),
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
                    Surface(
                        color = NavyElevated,
                        shape = CircleShape,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = GoldPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleMedium,
                            color = GoldLight,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = SilverSubtext
                        )
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = actionLabel,
                        style = MaterialTheme.typography.labelMedium,
                        color = GoldPrimary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = actionLabel,
                        tint = GoldPrimary,
                        modifier = Modifier.size(15.dp)
                    )
                }
            }
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 10.dp),
                color = GoldMuted.copy(alpha = 0.25f)
            )
            content()
        }
    }
}

@Composable
fun OrunkoPrivacyBanner() {
    Surface(
        color = NavyElevated,
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, GoldPrimary.copy(alpha = 0.45f), RoundedCornerShape(10.dp))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Shield,
                contentDescription = "Regra do Orunkó",
                tint = GoldPrimary,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Regra do Orunkó ativa: calendários coletivos exibem exclusivamente o Orunkó (nunca o nome civil).",
                style = MaterialTheme.typography.bodySmall,
                color = GoldLight
            )
        }
    }
}

@Composable
fun PaymentStatusBadge(status: String) {
    val (label, textColor, bgColor) = when (status) {
        "PAGO" -> Triple("🟢 PAGO", StatusPaidGreen, StatusPaidGreen.copy(alpha = 0.18f))
        "EM_ATRASO" -> Triple("🔴 EM ATRASO", StatusOverdueRed, StatusOverdueRed.copy(alpha = 0.18f))
        else -> Triple("🟡 PENDENTE", StatusPendingYellow, StatusPendingYellow.copy(alpha = 0.18f))
    }
    Surface(
        color = bgColor,
        shape = RoundedCornerShape(50),
        modifier = Modifier.border(1.dp, textColor.copy(alpha = 0.6f), RoundedCornerShape(50))
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = textColor,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
        )
    }
}

@Composable
fun TaskStatusChip(status: String) {
    val (label, color) = when (status) {
        "CONCLUIDA" -> "🟢 Concluída" to StatusPaidGreen
        "EM_ANDAMENTO" -> "🔵 Em andamento" to StatusInProgressBlue
        else -> "⚪ Pendente" to SilverSubtext
    }
    Surface(
        color = color.copy(alpha = 0.16f),
        shape = RoundedCornerShape(50),
        modifier = Modifier.border(1.dp, color.copy(alpha = 0.5f), RoundedCornerShape(50))
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = color,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}

@Composable
private fun MiniMetric(label: String, value: String, valueColor: Color) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = SilverSubtext
        )
        Text(
            text = value,
            style = MaterialTheme.typography.labelLarge,
            color = valueColor,
            fontWeight = FontWeight.Bold
        )
    }
}
