package com.example.data.remote

import android.util.Log
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class GeminiService {
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val systemPrompt = """
        আপনি 'রিফাত কম্পিউটার ও ভ্যারাইটি স্টোর' (Rifat Computer & Variety Store)-এর অফিশিয়াল স্মার্ট এআই ডিজিটাল অ্যাসিস্ট্যান্ট।
        দোকানের প্রোফাইল তথ্য:
        - প্রোপাইটর/পরিচালক: মো: রিফাত হোসেন
        - যোগাযোগ নম্বর / WhatsApp: +8801706727116 (01706727116)
        - অফিসিয়াল ইমেইল: rjrifat700@gmail.com
        - সময়সূচি: প্রতিদিন সকাল ৮:০০ টা থেকে রাত ১০:০০ টা পর্যন্ত (শুক্রবার জুমার নামাজের সময় দুপুর ১২:৩০-২:০০ বিরতি)।
        - প্রধান সেবাসমূহ: এসএসসি/এইচএসসি/জেএসসি রেজাল্ট ও মার্কশিট প্রিন্ট, এনআইডি (NID) সংশোধন ও রি-ইস্যু, ই-পর্চা ও জমির খতিয়ান, বিআরটিএ ড্রাইভিং লাইসেন্স, সরকারি ও প্রাইভেট চাকরির অনলাইন আবেদন, ই-পাসপোর্ট ফর্ম পূরণ ও ফি, একাদশ শ্রেণীতে ভর্তি, ডিজিটাল জন্ম ও মৃত্যু নিবন্ধন, পাসপোর্ট সাইজ স্টুডিও ফটো প্রিন্ট, বাংলা ও ইংরেজি কম্পোজ/টাইপিং, কালার ও ব্ল্যাক প্রিন্ট, স্পাইরাল বাইন্ডিং ও লেমিনেশন।
        - বিক্রয়যোগ্য মালামাল: পেনড্রাইভ (SanDisk 32GB/64GB), অপটিক্যাল মাউস, মাল্টিমিডিয়া কিবোর্ড, বসুন্ধরা A4 পেপার রিম, মেটাডোর বলপেন, ক্যাসিও সায়েন্টিফিক ক্যালকুলেটর (FX-991EX), ফোল্ডার ও ফাইল, টাইপ-সি ডেটা কেবল ইত্যাদি।

        আপনার দায়িত্ব:
        ১. অত্যন্ত ভদ্র, নম্র, সাবলীল এবং পরিষ্কার বাংলা ভাষায় উত্তর দিন।
        ২. সরকারি ফি, প্রয়োজনীয় কাগজপত্র এবং দোকানের সার্ভিস চার্জ সুস্পষ্টভাবে বুলেট পয়েন্টে উল্লেখ করুন।
        ৩. গ্রাহককে দোকানে আসার জন্য বা WhatsApp/অনলাইনে ফর্ম জমা দেওয়ার জন্য আন্তরিকভাবে আমন্ত্রণ জানান।
        ৪. তথ্য সংক্ষিপ্ত কিন্তু তথ্যবহুল রাখুন।
    """.trimIndent()

    suspend fun generateResponse(
        prompt: String,
        history: List<Pair<String, String>> = emptyList()
    ): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY

        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext getLocalKnowledgeResponse(prompt)
        }

        try {
            val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey"

            val contentsArray = JSONArray()

            // Append short history if available
            val recentHistory = history.takeLast(4)
            for (turn in recentHistory) {
                val role = if (turn.first == "user") "user" else "model"
                val partObj = JSONObject().put("text", turn.second)
                val contentObj = JSONObject()
                    .put("role", role)
                    .put("parts", JSONArray().put(partObj))
                contentsArray.put(contentObj)
            }

            // Current prompt
            val userContent = JSONObject()
                .put("role", "user")
                .put("parts", JSONArray().put(JSONObject().put("text", prompt)))
            contentsArray.put(userContent)

            val rootJson = JSONObject().apply {
                put("contents", contentsArray)
                put(
                    "systemInstruction",
                    JSONObject().put(
                        "parts",
                        JSONArray().put(JSONObject().put("text", systemPrompt))
                    )
                )
                put(
                    "generationConfig",
                    JSONObject().apply {
                        put("temperature", 0.7)
                        put("topP", 0.9)
                        put("maxOutputTokens", 800)
                    }
                )
            }

            val mediaType = "application/json; charset=utf-8".toMediaType()
            val requestBody = rootJson.toString().toRequestBody(mediaType)

            val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string() ?: ""

            if (response.isSuccessful) {
                val json = JSONObject(responseBody)
                val candidates = json.optJSONArray("candidates")
                if (candidates != null && candidates.length() > 0) {
                    val firstCandidate = candidates.getJSONObject(0)
                    val content = firstCandidate.getJSONObject("content")
                    val parts = content.getJSONArray("parts")
                    if (parts.length() > 0) {
                        return@withContext parts.getJSONObject(0).getString("text").trim()
                    }
                }
            } else {
                Log.w("GeminiService", "API error: ${response.code} $responseBody")
            }
        } catch (e: Exception) {
            Log.e("GeminiService", "Error calling Gemini: ${e.message}", e)
        }

        // Fallback to offline knowledge base
        return@withContext getLocalKnowledgeResponse(prompt)
    }

    private fun getLocalKnowledgeResponse(prompt: String): String {
        val query = prompt.lowercase()

        return when {
            query.contains("সময়") || query.contains("খোলা") || query.contains("টাইম") || query.contains("কখন") -> {
                "🕒 **রিফাত কম্পিউটার ও ভ্যারাইটি স্টোর-এর সময়সূচি:**\n\n" +
                "• **কার্যদিবস:** প্রতিদিন খোলা থাকে।\n" +
                "• **দোকান খোলার সময়:** সকাল ৮:০০ টা।\n" +
                "• **দোকান বন্ধের সময়:** রাত ১০:০০ টা।\n" +
                "• **জুমার দিন:** শুক্রবার দুপুর ১২:৩০ থেকে দুপুর ২:০০ পর্যন্ত নামাজের বিরতি বাদে সবসময় খোলা।\n\n" +
                "📞 যেকোনো প্রয়োজনে সরাসরি কল বা হোয়াটসঅ্যাপ করতে পারেন: **+8801706727116**"
            }
            query.contains("এনআইডি") || query.contains("nid") || query.contains("ভোটার") || query.contains("পরিচয়পত্র") -> {
                "🪪 **জাতীয় পরিচয়পত্র (NID) সংশোধন ও রি-ইস্যু সংক্রান্ত তথ্য:**\n\n" +
                "📌 **প্রয়োজনীয় কাগজপত্র:**\n" +
                "১. বর্তমান NID কার্ডের কপি বা নম্বর\n" +
                "২. অনলাইন ডিজিটাল জন্ম নিবন্ধন সনদ\n" +
                "৩. এসএসসি/জেএসসি শিক্ষাগত যোগ্যতার সার্টিফিকেট\n" +
                "৪. পিতা ও মাতার এনআইডি কার্ডের কপি\n\n" +
                "💰 **ফি বিবরণ:**\n" +
                "• সরকারি ফি: ২৩০ টাকা\n" +
                "• দোকান সার্ভিস চার্জ: ১৫০ টাকা\n" +
                "⏱️ **সময়:** ১-২ কার্যদিবস\n\n" +
                "👉 আপনি অ্যাপের 'অনলাইন সেবা' ট্যাব থেকে সরাসরি অনলাইনে আবেদন জমা দিতে পারেন।"
            }
            query.contains("রেজাল্ট") || query.contains("ssc") || query.contains("hsc") || query.contains("মার্কশিট") || query.contains("ফলাফল") -> {
                "🎓 **এসএসসি / এইচএসসি পরীক্ষার রেজাল্ট ও মার্কশিট সেবা:**\n\n" +
                "📌 **প্রয়োজনীয় তথ্য:**\n" +
                "১. পরীক্ষার নাম (SSC / HSC / Dakhil)\n" +
                "২. রোল নম্বর (Roll Number)\n" +
                "৩. রেজিস্ট্রেশন নম্বর (Registration Number)\n" +
                "৪. পাসের সাল ও শিক্ষা বোর্ডের নাম\n\n" +
                "💰 **ফি:** রেজাল্ট ও কালার ফুল মার্কশিট প্রিন্ট মাত্র ২০ টাকা।\n" +
                "⏱️ **সময়:** ৫-১০ মিনিটেই সাথে সাথে ডেলিভারি।"
            }
            query.contains("পর্চা") || query.contains("খতিয়ান") || query.contains("জমি") || query.contains("নামজারি") || query.contains("দাগ") -> {
                "📜 **ই-পর্চা ও জমির খতিয়ান উত্তোলন সেবা:**\n\n" +
                "📌 **প্রয়োজনীয় তথ্য:**\n" +
                "১. জেলা, উপজেলা ও মৌজার নাম\n" +
                "২. খতিয়ান নম্বর অথবা দাগ নম্বর\n" +
                "৩. মালিকের নাম বা পূর্বপুরুষের নাম\n" +
                "৪. আবেদনকারীর NID ও মোবাইল নম্বর\n\n" +
                "💰 **ফি:** সরকারি ফি ১০০ টাকা + সার্ভিস চার্জ ১২০ টাকা।\n" +
                "⏱️ **বিতরণ সময়:** ২-৩ কার্যদিবসের মধ্যে ডাকযোগে বা অনলাইন কপি।"
            }
            query.contains("ড্রাইভিং") || query.contains("লাইসেন্স") || query.contains("brta") || query.contains("লার্নার") -> {
                "🚗 **BRTA ড্রাইভিং লাইসেন্স লার্নার ও স্মার্ট কার্ড আবেদন:**\n\n" +
                "📌 **প্রয়োজনীয় কাগজপত্র:**\n" +
                "১. জাতীয় পরিচয়পত্র (NID) এর কপি\n" +
                "২. রেজিস্টার্ড ডাক্তারের সিলযুক্ত মেডিকেল সার্টিফিকেট\n" +
                "৩. ন্যূনতম ৮ম শ্রেণি / এসএসসি পাশের সার্টিফিকেট\n" +
                "৪. পাসপোর্ট সাইজের ল্যাব প্রিন্ট ছবি\n\n" +
                "💰 **ফি:** সরকারি ফি ৫১৮ টাকা (মোটরবাইক/হালকা) + সার্ভিস চার্জ ২০০ টাকা।\n" +
                "⏱️ **সময়:** একই দিনে অনলাইন লার্নার অনুমোদন।"
            }
            query.contains("চাকরি") || query.contains("আবেদন") || query.contains("job") || query.contains("সার্কুলার") || query.contains("নিয়োগ") -> {
                "💼 **সরকারি ও প্রাইভেট চাকরির অনলাইন আবেদন সেবা:**\n\n" +
                "আমরা টেলিটক (Teletalk Alljobs) ও যেকোনো সরকারি দপ্তরের নির্ভুল আবেদন করে থাকি।\n\n" +
                "📌 **যা যা সাথে আনবেন:**\n" +
                "১. শিক্ষাগত সকল সনদের জিপিএ, সাল ও বোর্ড\n" +
                "২. পাসপোর্ট সাইজের রঙিন ছবি (300x300 px)\n" +
                "৩. সাদা কাগজে স্বাক্ষর (300x80 px)\n" +
                "৪. জাতীয় পরিচয়পত্র নম্বর\n\n" +
                "💰 **ফি:** পরীক্ষার সরকারি ফি + দোকান সার্ভিস চার্জ ৮০ টাকা।"
            }
            query.contains("পাসপোর্ট") || query.contains("passport") || query.contains("ই-পাসপোর্ট") -> {
                "🛂 **ই-পাসপোর্ট (e-Passport) অনলাইন আবেদন:**\n\n" +
                "📌 **প্রয়োজনীয় কাগজপত্র:**\n" +
                "১. NID কার্ড বা অনলাইন জন্ম নিবন্ধন সনদ\n" +
                "২. পিতা-মাতার এনআইডি তথ্য\n" +
                "৩. পেশাগত আইডি/প্রত্যয়নপত্র\n" +
                "৪. পূর্বের পাসপোর্ট (রি-ইস্যুর ক্ষেত্রে)\n\n" +
                "💰 **ফি:** ৫ বছর মেয়াদী (৪৮ পাতা) সরকারি ফি ৪,০২৫ টাকা + সার্ভিস চার্জ ২৫০ টাকা।"
            }
            query.contains("পেনড্রাইভ") || query.contains("মাউস") || query.contains("কিবোর্ড") || query.contains("দাম") || query.contains("প্রোডাক্ট") || query.contains("মালামাল") -> {
                "🖥️ **দোকানের জনপ্রিয় প্রোডাক্ট ও মূল্য তালিকা:**\n\n" +
                "• **SanDisk 64GB USB 3.0 Pen Drive:** ৳৬৮০\n" +
                "• **A4Tech Optical USB Mouse:** ৳৩৫০\n" +
                "• **Havit Multimedia Bangla Keyboard:** ৳৪৮০\n" +
                "• **Bashundhara A4 80GSM Paper Ream:** ৳৪২০\n" +
                "• **Casio FX-991EX Scientific Calculator:** ৳১৮৫০\n" +
                "• **Matador Pinpoint Gel Pen (20 pcs Box):** ৳১২০\n\n" +
                "🛍️ আপনি সরাসরি অ্যাপের 'প্রোডাক্টস' ট্যাব থেকে কার্টে যোগ করে অর্ডার করতে পারেন।"
            }
            query.contains("ঠিকানা") || query.contains("লোকেশন") || query.contains("কোথায়") || query.contains("দোকান") -> {
                "📍 **রিফাত কম্পিউটার ও ভ্যারাইটি স্টোর-এর ঠিকানা:**\n\n" +
                "• প্রধান বাজার রোড, বাজার সংলগ্ন মোড়।\n" +
                "• প্রোপাইটর: মো: রিফাত হোসেন\n" +
                "• 📞 মোবাইল / WhatsApp: +8801706727116\n" +
                "• 📧 ইমেইল: rjrifat700@gmail.com\n\n" +
                "🗺️ বিস্তারিত লোকেশন ম্যাপ দেখতে হোম স্ক্রিনের 'লোকেশন ম্যাপ' বাটনে ট্যাপ করুন।"
            }
            else -> {
                "ধন্যবাদ আপনার প্রশ্নের জন্য! 'রিফাত কম্পিউটার ও ভ্যারাইটি স্টোর' সকল প্রকার অনলাইন সেবা (NID, SSC/HSC রেজাল্ট, চাকরির আবেদন, ই-পর্চা, ড্রাইভিং লাইসেন্স, ই-পাসপোর্ট) এবং কম্পিউটার এক্সেসরিজ ও স্টেশনারি সামগ্রী সরবরাহ করে থাকে।\n\n" +
                "📌 আপনি কি নির্দিষ্ট কোনো সেবা বা প্রডাক্টের দাম/ফি জানতে চান? যেমন:\n" +
                "• এনআইডি সংশোধন ফি ও ডকুমেন্টস\n" +
                "• ড্রাইভিং লাইসেন্স বা ই-পর্চা আবেদন পদ্ধতি\n" +
                "• চাকরির আবেদন ফি\n" +
                "• পেনড্রাইভ বা মাউসের দাম\n\n" +
                "প্রয়োজনে সরাসরি আমাদের কল বা হোয়াটসঅ্যাপ করুন: **+8801706727116**"
            }
        }
    }
}
