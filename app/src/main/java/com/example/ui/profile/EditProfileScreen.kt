package com.example.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.AccountType
import com.example.data.AppState
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(onBack: () -> Unit) {
    val user = AppState.currentUser
    val isStore = user.accountType == AccountType.STORE_OWNER || user.isSeller
    
    var name by remember { mutableStateOf(user.name) }
    var username by remember { mutableStateOf(user.username) }
    var bio by remember { mutableStateOf(user.bio ?: "") }
    var city by remember { mutableStateOf(user.city) }
    var interests by remember { mutableStateOf(user.interests) }
    var phone by remember { mutableStateOf(user.phone) }
    var gmail by remember { mutableStateOf(user.gmail) }
    var address by remember { mutableStateOf(user.location?.address ?: "تهران، خیابان ولیعصر") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isStore) "ویرایش اطلاعات فروشگاه" else "ویرایش حساب کاربری", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowForward, contentDescription = "Back", tint = SlateText)
                    }
                },
                actions = {
                    TextButton(onClick = {
                        AppState.currentUser = AppState.currentUser.copy(
                            name = name,
                            username = username,
                            bio = bio,
                            city = city,
                            interests = interests,
                            phone = phone,
                            gmail = gmail
                        )
                        onBack()
                    }) {
                        Text("ذخیره", color = EmeraldSuccess, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = SlateText
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            item {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(modifier = Modifier.size(96.dp)) {
                        AsyncImage(
                            model = user.avatarUrl,
                            contentDescription = "Avatar",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize().clip(CircleShape)
                        )
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .size(30.dp)
                                .clip(CircleShape)
                                .background(IndigoPrimary)
                                .clickable { },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.CameraAlt, contentDescription = "Change Photo", tint = Color.White, modifier = Modifier.size(16.dp))
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text("تغییر تصویر پروفایل", color = IndigoPrimary, style = MaterialTheme.typography.labelLarge)
                }
            }

            item { SectionTitle("اطلاعات فردی و هویتی") }
            item { ProfileTextField(if (isStore) "نام فروشگاه / برند" else "نام و نام خانوادگی", name, { name = it }, icon = Icons.Default.Person) }
            item { ProfileTextField("نام کاربری", username, { username = it }, prefix = "@") }
            item { ProfileTextField("آدرس جیمیل (Gmail)", gmail, { gmail = it }, icon = Icons.Default.Email) }
            item { ProfileTextField("شماره همراه", phone, { phone = it }, icon = Icons.Default.Phone) }
            item { ProfileTextField("شهر محل سکونت", city, { city = it }, icon = Icons.Default.LocationCity) }
            
            item { Spacer(modifier = Modifier.height(16.dp)) }
            item { SectionTitle("شخصی‌سازی سلیقه و علایق") }
            item { ProfileTextField("متن علایق، سبک‌های مورد پسند و کلمات کلیدی", interests, { interests = it }, singleLine = false, icon = Icons.Default.Favorite, modifier = Modifier.height(90.dp)) }
            item { ProfileTextField("بیوگرافی و معرفی کوتاه", bio, { bio = it }, singleLine = false, icon = Icons.Default.Description, modifier = Modifier.height(90.dp)) }

            if (isStore) {
                item { Spacer(modifier = Modifier.height(16.dp)) }
                item { SectionTitle("اطلاعات رسمی و آدرس فروشگاه") }
                item { ProfileTextField("آدرس فروشگاه / محل تحویل حضوری", address, { address = it }, singleLine = false, icon = Icons.Default.Place) }
            }

            item { Spacer(modifier = Modifier.height(40.dp)) }
        }
    }
}

@Composable
fun SectionTitle(title: String) {
    Text(
        text = title,
        color = SlateMuted,
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(vertical = 8.dp)
    )
}

@Composable
fun ProfileTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    singleLine: Boolean = true,
    prefix: String? = null,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, color = SlateMuted) },
        leadingIcon = if (icon != null || prefix != null) {
            {
                if (icon != null) {
                    Icon(icon, contentDescription = null, tint = IndigoPrimary, modifier = Modifier.size(20.dp))
                } else if (prefix != null) {
                    Text(prefix, color = SlateMuted)
                }
            }
        } else null,
        modifier = modifier.fillMaxWidth().padding(vertical = 4.dp),
        singleLine = singleLine,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = IndigoPrimary,
            unfocusedBorderColor = GlassBorder,
            focusedTextColor = SlateText,
            unfocusedTextColor = SlateText,
            cursorColor = IndigoPrimary,
            focusedContainerColor = Color(0xFF1E293B).copy(alpha = 0.5f),
            unfocusedContainerColor = Color(0xFF1E293B).copy(alpha = 0.3f)
        ),
        shape = RoundedCornerShape(12.dp)
    )
}
