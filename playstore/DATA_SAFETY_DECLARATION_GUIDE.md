# 📋 Panduan Pengisian Formulir Keamanan Data (Data Safety) Google Play Console

Saat mengunggah TokoPOS ke **Google Play Console**, Anda wajib mengisi kuesioner **"Keamanan Data" (Data Safety)**. Formulir ini sangat ketat diperiksa oleh Google. 

Gunakan panduan jawaban resmi di bawah ini agar aplikasi TokoPOS **langsung disetujui (Approved)** tanpa penolakan:

---

## 1. Ikhtisar Pengumpulan dan Pembagian Data

| Pertanyaan Google Play Console | Jawaban yang Harus Dipilih | Penjelasan Tambahan |
| :--- | :--- | :--- |
| **Apakah aplikasi Anda mengumpulkan atau membagikan jenis data pengguna yang diperlukan?** | **TIDAK (No)** | TokoPOS beroperasi 100% offline. Semua data transaksi tersimpan di database lokal smartphone pengguna. |
| **Apakah semua data pengguna yang dikumpulkan oleh aplikasi Anda dienkripsi saat transit?** | *(Otomatis terlewati jika menjawab Tidak)* | N/A |
| **Apakah aplikasi Anda menyediakan cara bagi pengguna untuk meminta agar data mereka dihapus?** | **YA (Yes)** | Pengguna dapat menghapus seluruh data kapan saja melalui menu Pengaturan > Reset Database, atau dengan mencopot pemasangan aplikasi (uninstall). |

---

## 2. Rincian Izin Perangkat Spesifik

Jika Google Play Console menanyakan tentang izin tertentu:

### A. Kamera (`CAMERA`)
- **Apakah data kamera dikumpulkan atau dibagikan?** -> **TIDAK**.
- **Tujuan:** Hanya digunakan untuk fitur deteksi barcode kemasan produk dan scan QRIS secara instan di perangkat (*on-device scanning* dengan Google ML Kit). Tidak ada foto yang disimpan atau dikirim ke internet.

### B. Bluetooth (`BLUETOOTH_CONNECT`, `BLUETOOTH_SCAN`)
- **Apakah Bluetooth digunakan untuk melacak lokasi?** -> **TIDAK**.
- **Tujuan:** Hanya digunakan untuk transmisi data cetak teks/struk (ESC/POS) ke printer thermal Bluetooth. Menggunakan flag resmi `neverForLocation`.

### C. Lokasi (`ACCESS_FINE_LOCATION`, `ACCESS_COARSE_LOCATION`)
- **Status:** Di manifest dibatasi hanya untuk Android lama (`maxSdkVersion="30"`). Aplikasi **TIDAK** meminta izin lokasi pada Android 12+.
- Tidak ada data lokasi yang dikumpulkan atau disimpan.

### D. Keuangan & Pembayaran
- **Apakah data keuangan pengguna dikumpulkan ke cloud?** -> **TIDAK**.
- Seluruh nominal kasir, riwayat penjualan, dan tagihan disimpan hanya di memori internal pengguna.

---

## 3. URL Kebijakan Privasi (Privacy Policy URL)

Di bagian **Kebijakan Privasi (Privacy Policy)** di Play Console:
- Masukkan URL publik yang mengarah ke file `privacy-policy.html`.
- Contoh menggunakan GitHub Pages (aktifkan GitHub Pages di repositori):
  ```
  https://alijayanet.github.io/POS-offline/privacy-policy.html
  ```
- Atau unggah file `privacy-policy.html` ke hosting/Google Sites Anda sendiri.
