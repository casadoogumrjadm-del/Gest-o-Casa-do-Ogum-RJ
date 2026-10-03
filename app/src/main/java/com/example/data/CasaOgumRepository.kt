package com.example.data

import com.example.util.DateAndCalendarUtils
import com.example.util.SecurityUtils
import kotlinx.coroutines.flow.Flow

class CasaOgumRepository(private val dao: CasaOgumDao) {

    val allPessoas: Flow<List<PessoaEntity>> = dao.getAllPessoas()
    val allPagamentos: Flow<List<PagamentoEntity>> = dao.getAllPagamentos()
    val allEventos: Flow<List<EventoEntity>> = dao.getAllEventos()
    val allComunicados: Flow<List<ComunicadoEntity>> = dao.getAllComunicados()
    val allTarefas: Flow<List<TarefaEntity>> = dao.getAllTarefas()
    val allHistorico: Flow<List<HistoricoAdminEntity>> = dao.getAllHistorico()
    val allConfiguracoes: Flow<List<ConfiguracaoEntity>> = dao.getAllConfiguracoes()

    suspend fun ensureSeeded() {
        CasaOgumDatabase.seedIfEmpty(dao)
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
    }

    suspend fun concluirPrimeiroAcesso(pessoa: PessoaEntity, novaSenhaPessoal: String): PessoaEntity {
        val atualizado = pessoa.copy(
            primeiroAcesso = false,
            senhaHash = SecurityUtils.hashPassword(novaSenhaPessoal),
            senhaTemporariaDica = "",
            ultimoAcesso = DateAndCalendarUtils.currentDateTimeFormatted()
        )
        dao.updatePessoa(atualizado)
        dao.insertHistorico(
            HistoricoAdminEntity(
                acao = "Primeiro acesso concluído pelo membro",
                detalhes = "O membro ${pessoa.orunko} definiu sua senha definitiva. A senha temporária foi invalidada.",
                adminNome = pessoa.orunko,
                dataHora = DateAndCalendarUtils.currentDateTimeFormatted(),
                categoria = "ACESSO"
            )
        )
        return atualizado
    }

    suspend fun cadastrarNovaPessoa(
        pessoa: PessoaEntity,
        senhaTemporariaGerada: String,
        adminOrunko: String
    ): Long {
        val id = dao.insertPessoa(pessoa)
        dao.insertHistorico(
            HistoricoAdminEntity(
                acao = "Cadastrou membro e criou acesso",
                detalhes = "Cadastrado Orunkó '${pessoa.orunko}' (${pessoa.cargo}) com CPF ${SecurityUtils.maskCpf(pessoa.cpf)} e senha temporária '$senhaTemporariaGerada'.",
                adminNome = adminOrunko,
                dataHora = DateAndCalendarUtils.currentDateTimeFormatted(),
                categoria = "CADASTRO"
            )
        )
        return id
    }

    suspend fun editarPessoa(
        pessoaAtualizada: PessoaEntity,
        resumoAlteracao: String,
        adminOrunko: String
    ) {
        dao.updatePessoa(pessoaAtualizada)
        dao.insertHistorico(
            HistoricoAdminEntity(
                acao = "Editou dados cadastrais de ${pessoaAtualizada.orunko}",
                detalhes = resumoAlteracao,
                adminNome = adminOrunko,
                dataHora = DateAndCalendarUtils.currentDateTimeFormatted(),
                categoria = "CADASTRO"
            )
        )
    }

