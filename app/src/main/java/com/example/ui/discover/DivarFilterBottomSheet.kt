package com.example.ui.discover

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.DivarTaxonomy
import com.example.data.MainCategory
import com.example.data.Province
import com.example.ui.theme.*
import com.example.utils.toPersianCurrency
import com.example.utils.toPersianNumber

data class DivarFilterState(
    val selectedProvince: String = "تهران",
    val selectedCities: Set<String> = setOf("تهران"),
    val selectedMainCategory: String? = null,
    val selectedSubCategories: Set<String> = emptySet(),
    val minPrice: Long? = null,
    val maxPrice: Long? = null,
    val onlyDiscounted: Boolean = false,
    val onlyInStock: Boolean = true,
    val onlyLiveOrVideo: Boolean = false,
    val onlyFreeDelivery: Boolean = false,
    val sortBy: SortOption = SortOption.NEWEST
) {
    val activeFiltersCount: Int
        get() {
            var count = 0
            if (selectedProvince != "تهران" || selectedCities != setOf("تهران")) count++
            if (selectedMainCategory != null) count++
            if (selectedSubCategories.isNotEmpty()) count++
            if (minPrice != null || maxPrice != null) count++
            if (onlyDiscounted) count++
            if (!onlyInStock) count++
            if (onlyLiveOrVideo) count++
            if (onlyFreeDelivery) count++
            if (sortBy != SortOption.NEWEST) count++
            return count
        }
}

