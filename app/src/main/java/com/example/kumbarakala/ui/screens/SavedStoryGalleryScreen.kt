package com.example.kumbarakala.ui.screens

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import android.graphics.Bitmap
import android.widget.Toast
import com.example.kumbarakala.data.SavedStoryCard
import com.example.kumbarakala.data.StoryCardRepository
import com.example.kumbarakala.utils.ShareUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.DateFormat
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavedStoryGalleryScreen(
    onNavigateBack: () -> Unit,
    onEditSavedCard: (String) -> Unit
) {
    val context = LocalContext.current
    val repository = remember { StoryCardRepository(context) }
    val scope = rememberCoroutineScope()
    var cards by remember { mutableStateOf(repository.listSavedCards()) }
    var cardPendingDelete by remember { mutableStateOf<SavedStoryCard?>(null) }

    fun refresh() {
        cards = repository.listSavedCards()
    }

    LaunchedEffect(Unit) {
        refresh()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "My Story Cards",
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        if (cards.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No saved story cards yet. Generate a card and choose Save when prompted.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp)
            ) {
                items(cards, key = { it.id }) { card ->
                    SavedStoryCardItem(
                        card = card,
                        repository = repository,
                        onDeleteClick = { cardPendingDelete = card },
                        onEditClick = { onEditSavedCard(card.id) },
                        onShareClick = { bmp ->
                            ShareUtils.shareBitmapToWhatsApp(context, bmp)
                        }
                    )
                }
            }
        }
    }

    cardPendingDelete?.let { card ->
        AlertDialog(
            onDismissRequest = { cardPendingDelete = null },
            title = { Text("Delete story card?") },
            text = {
                Text(
                    "This removes \"${card.productName}\" from your gallery. This cannot be undone.",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        scope.launch {
                            withContext(Dispatchers.IO) {
                                repository.deleteCard(card.id)
                            }
                            cardPendingDelete = null
                            refresh()
                        }
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { cardPendingDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun SavedStoryCardItem(
    card: SavedStoryCard,
    repository: StoryCardRepository,
    onDeleteClick: () -> Unit,
    onEditClick: () -> Unit,
    onShareClick: (Bitmap) -> Unit
) {
    val context = LocalContext.current
    val file = remember(card.id) { repository.getCardFile(card) }
    var bitmap by remember(card.id) { mutableStateOf<android.graphics.Bitmap?>(null) }

    LaunchedEffect(card.id) {
        bitmap = withContext(Dispatchers.IO) {
            BitmapFactory.decodeFile(file.absolutePath)
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column {
            Box {
                if (bitmap != null) {
                    Image(
                        bitmap = bitmap!!.asImageBitmap(),
                        contentDescription = card.productName,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(32.dp))
                    }
                }
                TextButton(
                    onClick = onDeleteClick,
                    modifier = Modifier.align(Alignment.TopEnd),
                    colors = ButtonDefaults.textButtonColors(
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f)
                    ),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        "Remove",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
            Column(modifier = Modifier.padding(10.dp)) {
                Text(
                    text = card.productName,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = DateFormat.getDateTimeInstance(
                        DateFormat.SHORT,
                        DateFormat.SHORT
                    ).format(Date(card.createdAtMillis)),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    TextButton(
                        onClick = {
                            if (bitmap != null) onShareClick(bitmap!!)
                            else Toast.makeText(
                                context,
                                "Image still loading",
                                Toast.LENGTH_SHORT
                            ).show()
                        },
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 4.dp)
                    ) {
                        Text("Share", style = MaterialTheme.typography.labelMedium)
                    }
                    TextButton(
                        onClick = onEditClick,
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 4.dp)
                    ) {
                        Text("Edit", style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
        }
    }
}
