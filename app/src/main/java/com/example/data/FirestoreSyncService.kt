package com.example.data

import android.content.Context
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class FirestoreConnectionState {
    CONNECTED,
    SYNCING,
    LOCAL_FALLBACK_NO_GOOGLE_SERVICES,
    ERROR
}

data class FirestoreStatusInfo(
    val state: FirestoreConnectionState = FirestoreConnectionState.LOCAL_FALLBACK_NO_GOOGLE_SERVICES,
    val message: String = "Verificando conexão com Firebase Firestore...",
    val lastSyncTime: String = "—",
    val syncedMembersCount: Int = 0,
    val syncedPaymentsCount: Int = 0
)

class FirestoreSyncService(
    private val context: Context,
    private val dao: CasaOgumDao
) {
    companion object {
        private const val TAG = "FirestoreSyncService"
        const val COLLECTION_MEMBROS = "membros"
        const val COLLECTION_MENSALIDADES = "mensalidades"
        const val COLLECTION_EVENTOS = "eventos"
        const val COLLECTION_COMUNICADOS = "comunicados"
        const val COLLECTION_TAREFAS = "tarefas"
        const val COLLECTION_HISTORICO = "historico_administrativo"
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var firestore: FirebaseFirestore? = null
    private var membrosListener: ListenerRegistration? = null
    private var mensalidadesListener: ListenerRegistration? = null

    private val _statusInfo = MutableStateFlow(FirestoreStatusInfo())
    val statusInfo: StateFlow<FirestoreStatusInfo> = _statusInfo.asStateFlow()

    init {
        initializeFirestoreSafely()
    }

    private fun initializeFirestoreSafely() {
        try {
            val apps = FirebaseApp.getApps(context)
            val app = if (apps.isNotEmpty()) {
                apps.first()
            } else {
                FirebaseApp.initializeApp(context)
            }

            if (app != null) {
                val db = FirebaseFirestore.getInstance(app)
                val settings = FirebaseFirestoreSettings.Builder()
                    .setPersistenceEnabled(true)
                    .build()
                db.firestoreSettings = settings
                firestore = db
                _statusInfo.value = FirestoreStatusInfo(
                    state = FirestoreConnectionState.CONNECTED,
                    message = "Firebase Firestore ativo (Coleções: 'membros' e 'mensalidades')."
                )
                startRealtimeListeners(db)
            } else {
                _statusInfo.value = FirestoreStatusInfo(
                    state = FirestoreConnectionState.LOCAL_FALLBACK_NO_GOOGLE_SERVICES,
                    message = "SDK Firebase Firestore integrado. Operando com banco local (adicione google-services.json em app/ para sincronizar na nuvem)."
                )
            }
        } catch (e: Exception) {
            Log.w(TAG, "Firestore operando em modo offline/local: ${e.message}")
            _statusInfo.value = FirestoreStatusInfo(
                state = FirestoreConnectionState.LOCAL_FALLBACK_NO_GOOGLE_SERVICES,
                message = "SDK Firebase Firestore configurado. Banco local ativo."
            )
        }
    }

    private fun startRealtimeListeners(db: FirebaseFirestore) {
        try {
            membrosListener?.remove()
            membrosListener = db.collection(COLLECTION_MEMBROS)
                .addSnapshotListener { snapshot, error ->
                    if (error != null || snapshot == null) return@addSnapshotListener
                    scope.launch {
                        var count = 0
                        for (doc in snapshot.documents) {
                            val pessoa = doc.toPessoaEntity() ?: continue
                            val existente = dao.getPessoaByCpf(pessoa.cpf)
                            if (existente == null) {
                                dao.insertPessoa(pessoa)
                            } else {
                                dao.updatePessoa(pessoa.copy(id = existente.id))
                            }
                            count++
                        }
                        _statusInfo.value = _statusInfo.value.copy(
                            state = FirestoreConnectionState.CONNECTED,
                            syncedMembersCount = count
                        )
                    }
                }

            mensalidadesListener?.remove()
            mensalidadesListener = db.collection(COLLECTION_MENSALIDADES)
                .addSnapshotListener { snapshot, error ->
                    if (error != null || snapshot == null) return@addSnapshotListener
                    scope.launch {
                        var count = 0
                        for (doc in snapshot.documents) {
                            val pag = doc.toPagamentoEntity() ?: continue
                            dao.insertPagamento(pag)
                            count++
                        }
                        _statusInfo.value = _statusInfo.value.copy(
                            state = FirestoreConnectionState.CONNECTED,
                            syncedPaymentsCount = count
                        )
                    }
                }
        } catch (e: Exception) {
            Log.w(TAG, "Falha ao registrar listeners do Firestore: ${e.message}")
        }
    }

    fun upsertMembro(pessoa: PessoaEntity) {
        val db = firestore ?: return
        val docId = pessoa.cpf.ifBlank { "membro_${pessoa.id}" }
        val payload = mapOf(
            "id" to pessoa.id,
            "nome" to pessoa.nome,
            "orunko" to pessoa.orunko,
            "cargo" to pessoa.cargo,
            "cpf" to pessoa.cpf,
            "telefone" to pessoa.telefone,
            "endereco" to pessoa.endereco,
            "orixa" to pessoa.orixa,
            "entidades" to pessoa.entidades,
            "dataNascimento" to pessoa.dataNascimento,
            "dataIniciacao" to pessoa.dataIniciacao,
            "data1Ano" to pessoa.data1Ano,
            "data3Anos" to pessoa.data3Anos,
            "data7Anos" to pessoa.data7Anos,
            "data14Anos" to pessoa.data14Anos,
            "data21Anos" to pessoa.data21Anos,
            "observacoes" to pessoa.observacoes,
            "telefonesContatos" to pessoa.telefonesContatos,
            "dataCadastro" to pessoa.dataCadastro,
            "ativo" to pessoa.ativo,
            "ultimoAcesso" to pessoa.ultimoAcesso,
            "perfilAcesso" to pessoa.perfilAcesso,
            "primeiroAcesso" to pessoa.primeiroAcesso,
            "senhaHash" to pessoa.senhaHash,
            "senhaTemporariaDica" to pessoa.senhaTemporariaDica,
            "fotoBase64" to pessoa.fotoBase64,
            "updatedAt" to System.currentTimeMillis()
        )
        db.collection(COLLECTION_MEMBROS)
            .document(docId)
            .set(payload, SetOptions.merge())
            .addOnSuccessListener {
                Log.d(TAG, "Membro ${pessoa.orunko} sincronizado no Firestore.")
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Erro ao sincronizar membro no Firestore: ${e.message}")
            }
    }

    fun upsertMensalidade(pagamento: PagamentoEntity, orunkoMembro: String = "") {
        val db = firestore ?: return
        val docId = if (pagamento.id > 0) {
            "pag_${pagamento.id}"
        } else {
            "pag_${pagamento.pessoaId}_${pagamento.ano}_${pagamento.competenciaMes}"
        }
        val payload = mapOf(
            "id" to pagamento.id,
            "pessoaId" to pagamento.pessoaId,
            "orunkoMembro" to orunkoMembro,
            "competenciaMes" to pagamento.competenciaMes,
            "ano" to pagamento.ano,
            "valor" to pagamento.valor,
            "dataPagamento" to pagamento.dataPagamento,
            "status" to pagamento.status,
            "observacao" to pagamento.observacao,
            "adminResponsavel" to pagamento.adminResponsavel,
            "dataHoraRegistro" to pagamento.dataHoraRegistro,
            "updatedAt" to System.currentTimeMillis()
        )
        db.collection(COLLECTION_MENSALIDADES)
            .document(docId)
            .set(payload, SetOptions.merge())
            .addOnSuccessListener {
                Log.d(TAG, "Mensalidade $docId sincronizada no Firestore.")
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Erro ao sincronizar mensalidade no Firestore: ${e.message}")
            }
    }

    fun upsertEvento(evento: EventoEntity) {
        val db = firestore ?: return
        val docId = if (evento.id > 0) "ev_${evento.id}" else "ev_${System.currentTimeMillis()}"
        val payload = mapOf(
            "id" to evento.id,
            "nome" to evento.nome,
            "data" to evento.data,
            "horario" to evento.horario,
            "local" to evento.local,
            "descricao" to evento.descricao,
            "valorContribuicao" to evento.valorContribuicao,
            "responsaveis" to evento.responsaveis,
            "participantes" to evento.participantes,
            "observacoes" to evento.observacoes
        )
        db.collection(COLLECTION_EVENTOS).document(docId).set(payload, SetOptions.merge())
    }

    fun upsertComunicado(comunicado: ComunicadoEntity) {
        val db = firestore ?: return
        val docId = if (comunicado.id > 0) "com_${comunicado.id}" else "com_${System.currentTimeMillis()}"
        val payload = mapOf(
            "id" to comunicado.id,
            "titulo" to comunicado.titulo,
            "texto" to comunicado.texto,
            "data" to comunicado.data,
            "publicoAlvo" to comunicado.publicoAlvo,
            "destaque" to comunicado.destaque,
            "ativo" to comunicado.ativo
        )
        db.collection(COLLECTION_COMUNICADOS).document(docId).set(payload, SetOptions.merge())
    }

    fun upsertTarefa(tarefa: TarefaEntity) {
        val db = firestore ?: return
        val docId = if (tarefa.id > 0) "tar_${tarefa.id}" else "tar_${System.currentTimeMillis()}"
        val payload = mapOf(
            "id" to tarefa.id,
            "descricao" to tarefa.descricao,
            "responsavelPessoaId" to tarefa.responsavelPessoaId,
            "responsavelOrunko" to tarefa.responsavelOrunko,
            "prazo" to tarefa.prazo,
            "prioridade" to tarefa.prioridade,
            "status" to tarefa.status,
            "observacoes" to tarefa.observacoes
        )
        db.collection(COLLECTION_TAREFAS).document(docId).set(payload, SetOptions.merge())
    }

    fun registrarHistoricoFirestore(historico: HistoricoAdminEntity) {
        val db = firestore ?: return
        val docId = "hist_${System.currentTimeMillis()}"
        val payload = mapOf(
            "acao" to historico.acao,
            "detalhes" to historico.detalhes,
            "adminNome" to historico.adminNome,
            "dataHora" to historico.dataHora,
            "categoria" to historico.categoria
        )
        db.collection(COLLECTION_HISTORICO).document(docId).set(payload, SetOptions.merge())
    }

    fun sincronizarTudoParaFirestore(
        pessoas: List<PessoaEntity>,
        pagamentos: List<PagamentoEntity>,
        horaAtual: String
    ): String {
        val db = firestore
        if (db == null) {
            _statusInfo.value = _statusInfo.value.copy(
                lastSyncTime = horaAtual,
                syncedMembersCount = pessoas.size,
                syncedPaymentsCount = pagamentos.size,
                message = "Dados salvos localmente (${pessoas.size} membros, ${pagamentos.size} mensalidades). Adicione google-services.json para enviar à nuvem Firestore."
            )
            return _statusInfo.value.message
        }

        _statusInfo.value = _statusInfo.value.copy(
            state = FirestoreConnectionState.SYNCING,
            message = "Sincronizando ${pessoas.size} membros e ${pagamentos.size} mensalidades com o Firebase Firestore..."
        )
        val mapaOrunko = pessoas.associateBy({ it.id }, { it.orunko })
        pessoas.forEach { upsertMembro(it) }
        pagamentos.forEach { pag -> upsertMensalidade(pag, mapaOrunko[pag.pessoaId] ?: "") }

        _statusInfo.value = FirestoreStatusInfo(
            state = FirestoreConnectionState.CONNECTED,
            message = "Sincronizado com Firebase Firestore (${pessoas.size} membros e ${pagamentos.size} mensalidades).",
            lastSyncTime = horaAtual,
            syncedMembersCount = pessoas.size,
            syncedPaymentsCount = pagamentos.size
        )
        return _statusInfo.value.message
    }

    private fun com.google.firebase.firestore.DocumentSnapshot.toPessoaEntity(): PessoaEntity? {
        val cpfVal = getString("cpf") ?: return null
        val orunkoVal = getString("orunko") ?: return null
        return PessoaEntity(
            id = getLong("id") ?: 0L,
            nome = getString("nome") ?: "",
            orunko = orunkoVal,
            cargo = getString("cargo") ?: "Membro",
            cpf = cpfVal,
            telefone = getString("telefone") ?: "",
            endereco = getString("endereco") ?: "",
            orixa = getString("orixa") ?: "",
            entidades = getString("entidades") ?: "",
            dataNascimento = getString("dataNascimento") ?: "",
            dataIniciacao = getString("dataIniciacao") ?: "",
            data1Ano = getString("data1Ano") ?: "",
            data3Anos = getString("data3Anos") ?: "",
            data7Anos = getString("data7Anos") ?: "",
            data14Anos = getString("data14Anos") ?: "",
            data21Anos = getString("data21Anos") ?: "",
            observacoes = getString("observacoes") ?: "",
            telefonesContatos = getString("telefonesContatos") ?: "",
            dataCadastro = getString("dataCadastro") ?: "",
            ativo = getBoolean("ativo") ?: true,
            ultimoAcesso = getString("ultimoAcesso") ?: "",
            perfilAcesso = getString("perfilAcesso") ?: "MEMBRO",
            primeiroAcesso = getBoolean("primeiroAcesso") ?: false,
            senhaHash = getString("senhaHash") ?: "",
            senhaTemporariaDica = getString("senhaTemporariaDica") ?: "",
            fotoBase64 = getString("fotoBase64") ?: ""
        )
    }

    fun carregarMembrosDoFirestore() {
        val db = firestore ?: return
        db.collection(COLLECTION_MEMBROS)
            .get()
            .addOnSuccessListener { snapshot ->
                scope.launch {
                    var count = 0
                    for (doc in snapshot.documents) {
                        val pessoa = doc.toPessoaEntity() ?: continue
                        val existente = dao.getPessoaByCpf(pessoa.cpf)
                        if (existente == null) {
                            dao.insertPessoa(pessoa)
                        } else {
                            dao.updatePessoa(pessoa.copy(id = existente.id))
                        }
                        count++
                    }
                    _statusInfo.value = _statusInfo.value.copy(
                        state = FirestoreConnectionState.CONNECTED,
                        syncedMembersCount = count,
                        message = "Lista de membros sincronizada com o Firestore ($count registros)."
                    )
                }
            }
    }

    private fun com.google.firebase.firestore.DocumentSnapshot.toPagamentoEntity(): PagamentoEntity? {
        val pid = getLong("pessoaId") ?: return null
        val mes = getLong("competenciaMes")?.toInt() ?: return null
        val anoVal = getLong("ano")?.toInt() ?: return null
        return PagamentoEntity(
            id = getLong("id") ?: 0L,
            pessoaId = pid,
            competenciaMes = mes,
            ano = anoVal,
            valor = getDouble("valor") ?: 150.0,
            dataPagamento = getString("dataPagamento") ?: "",
            status = getString("status") ?: "PENDENTE",
            observacao = getString("observacao") ?: "",
            adminResponsavel = getString("adminResponsavel") ?: "",
            dataHoraRegistro = getString("dataHoraRegistro") ?: ""
        )
    }
}
