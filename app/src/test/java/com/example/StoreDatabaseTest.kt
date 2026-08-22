package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AppDatabase
import com.example.data.local.dao.StoreDao
import com.example.data.local.entities.NoticeItem
import com.example.data.local.entities.ProductItem
import com.example.data.local.entities.ServiceItem
import com.example.data.repository.StoreRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.io.IOException

@RunWith(RobolectricTestRunner::class)
class StoreDatabaseTest {
    private lateinit var storeDao: StoreDao
    private lateinit var db: AppDatabase
    private lateinit var repository: StoreRepository

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        storeDao = db.storeDao()
        repository = StoreRepository(storeDao)
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }

    @Test
    fun insertAndRetrieveService() = runBlocking {
        val service = ServiceItem(
            title = "এসএসসি ও এইচএসসি পরীক্ষার রেজাল্ট",
            category = "শিক্ষা ও ফলাফল",
            description = "অনলাইনে দ্রুত রেজাল্ট চেক ও মার্কশিট প্রিন্ট।",
            requiredDocs = "রোল নম্বর, রেজিস্ট্রেশন নম্বর, বোর্ড ও পাশের বছর।",
            govtFee = 0,
            storeCharge = 20,
            estimatedTime = "৫-১০ মিনিট",
            isPopular = true
        )
        storeDao.insertService(service)

        val allServices = storeDao.getAllServices().first()
        assertEquals(1, allServices.size)
        assertEquals("এসএসসি ও এইচএসসি পরীক্ষার রেজাল্ট", allServices[0].title)
        assertEquals(20, allServices[0].storeCharge)
    }

    @Test
    fun insertAndRetrieveProduct() = runBlocking {
        val product = ProductItem(
            name = "SanDisk 64GB USB 3.0 Pen Drive",
            category = "কম্পিউটার এক্সেসরিজ",
            price = 680,
            originalPrice = 750,
            stockStatus = "ইন স্টক",
            description = "High Speed 3.0 Flash Drive with 5 Years Warranty",
            iconType = "pendrive",
            isFeatured = true
        )
        storeDao.insertProduct(product)

        val allProducts = storeDao.getAllProducts().first()
        assertEquals(1, allProducts.size)
        assertEquals("SanDisk 64GB USB 3.0 Pen Drive", allProducts[0].name)
        assertEquals(680, allProducts[0].price)
    }

    @Test
    fun submitAndTrackServiceApplication() = runBlocking {
        val application = repository.submitServiceApplication(
            serviceTitle = "এনআইডি (NID) সংশোধন ও রি-ইস্যু",
            applicantName = "মো: রাফি ইসলাম",
            mobileNumber = "01700112233",
            details = "পিতার নাম সংশোধন করতে চাই",
            deliveryOption = "WhatsApp এ PDF কপি সংগ্রহ",
            govtFee = 230,
            storeCharge = 150
        )

        assertTrue(application.trackingCode.startsWith("RC-"))
        assertEquals("মো: রাফি ইসলাম", application.applicantName)

        val tracked = repository.trackServiceApplication(application.trackingCode)
        assertNotNull(tracked)
        assertEquals("01700112233", tracked?.mobileNumber)
        assertEquals("পেন্ডিং (অপেক্ষমান)", tracked?.status)
    }

    @Test
    fun submitAndTrackProductOrder() = runBlocking {
        val order = repository.submitProductOrder(
            customerName = "তানভীর আহমেদ",
            mobileNumber = "01811223344",
            deliveryAddress = "বাজার মোড়, রিফাত স্টোর",
            deliveryType = "দোকান থেকে সরাসরি পিকআপ",
            itemsSummary = "• SanDisk 64GB (x1)",
            subtotal = 680,
            discount = 68,
            totalAmount = 612
        )

        assertTrue(order.orderCode.startsWith("ORD-"))
        assertEquals(612, order.totalAmount)

        val tracked = repository.trackProductOrder(order.orderCode)
        assertNotNull(tracked)
        assertEquals("তানভীর আহমেদ", tracked?.customerName)
    }

    @Test
    fun noticeManagement() = runBlocking {
        val notice = NoticeItem(
            title = "৪৬তম বিসিএস (BCS) সার্কুলার ও আবেদন",
            category = "চাকরির খবর",
            date = "২২ আগস্ট ২০২৬",
            deadline = "৩১ আগস্ট ২০২৬",
            description = "সকল ক্যাডারের জন্য অনলাইন আবেদন চলছে।",
            link = "http://bpsc.teletalk.com.bd",
            isUrgent = true
        )
        val id = storeDao.insertNotice(notice)
        assertTrue(id > 0)

        val notices = storeDao.getAllNotices().first()
        assertEquals(1, notices.size)
        assertTrue(notices[0].isUrgent)
    }
}
