package fr.isen.activevibe.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Suppress("DEPRECATION")
@Composable
fun TopBar(selectedItem: Int, onItemSelected: (Int) -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .statusBarsPadding()
    ) {
        if (selectedItem == 0) {
            IconButton(
                onClick = { onItemSelected(5) },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(y = 10.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Send,
                    contentDescription = "Envoyer",
                    modifier = Modifier.size(40.dp)
                )
            }
        }
    }
}
