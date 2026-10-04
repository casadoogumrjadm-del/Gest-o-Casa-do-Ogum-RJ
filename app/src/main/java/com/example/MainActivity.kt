package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.DashboardCustomize
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.CasaOgumDatabase
import com.example.data.CasaOgumRepository
import com.example.data.FirestoreConnectionState
import com.example.data.FirestoreSyncService
import com.example.ui.components.MemberAvatar
import com.example.ui.screens.CalendarsAndEventsScreen
import com.example.ui.screens.FinanceScreen
import com.example.ui.screens.FirstAccessPasswordChangeScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.LoginScreen
import com.example.ui.screens.ManagementAndReportsScreen
import com.example.ui.screens.MembersScreen
import com.example.ui.screens.WebBrowserScreen
import com.example.ui.theme.GoldLight
import com.example.ui.theme.GoldMuted
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.IvoryText
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.NavyCard
import com.example.ui.theme.NavyDeep
import com.example.ui.theme.NavyElevated
import com.example.ui.theme.NavyPrimary
import com.example.ui.theme.NavySurface
import com.example.ui.theme.SilverSubtext
import com.example.util.ReportExporter
import com.example.viewmodel.AppDestination
import com.example.viewmodel.CasaOgumViewModel
import com.example.viewmodel.SubScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val database = CasaOgumDatabase.getInstance(applicationContext)
        val firestoreService = FirestoreSyncService(applicationContext, database.dao())
        val repository = CasaOgumRepository(database.dao(), firestoreService)

        setContent {
            MyApplicationTheme {
                val vm: CasaOgumViewModel = viewModel(
                    factory = CasaOgumViewModel.Factory(repository)
                )
                CasaDoOgumApp(viewModel = vm)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CasaDoOgumApp(viewModel: CasaOgumViewModel) {
    val context = LocalContext.current
    val usuarioLogado by viewModel.usuarioLogado.collectAsStateWithLifecycle()
    val erroLogin by viewModel.erroLogin.collectAsStateWithLifecycle()
    val mensagemFeedback by viewModel.mensagemFeedback.collectAsStateWithLifecycle()
    val credencialModal by viewModel.credencialGeradaModal.collectAsStateWithLifecycle()
    val relatorioExportado by viewModel.ultimoRelatorioExportado.collectAsStateWithLifecycle()

    val pessoas by viewModel.pessoas.collectAsStateWithLifecycle()
    val pagamentos by viewModel.pagamentos.collectAsStateWithLifecycle()
    val eventos by viewModel.eventos.collectAsStateWithLifecycle()
    val comunicados by viewModel.comunicados.collectAsStateWithLifecycle()
    val tarefas by viewModel.tarefas.collectAsStateWithLifecycle()
    val historico by viewModel.historico.collectAsStateWithLifecycle()

    val currentDestination by viewModel.currentDestination.collectAsStateWithLifecycle()
    val currentSubScreen by viewModel.currentSubScreen.collectAsStateWithLifecycle()
    val mesFinanceiro by viewModel.mesFinanceiroSelecionado.collectAsStateWithLifecycle()
    val anoFinanceiro by viewModel.anoFinanceiroSelecionado.collectAsStateWithLifecycle()
    val mesCalendario by viewModel.mesCalendarioSelecionado.collectAsStateWithLifecycle()
    val firestoreStatus by viewModel.firestoreStatus.collectAsStateWithLifecycle()
    val modoNavegadorWeb by viewModel.modoNavegadorWebAtivo.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(mensagemFeedback) {
        mensagemFeedback?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.limparFeedback()
        }
    }

    // 0. Modo Navegador Web (Chrome, Safari, Edge, Firefox, Internet Explorer)
    if (modoNavegadorWeb) {
        Scaffold(
            contentWindowInsets = WindowInsets.safeDrawing,
            snackbarHost = { SnackbarHost(snackbarHostState) }
        ) { innerPadding ->
            WebBrowserScreen(
                pessoas = pessoas,
                pagamentos = pagamentos,
                eventos = eventos,
                comunicados = comunicados,
                tarefas = tarefas,
                historico = historico,
                onExportWebFile = { viewModel.exportarVersaoWebNavegador(context) },
                onSwitchToNativeApp = { viewModel.definirModoNavegadorWeb(false) },
                modifier = Modifier.padding(innerPadding)
            )
        }
        relatorioExportado?.let { res ->
            AlertDialog(
                onDismissRequest = { viewModel.fecharModalRelatorio() },
                containerColor = NavyCard,
                title = {
                    Text(
                        text = "Sistema Web (.HTML) Exportado",
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
                        Text(text = res.message, style = MaterialTheme.typography.bodySmall, color = GoldLight)
                        Surface(
                            color = NavySurface,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, GoldMuted.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                        ) {
                            Text(
                                text = res.previewContent,
                                style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                                color = IvoryText,
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = { viewModel.fecharModalRelatorio() },
                        colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = NavyDeep)
                    ) {
                        Text("Entendido", fontWeight = FontWeight.Bold)
                    }
                }
            )
        }
        return
    }

    // 1. Fluxo de Login (CPF + Senha)
    val user = usuarioLogado
    if (user == null) {
        Scaffold(
            contentWindowInsets = WindowInsets.safeDrawing,
            snackbarHost = { SnackbarHost(snackbarHostState) }
        ) { innerPadding ->
            LoginScreen(
                erroLogin = erroLogin,
                onLogin = { cpf, senha -> viewModel.realizarLogin(cpf, senha) },
                onOpenWebBrowser = { viewModel.definirModoNavegadorWeb(true) },
                onExportWebHtml = { viewModel.exportarVersaoWebNavegador(context) },
                modifier = Modifier.padding(innerPadding)
            )
        }
        return
    }

    // 2. Fluxo Obrigatório de Primeiro Acesso (Troca de Senha Temporária)
    if (user.primeiroAcesso) {
        BackHandler {
            viewModel.realizarLogout()
        }
        Scaffold(
            contentWindowInsets = WindowInsets.safeDrawing,
            snackbarHost = { SnackbarHost(snackbarHostState) }
        ) { innerPadding ->
            FirstAccessPasswordChangeScreen(
                usuario = user,
                erro = erroLogin,
                onConfirmNewPassword = { nova, conf ->
                    viewModel.concluirTrocaSenhaPrimeiroAcesso(nova, conf)
                },
                onCancelLogout = { viewModel.realizarLogout() },
                modifier = Modifier.padding(innerPadding)
            )
        }
        return
    }

    // BackHandler para SubScreens e abas secundárias
    BackHandler(enabled = currentSubScreen != SubScreen.NONE || currentDestination != AppDestination.HOME) {
        if (currentSubScreen != SubScreen.NONE) {
            viewModel.closeSubScreen()
        } else {
            viewModel.navigateTo(AppDestination.HOME)
        }
    }

    val isAdmin = user.perfilAcesso == "ADMIN"
    val aniversariantes = remember(pessoas) { viewModel.construirCalendarioAniversariantes(pessoas) }
    val odunKodunList = remember(pessoas) { viewModel.construirCalendarioOdunKodun(pessoas) }
    val resumoOutubro = remember(pagamentos) { viewModel.calcularPainelFinanceiro(pagamentos, 10, 2026) }
    val resumoMesSelecionado = remember(pagamentos, mesFinanceiro, anoFinanceiro) {
        viewModel.calcularPainelFinanceiro(pagamentos, mesFinanceiro, anoFinanceiro)
    }

    val navItems = listOf(
        Triple(AppDestination.HOME, "Início", Icons.Default.Home),
        Triple(AppDestination.MEMBROS, if (isAdmin) "Membros" else "Meus Dados", Icons.Default.Groups),
        Triple(AppDestination.FINANCEIRO, "Financeiro", Icons.Default.AccountBalanceWallet),
        Triple(AppDestination.CALENDARIOS, "Calendários", Icons.Default.CalendarMonth),
        Triple(AppDestination.GESTAO_MAIS, if (isAdmin) "Gestão" else "Avisos/Tarefas", Icons.Default.DashboardCustomize)
    )

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isExpandedScreen = maxWidth >= 700.dp

        Scaffold(
            contentWindowInsets = WindowInsets.safeDrawing,
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                TopAppBar(
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = NavyDeep,
                        titleContentColor = GoldPrimary
                    ),
                    navigationIcon = {
                        if (currentSubScreen != SubScreen.NONE) {
                            IconButton(
                                onClick = { viewModel.closeSubScreen() },
                                modifier = Modifier.testTag("back_subscreen_button")
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Voltar",
                                    tint = GoldPrimary
                                )
                            }
                        } else {
                            Image(
                                painter = painterResource(id = R.drawable.img_logo_oficial),
                                contentDescription = "Brasão Casa do Ogum - RJ",
                                modifier = Modifier
                                    .padding(start = 12.dp, end = 4.dp)
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .border(1.5.dp, GoldPrimary, CircleShape)
                            )
                        }
                    },
                    title = {
                        Column {
                            Text(
                                text = "CASA DO OGUM - RJ",
                                style = MaterialTheme.typography.titleMedium,
                                color = GoldPrimary,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${user.orunko} • ${if (isAdmin) "👑 Administrador" else "👤 Membro"}",
                                style = MaterialTheme.typography.labelSmall,
                                color = GoldLight
                            )
                        }
                    },
                    actions = {
                        MemberAvatar(
                            fotoBase64 = user.fotoBase64,
                            nameOrOrunko = user.orunko,
                            size = 34.dp,
                            onClick = { viewModel.openSubScreen(SubScreen.MEUS_DADOS) }
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        IconButton(
                            onClick = { viewModel.definirModoNavegadorWeb(true) },
                            modifier = Modifier.testTag("open_web_browser_mode_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Language,
                                contentDescription = "Abrir Versão Navegador Web (Chrome, Safari, Edge, IE)",
                                tint = GoldPrimary
                            )
                        }
                        IconButton(
                            onClick = { viewModel.sincronizarComFirestore() },
                            modifier = Modifier.testTag("firestore_sync_button")
                        ) {
                            Icon(
                                imageVector = if (firestoreStatus.state == FirestoreConnectionState.CONNECTED) {
                                    Icons.Default.CloudDone
                                } else {
                                    Icons.Default.CloudSync
                                },
                                contentDescription = "Sincronizar Membros e Mensalidades com Firebase Firestore",
                                tint = GoldPrimary
                            )
                        }
                        IconButton(
                            onClick = { viewModel.realizarLogout() },
                            modifier = Modifier.testTag("logout_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Logout,
                                contentDescription = "Sair da conta",
                                tint = GoldLight
                            )
                        }
                    }
                )
            },
            bottomBar = {
                if (!isExpandedScreen) {
                    NavigationBar(
                        containerColor = NavyDeep,
                        contentColor = GoldPrimary
                    ) {
                        navItems.forEach { (dest, label, icon) ->
                            val selected = currentSubScreen == SubScreen.NONE && currentDestination == dest
                            NavigationBarItem(
                                selected = selected,
                                onClick = { viewModel.navigateTo(dest) },
                                icon = { Icon(icon, contentDescription = label) },
                                label = { Text(label, style = MaterialTheme.typography.labelSmall) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = NavyDeep,
                                    selectedTextColor = GoldPrimary,
                                    indicatorColor = GoldPrimary,
                                    unselectedIconColor = SilverSubtext,
                                    unselectedTextColor = SilverSubtext
                                ),
                                modifier = Modifier.testTag("nav_${dest.name.lowercase()}")
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .background(NavyPrimary)
                    .padding(innerPadding)
            ) {
                if (isExpandedScreen) {
                    NavigationRail(
                        containerColor = NavyDeep,
                        contentColor = GoldPrimary,
                        header = {
                            Spacer(modifier = Modifier.height(8.dp))
                            Image(
                                painter = painterResource(id = R.drawable.img_logo_oficial),
                                contentDescription = "Brasão Oficial Casa do Ogum - RJ",
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(CircleShape)
                                    .border(1.5.dp, GoldPrimary, CircleShape)
                            )
                        },
                        modifier = Modifier.fillMaxHeight()
                    ) {
                        Spacer(modifier = Modifier.height(12.dp))
                        navItems.forEach { (dest, label, icon) ->
                            val selected = currentSubScreen == SubScreen.NONE && currentDestination == dest
                            NavigationRailItem(
                                selected = selected,
                                onClick = { viewModel.navigateTo(dest) },
                                icon = { Icon(icon, contentDescription = label) },
                                label = { Text(label) },
                                colors = NavigationRailItemDefaults.colors(
                                    selectedIconColor = NavyDeep,
                                    selectedTextColor = GoldPrimary,
                                    indicatorColor = GoldPrimary,
                                    unselectedIconColor = SilverSubtext,
                                    unselectedTextColor = SilverSubtext
                                )
                            )
                        }
                    }
                }

                Box(modifier = Modifier.fillMaxSize()) {
                    // Roteamento entre SubScreen ou Destination principal
                    when (currentSubScreen) {
                        SubScreen.MEUS_DADOS -> {
                            MembersScreen(
                                usuarioLogado = user,
                                pessoas = pessoas,
                                firestoreStatus = firestoreStatus,
                                somenteMeusDados = true,
                                onCarregarMembrosFirestore = viewModel::carregarMembrosDoFirestore,
                                onSincronizarFirestore = viewModel::sincronizarComFirestore,
                                onSalvarPessoa = viewModel::salvarPessoa,
                                onAtualizarFotoMembro = viewModel::atualizarFotoMembro,
                                onAlternarAtivo = viewModel::alternarAtivoInativo,
                                onResetarSenhaTemporaria = viewModel::resetarSenhaTemporaria
                            )
                        }
                        SubScreen.COMUNICADOS -> {
                            ManagementAndReportsScreen(
                                usuarioLogado = user,
                                abaInicial = 0,
                                pessoas = pessoas,
                                pagamentos = pagamentos,
                                comunicados = comunicados,
                                tarefas = tarefas,
                                historico = historico,
                                onSalvarComunicado = viewModel::salvarComunicado,
                                onAlternarStatusComunicado = viewModel::alternarStatusComunicado,
                                onSalvarTarefa = viewModel::salvarTarefa,
                                onAlterarStatusTarefa = viewModel::alterarStatusTarefa,
                                onExportarFinanceiro = { pdf, m, a ->
                                    viewModel.exportarRelatorioFinanceiro(context, pdf, m, a, pagamentos, pessoas)
                                },
                                onExportarCadastral = { pdf ->
                                    viewModel.exportarRelatorioCadastral(context, pdf, pessoas)
                                }
                            )
                        }
                        SubScreen.TAREFAS -> {
                            ManagementAndReportsScreen(
                                usuarioLogado = user,
                                abaInicial = 1,
                                pessoas = pessoas,
                                pagamentos = pagamentos,
                                comunicados = comunicados,
                                tarefas = tarefas,
                                historico = historico,
                                onSalvarComunicado = viewModel::salvarComunicado,
                                onAlternarStatusComunicado = viewModel::alternarStatusComunicado,
                                onSalvarTarefa = viewModel::salvarTarefa,
                                onAlterarStatusTarefa = viewModel::alterarStatusTarefa,
                                onExportarFinanceiro = { pdf, m, a ->
                                    viewModel.exportarRelatorioFinanceiro(context, pdf, m, a, pagamentos, pessoas)
                                },
                                onExportarCadastral = { pdf ->
                                    viewModel.exportarRelatorioCadastral(context, pdf, pessoas)
                                }
                            )
                        }
                        SubScreen.RELATORIOS -> {
                            ManagementAndReportsScreen(
                                usuarioLogado = user,
                                abaInicial = 2,
                                pessoas = pessoas,
                                pagamentos = pagamentos,
                                comunicados = comunicados,
                                tarefas = tarefas,
                                historico = historico,
                                onSalvarComunicado = viewModel::salvarComunicado,
                                onAlternarStatusComunicado = viewModel::alternarStatusComunicado,
                                onSalvarTarefa = viewModel::salvarTarefa,
                                onAlterarStatusTarefa = viewModel::alterarStatusTarefa,
                                onExportarFinanceiro = { pdf, m, a ->
                                    viewModel.exportarRelatorioFinanceiro(context, pdf, m, a, pagamentos, pessoas)
                                },
                                onExportarCadastral = { pdf ->
                                    viewModel.exportarRelatorioCadastral(context, pdf, pessoas)
                                }
                            )
                        }
                        SubScreen.HISTORICO_AUDITORIA -> {
                            ManagementAndReportsScreen(
                                usuarioLogado = user,
                                abaInicial = 3,
                                pessoas = pessoas,
                                pagamentos = pagamentos,
                                comunicados = comunicados,
                                tarefas = tarefas,
                                historico = historico,
                                onSalvarComunicado = viewModel::salvarComunicado,
                                onAlternarStatusComunicado = viewModel::alternarStatusComunicado,
                                onSalvarTarefa = viewModel::salvarTarefa,
                                onAlterarStatusTarefa = viewModel::alterarStatusTarefa,
                                onExportarFinanceiro = { pdf, m, a ->
                                    viewModel.exportarRelatorioFinanceiro(context, pdf, m, a, pagamentos, pessoas)
                                },
                                onExportarCadastral = { pdf ->
                                    viewModel.exportarRelatorioCadastral(context, pdf, pessoas)
                                }
                            )
                        }
                        SubScreen.NONE -> {
                            when (currentDestination) {
                                AppDestination.HOME -> {
                                    HomeScreen(
                                        usuario = user,
                                        pessoas = pessoas,
                                        pagamentos = pagamentos,
                                        resumoFinanceiroOutubro = resumoOutubro,
                                        eventos = eventos,
                                        aniversariantes = aniversariantes,
                                        odunKodunList = odunKodunList,
                                        comunicados = comunicados,
                                        tarefas = tarefas,
                                        onNavigateToDestination = viewModel::navigateTo,
                                        onOpenSubScreen = viewModel::openSubScreen,
                                        onSelectCalendarMonth = viewModel::selecionarMesCalendario,
                                        onAtualizarFotoMembro = viewModel::atualizarFotoMembro
                                    )
                                }
                                AppDestination.MEMBROS -> {
                                    MembersScreen(
                                        usuarioLogado = user,
                                        pessoas = pessoas,
                                        firestoreStatus = firestoreStatus,
                                        somenteMeusDados = false,
                                        onCarregarMembrosFirestore = viewModel::carregarMembrosDoFirestore,
                                        onSincronizarFirestore = viewModel::sincronizarComFirestore,
                                        onSalvarPessoa = viewModel::salvarPessoa,
                                        onAtualizarFotoMembro = viewModel::atualizarFotoMembro,
                                        onAlternarAtivo = viewModel::alternarAtivoInativo,
                                        onResetarSenhaTemporaria = viewModel::resetarSenhaTemporaria
                                    )
                                }
                                AppDestination.FINANCEIRO -> {
                                    FinanceScreen(
                                        usuarioLogado = user,
                                        pessoas = pessoas,
                                        pagamentos = pagamentos,
                                        mesSelecionado = mesFinanceiro,
                                        anoSelecionado = anoFinanceiro,
                                        resumoMes = resumoMesSelecionado,
                                        onSelectMes = viewModel::selecionarMesFinanceiro,
                                        onSalvarPagamento = viewModel::salvarPagamento,
                                        onExportarFinanceiro = { pdf, m, a ->
                                            viewModel.exportarRelatorioFinanceiro(context, pdf, m, a, pagamentos, pessoas)
                                        }
                                    )
                                }
                                AppDestination.CALENDARIOS -> {
                                    CalendarsAndEventsScreen(
                                        usuarioLogado = user,
                                        pessoas = pessoas,
                                        eventos = eventos,
                                        aniversariantes = aniversariantes,
                                        odunKodunList = odunKodunList,
                                        mesSelecionado = mesCalendario,
                                        onSelectMes = viewModel::selecionarMesCalendario,
                                        onSalvarEvento = viewModel::salvarEvento,
                                        onExcluirEvento = viewModel::excluirEvento
                                    )
                                }
                                AppDestination.GESTAO_MAIS -> {
                                    ManagementAndReportsScreen(
                                        usuarioLogado = user,
                                        abaInicial = 0,
                                        pessoas = pessoas,
                                        pagamentos = pagamentos,
                                        comunicados = comunicados,
                                        tarefas = tarefas,
                                        historico = historico,
                                        onSalvarComunicado = viewModel::salvarComunicado,
                                        onAlternarStatusComunicado = viewModel::alternarStatusComunicado,
                                        onSalvarTarefa = viewModel::salvarTarefa,
                                        onAlterarStatusTarefa = viewModel::alterarStatusTarefa,
                                        onExportarFinanceiro = { pdf, m, a ->
                                            viewModel.exportarRelatorioFinanceiro(context, pdf, m, a, pagamentos, pessoas)
                                        },
                                        onExportarCadastral = { pdf ->
                                            viewModel.exportarRelatorioCadastral(context, pdf, pessoas)
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Modal de Credencial Temporária gerada pelo Administrador
    credencialModal?.let { (orunko, cpfFormatado, senhaTemp) ->
        AlertDialog(
            onDismissRequest = { viewModel.fecharModalCredencial() },
            containerColor = NavyCard,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Key, contentDescription = null, tint = GoldPrimary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Credencial de 1º Acesso Gerada",
                        style = MaterialTheme.typography.titleLarge,
                        color = GoldPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Forneça os dados abaixo ao membro para que ele realize seu primeiro acesso no sistema:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = IvoryText
                    )
                    Surface(
                        color = NavyElevated,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, GoldPrimary, RoundedCornerShape(12.dp))
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text("Orunkó: $orunko", style = MaterialTheme.typography.titleMedium, color = GoldPrimary, fontWeight = FontWeight.Bold)
                            Text("Login (CPF): $cpfFormatado", style = MaterialTheme.typography.bodyMedium, color = IvoryText)
                            Text("Senha Temporária: $senhaTemp", style = MaterialTheme.typography.titleMedium, color = GoldLight, fontWeight = FontWeight.Bold)
                        }
                    }
                    Text(
                        text = "No primeiro login, o sistema obrigará a criação de uma nova senha pessoal e esta senha temporária deixará de ser válida.",
                        style = MaterialTheme.typography.bodySmall,
                        color = SilverSubtext
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.fecharModalCredencial() },
                    colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = NavyDeep)
                ) {
                    Text("Entendido", fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    // Modal de Visualização e Compartilhamento de Relatório Exportado (PDF / Excel)
    relatorioExportado?.let { res ->
        AlertDialog(
            onDismissRequest = { viewModel.fecharModalRelatorio() },
            containerColor = NavyCard,
            title = {
                Text(
                    text = "Relatório Exportado com Sucesso",
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
                    Text(
                        text = res.message,
                        style = MaterialTheme.typography.bodySmall,
                        color = GoldLight
                    )
                    Text(
                        text = "Arquivo salvo: ${res.fileName}",
                        style = MaterialTheme.typography.labelSmall,
                        color = SilverSubtext
                    )
                    Surface(
                        color = NavySurface,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, GoldMuted.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                    ) {
                        Text(
                            text = res.previewContent,
                            style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                            color = IvoryText,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }
            },
            confirmButton = {
                OutlinedButton(
                    onClick = {
                        ReportExporter.shareTextReport(context, res.fileName, res.previewContent)
                    }
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, tint = GoldPrimary, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Compartilhar", color = GoldPrimary)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.fecharModalRelatorio() }) {
                    Text("Fechar", color = SilverSubtext)
                }
            }
        )
    }
}
