package com.example.loginapp

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object ListaManager {
    private const val PREFS_NAME = "listas_prefs"
    private const val KEY_LISTAS = "listas_salvas"

    fun salvarLista(context: Context, lista: ListaCompras) {
        val listasExistentes = carregarListas(context).toMutableList()
        listasExistentes.add(lista)

        val sharedPref = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val editor = sharedPref.edit()

        val gson = Gson()
        val json = gson.toJson(listasExistentes)
        editor.putString(KEY_LISTAS, json)
        editor.apply()
    }

    fun carregarListas(context: Context): List<ListaCompras> {
        val sharedPref = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val json = sharedPref.getString(KEY_LISTAS, null)

        return if (json != null) {
            val gson = Gson()
            val type = object : TypeToken<List<ListaCompras>>() {}.type
            gson.fromJson(json, type) ?: emptyList()
        } else {
            emptyList()
        }
    }

    fun carregarListasPessoais(context: Context): List<ListaCompras> {
        return carregarListas(context).filter { !it.compartilhada }
    }

    fun carregarListasCompartilhadas(context: Context): List<ListaCompras> {
        return carregarListas(context).filter { it.compartilhada }
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