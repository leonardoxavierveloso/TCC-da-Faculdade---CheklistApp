package com.example.loginapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class CriarListaActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ItemAdapter
    private val listaItens = mutableListOf<ItemLista>()

    private lateinit var editTextNomeLista: EditText
    private lateinit var editTextProduto: EditText
    private lateinit var editTextQuantidade: EditText
    private lateinit var btnAdicionarItem: Button
    private lateinit var btnSalvarLista: Button
    private lateinit var btnCancelar: Button
    private lateinit var txtSaudacao: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_criar_lista)

        initViews()
        setupRecyclerView()
        setupClickListeners()

        // Saudação com nome do usuário
        val nomeUsuario = intent.getStringExtra("NOME_USUARIO") ?: "Leonardo"
        txtSaudacao.text = "Olá, $nomeUsuario"
    }

    private fun initViews() {
        editTextNomeLista = findViewById(R.id.editTextNomeLista)
        editTextProduto = findViewById(R.id.editTextProduto)
        editTextQuantidade = findViewById(R.id.editTextQuantidade)
        btnAdicionarItem = findViewById(R.id.btnAdicionarItem)
        btnSalvarLista = findViewById(R.id.btnSalvarLista)
        btnCancelar = findViewById(R.id.btnCancelar)
        txtSaudacao = findViewById(R.id.txtSaudacao)
        recyclerView = findViewById(R.id.recyclerViewItens)
    }

    private fun setupRecyclerView() {
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = ItemAdapter(listaItens)
        recyclerView.adapter = adapter
    }

    private fun setupClickListeners() {
        btnAdicionarItem.setOnClickListener {
            adicionarNovoItem()
        }

        btnSalvarLista.setOnClickListener {
            salvarLista()
        }

        btnCancelar.setOnClickListener {
            voltarParaHome()
        }
    }

    private fun adicionarNovoItem() {
        val produto = editTextProduto.text.toString().trim()
        val quantidade = editTextQuantidade.text.toString().trim()

        if (produto.isEmpty() || quantidade.isEmpty()) {
            Toast.makeText(this, "Preencha o produto e a quantidade", Toast.LENGTH_SHORT).show()
            return
        }

        val novoItem = ItemLista(produto, quantidade)
        listaItens.add(novoItem)
        adapter.notifyItemInserted(listaItens.size - 1)

        // Limpar campos
        editTextProduto.text.clear()
        editTextQuantidade.text.clear()

        Toast.makeText(this, "Item adicionado!", Toast.LENGTH_SHORT).show()
    }

    private fun salvarLista() {
        val nomeLista = editTextNomeLista.text.toString().trim()

        if (nomeLista.isEmpty()) {
            Toast.makeText(this, "Digite um nome para a lista", Toast.LENGTH_SHORT).show()
            return
        }

        if (listaItens.isEmpty()) {
            Toast.makeText(this, "Adicione pelo menos um item à lista", Toast.LENGTH_SHORT).show()
            return
        }

        // Criar objeto da lista
        val novaLista = ListaCompras(
            nome = nomeLista,
            itens = listaItens.toList(),
            compartilhada = false,
            concluida = false
        )

        // Salvar a lista
        ListaManager.salvarLista(this, novaLista)

        Toast.makeText(this, "Lista '$nomeLista' salva com sucesso!", Toast.LENGTH_LONG).show()

        // Voltar para a home
        voltarParaHome()
    }

    private fun voltarParaHome() {
        val intent = Intent(this, ListaComprasActivity::class.java)
        startActivity(intent)
        finish()
    }
}

// Adapter para a lista de itens
class ItemAdapter(private val itens: List<ItemLista>) :
    RecyclerView.Adapter<ItemAdapter.ItemViewHolder>() {

    class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textItem: TextView = itemView.findViewById(R.id.textItem)
        val textQuantidade: TextView = itemView.findViewById(R.id.textQuantidade)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view = View.inflate(parent.context, R.layout.item_lista_produto, null)
        return ItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val item = itens[position]
        holder.textItem.text = item.nome
        holder.textQuantidade.text = item.quantidade
    }

    override fun getItemCount(): Int = itens.size
}