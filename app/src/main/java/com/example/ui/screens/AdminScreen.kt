package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entities.*
import com.example.ui.MainViewModel
import com.example.ui.components.StatusBadge

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminScreen(
    viewModel: MainViewModel,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isAuthenticated by viewModel.isAdminAuthenticated.collectAsState()

    if (!isAuthenticated) {
        AdminLoginView(
            onVerifyPin = { pin -> viewModel.verifyAdminPin(pin) },
            onCancel = onClose
        )
    } else {
        AdminDashboardView(
            viewModel = viewModel,
            onLogout = { viewModel.logoutAdmin() },
            onClose = onClose,
            modifier = modifier
        )
    }
}

@Composable
private fun AdminLoginView(
    onVerifyPin: (String) -> Boolean,
    onCancel: () -> Unit
) {
    var pin by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onCancel,
        confirmButton = {
            Button(
                onClick = {
                    if (onVerifyPin(pin)) {
                        error = ""
                    } else {
                        error = "ভুল পিন নম্বর! (ডিফল্ট পিন: 7116 বা 1234)"
                    }
                },
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("admin_login_btn")
            ) {
                Text("লগইন করুন", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onCancel) {
                Text("বাতিল")
            }
        },
        icon = {
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Filled.AdminPanelSettings,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(32.dp)
                )
            }
        },
        title = {
            Text("অ্যাডমিন প্যানেল প্রবেশ", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "দোকানের মালিক ও ম্যানেজারের জন্য সিকিউরিটি পিন দিন:",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedTextField(
                    value = pin,
                    onValueChange = { if (it.length <= 6) pin = it },
                    placeholder = { Text("পিন (PIN) নম্বর লিখুন") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth().testTag("admin_pin_input")
                )

                if (error.isNotBlank()) {
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Text(
                    text = "সহায়তা: ডিফল্ট অ্যাডমিন পিন '7116' অথবা '1234'",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AdminDashboardView(
    viewModel: MainViewModel,
    onLogout: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val applications by viewModel.allApplications.collectAsState()
    val orders by viewModel.allOrders.collectAsState()
    val notices by viewModel.notices.collectAsState()
    val products by viewModel.products.collectAsState()
    val services by viewModel.services.collectAsState()
    val context = LocalContext.current

    var selectedAdminTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("গ্রাহক আবেদন", "অর্ডার তালিকা", "নোটিশ", "পণ্য তালিকা")

    // Dialog state for adding/editing notice
    var showNoticeDialog by remember { mutableStateOf(false) }
    var editingNotice by remember { mutableStateOf<NoticeItem?>(null) }

    // Dialog state for adding/editing product
    var showProductDialog by remember { mutableStateOf(false) }
    var editingProduct by remember { mutableStateOf<ProductItem?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("দোকান অ্যাডমিন প্যানেল", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text("রিফাত কম্পিউটার ও ভ্যারাইটি স্টোর", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                    }
                },
                actions = {
                    IconButton(onClick = onLogout) {
                        Icon(Icons.Filled.Lock, contentDescription = "লগআউট", tint = MaterialTheme.colorScheme.error)
                    }
                    IconButton(onClick = onClose) {
                        Icon(Icons.Filled.Close, contentDescription = "বন্ধ করুন")
                    }
                }
            )
        },
        floatingActionButton = {
            if (selectedAdminTab == 2) {
                FloatingActionButton(
                    onClick = {
                        editingNotice = null
                        showNoticeDialog = true
                    },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = Color.White,
                    modifier = Modifier.testTag("admin_add_notice_fab")
                ) {
                    Icon(Icons.Filled.Add, contentDescription = "নতুন নোটিশ")
                }
            } else if (selectedAdminTab == 3) {
                FloatingActionButton(
                    onClick = {
                        editingProduct = null
                        showProductDialog = true
                    },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = Color.White,
                    modifier = Modifier.testTag("admin_add_product_fab")
                ) {
                    Icon(Icons.Filled.Add, contentDescription = "নতুন পণ্য")
                }
            }
        },
        modifier = modifier.fillMaxSize().testTag("admin_dashboard_view")
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Tab row
            ScrollableTabRow(
                selectedTabIndex = selectedAdminTab,
                edgePadding = 16.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedAdminTab == index,
                        onClick = { selectedAdminTab = index },
                        text = {
                            Text(
                                text = when (index) {
                                    0 -> "$title (${applications.size})"
                                    1 -> "$title (${orders.size})"
                                    2 -> "$title (${notices.size})"
                                    else -> "$title (${products.size})"
                                },
                                fontSize = 12.sp,
                                fontWeight = if (selectedAdminTab == index) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    )
                }
            }

            // Tab content
            when (selectedAdminTab) {
                // Tab 0: Customer Applications
                0 -> {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        if (applications.isEmpty()) {
                            item {
                                Box(
                                    modifier = Modifier.fillMaxWidth().padding(40.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("এখনও কোনো অনলাইন আবেদন জমা পড়েনি", color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }

                        items(applications) { app ->
                            AdminApplicationCard(
                                application = app,
                                onStatusChange = { newStatus -> viewModel.updateApplicationStatus(app, newStatus) },
                                onDelete = { viewModel.deleteServiceApplication(app) },
                                onWhatsApp = {
                                    viewModel.openWhatsApp(context, "আসসালামু আলাইকুম ${app.applicantName}, রিফাত কম্পিউটার স্টোর থেকে আপনার আবেদন (ট্র্যাকিং: ${app.trackingCode}, সেবা: ${app.serviceTitle}) সংক্রান্ত বিষয়ে যোগাযোগ করছি। বর্তমান অবস্থা: ${app.status}")
                                }
                            )
                        }
                    }
                }

                // Tab 1: Product Orders
                1 -> {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        if (orders.isEmpty()) {
                            item {
                                Box(
                                    modifier = Modifier.fillMaxWidth().padding(40.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("এখনও কোনো প্রোডাক্ট অর্ডার আসেনি", color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }

                        items(orders) { order ->
                            AdminOrderCard(
                                order = order,
                                onStatusChange = { newStatus -> viewModel.updateOrderStatus(order, newStatus) },
                                onDelete = { viewModel.deleteProductOrder(order) },
                                onWhatsApp = {
                                    viewModel.openWhatsApp(context, "আসসালামু আলাইকুম ${order.customerName}, রিফাত কম্পিউটার স্টোর থেকে আপনার অর্ডার (কোড: ${order.orderCode}, মূল্য: ৳${order.totalAmount}) সংক্রান্ত বিষয়ে যোগাযোগ করছি। স্ট্যাটাস: ${order.status}")
                                }
                            )
                        }
                    }
                }

                // Tab 2: Notices Management
                2 -> {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(notices) { notice ->
                            AdminNoticeCard(
                                notice = notice,
                                onEdit = {
                                    editingNotice = notice
                                    showNoticeDialog = true
                                },
                                onDelete = { viewModel.deleteNotice(notice) }
                            )
                        }
                    }
                }

                // Tab 3: Products Management
                3 -> {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(products) { product ->
                            AdminProductCard(
                                product = product,
                                onEdit = {
                                    editingProduct = product
                                    showProductDialog = true
                                },
                                onDelete = { viewModel.deleteProduct(product) }
                            )
                        }
                    }
                }
            }
        }

        // Add/Edit Notice Dialog
        if (showNoticeDialog) {
            NoticeEditDialog(
                initialNotice = editingNotice,
                onDismiss = { showNoticeDialog = false },
                onSave = { notice ->
                    if (editingNotice != null) {
                        viewModel.updateNotice(notice)
                    } else {
                        viewModel.addNotice(notice)
                    }
                    showNoticeDialog = false
                }
            )
        }

        // Add/Edit Product Dialog
        if (showProductDialog) {
            ProductEditDialog(
                initialProduct = editingProduct,
                onDismiss = { showProductDialog = false },
                onSave = { prod ->
                    if (editingProduct != null) {
                        viewModel.updateProduct(prod)
                    } else {
                        viewModel.addProduct(prod)
                    }
                    showProductDialog = false
                }
            )
        }
    }
}

@Composable
private fun AdminApplicationCard(
    application: ServiceApplication,
    onStatusChange: (String) -> Unit,
    onDelete: () -> Unit,
    onWhatsApp: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "কোড: ${application.trackingCode}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.primary
                )
                StatusBadge(status = application.status)
            }

            Text("সেবা: ${application.serviceTitle}", fontWeight = FontWeight.Bold, fontSize = 13.sp)
            Text("আবেদনকারী: ${application.applicantName} | মোবাইল: ${application.mobileNumber}", fontSize = 12.sp)

            if (application.details.isNotBlank()) {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "বিবরণ: ${application.details}",
                        fontSize = 11.sp,
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }

            Text("ডেলিভারি পদ্ধতি: ${application.deliveryOption}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("মোট ফি: ৳${application.govtFee + application.storeCharge}", fontSize = 12.sp, fontWeight = FontWeight.Bold)

            // Status Update dropdown / buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("স্ট্যাটাস:", fontSize = 11.sp, fontWeight = FontWeight.Bold)

                val statuses = listOf("পেন্ডিং", "প্রসেসিং", "সম্পন্ন", "ডেলিভার্ড")
                statuses.forEach { s ->
                    val isSelected = application.status.contains(s)
                    FilterChip(
                        selected = isSelected,
                        onClick = { onStatusChange(when(s) {
                            "পেন্ডিং" -> "পেন্ডিং (অপেক্ষমান)"
                            "প্রসেসিং" -> "প্রসেসিং (কাজ চলছে)"
                            "সম্পন্ন" -> "সম্পন্ন (ডেলিভারি প্রস্তুত)"
                            else -> "ডেলিভার্ড"
                        }) },
                        label = { Text(s, fontSize = 10.sp) }
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = onDelete) {
                    Icon(Icons.Outlined.Delete, contentDescription = "মুছুন", tint = MaterialTheme.colorScheme.error)
                }

                Button(
                    onClick = onWhatsApp,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Filled.Chat, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("গ্রাহককে WhatsApp বার্তা", fontSize = 11.sp, color = Color.White)
                }
            }
        }
    }
}

@Composable
private fun AdminOrderCard(
    order: ProductOrder,
    onStatusChange: (String) -> Unit,
    onDelete: () -> Unit,
    onWhatsApp: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "অর্ডার: ${order.orderCode}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.primary
                )
                StatusBadge(status = order.status)
            }

            Text("গ্রাহক: ${order.customerName} | ফোন: ${order.mobileNumber}", fontWeight = FontWeight.Bold, fontSize = 12.sp)
            Text("ডেলিভারি: ${order.deliveryType} (${order.deliveryAddress})", fontSize = 11.sp)

            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                shape = RoundedCornerShape(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = order.itemsSummary,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(8.dp)
                )
            }

            Text("সর্বমোট প্রদেয়: ৳${order.totalAmount}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("স্ট্যাটাস:", fontSize = 11.sp, fontWeight = FontWeight.Bold)

                val statuses = listOf("নতুন", "প্রক্রিয়াধীন", "প্রস্তুত", "সম্পন্ন")
                statuses.forEach { s ->
                    val isSelected = order.status.contains(s)
                    FilterChip(
                        selected = isSelected,
                        onClick = { onStatusChange(when(s) {
                            "নতুন" -> "নতুন অর্ডার"
                            "প্রক্রিয়াধীন" -> "প্রক্রিয়াধীন"
                            "প্রস্তুত" -> "ডেলিভারি প্রস্তুত"
                            else -> "সম্পন্ন"
                        }) },
                        label = { Text(s, fontSize = 10.sp) }
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = onDelete) {
                    Icon(Icons.Outlined.Delete, contentDescription = "মুছুন", tint = MaterialTheme.colorScheme.error)
                }

                Button(
                    onClick = onWhatsApp,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Filled.Chat, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("গ্রাহককে WhatsApp দিন", fontSize = 11.sp, color = Color.White)
                }
            }
        }
    }
}

