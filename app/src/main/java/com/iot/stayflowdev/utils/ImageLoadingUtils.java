package com.iot.stayflowdev.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.widget.ImageView;

import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.iot.stayflowdev.R;

public class ImageLoadingUtils {
    private static final String TAG = "ImageLoadingUtils";

    public interface ImageLoadCallback {
        default void onLoadSuccess() {}
        default void onLoadFailed() {}
        default void onLoadStarted() {}
    }

    /**
     * Carga una imagen de manera segura con caché automático
     */
    public static void loadImageSafely(Context context, String imageUrl, ImageView imageView,
                                      int placeholderRes, @Nullable ImageLoadCallback callback) {

        if (context == null || imageView == null) {
            Log.w(TAG, "Context o ImageView es null, no se puede cargar la imagen");
            if (callback != null) callback.onLoadFailed();
            return;
        }

        if (imageUrl == null || imageUrl.trim().isEmpty()) {
            Log.d(TAG, "URL de imagen vacía, usando placeholder");
            imageView.setImageResource(placeholderRes);
            if (callback != null) callback.onLoadFailed();
            return;
        }

        try {
            if (callback != null) callback.onLoadStarted();

            // 1. PRIMERO: Verificar caché local
            ImageCacheManager cacheManager = ImageCacheManager.getInstance(context);
            Bitmap cachedBitmap = cacheManager.getImageFromCache(imageUrl);

            if (cachedBitmap != null) {
                Log.d(TAG, "Imagen cargada desde caché local: " + imageUrl);
                imageView.setImageBitmap(cachedBitmap);
                if (callback != null) callback.onLoadSuccess();
                return;
            }

            // 2. SI NO ESTÁ EN CACHÉ: Cargar desde red y guardar en caché
            Glide.with(context)
                    .asBitmap() // Obtener como Bitmap para poder guardar en caché
                    .load(imageUrl)
                    .placeholder(placeholderRes)
                    .error(placeholderRes)
                    .fallback(placeholderRes)
                    .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                    .timeout(8000) // 8 segundos timeout
                    .listener(new RequestListener<Bitmap>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model,
                                                   Target<Bitmap> target, boolean isFirstResource) {
                            Log.w(TAG, "Error cargando imagen desde red: " + imageUrl, e);
                            if (callback != null) callback.onLoadFailed();
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Bitmap resource, Object model,
                                                     Target<Bitmap> target, DataSource dataSource,
                                                     boolean isFirstResource) {
                            Log.d(TAG, "Imagen cargada desde red: " + imageUrl);

                            // Guardar en caché local para futuras cargas
                            cacheManager.saveImageToCache(imageUrl, resource);

                            if (callback != null) callback.onLoadSuccess();
                            return false;
                        }
                    })
                    .into(imageView);

        } catch (Exception e) {
            Log.e(TAG, "Error al configurar carga de imagen", e);
            try {
                imageView.setImageResource(placeholderRes);
            } catch (Exception ex) {
                Log.e(TAG, "Error al establecer placeholder", ex);
            }
            if (callback != null) callback.onLoadFailed();
        }
    }

    /**
     * Carga imagen circular para perfiles con caché local
     */
    public static void loadProfileImage(Context context, String imageUrl, ImageView imageView,
                                       @Nullable ImageLoadCallback callback) {

        if (context == null || imageView == null) {
            if (callback != null) callback.onLoadFailed();
            return;
        }

        if (imageUrl == null || imageUrl.trim().isEmpty()) {
            Log.d(TAG, "URL de imagen de perfil vacía, usando placeholder");
            imageView.setImageResource(R.drawable.ic_person);
            if (callback != null) callback.onLoadFailed();
            return;
        }

        try {
            if (callback != null) callback.onLoadStarted();

            // 1. PRIMERO: Verificar caché local
            ImageCacheManager cacheManager = ImageCacheManager.getInstance(context);
            Bitmap cachedBitmap = cacheManager.getImageFromCache(imageUrl);

            if (cachedBitmap != null) {
                Log.d(TAG, "Imagen de perfil cargada desde caché local: " + imageUrl);

                // Aplicar círculo al bitmap y establecer en ImageView
                Glide.with(context)
                        .load(cachedBitmap)
                        .placeholder(R.drawable.ic_person)
                        .error(R.drawable.ic_person)
                        .circleCrop()
                        .into(imageView);

                if (callback != null) callback.onLoadSuccess();
                return;
            }

            // 2. SI NO ESTÁ EN CACHÉ: Cargar desde red con transformación circular
            Glide.with(context)
                    .asBitmap() // Obtener como Bitmap para guardar en caché
                    .load(imageUrl)
                    .placeholder(R.drawable.ic_person)
                    .error(R.drawable.ic_person)
                    .fallback(R.drawable.ic_person)
                    .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                    .timeout(8000)
                    .listener(new RequestListener<Bitmap>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model,
                                                   Target<Bitmap> target, boolean isFirstResource) {
                            Log.w(TAG, "Error cargando imagen de perfil desde red: " + imageUrl, e);
                            if (callback != null) callback.onLoadFailed();
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Bitmap resource, Object model,
                                                     Target<Bitmap> target, DataSource dataSource,
                                                     boolean isFirstResource) {
                            Log.d(TAG, "Imagen de perfil cargada desde red: " + imageUrl);

                            // Guardar el bitmap original en caché local
                            cacheManager.saveImageToCache(imageUrl, resource);

                            if (callback != null) callback.onLoadSuccess();
                            return false; // Permitir que Glide continue con la transformación
                        }
                    })
                    .circleCrop() // Aplicar transformación circular
                    .into(imageView);

        } catch (Exception e) {
            Log.e(TAG, "Error al cargar imagen de perfil", e);
            try {
                imageView.setImageResource(R.drawable.ic_person);
            } catch (Exception ex) {
                Log.e(TAG, "Error al establecer imagen por defecto", ex);
            }
            if (callback != null) callback.onLoadFailed();
        }
    }

    /**
     * Método de compatibilidad para cargar imágenes con Glide de manera optimizada
     * Mantiene la misma interfaz que el código anterior pero con optimizaciones
     */
    public static void loadImageWithGlide(Context context, String imageUrl, ImageView imageView, int placeholderRes) {
        loadImageSafely(context, imageUrl, imageView, placeholderRes, new ImageLoadCallback() {
            @Override
            public void onLoadSuccess() {
                // Limpiar cualquier tinte o padding cuando la imagen se carga exitosamente
                try {
                    imageView.setImageTintList(null);
                    imageView.setPadding(0, 0, 0, 0);
                } catch (Exception e) {
                    Log.w(TAG, "Error limpiando tinte de imagen", e);
                }
            }

            @Override
            public void onLoadFailed() {
                Log.d(TAG, "Imagen falló al cargar, usando placeholder");
            }
        });
    }

    /**
     * Limpia una ImageView y cancela cualquier carga pendiente
     */
    public static void clearImage(ImageView imageView) {
        try {
            if (imageView != null && imageView.getContext() != null) {
                Glide.with(imageView.getContext()).clear(imageView);
                imageView.setImageDrawable(null);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error limpiando imagen", e);
        }
    }

    /**
     * Método sobrecargado para compatibilidad con código existente
     */
    public static void loadImageWithGlide(Context context, String imageUrl, ImageView imageView,
                                         int placeholderRes, int errorRes) {
        loadImageSafely(context, imageUrl, imageView, placeholderRes, new ImageLoadCallback() {
            @Override
            public void onLoadSuccess() {
                try {
                    imageView.setImageTintList(null);
                    imageView.setPadding(0, 0, 0, 0);
                    imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                } catch (Exception e) {
                    Log.w(TAG, "Error configurando imagen después de carga exitosa", e);
                }
            }

            @Override
            public void onLoadFailed() {
                try {
                    imageView.setImageResource(errorRes);
                } catch (Exception e) {
                    Log.w(TAG, "Error estableciendo imagen de error", e);
                }
            }
        });
    }

    /**
     * Limpia la caché de Glide cuando sea necesario
     */
    public static void clearCache(Context context) {
        try {
            new Thread(() -> {
                try {
                    Glide.get(context).clearDiskCache();
                } catch (Exception e) {
                    Log.e(TAG, "Error limpiando caché de disco", e);
                }
            }).start();

            Glide.get(context).clearMemory();
            Log.d(TAG, "Caché de Glide limpiada");
        } catch (Exception e) {
            Log.e(TAG, "Error limpiando caché de Glide", e);
        }
    }

    /**
     * Cancela todas las cargas de imagen pendientes para un contexto
     */
    public static void cancelPendingLoads(Context context) {
        try {
            // Pausar todas las cargas de Glide para este contexto
            Glide.with(context).pauseAllRequests();
            Log.d(TAG, "Cargas pendientes pausadas");
        } catch (Exception e) {
            Log.e(TAG, "Error pausando cargas pendientes", e);
        }
    }

    /**
     * Reanuda las cargas de imagen para un contexto
     */
    public static void resumePendingLoads(Context context) {
        try {
            Glide.with(context).resumeRequests();
            Log.d(TAG, "Cargas reanudadas");
        } catch (Exception e) {
            Log.e(TAG, "Error reanudando cargas", e);
        }
    }

    /**
     * Métodos para gestión de caché desde la UI
     */
    public static void clearAllImageCache(Context context) {
        try {
            // Limpiar caché de Glide
            clearCache(context);

            // Limpiar caché local personalizado
            ImageCacheManager.getInstance(context).clearAllCache();

            Log.d(TAG, "Todo el caché de imágenes limpiado");
        } catch (Exception e) {
            Log.e(TAG, "Error limpiando todo el caché", e);
        }
    }

    /**
     * Obtiene información del caché para mostrar en configuraciones
     */
    public static String getCacheInfoString(Context context) {
        try {
            ImageCacheManager.CacheInfo info = ImageCacheManager.getInstance(context).getCacheInfo();
            return info.toString();
        } catch (Exception e) {
            Log.e(TAG, "Error obteniendo información de caché", e);
            return "Error obteniendo información de caché";
        }
    }

    /**
     * Verifica si una imagen está disponible offline
     */
    public static boolean isImageAvailableOffline(Context context, String imageUrl) {
        try {
            return ImageCacheManager.getInstance(context).isImageCached(imageUrl);
        } catch (Exception e) {
            Log.e(TAG, "Error verificando disponibilidad offline", e);
            return false;
        }
    }

    /**
     * Pre-carga una imagen en segundo plano para que esté disponible offline
     */
    public static void preloadImageForOfflineUse(Context context, String imageUrl) {
        if (context == null || imageUrl == null || imageUrl.trim().isEmpty()) {
            return;
        }

        // Verificar si ya está en caché
        if (isImageAvailableOffline(context, imageUrl)) {
            Log.d(TAG, "Imagen ya está en caché: " + imageUrl);
            return;
        }

        // Pre-cargar en segundo plano
        new Thread(() -> {
            try {
                Glide.with(context.getApplicationContext())
                        .asBitmap()
                        .load(imageUrl)
                        .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                        .submit()
                        .get(); // Ejecutar síncronamente en hilo secundario

                Log.d(TAG, "Imagen pre-cargada para uso offline: " + imageUrl);
            } catch (Exception e) {
                Log.e(TAG, "Error pre-cargando imagen: " + imageUrl, e);
            }
        }).start();
    }
}
