# AI Call Assistant 🤖📞

AI Call Assistant - bu Android platformasi uchun maxsus ishlab chiqilgan, production darajasidagi va to'liq offline-resilient ishlaydigan sun'iy intellektli ovozli yordamchi ilovasi. Ilova **Kotlin** va **Jetpack Compose** yordamida yozilgan bo'lib, eng zamonaviy **Material 3** dizayn tili va jozibador **Cyberpunk / Cyber UI** estetikasiga asoslangan.

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
    *   **Foreground Service (`CallAssistantService`):** Android fon cheklovlaridan omon qoluvchi, tizimli qo'ng'iroq signallarini doimiy kuzatuvchi xizmat.
    *   **BroadcastReceiver (`CallReceiver`):** Kiruvchi qo'ng'iroqlarni (`RINGING`, `OFFHOOK`, `IDLE`) real vaqtda aniqlash uchun TelephonyManager signallarini tutib oladi.
3.  **Local Persistence (Room Database):**
    *   **SQLite-based Room persistence:** Suhbatlar transkriptlari tarixi (`ConversationEntity`) va diagnostika tizim jurnallari jadvallarini (`LogEntity`) reactive Flow orqali UI ga uzatadi.
4.  **Network Integration (Retrofit & OkHttp):**
    *   **Groq API client:** OpenAI standartlariga mos keluvchi Groq completions API'si (`api.groq.com/openai/v1`).
    *   **Dynamic Auth Interceptor:** Sozlamalardan kiritilgan API kalitlarini real vaqtda yangilab yuborish.
5.  **Voice Interaction (Speech Engine):**
    *   **Speech-to-Text (STT):** Native Android `SpeechRecognizer` API.
    *   **Text-to-Speech (TTS):** Native Android `TextToSpeech` API. Ovoz tezligi, balandligi va tillarini (Uzbek, Russian, English) sozlash imkoniyati bilan.
    *   **Language Auto-Detection Heuristics:** Ovoz yoki matn tarkibiga qarab muloqot tilini avtomatik aniqlash algoritmi.

---

## 🔒 Xavfsizlik va Maxfiylik (Security First)
*   **Secure API Storage:** Foydalanuvchining shaxsiy Groq API kaliti oddiy matn sifatida saqlanmaydi. U **Base64** va maxsus **XOR bayt manipulyatsiyasi** yordamida shifrlanib, so'ng SharedPreferences'ga yoziladi. Bu xavfsizlik chorasi APK dekompilyatsiya qilinganda kalitlarning o'g'irlanishini oldini oladi.
*   **SQL Injection Protection:** Room kutubxonasi yordamida barcha so'rovlar parametrlar bilan bog'lanadi (Precompiled Queries), bu esa SQL inyeksiyalari xavfini nolga tushiradi.
*   **Memory Leak Prevention:** TTS, STT va BroadCast ruxsatnomalari kerakli hayotiy tsikllarda (onCleared, onDestroy) xavfsiz o'chiriladi.

---

## ⚙️ Asosiy Ekranlar va Imkoniyatlar

1.  **Pult (Dashboard Console):** AI rejimini yoqish/o'chirish master tugmasi, tarmoq datchiklari, so'nggi statistika va eng asosiysi — **Qo'ng'iroq Multi-Simulyatori**. Ism va raqam kiritib istalgan tilda qo'ng'iroq simulyatsiyasini ishga tushirish mumkin.
2.  **Ovozli Tarix (History Screen):** Saqlangan suhbatlar ro'yxati, matn bo'yicha tezkor qidiruv, saralash (sevimlilar), transkript muloqot pufakchalari va chatni xavfsiz `.txt` fayl sifatida boshqa ilovalar bilan ulashish (Export).
3.  **Simulyator Ekran (Active Call Console):** Aloqa davomiyligi taymeri, real vaqtda sinxron mikrofon animatsiyalari, live muloqot transkript tasmasi va aloqani yakunlash boshqaruvi.
4.  **Diagnostika Terminali (Diagnostics Log):** Tarmoq tezligi tekshiruvi, Groq ping-testi va ilova ichidagi jonli log jurnali (Log Terminal).
5.  **Sozlamalar (Settings Panel):** API kalitini kiritish/ko'rish, AI tizimli promptini o'zgartirish, ovoz tezligi va balandligini slayderlar orqali sozlash va ma'lumotlar bazasini butunlay tozalash.

---

## 🚀 Qanday Kompilyatsiya va Build Qilinadi?

Loyiha Android Studio'da to'g'ridan-to'g'ri ochish va ishga tushirish uchun 100% tayyor holatda:
1.  Loyihani yuklab oling va **Android Studio** orqali oching.
2.  Loyiha avtomatik ravishda Gradle sinxronizatsiyasini amalga oshiradi.
3.  Ilova ichidagi **Settings** bo'limiga o'ting va o'zingizning shaxsiy `GROQ_API_KEY` kalitingizni kiriting (Kalit olish uchun: [console.groq.com](https://console.groq.com/)).
4.  **Run** (Yashil uchburchak) tugmasini bosing yoki `./gradlew assembleDebug` buyrug'i orqali APK faylini build qiling.
