package com.example.ui.feed

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.User
import com.example.ui.theme.*
import com.example.utils.toPersianCurrency
import kotlinx.coroutines.delay

@Composable
fun LiveShoppingOverlay(user: User, onClose: () -> Unit) {
    var comments by remember { mutableStateOf(listOf<String>()) }
    var showProductPopup by remember { mutableStateOf(false) }
    var commentInput by remember { mutableStateOf("") }
    var showGiftSentMessage by remember { mutableStateOf<String?>(null) }
    var showBuySuccessDialog by remember { mutableStateOf(false) }
    val storeDisplayName = user.storeApplication?.storeName ?: user.name.ifBlank { "بوتیک کوک" }

    val liveProduct = remember {
        com.example.data.Product(
            id = "live_prod_nike",
            title = "کتونی نایک ایر لایو",
            price = 2500000L,
            oldPrice = 3100000L,
            imageUrl = "https://images.unsplash.com/photo-1542291026-7eec264c27ff?auto=format&fit=crop&w=400&q=80"
        )
    }

    LaunchedEffect(Unit) {
        val dummyComments = listOf(
            "عالیه!", "قیمتش چنده؟", "سایز لارج هم داره؟", "ارسال به شهرستان دارید؟",
            "چه رنگایی داره؟", "من یکی میخوام", "لطفا پیاممو جواب بدید", "چقدر خوشگله 😍"
        )
        delay(1000)
        showProductPopup = true
        while (true) {
            delay((1500..3000).random().toLong())
            val newComment = dummyComments.random()
            comments = (comments + newComment).takeLast(10)
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        // Mock Live Video
        AsyncImage(
            model = "https://images.unsplash.com/photo-1483985988355-763728e1935b?auto=format&fit=crop&w=800&q=80",
            contentDescription = "Live Video",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // Overlay Gradient
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Black.copy(alpha = 0.5f), Color.Transparent, Color.Black.copy(alpha = 0.85f))
                    )
                )
        )

        // Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(16.dp)
                .align(Alignment.TopCenter),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .background(FuchsiaAccent, RoundedCornerShape(4.dp))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text("LIVE", color = Color.White, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .background(GlassBackground, RoundedCornerShape(12.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Favorite, contentDescription = null, tint = Color.Red, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("1.2K", color = Color.White, style = MaterialTheme.typography.labelSmall)
                    }
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(storeDisplayName, color = Color.White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
            }

            IconButton(onClick = onClose, modifier = Modifier.background(GlassBackground, CircleShape)) {
                Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
            }
        }

        // Product Popup
        AnimatedVisibility(
            visible = showProductPopup,
            enter = slideInHorizontally { it } + fadeIn(),
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 16.dp)
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = GlassBackground),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, FuchsiaAccent.copy(alpha = 0.5f)),
                modifier = Modifier.width(180.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("🔥 پیشنهاد ویژه لایو!", color = FuchsiaAccent, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(liveProduct.title, color = Color.White, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                    Text(liveProduct.price.toPersianCurrency(), color = EmeraldSuccess, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = {
                            com.example.data.AppState.addToCart(liveProduct, storeDisplayName, 1)
                            showBuySuccessDialog = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = IndigoPrimary),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth().height(36.dp)
                    ) {
                        Text("خرید فوری", style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
        }

        // Comments
        LazyColumn(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth(0.65f)
                .height(200.dp)
                .padding(start = 16.dp, bottom = 80.dp),
            verticalArrangement = Arrangement.Bottom
        ) {
            items(comments) { comment ->
                Row(
                    modifier = Modifier.padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AsyncImage(
                        model = "https://images.unsplash.com/photo-1438761681033-6461ffad8d80?auto=format&fit=crop&w=200&q=80",
                        contentDescription = "User",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.size(24.dp).clip(CircleShape)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(comment, color = Color.White, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }

        // Gift Notification
        showGiftSentMessage?.let { msg ->
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color(0xFFF59E0B).copy(alpha = 0.9f),
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(16.dp)
            ) {
                Text(
                    msg,
                    color = Color.Black,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
        }

        // Bottom Input Row
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = commentInput,
                onValueChange = { commentInput = it },
                placeholder = { Text("ثبت نظر در لایو...", color = Color.LightGray) },
                modifier = Modifier.weight(1f).height(52.dp),
                shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = GlassBorder,
                    focusedBorderColor = Color.White,
                    unfocusedContainerColor = GlassBackground,
                    focusedContainerColor = GlassBackground,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                singleLine = true
            )
            Spacer(modifier = Modifier.width(8.dp))
            
            // Send Comment Button
            IconButton(
                onClick = {
                    if (commentInput.isNotBlank()) {
                        comments = comments + "من: ${commentInput.trim()}"
                        commentInput = ""
                    }
                },
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(IndigoPrimary)
            ) {
                Icon(Icons.Default.Send, contentDescription = "Send", tint = Color.White, modifier = Modifier.size(20.dp))
            }
            
            Spacer(modifier = Modifier.width(8.dp))
            
            // Donate Coin / Gift Button (ارسال گل یا سکه لایو)
            IconButton(
                onClick = {
                    if (com.example.data.AppState.kookCoins >= 5) {
                        com.example.data.AppState.kookCoins -= 5
                        showGiftSentMessage = "🌹 ۵ سکه هدیه به لایو استریمر اهدا شد!"
                        comments = comments + "من یک گل 🌹 اهدا کردم"
                    } else {
                        showGiftSentMessage = "موجودی سکه کافی نیست! از تب کیف پول شارژ کنید."
                    }
                },
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFD97706))
            ) {
                Text("🪙", fontSize = 20.sp)
            }
        }

        if (showBuySuccessDialog) {
            AlertDialog(
                onDismissRequest = { showBuySuccessDialog = false },
                containerColor = Color(0xFF1E293B),
                title = { Text("افزوده شد به سبد خرید 🛍️", color = Color.White, fontWeight = FontWeight.Bold) },
                text = {
                    Text(
                        "کتونی نایک ایر با تخفیف ویژه لایو به سبد خرید شما اضافه شد. هم‌اکنون می‌توانید از تب کیف پول خرید خود را تکمیل کنید.",
                        color = SlateMuted
                    )
                },
                confirmButton = {
                    Button(
                        onClick = { showBuySuccessDialog = false },
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldSuccess)
                    ) {
                        Text("متوجه شدم", fontWeight = FontWeight.Bold)
                    }
                }
            )
        }
    }
}
