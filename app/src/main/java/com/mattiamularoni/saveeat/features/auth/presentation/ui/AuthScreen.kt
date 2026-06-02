package com.mattiamularoni.saveeat.features.auth.presentation.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.mattiamularoni.saveeat.features.auth.presentation.util.AuthValidation
import com.mattiamularoni.saveeat.features.auth.presentation.viewmodel.AuthEffect
import com.mattiamularoni.saveeat.features.auth.presentation.viewmodel.AuthUiState
import com.mattiamularoni.saveeat.features.auth.presentation.viewmodel.AuthViewModel
import kotlinx.coroutines.flow.collectLatest
import org.koin.androidx.compose.koinViewModel

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun AuthScreen(
    onNavigateToPantry: () -> Unit = {},
    viewModel: AuthViewModel = koinViewModel()
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    // Nuovi campi
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }

    var isLoginMode by remember { mutableStateOf(true) }

    val snackbarHostState = remember { SnackbarHostState() }
    val authUiState by viewModel.authUiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.authEffect.collectLatest { effect ->
            when (effect) {
                is AuthEffect.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(
                        message = effect.message,
                        duration = SnackbarDuration.Short
                    )
                }
            }
        }
    }

    val emailError = AuthValidation.getEmailError(email)
    val passwordError = AuthValidation.getPasswordError(password)

    // Validazione dinamica: se siamo in registrazione, nome e cognome non devono essere vuoti
    val isFormValid = if (isLoginMode) {
        emailError == null && passwordError == null && email.isNotEmpty() && password.isNotEmpty()
    } else {
        emailError == null && passwordError == null && email.isNotEmpty() && password.isNotEmpty() &&
                firstName.isNotBlank() && lastName.isNotBlank()
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(if (isLoginMode) "Login" else "Register") }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // NOME E COGNOME (Visibili solo in registrazione)
            AnimatedVisibility(visible = !isLoginMode) {
                Column {
                    TextField(
                        value = firstName,
                        onValueChange = { firstName = it },
                        label = { Text("Nome") },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = authUiState !is AuthUiState.Loading,
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    TextField(
                        value = lastName,
                        onValueChange = { lastName = it },
                        label = { Text("Cognome") },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = authUiState !is AuthUiState.Loading,
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            // Email input field
            TextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth(),
                enabled = authUiState !is AuthUiState.Loading,
                isError = emailError != null && email.isNotEmpty(),
                singleLine = true
            )
            if (emailError != null && email.isNotEmpty()) {
                Text(
                    text = emailError,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 4.dp, start = 16.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Password input field
            TextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                modifier = Modifier.fillMaxWidth(),
                enabled = authUiState !is AuthUiState.Loading,
                visualTransformation = PasswordVisualTransformation(),
                isError = passwordError != null && password.isNotEmpty(),
                singleLine = true
            )
            if (passwordError != null && password.isNotEmpty()) {
                Text(
                    text = passwordError,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 4.dp, start = 16.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Action button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                contentAlignment = Alignment.Center
            ) {
                if (authUiState is AuthUiState.Loading) {
                    CircularProgressIndicator()
                } else {
                    Button(
                        onClick = {
                            if (isLoginMode) {
                                viewModel.signIn(email, password)
                            } else {
                                viewModel.signUp(email, password, firstName, lastName)
                            }
                        },
                        enabled = isFormValid,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(if (isLoginMode) "Login" else "Registrati")
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = if (isLoginMode) "Non hai un account?" else "Hai già un account?",
                    modifier = Modifier.align(Alignment.CenterVertically)
                )
                TextButton(
                    onClick = {
                        isLoginMode = !isLoginMode
                        viewModel.resetState()
                    }
                ) {
                    Text(if (isLoginMode) "Registrati" else "Login")
                }
            }
        }
    }
}