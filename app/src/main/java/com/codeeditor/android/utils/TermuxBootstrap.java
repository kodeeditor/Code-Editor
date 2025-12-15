package com.codeeditor.android.utils;

import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.system.Os;
import android.system.ErrnoException;
import android.util.Log;
import android.util.Pair;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class TermuxBootstrap {
    private static final String TAG = "TermuxBootstrap";
    
    private static final String BOOTSTRAP_URL = 
        "https://github.com/termux/termux-packages/releases/download/bootstrap-2025.12.14-r1%2Bapt.android-7/bootstrap-aarch64.zip";
    
    private static final String BOOTSTRAP_VERSION = "2025.12.14-r1";
    
    private final Context context;
    private final File filesDir;
    private final File prefixDir;
    private final File stagingPrefixDir;
    private final File homeDir;
    private final File tmpDir;
    private BootstrapListener listener;
    
    public interface BootstrapListener {
        void onProgress(String message, int progress);
        void onSuccess();
        void onError(String error);
    }
    
    public TermuxBootstrap(Context context) {
        this.context = context;
        this.filesDir = context.getFilesDir();
        this.prefixDir = new File(filesDir, "usr");
        this.stagingPrefixDir = new File(filesDir, "usr-staging");
        this.homeDir = new File(filesDir, "home");
        this.tmpDir = context.getCacheDir();
    }
    
    public void setListener(BootstrapListener listener) {
        this.listener = listener;
    }
    
    public boolean isBootstrapInstalled() {
        File bashFile = new File(prefixDir, "bin/bash");
        File pkgFile = new File(prefixDir, "bin/pkg");
        File aptFile = new File(prefixDir, "bin/apt");
        File versionFile = new File(prefixDir, ".bootstrap_version");
        
        boolean bashExists = bashFile.exists() && bashFile.canExecute();
        boolean pkgExists = pkgFile.exists();
        boolean aptExists = aptFile.exists();
        
        if (bashExists && (pkgExists || aptExists)) {
            if (versionFile.exists()) {
                try {
                    String installedVersion = readFile(versionFile);
                    return BOOTSTRAP_VERSION.equals(installedVersion.trim());
                } catch (Exception e) {
                    return true;
                }
            }
            return true;
        }
        return false;
    }
    
    public String getPrefixPath() {
        return prefixDir.getAbsolutePath();
    }
    
    public String getHomePath() {
        return homeDir.getAbsolutePath();
    }
    
    public String getBashPath() {
        return new File(prefixDir, "bin/bash").getAbsolutePath();
    }
    
    public void installBootstrap() {
        new Thread(() -> {
            try {
                notifyProgress("Mempersiapkan instalasi...", 0);
                
                deleteDirectoryRecursive(stagingPrefixDir);
                deleteDirectoryRecursive(prefixDir);
                
                ensureDirectoryExists(stagingPrefixDir);
                ensurePrivateDirectoryExists(homeDir);
                
                notifyProgress("Mengunduh Termux bootstrap...", 5);
                byte[] zipBytes = downloadBootstrap(BOOTSTRAP_URL);
                
                notifyProgress("Mengekstrak bootstrap...", 40);
                extractBootstrap(zipBytes);
                
                notifyProgress("Memindahkan ke direktori prefix...", 85);
                deleteDirectoryRecursive(prefixDir);
                if (!stagingPrefixDir.renameTo(prefixDir)) {
                    throw new RuntimeException("Gagal memindahkan staging ke prefix");
                }
                
                notifyProgress("Mengatur permission file...", 87);
                fixExecutablePermissions();
                
                notifyProgress("Menulis konfigurasi...", 90);
                writeConfigs();
                
                notifyProgress("Verifikasi instalasi...", 95);
                verifyInstallation();
                
                File versionFile = new File(prefixDir, ".bootstrap_version");
                writeFile(versionFile, BOOTSTRAP_VERSION);
                
                notifyProgress("Selesai!", 100);
                notifySuccess();
                
            } catch (Exception e) {
                Log.e(TAG, "Error installing bootstrap", e);
                notifyError("Gagal menginstal bootstrap: " + e.getMessage());
            }
        }).start();
    }
    
    private void ensureDirectoryExists(File dir) {
        if (dir == null) return;
        
        if (!dir.exists()) {
            dir.mkdirs();
        }
        
        if (dir.isDirectory()) {
            try {
                Os.chmod(dir.getAbsolutePath(), 0755);
            } catch (ErrnoException e) {
                dir.setReadable(true, false);
                dir.setWritable(true, true);
                dir.setExecutable(true, false);
            }
        }
    }
    
    private void ensurePrivateDirectoryExists(File dir) {
        if (dir == null) return;
        
        if (!dir.exists()) {
            dir.mkdirs();
        }
        
        if (dir.isDirectory()) {
            try {
                Os.chmod(dir.getAbsolutePath(), 0700);
            } catch (ErrnoException e) {
                dir.setReadable(true, true);
                dir.setWritable(true, true);
                dir.setExecutable(true, true);
            }
        }
    }
    
    private byte[] downloadBootstrap(String urlString) throws IOException {
        HttpURLConnection connection = null;
        InputStream input = null;
        ByteArrayOutputStream output = null;
        
        try {
            URL url = new URL(urlString);
            connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(30000);
            connection.setReadTimeout(120000);
            connection.setInstanceFollowRedirects(true);
            connection.setRequestProperty("User-Agent", "CodeEditor-Android/1.0");
            connection.connect();
            
            int responseCode = connection.getResponseCode();
            
            while (responseCode == HttpURLConnection.HTTP_MOVED_PERM || 
                   responseCode == HttpURLConnection.HTTP_MOVED_TEMP ||
                   responseCode == 307 || responseCode == 308) {
                String newUrl = connection.getHeaderField("Location");
                connection.disconnect();
                url = new URL(newUrl);
                connection = (HttpURLConnection) url.openConnection();
                connection.setConnectTimeout(30000);
                connection.setReadTimeout(120000);
                connection.setRequestProperty("User-Agent", "CodeEditor-Android/1.0");
                connection.connect();
                responseCode = connection.getResponseCode();
            }
            
            if (responseCode != HttpURLConnection.HTTP_OK) {
                throw new IOException("Server returned HTTP " + responseCode);
            }
            
            int fileLength = connection.getContentLength();
            input = new BufferedInputStream(connection.getInputStream());
            output = new ByteArrayOutputStream();
            
            byte[] buffer = new byte[8192];
            long total = 0;
            int count;
            int lastProgress = 5;
            
            while ((count = input.read(buffer)) != -1) {
                total += count;
                output.write(buffer, 0, count);
                
                if (fileLength > 0) {
                    int progress = 5 + (int) ((total * 35) / fileLength);
                    if (progress > lastProgress) {
                        lastProgress = progress;
                        notifyProgress("Mengunduh... " + (total / 1024) + " KB / " + (fileLength / 1024) + " KB", progress);
                    }
                }
            }
            
            return output.toByteArray();
            
        } finally {
            if (output != null) try { output.close(); } catch (Exception e) {}
            if (input != null) try { input.close(); } catch (Exception e) {}
            if (connection != null) connection.disconnect();
        }
    }
    
    private void extractBootstrap(byte[] zipBytes) throws IOException {
        final byte[] buffer = new byte[8192];
        final List<Pair<String, String>> symlinks = new ArrayList<>(50);
        final String stagingPath = stagingPrefixDir.getAbsolutePath();
        
        try (ZipInputStream zipInput = new ZipInputStream(new ByteArrayInputStream(zipBytes))) {
            ZipEntry zipEntry;
            int entryCount = 0;
            
            while ((zipEntry = zipInput.getNextEntry()) != null) {
                String entryName = zipEntry.getName();
                
                if (entryName.equals("SYMLINKS.txt")) {
                    BufferedReader symlinksReader = new BufferedReader(new InputStreamReader(zipInput, "UTF-8"));
                    String line;
                    while ((line = symlinksReader.readLine()) != null) {
                        line = line.trim();
                        if (line.isEmpty()) continue;
                        
                        String[] parts = line.split("\u2190");
                        if (parts.length != 2) {
                            Log.w(TAG, "Malformed symlink line: " + line);
                            continue;
                        }
                        
                        String oldPath = parts[0].trim();
                        String newPath = parts[1].trim();
                        
                        if (newPath.startsWith("./")) {
                            newPath = newPath.substring(2);
                        }
                        
                        String fullNewPath = stagingPath + "/" + newPath;
                        symlinks.add(Pair.create(oldPath, fullNewPath));
                        
                        File parentDir = new File(fullNewPath).getParentFile();
                        ensureDirectoryExists(parentDir);
                    }
                } else {
                    File targetFile = new File(stagingPrefixDir, entryName);
                    boolean isDirectory = zipEntry.isDirectory();
                    
                    File parentDir = isDirectory ? targetFile : targetFile.getParentFile();
                    ensureDirectoryExists(parentDir);
                    
                    if (isDirectory) {
                        ensureDirectoryExists(targetFile);
                    } else {
                        try (FileOutputStream outStream = new FileOutputStream(targetFile)) {
                            int readBytes;
                            while ((readBytes = zipInput.read(buffer)) != -1) {
                                outStream.write(buffer, 0, readBytes);
                            }
                        }
                        
                        if (isExecutable(entryName)) {
                            setFileMode(targetFile, 0700);
                        } else {
                            setFileMode(targetFile, 0600);
                        }
                    }
                }
                
                entryCount++;
                if (entryCount % 200 == 0) {
                    int progress = 40 + Math.min(40, entryCount / 50);
                    notifyProgress("Mengekstrak: " + entryCount + " file", progress);
                }
                
                zipInput.closeEntry();
            }
        }
        
        if (symlinks.isEmpty()) {
            throw new RuntimeException("SYMLINKS.txt tidak ditemukan atau kosong");
        }
        
        notifyProgress("Membuat symlinks (" + symlinks.size() + ")...", 80);
        
        int symlinkCount = 0;
        int symlinkErrors = 0;
        for (Pair<String, String> symlink : symlinks) {
            try {
                File linkFile = new File(symlink.second);
                if (linkFile.exists()) {
                    linkFile.delete();
                }
                
                Os.symlink(symlink.first, symlink.second);
                symlinkCount++;
                
                if (symlinkCount % 100 == 0) {
                    notifyProgress("Membuat symlinks: " + symlinkCount + "/" + symlinks.size(), 80 + (symlinkCount * 5 / symlinks.size()));
                }
            } catch (ErrnoException e) {
                symlinkErrors++;
                Log.w(TAG, "Failed to create symlink: " + symlink.first + " -> " + symlink.second + ": " + e.getMessage());
            }
        }
        
        Log.i(TAG, "Created " + symlinkCount + " symlinks, " + symlinkErrors + " errors, out of " + symlinks.size() + " total");
    }
    
    private boolean isExecutable(String entryName) {
        return entryName.startsWith("bin/") ||
               entryName.startsWith("libexec/") ||
               entryName.startsWith("lib/apt/apt-helper") ||
               entryName.startsWith("lib/apt/methods/");
    }
    
    private void setFileMode(File file, int mode) {
        try {
            Os.chmod(file.getAbsolutePath(), mode);
        } catch (ErrnoException e) {
            boolean ownerOnly = (mode & 0077) == 0;
            file.setReadable(true, ownerOnly);
            file.setWritable(true, true);
            if ((mode & 0100) != 0) {
                file.setExecutable(true, ownerOnly);
            }
        }
    }
    
    private void fixExecutablePermissions() {
        String[] executableDirs = {"bin", "libexec", "lib/apt/methods"};
        
        for (String dirPath : executableDirs) {
            File dir = new File(prefixDir, dirPath);
            if (dir.exists() && dir.isDirectory()) {
                setExecutableRecursive(dir);
            }
        }
        
        File aptHelper = new File(prefixDir, "lib/apt/apt-helper");
        if (aptHelper.exists()) {
            setFileMode(aptHelper, 0755);
        }
        
        String[] criticalBinaries = {"bash", "sh", "pkg", "apt", "dpkg", "apt-get", "apt-cache"};
        for (String binary : criticalBinaries) {
            File binFile = new File(prefixDir, "bin/" + binary);
            if (binFile.exists()) {
                setFileMode(binFile, 0755);
                
                if (!binFile.canExecute()) {
                    try {
                        String target = Os.readlink(binFile.getAbsolutePath());
                        if (target != null) {
                            File targetFile;
                            if (target.startsWith("/")) {
                                targetFile = new File(target);
                            } else {
                                targetFile = new File(binFile.getParentFile(), target);
                            }
                            if (targetFile.exists()) {
                                setFileMode(targetFile, 0755);
                            }
                        }
                    } catch (ErrnoException e) {
                        Log.w(TAG, "Not a symlink or error reading: " + binary);
                    }
                }
            }
        }
        
        Log.i(TAG, "Fixed executable permissions");
    }
    
    private void setExecutableRecursive(File dir) {
        if (dir == null || !dir.exists()) return;
        
        File[] files = dir.listFiles();
        if (files == null) return;
        
        for (File file : files) {
            if (file.isDirectory()) {
                setExecutableRecursive(file);
            } else if (file.isFile()) {
                setFileMode(file, 0755);
            }
        }
    }
    
    private void verifyInstallation() throws Exception {
        String[] criticalFiles = {
            "bin/bash",
            "bin/sh",
            "bin/pkg",
            "bin/apt",
            "bin/dpkg"
        };
        
        List<String> missingFiles = new ArrayList<>();
        List<String> nonExecutableFiles = new ArrayList<>();
        
        for (String path : criticalFiles) {
            File file = new File(prefixDir, path);
            if (!file.exists()) {
                try {
                    String target = Os.readlink(file.getAbsolutePath());
                    if (target != null) {
                        continue;
                    }
                } catch (ErrnoException e) {
                    missingFiles.add(path);
                }
            } else if (!file.canExecute()) {
                setFileMode(file, 0755);
                if (!file.canExecute()) {
                    nonExecutableFiles.add(path);
                }
            }
        }
        
        if (!missingFiles.isEmpty()) {
            Log.w(TAG, "Missing critical files: " + missingFiles);
        }
        
        if (!nonExecutableFiles.isEmpty()) {
            Log.w(TAG, "Non-executable critical files: " + nonExecutableFiles);
        }
    }
    
    private void writeConfigs() {
        try {
            File bashrc = new File(homeDir, ".bashrc");
            if (!bashrc.exists()) {
                StringBuilder bashrcContent = new StringBuilder();
                bashrcContent.append("# CodeEditor Terminal - Termux Environment\n\n");
                bashrcContent.append("export PREFIX=\"").append(prefixDir.getAbsolutePath()).append("\"\n");
                bashrcContent.append("export HOME=\"").append(homeDir.getAbsolutePath()).append("\"\n");
                bashrcContent.append("export TMPDIR=\"").append(tmpDir.getAbsolutePath()).append("\"\n");
                bashrcContent.append("export LD_LIBRARY_PATH=\"$PREFIX/lib\"\n");
                bashrcContent.append("export PATH=\"$PREFIX/bin:$PATH\"\n");
                bashrcContent.append("export LANG=en_US.UTF-8\n");
                bashrcContent.append("export TERM=xterm-256color\n\n");
                bashrcContent.append("# Aliases\n");
                bashrcContent.append("alias ll='ls -la'\n");
                bashrcContent.append("alias la='ls -A'\n");
                bashrcContent.append("alias l='ls -CF'\n\n");
                bashrcContent.append("# PS1 Prompt\n");
                bashrcContent.append("PS1='\\[\\033[01;32m\\]termux\\[\\033[00m\\]:\\[\\033[01;34m\\]\\w\\[\\033[00m\\]\\$ '\n");
                
                writeFile(bashrc, bashrcContent.toString());
            }
            
            File profile = new File(homeDir, ".profile");
            if (!profile.exists()) {
                StringBuilder profileContent = new StringBuilder();
                profileContent.append("# Profile for CodeEditor Terminal\n");
                profileContent.append("if [ -f ~/.bashrc ]; then\n");
                profileContent.append("    . ~/.bashrc\n");
                profileContent.append("fi\n");
                
                writeFile(profile, profileContent.toString());
            }
            
            File etcDir = new File(prefixDir, "etc");
            if (!etcDir.exists()) {
                etcDir.mkdirs();
            }
            
            File motd = new File(etcDir, "motd");
            if (!motd.exists()) {
                StringBuilder motdContent = new StringBuilder();
                motdContent.append("Welcome to CodeEditor Terminal!\n");
                motdContent.append("Package manager: pkg install <package>\n");
                motdContent.append("Example: pkg install python nodejs git\n");
                
                writeFile(motd, motdContent.toString());
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Error writing configs", e);
        }
    }
    
    private void deleteDirectoryRecursive(File dir) {
        if (dir == null || !dir.exists()) return;
        
        File[] files = dir.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    deleteDirectoryRecursive(file);
                } else {
                    file.delete();
                }
            }
        }
        dir.delete();
    }
    
    private String readFile(File file) throws IOException {
        FileInputStream fis = new FileInputStream(file);
        byte[] data = new byte[(int) file.length()];
        fis.read(data);
        fis.close();
        return new String(data, "UTF-8");
    }
    
    private void writeFile(File file, String content) throws IOException {
        FileOutputStream fos = new FileOutputStream(file);
        fos.write(content.getBytes("UTF-8"));
        fos.close();
    }
    
    public String[] buildTermuxEnvironment() {
        java.util.List<String> env = new java.util.ArrayList<>();
        
        String prefixPath = prefixDir.getAbsolutePath();
        String homePath = homeDir.getAbsolutePath();
        String tmpPath = tmpDir.getAbsolutePath();
        
        env.add("PREFIX=" + prefixPath);
        env.add("HOME=" + homePath);
        env.add("TMPDIR=" + tmpPath);
        env.add("PATH=" + prefixPath + "/bin:" + prefixPath + "/bin/applets:/system/bin:/system/xbin");
        env.add("LD_LIBRARY_PATH=" + prefixPath + "/lib");
        env.add("LANG=en_US.UTF-8");
        env.add("TERM=xterm-256color");
        env.add("COLORTERM=truecolor");
        env.add("SHELL=" + prefixPath + "/bin/bash");
        
        int uid = android.os.Process.myUid();
        String user = "u0_a" + (uid % 100000);
        env.add("USER=" + user);
        env.add("LOGNAME=" + user);
        env.add("HOSTNAME=localhost");
        
        File externalStorage = android.os.Environment.getExternalStorageDirectory();
        if (externalStorage != null) {
            env.add("EXTERNAL_STORAGE=" + externalStorage.getAbsolutePath());
        }
        
        env.add("ANDROID_DATA=/data");
        env.add("ANDROID_ROOT=/system");
        
        env.add("TERMUX_VERSION=0.118");
        env.add("TERMUX_APK_RELEASE=GITHUB");
        env.add("TERMUX_APP_PACKAGE_NAME=" + context.getPackageName());
        env.add("TERMUX_PREFIX=" + prefixPath);
        
        return env.toArray(new String[0]);
    }
    
    private void notifyProgress(String message, int progress) {
        if (listener != null) {
            new Handler(Looper.getMainLooper()).post(() -> 
                listener.onProgress(message, progress));
        }
    }
    
    private void notifySuccess() {
        if (listener != null) {
            new Handler(Looper.getMainLooper()).post(() -> 
                listener.onSuccess());
        }
    }
    
    private void notifyError(String error) {
        if (listener != null) {
            new Handler(Looper.getMainLooper()).post(() -> 
                listener.onError(error));
        }
    }
}
