package com.example.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "services")
data class ServiceItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val category: String,
    val description: String,
    val requiredDocs: String,
    val govtFee: Int,
    val storeCharge: Int,
    val estimatedTime: String,
    val officialUrl: String = "",
    val isPopular: Boolean = false
)

@Entity(tableName = "products")
data class ProductItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val category: String,
    val price: Int,
    val originalPrice: Int = 0,
    val stockStatus: String = "ইন স্টক", // "ইন স্টক", "স্টক সীমিত", "স্টক শেষ"
    val unit: String = "পিস",
    val description: String = "",
    val iconType: String = "general",
    val isFeatured: Boolean = false
)

@Entity(tableName = "notices")
data class NoticeItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val category: String, // চাকরির খবর, পরীক্ষার রেজাল্ট, প্রবেশপত্র, বিশেষ ছাড়, জরুরি নোটিশ
    val date: String,
    val deadline: String = "",
    val description: String,
    val link: String = "",
    val isUrgent: Boolean = false
)

@Entity(tableName = "service_applications")
data class ServiceApplication(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val trackingCode: String,
    val serviceTitle: String,
    val applicantName: String,
    val mobileNumber: String,
    val details: String,
    val deliveryOption: String,
    val status: String = "পেন্ডিং (অপেক্ষমান)", // "পেন্ডিং (অপেক্ষমান)", "প্রসেসিং (কাজ চলছে)", "সম্পন্ন (ডেলিভারি প্রস্তুত)", "ডেলিভার্ড"
    val govtFee: Int = 0,
    val storeCharge: Int = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val adminNotes: String = ""
)

@Entity(tableName = "product_orders")
data class ProductOrder(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val orderCode: String,
    val customerName: String,
    val mobileNumber: String,
    val deliveryAddress: String,
    val deliveryType: String,
    val itemsSummary: String,
    val subtotal: Int,
    val discount: Int = 0,
    val totalAmount: Int,
    val status: String = "নতুন অর্ডার", // "নতুন অর্ডার", "প্রক্রিয়াধীন", "ডেলিভারি প্রস্তুত", "সম্পন্ন"
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "chat_messages")
data class ChatMessage(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val sender: String, // "user", "assistant"
    val message: String,
    val timestamp: Long = System.currentTimeMillis()
)
