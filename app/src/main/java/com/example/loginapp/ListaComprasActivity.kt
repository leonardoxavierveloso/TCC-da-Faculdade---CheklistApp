package com.example.loginapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class ListaComprasActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ListaAdapter
    private lateinit var btnAdicionar: ImageButton
    private lateinit var btnSair: Button
    private lateinit var txtUsuario: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lista_compras)

        // Obter o nome do usuário da tela de login
        val nomeUsuario = intent.getStringExtra("NOME_USUARIO") ?: "Leonardo"

        // Configurar a saudação com o nome do usuário
        txtUsuario = findViewById(R.id.txtUsuario)
        txtUsuario.text = "Olá, $nomeUsuario"

        // Configurar botão de adicionar
        btnAdicionar = findViewById(R.id.btnAdicionar)
        btnAdicionar.setOnClickListener {
            val intent = Intent(this, CriarListaActivity::class.java)
            intent.putExtra("NOME_USUARIO", nomeUsuario)
            startActivity(intent)
        }

        // Configurar botão de sair
        btnSair = findViewById(R.id.btnSair)
        btnSair.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        // Configurar RecyclerView para as listas
        setupRecyclerView()
    }

    private fun setupRecyclerView() {
        recyclerView = findViewById(R.id.recyclerViewListas)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Carregar listas salvas
        val listasPessoais = ListaManager.carregarListasPessoais(this)
        val listasCompartilhadas = ListaManager.carregarListasCompartilhadas(this)

        // Se não tiver listas salvas, usar exemplos
        val minhasListas = if (listasPessoais.isNotEmpty()) {
            listasPessoais
        } else {
            listOf(
                ListaCompras("Mercado", emptyList(), false, true),
                ListaCompras("Farmácia", emptyList(), false, true),
                ListaCompras("Churrasco", emptyList(), false, true)
            )
        }

        val listasCompart = if (listasCompartilhadas.isNotEmpty()) {
            listasCompartilhadas
        } else {
            listOf(
                ListaCompras("Mercado", emptyList(), true, true),
                ListaCompras("Farmácia", emptyList(), true, true)
            )
        }

        adapter = ListaAdapter(minhasListas, listasCompart)
        recyclerView.adapter = adapter
    }

    override fun onResume() {
        super.onResume()
        // Recarregar as listas quando voltar para esta tela
        setupRecyclerView()
    }
}

// Adapter para o RecyclerView
class ListaAdapter(
    private val minhasListas: List<ListaCompras>,
    private val listasCompartilhadas: List<ListaCompras>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val TIPO_MINHAS_LISTAS = 0
        private const val TIPO_LISTAS_COMPARTILHADAS = 1
        private const val TIPO_TITULO = 2
    }

    override fun getItemViewType(position: Int): Int {
        return when {
            position == 0 -> TIPO_TITULO
            position <= minhasListas.size -> TIPO_MINHAS_LISTAS
            position == minhasListas.size + 1 -> TIPO_TITULO
            else -> TIPO_LISTAS_COMPARTILHADAS
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)

        return when (viewType) {
            TIPO_TITULO -> {
                val view = inflater.inflate(R.layout.item_titulo_lista, parent, false)
                TituloViewHolder(view)
            }
            TIPO_MINHAS_LISTAS -> {
                val view = inflater.inflate(R.layout.item_lista, parent, false)
                ListaViewHolder(view)
            }
            else -> {
                val view = inflater.inflate(R.layout.item_lista, parent, false)
                ListaViewHolder(view)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is TituloViewHolder -> {
                val titulo = if (position == 0) "Minhas listas de compras:" else "Listas compartilhadas:"
                holder.bind(titulo)
            }
            is ListaViewHolder -> {
                val lista = if (position <= minhasListas.size) {
                    minhasListas[position - 1]
                } else {
                    listasCompartilhadas[position - minhasListas.size - 2]
                }
                holder.bind(lista)
            }
        }
    }

    override fun getItemCount(): Int {
        return 2 + minhasListas.size + listasCompartilhadas.size // 2 títulos + itens
    }

    class TituloViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val titulo: TextView = itemView.findViewById(R.id.titulo)

        fun bind(tituloTexto: String) {
            titulo.text = tituloTexto
        }
    }

    class ListaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nomeLista: TextView = itemView.findViewById(R.id.nomeLista)
        private val iconeConcluido: TextView = itemView.findViewById(R.id.iconeConcluido)

        fun bind(lista: ListaCompras) {
            nomeLista.text = lista.nome
            iconeConcluido.visibility = if (lista.concluida) View.VISIBLE else View.GONE

            // Clique para abrir detalhes da lista
            itemView.setOnClickListener {
                val context = itemView.context
                val intent = Intent(context, DetalhesListaActivity::class.java)
                intent.putExtra("NOME_USUARIO", "Leonardo")
                intent.putExtra("NOME_LISTA", lista.nome)
                context.startActivity(intent)
            }
        }
    }
}