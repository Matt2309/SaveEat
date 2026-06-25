package com.mattiamularoni.saveeat.features.auth.presentation.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.mattiamularoni.saveeat.BuildConfig
import com.mattiamularoni.saveeat.R
import com.mattiamularoni.saveeat.features.auth.presentation.util.AuthValidation
import com.mattiamularoni.saveeat.features.auth.presentation.viewmodel.AuthEffect
import com.mattiamularoni.saveeat.features.auth.presentation.viewmodel.AuthUiState
import com.mattiamularoni.saveeat.features.auth.presentation.viewmodel.AuthViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun AuthScreen(
    onNavigateToPantry: () -> Unit = {},
    viewModel: AuthViewModel = koinViewModel(),
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }

    var isLoginMode by remember { mutableStateOf(true) }
    var passwordVisible by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val authUiState by viewModel.authUiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.authEffect.collectLatest { effect ->
            when (effect) {
                is AuthEffect.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(
                        message = effect.message,
                        duration = SnackbarDuration.Short,
                    )
                }
            }
        }
    }

    val emailError = AuthValidation.getEmailError(email)
    val passwordError = AuthValidation.getPasswordError(password)

    val isFormValid =
        if (isLoginMode) {
            emailError == null && passwordError == null && email.isNotEmpty() && password.isNotEmpty()
        } else {
            emailError == null &&
                passwordError == null &&
                email.isNotEmpty() &&
                password.isNotEmpty() &&
                firstName.isNotBlank() &&
                lastName.isNotBlank()
        }

    val isLoading = authUiState is AuthUiState.Loading

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 24.dp),
            contentAlignment = Alignment.Center,
        ) {
            Card(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .widthIn(max = 448.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            ) {
                Column(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp),
                ) {
                    // ---------- Header ----------
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Text(
                            text = "SaveEat",
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 28.sp,
                            lineHeight = 36.sp,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Text(
                            text =
                                if (isLoginMode) {
                                    "Bentornato! Accedi al tuo account."
                                } else {
                                    "Crea un account per iniziare a salvare cibo."
                                },
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                        )
                    }

                    // ---------- Form ----------
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        // Nome / Cognome (solo registrazione)
                        AnimatedVisibility(visible = !isLoginMode) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                LabeledField(
                                    label = "Nome",
                                    modifier = Modifier.weight(1f),
                                ) {
                                    SaveEatTextField(
                                        value = firstName,
                                        onValueChange = { firstName = it },
                                        placeholder = "Mario",
                                        enabled = !isLoading,
                                    )
                                }
                                LabeledField(
                                    label = "Cognome",
                                    modifier = Modifier.weight(1f),
                                ) {
                                    SaveEatTextField(
                                        value = lastName,
                                        onValueChange = { lastName = it },
                                        placeholder = "Rossi",
                                        enabled = !isLoading,
                                    )
                                }
                            }
                        }

                        // Email
                        LabeledField(label = "Email") {
                            SaveEatTextField(
                                value = email,
                                onValueChange = { email = it },
                                placeholder = "mario.rossi@example.com",
                                enabled = !isLoading,
                                isError = emailError != null && email.isNotEmpty(),
                                keyboardType = KeyboardType.Email,
                                leadingIcon = {
                                    Icon(
                                        Icons.Filled.Email,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                },
                            )
                            if (emailError != null && email.isNotEmpty()) {
                                FieldError(emailError)
                            }
                        }

                        // Password
                        LabeledField(label = "Password") {
                            SaveEatTextField(
                                value = password,
                                onValueChange = { password = it },
                                placeholder = "••••••••",
                                enabled = !isLoading,
                                isError = passwordError != null && password.isNotEmpty(),
                                keyboardType = KeyboardType.Password,
                                visualTransformation =
                                    if (passwordVisible) {
                                        VisualTransformation.None
                                    } else {
                                        PasswordVisualTransformation()
                                    },
                                leadingIcon = {
                                    Icon(
                                        Icons.Filled.Lock,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                },
                                trailingIcon = {
                                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                        Icon(
                                            imageVector =
                                                if (passwordVisible) {
                                                    Icons.Filled.Visibility
                                                } else {
                                                    Icons.Filled.VisibilityOff
                                                },
                                            contentDescription =
                                                if (passwordVisible) {
                                                    "Nascondi password"
                                                } else {
                                                    "Mostra password"
                                                },
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        )
                                    }
                                },
                            )
                            if (passwordError != null && password.isNotEmpty()) {
                                FieldError(passwordError)
                            }
                        }

                        // Pulsante principale
                        Button(
                            onClick = {
                                if (isLoginMode) {
                                    viewModel.signIn(email, password)
                                } else {
                                    viewModel.signUp(email, password, firstName, lastName)
                                }
                            },
                            enabled = isFormValid && !isLoading,
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .height(52.dp)
                                    .padding(top = 4.dp),
                            shape = CircleShape,
                            colors =
                                ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = MaterialTheme.colorScheme.onPrimary,
                                ),
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(22.dp),
                                    strokeWidth = 2.dp,
                                    color = MaterialTheme.colorScheme.onPrimary,
                                )
                            } else {
                                Text(
                                    text = if (isLoginMode) "Accedi" else "Registrati",
                                    style = MaterialTheme.typography.titleMedium,
                                )
                            }
                        }
                    }

                    // ---------- Divider ----------
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        HorizontalDivider(
                            modifier = Modifier.weight(1f),
                            color = MaterialTheme.colorScheme.outlineVariant,
                        )
                        Text(
                            text = "Oppure continua con",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.labelSmall,
                        )
                        HorizontalDivider(
                            modifier = Modifier.weight(1f),
                            color = MaterialTheme.colorScheme.outlineVariant,
                        )
                    }

                    // ---------- Google Sign-In ----------
                    OutlinedButton(
                        onClick = {
                            scope.launch {
                                try {
                                    val credentialManager = CredentialManager.create(context)
                                    val googleIdOption =
                                        GetGoogleIdOption
                                            .Builder()
                                            .setFilterByAuthorizedAccounts(false)
                                            .setServerClientId(BuildConfig.GOOGLE_WEB_CLIENT_ID)
                                            .setAutoSelectEnabled(true)
                                            .build()
                                    val request =
                                        GetCredentialRequest
                                            .Builder()
                                            .addCredentialOption(googleIdOption)
                                            .build()
                                    val result = credentialManager.getCredential(context, request)
                                    val credential = result.credential
                                    if (credential is CustomCredential &&
                                        credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
                                    ) {
                                        val idToken = GoogleIdTokenCredential.createFrom(credential.data).idToken
                                        viewModel.onGoogleIdTokenReceived(idToken)
                                    } else {
                                        snackbarHostState.showSnackbar("Credenziale non supportata")
                                    }
                                } catch (e: GetCredentialCancellationException) {
                                    // utente ha annullato, nessuna azione
                                } catch (e: GetCredentialException) {
                                    snackbarHostState.showSnackbar("Accesso con Google non riuscito. Riprova.")
                                }
                            }
                        },
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .height(52.dp),
                        shape = CircleShape,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        colors =
                            ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.onSurface,
                            ),
                    ) {
                        GoogleLogo(modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Accedi con Google",
                            style = MaterialTheme.typography.titleMedium,
                        )
                    }

                    // ---------- Footer toggle ----------
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = if (isLoginMode) "Non hai un account?" else "Hai già un account?",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                        TextButton(
                            onClick = {
                                isLoginMode = !isLoginMode
                                viewModel.resetState()
                            },
                        ) {
                            Text(
                                text = if (isLoginMode) "Registrati" else "Accedi",
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Medium,
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        }
                    }
                }
            }
        }
    }
}

