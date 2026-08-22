package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entities.ServiceItem
import com.example.ui.MainViewModel
import com.example.ui.components.FilterChipRow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServicesScreen(
    viewModel: MainViewModel,
    onSelectServiceDetails: (ServiceItem) -> Unit,
    onApplyOnline: (ServiceItem) -> Unit,
    modifier: Modifier = Modifier
) {
    val services by viewModel.services.collectAsState()
    val selectedCategory by viewModel.selectedServiceCategory.collectAsState()
    val searchQuery by viewModel.serviceSearchQuery.collectAsState()
    val context = LocalContext.current

    val categories = listOf(
        "সকল সেবা",
        "শিক্ষা ও ফলাফল",
        "নাগরিক ও সরকারি সেবা",
        "চাকরির আবেদন",
        "ডিজিটাল ও প্রিন্টিং কাজ"
    )

    val filteredServices = services.filter { service ->
        val matchesCategory = (selectedCategory == "সকল সেবা" || service.category == selectedCategory)
        val matchesSearch = if (searchQuery.isBlank()) true else {
            service.title.contains(searchQuery, ignoreCase = true) ||
            service.description.contains(searchQuery, ignoreCase = true) ||
            service.requiredDocs.contains(searchQuery, ignoreCase = true)
        }
        matchesCategory && matchesSearch
    }

    Column(
        modifier = modifier.fillMaxSize().testTag("services_screen_content")
    ) {
        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { viewModel.setServiceSearchQuery(it) },
            placeholder = { Text("সেবা বা প্রয়োজনীয় কাগজ খুঁজুন...", fontSize = 13.sp) },
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
            trailingIcon = {
                if (searchQuery.isNotBlank()) {
                    IconButton(onClick = { viewModel.setServiceSearchQuery("") }) {
                        Icon(Icons.Filled.Clear, contentDescription = "Clear")
                    }
                }
            },
            shape = RoundedCornerShape(14.dp),
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .testTag("service_search_field")
        )

        // Categories Chips
        FilterChipRow(
            categories = categories,
            selectedCategory = selectedCategory,
            onCategorySelected = { viewModel.setServiceCategory(it) }
        )

        // Service List
        LazyColumn(
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 80.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            if (filteredServices.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Outlined.SearchOff,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.outline
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                "কোনো সেবা খুঁজে পাওয়া যায়নি",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }

            items(filteredServices) { service ->
                ServiceItemCard(
                    service = service,
                    onViewDetails = { onSelectServiceDetails(service) },
                    onApplyOnline = { onApplyOnline(service) },
                    onOfficialUrl = { url -> viewModel.openWebUrl(context, url) }
                )
            }
        }
    }
}

@Composable
fun ServiceItemCard(
    service: ServiceItem,
    onViewDetails: () -> Unit,
    onApplyOnline: () -> Unit,
    onOfficialUrl: (String) -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)),
        modifier = Modifier.fillMaxWidth().testTag("service_card_${service.id}")
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = when {
                            service.category.contains("শিক্ষা") -> Icons.Filled.School
                            service.category.contains("নাগরিক") -> Icons.Filled.Badge
                            service.category.contains("চাকরি") -> Icons.Filled.WorkOutline
                            else -> Icons.Filled.Print
                        },
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = service.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        lineHeight = 20.sp
                    )
                    Text(
                        text = service.category,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // Description
            Text(
                text = service.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 18.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            // Fee & Time Pill Info
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        Column {
                            Text("সরকারি ফি", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(if (service.govtFee > 0) "৳${service.govtFee}" else "ফ্রি (০)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                        Column {
                            Text("দোকান চার্জ", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("৳${service.storeCharge}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        }
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text("আনুমানিক সময়", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(service.estimatedTime, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onViewDetails,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f).testTag("service_details_btn")
                ) {
                    Icon(Icons.Filled.Info, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("কাগজপত্র", fontSize = 12.sp)
                }

                Button(
                    onClick = onApplyOnline,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1.2f).testTag("service_apply_btn")
                ) {
                    Icon(Icons.Filled.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("আবেদন করুন", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
