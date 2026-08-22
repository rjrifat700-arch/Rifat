package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entities.ProductItem
import com.example.data.local.entities.ProductOrder
import com.example.data.local.entities.ServiceApplication
import com.example.data.local.entities.ServiceItem
import com.example.ui.MainViewModel
import com.example.ui.ScreenTab
import com.example.ui.components.*
import com.example.ui.screens.*
import com.example.ui.theme.RifatStoreTheme

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RifatStoreTheme {
                MainAppContent(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun MainAppContent(viewModel: MainViewModel) {
    val currentTab by viewModel.currentTab.collectAsState()
    val context = LocalContext.current

    // Dialog states
    var selectedServiceForDetails by remember { mutableStateOf<ServiceItem?>(null) }
    var selectedServiceForApply by remember { mutableStateOf<ServiceItem?>(null) }
    var applicationSuccessData by remember { mutableStateOf<ServiceApplication?>(null) }
    var orderSuccessData by remember { mutableStateOf<ProductOrder?>(null) }
    var showCartSheet by remember { mutableStateOf(false) }
    var showTrackingDialog by remember { mutableStateOf(false) }
    var showAdminScreen by remember { mutableStateOf(false) }

    if (showAdminScreen) {
        AdminScreen(
            viewModel = viewModel,
            onClose = { showAdminScreen = false }
        )
    } else {
        Scaffold(
            topBar = {
                StoreTopAppBar(
                    viewModel = viewModel,
                    onOpenAdmin = { showAdminScreen = true },
                    onOpenTracking = { showTrackingDialog = true },
                    onOpenCart = { showCartSheet = true }
                )
            },
            bottomBar = {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 8.dp,
                    modifier = Modifier.testTag("main_bottom_nav")
                ) {
                    val tabs = listOf(
                        Triple(ScreenTab.HOME, Icons.Filled.Home, Icons.Outlined.Home),
                        Triple(ScreenTab.SERVICES, Icons.Filled.Work, Icons.Outlined.WorkOutline),
                        Triple(ScreenTab.PRODUCTS, Icons.Filled.ShoppingBag, Icons.Outlined.ShoppingBag),
                        Triple(ScreenTab.NOTICES, Icons.Filled.Campaign, Icons.Outlined.Campaign),
                        Triple(ScreenTab.ASSISTANT, Icons.Filled.AutoAwesome, Icons.Outlined.AutoAwesome)
                    )

                    tabs.forEach { (tab, selectedIcon, unselectedIcon) ->
                        val isSelected = currentTab == tab
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = { viewModel.setTab(tab) },
                            icon = {
                                Icon(
                                    imageVector = if (isSelected) selectedIcon else unselectedIcon,
                                    contentDescription = tab.title,
                                    modifier = Modifier.size(22.dp)
                                )
                            },
                            label = {
                                Text(
                                    text = tab.title,
                                    fontSize = 10.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            modifier = Modifier.testTag("nav_tab_${tab.name.lowercase()}")
                        )
                    }
                }
            },
            modifier = Modifier.fillMaxSize()
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                when (currentTab) {
                    ScreenTab.HOME -> {
                        HomeScreen(
                            viewModel = viewModel,
                            onSelectService = { service -> selectedServiceForDetails = service },
                            onSelectProduct = { product -> viewModel.addToCart(product) },
                            onOpenTracking = { showTrackingDialog = true }
                        )
                    }
                    ScreenTab.SERVICES -> {
                        ServicesScreen(
                            viewModel = viewModel,
                            onSelectServiceDetails = { service -> selectedServiceForDetails = service },
                            onApplyOnline = { service -> selectedServiceForApply = service }
                        )
                    }
                    ScreenTab.PRODUCTS -> {
                        ProductsScreen(
                            viewModel = viewModel,
                            onOpenCart = { showCartSheet = true }
                        )
                    }
                    ScreenTab.NOTICES -> {
                        NoticeBoardScreen(
                            viewModel = viewModel
                        )
                    }
                    ScreenTab.ASSISTANT -> {
                        AssistantScreen(
                            viewModel = viewModel
                        )
                    }
                }
            }
        }
    }

    // Service Details Dialog
    selectedServiceForDetails?.let { service ->
        ServiceDetailsDialog(
            service = service,
            onDismiss = { selectedServiceForDetails = null },
            onApplyOnline = {
                selectedServiceForDetails = null
                selectedServiceForApply = service
            },
            onOpenOfficialUrl = { url ->
                viewModel.openWebUrl(context, url)
            }
        )
    }

    // Service Online Application Dialog
    selectedServiceForApply?.let { service ->
        ServiceApplicationDialog(
            service = service,
            onDismiss = { selectedServiceForApply = null },
            onSubmit = { name, phone, details, delivery ->
                viewModel.submitServiceApplication(
                    service = service,
                    applicantName = name,
                    mobileNumber = phone,
                    details = details,
                    deliveryOption = delivery,
                    onSuccess = { application ->
                        selectedServiceForApply = null
                        applicationSuccessData = application
                    }
                )
            }
        )
    }

    // Application Success Confirmation Dialog
    applicationSuccessData?.let { app ->
        ApplicationSuccessDialog(
            application = app,
            onDismiss = { applicationSuccessData = null },
            onOpenWhatsApp = { msg ->
                viewModel.openWhatsApp(context, msg)
                applicationSuccessData = null
            }
        )
    }

    // Shopping Cart Sheet
    if (showCartSheet) {
        CartBottomSheet(
            viewModel = viewModel,
            onDismiss = { showCartSheet = false },
            onOrderSuccess = { order ->
                showCartSheet = false
                orderSuccessData = order
            }
        )
    }

    // Order Success Dialog
    orderSuccessData?.let { order ->
        OrderSuccessDialog(
            order = order,
            onDismiss = { orderSuccessData = null },
            onOpenWhatsApp = { msg ->
                viewModel.openWhatsApp(context, msg)
                orderSuccessData = null
            }
        )
    }

    // Tracking Dialog
    if (showTrackingDialog) {
        TrackingDialog(
            viewModel = viewModel,
            onDismiss = { showTrackingDialog = false }
        )
    }
}
