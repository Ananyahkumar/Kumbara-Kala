package com.example.kumbarakala.ui.screens

import android.graphics.Bitmap
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kumbarakala.data.ProductData
import com.example.kumbarakala.utils.BitmapUtils
import com.example.kumbarakala.utils.ImageUtils
import com.example.kumbarakala.utils.ShareUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StoryGeneratorScreen(
    productId: String?,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val profileRepository = remember { com.example.kumbarakala.data.ProfileRepository(context) }
    val coroutineScope = rememberCoroutineScope()
    
    val product = ProductData.products.find { it.id == productId }
    val isCustomMode = productId == "custom"
    val makerTitle = profileRepository.getTitle()
    val makerLocation = profileRepository.getMakerLocation()
    val makerExperience = profileRepository.getMakerExperience()
    val makerSpecialization = profileRepository.getMakerSpecialization()
    val makerEmail = profileRepository.getMakerEmail()
    val makerName = profileRepository.getName()
    val makerPhone = profileRepository.getPhone()
    
    var productName by remember { mutableStateOf(product?.name ?: "") }
    var healthBenefit by remember { mutableStateOf(product?.healthBenefitDescription ?: "") }
    var ecoBenefit by remember { mutableStateOf(product?.ecoBenefitDescription ?: "") }
    var extraDetails by remember { mutableStateOf("") }
    var productImageUri by remember { mutableStateOf<Uri?>(null) }
    var previewBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var showPreview by remember { mutableStateOf(false) }
    var isGenerating by remember { mutableStateOf(false) }
    val selectedPreviewBitmap by remember(productImageUri) {
        mutableStateOf(productImageUri?.let { ImageUtils.decodeUriToBitmap(context, it) })
    }

    val cameraImageUri = remember { ImageUtils.createTempImageUri(context) }
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            productImageUri = cameraImageUri
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            productImageUri = uri
        }
    }

    if (product == null && !isCustomMode) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("Product not found.")
            Button(onClick = onNavigateBack) {
                Text("Go Back")
            }
        }
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Generate Story Card", color = MaterialTheme.colorScheme.onPrimary) },
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column {
                    if (selectedPreviewBitmap != null) {
                        Image(
                            bitmap = selectedPreviewBitmap!!.asImageBitmap(),
                            contentDescription = "Selected Product Image",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                                .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)),
                            contentScale = ContentScale.Crop
                        )
                    } else if (product != null) {
                        Image(
                            painter = painterResource(id = product.imageResId),
                            contentDescription = product.name,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                                .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)),
                            contentScale = ContentScale.Crop
                        )
                    }
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = productName.ifBlank { "Custom Product" },
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Health: ${healthBenefit.ifBlank { "Add health benefits below" }}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Eco: ${ecoBenefit.ifBlank { "Add eco benefits below" }}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.tertiary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = "Product Details",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            OutlinedTextField(
                value = productName,
                onValueChange = { productName = it },
                label = { Text("Product Name") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = healthBenefit,
                onValueChange = { healthBenefit = it },
                label = { Text("Health Benefits") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = ecoBenefit,
                onValueChange = { ecoBenefit = it },
                label = { Text("Eco / Sustainability Benefits") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = extraDetails,
                onValueChange = { extraDetails = it },
                label = { Text("Other Details (Optional)") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { cameraLauncher.launch(cameraImageUri) },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Take Photo")
                }

                OutlinedButton(
                    onClick = {
                        galleryLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Choose Photo")
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    if (productName.isBlank() || healthBenefit.isBlank() || ecoBenefit.isBlank()) {
                        Toast.makeText(context, "Please fill product name and benefits", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    if (makerName.isBlank() || makerPhone.isBlank()) {
                        Toast.makeText(
                            context,
                            "Please complete Name and Contact Number in Meet the Maker page",
                            Toast.LENGTH_SHORT
                        ).show()
                        return@Button
                    }
                    isGenerating = true
                    coroutineScope.launch {
                        try {
                            val bitmap = withContext(Dispatchers.IO) {
                                val inputBitmap = productImageUri?.let { ImageUtils.decodeUriToBitmap(context, it) }
                                BitmapUtils.generateStoryCard(
                                    context = context,
                                    productName = productName,
                                    healthBenefit = healthBenefit,
                                    ecoBenefit = ecoBenefit,
                                    otherDetails = extraDetails,
                                    artisanName = makerName,
                                    artisanPhone = makerPhone,
                                    makerTitle = makerTitle,
                                    makerLocation = makerLocation,
                                    makerExperience = makerExperience,
                                    makerSpecialization = makerSpecialization,
                                    makerEmail = makerEmail,
                                    productBitmap = inputBitmap,
                                    fallbackImageResId = product?.imageResId
                                )
                            }
                            previewBitmap = bitmap
                            showPreview = true
                        } catch (e: Exception) {
                            Toast.makeText(context, "Error generating card", Toast.LENGTH_SHORT).show()
                            e.printStackTrace()
                        } finally {
                            isGenerating = false
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(28.dp),
                enabled = !isGenerating
            ) {
                if (isGenerating) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Generating...")
                } else {
                    Text("Generate Preview", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }

            if (showPreview && previewBitmap != null) {
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "Story Card Preview",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Image(
                        bitmap = previewBitmap!!.asImageBitmap(),
                        contentDescription = "Story Card Preview",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(420.dp),
                        contentScale = ContentScale.Crop
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { showPreview = false },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Close Preview")
                    }
                    Button(
                        onClick = { ShareUtils.shareBitmapToWhatsApp(context, previewBitmap!!) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(imageVector = Icons.Default.Share, contentDescription = "Share")
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Share WhatsApp")
                    }
                }
            }
        }
    }
}
