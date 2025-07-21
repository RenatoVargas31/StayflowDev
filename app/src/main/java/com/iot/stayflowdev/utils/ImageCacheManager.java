package com.iot.stayflowdev.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.util.LruCache;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Sistema de caché de imágenes local optimizado
 * Combina caché en memoria (LRU) con caché en disco
 */
public class ImageCacheManager {
    private static final String TAG = "ImageCacheManager";
    private static final String CACHE_DIR_NAME = "stayflow_images";
    private static final int MAX_MEMORY_CACHE_SIZE = 20 * 1024 * 1024; // 20MB en memoria
    private static final int MAX_DISK_CACHE_SIZE = 100 * 1024 * 1024; // 100MB en disco
    private static final int BITMAP_QUALITY = 85; // Calidad de compresión JPEG

    private static ImageCacheManager instance;
    private final LruCache<String, Bitmap> memoryCache;
    private final File cacheDir;
    private final Context context;

    private ImageCacheManager(Context context) {
        this.context = context.getApplicationContext();

        // Configurar caché en memoria
        memoryCache = new LruCache<String, Bitmap>(MAX_MEMORY_CACHE_SIZE) {
            @Override
            protected int sizeOf(String key, Bitmap bitmap) {
                return bitmap.getByteCount();
            }
        };

        // Configurar directorio de caché en disco
        cacheDir = new File(context.getCacheDir(), CACHE_DIR_NAME);
        if (!cacheDir.exists()) {
            boolean created = cacheDir.mkdirs();
            Log.d(TAG, "Directorio de caché creado: " + created);
        }

        // Limpiar caché antigua si excede el tamaño máximo
        cleanOldCacheIfNeeded();
    }

    public static synchronized ImageCacheManager getInstance(Context context) {
        if (instance == null) {
            instance = new ImageCacheManager(context);
        }
        return instance;
    }

    /**
     * Obtiene una imagen del caché (memoria primero, luego disco)
     * @param url URL de la imagen original
     * @return Bitmap si existe en caché, null si no existe
     */
    public Bitmap getImageFromCache(String url) {
        if (url == null || url.trim().isEmpty()) {
            return null;
        }

        String key = generateCacheKey(url);

        // 1. Buscar en memoria primero (más rápido)
        Bitmap memoryBitmap = memoryCache.get(key);
        if (memoryBitmap != null && !memoryBitmap.isRecycled()) {
            Log.d(TAG, "Imagen encontrada en caché de memoria: " + url);
            return memoryBitmap;
        }

        // 2. Buscar en disco
        Bitmap diskBitmap = loadFromDisk(key);
        if (diskBitmap != null && !diskBitmap.isRecycled()) {
            Log.d(TAG, "Imagen encontrada en caché de disco: " + url);
            // Guardar en memoria para acceso rápido futuro
            memoryCache.put(key, diskBitmap);
            return diskBitmap;
        }

        Log.d(TAG, "Imagen no encontrada en caché: " + url);
        return null;
    }

    /**
     * Guarda una imagen en el caché (memoria y disco)
     * @param url URL original de la imagen
     * @param bitmap Bitmap a guardar
     */
    public void saveImageToCache(String url, Bitmap bitmap) {
        if (url == null || url.trim().isEmpty() || bitmap == null || bitmap.isRecycled()) {
            return;
        }

        String key = generateCacheKey(url);

        try {
            // 1. Guardar en memoria
            memoryCache.put(key, bitmap);
            Log.d(TAG, "Imagen guardada en memoria: " + url);

            // 2. Guardar en disco (en hilo secundario para no bloquear UI)
            new Thread(() -> saveToDisk(key, bitmap)).start();

        } catch (Exception e) {
            Log.e(TAG, "Error guardando imagen en caché: " + url, e);
        }
    }

    /**
     * Verifica si una imagen existe en caché
     * @param url URL de la imagen
     * @return true si existe en memoria o disco
     */
    public boolean isImageCached(String url) {
        if (url == null || url.trim().isEmpty()) {
            return false;
        }

        String key = generateCacheKey(url);

        // Verificar memoria
        if (memoryCache.get(key) != null) {
            return true;
        }

        // Verificar disco
        File file = new File(cacheDir, key + ".jpg");
        return file.exists() && file.length() > 0;
    }

    /**
     * Limpia el caché de memoria
     */
    public void clearMemoryCache() {
        try {
            memoryCache.evictAll();
            Log.d(TAG, "Caché de memoria limpiado");
        } catch (Exception e) {
            Log.e(TAG, "Error limpiando caché de memoria", e);
        }
    }

