package com.example.loginapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText

class MainActivity : AppCompatActivity() {

    private lateinit var loginForm: LinearLayout
    private lateinit var registerForm: LinearLayout
    private lateinit var emailEditText: TextInputEditText
    private lateinit var passwordEditText: TextInputEditText
    private lateinit var nameEditText: TextInputEditText
    private lateinit var registerEmailEditText: TextInputEditText
    private lateinit var registerPasswordEditText: TextInputEditText
    private lateinit var loginButton: Button
    private lateinit var registerButton: Button
    private lateinit var switchToRegister: TextView
    private lateinit var switchToLogin: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Inicializar views
        initViews()

        // Configurar listeners
        setupListeners()

        Toast.makeText(this, "App iniciado", Toast.LENGTH_SHORT).show()
    }

    private fun initViews() {
        loginForm = findViewById(R.id.loginForm)
        registerForm = findViewById(R.id.registerForm)
        emailEditText = findViewById(R.id.emailEditText)
        passwordEditText = findViewById(R.id.passwordEditText)
        nameEditText = findViewById(R.id.nameEditText)
        registerEmailEditText = findViewById(R.id.registerEmailEditText)
        registerPasswordEditText = findViewById(R.id.registerPasswordEditText)
        loginButton = findViewById(R.id.loginButton)
        registerButton = findViewById(R.id.registerButton)
        switchToRegister = findViewById(R.id.switchToRegister)
        switchToLogin = findViewById(R.id.switchToLogin)
    }

    private fun setupListeners() {
        // Switch between login and register forms
        switchToRegister.setOnClickListener {
            showRegisterForm()
        }

        switchToLogin.setOnClickListener {
            showLoginForm()
        }

        // Login button
        loginButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()

            if (validateLoginInput(email, password)) {
                performLogin(email, password)
            }
        }

        // Register button
        registerButton.setOnClickListener {
            val name = nameEditText.text.toString().trim()
            val email = registerEmailEditText.text.toString().trim()
            val password = registerPasswordEditText.text.toString().trim()

            if (validateRegisterInput(name, email, password)) {
                performRegister(name, email, password)
            }
        }
    }

    private fun showLoginForm() {
        loginForm.visibility = LinearLayout.VISIBLE
        registerForm.visibility = LinearLayout.GONE
        clearAllFields()
    }

    private fun showRegisterForm() {
        loginForm.visibility = LinearLayout.GONE
        registerForm.visibility = LinearLayout.VISIBLE
        clearAllFields()
    }

    private fun clearAllFields() {
        emailEditText.text?.clear()
        passwordEditText.text?.clear()
        nameEditText.text?.clear()
        registerEmailEditText.text?.clear()
        registerPasswordEditText.text?.clear()
    }

    private fun validateLoginInput(email: String, password: String): Boolean {
        if (email.isEmpty()) {
            showToast("Por favor, insira seu email")
            return false
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            showToast("Por favor, insira um email válido")
            return false
        }

        if (password.isEmpty()) {
            showToast("Por favor, insira sua senha")
            return false
        }

        if (password.length < 6) {
            showToast("A senha deve ter pelo menos 6 caracteres")
            return false
        }

        return true
    }

    private fun validateRegisterInput(name: String, email: String, password: String): Boolean {
        if (name.isEmpty()) {
            showToast("Por favor, insira seu nome")
            return false
        }

        if (email.isEmpty()) {
            showToast("Por favor, insira seu email")
            return false
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            showToast("Por favor, insira um email válido")
            return false
        }

        if (password.isEmpty()) {
            showToast("Por favor, insira sua senha")
            return false
        }

        if (password.length < 6) {
            showToast("A senha deve ter pelo menos 6 caracteres")
            return false
        }

        return true
    }

    private fun performLogin(email: String, password: String) {
        showToast("✅ Login realizado!")

        try {
            // Teste 1: Verificar se a activity existe
            Class.forName("com.example.loginapp.ListaComprasActivity")
            showToast("✅ Activity encontrada!")

            // Teste 2: Tentar abrir
            val intent = Intent(this, ListaComprasActivity::class.java)
            intent.putExtra("NOME_USUARIO", "Leonardo")
            startActivity(intent)
            finish()

        } catch (e: ClassNotFoundException) {
            showToast("❌ Activity não encontrada!")
        } catch (e: Exception) {
            showToast("❌ Erro: ${e.message}")
        }
    }

    private fun performRegister(name: String, email: String, password: String) {
        // Simulação de cadastro
        showToast("Cadastro realizado com sucesso!\nNome: $name\nEmail: $email")
        showLoginForm()
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}