# Panduan Membuat APT Repository Termux Sendiri

## Gambaran Umum

Dengan custom package name (`com.codeeditor.android`), Anda **tidak bisa** menggunakan repository resmi Termux. Solusinya adalah membuat repository APT sendiri dengan packages yang di-compile untuk prefix Anda.

## Arsitektur

```
┌─────────────────────────────────────────────────────────────┐
│                    GitHub Repository                         │
│                                                              │
│  ┌──────────────────┐    ┌──────────────────┐               │
│  │ termux-packages  │───>│   .deb files     │               │
│  │ (build dengan    │    │  (output/)       │               │
│  │  custom prefix)  │    └────────┬─────────┘               │
│  └──────────────────┘             │                         │
│                                   ▼                         │
│                      ┌──────────────────┐                   │
│                      │ termux-apt-repo  │                   │
│                      │ (generate repo)  │                   │
│                      └────────┬─────────┘                   │
│                               │                             │
│                               ▼                             │
│                      ┌──────────────────┐                   │
│                      │  GitHub Pages    │                   │
│                      │  (host repo)     │                   │
│                      └────────┬─────────┘                   │
└───────────────────────────────┼─────────────────────────────┘
                                │
                                ▼
                ┌───────────────────────────────┐
                │   CodeEditor Android App      │
                │   apt update && apt install   │
                └───────────────────────────────┘
```

## Langkah-langkah

### 1. Setup GitHub Pages

1. Buka repository GitHub Anda
2. Pergi ke **Settings** > **Pages**
3. Source: **Deploy from a branch**
4. Branch: **gh-pages** (akan dibuat otomatis oleh workflow)
5. Save

### 2. Jalankan Workflow

1. Buka **Actions** > **Build Termux APT Repository**
2. Klik **Run workflow**
3. Pilih options:
   - **architectures**: `aarch64` (untuk HP Android modern)
   - **packages**: `essential` atau daftar custom (contoh: `python,nodejs,git,vim`)
4. Klik **Run workflow**

### 3. Packages Essential (Default)

Jika pilih `essential`, packages berikut akan di-build:
- bash, coreutils, findutils, grep, sed, gawk
- tar, gzip, xz-utils, libbz2
- curl, wget, git, openssh
- python, nodejs
- vim, nano, less, file, which, tree

### 4. Tunggu Build Selesai

- Build bisa memakan waktu **4-12 jam** tergantung jumlah packages
- Setiap package harus di-compile dari source
- Setelah selesai, repository akan di-deploy ke GitHub Pages

### 5. URL Repository Anda

Setelah deploy, repository tersedia di:
```
https://YOUR_USERNAME.github.io/YOUR_REPO/
```

Contoh:
```
https://developer-syntax.github.io/Code-Editor/
```

### 6. Konfigurasi di CodeEditor

Update `TermuxBootstrap.java` untuk menggunakan repository:

```java
// Di writeConfigs(), tambahkan:
File sourcesListDir = new File(etcDir, "apt/sources.list.d");
sourcesListDir.mkdirs();

File customSource = new File(sourcesListDir, "codeeditor.list");
StringBuilder sourceContent = new StringBuilder();
sourceContent.append("deb [trusted=yes] https://YOUR_USERNAME.github.io/YOUR_REPO stable main\n");
writeFile(customSource, sourceContent.toString());
```

### 7. Test Repository

Di terminal CodeEditor:
```bash
apt update
apt search .
apt install python
```

## Menambah Packages Baru

### Via Workflow Dispatch

1. Jalankan workflow lagi dengan packages baru di input
2. Repository akan di-update dengan packages tambahan

### Packages Populer untuk Development

```
python,nodejs,ruby,perl,lua,php,clang,cmake,make,git,vim,nano,tmux,htop,curl,wget,openssh,rsync
```

### Packages untuk Web Development

```
nodejs,python,ruby,nginx,sqlite,postgresql,redis,git,vim
```

## Estimasi Waktu Build

| Packages | Estimasi Waktu |
|----------|----------------|
| 10 packages | 2-4 jam |
| 20 packages | 4-8 jam |
| 50 packages | 8-16 jam |
| All packages | 24-48 jam |

## Estimasi Ukuran Repository

| Packages | Ukuran |
|----------|--------|
| Essential (20) | ~100-200 MB |
| Full dev setup | ~500 MB - 1 GB |

## Troubleshooting

### Build Gagal untuk Package Tertentu

- Beberapa packages mungkin gagal build
- Workflow akan lanjut ke package berikutnya
- Cek build logs untuk detail error

### Repository Tidak Update

- Pastikan GitHub Pages enabled
- Cek branch `gh-pages` ada
- Tunggu beberapa menit untuk propagation

### apt update Error

1. Cek URL repository benar
2. Pastikan `[trusted=yes]` ada di sources.list
3. Cek koneksi internet

## Limitasi GitHub Pages

- **Ukuran maksimal**: 1 GB
- **Bandwidth**: 100 GB/bulan
- **File size**: 100 MB per file

Untuk repository besar, pertimbangkan:
- Self-hosted server
- AWS S3 + CloudFront
- DigitalOcean Spaces

## Referensi

- [termux-apt-repo](https://github.com/termux/termux-apt-repo)
- [termux-packages](https://github.com/termux/termux-packages)
- [Building packages](https://github.com/termux/termux-packages/wiki/Building-packages)
