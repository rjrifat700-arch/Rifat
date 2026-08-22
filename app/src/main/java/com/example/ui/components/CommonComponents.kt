package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entities.*
import com.example.ui.MainViewModel
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StoreTopAppBar(
    viewModel: MainViewModel,
    onOpenAdmin: () -> Unit,
    onOpenTracking: () -> Unit,
    onOpenCart: () -> Unit,
    modifier: Modifier = Modifier
) {
    val cart by viewModel.cart.collectAsState()
    val totalCartItems = cart.values.sumOf { it.quantity }
    val isAdmin by viewModel.isAdminAuthenticated.collectAsState()

    TopAppBar(
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Computer,
                        contentDescription = "Store Logo",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Column {
                    Text(
                        text = "রিফাত কম্পিউটার",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                    Text(
                        text = "& ভ্যারাইটি স্টোর",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        },
        actions = {
            // Tracking Button
            IconButton(
                onClick = onOpenTracking,
                modifier = Modifier.testTag("track_button")
            ) {
                Icon(
                    imageVector = Icons.Outlined.Search,
                    contentDescription = "ট্র্যাকিং করুন",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Cart Button with Badge
            IconButton(
                onClick = onOpenCart,
                modifier = Modifier.testTag("cart_button")
            ) {
                BadgedBox(
                    badge = {
                        if (totalCartItems > 0) {
                            Badge(
                                containerColor = MaterialTheme.colorScheme.tertiary,
                                contentColor = MaterialTheme.colorScheme.onTertiary
                            ) {
                                Text("$totalCartItems", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Outlined.ShoppingCart,
                        contentDescription = "শপিং কার্ট",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Admin Button
            IconButton(
                onClick = onOpenAdmin,
                modifier = Modifier.testTag("admin_button")
            ) {
                Icon(
                    imageVector = if (isAdmin) Icons.Filled.AdminPanelSettings else Icons.Outlined.AdminPanelSettings,
                    contentDescription = "অ্যাডমিন মোড",
                    tint = if (isAdmin) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface
        ),
        modifier = modifier
    )
}

@Composable
fun StatusBadge(
    status: String,
    modifier: Modifier = Modifier
) {
    val (bgColor, textColor) = when {
        status.contains("পেন্ডিং") || status.contains("নতুন") -> WarningContainer to WarningOrange
        status.contains("প্রসেসিং") || status.contains("প্রক্রিয়া") -> InfoContainer to InfoBlue
        status.contains("সম্পন্ন") || status.contains("ডেলিভারি প্রস্তুত") || status.contains("ডেলিভার্ড") -> SuccessContainer to SuccessGreen
        else -> MaterialTheme.colorScheme.surfaceVariant to MaterialTheme.colorScheme.onSurfaceVariant
    }

    Surface(
        color = bgColor,
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(textColor)
            )
            Text(
                text = status,
                color = textColor,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun FilterChipRow(
    categories: List<String>,
    selectedCategory: String,
    onCategorySelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier
    ) {
        items(categories) { category ->
            val isSelected = category == selectedCategory
            FilterChip(
                selected = isSelected,
                onClick = { onCategorySelected(category) },
                label = {
                    Text(
                        text = category,
                        fontSize = 13.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                    labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                border = null,
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.testTag("filter_chip_$category")
            )
        }
    }
}

@Composable
fun CommunicationChannelsCard(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "সরাসরি যোগাযোগ ও সহায়তা",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "সকাল ৮টা - রাত ১০টা",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                CommunicationButton(
                    icon = Icons.Filled.Chat,
                    label = "হোয়াটসঅ্যাপ",
                    color = Color(0xFF25D366),
                    onClick = { viewModel.openWhatsApp(context) }
                )
                CommunicationButton(
                    icon = Icons.Filled.Call,
                    label = "সরাসরি কল",
                    color = Color(0xFF0284C7),
                    onClick = { viewModel.callStore(context) }
                )
                CommunicationButton(
                    icon = Icons.Filled.Share,
                    label = "ফেসবুক পেজ",
                    color = Color(0xFF1877F2),
                    onClick = { viewModel.openFacebookPage(context) }
                )
                CommunicationButton(
                    icon = Icons.Filled.LocationOn,
                    label = "দোকানের ম্যাপ",
                    color = Color(0xFFEA580C),
                    onClick = { viewModel.openLocationMap(context) }
                )
            }
        }
    }
}

@Composable
private fun CommunicationButton(
    icon: ImageVector,
    label: String,
    color: Color,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = color,
                modifier = Modifier.size(24.dp)
            )
        }
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun ServiceDetailsDialog(
    service: ServiceItem,
    onDismiss: () -> Unit,
    onApplyOnline: () -> Unit,
    onOpenOfficialUrl: (String) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = onApplyOnline,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.testTag("apply_online_modal_btn")
            ) {
                Icon(Icons.Filled.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("অনলাইনে আবেদন করুন", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("বন্ধ করুন")
            }
        },
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Filled.Description,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Text(
                    text = service.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = service.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Pricing and Time card
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("সরকারি ফি", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                if (service.govtFee > 0) "৳${service.govtFee}" else "ফ্রি (০)",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Divider(modifier = Modifier.height(30.dp).width(1.dp))
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("সার্ভিস চার্জ", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("৳${service.storeCharge}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.tertiary)
                        }
                        Divider(modifier = Modifier.height(30.dp).width(1.dp))
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("সময় লাগবে", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(service.estimatedTime, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        }
                    }
                }

                // Required Documents
                Text(
                    text = "প্রয়োজনীয় কাগজপত্র:",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = service.requiredDocs,
                        fontSize = 12.sp,
                        lineHeight = 18.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(10.dp)
                    )
                }

                // Official Link Button if available
                if (service.officialUrl.isNotBlank()) {
                    OutlinedButton(
                        onClick = { onOpenOfficialUrl(service.officialUrl) },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Filled.Language, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("অফিশিয়াল পোর্টাল ভিজিট করুন", fontSize = 12.sp)
                    }
                }
            }
        }
    )
}

@Composable
fun ServiceApplicationDialog(
    service: ServiceItem,
    onDismiss: () -> Unit,
    onSubmit: (applicantName: String, phone: String, details: String, delivery: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var details by remember { mutableStateOf("") }
    var deliveryOption by remember { mutableStateOf("দোকান থেকে প্রিন্ট কপি সংগ্রহ") }
    var errorMessage by remember { mutableStateOf("") }

    val deliveryOptions = listOf(
        "দোকান থেকে প্রিন্ট কপি সংগ্রহ",
        "WhatsApp এ PDF কপি সংগ্রহ",
        "জরুরি ইমেইল ডেলিভারি"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = {
                    if (name.isBlank() || phone.isBlank()) {
                        errorMessage = "দয়া করে আবেদনকারীর নাম ও মোবাইল নম্বর পূরণ করুন।"
                    } else if (phone.length < 11) {
                        errorMessage = "সঠিক ১১ ডিজিটের মোবাইল নম্বর দিন।"
                    } else {
                        errorMessage = ""
                        onSubmit(name, phone, details, deliveryOption)
                    }
                },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                modifier = Modifier.testTag("submit_service_app_btn")
            ) {
                Text("আবেদন নিশ্চিত করুন", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("বাতিল")
            }
        },
        title = {
            Text(
                text = "অনলাইন আবেদন ফর্ম",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "সেবা: ${service.title}",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Text(
                    text = "মোট খরচের হিসাব: সরকারি ফি ৳${service.govtFee} + চার্জ ৳${service.storeCharge} = মোট ৳${service.govtFee + service.storeCharge}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.tertiary,
                    fontWeight = FontWeight.SemiBold
                )

                if (errorMessage.isNotBlank()) {
                    Text(
                        text = errorMessage,
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("আবেদনকারীর পূর্ণ নাম *") },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth().testTag("app_name_input")
                )

                OutlinedTextField(
                    value = phone,
                    onValueChange = { if (it.length <= 11) phone = it },
                    label = { Text("সক্রিয় মোবাইল নম্বর (WhatsApp) *") },
                    placeholder = { Text("01706727116") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth().testTag("app_phone_input")
                )

                OutlinedTextField(
                    value = details,
                    onValueChange = { details = it },
                    label = { Text("প্রয়োজনীয় বিবরণ (রোল/রেজিস্ট্রেশন/NID/তথ্য)") },
                    placeholder = { Text("যেমন: SSC Roll: 123456, Reg: 789012, Board: Dhaka") },
                    minLines = 2,
                    maxLines = 4,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth().testTag("app_details_input")
                )

                Text(
                    text = "ডেলিভারি নেওয়ার পদ্ধতি:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )

                deliveryOptions.forEach { option ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { deliveryOption = option }
                            .padding(vertical = 2.dp)
                    ) {
                        RadioButton(
                            selected = (deliveryOption == option),
                            onClick = { deliveryOption = option }
                        )
                        Text(text = option, fontSize = 12.sp)
                    }
                }
            }
        }
    )
}

@Composable
fun ApplicationSuccessDialog(
    application: ServiceApplication,
    onDismiss: () -> Unit,
    onOpenWhatsApp: (String) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = {
                    val msg = "আসসালামু আলাইকুম, আমি অনলাইনে আবেদন জমা দিয়েছি। ট্র্যাকিং কোড: ${application.trackingCode}, সেবা: ${application.serviceTitle}, নাম: ${application.applicantName}"
                    onOpenWhatsApp(msg)
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Filled.Chat, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("WhatsApp এ নিশ্চিত করুন", color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("ঠিক আছে")
            }
        },
        icon = {
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(CircleShape)
                    .background(SuccessContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = SuccessGreen, modifier = Modifier.size(34.dp))
            }
        },
        title = {
            Text("আবেদন সফলভাবে গৃহীত হয়েছে!", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "আপনার ট্র্যাকিং কোড:",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = application.trackingCode,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                    )
                }
                Text(
                    text = "সেবা: ${application.serviceTitle}\nনাম: ${application.applicantName}\nমোট প্রদেয় ফি: ৳${application.govtFee + application.storeCharge}\nবর্তমান অবস্থা: ${application.status}",
                    fontSize = 12.sp,
                    lineHeight = 18.sp,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "কোডটি সংরক্ষণ করুন। অ্যাপের 'ট্র্যাকিং' অপশন থেকে লাইভ স্ট্যাটাস দেখতে পারবেন।",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center
                )
            }
        }
    )
}

@Composable
fun CartBottomSheet(
    viewModel: MainViewModel,
    onDismiss: () -> Unit,
    onOrderSuccess: (ProductOrder) -> Unit
) {
    val cart by viewModel.cart.collectAsState()
    val appliedPromo by viewModel.appliedPromoCode.collectAsState()
    val discountPercent by viewModel.discountPercent.collectAsState()
    val context = LocalContext.current

    var customerName by remember { mutableStateOf("") }
    var customerPhone by remember { mutableStateOf("") }
    var customerAddress by remember { mutableStateOf("") }
    var deliveryType by remember { mutableStateOf("দোকান থেকে সরাসরি পিকআপ") }
    var promoInput by remember { mutableStateOf("") }
    var promoMessage by remember { mutableStateOf("") }
    var orderError by remember { mutableStateOf("") }

    val itemsList = cart.values.toList()
    val subtotal = itemsList.sumOf { it.product.price * it.quantity }
    val discount = (subtotal * discountPercent) / 100
    val total = subtotal - discount

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = {
                    if (itemsList.isEmpty()) {
                        orderError = "কার্ট খালি!"
                    } else if (customerName.isBlank() || customerPhone.isBlank()) {
                        orderError = "দয়া করে আপনার নাম ও মোবাইল নম্বর পূরণ করুন।"
                    } else if (customerPhone.length < 11) {
                        orderError = "সঠিক ১১ ডিজিটের মোবাইল নম্বর দিন।"
                    } else {
                        orderError = ""
                        viewModel.submitProductOrder(
                            customerName = customerName,
                            mobileNumber = customerPhone,
                            address = if (customerAddress.isBlank()) "দোকান পিকআপ" else customerAddress,
                            deliveryType = deliveryType,
                            onSuccess = { order ->
                                onOrderSuccess(order)
                            }
                        )
                    }
                },
                enabled = itemsList.isNotEmpty(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.testTag("submit_cart_order_btn")
            ) {
                Text("অর্ডার প্লেস করুন (৳$total)", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("বন্ধ করুন")
            }
        },
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("শপিং কার্ট ও অর্ডার", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                if (itemsList.isNotEmpty()) {
                    TextButton(onClick = { viewModel.clearCart() }) {
                        Text("সব মুছুন", color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                    }
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 450.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (itemsList.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Outlined.ShoppingCart, contentDescription = null, modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.outline)
                            Spacer(Modifier.height(8.dp))
                            Text("কার্টে কোন পণ্য যোগ করা হয়নি", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                } else {
                    // Items List
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        itemsList.forEach { cartItem ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(cartItem.product.name, fontSize = 12.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                                    Text("৳${cartItem.product.price} × ${cartItem.quantity} = ৳${cartItem.product.price * cartItem.quantity}", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(
                                        onClick = { viewModel.removeFromCart(cartItem.product.id) },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(Icons.Filled.RemoveCircleOutline, contentDescription = null, modifier = Modifier.size(18.dp))
                                    }
                                    Text("${cartItem.quantity}", fontWeight = FontWeight.Bold, fontSize = 13.sp, modifier = Modifier.padding(horizontal = 4.dp))
                                    IconButton(
                                        onClick = { viewModel.addToCart(cartItem.product) },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(Icons.Filled.AddCircleOutline, contentDescription = null, modifier = Modifier.size(18.dp))
                                    }
                                }
                            }
                        }
                    }

                    // Promo Code Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        OutlinedTextField(
                            value = promoInput,
                            onValueChange = { promoInput = it },
                            placeholder = { Text("প্রোমো কোড (যেমন: RIFAT10)", fontSize = 11.sp) },
                            singleLine = true,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        )
                        Button(
                            onClick = {
                                if (viewModel.applyPromoCode(promoInput)) {
                                    promoMessage = "১০% ডিসকাউন্ট যোগ হয়েছে!"
                                } else {
                                    promoMessage = "ভুল প্রোমো কোড!"
                                }
                            },
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("প্রয়োগ", fontSize = 12.sp)
                        }
                    }

                    if (promoMessage.isNotBlank()) {
                        Text(
                            text = promoMessage,
                            fontSize = 11.sp,
                            color = if (appliedPromo.isNotBlank()) SuccessGreen else MaterialTheme.colorScheme.error
                        )
                    }

                    // Pricing breakdown
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("মোট সাবটোটাল:", fontSize = 12.sp)
                                Text("৳$subtotal", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                            if (discount > 0) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("ডিসকাউন্ট ($discountPercent%):", fontSize = 12.sp, color = SuccessGreen)
                                    Text("-৳$discount", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = SuccessGreen)
                                }
                            }
                            Divider()
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("সর্বমোট প্রদেয়:", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                Text("৳$total", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }

                    if (orderError.isNotBlank()) {
                        Text(orderError, color = MaterialTheme.colorScheme.error, fontSize = 11.sp)
                    }

                    // Customer info
                    OutlinedTextField(
                        value = customerName,
                        onValueChange = { customerName = it },
                        label = { Text("আপনার নাম *", fontSize = 11.sp) },
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = customerPhone,
                        onValueChange = { if (it.length <= 11) customerPhone = it },
                        label = { Text("মোবাইল নম্বর *", fontSize = 11.sp) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            selected = (deliveryType == "দোকান থেকে সরাসরি পিকআপ"),
                            onClick = { deliveryType = "দোকান থেকে সরাসরি পিকআপ" }
                        )
                        Text("দোকান থেকে পিকআপ", fontSize = 12.sp)

                        Spacer(Modifier.width(8.dp))

                        RadioButton(
                            selected = (deliveryType == "হোম ডেলিভারি"),
                            onClick = { deliveryType = "হোম ডেলিভারি" }
                        )
                        Text("হোম ডেলিভারি", fontSize = 12.sp)
                    }

                    if (deliveryType == "হোম ডেলিভারি") {
                        OutlinedTextField(
                            value = customerAddress,
                            onValueChange = { customerAddress = it },
                            label = { Text("ডেলিভারি ঠিকানা", fontSize = 11.sp) },
                            singleLine = true,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    )
}

@Composable
fun OrderSuccessDialog(
    order: ProductOrder,
    onDismiss: () -> Unit,
    onOpenWhatsApp: (String) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = {
                    val msg = "আসসালামু আলাইকুম, আমি প্রোডাক্ট অর্ডার করেছি। অর্ডার কোড: ${order.orderCode}, নাম: ${order.customerName}, মোট মূল্য: ৳${order.totalAmount}\nআইটেমস:\n${order.itemsSummary}"
                    onOpenWhatsApp(msg)
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Filled.Chat, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("WhatsApp এ কনফার্ম করুন", color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("ঠিক আছে")
            }
        },
        icon = {
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(CircleShape)
                    .background(SuccessContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = SuccessGreen, modifier = Modifier.size(34.dp))
            }
        },
        title = {
            Text("অর্ডার সফলভাবে প্লেস হয়েছে!", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("অর্ডার নম্বর / ট্র্যাকিং কোড:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = order.orderCode,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                    )
                }
                Text(
                    text = "গ্রাহক: ${order.customerName}\nমোট মূল্য: ৳${order.totalAmount}\nডেলিভারি: ${order.deliveryType}\nস্ট্যাটাস: ${order.status}",
                    fontSize = 12.sp,
                    lineHeight = 18.sp,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    )
}

@Composable
fun TrackingDialog(
    viewModel: MainViewModel,
    onDismiss: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val trackedApp by viewModel.trackedApplication.collectAsState()
    val trackedOrder by viewModel.trackedOrder.collectAsState()
    val searched by viewModel.trackingSearched.collectAsState()

    AlertDialog(
        onDismissRequest = {
            viewModel.clearTracking()
            onDismiss()
        },
        confirmButton = {
            TextButton(onClick = {
                viewModel.clearTracking()
                onDismiss()
            }) {
                Text("বন্ধ করুন")
            }
        },
        title = {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Filled.Search, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Text("সার্ভিস ও অর্ডার ট্র্যাকিং", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 400.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "আপনার ট্র্যাকিং কোড (যেমন: RC-123456 বা ORD-12345) অথবা মোবাইল নম্বর লিখুন:",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("কোড বা মোবাইল নম্বর", fontSize = 12.sp) },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f).testTag("tracking_input_field")
                    )
                    Button(
                        onClick = { viewModel.trackItem(searchQuery) },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("search_track_btn")
                    ) {
                        Text("খুঁজুন")
                    }
                }

                if (searched) {
                    if (trackedApp == null && trackedOrder == null) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "দুঃখিত! এই কোড বা মোবাইল নম্বরে কোনো সক্রিয় আবেদন বা অর্ডার খুঁজে পাওয়া যায়নি।",
                                color = MaterialTheme.colorScheme.error,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }

                    trackedApp?.let { app ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("অনলাইন আবেদন", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                                    StatusBadge(status = app.status)
                                }
                                Text("সেবা: ${app.serviceTitle}", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text("কোড: ${app.trackingCode}", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                                Text("আবেদনকারী: ${app.applicantName} (${app.mobileNumber})", fontSize = 12.sp)
                                Text("ডেলিভারি: ${app.deliveryOption}", fontSize = 11.sp)
                                Text("মোট ফি: ৳${app.govtFee + app.storeCharge}", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    trackedOrder?.let { ord ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("প্রোডাক্ট অর্ডার", fontSize = 11.sp, color = MaterialTheme.colorScheme.tertiary, fontWeight = FontWeight.Bold)
                                    StatusBadge(status = ord.status)
                                }
                                Text("অর্ডার কোড: ${ord.orderCode}", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text("গ্রাহক: ${ord.customerName} (${ord.mobileNumber})", fontSize = 12.sp)
                                Text("আইটেমস:\n${ord.itemsSummary}", fontSize = 11.sp)
                                Text("সর্বমোট: ৳${ord.totalAmount}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }
            }
        }
    )
}
