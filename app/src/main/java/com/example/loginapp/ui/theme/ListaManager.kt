package com.example.loginapp

import android.content.Context
import android.content.SharedPreferences
import com.example.loginapp.ui.theme.DatabaseHelper
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object ListaManager {
    private lateinit var dbHelper: DatabaseHelper

    fun init(context: Context) {
        dbHelper = DatabaseHelper(context)
    }

    fun salvarLista(context: Context, lista: ListaCompras) {
        if (!::dbHelper.isInitialized) init(context)
        dbHelper.salvarLista(lista)
    }

    fun carregarListas(context: Context): List<ListaCompras> {
        if (!::dbHelper.isInitialized) init(context)
        return dbHelper.carregarTodasListas()
    }

    fun carregarListasPessoais(context: Context): List<ListaCompras> {
        if (!::dbHelper.isInitialized) init(context)
        return dbHelper.carregarListasPessoais()
    }

    fun carregarListasCompartilhadas(context: Context): List<ListaCompras> {
        if (!::dbHelper.isInitialized) init(context)
        return dbHelper.carregarListasCompartilhadas()
    }

    fun atualizarItemLista(context: Context, listaId: Long, item: ItemLista): Boolean {
        if (!::dbHelper.isInitialized) init(context)
        return dbHelper.atualizarItemLista(listaId, item)
    }

    fun deletarLista(context: Context, nomeLista: String): Boolean {
        if (!::dbHelper.isInitialized) init(context)
        return dbHelper.deletarLista(nomeLista)
    }
}

data class ListaCompras(
    val nome: String,
    val itens: List<ItemLista> = emptyList(),
    val compartilhada: Boolean = false,
    val concluida: Boolean = false
)

data class ItemLista(
    val nome: String,
    val quantidade: String,
    val concluido: Boolean = false
)