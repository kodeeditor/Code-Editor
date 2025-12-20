# ⚡ Quick Start - GitHub Pages Setup

## 3 Langkah Setup (5 Menit)

### ✅ Langkah 1: Enable GitHub Pages

```
1. Buka: https://github.com/Developer-Syntax/Code-Editor/settings/pages
2. Pilih:
   - Source: Deploy from a branch
   - Branch: gh-pages
   - Folder: / (root)
3. Klik: Save
```

Status: GitHub Pages akan aktif dalam 1-2 menit

### ✅ Langkah 2: Build Custom Bootstrap

```
1. Pergi ke: https://github.com/Developer-Syntax/Code-Editor/actions
2. Pilih: "Build Custom Termux Bootstrap (Aggressive Mode)"
3. Klik: "Run workflow"
4. Tunggu selesai (2-6 jam)
```

Hasil: `bootstrap-aarch64-custom.zip` di Releases

### ✅ Langkah 3: Build APT Repository

```
1. Pergi ke: https://github.com/Developer-Syntax/Code-Editor/actions
2. Pilih: "Build Custom APT Repository"
3. Klik: "Run workflow"
4. Tunggu selesai (15-30 menit)
```

Hasil: APT repository live di GitHub Pages

---

## 📊 Status Sekarang

| Item | Status | Action |
|------|--------|--------|
| APK Build | ✅ Success | Done - output/app-debug.apk |
| App Config | ✅ Configured | Points to GitHub Pages APT |
| GitHub Pages | ⚠️ Not Enabled | Go to Settings → Pages |
| Bootstrap Build | ⏳ Not Started | Run Workflow #1 |
| APT Repository | ⏳ Not Started | Run Workflow #2 |

---

## 🎯 URLs (Setelah Setup)

```
GitHub Pages: https://Developer-Syntax.github.io/Code-Editor/
APT Repo:     https://raw.githubusercontent.com/Developer-Syntax/Code-Editor/gh-pages/apt
Bootstrap:    https://github.com/Developer-Syntax/Code-Editor/releases
```

---

## 🧪 Test APT (Setelah Install Bootstrap)

```bash
# Di Terminal Code Editor:
$ apt update
$ apt search termux-bootstrap
$ apt install termux-bootstrap
```

---

## ❌ Jika Ada Error

**GitHub Pages 404?**
- ✓ Cek Settings → Pages → Enabled?
- ✓ Branch: `gh-pages` selected?

**APT Repository 404?**
- ✓ Run "Build Custom APT Repository" workflow
- ✓ Tunggu workflow complete
- ✓ GitHub Pages ada file di `apt/` folder

**Bootstrap tidak download?**
- ✓ Run "Build Custom Termux Bootstrap" workflow
- ✓ Check internet connection
- ✓ Bootstrap release ada di Releases tab

---

## 🚀 Complete Workflow

```
GitHub Pages Setup (5 min)
    ↓
Build Bootstrap (2-6 hours) 
    ↓
Build APT Repository (15-30 min)
    ↓
APT Server Ready! ✅
    ↓
apt/pkg works di app! 🎉
```

---

**NEXT STEP:** Go to Settings → Pages and enable GitHub Pages!
