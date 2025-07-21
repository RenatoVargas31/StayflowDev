package com.iot.stayflowdev.utils;

import android.util.Log;

import java.util.HashMap;
import java.util.Map;

public class NavigationThrottleManager {
    private static final String TAG = "NavigationThrottle";
    private static final long DEFAULT_THROTTLE_DELAY = 800; // 800ms entre navegaciones
    private static NavigationThrottleManager instance;

    private final Map<String, Long> lastNavigationTimes;
    private long throttleDelay;

    private NavigationThrottleManager() {
        this.lastNavigationTimes = new HashMap<>();
        this.throttleDelay = DEFAULT_THROTTLE_DELAY;
    }

    public static synchronized NavigationThrottleManager getInstance() {
        if (instance == null) {
            instance = new NavigationThrottleManager();
        }
        return instance;
    }

    /**
     * Verifica si se puede navegar desde una vista específica
     * @param viewKey Identificador único de la vista (ej: "SuperAdminActivity")
     * @return true si se puede navegar, false si debe esperar
     */
    public boolean canNavigate(String viewKey) {
        long currentTime = System.currentTimeMillis();
        Long lastTime = lastNavigationTimes.get(viewKey);

        if (lastTime == null) {
            lastNavigationTimes.put(viewKey, currentTime);
            Log.d(TAG, "Primera navegación desde: " + viewKey);
            return true;
        }

        long timeDiff = currentTime - lastTime;
        if (timeDiff >= throttleDelay) {
            lastNavigationTimes.put(viewKey, currentTime);
            Log.d(TAG, "Navegación permitida desde: " + viewKey + " (transcurrido: " + timeDiff + "ms)");
            return true;
        } else {
            Log.d(TAG, "Navegación bloqueada desde: " + viewKey + " (faltan: " + (throttleDelay - timeDiff) + "ms)");
            return false;
        }
    }

    /**
     * Registra una navegación forzada (para casos especiales)
     */
    public void forceNavigation(String viewKey) {
        lastNavigationTimes.put(viewKey, System.currentTimeMillis());
        Log.d(TAG, "Navegación forzada registrada para: " + viewKey);
    }

    /**
     * Limpia el historial de navegación para una vista específica
     */
    public void clearNavigationHistory(String viewKey) {
        lastNavigationTimes.remove(viewKey);
        Log.d(TAG, "Historial de navegación limpiado para: " + viewKey);
    }

    /**
     * Configura el delay personalizado para el throttling
     */
    public void setThrottleDelay(long delayMs) {
        this.throttleDelay = delayMs;
        Log.d(TAG, "Delay de throttling configurado a: " + delayMs + "ms");
    }

    /**
     * Obtiene el tiempo restante antes de poder navegar nuevamente
     */
    public long getRemainingThrottleTime(String viewKey) {
        Long lastTime = lastNavigationTimes.get(viewKey);
        if (lastTime == null) {
            return 0;
        }

        long timeDiff = System.currentTimeMillis() - lastTime;
        return Math.max(0, throttleDelay - timeDiff);
    }
}
