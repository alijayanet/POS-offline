# Kebijakan Privasi TokoPOS

**Terakhir Diperbarui:** 11 Oktober 2026  
**Pengembang:** ALIJAYA NET  
**Kontak Pengembang:** alijayanet@gmail.com | 081947215703  
**Aplikasi:** TokoPOS (com.alijaya.tokopos.app)

Selamat datang di **TokoPOS**. Kami sangat menghargai privasi Anda dan berkomitmen untuk melindungi data bisnis Anda. Kebijakan Privasi ini menjelaskan bagaimana aplikasi TokoPOS menangani informasi dan data pengguna saat Anda menggunakan aplikasi kami.

---

## 1. Prinsip Utama: 100% Offline & Tanpa Server
TokoPOS adalah aplikasi kasir (*Point of Sale*), manajemen stok, dan pembukuan tagihan yang beroperasi **100% Offline**.
- Seluruh data transaksi, pelanggan, katalog produk, harga, dan laporan keuangan disimpan **secara lokal di penyimpanan perangkat Anda** menggunakan database SQLite Room terenkripsi pada sistem Android.
- Kami **TIDAK** mengoperasikan server penyimpanan eksternal, **TIDAK** mengumpulkan data Anda ke cloud, dan **TIDAK** membagikan atau menjual data Anda kepada pihak ketiga mana pun.

---

## 2. Penggunaan Izin Perangkat (Permissions)

TokoPOS hanya meminta izin yang mutlak diperlukan untuk mengoperasikan fitur-fitur fungsional aplikasi:

### A. Izin Kamera (`android.permission.CAMERA`)
- **Tujuan Penggunaan:** Digunakan secara eksklusif untuk memindai barcode produk bawaan kemasan/pabrik dan scan QRIS secara instan.
- **Pemrosesan Data:** Deteksi barcode diproses langsung di perangkat Anda (*on-device*) menggunakan pustaka Google ML Kit Vision.
- **Jaminan Privasi:** Aplikasi **tidak pernah mengambil foto, tidak merekam video, dan tidak mengunggah gambar apa pun ke internet**.

### B. Izin Bluetooth (`android.permission.BLUETOOTH_CONNECT`, `BLUETOOTH_SCAN`)
- **Tujuan Penggunaan:** Digunakan untuk menghubungkan smartphone ke printer thermal Bluetooth (ESC/POS) 58mm/80mm guna mencetak struk penjualan, nota kuitansi manual, dan label barcode.
- **Penegasan Tanpa Lokasi (`neverForLocation`):** Pada Android 12 ke atas, izin Bluetooth kami konfigurasikan dengan atribut `neverForLocation`. Kami **tidak pernah melacak, mengambil, atau menggunakan lokasi fisik GPS perangkat Anda**.
- Izin lokasi lama (`ACCESS_FINE_LOCATION`) dibatasi hanya untuk Android versi 11 ke bawah (`maxSdkVersion="30"`) sesuai standar sistem operasi Android lama untuk pencarian perangkat Bluetooth terdekat.

### C. Izin Internet (`android.permission.INTERNET`)
- Digunakan untuk intent standar sistem operasi (misalnya membuka tautan WhatsApp saat pengguna memilih fitur *Kirim Struk via WhatsApp*).
- Aplikasi tidak menggunakan internet untuk mengirimkan data transaksi ataupun data pribadi Anda ke server pengembang.

---

## 3. Penyimpanan dan Berbagi File (Export / Backup)
- Aplikasi menyediakan fitur **Backup Database JSON** dan **Ekspor Laporan Excel/CSV**.
- File-file tersebut hanya dibuat ketika Anda secara sadar menekan tombol "Backup" atau "Ekspor".
- File dibagikan melalui protokol resmi **Android FileProvider** yang aman dan terisolasi. Anda memiliki hak penuh untuk memilih ke mana file tersebut disimpan atau dibagikan (misalnya ke Google Drive pribadi Anda, WhatsApp, atau penyimpanan lokal).

---

## 4. Layanan Pihak Ketiga (Third-Party Services)
Aplikasi kami mengintegrasikan pustaka pihak ketiga berikut yang berjalan secara lokal pada perangkat:
1. **Google ML Kit Barcode Scanning**: Digunakan untuk deteksi barcode secara lokal di perangkat tanpa koneksi internet.
2. **ZXing Android**: Digunakan untuk membuat gambar QR Code dan Barcode 1D secara lokal untuk dicetak ke printer.

Aplikasi kami **TIDAK** menyertakan jaringan iklan (*Ad Networks*), pelacak analitik (*Analytics Trackers*), ataupun pustaka pengumpul profil pengguna.

---

## 5. Hak Pengguna & Penghapusan Data
Karena seluruh data disimpan secara lokal pada perangkat Anda:
- Anda memiliki kendali 100% atas data Anda setiap saat.
- Anda dapat menghapus data tertentu langsung di dalam aplikasi (hapus produk, reset data toko).
- Anda dapat menghapus seluruh data secara permanen kapan saja dengan cara:
  1. Melakukan **Hapus Data (Clear Data)** melalui menu *Pengaturan HP > Aplikasi > TokoPOS > Penyimpanan*, atau
  2. Melakukan **Copot Pemasangan (Uninstall)** aplikasi dari perangkat Anda. Setelah dicopot, seluruh data lokal akan terhapus otomatis oleh sistem Android.

---

## 6. Privasi Anak-Anak
Aplikasi TokoPOS ditujukan untuk pelaku usaha dan operasional toko. Aplikasi ini tidak secara sengaja mengumpulkan data apa pun dari anak-anak di bawah umur 13 tahun.

---

## 7. Perubahan Kebijakan Privasi
Kami dapat memperbarui Kebijakan Privasi ini dari waktu ke waktu jika terdapat pembaruan fitur atau regulasi. Pembaruan akan selalu dipublikasikan di halaman ini dan dapat diakses langsung melalui menu Pengaturan di dalam aplikasi TokoPOS.

---

## 8. Kontak Kami
Jika Anda memiliki pertanyaan atau masukan mengenai Kebijakan Privasi ini, silakan hubungi kami:
- **Pengembang:** ALIJAYA NET
- **Email:** alijayanet@gmail.com
- **Nomor Telepon / WhatsApp:** +62 819-4721-5703 (081947215703)
- **Repositori GitHub:** [https://github.com/alijayanet/POS-offline](https://github.com/alijayanet/POS-offline)