    suspend fun alternarAtivoInativo(
        pessoa: PessoaEntity,
        adminOrunko: String
    ) {
        val novoStatus = !pessoa.ativo
        dao.updatePessoa(pessoa.copy(ativo = novoStatus))
        val verbo = if (novoStatus) "Reativou" else "Inativou"
        dao.insertHistorico(
            HistoricoAdminEntity(
                acao = "$verbo cadastro de ${pessoa.orunko}",
                detalhes = "Status cadastral de ${pessoa.orunko} alterado para ${if (novoStatus) "ATIVO" else "INATIVO"}.",
                adminNome = adminOrunko,
                dataHora = DateAndCalendarUtils.currentDateTimeFormatted(),
                categoria = "CADASTRO"
            )
        )
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
        dao.insertHistorico(
            HistoricoAdminEntity(
                acao = "Gerou/resetou senha temporária",
                detalhes = "Nova senha temporária '$novaTemp' gerada para ${pessoa.orunko}. Exigida troca no próximo login.",
                adminNome = adminOrunko,
                dataHora = DateAndCalendarUtils.currentDateTimeFormatted(),
                categoria = "ACESSO"
            )
        )
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
            dao.insertPagamento(registro)
            dao.insertHistorico(
                HistoricoAdminEntity(
                    acao = "Registrou pagamento de ${DateAndCalendarUtils.monthName(pagamento.competenciaMes)}/${pagamento.ano}",
                    detalhes = "Membro: $orunkoMembro | Valor: ${DateAndCalendarUtils.formatCurrency(pagamento.valor)} | Status: ${pagamento.status}",
                    adminNome = adminOrunko,
                    dataHora = now,
                    categoria = "FINANCEIRO"
                )
            )
        } else {
            dao.updatePagamento(registro)
            val transicao = if (statusAnterior != null && statusAnterior != pagamento.status) {
                "de $statusAnterior para ${pagamento.status}"
            } else {
                "Status: ${pagamento.status}"
            }
            dao.insertHistorico(
                HistoricoAdminEntity(
                    acao = "Corrigiu lançamento financeiro ($orunkoMembro)",
                    detalhes = "Competência ${DateAndCalendarUtils.monthName(pagamento.competenciaMes)}/${pagamento.ano} alterada ($transicao) — Valor: ${DateAndCalendarUtils.formatCurrency(pagamento.valor)}. Obs: ${pagamento.observacao}",
                    adminNome = adminOrunko,
                    dataHora = now,
                    categoria = "FINANCEIRO"
                )
            )
        }
    }

    suspend fun salvarEvento(evento: EventoEntity, adminOrunko: String) {
        val isNew = evento.id == 0L
        dao.insertEvento(evento)
        dao.insertHistorico(
            HistoricoAdminEntity(
                acao = if (isNew) "Criou evento no calendário" else "Atualizou evento",
                detalhes = "Evento '${evento.nome}' em ${evento.data} às ${evento.horario} (${evento.local}).",
                adminNome = adminOrunko,
                dataHora = DateAndCalendarUtils.currentDateTimeFormatted(),
                categoria = "EVENTO"
            )
        )
    }

    suspend fun excluirEvento(evento: EventoEntity, adminOrunko: String) {
        dao.deleteEvento(evento.id)
        dao.insertHistorico(
            HistoricoAdminEntity(
                acao = "Removeu evento",
                detalhes = "Evento '${evento.nome}' (${evento.data}) removido do calendário.",
                adminNome = adminOrunko,
                dataHora = DateAndCalendarUtils.currentDateTimeFormatted(),
                categoria = "EVENTO"
            )
        )
    }

    suspend fun salvarComunicado(comunicado: ComunicadoEntity, adminOrunko: String) {
        val isNew = comunicado.id == 0L
        dao.insertComunicado(comunicado)
        dao.insertHistorico(
            HistoricoAdminEntity(
                acao = if (isNew) "Publicou comunicado" else "Editou comunicado",
                detalhes = "Título: '${comunicado.titulo}' | Público: ${comunicado.publicoAlvo} | Destaque: ${if (comunicado.destaque) "SIM" else "NÃO"}",
                adminNome = adminOrunko,
                dataHora = DateAndCalendarUtils.currentDateTimeFormatted(),
                categoria = "COMUNICADO"
            )
        )
    }

    suspend fun alternarComunicadoAtivo(comunicado: ComunicadoEntity, adminOrunko: String) {
        val atualizado = comunicado.copy(ativo = !comunicado.ativo)
        dao.updateComunicado(atualizado)
        dao.insertHistorico(
            HistoricoAdminEntity(
                acao = "Alterou visibilidade de comunicado",
                detalhes = "Comunicado '${comunicado.titulo}' marcado como ${if (atualizado.ativo) "ATIVO" else "INATIVO"}.",
                adminNome = adminOrunko,
                dataHora = DateAndCalendarUtils.currentDateTimeFormatted(),
                categoria = "COMUNICADO"
            )
        )
    }

    suspend fun salvarTarefa(tarefa: TarefaEntity, adminOrunko: String) {
        val isNew = tarefa.id == 0L
        dao.insertTarefa(tarefa)
        dao.insertHistorico(
            HistoricoAdminEntity(
                acao = if (isNew) "Criou tarefa para ${tarefa.responsavelOrunko}" else "Atualizou tarefa de ${tarefa.responsavelOrunko}",
                detalhes = "Tarefa: '${tarefa.descricao}' | Prioridade: ${tarefa.prioridade} | Status: ${tarefa.status} | Prazo: ${tarefa.prazo}",
                adminNome = adminOrunko,
                dataHora = DateAndCalendarUtils.currentDateTimeFormatted(),
                categoria = "TAREFA"
            )
        )
    }

    suspend fun atualizarStatusTarefa(tarefa: TarefaEntity, novoStatus: String, autorOrunko: String) {
        val atualizada = tarefa.copy(status = novoStatus)
        dao.updateTarefa(atualizada)
        dao.insertHistorico(
            HistoricoAdminEntity(
                acao = "Status de tarefa alterado para $novoStatus",
                detalhes = "Tarefa '${tarefa.descricao}' (${tarefa.responsavelOrunko}) alterada de ${tarefa.status} para $novoStatus.",
                adminNome = autorOrunko,
                dataHora = DateAndCalendarUtils.currentDateTimeFormatted(),
                categoria = "TAREFA"
            )
        )
    }
}
