package com.example.data.local.dao

import androidx.room.*
import com.example.data.local.entities.*
import kotlinx.coroutines.flow.Flow

@Dao
interface StoreDao {
    // Services
    @Query("SELECT * FROM services ORDER BY isPopular DESC, id ASC")
    fun getAllServices(): Flow<List<ServiceItem>>

    @Query("SELECT * FROM services WHERE category = :category ORDER BY id ASC")
    fun getServicesByCategory(category: String): Flow<List<ServiceItem>>

    @Query("SELECT * FROM services WHERE id = :id")
    suspend fun getServiceById(id: Int): ServiceItem?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertServices(services: List<ServiceItem>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertService(service: ServiceItem): Long

    @Update
    suspend fun updateService(service: ServiceItem)

    @Delete
    suspend fun deleteService(service: ServiceItem)

    @Query("SELECT COUNT(*) FROM services")
    suspend fun getServiceCount(): Int

    // Products
    @Query("SELECT * FROM products ORDER BY isFeatured DESC, id ASC")
    fun getAllProducts(): Flow<List<ProductItem>>

    @Query("SELECT * FROM products WHERE category = :category ORDER BY id ASC")
    fun getProductsByCategory(category: String): Flow<List<ProductItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProducts(products: List<ProductItem>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: ProductItem): Long

    @Update
    suspend fun updateProduct(product: ProductItem)

    @Delete
    suspend fun deleteProduct(product: ProductItem)

    @Query("SELECT COUNT(*) FROM products")
    suspend fun getProductCount(): Int

    // Notices
    @Query("SELECT * FROM notices ORDER BY isUrgent DESC, id DESC")
    fun getAllNotices(): Flow<List<NoticeItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotices(notices: List<NoticeItem>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotice(notice: NoticeItem): Long

    @Update
    suspend fun updateNotice(notice: NoticeItem)

    @Delete
    suspend fun deleteNotice(notice: NoticeItem)

    @Query("SELECT COUNT(*) FROM notices")
    suspend fun getNoticeCount(): Int

    // Service Applications
    @Query("SELECT * FROM service_applications ORDER BY id DESC")
    fun getAllServiceApplications(): Flow<List<ServiceApplication>>

    @Query("SELECT * FROM service_applications WHERE trackingCode = :code OR mobileNumber = :code")
    suspend fun findServiceApplication(code: String): ServiceApplication?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertServiceApplication(application: ServiceApplication): Long

    @Update
    suspend fun updateServiceApplication(application: ServiceApplication)

    @Delete
    suspend fun deleteServiceApplication(application: ServiceApplication)

    // Product Orders
    @Query("SELECT * FROM product_orders ORDER BY id DESC")
    fun getAllProductOrders(): Flow<List<ProductOrder>>

    @Query("SELECT * FROM product_orders WHERE orderCode = :code OR mobileNumber = :code")
    suspend fun findProductOrder(code: String): ProductOrder?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProductOrder(order: ProductOrder): Long

    @Update
    suspend fun updateProductOrder(order: ProductOrder)

    @Delete
    suspend fun deleteProductOrder(order: ProductOrder)

    // Chat History
    @Query("SELECT * FROM chat_messages ORDER BY timestamp ASC")
    fun getAllChatMessages(): Flow<List<ChatMessage>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChatMessage(message: ChatMessage): Long

    @Query("DELETE FROM chat_messages")
    suspend fun clearChat()
}
