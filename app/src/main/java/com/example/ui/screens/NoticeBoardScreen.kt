package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entities.NoticeItem
import com.example.ui.MainViewModel
import com.example.ui.ScreenTab
import com.example.ui.components.FilterChipRow
import com.example.ui.theme.ErrorContainer
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.InfoContainer
import com.example.ui.theme.InfoBlue
import com.example.ui.theme.SuccessContainer
import com.example.ui.theme.SuccessGreen

@Composable
fun NoticeBoardScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val notices by viewModel.notices.collectAsState()
    val context = LocalContext.current

    var selectedCategory by remember { mutableStateOf("সব নোটিশ") }
    val categories = listOf("সব নোটিশ", "চাকরির খবর", "পরীক্ষার রেজাল্ট", "প্রবেশপত্র", "বিশেষ ছাড়")

    val filteredNotices = notices.filter { notice ->
        selectedCategory == "সব নোটিশ" || notice.category == selectedCategory
    }

    Column(
        modifier = modifier.fillMaxSize().testTag("notice_board_screen_content")
    ) {
        // Category Filter
        FilterChipRow(
            categories = categories,
            selectedCategory = selectedCategory,
            onCategorySelected = { selectedCategory = it }
        )

        LazyColumn(
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 80.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            if (filteredNotices.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Outlined.Campaign,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.outline
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                "বর্তমানে কোনো নোটিশ নেই",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }

            items(filteredNotices) { notice ->
                NoticeCard(
                    notice = notice,
                    onOpenLink = { url -> viewModel.openWebUrl(context, url) },
                    onContact = { viewModel.openWhatsApp(context, "আসসালামু আলাইকুম, আমি নোটিশ সম্পর্কিত তথ্য জানতে চাচ্ছি: ${notice.title}") }
                )
            }
        }
    }
}

@Composable
fun NoticeCard(
    notice: NoticeItem,
    onOpenLink: (String) -> Unit,
    onContact: () -> Unit
) {
    val (categoryColor, categoryBg) = when (notice.category) {
        "চাকরির খবর" -> MaterialTheme.colorScheme.primary to MaterialTheme.colorScheme.primaryContainer
        "পরীক্ষার রেজাল্ট" -> InfoBlue to InfoContainer
        "বিশেষ ছাড়" -> SuccessGreen to SuccessContainer
        else -> MaterialTheme.colorScheme.tertiary to MaterialTheme.colorScheme.tertiaryContainer
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(
            if (notice.isUrgent) 1.5.dp else 1.dp,
            if (notice.isUrgent) ErrorRed else MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
        ),
        modifier = Modifier.fillMaxWidth().testTag("notice_card_${notice.id}")
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header Row: Category Badge + Date + Urgent Pill
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(
                        color = categoryBg,
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = notice.category,
                            color = categoryColor,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }

                    if (notice.isUrgent) {
                        Surface(
                            color = ErrorContainer,
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = "জরুরি",
                                color = ErrorRed,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                Text(
                    text = notice.date,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Title
            Text(
                text = notice.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                lineHeight = 22.sp
            )

            // Description
            Text(
                text = notice.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 18.sp
            )

            // Deadline if present
            if (notice.deadline.isNotBlank()) {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            Icons.Filled.Event,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "আবেদনের শেষ তারিখ / সময়সীমা: ${notice.deadline}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (notice.link.isNotBlank()) {
                    OutlinedButton(
                        onClick = { onOpenLink(notice.link) },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Filled.OpenInNew, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("সার্কুলার লিঙ্ক", fontSize = 11.sp)
                    }
                }

                Button(
                    onClick = onContact,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Filled.Chat, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("অনলাইনে আবেদন", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