/** Etichetta piccola sopra al campo, come nel mockup. */
@Composable
private fun LabeledField(
    label: String,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = label,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.labelSmall,
        )
        content()
    }
}

@Composable
private fun FieldError(message: String) {
    Text(
        text = message,
        color = MaterialTheme.colorScheme.error,
        style = MaterialTheme.typography.bodySmall,
        modifier = Modifier.padding(start = 4.dp, top = 2.dp),
    )
}

/** Campo di testo con stile "filled" chiaro e angoli arrotondati come nel mockup. */
@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
private fun SaveEatTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    enabled: Boolean = true,
    isError: Boolean = false,
    keyboardType: KeyboardType = KeyboardType.Text,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    leadingIcon: (@Composable () -> Unit)? = null,
    trailingIcon: (@Composable () -> Unit)? = null,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        enabled = enabled,
        isError = isError,
        singleLine = true,
        placeholder = {
            Text(
                text = placeholder,
                style = MaterialTheme.typography.bodyMedium,
            )
        },
        leadingIcon = leadingIcon,
        trailingIcon = trailingIcon,
        visualTransformation = visualTransformation,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        shape = RoundedCornerShape(8.dp),
        colors =
            OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                disabledContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                focusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                unfocusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
            ),
    )
}

@Composable
private fun GoogleLogo(modifier: Modifier = Modifier) {
    Icon(
        painter = painterResource(id = R.drawable.glogo),
        contentDescription = "Logo Google",
        modifier = modifier,
        tint = Color.Unspecified,
    )
}
