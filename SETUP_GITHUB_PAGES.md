# Setup GitHub Pages untuk APT Repository

Panduan lengkap untuk mengaktifkan GitHub Pages dan menggunakan custom APT repository.

## 1. Setup GitHub Pages Otomatis

Workflow `enable-github-pages.yml` sudah otomatis membuat branch `gh-pages`. Untuk memastikan GitHub Pages aktif:

### Langkah Manual (Opsional):

1. Buka repository di GitHub: `https://github.com/Developer-Syntax/Code-Editor`
2. Pergi ke **Settings** → **Pages**
3. Di bagian "Source", pilih:
   - **Branch:** `gh-pages`
   - **Folder:** `/ (root)`
4. Klik **Save**

## 2. Trigger Workflow untuk Deploy APT Repository

### Option A: Automatic Deploy (Recommended)

APT repository akan otomatis di-deploy setiap bulan (1st of month) atau manual trigger.

### Option B: Manual Trigger

1. Pergi ke **Actions** di repository
2. Pilih workflow: **Build Custom APT Repository**
3. Klik **Run workflow**
4. Tunggu sampai selesai (15-30 menit)

## 3. Verifikasi GitHub Pages Status

Setelah workflow selesai, cek status:

```bash
# GitHub Pages URL akan ada di:
# https://Developer-Syntax.github.io/Code-Editor/apt/

# Test APT repository accessibility:
curl -I https://raw.githubusercontent.com/Developer-Syntax/Code-Editor/gh-pages/apt/dists/stable/Release
```

Expected response: `HTTP/1.1 200 OK`

## 4. URL Konfigurasi APT

### APT Repository URL:
```
https://raw.githubusercontent.com/Developer-Syntax/Code-Editor/gh-pages/apt
```

### Di-set otomatis di app sebagai:
```java
deb [trusted=yes] https://raw.githubusercontent.com/Developer-Syntax/Code-Editor/gh-pages/apt stable main
```

## 5. Struktur Directory GitHub Pages

```
gh-pages branch/
├── apt/
│   ├── dists/
│   │   └── stable/
│   │       ├── Release
│   │       ├── InRelease
│   │       └── main/
│   │           └── binary-aarch64/
│   │               ├── Packages
│   │               └── Packages.gz
│   ├── pool/
│   │   └── main/
│   │       └── [packages di sini]
│   └── metadata/
└── index.html
```

## 6. Workflows yang Sudah Setup

### A. `build-termux-bootstrap-brutal.yml`
- Builds custom Termux bootstrap
- Replaces `com.termux` → `com.codeeditor.android`
- Upload ke GitHub Releases

**Trigger:** Manual via Actions

```
Actions → Build Custom Termux Bootstrap → Run workflow
```

### B. `build-apt-repository.yml`
- Builds custom APT repository
- Deploy ke GitHub Pages
- Create package indexes

**Trigger:** Manual or Monthly (1st of month)

```
Actions → Build Custom APT Repository → Run workflow
```

### C. `enable-github-pages.yml`
- Setup gh-pages branch
- Configure untuk hosting

**Auto-triggered** saat push ke main

## 7. Testing APT Repository

Setelah bootstrap terinstall di app, test:

```bash
# Di terminal Code Editor:
$ apt update
$ apt search termux-bootstrap
$ apt install termux-bootstrap
```

## 8. Troubleshooting

### GitHub Pages tidak aktif?
```
Settings → Pages → Source: gh-pages / root → Save
```

### APT repository 404?
```bash
# Cek apakah gh-pages punya file:
curl https://raw.githubusercontent.com/Developer-Syntax/Code-Editor/gh-pages/apt/dists/stable/Release -v
```

### Bootstrap not downloading?
- Cek internet connection
- Verify Release di GitHub ada
- Check TermuxBootstrap.java URL benar

## 9. URL Reference

| Item | URL |
|------|-----|
| Repository | https://github.com/Developer-Syntax/Code-Editor |
| GitHub Pages | https://Developer-Syntax.github.io/Code-Editor/ |
| APT Repo | https://raw.githubusercontent.com/Developer-Syntax/Code-Editor/gh-pages/apt |
| Bootstrap Releases | https://github.com/Developer-Syntax/Code-Editor/releases |

## 10. Struktur Build Pipeline

```
Push Code
    ↓
enable-github-pages.yml (auto)
    ↓ Creates/updates gh-pages branch
    ↓
Manual: Build Custom Termux Bootstrap
    ↓ Outputs: bootstrap-aarch64-custom.zip
    ↓ Uploads to Releases
    ↓
Manual: Build Custom APT Repository
    ↓ Outputs: APT repository structure
    ↓ Deploys to GitHub Pages
    ↓
App downloads from GitHub Pages
    ↓
apt/pkg system works!
```

---

**Jangan lupa:** Run workflows minimal sekali untuk initialize structure!
