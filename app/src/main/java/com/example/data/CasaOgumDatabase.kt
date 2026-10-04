package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.util.DateAndCalendarUtils
import com.example.util.SecurityUtils

@Database(
    entities = [
        PessoaEntity::class,
        PagamentoEntity::class,
        EventoEntity::class,
        ComunicadoEntity::class,
        TarefaEntity::class,
        HistoricoAdminEntity::class,
        ConfiguracaoEntity::class
    ],
    version = 4,
    exportSchema = false
)
abstract class CasaOgumDatabase : RoomDatabase() {
    abstract fun dao(): CasaOgumDao

    companion object {
        const val ADMIN_OFICIAL_CPF = "05692005719"
        const val ADMIN_OFICIAL_SENHA_INICIAL = "8691"

        @Volatile
        private var INSTANCE: CasaOgumDatabase? = null

        fun getInstance(context: Context): CasaOgumDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    CasaOgumDatabase::class.java,
                    "casa_do_ogum_rj.db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }

        suspend fun seedIfEmpty(dao: CasaOgumDao) {
            // Garante que a conta oficial do Administrador (CPF 05692005719 / Senha 8691) esteja sempre pronta
            val adminExistente = dao.getPessoaByCpf(ADMIN_OFICIAL_CPF)
            if (adminExistente == null) {
                dao.insertPessoa(
                    PessoaEntity(
                        nome = "Administrador — Casa do Ogum - RJ",
                        orunko = "Administração Casa do Ogum",
                        cargo = "Administrador Geral",
                        cpf = ADMIN_OFICIAL_CPF,
                        telefone = "",
                        endereco = "Rio de Janeiro - RJ",
                        orixa = "Ogum",
                        entidades = "",
                        dataNascimento = "03/10/1980",
                        dataIniciacao = "03/10/2016",
                        data1Ano = "03/10/2017",
                        data3Anos = "03/10/2019",
                        data7Anos = "03/10/2023",
                        data14Anos = "03/10/2030",
                        data21Anos = "03/10/2037",
                        observacoes = "Conta oficial de Administração Geral da Casa do Ogum - RJ.",
                        telefonesContatos = "",
                        dataCadastro = DateAndCalendarUtils.currentDateFormatted(),
                        ativo = true,
                        ultimoAcesso = "Pronto para acesso",
                        perfilAcesso = "ADMIN",
                        primeiroAcesso = false,
                        senhaHash = SecurityUtils.hashPassword(ADMIN_OFICIAL_SENHA_INICIAL),
                        senhaTemporariaDica = ""
                    )
                )

                dao.insertComunicado(
                    ComunicadoEntity(
                        titulo = "Bem-vindos ao Sistema Oficial da Casa do Ogum - RJ",
                        texto = "Sistema institucional ativo. Todos os calendários coletivos (Aniversariantes e Odún Kodún) respeitam rigorosamente a Regra do Orunkó, preservando o sigilo do nome civil.",
                        data = DateAndCalendarUtils.currentDateFormatted(),
                        publicoAlvo = "Todos",
                        destaque = true,
                        ativo = true
                    )
                )

                dao.insertHistorico(
                    HistoricoAdminEntity(
                        acao = "Sistema inicializado para produção",
                        detalhes = "Conta oficial de Administrador configurada e pronta para uso (CPF final 57-19).",
                        adminNome = "Sistema Casa do Ogum - RJ",
                        dataHora = DateAndCalendarUtils.currentDateTimeFormatted(),
                        categoria = "CONFIG"
                    )
                )

                dao.setConfiguracao(ConfiguracaoEntity("VALOR_MENSALIDADE_PADRAO", "150.00"))
                dao.setConfiguracao(ConfiguracaoEntity("DIA_VENCIMENTO_PADRAO", "10"))
                dao.setConfiguracao(ConfiguracaoEntity("REGRA_ORUNKO_ESTRITA", "ATIVA"))
                dao.setConfiguracao(ConfiguracaoEntity("CASA_NOME_OFICIAL", "CASA DO OGUM - RJ"))
            }
        }
    }
}
