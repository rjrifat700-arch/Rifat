package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.local.dao.StoreDao
import com.example.data.local.entities.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        ServiceItem::class,
        ProductItem::class,
        NoticeItem::class,
        ServiceApplication::class,
        ProductOrder::class,
        ChatMessage::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun storeDao(): StoreDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "rifat_store_database"
                )
                .addCallback(object : Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        CoroutineScope(Dispatchers.IO).launch {
                            val dao = getInstance(context).storeDao()
                            dao.insertServices(SeedData.defaultServices)
                            dao.insertProducts(SeedData.defaultProducts)
                            dao.insertNotices(SeedData.defaultNotices)
                            dao.insertChatMessage(
                                ChatMessage(
                                    sender = "assistant",
                                    message = "আসসালামু আলাইকুম! 'রিফাত কম্পিউটার ও ভ্যারাইটি স্টোর'-এর ডিজিটাল এসিস্ট্যান্টে আপনাকে স্বাগতম। আপনি যেকোনো অনলাইন আবেদন, পরীক্ষার ফলাফল, স্টোর প্রডাক্ট বা সরকারি ফি সম্পর্কে যেকোনো প্রশ্ন করতে পারেন।"
                                )
                            )
                        }
                    }
                })
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

object SeedData {
    val defaultServices = listOf(
        ServiceItem(
            title = "এসএসসি ও এইচএসসি রেজাল্ট ও মার্কশিট",
            category = "শিক্ষা ও ফলাফল",
            description = "সকল শিক্ষা বোর্ডের অফিশিয়াল সার্ভার থেকে গ্রেড পয়েন্ট (GPA) ও বিষয়ভিত্তিক মূল মার্কশিট দ্রুত প্রিন্ট ও ডাউনলোড সুবিধা।",
            requiredDocs = "১. পরীক্ষার রোল নম্বর (Roll No)\n২. রেজিস্ট্রেশন নম্বর (Registration No)\n৩. পরীক্ষার সন ও বোর্ড নাম",
            govtFee = 0,
            storeCharge = 20,
            estimatedTime = "১০ মিনিট",
            officialUrl = "http://www.educationboardresults.gov.bd",
            isPopular = true
        ),
        ServiceItem(
            title = "জাতীয় পরিচয়পত্র (NID) সংশোধন ও রি-ইস্যু",
            category = "নাগরিক ও সরকারি সেবা",
            description = "ভোটার আইডি কার্ডের নাম, জন্মতারিখ, পিতা/মাতার নাম ভুল সংশোধন, ঠিকানা পরিবর্তন ও নতুন অনলাইন কপি ডাউনলোড।",
            requiredDocs = "১. বর্তমান NID কার্ডের ফটোকপি/নম্বর\n২. অনলাইন ডিজিটাল জন্ম সনদ\n৩. শিক্ষাগত সনদপত্র (SSC/JSC)\n৪. পিতা-মাতার NID কার্ডের কপি",
            govtFee = 230,
            storeCharge = 150,
            estimatedTime = "১-২ কার্যদিবস",
            officialUrl = "https://services.nidw.gov.bd",
            isPopular = true
        ),
        ServiceItem(
            title = "ই-পর্চা, জমির খতিয়ান ও নামজারি আবেদন",
            category = "নাগরিক ও সরকারি সেবা",
            description = "ভূমি রেকর্ড ও জরিপ অধিদপ্তরের অনলাইন পর্চা উত্তোলন, আরএস/সিএস/এসএ/বিএস খতিয়ান যাচাই ও ই-নামজারি আবেদন।",
            requiredDocs = "১. মৌজা নাম ও জেএল নম্বর\n২. খতিয়ান নং ও দাগ নম্বর\n৩. আবেদনকারীর NID ও মোবাইল নম্বর\n৪. দলিলের কপি (প্রয়োজনে)",
            govtFee = 100,
            storeCharge = 120,
            estimatedTime = "২-৩ কার্যদিবস",
            officialUrl = "https://eporcha.gov.bd",
            isPopular = true
        ),
        ServiceItem(
            title = "বিআরটিএ ড্রাইভিং লাইসেন্স লার্নার ও স্মার্ট কার্ড",
            category = "নাগরিক ও সরকারি সেবা",
            description = "BRTA অনলাইন সার্ভিস পোর্টাল থেকে লার্নার ড্রাইভিং লাইসেন্স আবেদন, পরীক্ষা ফি জমা এবং বায়োমেট্রিক স্লট বুকিং।",
            requiredDocs = "১. এনআইডি কার্ডের কপি\n২. রেজিস্টার্ড চিকিৎসকের মেডিকেল সার্টিফিকেট\n৩. শিক্ষাগত যোগ্যতার সনদ (কমপক্ষে ৮ম/SSC)\n৪. পাসপোর্ট সাইজ ডিজিটাল ছবি",
            govtFee = 518,
            storeCharge = 200,
            estimatedTime = "১ কার্যদিবস",
            officialUrl = "https://bsp.brta.gov.bd",
            isPopular = true
        ),
        ServiceItem(
            title = "সরকারি চাকরি ও বিসিএস অনলাইন আবেদন",
            category = "চাকরির আবেদন",
            description = "টেলিটক ও অন্যান্য সরকারি মন্ত্রণালয়ের নির্ভুল অনলাইন আবেদন ফর্ম পূরণ, ছবি-স্বাক্ষর রিসাইজ ও এসএমএস পেমেন্ট।",
            requiredDocs = "১. শিক্ষাগত যোগ্যতার তথ্য (GPA, বোর্ড, সাল)\n২. পাসপোর্ট সাইজ ছবি (300x300)\n৩. স্ক্যান করা স্বাক্ষর (300x80)\n৪. এনআইডি ও স্থায়ী ঠিকানা",
            govtFee = 112,
            storeCharge = 80,
            estimatedTime = "২০ মিনিট",
            officialUrl = "http://alljobs.teletalk.com.bd",
            isPopular = true
        ),
        ServiceItem(
            title = "ডিজিটাল জন্ম ও মৃত্যু নিবন্ধন আবেদন",
            category = "নাগরিক ও সরকারি সেবা",
            description = "ইউনিয়ন পরিষদ ও পৌরসভার ১৭ ডিজিটের ডিজিটাল জন্ম সনদ নতুন আবেদন ও তথ্য সংশোধনের অনলাইন ফর্ম সাবমিশন।",
            requiredDocs = "১. ইপিআই টিকা কার্ড বা চিকিৎসকের প্রত্যয়ন\n২. পিতা-মাতার ডিজিটাল জন্ম সনদ ও এনআইডি\n৩. হোল্ডিং ট্যাক্স রসিদ\n৪. আবেদনকারীর ছবি",
            govtFee = 50,
            storeCharge = 100,
            estimatedTime = "১ দিন",
            officialUrl = "https://bdris.gov.bd",
            isPopular = false
        ),
        ServiceItem(
            title = "ই-পাসপোর্ট নতুন আবেদন ও রি-ইস্যু ফর্ম",
            category = "নাগরিক ও সরকারি সেবা",
            description = "অনলাইনে ই-পাসপোর্ট আবেদন ফর্ম পূরণ, শিডিউল ডেট বুকিং, ব্যাংক চালান পেমেন্ট গাইড ও প্রিন্ট কপি সরবরাহ।",
            requiredDocs = "১. জাতীয় পরিচয়পত্র (NID) বা অনলাইন জন্ম সনদ\n২. পুরাতন পাসপোর্টের ফটোকপি (রি-ইস্যুর ক্ষেত্রে)\n৩. নাগরিক সনদপত্র\n৪. পেশাগত প্রমাণপত্র",
            govtFee = 4025,
            storeCharge = 250,
            estimatedTime = "১ কার্যদিবস",
            officialUrl = "https://www.epassport.gov.bd",
            isPopular = true
        ),
        ServiceItem(
            title = "একাদশ শ্রেণি (XI Class) কলেজ ভর্তি আবেদন",
            category = "শিক্ষা ও ফলাফল",
            description = "এসএসসি উত্তীর্ণ শিক্ষার্থীদের পছন্দক্রম অনুযায়ী ৫-১০টি কলেজ চয়েজ আবেদন ও ভর্তি সিকিউরিটি ফি পেমেন্ট।",
            requiredDocs = "১. এসএসসি রোল ও রেজিস্ট্রেশন নম্বর\n২. পাসের সন ও শিক্ষা বোর্ড\n৩. সক্রিয় ব্যক্তিগত মোবাইল নম্বর",
            govtFee = 150,
            storeCharge = 70,
            estimatedTime = "১৫ মিনিট",
            officialUrl = "http://xiclassadmission.gov.bd",
            isPopular = true
        ),
        ServiceItem(
            title = "পাসপোর্ট সাইজ স্টুডিও ফটো প্রিন্ট ও এডিটিং",
            category = "ডিজিটাল ও প্রিন্টিং কাজ",
            description = "হাই গ্লসি ফটো পেপারে ল্যাব কোয়ালিটি পাসপোর্ট, স্ট্যাম্প সাইজ ছবি প্রিন্ট, ব্যাকগ্রাউন্ড রিমুভ ও কোট-টাই এডিটিং।",
            requiredDocs = "১. সরাসরি দোকানে ছবি তোলা অথবা মোবাইলের ছবি ফাইল",
            govtFee = 0,
            storeCharge = 40,
            estimatedTime = "১০ মিনিট",
            officialUrl = "",
            isPopular = true
        ),
        ServiceItem(
            title = "বাংলা ও ইংরেজি টাইপিং এবং কালার প্রিন্ট",
            category = "ডিজিটাল ও প্রিন্টিং কাজ",
            description = "অ্যাসাইনমেন্ট, নোটিশ, চুক্তিপত্র, আবেদনপত্র, জীবনবৃত্তান্ত (Professional CV) নির্ভুল টাইপিং ও স্পাইরাল বাইন্ডিং।",
            requiredDocs = "১. হাতে লেখা খসড়া বা ছবি/পিডিএফ কপি",
            govtFee = 0,
            storeCharge = 30,
            estimatedTime = "১৫-৩০ মিনিট",
            officialUrl = "",
            isPopular = false
        )
    )

