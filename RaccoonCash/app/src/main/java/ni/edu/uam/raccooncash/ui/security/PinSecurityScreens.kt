package ni.edu.uam.raccooncash.ui.security

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ni.edu.uam.raccooncash.data.security.PinSecurityStore

private const val PinLength = 4
private val BackgroundColor = Color(0xFF0F111A)
private val CardColor = Color(0xFF1E222D)
private val AccentColor = Color(0xFFB5A9D4)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SecurityScreen(
    pinSecurityStore: PinSecurityStore,
    onBack: () -> Unit,
    onPinStateChanged: (Boolean) -> Unit
) {
    var hasPin by remember { mutableStateOf(pinSecurityStore.hasPin()) }
    var pinEnabled by remember { mutableStateOf(pinSecurityStore.isPinEnabled()) }
    var currentPin by remember { mutableStateOf("") }
    var newPin by remember { mutableStateOf("") }
    var confirmPin by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }

    fun refreshPinState() {
        hasPin = pinSecurityStore.hasPin()
        pinEnabled = pinSecurityStore.isPinEnabled()
        onPinStateChanged(pinEnabled)
    }

    fun clearPinFields() {
        currentPin = ""
        newPin = ""
        confirmPin = ""
    }

    fun showError(message: String) {
        errorMessage = message
        successMessage = null
    }

    fun showSuccess(message: String) {
        successMessage = message
        errorMessage = null
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Seguridad", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atras", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BackgroundColor)
            )
        },
        containerColor = BackgroundColor
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            SecurityStatusCard(
                pinEnabled = pinEnabled,
                onPinEnabledChange = { enabled ->
                    if (enabled && !hasPin) {
                        showError("Primero crea un PIN para habilitar el bloqueo.")
                        return@SecurityStatusCard
                    }

                    pinSecurityStore.setPinEnabled(enabled)
                    refreshPinState()
                    showSuccess(if (enabled) "Bloqueo con PIN habilitado." else "Bloqueo con PIN deshabilitado.")
                }
            )

            PinManagementCard(
                hasPin = hasPin,
                currentPin = currentPin,
                newPin = newPin,
                confirmPin = confirmPin,
                onCurrentPinChange = { currentPin = it },
                onNewPinChange = { newPin = it },
                onConfirmPinChange = { confirmPin = it },
                onCreatePin = {
                    val validationError = validateNewPin(newPin, confirmPin)
                    if (validationError != null) {
                        showError(validationError)
                        return@PinManagementCard
                    }

                    pinSecurityStore.setPin(newPin)
                    pinSecurityStore.setPinEnabled(true)
                    clearPinFields()
                    refreshPinState()
                    showSuccess("PIN creado y bloqueo habilitado.")
                },
                onChangePin = {
                    if (!pinSecurityStore.verifyPin(currentPin)) {
                        showError("El PIN actual no es correcto.")
                        return@PinManagementCard
                    }

                    val validationError = validateNewPin(newPin, confirmPin)
                    if (validationError != null) {
                        showError(validationError)
                        return@PinManagementCard
                    }

                    pinSecurityStore.setPin(newPin)
                    clearPinFields()
                    refreshPinState()
                    showSuccess("PIN actualizado correctamente.")
                }
            )

            errorMessage?.let { message -> FeedbackText(message = message, isError = true) }
            successMessage?.let { message -> FeedbackText(message = message, isError = false) }
        }
    }
}

@Composable
fun PinLockScreen(
    onValidatePin: (String) -> Boolean,
    onUnlocked: () -> Unit
) {
    var pin by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = BackgroundColor
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = CardColor),
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(18.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .background(Color(0xFF2C313F), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Lock, contentDescription = null, tint = AccentColor, modifier = Modifier.size(36.dp))
                    }

                    Text(
                        text = "Raccoon Cash bloqueado",
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "Ingresa tu PIN para continuar.",
                        color = Color.Gray,
                        textAlign = TextAlign.Center
                    )

                    PinInputField(
                        label = "PIN",
                        value = pin,
                        onValueChange = {
                            pin = it
                            errorMessage = null
                        }
                    )

                    errorMessage?.let { FeedbackText(message = it, isError = true) }

                    Button(
                        onClick = {
                            if (onValidatePin(pin)) {
                                pin = ""
                                errorMessage = null
                                onUnlocked()
                            } else {
                                pin = ""
                                errorMessage = "PIN incorrecto. Intentalo de nuevo."
                            }
                        },
                        enabled = pin.length == PinLength,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AccentColor,
                            contentColor = BackgroundColor
                        )
                    ) {
                        Text("Acceder", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun SecurityStatusCard(
    pinEnabled: Boolean,
    onPinEnabledChange: (Boolean) -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = CardColor),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(Color(0xFF2C313F), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Lock, contentDescription = null, tint = Color.White, modifier = Modifier.size(22.dp))
            }

            Column(modifier = Modifier.weight(1f)) {
                Text("Bloqueo con PIN", color = Color.White, fontWeight = FontWeight.Bold)
                Text(
                    text = "Pide el PIN cada vez que abras la app.",
                    color = Color.Gray,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Switch(
                checked = pinEnabled,
                onCheckedChange = onPinEnabledChange
            )
        }
    }
}

@Composable
private fun PinManagementCard(
    hasPin: Boolean,
    currentPin: String,
    newPin: String,
    confirmPin: String,
    onCurrentPinChange: (String) -> Unit,
    onNewPinChange: (String) -> Unit,
    onConfirmPinChange: (String) -> Unit,
    onCreatePin: () -> Unit,
    onChangePin: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = CardColor),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = if (hasPin) "Cambiar PIN" else "Crear PIN",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )

            if (hasPin) {
                PinInputField(
                    label = "PIN actual",
                    value = currentPin,
                    onValueChange = onCurrentPinChange
                )
            } else {
                Text(
                    text = "Crea un PIN de 4 digitos para activar el bloqueo.",
                    color = Color.Gray,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            PinInputField(
                label = "Nuevo PIN",
                value = newPin,
                onValueChange = onNewPinChange
            )
            PinInputField(
                label = "Verificacion del PIN",
                value = confirmPin,
                onValueChange = onConfirmPinChange
            )

            Spacer(modifier = Modifier.height(2.dp))

            Button(
                onClick = if (hasPin) onChangePin else onCreatePin,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AccentColor,
                    contentColor = BackgroundColor
                )
            ) {
                Text(if (hasPin) "Cambiar PIN" else "Guardar PIN", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun PinInputField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = { input -> onValueChange(input.filter { it.isDigit() }.take(PinLength)) },
        label = { Text(label) },
        singleLine = true,
        visualTransformation = PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
        modifier = Modifier.fillMaxWidth(),
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
            focusedBorderColor = AccentColor,
            unfocusedBorderColor = Color.Gray,
            focusedLabelColor = AccentColor,
            unfocusedLabelColor = Color.Gray,
            cursorColor = AccentColor
        )
    )
}

@Composable
private fun FeedbackText(
    message: String,
    isError: Boolean
) {
    Text(
        text = message,
        color = if (isError) Color(0xFFFF8A80) else Color(0xFF80CBC4),
        style = MaterialTheme.typography.bodySmall,
        modifier = Modifier.fillMaxWidth(),
        textAlign = TextAlign.Center
    )
}

private fun validateNewPin(newPin: String, confirmPin: String): String? {
    return when {
        newPin.length != PinLength -> "El PIN debe tener 4 digitos."
        confirmPin.length != PinLength -> "Confirma el PIN de 4 digitos."
        newPin != confirmPin -> "La verificacion no coincide con el nuevo PIN."
        else -> null
    }
}
