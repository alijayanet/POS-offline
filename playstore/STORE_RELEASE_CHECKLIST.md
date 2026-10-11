# 🚀 Checklist Persiapan Rilis Google Play Console (TokoPOS)

Ikuti langkah-langkah di bawah ini untuk mengunggah aplikasi **TokoPOS** ke Google Play Console hingga siap dipublikasikan:

---

## 1. Verifikasi Izin & Manifest (Sudah Disesuaikan ✅)
- [x] **Izin Lokasi**: Dibatasi dengan `maxSdkVersion="30"` agar tidak meminta izin lokasi berbahaya di Android 12+.
- [x] **Izin Bluetooth**: Menggunakan `neverForLocation` pada `BLUETOOTH_SCAN`.
- [x] **Izin Kamera**: Digunakan murni on-device untuk scan barcode produk via Google ML Kit tanpa menyimpan foto.
- [x] **Scoped Storage**: Tidak meminta `MANAGE_EXTERNAL_STORAGE` ataupun `WRITE_EXTERNAL_STORAGE`.
- [x] **Package Visibility**: Dideklarasikan secara eksplisit dengan `<queries>` untuk WhatsApp dan RawBT tanpa `QUERY_ALL_PACKAGES`.
- [x] **Kebijakan Privasi di Aplikasi**: Sudah tersedia di menu *Pengaturan > Kebijakan Privasi & Keamanan Data*.

---

## 2. Cara Generate File `.aab` (Android App Bundle) untuk Play Store

Google Play Store mewajibkan format **`.aab` (Android App Bundle)** untuk aplikasi baru (bukan `.apk`).

### Langkah Pembuatan:
1. Pastikan Anda memiliki keystore release (atau buat keystore baru di Android Studio melalui menu `Build > Generate Signed Bundle / APK > Android App Bundle`).
2. Atau jalankan perintah Gradle berikut di terminal:
   ```powershell
   $env:JAVA_HOME = "C:\Program Files\Android\Android Studio\jbr"; & "$env:JAVA_HOME\bin\java.exe" "-Dfile.encoding=UTF-8" -jar "gradle\wrapper\gradle-wrapper.jar" bundleRelease
   ```
3. File `.aab` yang dihasilkan akan berada di folder:
   `app/build/outputs/bundle/release/app-release.aab`
4. File `.aab` inilah yang diunggah ke Google Play Console pada menu **Produksi (Production) > Buat Rilis Baru (Create New Release)**.

---

## 3. Checklist Formulir di Google Play Console

Selesaikan checklist "Penyiapan Aplikasi" (Set up your app) di Play Console:

1. **Kebijakan Privasi (Privacy Policy):**
   - Masukkan link URL: `https://alijayanet.github.io/POS-offline/privacy-policy.html`
   - *(Atau unggah file `privacy-policy.html` ke hosting/domain Anda sendiri).*
2. **Akses Aplikasi (App Access):**
   - Pilih: *Semua fungsionalitas tersedia tanpa batasan* (Aplikasi tidak memiliki login/password).
3. **Iklan (Ads):**
   - Pilih: *Tidak, aplikasi saya tidak berisi iklan*.
4. **Rating Konten (Content Rating):**
   - Isi kuesioner rating: Pilih kategori *Utilitas, Produktivitas, Komunikasi, atau Lainnya*.
   - Jawab "Tidak" pada semua pertanyaan kekerasan, konten seksual, atau perjudian.
   - Hasil rating: **PEGI 3 / Everyone**.
5. **Target Audiens (Target Audience):**
   - Pilih kelompok usia: **18 tahun ke atas**.
   - Pilih "Tidak" untuk apakah aplikasi menarik bagi anak-anak.
6. **Aplikasi Berita, COVID-19, atau Finansial:**
   - Jawab "Bukan aplikasi berita", "Bukan aplikasi pelacak COVID-19", "Bukan aplikasi pinjaman uang online / P2P lending".
7. **Keamanan Data (Data Safety):**
   - Ikuti panduan lengkap di file [`DATA_SAFETY_DECLARATION_GUIDE.md`](./DATA_SAFETY_DECLARATION_GUIDE.md) (Pilih **Tidak ada data yang dikumpulkan/dibagikan**).
8. **Detail Listingan Toko (Store Listing):**
   - Salin Judul, Deskripsi Singkat, dan Deskripsi Lengkap dari file [`APP_LISTING_METADATA.md`](./APP_LISTING_METADATA.md).
   - Unggah Ikon Aplikasi (512x512 px).
   - Unggah Grafis Fitur (Feature Graphic 1024x500 px).
   - Unggah Tangkapan Layar (Screenshots) minimal 4 gambar (Kasir, Stok, Nota Manual, Tagihan Bulanan).
