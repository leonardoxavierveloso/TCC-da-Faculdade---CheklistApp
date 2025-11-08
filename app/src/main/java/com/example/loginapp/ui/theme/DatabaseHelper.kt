package com.example.loginapp.ui.theme
import android.content.Context
import com.example.loginapp.ItemLista
import com.example.loginapp.ListaCompras
import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException

class DatabaseHelper(private val context: Context) {

    private var connection: Connection? = null

    init {
        initDatabase()
        createTables()
    }

    fun deletarListaPorNome(nomeLista: String): Boolean {
        try {
            // Primeiro deleta os itens (por causa da foreign key)
            val sqlDeleteItens = "DELETE FROM itens_lista WHERE lista_id IN (SELECT id FROM listas_compras WHERE nome = ?)"
            val sqlDeleteLista = "DELETE FROM listas_compras WHERE nome = ?"

            connection?.prepareStatement(sqlDeleteItens)?.use { stmt ->
                stmt.setString(1, nomeLista)
                stmt.executeUpdate()
            }

            connection?.prepareStatement(sqlDeleteLista)?.use { stmt ->
                stmt.setString(1, nomeLista)
                return stmt.executeUpdate() > 0
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return false
    }

    private fun initDatabase() {
        try {
            // O H2 for Android cria o banco no diretório de dados do app
            val dbPath = context.getDatabasePath("mercado_db").absolutePath
            val url = "jdbc:h2:file:$dbPath;DB_CLOSE_DELAY=-1"

            Class.forName("org.h2.Driver")
            connection = DriverManager.getConnection(url, "sa", "")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun createTables() {
        // Tabela para listas de compras
        val sqlListas = """
            CREATE TABLE IF NOT EXISTS listas_compras (
                id INTEGER PRIMARY KEY AUTO_INCREMENT,
                nome VARCHAR(255) NOT NULL,
                compartilhada BOOLEAN DEFAULT FALSE,
                concluida BOOLEAN DEFAULT FALSE
            )
        """

        // Tabela para itens da lista
        val sqlItens = """
            CREATE TABLE IF NOT EXISTS itens_lista (
                id INTEGER PRIMARY KEY AUTO_INCREMENT,
                lista_id INTEGER NOT NULL,
                nome VARCHAR(255) NOT NULL,
                quantidade VARCHAR(50) NOT NULL,
                concluido BOOLEAN DEFAULT FALSE,
                FOREIGN KEY (lista_id) REFERENCES listas_compras(id) ON DELETE CASCADE
            )
        """

        connection?.createStatement()?.execute(sqlListas)
        connection?.createStatement()?.execute(sqlItens)
    }

    // === OPERAÇÕES PARA LISTAS ===

    fun salvarLista(lista: ListaCompras): Long {
        val sql = "INSERT INTO listas_compras (nome, compartilhada, concluida) VALUES (?, ?, ?)"

        connection?.prepareStatement(sql, java.sql.Statement.RETURN_GENERATED_KEYS)?.use { stmt ->
            stmt.setString(1, lista.nome)
            stmt.setBoolean(2, lista.compartilhada)
            stmt.setBoolean(3, lista.concluida)
            stmt.executeUpdate()

            val keys = stmt.generatedKeys
            if (keys.next()) {
                val listaId = keys.getLong(1)
                // Salva os itens da lista
                salvarItensLista(listaId, lista.itens)
                return listaId
            }
        }
        return -1
    }

    private fun salvarItensLista(listaId: Long, itens: List<ItemLista>) {
        val sql = "INSERT INTO itens_lista (lista_id, nome, quantidade, concluido) VALUES (?, ?, ?, ?)"

        connection?.prepareStatement(sql)?.use { stmt ->
            for (item in itens) {
                stmt.setLong(1, listaId)
                stmt.setString(2, item.nome)
                stmt.setString(3, item.quantidade)
                stmt.setBoolean(4, item.concluido)
                stmt.addBatch()
            }
            stmt.executeBatch()
        }
    }

    fun carregarTodasListas(): List<ListaCompras> {
        val listas = mutableListOf<ListaCompras>()
        val sql = "SELECT * FROM listas_compras"

        connection?.createStatement()?.use { stmt ->
            val rs = stmt.executeQuery(sql)
            while (rs.next()) {
                val listaId = rs.getLong("id")
                val itens = carregarItensPorLista(listaId)

                listas.add(
                    ListaCompras(
                        nome = rs.getString("nome"),
                        itens = itens,
                        compartilhada = rs.getBoolean("compartilhada"),
                        concluida = rs.getBoolean("concluida")
                    )
                )
            }
        }
        return listas
    }

    fun carregarListasPessoais(): List<ListaCompras> {
        return carregarTodasListas().filter { !it.compartilhada }
    }

    fun carregarListasCompartilhadas(): List<ListaCompras> {
        return carregarTodasListas().filter { it.compartilhada }
    }

    // === OPERAÇÕES PARA ITENS ===

    private fun carregarItensPorLista(listaId: Long): List<ItemLista> {
        val itens = mutableListOf<ItemLista>()
        val sql = "SELECT * FROM itens_lista WHERE lista_id = ?"

        connection?.prepareStatement(sql)?.use { stmt ->
            stmt.setLong(1, listaId)
            val rs = stmt.executeQuery()

            while (rs.next()) {
                itens.add(
                    ItemLista(
                        nome = rs.getString("nome"),
                        quantidade = rs.getString("quantidade"),
                        concluido = rs.getBoolean("concluido")
                    )
                )
            }
        }
        return itens
    }

    fun atualizarItemLista(listaId: Long, item: ItemLista): Boolean {
        val sql = "UPDATE itens_lista SET concluido = ? WHERE lista_id = ? AND nome = ? AND quantidade = ?"

        connection?.prepareStatement(sql)?.use { stmt ->
            stmt.setBoolean(1, item.concluido)
            stmt.setLong(2, listaId)
            stmt.setString(3, item.nome)
            stmt.setString(4, item.quantidade)
            return stmt.executeUpdate() > 0
        }
        return false
    }

    fun deletarLista(nomeLista: String): Boolean {
        // Primeiro deleta os itens (por causa da foreign key)
        val sqlDeleteItens = "DELETE FROM itens_lista WHERE lista_id IN (SELECT id FROM listas_compras WHERE nome = ?)"
        val sqlDeleteLista = "DELETE FROM listas_compras WHERE nome = ?"

        connection?.prepareStatement(sqlDeleteItens)?.use { stmt ->
            stmt.setString(1, nomeLista)
            stmt.executeUpdate()
        }

        connection?.prepareStatement(sqlDeleteLista)?.use { stmt ->
            stmt.setString(1, nomeLista)
            return stmt.executeUpdate() > 0
        }
        return false
    }

    fun close() {
        connection?.close()
    }
}