    val defaultProducts = listOf(
        ProductItem(
            name = "SanDisk Ultra 64GB USB 3.0 Pen Drive",
            category = "কম্পিউটার এক্সেসরিজ",
            price = 680,
            originalPrice = 750,
            stockStatus = "ইন স্টক",
            unit = "পিস",
            description = "হাই-স্পিড ডেটা ট্রান্সফার (130MB/s পর্যন্ত), ৫ বছরের অফিসিয়াল ওয়ারেন্টি।",
            iconType = "pendrive",
            isFeatured = true
        ),
        ProductItem(
            name = "A4Tech OP-720 USB Optical Mouse",
            category = "কম্পিউটার এক্সেসরিজ",
            price = 350,
            originalPrice = 400,
            stockStatus = "ইন স্টক",
            unit = "পিস",
            description = "দীর্ঘস্থায়ী টেকসই বাটন, স্মুথ গ্লাইডিং এবং ১০০০ ডিপিআই অপটিক্যাল সেন্সর।",
            iconType = "mouse",
            isFeatured = true
        ),
        ProductItem(
            name = "Havit HV-KB327 USB Multimedia Keyboard",
            category = "কম্পিউটার এক্সেসরিজ",
            price = 480,
            originalPrice = 550,
            stockStatus = "ইন স্টক",
            unit = "পিস",
            description = "বাংলা ও ইংরেজি ফন্ট প্রিন্টেড, ওয়াটারপ্রুফ ও সফট কি-প্রেস ডিজাইন।",
            iconType = "keyboard",
            isFeatured = true
        ),
        ProductItem(
            name = "Bashundhara A4 Offset Paper 80GSM (Ream)",
            category = "স্টেশনারি ও খাতা",
            price = 420,
            originalPrice = 460,
            stockStatus = "ইন স্টক",
            unit = "রিম (৫০০ পাতা)",
            description = "উন্নত মানের সাদা অফসেট পেপার, প্রিন্টার ও ফটোকপির জন্য সেরা।",
            iconType = "paper",
            isFeatured = true
        ),
        ProductItem(
            name = "Matador Pinpoint 0.5mm Ball Pen (Box of 20)",
            category = "স্টেশনারি ও খাতা",
            price = 120,
            originalPrice = 140,
            stockStatus = "ইন স্টক",
            unit = "বক্স (২০টি)",
            description = "স্মুথ রাইটিং জেল বলপেন, পরীক্ষার জন্য শিক্ষার্থীদের সবচেয়ে বিশ্বস্ত পছন্দ।",
            iconType = "pen",
            isFeatured = true
        ),
        ProductItem(
            name = "Casio FX-991EX ClassWiz Scientific Calculator",
            category = "ভ্যারাইটি ও গিফট",
            price = 1850,
            originalPrice = 2100,
            stockStatus = "ইন স্টক",
            unit = "পিস",
            description = "৫৫২ ফাংশন বিশিষ্ট আসল ক্যাসিও সাইন্টিফিক ক্যালকুলেটর, ৩ বছরের ওয়ারেন্টি।",
            iconType = "calculator",
            isFeatured = true
        ),
        ProductItem(
            name = "SanDisk 32GB MicroSD Memory Card Class 10",
            category = "কম্পিউটার এক্সেসরিজ",
            price = 420,
            originalPrice = 480,
            stockStatus = "ইন স্টক",
            unit = "পিস",
            description = "স্মার্টফোন, ক্যামেরা ও সিসিটিভির জন্য ফুল এইচডি মেমোরি কার্ড।",
            iconType = "pendrive",
            isFeatured = false
        ),
        ProductItem(
            name = "Deli Clear Practical File & Certificate Folder",
            category = "স্টেশনারি ও খাতা",
            price = 80,
            originalPrice = 100,
            stockStatus = "ইন স্টক",
            unit = "পিস",
            description = "সার্টিফিকেট ও দরকারি কাগজপত্র নিরাপদে সাজিয়ে রাখার প্লাস্টিক ফোল্ডার।",
            iconType = "paper",
            isFeatured = false
        ),
        ProductItem(
            name = "High Speed Type-C to USB OTG Adapter",
            category = "কম্পিউটার এক্সেসরিজ",
            price = 90,
            originalPrice = 120,
            stockStatus = "ইন স্টক",
            unit = "পিস",
            description = "স্মার্টফোনে সরাসরি পেনড্রাইভ ও কীবোর্ড-মাউস কানেক্ট করার অ্যাডাপ্টার।",
            iconType = "cable",
            isFeatured = false
        ),
        ProductItem(
            name = "Fast Charge Type-C Braided Data Cable",
            category = "কম্পিউটার এক্সেসরিজ",
            price = 150,
            originalPrice = 200,
            stockStatus = "ইন স্টক",
            unit = "পিস",
            description = "দ্রুত চার্জিং ও ফাস্ট ডেটা সিঙ্ক কেবল, মজবুত কটন ব্রেইডেড কোটিং।",
            iconType = "cable",
            isFeatured = false
        )
    )

