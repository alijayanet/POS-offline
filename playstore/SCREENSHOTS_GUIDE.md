# 📸 Panduan Screenshot & Aset Grafis Google Play Store (TokoPOS)

Saat mengunggah ke Google Play Console, Anda wajib mengunggah **Tangkapan Layar (Screenshots)**, **Ikon Aplikasi**, dan **Grafis Fitur (Feature Graphic)**.

---

## 1. Package Name (Application ID)

- **Package Name Resmi:**
  ```
  com.alijaya.tokopos.app
  ```
  *(Dapat dilihat pada berkas `app/build.gradle.kts`)*

> [!IMPORTANT]
> Begitu Anda mengunggah file `TokoPOS.aab` ke Google Play Console untuk pertama kalinya, Package Name ini akan **terkunci permanen**. Jika Anda ingin mengubahnya menjadi nama domain/brand Anda sendiri (misal: `com.alijayanet.tokopos`), ubah `applicationId` di `app/build.gradle.kts` **sebelum** upload pertama kali.

---

## 2. Ketentuan Screenshot Google Play Store

- **Jumlah:** Minimal **4 screenshot** (Maksimal 8) untuk kategori Ponsel (*Phone*).
- **Format:** PNG (tanpa alpha) atau JPEG.
- **Rasio:** 16:9 atau 9:16 (vertikal/potret).
- **Resolusi:** Minimal 320 px, maksimal 3840 px.
- **Cara Termudah:** Instal file `TokoPOS.apk` yang sudah ada di HP Android Anda, lalu ambil screenshot langsung (tombol *Power + Volume Bawah*). Hasil tangkapan layar HP modern (1080x2400, 1080x1920, 720x1600 px) **100% otomatis diterima oleh Play Store**.

---

## 3. Rekomendasi 6 Tangkapan Layar Terbaik

Untuk menarik calon pembeli dan terlihat profesional di Play Store, ambil 6 screenshot berikut dari aplikasi TokoPOS:

| No | Halaman Aplikasi | Yang Perlu Ditampilkan di Layar | Keterangan Teks Promosi (Jika Diedit) |
| :--- | :--- | :--- | :--- |
| **1** | **Kasir POS (Checkout)** | Buka menu Kasir, tambahkan beberapa item belanja ke keranjang, tampilkan total belanja dan tombol "Bayar". | *"Kasir Penjualan Cepat & Multi Pembayaran"* |
| **2** | **Kelola Stok & Input Produk** | Buka menu Stok Barang, klik tombol tambah produk baru, perlihatkan kolom **Kode SKU** dan **Barcode Produk** dengan tombol **Scan**. | *"Manajemen Stok, SKU & Scan Barcode Kamera"* |
| **3** | **Cetak Struk & QRIS** | Lakukan checkout, buka dialog struk thermal atau cetak QRIS dinamis. | *"Cetak Struk Bluetooth Thermal & QRIS Dinamis"* |
| **4** | **Nota Manual Bebas** | Buka menu Nota Manual, isi transaksi barang/jasa instan dengan pilihan satuan (Pcs, Kg, Porsi, Meter, dll.). | *"Kuitansi Bebas Satuan & Kirim Nota WhatsApp"* |
| **5** | **Tagihan Pelanggan Bulanan** | Buka menu Tagihan, perlihatkan daftar pelanggan WiFi/TV Kabel, status lunas/jatuh tempo, dan tombol cetak ID Card Member. | *"Kelola Tagihan Bulanan RT/RW Net & Iuran"* |
| **6** | **Laporan Keuangan** | Buka menu Laporan, tampilkan ringkasan omzet harian/bulanan, laba kotor, laba bersih, dan pengeluaran. | *"Laporan Keuangan & Laba Rugi Otomatis"* |

---

## 4. Aset Grafis Lain yang Wajib Diunggah di Play Console

Selain screenshot, formulir *Main Store Listing* mewajibkan 2 aset grafis berikut:

### A. Ikon Aplikasi (App Icon)
- **Ukuran:** Tepat **512 x 512 px**
- **Format:** PNG 32-bit (dengan alpha atau tanpa alpha)
- **Ukuran File Maksimal:** 1 MB
- *(Anda bisa menggunakan logo `ic_launcher` toko yang sudah ada atau membuat logo 512x512 di Canva).*

### B. Grafis Fitur (Feature Graphic)
- **Ukuran:** Tepat **1024 x 500 px**
- **Format:** JPEG atau PNG 24-bit (tanpa alpha)
- **Ukuran File Maksimal:** 15 MB
- **Deskripsi:** Banner promosi yang muncul di bagian atas halaman aplikasi di Play Store. Berisi logo TokoPOS dan tagline singkat, misal: *"Aplikasi Kasir & Stok Toko 100% Offline"*. Anda dapat mendesainnya secara gratis di Canva (template ukuran 1024x500 px).
