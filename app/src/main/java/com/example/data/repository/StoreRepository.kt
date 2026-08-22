package com.example.data.repository

import com.example.data.local.dao.StoreDao
import com.example.data.local.entities.*
import com.example.data.remote.GeminiService
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.*
import kotlin.random.Random

class StoreRepository(
    private val storeDao: StoreDao,
    private val geminiService: GeminiService = GeminiService()
) {
    // Services
    val allServices: Flow<List<ServiceItem>> = storeDao.getAllServices()

    fun getServicesByCategory(category: String): Flow<List<ServiceItem>> =
        storeDao.getServicesByCategory(category)

    suspend fun insertService(service: ServiceItem): Long =
        storeDao.insertService(service)

    suspend fun updateService(service: ServiceItem) =
        storeDao.updateService(service)

    suspend fun deleteService(service: ServiceItem) =
        storeDao.deleteService(service)

    // Products
    val allProducts: Flow<List<ProductItem>> = storeDao.getAllProducts()

    fun getProductsByCategory(category: String): Flow<List<ProductItem>> =
        storeDao.getProductsByCategory(category)

    suspend fun insertProduct(product: ProductItem): Long =
        storeDao.insertProduct(product)

    suspend fun updateProduct(product: ProductItem) =
        storeDao.updateProduct(product)

    suspend fun deleteProduct(product: ProductItem) =
        storeDao.deleteProduct(product)

    // Notices
    val allNotices: Flow<List<NoticeItem>> = storeDao.getAllNotices()

    suspend fun insertNotice(notice: NoticeItem): Long =
        storeDao.insertNotice(notice)

    suspend fun updateNotice(notice: NoticeItem) =
        storeDao.updateNotice(notice)

    suspend fun deleteNotice(notice: NoticeItem) =
        storeDao.deleteNotice(notice)

    // Service Applications
    val allServiceApplications: Flow<List<ServiceApplication>> =
        storeDao.getAllServiceApplications()

    suspend fun submitServiceApplication(
        serviceTitle: String,
        applicantName: String,
        mobileNumber: String,
        details: String,
        deliveryOption: String,
        govtFee: Int,
        storeCharge: Int
    ): ServiceApplication {
        val code = "RC-" + (100000 + Random.nextInt(900000))
        val application = ServiceApplication(
            trackingCode = code,
            serviceTitle = serviceTitle,
            applicantName = applicantName,
            mobileNumber = mobileNumber,
            details = details,
            deliveryOption = deliveryOption,
            govtFee = govtFee,
            storeCharge = storeCharge,
            status = "পেন্ডিং (অপেক্ষমান)",
            timestamp = System.currentTimeMillis()
        )
        val id = storeDao.insertServiceApplication(application)
        return application.copy(id = id.toInt())
    }

    suspend fun trackServiceApplication(codeOrPhone: String): ServiceApplication? =
        storeDao.findServiceApplication(codeOrPhone.trim())

    suspend fun updateServiceApplication(application: ServiceApplication) =
        storeDao.updateServiceApplication(application)

    suspend fun deleteServiceApplication(application: ServiceApplication) =
        storeDao.deleteServiceApplication(application)

    // Product Orders
    val allProductOrders: Flow<List<ProductOrder>> =
        storeDao.getAllProductOrders()

    suspend fun submitProductOrder(
        customerName: String,
        mobileNumber: String,
        deliveryAddress: String,
        deliveryType: String,
        itemsSummary: String,
        subtotal: Int,
        discount: Int,
        totalAmount: Int
    ): ProductOrder {
        val code = "ORD-" + (10000 + Random.nextInt(90000))
        val order = ProductOrder(
            orderCode = code,
            customerName = customerName,
            mobileNumber = mobileNumber,
            deliveryAddress = deliveryAddress,
            deliveryType = deliveryType,
            itemsSummary = itemsSummary,
            subtotal = subtotal,
            discount = discount,
            totalAmount = totalAmount,
            status = "নতুন অর্ডার",
            timestamp = System.currentTimeMillis()
        )
        val id = storeDao.insertProductOrder(order)
        return order.copy(id = id.toInt())
    }

    suspend fun trackProductOrder(codeOrPhone: String): ProductOrder? =
        storeDao.findProductOrder(codeOrPhone.trim())

    suspend fun updateProductOrder(order: ProductOrder) =
        storeDao.updateProductOrder(order)

    suspend fun deleteProductOrder(order: ProductOrder) =
        storeDao.deleteProductOrder(order)

    // Chat
    val chatMessages: Flow<List<ChatMessage>> = storeDao.getAllChatMessages()

    suspend fun sendUserMessage(message: String, history: List<Pair<String, String>>): String {
        storeDao.insertChatMessage(ChatMessage(sender = "user", message = message))
        val response = geminiService.generateResponse(message, history)
        storeDao.insertChatMessage(ChatMessage(sender = "assistant", message = response))
        return response
    }

    suspend fun clearChat() {
        storeDao.clearChat()
        storeDao.insertChatMessage(
            ChatMessage(
                sender = "assistant",
                message = "আসসালামু আলাইকুম! 'রিফাত কম্পিউটার ও ভ্যারাইটি স্টোর'-এর ডিজিটাল এসিস্ট্যান্টে আপনাকে স্বাগতম। আপনি যেকোনো অনলাইন আবেদন, পরীক্ষার ফলাফল, স্টোর প্রডাক্ট বা সরকারি ফি সম্পর্কে যেকোনো প্রশ্ন করতে পারেন।"
            )
        )
    }
}