    val defaultNotices = listOf(
        NoticeItem(
            title = "৪৪তম ও ৪৫তম বিসিএস পরীক্ষার ভাইভা সময়সূচি প্রকাশ",
            category = "চাকরির খবর",
            date = "২২ আগস্ট ২০২৬",
            deadline = "১৫ সেপ্টেম্বর ২০২৬",
            description = "বাংলাদেশ সরকারি কর্ম কমিশন (BPSC) কর্তৃক মৌখিক পরীক্ষার চূড়ান্ত তারিখ ও অ্যাডমিট কার্ড নির্দেশনা প্রকাশিত হয়েছে। রিফাত স্টোর থেকে এখনই অ্যাডমিট ডাউনলোড করে নিতে পারেন।",
            link = "http://bpsc.gov.bd",
            isUrgent = true
        ),
        NoticeItem(
            title = "২০২৬ সালের এইচএসসি ও সমমান পরীক্ষার ফল প্রকাশ ও মার্কশিট সার্ভিস",
            category = "পরীক্ষার রেজাল্ট",
            date = "২০ আগস্ট ২০২৬",
            deadline = "চলমান",
            description = "অনলাইনে দ্রুততম সময়ে নির্ভুল গ্রেডশিট ও বিষয়ভিত্তিক মার্কশিট প্রিন্ট সেবা চলছে। পুনঃমূল্যায়নের (Board Challenge) আবেদনও আমাদের মাধ্যমে করা যাচ্ছে।",
            link = "http://www.educationboardresults.gov.bd",
            isUrgent = true
        ),
        NoticeItem(
            title = "প্রাথমিক সহকারী শিক্ষক নিয়োগ পরীক্ষার প্রবেশপত্র ডাউনলোড শুরু",
            category = "প্রবেশপত্র",
            date = "১৯ আগস্ট ২০২৬",
            deadline = "২৮ আগস্ট ২০২৬",
            description = "প্রাথমিক শিক্ষা অধিদপ্তর (DPE) সহকারী শিক্ষক পদের রঙিন প্রবেশপত্র ডাউনলোডের জন্য ইউজার আইডি ও পাসওয়ার্ড নিয়ে দোকানে আসুন অথবা আমাদের অনলাইনে অর্ডার করুন।",
            link = "http://dpe.teletalk.com.bd",
            isUrgent = false
        ),
        NoticeItem(
            title = "দোকানের বিশেষ ছাড়: পেনড্রাইভ ও স্টেশনারি আইটেমে ১০% ডিসকাউন্ট!",
            category = "বিশেষ ছাড়",
            date = "১৮ আগস্ট ২০২৬",
            deadline = "৩১ আগস্ট ২০২৬",
            description = "চলতি মাসজুড়ে সকল প্রকার SanDisk পেনড্রাইভ, মাউস, খাতা এবং পাসপোর্ট সাইজ ফটো প্রিন্টে বিশেষ ১০% ক্যাশ ছাড় উপভোগ করুন। প্রোমোকোড: RIFAT10",
            link = "",
            isUrgent = false
        )
    )
}