@Composable
private fun AdminNoticeCard(
    notice: NoticeItem,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(notice.category, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Text(notice.date, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text(notice.title, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            Text(notice.description, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onEdit) {
                    Icon(Icons.Outlined.Edit, contentDescription = "সম্পাদনা", tint = MaterialTheme.colorScheme.primary)
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Outlined.Delete, contentDescription = "মুছুন", tint = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

@Composable
private fun AdminProductCard(
    product: ProductItem,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(product.name, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Text("ক্যাটাগরি: ${product.category} | স্টক: ${product.stockStatus}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("মূল্য: ৳${product.price} (আসল: ৳${product.originalPrice})", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            }

            IconButton(onClick = onEdit) {
                Icon(Icons.Outlined.Edit, contentDescription = "সম্পাদনা", tint = MaterialTheme.colorScheme.primary)
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Outlined.Delete, contentDescription = "মুছুন", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Composable
private fun NoticeEditDialog(
    initialNotice: NoticeItem?,
    onDismiss: () -> Unit,
    onSave: (NoticeItem) -> Unit
) {
    var title by remember { mutableStateOf(initialNotice?.title ?: "") }
    var category by remember { mutableStateOf(initialNotice?.category ?: "চাকরির খবর") }
    var date by remember { mutableStateOf(initialNotice?.date ?: "২২ আগস্ট ২০২৬") }
    var deadline by remember { mutableStateOf(initialNotice?.deadline ?: "") }
    var desc by remember { mutableStateOf(initialNotice?.description ?: "") }
    var link by remember { mutableStateOf(initialNotice?.link ?: "") }
    var isUrgent by remember { mutableStateOf(initialNotice?.isUrgent ?: false) }

    val categories = listOf("চাকরির খবর", "পরীক্ষার রেজাল্ট", "প্রবেশপত্র", "বিশেষ ছাড়", "জরুরি নোটিশ")

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        val notice = NoticeItem(
                            id = initialNotice?.id ?: 0,
                            title = title,
                            category = category,
                            date = date,
                            deadline = deadline,
                            description = desc,
                            link = link,
                            isUrgent = isUrgent
                        )
                        onSave(notice)
                    }
                }
            ) {
                Text("সংরক্ষণ করুন")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("বাতিল") }
        },
        title = {
            Text(if (initialNotice == null) "নতুন নোটিশ প্রকাশ করুন" else "নোটিশ সম্পাদনা", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("নোটিশের শিরোনাম *", fontSize = 11.sp) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Category selection
                Text("ক্যাটাগরি:", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    categories.take(3).forEach { c ->
                        FilterChip(
                            selected = category == c,
                            onClick = { category = c },
                            label = { Text(c, fontSize = 9.sp) }
                        )
                    }
                }

                OutlinedTextField(
                    value = deadline,
                    onValueChange = { deadline = it },
                    label = { Text("আবেদনের শেষ তারিখ / সময়সীমা", fontSize = 11.sp) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = desc,
                    onValueChange = { desc = it },
                    label = { Text("বিস্তারিত বিবরণ", fontSize = 11.sp) },
                    minLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = link,
                    onValueChange = { link = it },
                    label = { Text("অফিশিয়াল ওয়েবসাইট লিঙ্ক (ঐচ্ছিক)", fontSize = 11.sp) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = isUrgent, onCheckedChange = { isUrgent = it })
                    Text("জরুরি নোটিশ হিসেবে হাইলাইট করুন", fontSize = 11.sp)
                }
            }
        }
    )
}

@Composable
private fun ProductEditDialog(
    initialProduct: ProductItem?,
    onDismiss: () -> Unit,
    onSave: (ProductItem) -> Unit
) {
    var name by remember { mutableStateOf(initialProduct?.name ?: "") }
    var category by remember { mutableStateOf(initialProduct?.category ?: "কম্পিউটার এক্সেসরিজ") }
    var priceStr by remember { mutableStateOf(initialProduct?.price?.toString() ?: "") }
    var originalPriceStr by remember { mutableStateOf(initialProduct?.originalPrice?.toString() ?: "") }
    var stockStatus by remember { mutableStateOf(initialProduct?.stockStatus ?: "ইন স্টক") }
    var desc by remember { mutableStateOf(initialProduct?.description ?: "") }

    val categories = listOf("কম্পিউটার এক্সেসরিজ", "স্টেশনারি ও খাতা", "ভ্যারাইটি ও গিফট")
    val stocks = listOf("ইন স্টক", "স্টক সীমিত", "স্টক শেষ")

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        val product = ProductItem(
                            id = initialProduct?.id ?: 0,
                            name = name,
                            category = category,
                            price = priceStr.toIntOrNull() ?: 0,
                            originalPrice = originalPriceStr.toIntOrNull() ?: (priceStr.toIntOrNull() ?: 0),
                            stockStatus = stockStatus,
                            description = desc,
                            iconType = when {
                                name.contains("Mouse", ignoreCase = true) || name.contains("মাউস") -> "mouse"
                                name.contains("Keyboard", ignoreCase = true) || name.contains("কিবোর্ড") -> "keyboard"
                                name.contains("Pen Drive", ignoreCase = true) || name.contains("পেনড্রাইভ") -> "pendrive"
                                name.contains("Calculator", ignoreCase = true) || name.contains("ক্যালকুলেটর") -> "calculator"
                                name.contains("Paper", ignoreCase = true) || name.contains("খাতা") || name.contains("পাতা") -> "paper"
                                name.contains("Pen", ignoreCase = true) || name.contains("কলম") -> "pen"
                                else -> "general"
                            }
                        )
                        onSave(product)
                    }
                }
            ) {
                Text("সংরক্ষণ করুন")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("বাতিল") }
        },
        title = {
            Text(if (initialProduct == null) "নতুন পণ্য যোগ করুন" else "পণ্য সম্পাদনা", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("পণ্যের নাম *", fontSize = 11.sp) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Text("ক্যাটাগরি:", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    categories.forEach { c ->
                        FilterChip(
                            selected = category == c,
                            onClick = { category = c },
                            label = { Text(c, fontSize = 9.sp) }
                        )
                    }
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = priceStr,
                        onValueChange = { priceStr = it },
                        label = { Text("বিক্রয় মূল্য (৳) *", fontSize = 11.sp) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = originalPriceStr,
                        onValueChange = { originalPriceStr = it },
                        label = { Text("আসল মূল্য (৳)", fontSize = 11.sp) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                Text("স্টক অবস্থা:", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    stocks.forEach { s ->
                        FilterChip(
                            selected = stockStatus == s,
                            onClick = { stockStatus = s },
                            label = { Text(s, fontSize = 9.sp) }
                        )
                    }
                }

                OutlinedTextField(
                    value = desc,
                    onValueChange = { desc = it },
                    label = { Text("বিবরণ / স্পেসিফিকেশন", fontSize = 11.sp) },
                    minLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    )
}
