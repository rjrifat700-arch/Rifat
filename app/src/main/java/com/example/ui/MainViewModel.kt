package com.example.ui

import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.entities.*
import com.example.data.repository.StoreRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class CartItem(
    val product: ProductItem,
    val quantity: Int
)

enum class ScreenTab(val title: String) {
    HOME("হোম"),
    SERVICES("অনলাইন সেবা"),
    PRODUCTS("প্রোডাক্টস"),
    NOTICES("নোটিশ বোর্ড"),
    ASSISTANT("এআই সহকারী")
}

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: StoreRepository

    init {
        val db = AppDatabase.getInstance(application)
        repository = StoreRepository(db.storeDao())
    }

    // Navigation Tab
    private val _currentTab = MutableStateFlow(ScreenTab.HOME)
    val currentTab: StateFlow<ScreenTab> = _currentTab.asStateFlow()

    fun setTab(tab: ScreenTab) {
        _currentTab.value = tab
    }

    // Services
    val services: StateFlow<List<ServiceItem>> = repository.allServices
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedServiceCategory = MutableStateFlow("সকল সেবা")
    val selectedServiceCategory: StateFlow<String> = _selectedServiceCategory.asStateFlow()

    private val _serviceSearchQuery = MutableStateFlow("")
    val serviceSearchQuery: StateFlow<String> = _serviceSearchQuery.asStateFlow()

    fun setServiceCategory(category: String) {
        _selectedServiceCategory.value = category
    }

    fun setServiceSearchQuery(query: String) {
        _serviceSearchQuery.value = query
    }

    // Products
    val products: StateFlow<List<ProductItem>> = repository.allProducts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedProductCategory = MutableStateFlow("সকল পণ্য")
    val selectedProductCategory: StateFlow<String> = _selectedProductCategory.asStateFlow()

    private val _productSearchQuery = MutableStateFlow("")
    val productSearchQuery: StateFlow<String> = _productSearchQuery.asStateFlow()

    fun setProductCategory(category: String) {
        _selectedProductCategory.value = category
    }

    fun setProductSearchQuery(query: String) {
        _productSearchQuery.value = query
    }

    // Notices
    val notices: StateFlow<List<NoticeItem>> = repository.allNotices
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Cart Management
    private val _cart = MutableStateFlow<Map<Int, CartItem>>(emptyMap())
    val cart: StateFlow<Map<Int, CartItem>> = _cart.asStateFlow()

    private val _appliedPromoCode = MutableStateFlow("")
    val appliedPromoCode: StateFlow<String> = _appliedPromoCode.asStateFlow()

    private val _discountPercent = MutableStateFlow(0)
    val discountPercent: StateFlow<Int> = _discountPercent.asStateFlow()

    fun addToCart(product: ProductItem) {
        val current = _cart.value.toMutableMap()
        val existing = current[product.id]
        if (existing != null) {
            current[product.id] = existing.copy(quantity = existing.quantity + 1)
        } else {
            current[product.id] = CartItem(product = product, quantity = 1)
        }
        _cart.value = current
    }

    fun removeFromCart(productId: Int) {
        val current = _cart.value.toMutableMap()
        val existing = current[productId]
        if (existing != null) {
            if (existing.quantity > 1) {
                current[productId] = existing.copy(quantity = existing.quantity - 1)
            } else {
                current.remove(productId)
            }
        }
        _cart.value = current
    }

    fun deleteFromCart(productId: Int) {
        val current = _cart.value.toMutableMap()
        current.remove(productId)
        _cart.value = current
    }

    fun clearCart() {
        _cart.value = emptyMap()
        _appliedPromoCode.value = ""
        _discountPercent.value = 0
    }

    fun applyPromoCode(code: String): Boolean {
        return if (code.trim().equals("RIFAT10", ignoreCase = true)) {
            _appliedPromoCode.value = "RIFAT10"
            _discountPercent.value = 10
            true
        } else {
            false
        }
    }

    // Service Application Submission
    private val _submittedApplication = MutableStateFlow<ServiceApplication?>(null)
    val submittedApplication: StateFlow<ServiceApplication?> = _submittedApplication.asStateFlow()

    fun submitServiceApplication(
        service: ServiceItem,
        applicantName: String,
        mobileNumber: String,
        details: String,
        deliveryOption: String,
        onSuccess: (ServiceApplication) -> Unit
    ) {
        viewModelScope.launch {
            val result = repository.submitServiceApplication(
                serviceTitle = service.title,
                applicantName = applicantName,
                mobileNumber = mobileNumber,
                details = details,
                deliveryOption = deliveryOption,
                govtFee = service.govtFee,
                storeCharge = service.storeCharge
            )
            _submittedApplication.value = result
            onSuccess(result)
        }
    }

    // Product Order Submission
    private val _submittedOrder = MutableStateFlow<ProductOrder?>(null)
    val submittedOrder: StateFlow<ProductOrder?> = _submittedOrder.asStateFlow()

    fun submitProductOrder(
        customerName: String,
        mobileNumber: String,
        address: String,
        deliveryType: String,
        onSuccess: (ProductOrder) -> Unit
    ) {
        viewModelScope.launch {
            val cartItems = _cart.value.values.toList()
            val summary = cartItems.joinToString("\n") {
                "• ${it.product.name} (x${it.quantity}) - ৳${it.product.price * it.quantity}"
            }
            val subtotal = cartItems.sumOf { it.product.price * it.quantity }
            val discount = (subtotal * _discountPercent.value) / 100
            val total = subtotal - discount

            val order = repository.submitProductOrder(
                customerName = customerName,
                mobileNumber = mobileNumber,
                deliveryAddress = address,
                deliveryType = deliveryType,
                itemsSummary = summary,
                subtotal = subtotal,
                discount = discount,
                totalAmount = total
            )
            _submittedOrder.value = order
            clearCart()
            onSuccess(order)
        }
    }

    // Tracking
    private val _trackedApplication = MutableStateFlow<ServiceApplication?>(null)
    val trackedApplication: StateFlow<ServiceApplication?> = _trackedApplication.asStateFlow()

    private val _trackedOrder = MutableStateFlow<ProductOrder?>(null)
    val trackedOrder: StateFlow<ProductOrder?> = _trackedOrder.asStateFlow()

    private val _trackingSearched = MutableStateFlow(false)
    val trackingSearched: StateFlow<Boolean> = _trackingSearched.asStateFlow()

    fun trackItem(query: String) {
        viewModelScope.launch {
            _trackingSearched.value = true
            _trackedApplication.value = repository.trackServiceApplication(query)
            _trackedOrder.value = repository.trackProductOrder(query)
        }
    }

    fun clearTracking() {
        _trackedApplication.value = null
        _trackedOrder.value = null
        _trackingSearched.value = false
    }

    // Chat / AI Assistant
    val chatMessages: StateFlow<List<ChatMessage>> = repository.chatMessages
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _isAiTyping = MutableStateFlow(false)
    val isAiTyping: StateFlow<Boolean> = _isAiTyping.asStateFlow()

    fun sendMessage(text: String) {
        if (text.isBlank()) return
        val currentHistory = chatMessages.value.map { it.sender to it.message }
        viewModelScope.launch {
            _isAiTyping.value = true
            try {
                repository.sendUserMessage(text, currentHistory)
            } finally {
                _isAiTyping.value = false
            }
        }
    }

    fun clearChat() {
        viewModelScope.launch {
            repository.clearChat()
        }
    }

    // Admin Mode
    private val _isAdminAuthenticated = MutableStateFlow(false)
    val isAdminAuthenticated: StateFlow<Boolean> = _isAdminAuthenticated.asStateFlow()

    val allApplications: StateFlow<List<ServiceApplication>> = repository.allServiceApplications
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allOrders: StateFlow<List<ProductOrder>> = repository.allProductOrders
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun verifyAdminPin(pin: String): Boolean {
        return if (pin == "7116" || pin == "1234") {
            _isAdminAuthenticated.value = true
            true
        } else {
            false
        }
    }

    fun logoutAdmin() {
        _isAdminAuthenticated.value = false
    }

    // Admin CRUD Operations
    fun addNotice(notice: NoticeItem) {
        viewModelScope.launch { repository.insertNotice(notice) }
    }

    fun updateNotice(notice: NoticeItem) {
        viewModelScope.launch { repository.updateNotice(notice) }
    }

    fun deleteNotice(notice: NoticeItem) {
        viewModelScope.launch { repository.deleteNotice(notice) }
    }

    fun addProduct(product: ProductItem) {
        viewModelScope.launch { repository.insertProduct(product) }
    }

    fun updateProduct(product: ProductItem) {
        viewModelScope.launch { repository.updateProduct(product) }
    }

    fun deleteProduct(product: ProductItem) {
        viewModelScope.launch { repository.deleteProduct(product) }
    }

    fun addService(service: ServiceItem) {
        viewModelScope.launch { repository.insertService(service) }
    }

    fun updateService(service: ServiceItem) {
        viewModelScope.launch { repository.updateService(service) }
    }

    fun deleteService(service: ServiceItem) {
        viewModelScope.launch { repository.deleteService(service) }
    }

    fun updateApplicationStatus(app: ServiceApplication, newStatus: String) {
        viewModelScope.launch { repository.updateServiceApplication(app.copy(status = newStatus)) }
    }

    fun deleteServiceApplication(app: ServiceApplication) {
        viewModelScope.launch { repository.deleteServiceApplication(app) }
    }

    fun updateOrderStatus(order: ProductOrder, newStatus: String) {
        viewModelScope.launch { repository.updateProductOrder(order.copy(status = newStatus)) }
    }

    fun deleteProductOrder(order: ProductOrder) {
        viewModelScope.launch { repository.deleteProductOrder(order) }
    }

    // External Intent Helpers
    fun openWhatsApp(context: Context, customMessage: String = "") {
        val phone = "+8801706727116"
        val defaultMsg = if (customMessage.isNotBlank()) customMessage else "আসসালামু আলাইকুম, আমি রিফাত কম্পিউটার ও ভ্যারাইটি স্টোর অ্যাপ থেকে যোগাযোগ করছি।"
        try {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("https://wa.me/8801706727116?text=${Uri.encode(defaultMsg)}")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "WhatsApp ইনস্টল করা নেই। ফোন নম্বর: +8801706727116", Toast.LENGTH_LONG).show()
        }
    }

    fun callStore(context: Context) {
        try {
            val intent = Intent(Intent.ACTION_DIAL).apply {
                data = Uri.parse("tel:+8801706727116")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "কল করা সম্ভব হচ্ছে না। নম্বর: 01706727116", Toast.LENGTH_SHORT).show()
        }
    }

    fun openFacebookPage(context: Context) {
        try {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("https://www.facebook.com/rifatcomputerstore")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "ব্রাউজার ওপেন করা যাচ্ছে না", Toast.LENGTH_SHORT).show()
        }
    }

    fun openEmail(context: Context) {
        try {
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:rjrifat700@gmail.com")
                putExtra(Intent.EXTRA_SUBJECT, "রিফাত কম্পিউটার স্টোর সেবা সম্পর্কিত")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "ইমেইল: rjrifat700@gmail.com", Toast.LENGTH_LONG).show()
        }
    }

    fun openLocationMap(context: Context) {
        try {
            val gmmIntentUri = Uri.parse("geo:0,0?q=Rifat+Computer+and+Variety+Store")
            val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri).apply {
                setPackage("com.google.android.apps.maps")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            if (mapIntent.resolveActivity(context.packageManager) != null) {
                context.startActivity(mapIntent)
            } else {
                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://maps.google.com/?q=Rifat+Computer+and+Variety+Store")).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(browserIntent)
            }
        } catch (e: Exception) {
            Toast.makeText(context, "লোকেশন: রিফাত কম্পিউটার ও ভ্যারাইটি স্টোর, বাজার মোড়।", Toast.LENGTH_LONG).show()
        }
    }

    fun openWebUrl(context: Context, url: String) {
        if (url.isBlank()) return
        try {
            val webUrl = if (!url.startsWith("http://") && !url.startsWith("https://")) "https://$url" else url
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(webUrl)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "লিঙ্ক খোলা সম্ভব হচ্ছে না: $url", Toast.LENGTH_SHORT).show()
        }
    }
}
