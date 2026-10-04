package com.example.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.AniversarianteItem
import com.example.data.CasaOgumRepository
import com.example.data.ComunicadoEntity
import com.example.data.ConfiguracaoEntity
import com.example.data.EventoEntity
import com.example.data.FirestoreStatusInfo
import com.example.data.HistoricoAdminEntity
import com.example.data.OdunKodunItem
import com.example.data.PagamentoEntity
import com.example.data.PainelFinanceiroResumo
import com.example.data.PessoaEntity
import com.example.data.TarefaEntity
import com.example.util.DateAndCalendarUtils
import com.example.util.ExportResult
import com.example.util.ReportExporter
import com.example.util.SecurityUtils
import com.example.util.WebAppExporter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class AppDestination {
    HOME,
    MEMBROS,
    FINANCEIRO,
    CALENDARIOS,
    GESTAO_MAIS
}

enum class SubScreen {
    NONE,
    COMUNICADOS,
    TAREFAS,
    RELATORIOS,
    HISTORICO_AUDITORIA,
    MEUS_DADOS
}

class CasaOgumViewModel(
    private val repository: CasaOgumRepository
) : ViewModel() {

    val pessoas: StateFlow<List<PessoaEntity>> = repository.allPessoas
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val pagamentos: StateFlow<List<PagamentoEntity>> = repository.allPagamentos
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val eventos: StateFlow<List<EventoEntity>> = repository.allEventos
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val comunicados: StateFlow<List<ComunicadoEntity>> = repository.allComunicados
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val tarefas: StateFlow<List<TarefaEntity>> = repository.allTarefas
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val historico: StateFlow<List<HistoricoAdminEntity>> = repository.allHistorico
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val configuracoes: StateFlow<List<ConfiguracaoEntity>> = repository.allConfiguracoes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val firestoreStatus: StateFlow<FirestoreStatusInfo> = repository.firestoreStatus

    fun sincronizarComFirestore() {
        val msg = repository.sincronizarMembrosEMensalidadesComFirestore(
            pessoas = pessoas.value,
            pagamentos = pagamentos.value
        )
        _mensagemFeedback.value = msg
    }

    fun carregarMembrosDoFirestore() {
        repository.carregarMembrosDoFirestore()
    }

    private val _usuarioLogado = MutableStateFlow<PessoaEntity?>(null)
    val usuarioLogado: StateFlow<PessoaEntity?> = _usuarioLogado.asStateFlow()

    private val _erroLogin = MutableStateFlow<String?>(null)
    val erroLogin: StateFlow<String?> = _erroLogin.asStateFlow()

    private val _mensagemFeedback = MutableStateFlow<String?>(null)
    val mensagemFeedback: StateFlow<String?> = _mensagemFeedback.asStateFlow()

    private val _credencialGeradaModal = MutableStateFlow<Triple<String, String, String>?>(null) // (Orunkó, CPF, SenhaTemp)
    val credencialGeradaModal: StateFlow<Triple<String, String, String>?> = _credencialGeradaModal.asStateFlow()

    private val _ultimoRelatorioExportado = MutableStateFlow<ExportResult?>(null)
    val ultimoRelatorioExportado: StateFlow<ExportResult?> = _ultimoRelatorioExportado.asStateFlow()

    private val _modoNavegadorWebAtivo = MutableStateFlow(
        !android.os.Build.FINGERPRINT.contains("robolectric", ignoreCase = true)
    )
    val modoNavegadorWebAtivo: StateFlow<Boolean> = _modoNavegadorWebAtivo.asStateFlow()

    fun definirModoNavegadorWeb(ativo: Boolean) {
        _modoNavegadorWebAtivo.value = ativo
    }

    fun exportarVersaoWebNavegador(context: Context) {
        val res = WebAppExporter.exportAndShareWebAppFile(
            context = context,
            pessoas = pessoas.value,
            pagamentos = pagamentos.value,
            eventos = eventos.value,
            comunicados = comunicados.value,
            tarefas = tarefas.value,
            historico = historico.value
        )
        _ultimoRelatorioExportado.value = res
        _mensagemFeedback.value = res.message
    }

    private val _currentDestination = MutableStateFlow(AppDestination.HOME)
    val currentDestination: StateFlow<AppDestination> = _currentDestination.asStateFlow()

    private val _currentSubScreen = MutableStateFlow(SubScreen.NONE)
    val currentSubScreen: StateFlow<SubScreen> = _currentSubScreen.asStateFlow()

    private val _mesFinanceiroSelecionado = MutableStateFlow(10) // Outubro/2026 default
    val mesFinanceiroSelecionado: StateFlow<Int> = _mesFinanceiroSelecionado.asStateFlow()

    private val _anoFinanceiroSelecionado = MutableStateFlow(2026)
    val anoFinanceiroSelecionado: StateFlow<Int> = _anoFinanceiroSelecionado.asStateFlow()

    private val _mesCalendarioSelecionado = MutableStateFlow(10) // Outubro default
    val mesCalendarioSelecionado: StateFlow<Int> = _mesCalendarioSelecionado.asStateFlow()

    init {
        viewModelScope.launch {
            repository.ensureSeeded()
        }
    }

    fun navigateTo(dest: AppDestination) {
        _currentSubScreen.value = SubScreen.NONE
        _currentDestination.value = dest
    }

    fun openSubScreen(sub: SubScreen) {
        _currentSubScreen.value = sub
    }

    fun closeSubScreen() {
        _currentSubScreen.value = SubScreen.NONE
    }

    fun selecionarMesFinanceiro(mes: Int, ano: Int = 2026) {
        _mesFinanceiroSelecionado.value = mes.coerceIn(1, 12)
        _anoFinanceiroSelecionado.value = ano
    }

    fun selecionarMesCalendario(mes: Int) {
        _mesCalendarioSelecionado.value = mes.coerceIn(0, 12) // 0 = Visão Anual Completa
    }

    fun limparFeedback() {
        _mensagemFeedback.value = null
    }

    fun fecharModalCredencial() {
        _credencialGeradaModal.value = null
    }

    fun fecharModalRelatorio() {
        _ultimoRelatorioExportado.value = null
    }

    fun realizarLogin(cpfInput: String, senhaInput: String) {
        viewModelScope.launch {
            _erroLogin.value = null
            val cpfLimpo = SecurityUtils.cleanCpf(cpfInput)
            if (cpfLimpo.length != 11) {
                _erroLogin.value = "Informe um CPF válido com 11 dígitos."
                return@launch
            }
            val senhaLimpa = SecurityUtils.cleanPin4(senhaInput)
            if (!SecurityUtils.isValidPin4(senhaLimpa)) {
                _erroLogin.value = "Informe sua senha numérica de 4 dígitos."
                return@launch
            }
            val pessoa = repository.findByCpf(cpfLimpo)
            if (pessoa == null) {
                _erroLogin.value = "CPF não encontrado no cadastro da Casa do Ogum - RJ."
                return@launch
            }
            if (!pessoa.ativo) {
                _erroLogin.value = "Este cadastro encontra-se INATIVO. Procure a Administração da Casa."
                return@launch
            }
            if (!SecurityUtils.verifyPassword(senhaLimpa, pessoa.senhaHash)) {
                _erroLogin.value = "Senha de 4 dígitos incorreta. Verifique os números informados."
                return@launch
            }

            if (!pessoa.primeiroAcesso) {
                repository.registrarAcessoLogin(pessoa)
            }
            val atualizado = repository.findById(pessoa.id) ?: pessoa
            _usuarioLogado.value = atualizado
            _currentDestination.value = AppDestination.HOME
            _currentSubScreen.value = SubScreen.NONE
        }
    }

    fun concluirTrocaSenhaPrimeiroAcesso(novaSenha: String, confirmacaoSenha: String) {
        val atual = _usuarioLogado.value ?: return
        val pinNovo = SecurityUtils.cleanPin4(novaSenha)
        val pinConf = SecurityUtils.cleanPin4(confirmacaoSenha)
        if (!SecurityUtils.isValidPin4(pinNovo)) {
            _erroLogin.value = "A nova senha pessoal deve conter exatamente 4 números."
            return
        }
        if (pinNovo != pinConf) {
            _erroLogin.value = "A confirmação dos 4 números não confere."
            return
        }
        if (SecurityUtils.verifyPassword(pinNovo, atual.senhaHash)) {
            _erroLogin.value = "A nova senha de 4 números deve ser diferente da senha temporária."
            return
        }
        viewModelScope.launch {
            _erroLogin.value = null
            val atualizado = repository.concluirPrimeiroAcesso(atual, pinNovo)
            _usuarioLogado.value = atualizado
            _mensagemFeedback.value = "Senha numérica de 4 dígitos definida com sucesso! Bem-vindo(a), ${atualizado.orunko}."
        }
    }

    fun realizarLogout() {
        _usuarioLogado.value = null
        _erroLogin.value = null
        _currentDestination.value = AppDestination.HOME
        _currentSubScreen.value = SubScreen.NONE
    }

    // AÇÕES ADMINISTRATIVAS — PESSOAS / MEMBROS
    fun salvarPessoa(
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
        fotoBase64: String = ""
    ) {
        val admin = _usuarioLogado.value ?: return
        val cpfLimpo = SecurityUtils.cleanCpf(cpf)
        if (nome.isBlank() || orunko.isBlank() || cpfLimpo.length != 11) {
            _mensagemFeedback.value = "Preencha Nome Civil, Orunkó e um CPF válido de 11 dígitos."
            return
        }

        // Preenche automaticamente datas de obrigação caso não tenham sido informadas
        val d1 = data1Ano.ifBlank { DateAndCalendarUtils.computeObligationDate(dataIniciacao, 1) }
        val d3 = data3Anos.ifBlank { DateAndCalendarUtils.computeObligationDate(dataIniciacao, 3) }
        val d7 = data7Anos.ifBlank { DateAndCalendarUtils.computeObligationDate(dataIniciacao, 7) }
        val d14 = data14Anos.ifBlank { DateAndCalendarUtils.computeObligationDate(dataIniciacao, 14) }
        val d21 = data21Anos.ifBlank { DateAndCalendarUtils.computeObligationDate(dataIniciacao, 21) }

        viewModelScope.launch {
            if (idExistente == 0L) {
                val existenteCpf = repository.findByCpf(cpfLimpo)
                if (existenteCpf != null) {
                    _mensagemFeedback.value = "Já existe um membro cadastrado com este CPF (${existenteCpf.orunko})."
                    return@launch
                }
                val senhaTemp = SecurityUtils.generateTemporaryPassword()
                val novaPessoa = PessoaEntity(
                    nome = nome.trim(),
                    orunko = orunko.trim(),
                    cargo = cargo.trim().ifBlank { "Membro" },
                    cpf = cpfLimpo,
                    telefone = telefone.trim(),
                    endereco = endereco.trim(),
                    orixa = orixa.trim(),
                    entidades = entidades.trim(),
                    dataNascimento = dataNascimento.trim(),
                    dataIniciacao = dataIniciacao.trim(),
                    data1Ano = d1,
                    data3Anos = d3,
                    data7Anos = d7,
                    data14Anos = d14,
                    data21Anos = d21,
                    observacoes = observacoes.trim(),
                    telefonesContatos = telefonesContatos.trim(),
                    dataCadastro = DateAndCalendarUtils.currentDateFormatted(),
                    ativo = true,
                    ultimoAcesso = "Aguardando 1º Acesso",
                    perfilAcesso = perfilAcesso,
                    primeiroAcesso = true,
                    senhaHash = SecurityUtils.hashPassword(senhaTemp),
                    senhaTemporariaDica = senhaTemp,
                    fotoBase64 = fotoBase64
                )
                repository.cadastrarNovaPessoa(novaPessoa, senhaTemp, "${admin.orunko} (Admin)")
                _credencialGeradaModal.value = Triple(novaPessoa.orunko, SecurityUtils.formatCpf(cpfLimpo), senhaTemp)
                _mensagemFeedback.value = "Cadastro de ${novaPessoa.orunko} criado com senha temporária!"
            } else {
                val anterior = repository.findById(idExistente) ?: return@launch
                val atualizada = anterior.copy(
                    nome = nome.trim(),
                    orunko = orunko.trim(),
                    cargo = cargo.trim(),
                    cpf = cpfLimpo,
                    telefone = telefone.trim(),
                    endereco = endereco.trim(),
                    orixa = orixa.trim(),
                    entidades = entidades.trim(),
                    dataNascimento = dataNascimento.trim(),
                    dataIniciacao = dataIniciacao.trim(),
                    data1Ano = d1,
                    data3Anos = d3,
                    data7Anos = d7,
                    data14Anos = d14,
                    data21Anos = d21,
                    observacoes = observacoes.trim(),
                    telefonesContatos = telefonesContatos.trim(),
                    perfilAcesso = perfilAcesso,
                    fotoBase64 = fotoBase64
                )
                val mudancas = buildList {
                    if (anterior.telefone != atualizada.telefone) add("Telefone alterado")
                    if (anterior.cargo != atualizada.cargo) add("Cargo: ${anterior.cargo} -> ${atualizada.cargo}")
                    if (anterior.orunko != atualizada.orunko) add("Orunkó atualizado")
                    if (anterior.endereco != atualizada.endereco) add("Endereço atualizado")
                    if (anterior.perfilAcesso != atualizada.perfilAcesso) add("Perfil: ${atualizada.perfilAcesso}")
                    if (anterior.fotoBase64 != atualizada.fotoBase64) add("Foto do membro atualizada")
                }.joinToString(", ").ifEmpty { "Dados cadastrais revisados pelo Administrador." }

                repository.editarPessoa(atualizada, mudancas, "${admin.orunko} (Admin)")
                if (_usuarioLogado.value?.id == atualizada.id) {
                    _usuarioLogado.value = atualizada
                }
                _mensagemFeedback.value = "Dados de ${atualizada.orunko} atualizados com sucesso."
            }
        }
    }

    fun atualizarFotoMembro(pessoa: PessoaEntity, novaFotoBase64: String) {
        val autor = _usuarioLogado.value ?: return
        viewModelScope.launch {
            val atualizada = pessoa.copy(fotoBase64 = novaFotoBase64)
            val descricaoAcao = if (novaFotoBase64.isBlank()) {
                "Foto de perfil de ${pessoa.orunko} removida."
            } else {
                "Foto de perfil de ${pessoa.orunko} cadastrada/atualizada."
            }
            repository.editarPessoa(atualizada, descricaoAcao, autor.orunko)
            if (_usuarioLogado.value?.id == atualizada.id) {
                _usuarioLogado.value = atualizada
            }
            _mensagemFeedback.value = "Foto de perfil de ${pessoa.orunko} salva com sucesso!"
        }
    }

    fun alternarAtivoInativo(pessoa: PessoaEntity) {
        val admin = _usuarioLogado.value ?: return
        if (pessoa.id == admin.id) {
            _mensagemFeedback.value = "Você não pode inativar seu próprio usuário administrador."
            return
        }
        viewModelScope.launch {
            repository.alternarAtivoInativo(pessoa, "${admin.orunko} (Admin)")
            _mensagemFeedback.value = "Status de ${pessoa.orunko} alterado para ${if (!pessoa.ativo) "ATIVO" else "INATIVO"}."
        }
    }

    fun resetarSenhaTemporaria(pessoa: PessoaEntity) {
        val admin = _usuarioLogado.value ?: return
        viewModelScope.launch {
            val novaTemp = repository.resetarSenhaTemporaria(pessoa, "${admin.orunko} (Admin)")
            _credencialGeradaModal.value = Triple(pessoa.orunko, SecurityUtils.formatCpf(pessoa.cpf), novaTemp)
            _mensagemFeedback.value = "Senha temporária gerada para ${pessoa.orunko}."
        }
    }

    // AÇÕES FINANCEIRAS (Exclusivas do Administrador)
    fun salvarPagamento(
        idExistente: Long,
        pessoaId: Long,
        competenciaMes: Int,
        ano: Int,
        valor: Double,
        dataPagamento: String,
        status: String,
        observacao: String,
        statusAnterior: String? = null
    ) {
        val admin = _usuarioLogado.value ?: return
        if (admin.perfilAcesso != "ADMIN") return
        viewModelScope.launch {
            val membro = repository.findById(pessoaId)
            val orunkoMembro = membro?.orunko ?: "Membro #$pessoaId"
            val dataPagFinal = if (status == "PAGO" && dataPagamento.isBlank()) {
                DateAndCalendarUtils.currentDateFormatted()
            } else if (status != "PAGO") {
                ""
            } else {
                dataPagamento.trim()
            }
            val pag = PagamentoEntity(
                id = idExistente,
                pessoaId = pessoaId,
                competenciaMes = competenciaMes,
                ano = ano,
                valor = valor,
                dataPagamento = dataPagFinal,
                status = status,
                observacao = observacao.trim(),
                adminResponsavel = "${admin.orunko} (Admin)",
                dataHoraRegistro = DateAndCalendarUtils.currentDateTimeFormatted()
            )
            repository.registrarOuCorrigirPagamento(pag, orunkoMembro, "${admin.orunko} (Admin)", statusAnterior)
            _mensagemFeedback.value = "Lançamento financeiro de $orunkoMembro salvo no histórico."
        }
    }

    // EVENTOS
    fun salvarEvento(
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
    ) {
        val admin = _usuarioLogado.value ?: return
        if (nome.isBlank() || data.isBlank()) {
            _mensagemFeedback.value = "Informe o nome e a data do evento."
            return
        }
        viewModelScope.launch {
            val ev = EventoEntity(
                id = idExistente,
                nome = nome.trim(),
                data = data.trim(),
                horario = horario.trim().ifBlank { "18:00" },
                local = local.trim().ifBlank { "Sede Casa do Ogum - RJ" },
                descricao = descricao.trim(),
                valorContribuicao = valorContribuicao,
                responsaveis = responsaveis.trim(),
                participantes = participantes.trim(),
                observacoes = observacoes.trim()
            )
            repository.salvarEvento(ev, "${admin.orunko} (Admin)")
            _mensagemFeedback.value = "Evento '${ev.nome}' salvo no calendário."
        }
    }

    fun excluirEvento(evento: EventoEntity) {
        val admin = _usuarioLogado.value ?: return
        viewModelScope.launch {
            repository.excluirEvento(evento, "${admin.orunko} (Admin)")
            _mensagemFeedback.value = "Evento '${evento.nome}' removido."
        }
    }

    // COMUNICADOS
    fun salvarComunicado(
        idExistente: Long,
        titulo: String,
        texto: String,
        publicoAlvo: String,
        destaque: Boolean
    ) {
        val admin = _usuarioLogado.value ?: return
        if (titulo.isBlank() || texto.isBlank()) {
            _mensagemFeedback.value = "Preencha título e texto do comunicado."
            return
        }
        viewModelScope.launch {
            val com = ComunicadoEntity(
                id = idExistente,
                titulo = titulo.trim(),
                texto = texto.trim(),
                data = DateAndCalendarUtils.currentDateFormatted(),
                publicoAlvo = publicoAlvo,
                destaque = destaque,
                ativo = true
            )
            repository.salvarComunicado(com, "${admin.orunko} (Admin)")
            _mensagemFeedback.value = "Comunicado '${com.titulo}' publicado!"
        }
    }

    fun alternarStatusComunicado(comunicado: ComunicadoEntity) {
        val admin = _usuarioLogado.value ?: return
        viewModelScope.launch {
            repository.alternarComunicadoAtivo(comunicado, "${admin.orunko} (Admin)")
        }
    }

    // TAREFAS
    fun salvarTarefa(
        idExistente: Long,
        descricao: String,
        responsavelPessoa: PessoaEntity,
        prazo: String,
        prioridade: String,
        status: String,
        observacoes: String
    ) {
        val admin = _usuarioLogado.value ?: return
        if (descricao.isBlank()) {
            _mensagemFeedback.value = "Informe a descrição da tarefa."
            return
        }
        viewModelScope.launch {
            val tar = TarefaEntity(
                id = idExistente,
                descricao = descricao.trim(),
                responsavelPessoaId = responsavelPessoa.id,
                responsavelOrunko = responsavelPessoa.orunko,
                prazo = prazo.trim().ifBlank { DateAndCalendarUtils.currentDateFormatted() },
                prioridade = prioridade,
                status = status,
                observacoes = observacoes.trim()
            )
            repository.salvarTarefa(tar, "${admin.orunko} (Admin)")
            _mensagemFeedback.value = "Tarefa atribuída a ${responsavelPessoa.orunko}."
        }
    }

    fun alterarStatusTarefa(tarefa: TarefaEntity, novoStatus: String) {
        val user = _usuarioLogado.value ?: return
        viewModelScope.launch {
            repository.atualizarStatusTarefa(tarefa, novoStatus, user.orunko)
            _mensagemFeedback.value = "Status da tarefa atualizado para $novoStatus."
        }
    }

    // CÁLCULOS DOS CALENDÁRIOS (SEMPRE UTILIZANDO APENAS O ORUNKÓ!)
    fun construirCalendarioAniversariantes(listaPessoas: List<PessoaEntity>, anoAlvo: Int = 2026): List<AniversarianteItem> {
        return listaPessoas
            .filter { it.ativo }
            .mapNotNull { p ->
                val parsed = DateAndCalendarUtils.parseDate(p.dataNascimento) ?: return@mapNotNull null
                AniversarianteItem(
                    pessoaId = p.id,
                    orunko = p.orunko, // REGRA DO ORUNKÓ: Jamais expor nome civil!
                    orixa = p.orixa,
                    cargo = p.cargo,
                    dia = parsed.day,
                    mes = parsed.month,
                    diaMesFormatado = "%02d/%02d".format(parsed.day, parsed.month),
                    idadeNoAno = DateAndCalendarUtils.calculateYearsInYear(p.dataNascimento, anoAlvo),
                    diasAteProximo = DateAndCalendarUtils.daysUntilNextOccurrence(p.dataNascimento),
                    fotoBase64 = p.fotoBase64
                )
            }
            .sortedWith(compareBy<AniversarianteItem> { it.mes }.thenBy { it.dia })
    }

    fun construirCalendarioOdunKodun(listaPessoas: List<PessoaEntity>, anoAlvo: Int = 2026): List<OdunKodunItem> {
        return listaPessoas
            .filter { it.ativo }
            .mapNotNull { p ->
                val parsed = DateAndCalendarUtils.parseDate(p.dataIniciacao) ?: return@mapNotNull null
                val anos = (anoAlvo - parsed.year).coerceAtLeast(0)
                val marco = when (anos) {
                    1 -> "Obrigação de 1 Ano"
                    3 -> "Obrigação de 3 Anos"
                    7 -> "Obrigação de 7 Anos (Odún Ejé)"
                    14 -> "Obrigação de 14 Anos (Odún Edogun)"
                    21 -> "Obrigação de 21 Anos (Odún Ogun)"
                    else -> null
                }
                OdunKodunItem(
                    pessoaId = p.id,
                    orunko = p.orunko, // REGRA DO ORUNKÓ: Jamais expor nome civil!
                    orixa = p.orixa,
                    cargo = p.cargo,
                    dataIniciacao = p.dataIniciacao,
                    dia = parsed.day,
                    mes = parsed.month,
                    diaMesFormatado = "%02d/%02d".format(parsed.day, parsed.month),
                    anosCompletos = anos,
                    marcoObrigacao = marco,
                    diasAteProximo = DateAndCalendarUtils.daysUntilNextOccurrence(p.dataIniciacao),
                    fotoBase64 = p.fotoBase64
                )
            }
            .sortedWith(compareBy<OdunKodunItem> { it.mes }.thenBy { it.dia })
    }

    fun calcularPainelFinanceiro(
        listaPagamentos: List<PagamentoEntity>,
        mes: Int,
        ano: Int
    ): PainelFinanceiroResumo {
        val filtrados = listaPagamentos.filter { it.competenciaMes == mes && it.ano == ano }
        val pagos = filtrados.filter { it.status == "PAGO" }
        val pendentes = filtrados.filter { it.status == "PENDENTE" }
        val atrasados = filtrados.filter { it.status == "EM_ATRASO" }

        val totalPrevisto = filtrados.sumOf { it.valor }
        val totalRecebido = pagos.sumOf { it.valor }
        val totalPendente = pendentes.sumOf { it.valor }
        val totalAtraso = atrasados.sumOf { it.valor }

        return PainelFinanceiroResumo(
            mesReferencia = mes,
            anoReferencia = ano,
            totalPrevisto = totalPrevisto,
            totalRecebido = totalRecebido,
            totalPendente = totalPendente + totalAtraso,
            totalEmAtraso = totalAtraso,
            qtdPagos = pagos.size,
            qtdPendentes = pendentes.size,
            qtdInadimplentes = atrasados.size
        )
    }

    // EXPORTAÇÃO DE RELATÓRIOS (PDF E EXCEL)
    fun exportarRelatorioFinanceiro(
        context: Context,
        formatoPdf: Boolean,
        mes: Int,
        ano: Int,
        listaPagamentos: List<PagamentoEntity>,
        listaPessoas: List<PessoaEntity>
    ) {
        val mapaPessoas = listaPessoas.associateBy { it.id }
        val filtrados = if (mes == 0) {
            listaPagamentos.filter { it.ano == ano }
        } else {
            listaPagamentos.filter { it.competenciaMes == mes && it.ano == ano }
        }
        val tituloPeriodo = if (mes == 0) "Anual $ano" else "${DateAndCalendarUtils.monthName(mes)}/$ano"
        val headers = listOf("Orunkó", "Cargo", "Comp.", "Valor", "Status", "Data Pag.", "Admin")
        val rows = filtrados.map { pag ->
            val p = mapaPessoas[pag.pessoaId]
            listOf(
                p?.orunko ?: "Membro #${pag.pessoaId}",
                p?.cargo ?: "-",
                "%02d/%d".format(pag.competenciaMes, pag.ano),
                DateAndCalendarUtils.formatCurrency(pag.valor),
                pag.status,
                pag.dataPagamento.ifBlank { "-" },
                pag.adminResponsavel
            )
        }
        val totalPrev = filtrados.sumOf { it.valor }
        val totalRec = filtrados.filter { it.status == "PAGO" }.sumOf { it.valor }
        val totalPend = filtrados.filter { it.status != "PAGO" }.sumOf { it.valor }
        val metrics = listOf(
            "Período" to tituloPeriodo,
            "Total Previsto" to DateAndCalendarUtils.formatCurrency(totalPrev),
            "Total Recebido" to DateAndCalendarUtils.formatCurrency(totalRec),
            "Total Pendente" to DateAndCalendarUtils.formatCurrency(totalPend),
            "Pagos" to "${filtrados.count { it.status == "PAGO" }} lançamentos",
            "Em Atraso" to "${filtrados.count { it.status == "EM_ATRASO" }} lançamentos"
        )

        val result = if (formatoPdf) {
            ReportExporter.exportToPdf(
                context = context,
                reportTitle = "Relatório Financeiro — $tituloPeriodo",
                subtitle = "Controle de Mensalidades e Contribuições",
                summaryMetrics = metrics,
                headers = headers,
                rows = rows
            )
        } else {
            ReportExporter.exportToExcelCsv(
                context = context,
                reportTitle = "Relatorio_Financeiro_$tituloPeriodo",
                headers = headers,
                rows = rows
            )
        }
        _ultimoRelatorioExportado.value = result
        _mensagemFeedback.value = result.message
    }

    fun exportarRelatorioCadastral(
        context: Context,
        formatoPdf: Boolean,
        listaPessoas: List<PessoaEntity>
    ) {
        val headers = listOf("Orunkó", "Cargo", "Orixá", "Iniciação", "Status", "Perfil")
        val rows = listaPessoas.map { p ->
            listOf(
                p.orunko,
                p.cargo,
                p.orixa,
                p.dataIniciacao,
                if (p.ativo) "ATIVO" else "INATIVO",
                p.perfilAcesso
            )
        }
        val ativos = listaPessoas.count { it.ativo }
        val inativos = listaPessoas.count { !it.ativo }
        val metrics = listOf(
            "Total de Cadastros" to "${listaPessoas.size} pessoas",
            "Membros Ativos" to "$ativos ativos",
            "Membros Inativos" to "$inativos inativos"
        )

        val result = if (formatoPdf) {
            ReportExporter.exportToPdf(
                context = context,
                reportTitle = "Relatório Geral de Membros e Orixás",
                subtitle = "Quadro Institucional da Casa do Ogum - RJ",
                summaryMetrics = metrics,
                headers = headers,
                rows = rows
            )
        } else {
            ReportExporter.exportToExcelCsv(
                context = context,
                reportTitle = "Relatorio_Cadastral_Membros",
                headers = headers,
                rows = rows
            )
        }
        _ultimoRelatorioExportado.value = result
        _mensagemFeedback.value = result.message
    }

    class Factory(private val repository: CasaOgumRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return CasaOgumViewModel(repository) as T
        }
    }
}
