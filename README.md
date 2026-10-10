# 🏪 TokoPOS - Aplikasi Kasir, Stok & Tagihan Bulanan (100% Offline)

[![Android](https://img.shields.io/badge/Platform-Android-3DDC84?style=flat&logo=android&logoColor=white)](https://android.com)
[![Kotlin](https://img.shields.io/badge/Language-Kotlin-7F52FF?style=flat&logo=kotlin&logoColor=white)](https://kotlinlang.org)
[![Jetpack Compose](https://img.shields.io/badge/UI-Jetpack%20Compose%20%26%20Material%203-4285F4?style=flat&logo=jetpackcompose&logoColor=white)](https://developer.android.com/jetpack/compose)
[![Database](https://img.shields.io/badge/Database-Room%20(SQLite)-003B57?style=flat&logo=sqlite&logoColor=white)](https://developer.android.com/training/data-storage/room)
[![Printer](https://img.shields.io/badge/Thermal%20Printer-Bluetooth%20ESC%2FPOS-orange?style=flat)](https://github.com/alijayanet/POS-offline)
[![License](https://img.shields.io/badge/License-MIT-green?style=flat)](LICENSE)

**TokoPOS** adalah aplikasi Kasir Pintar (*Point of Sale*), Manajemen Stok Barang, Pembukuan Tagihan/Pelanggan Bulanan (RT/RW Net, TV Kabel, Iuran, dll.), dan Pembuat Nota Manual yang dirancang **100% Offline (Tanpa Internet & Tanpa Server)**. Semua data tersimpan aman secara lokal di memori smartphone menggunakan database SQLite Room.

---

## 🚀 Fitur-Fitur Unggulan

### 1. 🛒 Kasir POS & Checkout Cepat
- **Multi Metode Pembayaran**: Tunai (Cash) dengan hitung kembalian otomatis, Transfer Bank, QRIS Dinamis, dan Kasbon / Hutang Tempo.
- **Katalog Produk Cepat**: Pencarian instan, filter kategori, dan scanner barcode kamera langsung.
- **Diskon & Pajak**: Fleksibilitas diskon per transaksi atau per item produk.
- **Struk Penjualan**: Cetak langsung ke printer Bluetooth thermal 58mm/80mm atau bagikan struk digital via WhatsApp.

### 2. 🧾 Nota & Kuitansi Manual Bebas
- Fitur pembuatan nota untuk jasa, custom order, atau transaksi bebas tanpa harus mendaftarkan barang ke stok terlebih dahulu.
- Input nama barang/jasa, kuantitas satuan (*Pcs, Kg, Meter, Porsi, Paket, Lembar, Liter, Box, dll.*), dan harga satuan dengan kalkulasi subtotal live.
- Cetak struk thermal kuitansi, kirim nota rincian ke WhatsApp pembeli, dan simpan langsung ke riwayat pembukuan toko.

### 3. 📦 Manajemen Stok & Inventaris Barang
- Manajemen katalog barang lengkap: SKU/Barcode, Kategori, Harga Beli (Modal), Harga Jual, dan Jumlah Stok.
- **Cetak Label Barcode Barang**: Cetak stiker label barcode 1D (Code128) langsung ke printer Bluetooth thermal.
- **Notifikasi Stok Menipis**: Peringatan visual otomatis untuk produk yang stoknya hampir habis.
- **Import & Export Excel / CSV**: Impor ribuan data barang dari file spreadsheet CSV/Excel dan ekspor stok toko dengan sekali klik.

### 4. 📄 Tagihan Pelanggan Bulanan (WiFi / TV Kabel / Iuran)
- Pengelolaan master data pelanggan bulanan dengan paket langganan dan tanggal jatuh tempo.
- **ID Card Barcode Member**: Cetak kartu ID Card barcode pelanggan untuk memudahkan pembayaran bulanan di kasir dengan scan barcode.
- **Pencegahan Bayar Ganda**: Sistem otomatis mendeteksi status tagihan dan memperpanjang masa aktif ke bulan berikutnya.
- **Notifikasi WhatsApp Kekinian**: Kirim rincian tagihan bulanan berformat rapi dan profesional lengkap dengan daftar nomor rekening toko (*BCA, BRI, Dana, OVO, ShopeePay*).
- **Cetak Kuitansi Langganan**: Cetak struk bukti bayar tagihan bulanan pada printer thermal.

### 5. 💳 Integrasi QRIS Dinamis Offline (EMVCo Engine)
- Mengubah QRIS Statis toko menjadi **QRIS Dinamis secara offline** sesuai nominal belanja yang harus dibayar.
- Pembeli cukup scan QR Code di layar HP atau di kertas struk thermal, nominal tagihan langsung terisi otomatis tanpa perlu mengetik manual.
- Dilengkapi kalibrasi feed margin agar hasil cetak QR Code di kertas struk tidak terpotong pisau printer.

### 6. 📊 Laporan Keuangan & Laba Rugi
- Ringkasan omzet harian, mingguan, dan bulanan.
- Laporan laba kotor, laba bersih, serta biaya pengeluaran operasional toko.
- Ekspor rekap transaksi penjualan ke format Excel/CSV.

### 7. 🖨️ Dukungan Printer Thermal Bluetooth Universal
- Kompatibel dengan hampir seluruh printer thermal Bluetooth 58mm & 80mm di pasaran (Panda, Goojprt, Iware, Zjiang, Epson, Enibit, MPT-II, POS-5802, dll.).
- Dilengkapi algoritma *RFCOMM fallback* untuk printer yang menolak UUID standar.

### 8. 💾 Backup & Restore Database Lokal
- Ekspor seluruh database toko ke file cadangan (Backup JSON/CSV).
- Restore data kapan saja saat berganti HP baru tanpa khawatir kehilangan data riwayat penjualan.

---

## 🛠️ Arsitektur & Teknologi

* **Bahasa Pemrograman**: [Kotlin](https://kotlinlang.org/) (100%)
* **UI Framework**: [Jetpack Compose](https://developer.android.com/jetpack/compose) dengan [Material 3 Design](https://m3.material.io/)
* **Pola Arsitektur**: MVVM (Model-View-ViewModel) + Clean Architecture
* **Database Lokal**: [Android Room Database](https://developer.android.com/training/data-storage/room) (SQLite ORM)
* **Asynchronous & Reactive**: Kotlin Coroutines & Kotlin StateFlow / SharedFlow
* **Barcode & QR Processing**:
  - [Google ML Kit Barcode Scanning](https://developers.google.com/ml-kit/vision/barcode-scanning)
  - [ZXing Core](https://github.com/zxing/zxing)
* **Thermal Printing Driver**: Custom Pure ESC/POS Raster & Text Engine over Android Bluetooth SPP RFCOMM Socket

---

## 💻 Panduan Membuka & Memodifikasi Source Code

### Prasyarat:
1. Pasang **[Android Studio](https://developer.android.com/studio)** (Versi Ladybug, Koala, Jellyfish, atau yang lebih baru).
2. **JDK 17** atau **JDK 21** terpasang di sistem operasi Anda.
3. Android SDK Platform 34 (Android 14) atau 35.

### Langkah Membuka Project:
1. Clone repository ini ke komputer Anda:
   ```bash
   git clone https://github.com/alijayanet/POS-offline.git
   ```
2. Buka Android Studio, pilih **Open...**, lalu arahkan ke folder hasil clone: `POS-offline`.
3. Tunggu proses **Gradle Sync** selesai mengunduh semua library dependensi yang dibutuhkan.
4. Anda bebas memodifikasi source code sesuai dengan kebutuhan usaha atau bisnis Anda.

---

## 📦 Cara Membuat / Meng-generate File APK Sendiri

### Cara 1: Menggunakan Android Studio (GUI)
1. Buka project di Android Studio.
2. Klik menu **Build** pada toolbar atas.
3. Pilih **Build Bundle(s) / APK(s)** -> **Build APK(s)**.
4. Tunggu beberapa saat hingga muncul notifikasi pop-up di pojok kanan bawah: *APK(s) generated successfully*.
5. Klik **locate** untuk membuka folder tempat file `app-debug.apk` tersimpan.

### Cara 2: Menggunakan Terminal / Command Line
Buka Terminal / PowerShell di folder root project, lalu jalankan:

* **Untuk Windows (PowerShell / Command Prompt):**
  ```powershell
  .\gradlew.bat assembleDebug
  ```

* **Untuk Linux / macOS:**
  ```bash
  chmod +x gradlew
  ./gradlew assembleDebug
  ```

Setelah build berhasil (*BUILD SUCCESSFUL*), file APK akan berada di lokasi:
```
app/build/outputs/apk/debug/app-debug.apk
```
Salin file tersebut ke smartphone Android Anda untuk langsung dipasang (*install*).

---

## 📂 Struktur Direktori Utama

```
TokoPOS---Kasir-&-Stok/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/example/
│   │   │   │   ├── data/
│   │   │   │   │   ├── db/          # Room AppDatabase & TypeConverters
│   │   │   │   │   ├── model/       # Data Class Entity (Product, Order, Customer, etc.)
│   │   │   │   │   └── repository/  # Room DAO & StoreRepository
│   │   │   │   ├── ui/
│   │   │   │   │   ├── components/  # Dialogs, Buttons, Widgets, Receipt Viewer
│   │   │   │   │   ├── navigation/  # Screen Routes & Bottom Navigation Setup
│   │   │   │   │   ├── screens/     # Dashboard, POS, Inventory, History, Subscription, etc.
│   │   │   │   │   └── theme/       # Material 3 Color Schemes & Typography
│   │   │   │   └── util/            # Bluetooth Thermal Driver, QRIS Engine, Barcode Tools
│   │   │   └── res/                 # App Icons, Strings, Drawables, XML Layouts
│   │   └── AndroidManifest.xml
│   └── build.gradle.kts
├── gradle/                          # Gradle Wrapper
├── build.gradle.kts
├── settings.gradle.kts
├── README.md
└── LICENSE
```

---

## 🤝 Kontribusi & Lisensi

Proyek ini dibuat untuk mendukung digitalisasi UMKM di seluruh Indonesia. Anda sangat dipersilakan untuk:
- Melakukan **Fork** repository ini.
- Menambahkan fitur-fitur baru (misal: multi-outlet, integrasi EDC, loyalti poin, dll.).
- Mengirimkan **Pull Request** atau membuka **Issues** jika menemukan bug atau kendala teknis.

Proyek ini dilisensikan di bawah [MIT License](LICENSE).

---
*Dibuat dengan ❤️ untuk kemajuan UMKM & Toko Indonesia.*
