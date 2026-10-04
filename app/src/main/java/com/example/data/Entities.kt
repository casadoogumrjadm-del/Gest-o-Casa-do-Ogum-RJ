package com.example.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "pessoas",
    indices = [Index(value = ["cpf"], unique = true)]
)
data class PessoaEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val nome: String, // Nome Civil (confidencial - exclusivo Admin e próprio membro)
    val orunko: String, // Orunkó (exibido obrigatoriamente em todos os calendários e áreas coletivas)
    val cargo: String,
    val cpf: String,
    val telefone: String,
    val endereco: String,
    val orixa: String,
    val entidades: String,
    val dataNascimento: String, // DD/MM/YYYY
    val dataIniciacao: String,  // DD/MM/YYYY
    val data1Ano: String,
    val data3Anos: String,
    val data7Anos: String,
    val data14Anos: String,
    val data21Anos: String,
    val observacoes: String,
    val telefonesContatos: String,
    // Informações Internas e de Acesso (USUÁRIOS -> PESSOAS)
    val dataCadastro: String,
    val ativo: Boolean = true,
    val ultimoAcesso: String = "Nunca acessou",
    val perfilAcesso: String = "MEMBRO", // "ADMIN" ou "MEMBRO"
    val primeiroAcesso: Boolean = true,
    val senhaHash: String,
    val senhaTemporariaDica: String = "", // Exibido apenas ao Admin enquanto primeiroAcesso == true
    val fotoBase64: String = "" // Foto do membro (armazenada em Base64 no Firestore e banco local)
)

@Entity(tableName = "pagamentos")
data class PagamentoEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val pessoaId: Long,
    val competenciaMes: Int, // 1..12
    val ano: Int,            // ex: 2026
    val valor: Double,
    val dataPagamento: String, // DD/MM/YYYY ou ""
    val status: String,        // "PAGO", "PENDENTE", "EM_ATRASO"
    val observacao: String,
    val adminResponsavel: String,
    val dataHoraRegistro: String
)

@Entity(tableName = "eventos")
data class EventoEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val nome: String,
    val data: String,    // DD/MM/YYYY
    val horario: String, // HH:mm
    val local: String,
    val descricao: String,
    val valorContribuicao: Double,
    val responsaveis: String,  // Orunkós responsáveis
    val participantes: String, // Orunkós participantes confirmados
    val observacoes: String
)

@Entity(tableName = "comunicados")
data class ComunicadoEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val titulo: String,
    val texto: String,
    val data: String,
    val publicoAlvo: String, // "Todos", "Iniciados", "Ogãs e Ekedis", "Diretoria"
    val destaque: Boolean = false, // ⭐ DESTAQUE
    val ativo: Boolean = true
)

@Entity(tableName = "tarefas")
data class TarefaEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val descricao: String,
    val responsavelPessoaId: Long,
    val responsavelOrunko: String, // Sempre Orunkó nas áreas coletivas
    val prazo: String,             // DD/MM/YYYY
    val prioridade: String,        // "Baixa", "Normal", "Alta", "Urgente"
    val status: String,            // "PENDENTE", "EM_ANDAMENTO", "CONCLUIDA"
    val observacoes: String
)

@Entity(tableName = "historico_administrativo")
data class HistoricoAdminEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val acao: String,
    val detalhes: String,
    val adminNome: String,
    val dataHora: String,
    val categoria: String // "CADASTRO", "FINANCEIRO", "ACESSO", "EVENTO", "COMUNICADO", "TAREFA"
)

@Entity(tableName = "configuracoes")
data class ConfiguracaoEntity(
    @PrimaryKey val chave: String,
    val valor: String
)

// Modelos de Domínio para Calendários (Regra Fundamental do Orunkó: sem nome civil!)
data class AniversarianteItem(
    val pessoaId: Long,
    val orunko: String,
    val orixa: String,
    val cargo: String,
    val dia: Int,
    val mes: Int,
    val diaMesFormatado: String,
    val idadeNoAno: Int?,
    val diasAteProximo: Int,
    val fotoBase64: String = ""
)

data class OdunKodunItem(
    val pessoaId: Long,
    val orunko: String,
    val orixa: String,
    val cargo: String,
    val dataIniciacao: String,
    val dia: Int,
    val mes: Int,
    val diaMesFormatado: String,
    val anosCompletos: Int,
    val marcoObrigacao: String?, // Ex: "Obrigação de 7 Anos", "Obrigação de 14 Anos", etc.
    val diasAteProximo: Int,
    val fotoBase64: String = ""
)

data class PainelFinanceiroResumo(
    val mesReferencia: Int,
    val anoReferencia: Int,
    val totalPrevisto: Double,
    val totalRecebido: Double,
    val totalPendente: Double,
    val totalEmAtraso: Double,
    val qtdPagos: Int,
    val qtdPendentes: Int,
    val qtdInadimplentes: Int
)
