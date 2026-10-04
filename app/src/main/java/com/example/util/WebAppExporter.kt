package com.example.util

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import com.example.R
import com.example.data.ComunicadoEntity
import com.example.data.EventoEntity
import com.example.data.HistoricoAdminEntity
import com.example.data.PagamentoEntity
import com.example.data.PessoaEntity
import com.example.data.TarefaEntity
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.File

object WebAppExporter {

    private var cachedCrestDataUri: String? = null

    fun getOfficialCrestDataUri(context: Context): String {
        cachedCrestDataUri?.let { return it }
        return try {
            val bitmap = BitmapFactory.decodeResource(context.resources, R.drawable.img_logo_oficial)
            if (bitmap != null) {
                val scaled = Bitmap.createScaledBitmap(bitmap, 260, 260, true)
                val out = ByteArrayOutputStream()
                scaled.compress(Bitmap.CompressFormat.JPEG, 88, out)
                val b64 = Base64.encodeToString(out.toByteArray(), Base64.NO_WRAP)
                val uri = "data:image/jpeg;base64,$b64"
                cachedCrestDataUri = uri
                uri
            } else {
                ""
            }
        } catch (_: Exception) {
            ""
        }
    }

    fun buildDatabaseJson(
        pessoas: List<PessoaEntity>,
        pagamentos: List<PagamentoEntity>,
        eventos: List<EventoEntity>,
        comunicados: List<ComunicadoEntity>,
        tarefas: List<TarefaEntity>,
        historico: List<HistoricoAdminEntity>
    ): String {
        val root = JSONObject()

        val pessoasArr = JSONArray()
        pessoas.forEach { p ->
            val obj = JSONObject()
            obj.put("id", p.id)
            obj.put("nome", p.nome)
            obj.put("orunko", p.orunko)
            obj.put("cargo", p.cargo)
            obj.put("cpf", p.cpf)
            obj.put("telefone", p.telefone)
            obj.put("endereco", p.endereco)
            obj.put("orixa", p.orixa)
            obj.put("entidades", p.entidades)
            obj.put("dataNascimento", p.dataNascimento)
            obj.put("dataIniciacao", p.dataIniciacao)
            obj.put("data1Ano", p.data1Ano)
            obj.put("data3Anos", p.data3Anos)
            obj.put("data7Anos", p.data7Anos)
            obj.put("data14Anos", p.data14Anos)
            obj.put("data21Anos", p.data21Anos)
            obj.put("observacoes", p.observacoes)
            obj.put("telefonesContatos", p.telefonesContatos)
            obj.put("dataCadastro", p.dataCadastro)
            obj.put("ativo", p.ativo)
            obj.put("ultimoAcesso", p.ultimoAcesso)
            obj.put("perfilAcesso", p.perfilAcesso)
            obj.put("primeiroAcesso", p.primeiroAcesso)
            obj.put("pinCode", if (p.cpf == "05692005719") "8691" else p.senhaTemporariaDica)
            obj.put("senhaHash", p.senhaHash)
            obj.put("senhaTemporariaDica", p.senhaTemporariaDica)
            obj.put("fotoBase64", p.fotoBase64)
            pessoasArr.put(obj)
        }
        root.put("pessoas", pessoasArr)

        val pagamentosArr = JSONArray()
        pagamentos.forEach { pg ->
            val obj = JSONObject()
            obj.put("id", pg.id)
            obj.put("pessoaId", pg.pessoaId)
            obj.put("competenciaMes", pg.competenciaMes)
            obj.put("ano", pg.ano)
            obj.put("valor", pg.valor)
            obj.put("dataPagamento", pg.dataPagamento)
            obj.put("status", pg.status)
            obj.put("observacao", pg.observacao)
            obj.put("adminResponsavel", pg.adminResponsavel)
            obj.put("dataHoraRegistro", pg.dataHoraRegistro)
            pagamentosArr.put(obj)
        }
        root.put("pagamentos", pagamentosArr)

        val eventosArr = JSONArray()
        eventos.forEach { ev ->
            val obj = JSONObject()
            obj.put("id", ev.id)
            obj.put("titulo", ev.nome)
            obj.put("data", ev.data)
            obj.put("horario", ev.horario)
            obj.put("tipo", ev.local)
            obj.put("descricao", ev.descricao)
            obj.put("orunkoResponsavel", ev.responsaveis)
            obj.put("observacoes", ev.observacoes)
            eventosArr.put(obj)
        }
        root.put("eventos", eventosArr)

        val comunicadosArr = JSONArray()
        comunicados.forEach { c ->
            val obj = JSONObject()
            obj.put("id", c.id)
            obj.put("titulo", c.titulo)
            obj.put("texto", c.texto)
            obj.put("data", c.data)
            obj.put("publicoAlvo", c.publicoAlvo)
            obj.put("destaque", c.destaque)
            obj.put("ativo", c.ativo)
            comunicadosArr.put(obj)
        }
        root.put("comunicados", comunicadosArr)

        val tarefasArr = JSONArray()
        tarefas.forEach { t ->
            val obj = JSONObject()
            obj.put("id", t.id)
            obj.put("titulo", t.descricao)
            obj.put("descricao", t.descricao)
            obj.put("eventoVinculado", "")
            obj.put("orunkoResponsavel", t.responsavelOrunko)
            obj.put("prazo", t.prazo)
            obj.put("prioridade", t.prioridade)
            obj.put("status", t.status)
            obj.put("observacao", t.observacoes)
            tarefasArr.put(obj)
        }
        root.put("tarefas", tarefasArr)

        val historicoArr = JSONArray()
        historico.forEach { h ->
            val obj = JSONObject()
            obj.put("id", h.id)
            obj.put("acao", h.acao)
            obj.put("detalhes", h.detalhes)
            obj.put("adminNome", h.adminNome)
            obj.put("dataHora", h.dataHora)
            obj.put("categoria", h.categoria)
            historicoArr.put(obj)
        }
        root.put("historico", historicoArr)

        return root.toString()
    }