enum class SortOption(val title: String) {
    NEWEST("جدیدترین"),
    POPULAR("محبوب‌ترین (بازدید بالا)"),
    PRICE_LOW_TO_HIGH("ارزان‌ترین"),
    PRICE_HIGH_TO_LOW("گران‌ترین"),
    DISCOUNT_PERCENT("بیشترین تخفیف")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DivarFilterBottomSheet(
    filterState: DivarFilterState,
    onFilterChange: (DivarFilterState) -> Unit,
    onDismiss: () -> Unit
) {
    var tempState by remember { mutableStateOf(filterState) }
    var activeTab by remember { mutableStateOf(FilterTab.ALL) } // Tab for quick navigation: ALL, LOCATION, CATEGORY, PRICE_SORT

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF140D24),
        dragHandle = {
            BottomSheetDefaults.DragHandle(color = GlassBorder)
        },
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.92f)
                .padding(horizontal = 16.dp)
        ) {
            // Top Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .background(
                                Brush.linearGradient(listOf(Color(0xFFE50914), Color(0xFFFF5252))),
                                RoundedCornerShape(12.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Tune,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            "فیلترهای پیشرفته دیوار",
                            color = Color.White,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "جستجوی دقیق بر اساس استان، چند شهر و زیردسته‌ها",
                            color = SlateMuted,
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }

                TextButton(
                    onClick = {
                        tempState = DivarFilterState()
                    }
                ) {
                    Text("حذف همه", color = FuchsiaAccent, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                }
            }

            // Filter Tabs (دسته‌بندی‌ها، شهرها، قیمت و مرتب‌سازی)
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                items(FilterTab.values()) { tab ->
                    val isSelected = activeTab == tab
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = if (isSelected) IndigoPrimary else GlassBackground,
                        border = BorderStroke(1.dp, if (isSelected) IndigoPrimary else GlassBorder),
                        modifier = Modifier.clickable { activeTab = tab }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(tab.icon, fontSize = 13.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                tab.title,
                                color = if (isSelected) Color.White else SlateText,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            }

            HorizontalDivider(color = GlassBorder)

            // Scrollable Content
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // LOCATION: Province & Multi-city selection
                if (activeTab == FilterTab.ALL || activeTab == FilterTab.LOCATION) {
                    item {
                        ProvinceAndMultiCitySelector(
                            selectedProvince = tempState.selectedProvince,
                            selectedCities = tempState.selectedCities,
                            onProvinceChanged = { provinceName, defaultCities ->
                                tempState = tempState.copy(
                                    selectedProvince = provinceName,
                                    selectedCities = defaultCities
                                )
                            },
                            onToggleCity = { city ->
                                val currentCities = tempState.selectedCities.toMutableSet()
                                if (city.startsWith("همه شهرهای")) {
                                    // Select all or reset
                                    val prov = DivarTaxonomy.provinces.find { it.name == tempState.selectedProvince }
                                    if (prov != null) {
                                        if (currentCities.contains(city)) {
                                            currentCities.clear()
                                        } else {
                                            currentCities.clear()
                                            currentCities.addAll(prov.cities)
                                        }
                                    }
                                } else {
                                    if (currentCities.contains(city)) {
                                        currentCities.remove(city)
                                    } else {
                                        currentCities.add(city)
                                    }
                                }
                                tempState = tempState.copy(selectedCities = currentCities)
                            }
                        )
                    }
                }

                // CATEGORIES: Main categories & Subcategories
                if (activeTab == FilterTab.ALL || activeTab == FilterTab.CATEGORY) {
                    item {
                        MainCategoryAndSubCategorySelector(
                            selectedMain = tempState.selectedMainCategory,
                            selectedSubs = tempState.selectedSubCategories,
                            onSelectMain = { mainCat ->
                                tempState = if (tempState.selectedMainCategory == mainCat) {
                                    tempState.copy(selectedMainCategory = null, selectedSubCategories = emptySet())
                                } else {
                                    tempState.copy(selectedMainCategory = mainCat, selectedSubCategories = emptySet())
                                }
                            },
                            onToggleSub = { subCat ->
                                val currentSubs = tempState.selectedSubCategories.toMutableSet()
                                if (subCat.startsWith("همه زیردسته‌های")) {
                                    val cat = DivarTaxonomy.categories.find { it.name == tempState.selectedMainCategory }
                                    if (cat != null) {
                                        if (currentSubs.contains(subCat)) {
                                            currentSubs.clear()
                                        } else {
                                            currentSubs.clear()
                                            currentSubs.addAll(cat.subCategories)
                                        }
                                    }
                                } else {
                                    if (currentSubs.contains(subCat)) {
                                        currentSubs.remove(subCat)
                                    } else {
                                        currentSubs.add(subCat)
                                    }
                                }
                                tempState = tempState.copy(selectedSubCategories = currentSubs)
                            }
                        )
                    }
                }

                // PRICE & SORT
                if (activeTab == FilterTab.ALL || activeTab == FilterTab.PRICE_SORT) {
                    // Sorting
                    item {
                        FilterSectionHeader(title = "مرتب‌سازی آگهی‌ها (دیوار)", icon = Icons.Default.Sort)
                        Spacer(modifier = Modifier.height(8.dp))
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            SortOption.values().forEach { option ->
                                val isSelected = tempState.sortBy == option
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(
                                            if (isSelected) IndigoPrimary.copy(alpha = 0.2f) else GlassBackground,
                                            RoundedCornerShape(12.dp)
                                        )
                                        .clickable { tempState = tempState.copy(sortBy = option) }
                                        .padding(horizontal = 14.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        option.title,
                                        color = if (isSelected) IndigoPrimary else SlateText,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                    RadioButton(
                                        selected = isSelected,
                                        onClick = { tempState = tempState.copy(sortBy = option) },
                                        colors = RadioButtonDefaults.colors(
                                            selectedColor = IndigoPrimary,
                                            unselectedColor = SlateMuted
                                        )
                                    )
                                }
                            }
                        }
                    }

                    // Price Range Filter
                    item {
                        FilterSectionHeader(title = "محدوده قیمت (تومان)", icon = Icons.Default.MonetizationOn)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedTextField(
                                value = tempState.minPrice?.toString() ?: "",
                                onValueChange = {
                                    tempState = tempState.copy(minPrice = it.filter { ch -> ch.isDigit() }.toLongOrNull())
                                },
                                label = { Text("از قیمت", color = SlateMuted, fontSize = 12.sp) },
                                placeholder = { Text("مثلا ۱,۰۰۰,۰۰۰", color = SlateMuted.copy(alpha = 0.5f), fontSize = 11.sp) },
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = IndigoPrimary,
                                    unfocusedBorderColor = GlassBorder,
                                    focusedTextColor = SlateText,
                                    unfocusedTextColor = SlateText,
                                    focusedContainerColor = GlassBackground,
                                    unfocusedContainerColor = GlassBackground
                                )
                            )

                            OutlinedTextField(
                                value = tempState.maxPrice?.toString() ?: "",
                                onValueChange = {
                                    tempState = tempState.copy(maxPrice = it.filter { ch -> ch.isDigit() }.toLongOrNull())
                                },
                                label = { Text("تا قیمت", color = SlateMuted, fontSize = 12.sp) },
                                placeholder = { Text("مثلا ۱۰,۰۰۰,۰۰۰", color = SlateMuted.copy(alpha = 0.5f), fontSize = 11.sp) },
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = IndigoPrimary,
                                    unfocusedBorderColor = GlassBorder,
                                    focusedTextColor = SlateText,
                                    unfocusedTextColor = SlateText,
                                    focusedContainerColor = GlassBackground,
                                    unfocusedContainerColor = GlassBackground
                                )
                            )
                        }
                    }

                    // Additional Smart Toggles
                    item {
                        FilterSectionHeader(title = "سایر فیلترهای پیشرفته بازار", icon = Icons.Default.FilterList)
                        Spacer(modifier = Modifier.height(8.dp))

                        FilterSwitchRow(
                            title = "فقط آگهی‌های تخفیف‌دار (حراج)",
                            checked = tempState.onlyDiscounted,
                            onCheckedChange = { tempState = tempState.copy(onlyDiscounted = it) }
                        )

                        FilterSwitchRow(
                            title = "فقط کالاهای موجود در انبار",
                            checked = tempState.onlyInStock,
                            onCheckedChange = { tempState = tempState.copy(onlyInStock = it) }
                        )

                        FilterSwitchRow(
                            title = "دارای ویدیو پرو یا لایو خرید آنلاین",
                            checked = tempState.onlyLiveOrVideo,
                            onCheckedChange = { tempState = tempState.copy(onlyLiveOrVideo = it) }
                        )

                        FilterSwitchRow(
                            title = "ارسال رایگان سراسری",
                            checked = tempState.onlyFreeDelivery,
                            onCheckedChange = { tempState = tempState.copy(onlyFreeDelivery = it) }
                        )
                    }
                }
            }

            HorizontalDivider(color = GlassBorder)

            // Bottom Action Apply Button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = {
                        onFilterChange(tempState)
                        onDismiss()
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = IndigoPrimary)
                ) {
                    Text(
                        "اعمال فیلترها (${tempState.activeFiltersCount.toPersianNumber()} فعال)",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.height(50.dp),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, GlassBorder)
                ) {
                    Text("انصراف", color = SlateMuted)
                }
            }
        }
    }
}

