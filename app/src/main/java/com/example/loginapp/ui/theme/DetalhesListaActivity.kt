package com.example.loginapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class DetalhesListaActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ItemDetalhesAdapter
    private lateinit var txtSaudacao: TextView
    private lateinit var txtTituloLista: TextView
    private lateinit var btnVoltar: Button
    private lateinit var btnCompartilhar: Button

    private var listaSelecionada: ListaCompras? = null
    private var nomeListaSelecionada: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detalhes_lista)

        initViews()
        setupRecyclerView()
        setupClickListeners()

        // Carregar dados da lista selecionada
        carregarDadosLista()
    }

    private fun initViews() {
        txtSaudacao = findViewById(R.id.txtSaudacao)
        txtTituloLista = findViewById(R.id.txtTituloLista)
        btnVoltar = findViewById(R.id.btnVoltar)
        btnCompartilhar = findViewById(R.id.btnCompartilhar)
        recyclerView = findViewById(R.id.recyclerViewItens)
    }

    private fun carregarDadosLista() {
        // Obter dados da intent
        val nomeUsuario = intent.getStringExtra("NOME_USUARIO") ?: "Leonardo"
        nomeListaSelecionada = intent.getStringExtra("NOME_LISTA") ?: "Lista de Itens"

        txtSaudacao.text = "Olá, $nomeUsuario"
        txtTituloLista.text = nomeListaSelecionada

        // Buscar lista completa do storage
        val todasListas = ListaManager.carregarListas(this)
        listaSelecionada = todasListas.find { it.nome == nomeListaSelecionada }

        if (listaSelecionada != null) {
            // Carregar itens da lista salva
            adapter.atualizarItens(listaSelecionada!!.itens)
            Toast.makeText(this, "Carregando lista: ${listaSelecionada!!.nome}", Toast.LENGTH_SHORT).show()
        } else {
            // Se não encontrou, usar dados de exemplo
            adapter.atualizarItens(getItensExemplo())
            Toast.makeText(this, "Lista não encontrada - Mostrando exemplo", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getItensExemplo(): List<ItemLista> {
        return listOf(
            ItemLista("Maçã", "6 um", false),
            ItemLista("Arroz", "2 pct", false),
            ItemLista("Feijão", "4 pct", false),
            ItemLista("Carne de franco", "2 kg", false)
        )
    }

    private fun setupRecyclerView() {
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = ItemDetalhesAdapter(emptyList()) { item, isChecked ->
            // Callback quando um item é marcado/desmarcado
            val status = if (isChecked) "marcado" else "desmarcado"
            Toast.makeText(this, "${item.nome} $status", Toast.LENGTH_SHORT).show()

            // Atualizar o estado do item na lista
            atualizarItemLista(item.nome, isChecked)
        }
        recyclerView.adapter = adapter
    }

    private fun atualizarItemLista(nomeItem: String, concluido: Boolean) {
        // Buscar a lista novamente
        val todasListas = ListaManager.carregarListas(this).toMutableList()
        val listaIndex = todasListas.indexOfFirst { it.nome == nomeListaSelecionada }

        if (listaIndex != -1) {
            // Atualizar o item específico
            val lista = todasListas[listaIndex]
            val novosItens = lista.itens.map { item ->
                if (item.nome == nomeItem) {
                    item.copy(concluido = concluido)
                } else {
                    item
                }
            }

            // Atualizar a lista completa
            val listaAtualizada = lista.copy(itens = novosItens)
            todasListas[listaIndex] = listaAtualizada

            // Salvar de volta
            salvarListasAtualizadas(todasListas)
        }
    }

    private fun salvarListasAtualizadas(listas: List<ListaCompras>) {
        val sharedPref = getSharedPreferences("listas_prefs", MODE_PRIVATE)
        val editor = sharedPref.edit()
        val gson = com.google.gson.Gson()
        val json = gson.toJson(listas)
        editor.putString("listas_salvas", json)
        editor.apply()
    }

    private fun setupClickListeners() {
        btnVoltar.setOnClickListener {
            finish() // Volta para a tela anterior
        }

        btnCompartilhar.setOnClickListener {
            compartilharLista()
        }
    }

    private fun compartilharLista() {
        val nomeLista = txtTituloLista.text.toString()

        if (listaSelecionada != null) {
            val itensTexto = listaSelecionada!!.itens.joinToString("\n") { item ->
                "${if (item.concluido) "☒" else "☐"} ${item.nome} - ${item.quantidade}"
            }

            val textoCompartilhar = """
                📋 Lista: $nomeLista
                
                $itensTexto
                
                Criado no CheckList App 📱
            """.trimIndent()

            val intent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, textoCompartilhar)
                type = "text/plain"
            }

            startActivity(Intent.createChooser(intent, "Compartilhar lista"))
        } else {
            Toast.makeText(this, "Nenhuma lista para compartilhar", Toast.LENGTH_SHORT).show()
        }
    }
}

// Adapter para os itens da lista com checkbox
class ItemDetalhesAdapter(
    private var itens: List<ItemLista>,
    private val onItemChecked: (ItemLista, Boolean) -> Unit
) : RecyclerView.Adapter<ItemDetalhesAdapter.ItemViewHolder>() {

    class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val checkBox: CheckBox = itemView.findViewById(R.id.checkBoxItem)
        val textItem: TextView = itemView.findViewById(R.id.textItem)
        val textQuantidade: TextView = itemView.findViewById(R.id.textQuantidade)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view = View.inflate(parent.context, R.layout.item_lista_detalhes, null)
        return ItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val item = itens[position]

        holder.textItem.text = item.nome
        holder.textQuantidade.text = item.quantidade

        // Remover listener anterior para evitar loops
        holder.checkBox.setOnCheckedChangeListener(null)

        // Configurar estado do checkbox
        holder.checkBox.isChecked = item.concluido

        // Configurar novo listener
        holder.checkBox.setOnCheckedChangeListener { _, isChecked ->
            onItemChecked(item, isChecked)
        }

        // Clique em toda a linha também marca/desmarca
        holder.itemView.setOnClickListener {
            holder.checkBox.isChecked = !holder.checkBox.isChecked
        }
    }

    override fun getItemCount(): Int = itens.size

    fun atualizarItens(novosItens: List<ItemLista>) {
        this.itens = novosItens
        notifyDataSetChanged()
    }
}