    fun buildStandaloneWebAppHtml(
        context: Context,
        pessoas: List<PessoaEntity>,
        pagamentos: List<PagamentoEntity>,
        eventos: List<EventoEntity>,
        comunicados: List<ComunicadoEntity>,
        tarefas: List<TarefaEntity>,
        historico: List<HistoricoAdminEntity>
    ): String {
        val rawTemplate = try {
            context.assets.open("casa_do_ogum_web.html").bufferedReader(Charsets.UTF_8).use { it.readText() }
        } catch (e: Exception) {
            "<html><body><h1>Erro ao carregar template web: ${e.localizedMessage}</h1></body></html>"
        }

        val crestUri = getOfficialCrestDataUri(context)
        val dbJson = buildDatabaseJson(pessoas, pagamentos, eventos, comunicados, tarefas, historico)

        return rawTemplate
            .replace("__CREST_DATA_URI_PLACEHOLDER__", crestUri)
            .replace("/*__INITIAL_DATA_START__*/ null /*__INITIAL_DATA_END__*/", dbJson)
    }

    fun exportAndShareWebAppFile(
        context: Context,
        pessoas: List<PessoaEntity>,
        pagamentos: List<PagamentoEntity>,
        eventos: List<EventoEntity>,
        comunicados: List<ComunicadoEntity>,
        tarefas: List<TarefaEntity>,
        historico: List<HistoricoAdminEntity>
    ): ExportResult {
        return try {
            val htmlContent = buildStandaloneWebAppHtml(
                context = context,
                pessoas = pessoas,
                pagamentos = pagamentos,
                eventos = eventos,
                comunicados = comunicados,
                tarefas = tarefas,
                historico = historico
            )
            val fileName = "Casa_do_Ogum_RJ_Navegador_Web.html"
            val dir = File(context.cacheDir, "web_export").apply { mkdirs() }
            val file = File(dir, fileName)
            file.writeText(htmlContent, Charsets.UTF_8)

            try {
                val sendIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/html"
                    putExtra(Intent.EXTRA_SUBJECT, "CASA DO OGUM - RJ — Sistema Web (Chrome, Safari, Edge, IE)")
                    putExtra(
                        Intent.EXTRA_TEXT,
                        "Sistema completo da Casa do Ogum - RJ para abrir diretamente em qualquer navegador (Google Chrome, Safari, Microsoft Edge, Firefox ou Internet Explorer)."
                    )
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                val chooser = Intent.createChooser(sendIntent, "Salvar ou Enviar Sistema Web (.html)").apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(chooser)
            } catch (_: Exception) {
            }

            ExportResult(
                success = true,
                filePath = file.absolutePath,
                fileName = fileName,
                previewContent = "SISTEMA WEB PARA NAVEGADORES GERADO COM SUCESSO!\n\n" +
                    "• Arquivo: $fileName\n" +
                    "• Navegadores suportados: Google Chrome, Apple Safari (iPhone, iPad e Mac), Microsoft Edge, Mozilla Firefox e Internet Explorer.\n" +
                    "• Brasão Oficial da Casa do Ogum - RJ embutido em alta definição.\n" +
                    "• Membros e fotos sincronizados no pacote: ${pessoas.size} cadastro(s).\n\n" +
                    "COMO ABRIR NO COMPUTADOR, TABLET OU CELULAR:\n" +
                    "1. Envie ou salve o arquivo '$fileName' (ou abra a pasta /web/index.html ao baixar o ZIP do projeto).\n" +
                    "2. Dê dois cliques no arquivo para abrir direto no Google Chrome, Safari, Edge ou outro navegador.\n" +
                    "3. Login do Administrador: CPF 05692005719 | Senha (4 números): 8691",
                message = "Arquivo Web '$fileName' gerado para Chrome, Safari, Edge e outros navegadores!"
            )
        } catch (e: Exception) {
            ExportResult(
                success = false,
                filePath = "",
                fileName = "",
                previewContent = "",
                message = "Erro ao exportar arquivo Web: ${e.localizedMessage}"
            )
        }
    }
}