    /**
     * Limpia el caché de disco
     */
    public void clearDiskCache() {
        new Thread(() -> {
            try {
                File[] files = cacheDir.listFiles();
                if (files != null) {
                    for (File file : files) {
                        if (file.delete()) {
                            Log.d(TAG, "Archivo de caché eliminado: " + file.getName());
                        }
                    }
                }
                Log.d(TAG, "Caché de disco limpiado");
            } catch (Exception e) {
                Log.e(TAG, "Error limpiando caché de disco", e);
            }
        }).start();
    }

    /**
     * Limpia todo el caché (memoria y disco)
     */
    public void clearAllCache() {
        clearMemoryCache();
        clearDiskCache();
    }

    /**
     * Obtiene información del caché
     */
    public CacheInfo getCacheInfo() {
        long memorySizeBytes = memoryCache.size();
        long diskSizeBytes = calculateDiskCacheSize();
        int memoryCount = memoryCache.snapshot().size();
        int diskCount = cacheDir.listFiles() != null ? cacheDir.listFiles().length : 0;

        return new CacheInfo(memorySizeBytes, diskSizeBytes, memoryCount, diskCount);
    }

    // --- Métodos privados ---

    private String generateCacheKey(String url) {
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            byte[] hash = digest.digest(url.getBytes());
            StringBuilder hexString = new StringBuilder();

            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }

            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            Log.e(TAG, "Error generando clave de caché", e);
            return String.valueOf(url.hashCode());
        }
    }

    private Bitmap loadFromDisk(String key) {
        File file = new File(cacheDir, key + ".jpg");
        if (!file.exists()) {
            return null;
        }

        try (FileInputStream fis = new FileInputStream(file)) {
            return BitmapFactory.decodeStream(fis);
        } catch (IOException e) {
            Log.e(TAG, "Error cargando imagen de disco: " + key, e);
            // Si hay error, eliminar archivo corrupto
            if (file.delete()) {
                Log.d(TAG, "Archivo corrupto eliminado: " + key);
            }
            return null;
        }
    }

    private void saveToDisk(String key, Bitmap bitmap) {
        File file = new File(cacheDir, key + ".jpg");
        try (FileOutputStream fos = new FileOutputStream(file)) {
            bitmap.compress(Bitmap.CompressFormat.JPEG, BITMAP_QUALITY, fos);
            fos.flush();
            Log.d(TAG, "Imagen guardada en disco: " + key);
        } catch (IOException e) {
            Log.e(TAG, "Error guardando imagen en disco: " + key, e);
            // Si hay error, eliminar archivo parcialmente escrito
            if (file.exists() && file.delete()) {
                Log.d(TAG, "Archivo parcial eliminado: " + key);
            }
        }
    }

    private long calculateDiskCacheSize() {
        long totalSize = 0;
        File[] files = cacheDir.listFiles();
        if (files != null) {
            for (File file : files) {
                totalSize += file.length();
            }
        }
        return totalSize;
    }

    private void cleanOldCacheIfNeeded() {
        new Thread(() -> {
            try {
                long currentSize = calculateDiskCacheSize();
                if (currentSize > MAX_DISK_CACHE_SIZE) {
                    Log.d(TAG, "Caché de disco excede tamaño máximo, limpiando...");

                    File[] files = cacheDir.listFiles();
                    if (files != null) {
                        // Ordenar por fecha de modificación (más antiguos primero)
                        java.util.Arrays.sort(files, (f1, f2) -> Long.compare(f1.lastModified(), f2.lastModified()));

                        // Eliminar archivos hasta estar bajo el límite
                        for (File file : files) {
                            if (currentSize <= MAX_DISK_CACHE_SIZE * 0.8) { // Dejar 20% de margen
                                break;
                            }
                            currentSize -= file.length();
                            if (file.delete()) {
                                Log.d(TAG, "Archivo antiguo eliminado: " + file.getName());
                            }
                        }
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "Error limpiando caché antigua", e);
            }
        }).start();
    }

    /**
     * Clase para información del caché
     */
    public static class CacheInfo {
        public final long memorySizeBytes;
        public final long diskSizeBytes;
        public final int memoryCount;
        public final int diskCount;

        public CacheInfo(long memorySizeBytes, long diskSizeBytes, int memoryCount, int diskCount) {
            this.memorySizeBytes = memorySizeBytes;
            this.diskSizeBytes = diskSizeBytes;
            this.memoryCount = memoryCount;
            this.diskCount = diskCount;
        }

        public String getMemorySizeMB() {
            return String.format("%.1f MB", memorySizeBytes / (1024.0 * 1024.0));
        }

        public String getDiskSizeMB() {
            return String.format("%.1f MB", diskSizeBytes / (1024.0 * 1024.0));
        }

        @Override
        public String toString() {
            return "CacheInfo{" +
                    "memoria=" + getMemorySizeMB() + " (" + memoryCount + " imágenes), " +
                    "disco=" + getDiskSizeMB() + " (" + diskCount + " archivos)" +
                    '}';
        }
    }
}
