package ni.edu.uam.raccooncash.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Face
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

val SharedEmojiOptions = listOf(
    "💰", "💵", "💳", "🏦", "🧾", "📈", "📉", "🪙", "💎", "🎯",
    "🍽️", "🍴", "🍔", "🍕", "🥦", "🍎", "☕", "🍺", "🍷", "🧁",
    "🛒", "🛍️", "👕", "👟", "💄", "🎁", "📦", "🏷️", "🧸", "📚",
    "🚗", "🚕", "🚌", "🚲", "⛽", "✈️", "🚢", "🚆", "🏖️", "🧳",
    "🏠", "🏢", "🔑", "🛠️", "💡", "💧", "📱", "💻", "📺", "🎮",
    "🏥", "💊", "🩺", "🏋️", "⚽", "🎬", "🎵", "🎸", "🎨", "📷",
    "🐶", "🐱", "🌱", "🌸", "☀️", "🌙", "⭐", "🔥", "🚨", "📝"
).distinct()

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmojiPickerDialog(
    onDismiss: () -> Unit,
    onEmojiSelected: (String) -> Unit,
    title: String = "Elige un emoji"
) {
    var customEmoji by remember { mutableStateOf("") }
    val customEmojiValue = customEmoji.trim()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF0F111A)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            Surface(
                color = Color(0xFF1E222D),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Face, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Emoji personalizado", color = Color.White, fontWeight = FontWeight.Bold)
                            Text("Puedes escribir o pegar uno desde tu teclado.", color = Color.Gray, fontSize = 12.sp)
                        }
                    }

                    OutlinedTextField(
                        value = customEmoji,
                        onValueChange = { customEmoji = it },
                        placeholder = { Text("Ej: 🦝") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Done
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFFB5A9D4),
                            unfocusedBorderColor = Color.Gray,
                            focusedLabelColor = Color(0xFFB5A9D4),
                            cursorColor = Color(0xFFB5A9D4)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Button(
                        onClick = { onEmojiSelected(customEmojiValue) },
                        enabled = customEmojiValue.isNotEmpty(),
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFB5A9D4),
                            contentColor = Color.Black
                        )
                    ) {
                        Text("Usar este emoji", fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text("Opciones", color = Color.Gray, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 4.dp))

            LazyVerticalGrid(
                columns = GridCells.Fixed(5),
                modifier = Modifier.heightIn(max = 400.dp),
                contentPadding = PaddingValues(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(SharedEmojiOptions) { emoji ->
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .background(Color(0xFF1E222D), RoundedCornerShape(16.dp))
                            .clickable { onEmojiSelected(emoji) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(emoji, fontSize = 30.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
