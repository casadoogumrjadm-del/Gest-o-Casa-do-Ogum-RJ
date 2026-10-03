package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface CasaOgumDao {

    // PESSOAS & USUÁRIOS
    @Query("SELECT * FROM pessoas ORDER BY orunko ASC")
    fun getAllPessoas(): Flow<List<PessoaEntity>>

    @Query("SELECT * FROM pessoas WHERE cpf = :cpf LIMIT 1")
    suspend fun getPessoaByCpf(cpf: String): PessoaEntity?

    @Query("SELECT * FROM pessoas WHERE id = :id LIMIT 1")
    suspend fun getPessoaById(id: Long): PessoaEntity?

    @Query("SELECT COUNT(*) FROM pessoas")
    suspend fun countPessoas(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPessoa(pessoa: PessoaEntity): Long

    @Update
    suspend fun updatePessoa(pessoa: PessoaEntity)

    @Query("DELETE FROM pessoas WHERE id = :id")
    suspend fun deletePessoa(id: Long)

    // PAGAMENTOS
    @Query("SELECT * FROM pagamentos ORDER BY ano DESC, competenciaMes DESC, id DESC")
    fun getAllPagamentos(): Flow<List<PagamentoEntity>>

    @Query("SELECT * FROM pagamentos WHERE pessoaId = :pessoaId ORDER BY ano DESC, competenciaMes DESC")
    fun getPagamentosByPessoa(pessoaId: Long): Flow<List<PagamentoEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPagamento(pagamento: PagamentoEntity): Long

    @Update
    suspend fun updatePagamento(pagamento: PagamentoEntity)

    @Query("DELETE FROM pagamentos WHERE id = :id")
    suspend fun deletePagamento(id: Long)

    // EVENTOS
    @Query("SELECT * FROM eventos ORDER BY id ASC")
    fun getAllEventos(): Flow<List<EventoEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvento(evento: EventoEntity): Long

    @Update
    suspend fun updateEvento(evento: EventoEntity)

    @Query("DELETE FROM eventos WHERE id = :id")
    suspend fun deleteEvento(id: Long)

    // COMUNICADOS
    @Query("SELECT * FROM comunicados ORDER BY destaque DESC, id DESC")
    fun getAllComunicados(): Flow<List<ComunicadoEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertComunicado(comunicado: ComunicadoEntity): Long

    @Update
    suspend fun updateComunicado(comunicado: ComunicadoEntity)

    @Query("DELETE FROM comunicados WHERE id = :id")
    suspend fun deleteComunicado(id: Long)

    // TAREFAS
    @Query("SELECT * FROM tarefas ORDER BY CASE prioridade WHEN 'Urgente' THEN 1 WHEN 'Alta' THEN 2 WHEN 'Normal' THEN 3 ELSE 4 END, id DESC")
    fun getAllTarefas(): Flow<List<TarefaEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTarefa(tarefa: TarefaEntity): Long

    @Update
    suspend fun updateTarefa(tarefa: TarefaEntity)

    @Query("DELETE FROM tarefas WHERE id = :id")
    suspend fun deleteTarefa(id: Long)

    // HISTÓRICO ADMINISTRATIVO
    @Query("SELECT * FROM historico_administrativo ORDER BY id DESC")
    fun getAllHistorico(): Flow<List<HistoricoAdminEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistorico(historico: HistoricoAdminEntity): Long

    // CONFIGURAÇÕES
    @Query("SELECT * FROM configuracoes")
    fun getAllConfiguracoes(): Flow<List<ConfiguracaoEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun setConfiguracao(config: ConfiguracaoEntity)
}
