# APT Server Setup Guide

## Current Configuration ✅

APK sudah dikonfigurasi untuk menggunakan APT repository dari GitHub Pages.

### URLs yang digunakan:

**Primary (Custom):**
```
https://raw.githubusercontent.com/Developer-Syntax/Code-Editor/gh-pages/apt
```

**Fallback (Original Termux):**
```
https://github.com/termux/termux-packages/releases/download/bootstrap-2025.12.14-r1+apt.android-7/bootstrap-aarch64.zip
```

---

## ⚙️ Setup GitHub Pages - 3 Langkah Mudah

### Langkah 1: Buka Settings Repository

1. Pergi ke: https://github.com/Developer-Syntax/Code-Editor
2. Klik tab **Settings**
3. Scroll ke bawah, cari bagian **Pages**

### Langkah 2: Aktifkan GitHub Pages

Di section **Pages**:

```
Source
├─ Deploy from a branch
   ├─ Branch: gh-pages
   └─ Folder: / (root)
```

Pilih seperti di atas, lalu klik **Save**.

### Langkah 3: Tunggu Deploy Selesai

- GitHub Pages akan aktif di: `https://Developer-Syntax.github.io/Code-Editor/`
- APT repository ada di: `https://Developer-Syntax.github.io/Code-Editor/apt/`

---

## 🚀 Build & Deploy Workflows

### Workflow 1: Build Custom Bootstrap

```
Actions 
  → "Build Custom Termux Bootstrap (Aggressive Mode)"
    → Click "Run workflow"
    → Select architecture: aarch64 (default)
    → Click "Run workflow"
```

**Hasil:** 
- ✅ bootstrap-aarch64-custom.zip di Releases
- ✅ APK bisa download dari Releases

**Waktu:** 2-6 jam

---

### Workflow 2: Build APT Repository

```
Actions
  → "Build Custom APT Repository"
    → Click "Run workflow"
    → Select build mode: repackage (default)
    → Click "Run workflow"
```

**Hasil:**
- ✅ APT repository structure di gh-pages branch
- ✅ GitHub Pages auto-deploy
- ✅ `apt update` dan `apt install` bisa digunakan

**Waktu:** 15-30 menit

---

## 📋 Testing APT Repository

Setelah APK terinstall dan bootstrap sudah ter-download, test di terminal:

```bash
# Login ke Terminal di Code Editor
$ apt update

# Cari package
$ apt search termux-bootstrap

# Install package
$ apt install termux-bootstrap
```

---

## 🔍 Verifikasi Konfigurasi

### Check APT URL di App

File: `app/src/main/java/com/codeeditor/android/utils/TermuxBootstrap.java`

**Primary URL (Line 39):**
```java
https://github.com/Developer-Syntax/Code-Editor/releases/download/bootstrap-custom-com.codeeditor.android-latest/bootstrap-aarch64-custom.zip
```

**Fallback URL (Line 43):**
```java
https://github.com/termux/termux-packages/releases/download/bootstrap-2025.12.14-r1%2Bapt.android-7/bootstrap-aarch64.zip
```

**APT Sources (Line 705):**
```java
deb [trusted=yes] https://raw.githubusercontent.com/Developer-Syntax/Code-Editor/gh-pages/apt stable main
```

### ✅ Semua sudah benar!

---

## 📊 Status Checklist

- [x] APK dikonfigurasi dengan URL APT Server GitHub Pages
- [x] Bootstrap download URL sudah di-set
- [x] APT sources.list sudah di-setup otomatis
- [x] GitHub Pages workflow siap di-trigger
- [x] APT repository workflow siap di-trigger

---

## 🔗 Quick Links

| Item | URL |
|------|-----|
| Repository | https://github.com/Developer-Syntax/Code-Editor |
| Settings → Pages | https://github.com/Developer-Syntax/Code-Editor/settings/pages |
| Actions | https://github.com/Developer-Syntax/Code-Editor/actions |
| Releases | https://github.com/Developer-Syntax/Code-Editor/releases |
| GitHub Pages | https://Developer-Syntax.github.io/Code-Editor/ |
| APT Repo Raw | https://raw.githubusercontent.com/Developer-Syntax/Code-Editor/gh-pages/apt |

---

## ❓ Jika Ada Masalah

### APT Repository tidak muncul di GitHub Pages?
```bash
# Cek apakah file ada:
curl https://raw.githubusercontent.com/Developer-Syntax/Code-Editor/gh-pages/apt/dists/stable/Release -v

# Expected: HTTP 200 OK
```

### gh-pages branch tidak ada?
- Workflow `enable-github-pages.yml` akan otomatis membuat
- Atau manual create di Settings → Pages

### Bootstrap tidak download?
- Check internet connection di device
- Verify Release ada di: https://github.com/Developer-Syntax/Code-Editor/releases

---

**APK sudah siap! GitHub Pages tinggal di-setup dan workflows tinggal di-trigger.** 🎉
