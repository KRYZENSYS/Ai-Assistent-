# AI Call Assistant 🤖📞

AI Call Assistant - bu Android platformasi uchun maxsus ishlab chiqilgan, production darajasidagi va to'liq offline-resilient ishlaydigan sun'iy intellektli ovozli yordamchi ilovasi. Ilova **Kotlin** va **Jetpack Compose** yordamida yozilgan bo'lib, eng zamonaviy **Material 3** dizayn tili va jozibador **Cyberpunk / Cyber UI** estetikasiga asoslangan.

Ilova Android 15 (API 35+) operatsion tizimi xavfsizlik va fon rejimlari talablariga 100% mos ravishda optimallashtirilgan.

---

## 🎨 Dizayn Kontseptsiyasi (Cyber UI)
Ilova dizayni quyidagi vizual xususiyatlar bilan ajralib turadi:
*   **Neon Glow & Obsidian Surfaces:** To'q rangli neon ko'k (Cyan), qizg'ish pushti (Magenta) va yorqin yashil (Lime Green) neon chiziqlar, yarim shaffof oynasimon (Glassmorphic) kartalar.
*   **Dynamic Sound Waveform:** Suhbat davomida ovoz kuchi va holatiga qarab real vaqtda harakatlanuvchi va rangini o'zgartiruvchi Canvas'da chizilgan dinamik to'lqinlar.
*   **Pulse Status Signals:** Tizim monitoringi, tarmoq ulanishi va AI dvigatelining faolligini bildiruvchi miltillovchi LED indikatorlar.
*   **Tactile Feedback:** Bosiladigan har bir Cyber-tugma foydalanuvchiga ripl (ripple) va vizual reaksiya qaytaradi.

---

## 🛠️ Arxitektura va Texnik Qurilma
Ilova yuqori unumdorlik va kengayuvchanlikni ta'minlash uchun **Clean Architecture** va **MVVM (Model-View-ViewModel)** naqshiga asoslangan:

1.  **UI & Presentation Layer:**
    *   **Jetpack Compose:** Butunlay deklarativ va reaktiv foydalanuvchi interfeysi.
    *   **Navigation Compose:** Type-safe ekranlararo yo'naltirish backstack tizimi.
2.  **Service & Telephony Monitoring:**
    *   **Foreground Service (`CallAssistantService`):** Android 14/15 fon cheklovlaridan omon qoluvchi va `FOREGROUND_SERVICE_TYPE_PHONE_CALL` turini qo'llab-quvvatlovchi, tizimli qo'ng'iroq signallarini doimiy kuzatuvchi xizmat.
    *   **BroadcastReceiver (`BootReceiver`):** Telefon qayta yoqilganda (`RECEIVE_BOOT_COMPLETED`), agar AI rejimi faol bo'lgan bo'lsa, xizmatni avtomatik qayta yuklovchi tizimli xizmat.
3.  **Local Persistence (Room Database):**
    *   **SQLite-based Room persistence:** Suhbatlar transkriptlari tarixi (`ConversationEntity`) va diagnostika tizim jurnallari jadvallarini (`LogEntity`) reactive Flow orqali UI ga uzatadi.
4.  **Multi-AI Provider Integration (Retrofit & OkHttp):**
    *   **AI Providers Support:** Groq, OpenAI, Gemini va OpenRouter xizmatlarini birgalikda va alohida qo'llab-quvvatlash.
    *   **SSL Pinning:** Barcha aloqa serverlari bilan xavfsiz SSL bog'lanish va HTTPS sertifikat pinning himoyasi.
    *   **Exponential Backoff Retry:** Tarmoq xatoliklari bo'lganda so'rovlarni avtomatik ravishda kutish bilan 2 marta qayta takrorlash mexanizmi.
5.  **Voice Interaction (Speech Engine):**
    *   **Speech-to-Text (STT):** Native Android `SpeechRecognizer` API.
    *   **Text-to-Speech (TTS):** Native Android `TextToSpeech` API. Ovoz tezligi, balandligi va tillarini (Uzbek, Russian, English) sozlash imkoniyati bilan.
    *   **Language Auto-Detection Heuristics:** Ovoz yoki matn tarkibiga qarab muloqot tilini avtomatik aniqlash algoritmi.