enum class FilterTab(val title: String, val icon: String) {
    ALL("همه فیلترها", "⚡"),
    LOCATION("استان و شهرها", "📍"),
    CATEGORY("دسته‌بندی و زیردسته‌ها", "🏷️"),
    PRICE_SORT("قیمت و ترتیب", "💰")
}

@Composable
fun ProvinceAndMultiCitySelector(
    selectedProvince: String,
    selectedCities: Set<String>,
    onProvinceChanged: (String, Set<String>) -> Unit,
    onToggleCity: (String) -> Unit
) {
    val currentProvinceObj = DivarTaxonomy.provinces.find { it.name == selectedProvince } 
        ?: DivarTaxonomy.provinces.first()

    Column(modifier = Modifier.fillMaxWidth()) {
        FilterSectionHeader(title = "استان و انتخاب چند شهر", icon = Icons.Default.LocationCity)
        Spacer(modifier = Modifier.height(8.dp))

        // Step 1: Province Selection Horizontal Row
        Text(
            "۱. انتخاب استان:",
            color = SlateMuted,
            style = MaterialTheme.typography.labelMedium
        )
        Spacer(modifier = Modifier.height(6.dp))
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(DivarTaxonomy.provinces) { prov ->
                val isSelected = prov.name == selectedProvince
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isSelected) IndigoPrimary else GlassBackground,
                    border = BorderStroke(1.dp, if (isSelected) IndigoPrimary else GlassBorder),
                    modifier = Modifier.clickable {
                        onProvinceChanged(prov.name, setOf(prov.cities.first()))
                    }
                ) {
                    Text(
                        text = prov.name,
                        color = if (isSelected) Color.White else SlateText,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Step 2: Multi-City Selection Grid/Wrap
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "۲. انتخاب شهرها در استان ${currentProvinceObj.name} (امکان انتخاب چند شهر):",
                color = SlateMuted,
                style = MaterialTheme.typography.labelMedium
            )
            if (selectedCities.isNotEmpty()) {
                Text(
                    "${selectedCities.size.toPersianNumber()} شهر انتخاب شده",
                    color = FuchsiaAccent,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Cities as Multi-Select Chips
        FlowRowLayout(
            items = currentProvinceObj.cities,
            selectedItems = selectedCities,
            onItemClick = onToggleCity
        )
    }
}

@Composable
fun MainCategoryAndSubCategorySelector(
    selectedMain: String?,
    selectedSubs: Set<String>,
    onSelectMain: (String) -> Unit,
    onToggleSub: (String) -> Unit
) {
    val currentMainObj = DivarTaxonomy.categories.find { it.name == selectedMain }

    Column(modifier = Modifier.fillMaxWidth()) {
        FilterSectionHeader(title = "دسته‌بندی اصلی و زیردسته‌ها (دیوار)", icon = Icons.Default.Category)
        Spacer(modifier = Modifier.height(8.dp))

        // Step 1: Main Category Cards
        Text(
            "۱. انتخاب دسته‌بندی اصلی:",
            color = SlateMuted,
            style = MaterialTheme.typography.labelMedium
        )
        Spacer(modifier = Modifier.height(6.dp))

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(DivarTaxonomy.categories) { cat ->
                val isSelected = cat.name == selectedMain
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = if (isSelected) FuchsiaAccent else GlassBackground,
                    border = BorderStroke(1.dp, if (isSelected) FuchsiaAccent else GlassBorder),
                    modifier = Modifier.clickable { onSelectMain(cat.name) }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(cat.icon, fontSize = 16.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = cat.name,
                            color = if (isSelected) Color.White else SlateText,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Step 2: Subcategories of selected Main Category
        AnimatedVisibility(visible = currentMainObj != null) {
            currentMainObj?.let { mainCat ->
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "۲. زیردسته‌های ${mainCat.name} (چند انتخابی):",
                            color = SlateMuted,
                            style = MaterialTheme.typography.labelMedium
                        )
                        if (selectedSubs.isNotEmpty()) {
                            Text(
                                "${selectedSubs.size.toPersianNumber()} زیردسته انتخاب شده",
                                color = FuchsiaAccent,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))

                    FlowRowLayout(
                        items = mainCat.subCategories,
                        selectedItems = selectedSubs,
                        onItemClick = onToggleSub
                    )
                }
            }
        }

        if (currentMainObj == null) {
            Text(
                "برای مشاهده و انتخاب زیردسته‌ها، ابتدا یک دسته‌بندی اصلی را انتخاب نمایید.",
                color = SlateMuted.copy(alpha = 0.7f),
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
fun FlowRowLayout(
    items: List<String>,
    selectedItems: Set<String>,
    onItemClick: (String) -> Unit
) {
    // Custom flow grid arrangement for multi-selection chips
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        val chunked = items.chunked(2)
        chunked.forEach { rowItems ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                rowItems.forEach { item ->
                    val isSelected = selectedItems.contains(item)
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) IndigoPrimary.copy(alpha = 0.25f) else GlassBackground,
                        border = BorderStroke(1.dp, if (isSelected) IndigoPrimary else GlassBorder),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onItemClick(item) }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = item,
                                color = if (isSelected) IndigoPrimary else SlateText,
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Checkbox(
                                checked = isSelected,
                                onCheckedChange = { onItemClick(item) },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = IndigoPrimary,
                                    uncheckedColor = SlateMuted
                                ),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
                if (rowItems.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
fun FilterSectionHeader(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = IndigoPrimary, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = title,
            color = SlateText,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun FilterSwitchRow(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(title, color = SlateText, style = MaterialTheme.typography.bodyMedium)
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = IndigoPrimary,
                uncheckedThumbColor = SlateMuted,
                uncheckedTrackColor = GlassBackground
            )
        )
    }
}
