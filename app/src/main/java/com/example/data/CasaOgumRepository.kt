package com.example.data

import com.example.util.DateAndCalendarUtils
import com.example.util.SecurityUtils
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class CasaOgumRepository(
    private val dao: CasaOgumDao,
    private val firestoreService: FirestoreSyncService? = null
) {

    val allPessoas: Flow<List<PessoaEntity>> = dao.getAllPessoas()
    val allPagamentos: Flow<List<PagamentoEntity>> = dao.getAllPagamentos()
    val allEventos: Flow<List<EventoEntity>> = dao.getAllEventos()
    val allComunicados: Flow<List<ComunicadoEntity>> = dao.getAllComunicados()
    val allTarefas: Flow<List<TarefaEntity>> = dao.getAllTarefas()
    val allHistorico: Flow<List<HistoricoAdminEntity>> = dao.getAllHistorico()
    val allConfiguracoes: Flow<List<ConfiguracaoEntity>> = dao.getAllConfiguracoes()

    private val fallbackStatus = MutableStateFlow(FirestoreStatusInfo())
    val firestoreStatus: StateFlow<FirestoreStatusInfo> = firestoreService?.statusInfo ?: fallbackStatus

    suspend fun ensureSeeded() {
        CasaOgumDatabase.seedIfEmpty(dao)
        val admin = dao.getPessoaByCpf(CasaOgumDatabase.ADMIN_OFICIAL_CPF)
        if (admin != null) {
            firestoreService?.upsertMembro(admin)
        }
    }

    suspend fun findByCpf(cpf: String): PessoaEntity? {
        return dao.getPessoaByCpf(SecurityUtils.cleanCpf(cpf))
    }

    suspend fun findById(id: Long): PessoaEntity? {
        return dao.getPessoaById(id)
    }

    suspend fun registrarAcessoLogin(pessoa: PessoaEntity) {
        val atualizado = pessoa.copy(ultimoAcesso = DateAndCalendarUtils.currentDateTimeFormatted())
        dao.updatePessoa(atualizado)
        firestoreService?.upsertMembro(atualizado)
    }

    suspend fun concluirPrimeiroAcesso(pessoa: PessoaEntity, novaSenhaPessoal: String): PessoaEntity {
        val atualizado = pessoa.copy(
            primeiroAcesso = false,
            senhaHash = SecurityUtils.hashPassword(novaSenhaPessoal),
            senhaTemporariaDica = "",
            ultimoAcesso = DateAndCalendarUtils.currentDateTimeFormatted()
        )
        dao.updatePessoa(atualizado)
        firestoreService?.upsertMembro(atualizado)
        val hist = HistoricoAdminEntity(
            acao = "Primeiro acesso concluído pelo membro",
            detalhes = "O membro ${pessoa.orunko} definiu sua senha definitiva de 4 números. A senha temporária foi invalidada.",
            adminNome = pessoa.orunko,
            dataHora = DateAndCalendarUtils.currentDateTimeFormatted(),
            categoria = "ACESSO"
        )
        dao.insertHistorico(hist)
        firestoreService?.registrarHistoricoFirestore(hist)
        return atualizado
    }

    suspend fun cadastrarNovaPessoa(
        pessoa: PessoaEntity,
        senhaTemporariaGerada: String,
        adminOrunko: String
    ): Long {
        val id = dao.insertPessoa(pessoa)
        val pessoaComId = pessoa.copy(id = id)
        firestoreService?.upsertMembro(pessoaComId)
        val hist = HistoricoAdminEntity(
            acao = "Cadastrou membro e criou acesso (Firestore + Local)",
            detalhes = "Cadastrado Orunkó '${pessoa.orunko}' (${pessoa.cargo}) com CPF ${SecurityUtils.maskCpf(pessoa.cpf)} e senha temporária '$senhaTemporariaGerada'.",
            adminNome = adminOrunko,
            dataHora = DateAndCalendarUtils.currentDateTimeFormatted(),
            categoria = "CADASTRO"
        )
        dao.insertHistorico(hist)
        firestoreService?.registrarHistoricoFirestore(hist)
        return id
    }

    suspend fun editarPessoa(
        pessoaAtualizada: PessoaEntity,
        resumoAlteracao: String,
        adminOrunko: String
    ) {
        dao.updatePessoa(pessoaAtualizada)
        firestoreService?.upsertMembro(pessoaAtualizada)
        val hist = HistoricoAdminEntity(
            acao = "Editou dados cadastrais de ${pessoaAtualizada.orunko}",
            detalhes = resumoAlteracao,
            adminNome = adminOrunko,
            dataHora = DateAndCalendarUtils.currentDateTimeFormatted(),
            categoria = "CADASTRO"
        )
        dao.insertHistorico(hist)
        firestoreService?.registrarHistoricoFirestore(hist)
    }

    suspend fun alternarAtivoInativo(
        pessoa: PessoaEntity,
        adminOrunko: String
    ) {
        val novoStatus = !pessoa.ativo
        val atualizada = pessoa.copy(ativo = novoStatus)
        dao.updatePessoa(atualizada)
        firestoreService?.upsertMembro(atualizada)
        val verbo = if (novoStatus) "Reativou" else "Inativou"
        val hist = HistoricoAdminEntity(
            acao = "$verbo cadastro de ${pessoa.orunko}",
            detalhes = "Status cadastral de ${pessoa.orunko} alterado para ${if (novoStatus) "ATIVO" else "INATIVO"}.",
            adminNome = adminOrunko,
            dataHora = DateAndCalendarUtils.currentDateTimeFormatted(),
            categoria = "CADASTRO"
        )
        dao.insertHistorico(hist)
        firestoreService?.registrarHistoricoFirestore(hist)
    }

    suspend fun resetarSenhaTemporaria(
        pessoa: PessoaEntity,
        adminOrunko: String
    ): String {
        val novaTemp = SecurityUtils.generateTemporaryPassword()
        val atualizado = pessoa.copy(
            primeiroAcesso = true,
            senhaHash = SecurityUtils.hashPassword(novaTemp),
            senhaTemporariaDica = novaTemp
        )
        dao.updatePessoa(atualizado)
        firestoreService?.upsertMembro(atualizado)
        val hist = HistoricoAdminEntity(
            acao = "Gerou/resetou senha temporária (4 números)",
            detalhes = "Nova senha temporária '$novaTemp' gerada para ${pessoa.orunko}. Exigida troca no próximo login.",
            adminNome = adminOrunko,
            dataHora = DateAndCalendarUtils.currentDateTimeFormatted(),
            categoria = "ACESSO"
        )
        dao.insertHistorico(hist)
        firestoreService?.registrarHistoricoFirestore(hist)
        return novaTemp
    }

    suspend fun registrarOuCorrigirPagamento(
        pagamento: PagamentoEntity,
        orunkoMembro: String,
        adminOrunko: String,
        statusAnterior: String? = null
    ) {
        val now = DateAndCalendarUtils.currentDateTimeFormatted()
        val registro = pagamento.copy(
            adminResponsavel = adminOrunko,
            dataHoraRegistro = now
        )
        if (pagamento.id == 0L) {
            val idGerado = dao.insertPagamento(registro)
            val salvo = registro.copy(id = idGerado)
            firestoreService?.upsertMensalidade(salvo, orunkoMembro)
            val hist = HistoricoAdminEntity(
                acao = "Registrou mensalidade de ${DateAndCalendarUtils.monthName(pagamento.competenciaMes)}/${pagamento.ano}",
                detalhes = "Membro: $orunkoMembro | Valor: ${DateAndCalendarUtils.formatCurrency(pagamento.valor)} | Status: ${pagamento.status}",
                adminNome = adminOrunko,
                dataHora = now,
                categoria = "FINANCEIRO"
            )
            dao.insertHistorico(hist)
            firestoreService?.registrarHistoricoFirestore(hist)
        } else {
            dao.updatePagamento(registro)
            firestoreService?.upsertMensalidade(registro, orunkoMembro)
            val transicao = if (statusAnterior != null && statusAnterior != pagamento.status) {
                "de $statusAnterior para ${pagamento.status}"
            } else {
                "Status: ${pagamento.status}"
            }
            val hist = HistoricoAdminEntity(
                acao = "Corrigiu lançamento financeiro ($orunkoMembro)",
                detalhes = "Competência ${DateAndCalendarUtils.monthName(pagamento.competenciaMes)}/${pagamento.ano} alterada ($transicao) — Valor: ${DateAndCalendarUtils.formatCurrency(pagamento.valor)}. Obs: ${pagamento.observacao}",
                adminNome = adminOrunko,
                dataHora = now,
                categoria = "FINANCEIRO"
            )
            dao.insertHistorico(hist)
            firestoreService?.registrarHistoricoFirestore(hist)
        }
    }

    suspend fun salvarEvento(evento: EventoEntity, adminOrunko: String) {
        val isNew = evento.id == 0L
        val id = dao.insertEvento(evento)
        firestoreService?.upsertEvento(evento.copy(id = id))
        val hist = HistoricoAdminEntity(
            acao = if (isNew) "Criou evento no calendário" else "Atualizou evento",
            detalhes = "Evento '${evento.nome}' em ${evento.data} às ${evento.horario} (${evento.local}).",
            adminNome = adminOrunko,
            dataHora = DateAndCalendarUtils.currentDateTimeFormatted(),
            categoria = "EVENTO"
        )
        dao.insertHistorico(hist)
        firestoreService?.registrarHistoricoFirestore(hist)
    }

    suspend fun excluirEvento(evento: EventoEntity, adminOrunko: String) {
        dao.deleteEvento(evento.id)
        val hist = HistoricoAdminEntity(
            acao = "Removeu evento",
            detalhes = "Evento '${evento.nome}' (${evento.data}) removido do calendário.",
            adminNome = adminOrunko,
            dataHora = DateAndCalendarUtils.currentDateTimeFormatted(),
            categoria = "EVENTO"
        )
        dao.insertHistorico(hist)
        firestoreService?.registrarHistoricoFirestore(hist)
    }

    suspend fun salvarComunicado(comunicado: ComunicadoEntity, adminOrunko: String) {
        val isNew = comunicado.id == 0L
        val id = dao.insertComunicado(comunicado)
        firestoreService?.upsertComunicado(comunicado.copy(id = id))
        val hist = HistoricoAdminEntity(
            acao = if (isNew) "Publicou comunicado" else "Editou comunicado",
            detalhes = "Título: '${comunicado.titulo}' | Público: ${comunicado.publicoAlvo} | Destaque: ${if (comunicado.destaque) "SIM" else "NÃO"}",
            adminNome = adminOrunko,
            dataHora = DateAndCalendarUtils.currentDateTimeFormatted(),
            categoria = "COMUNICADO"
        )
        dao.insertHistorico(hist)
        firestoreService?.registrarHistoricoFirestore(hist)
    }

    suspend fun alternarComunicadoAtivo(comunicado: ComunicadoEntity, adminOrunko: String) {
        val atualizado = comunicado.copy(ativo = !comunicado.ativo)
        dao.updateComunicado(atualizado)
        firestoreService?.upsertComunicado(atualizado)
        val hist = HistoricoAdminEntity(
            acao = "Alterou visibilidade de comunicado",
            detalhes = "Comunicado '${comunicado.titulo}' marcado como ${if (atualizado.ativo) "ATIVO" else "INATIVO"}.",
            adminNome = adminOrunko,
            dataHora = DateAndCalendarUtils.currentDateTimeFormatted(),
            categoria = "COMUNICADO"
        )
        dao.insertHistorico(hist)
        firestoreService?.registrarHistoricoFirestore(hist)
    }

    suspend fun salvarTarefa(tarefa: TarefaEntity, adminOrunko: String) {
        val isNew = tarefa.id == 0L
        val id = dao.insertTarefa(tarefa)
        firestoreService?.upsertTarefa(tarefa.copy(id = id))
        val hist = HistoricoAdminEntity(
            acao = if (isNew) "Criou tarefa para ${tarefa.responsavelOrunko}" else "Atualizou tarefa de ${tarefa.responsavelOrunko}",
            detalhes = "Tarefa: '${tarefa.descricao}' | Prioridade: ${tarefa.prioridade} | Status: ${tarefa.status} | Prazo: ${tarefa.prazo}",
            adminNome = adminOrunko,
            dataHora = DateAndCalendarUtils.currentDateTimeFormatted(),
            categoria = "TAREFA"
        )
        dao.insertHistorico(hist)
        firestoreService?.registrarHistoricoFirestore(hist)
    }

    suspend fun atualizarStatusTarefa(tarefa: TarefaEntity, novoStatus: String, autorOrunko: String) {
        val atualizada = tarefa.copy(status = novoStatus)
        dao.updateTarefa(atualizada)
        firestoreService?.upsertTarefa(atualizada)
        val hist = HistoricoAdminEntity(
            acao = "Status de tarefa alterado para $novoStatus",
            detalhes = "Tarefa '${tarefa.descricao}' (${tarefa.responsavelOrunko}) alterada de ${tarefa.status} para $novoStatus.",
            adminNome = autorOrunko,
            dataHora = DateAndCalendarUtils.currentDateTimeFormatted(),
            categoria = "TAREFA"
        )
        dao.insertHistorico(hist)
        firestoreService?.registrarHistoricoFirestore(hist)
    }

    fun sincronizarMembrosEMensalidadesComFirestore(
        pessoas: List<PessoaEntity>,
        pagamentos: List<PagamentoEntity>
    ): String {
        val now = DateAndCalendarUtils.currentDateTimeFormatted()
        return firestoreService?.sincronizarTudoParaFirestore(pessoas, pagamentos, now)
            ?: "Banco de dados sincronizado localmente."
    }

    fun carregarMembrosDoFirestore() {
        firestoreService?.carregarMembrosDoFirestore()
    }
}