---

## 🔒 Xavfsizlik va Maxfiylik (Security First)
*   **Android Keystore Protection:** Foydalanuvchining barcha API kalitlari oddiy matn sifatida saqlanmaydi. Ular **Android KeyStore** datchigi ichida generatsiya qilingan apparat darajasidagi **AES-256 bitli (AES/GCM/NoPadding)** kalit yordamida shifrlanib, so'ng SharedPreferences'ga yoziladi.
*   **Cleartext Block & SSL Pinning:** `network_security_config.xml` orqali shifrlanmagan (cleartext HTTP) aloqa butunlay taqiqlangan va HTTPS aloqa xavfsizligini ta'minlash uchun SSL pin digestlari o'rnatilgan.
*   **Log Scrubbing & Debug/Release Separation:** Ilova ichidagi `AppLogger` tizimi `BuildConfig.DEBUG` orqali faqat Debug versiyada batafsil jurnallarni chiqaradi. Chiqarilgan jurnallar tarkibidagi Authorization Bearer kalitlari va API keys regex orqali avtomatik ravishda loglardan tozalab tashlanadi (Redaction).
*   **R8 / ProGuard Obfuscation:** Release build turlarida kod optimizatsiyasi (`isMinifyEnabled = true`), resurslar siqilishi (`isShrinkResources = true`) va xavfsiz obfuscation ProGuard qoidalari yoqilgan.

---

## ⚙️ Asosiy Ekranlar va Imkoniyatlar

1.  **Pult (Dashboard Console):** AI rejimini yoqish/o'chirish master tugmasi, tarmoq datchiklari, so'nggi statistika va eng asosiysi — **Qo'ng'iroq Multi-Simulyatori**. Ism va raqam kiritib istalgan tilda qo'ng'iroq simulyatsiyasini ishga tushirish mumkin.
2.  **Ovozli Tarix (History Screen):** Saqlangan suhbatlar ro'yxati, matn bo'yicha tezkor qidiruv, saralash (sevimlilar), transkript muloqot pufakchalari va chatni xavfsiz `.txt` fayl sifatida boshqa ilovalar bilan ulashish (Export).
3.  **Simulyator Ekran (Active Call Console):** Aloqa davomiyligi taymeri, real vaqtda sinxron mikrofon animatsiyalari, live muloqot transkript tasmasi va aloqani yakunlash boshqaruvi.
4.  **Log Terminal (Diagnostics Log):** Tarmoq ulanish datchigi, Groq / AI ping-testi va ilova ichidagi xavfsiz tozalangan jonli log jurnali terminali.
5.  **Sozlamalar (Settings Panel):** 
    *   **AI Provider Selector:** Xizmatlar (Groq, Gemini, OpenAI, OpenRouter) orasidan bitta tugma orqali o'tish.
    *   **API Keys Secure Management:** Har bir xizmat uchun alohida Keystore-shifrlangan kalitlar maydoni.
    *   **Recommended Presets:** Har bir provayder uchun eng tezkor va arzon modellar (masalan, `gpt-4o-mini`, `gemini-3.5-flash`, `llama-3.3-70b-versatile`) bir marta bosish orqali tanlanadi.
    *   **Custom Model ID:** Ro'yxatda yo'q modellarni qo'lda yozib sozlash imkoniyati.
    *   **Voice & Calibration:** AI tizimli promptini o'zgartirish, ovoz tezligi va balandligini sozlash, ma'lumotlar bazasini tozalash.

---

## 🚀 Qanday Kompilyatsiya va Build Qilinadi?

Loyiha Android Studio'da to'g'ridan-to'g'ri ochish va ishga tushirish uchun 100% tayyor holatda:
1.  Loyihani yuklab oling va **Android Studio** orqali oching.
2.  Loyiha avtomatik ravishda Gradle sinxronizatsiyasini amalga oshiradi.
3.  Ilova ichidagi **Settings** bo'limiga o'ting, mos provayderni tanlang va o'zingizning shaxsiy API kalitingizni kiriting.
4.  **Run** (Yashil uchburchak) tugmasini bosing yoki terminalda `./gradlew assembleDebug` buyrug'i orqali APK build qiling.
