package com.iot.stayflowdev.viewmodels;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.iot.stayflowdev.Driver.Repository.TarjetaCreditoRepository;
import com.iot.stayflowdev.model.TarjetaCredito;

/**
 * ViewModel para gestionar operaciones relacionadas con tarjetas de crédito
 */
public class TarjetaCreditoViewModel extends ViewModel {
    private static final String TAG = "TarjetaCreditoViewModel";

    // Repository para acceder a los datos
    private final TarjetaCreditoRepository repository;

    // LiveData para comunicar datos a la UI
    private final MutableLiveData<TarjetaCredito> tarjetaData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> hasTarjeta = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<Boolean> operationSuccessful = new MutableLiveData<>();
    private final MutableLiveData<String> successMessage = new MutableLiveData<>();

    // Constructor
    public TarjetaCreditoViewModel() {
        repository = new TarjetaCreditoRepository();

        // Verificar si el usuario tiene tarjeta al inicializar
        verificarTarjeta();
    }

    // Getters para los LiveData
    public LiveData<TarjetaCredito> getTarjetaData() {
        return tarjetaData;
    }

    public LiveData<Boolean> getHasTarjeta() {
        return hasTarjeta;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public LiveData<Boolean> getOperationSuccessful() {
        return operationSuccessful;
    }

    public LiveData<String> getSuccessMessage() {
        return successMessage;
    }

    /**
     * Verifica si el usuario tiene tarjeta de crédito registrada
     */
    public void verificarTarjeta() {
        if (!repository.usuarioEstaAutenticado()) {
            errorMessage.setValue("Usuario no autenticado");
            return;
        }

        isLoading.setValue(true);

        repository.tieneTarjeta(new OnSuccessListener<Boolean>() {
            @Override
            public void onSuccess(Boolean tieneTarjeta) {
                hasTarjeta.setValue(tieneTarjeta);
                isLoading.setValue(false);

                // Si tiene tarjeta, cargar sus datos
                if (tieneTarjeta) {
                    cargarDatosTarjeta();
                }
            }
        }, new OnFailureListener() {
            @Override
            public void onFailure(Exception e) {
                Log.e(TAG, "Error al verificar tarjeta", e);
                errorMessage.setValue("Error al verificar tarjeta: " + e.getMessage());
                isLoading.setValue(false);
            }
        });
    }

    /**
     * Carga los datos de la tarjeta del usuario actual
     */
    public void cargarDatosTarjeta() {
        if (!repository.usuarioEstaAutenticado()) {
            errorMessage.setValue("Usuario no autenticado");
            return;
        }

        isLoading.setValue(true);

        repository.obtenerTarjetaUsuario(new OnSuccessListener<TarjetaCredito>() {
            @Override
            public void onSuccess(TarjetaCredito tarjeta) {
                tarjetaData.setValue(tarjeta);
                hasTarjeta.setValue(true);
                isLoading.setValue(false);
            }
        }, new OnFailureListener() {
            @Override
            public void onFailure(Exception e) {
                Log.e(TAG, "Error al cargar tarjeta", e);
                errorMessage.setValue("Error al cargar tarjeta: " + e.getMessage());
                hasTarjeta.setValue(false);
                isLoading.setValue(false);
            }
        });
    }

    /**
     * Guarda una nueva tarjeta o actualiza la existente
     * @param tarjeta La tarjeta a guardar
     */
    public void guardarTarjeta(TarjetaCredito tarjeta) {
        if (!repository.usuarioEstaAutenticado()) {
            errorMessage.setValue("Usuario no autenticado");
            return;
        }

        if (!validarTarjeta(tarjeta)) {
            return; // El método validarTarjeta ya establece el mensaje de error
        }

        isLoading.setValue(true);

        // Verificar si ya tiene tarjeta para decidir si agregar o actualizar
        repository.tieneTarjeta(new OnSuccessListener<Boolean>() {
            @Override
            public void onSuccess(Boolean tieneTarjeta) {
                if (tieneTarjeta) {
                    // Actualizar tarjeta existente
                    actualizarTarjetaExistente(tarjeta);
                } else {
                    // Agregar nueva tarjeta
                    agregarNuevaTarjeta(tarjeta);
                }
            }
        }, new OnFailureListener() {
            @Override
            public void onFailure(Exception e) {
                Log.e(TAG, "Error al verificar tarjeta existente", e);
                errorMessage.setValue("Error al verificar tarjeta: " + e.getMessage());
                isLoading.setValue(false);
            }
        });
    }

    /**
     * Agrega una nueva tarjeta
     * @param tarjeta La tarjeta a agregar
     */
    private void agregarNuevaTarjeta(TarjetaCredito tarjeta) {
        repository.agregarTarjeta(tarjeta, new OnSuccessListener<String>() {
            @Override
            public void onSuccess(String id) {
                tarjeta.setId(id);
                tarjetaData.setValue(tarjeta);
                hasTarjeta.setValue(true);
                successMessage.setValue("Tarjeta agregada correctamente");
                operationSuccessful.setValue(true);
                isLoading.setValue(false);
            }
        }, new OnFailureListener() {
            @Override
            public void onFailure(Exception e) {
                Log.e(TAG, "Error al agregar tarjeta", e);
                errorMessage.setValue("Error al agregar tarjeta: " + e.getMessage());
                operationSuccessful.setValue(false);
                isLoading.setValue(false);
            }
        });
    }

    /**
     * Actualiza una tarjeta existente
     * @param tarjeta La tarjeta con datos actualizados
     */
    private void actualizarTarjetaExistente(TarjetaCredito tarjeta) {
        repository.actualizarTarjeta(tarjeta, new OnSuccessListener<String>() {
            @Override
            public void onSuccess(String id) {
                tarjeta.setId(id);
                tarjetaData.setValue(tarjeta);
                successMessage.setValue("Tarjeta actualizada correctamente");
                operationSuccessful.setValue(true);
                isLoading.setValue(false);
            }
        }, new OnFailureListener() {
            @Override
            public void onFailure(Exception e) {
                Log.e(TAG, "Error al actualizar tarjeta", e);
                errorMessage.setValue("Error al actualizar tarjeta: " + e.getMessage());
                operationSuccessful.setValue(false);
                isLoading.setValue(false);
            }
        });
    }

    /**
     * Elimina la tarjeta del usuario actual
     */
    public void eliminarTarjeta() {
        if (!repository.usuarioEstaAutenticado()) {
            errorMessage.setValue("Usuario no autenticado");
            return;
        }

        isLoading.setValue(true);

        repository.eliminarTarjeta(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                tarjetaData.setValue(null);
                hasTarjeta.setValue(false);
                successMessage.setValue("Tarjeta eliminada correctamente");
                operationSuccessful.setValue(true);
                isLoading.setValue(false);
            }
        }, new OnFailureListener() {
            @Override
            public void onFailure(Exception e) {
                Log.e(TAG, "Error al eliminar tarjeta", e);
                errorMessage.setValue("Error al eliminar tarjeta: " + e.getMessage());
                operationSuccessful.setValue(false);
                isLoading.setValue(false);
            }
        });
    }

    /**
     * Desactiva la tarjeta del usuario actual (sin eliminarla)
     */
    public void desactivarTarjeta() {
        if (!repository.usuarioEstaAutenticado()) {
            errorMessage.setValue("Usuario no autenticado");
            return;
        }

        isLoading.setValue(true);

        repository.desactivarTarjeta(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                // Actualizar el objeto en memoria
                TarjetaCredito tarjetaActual = tarjetaData.getValue();
                if (tarjetaActual != null) {
                    tarjetaActual.setActiva(false);
                    tarjetaData.setValue(tarjetaActual);
                }

                hasTarjeta.setValue(false);
                successMessage.setValue("Tarjeta desactivada correctamente");
                operationSuccessful.setValue(true);
                isLoading.setValue(false);
            }
        }, new OnFailureListener() {
            @Override
            public void onFailure(Exception e) {
                Log.e(TAG, "Error al desactivar tarjeta", e);
                errorMessage.setValue("Error al desactivar tarjeta: " + e.getMessage());
                operationSuccessful.setValue(false);
                isLoading.setValue(false);
            }
        });
    }

    /**
     * Reactiva una tarjeta previamente desactivada
     */
    public void reactivarTarjeta() {
        if (!repository.usuarioEstaAutenticado()) {
            errorMessage.setValue("Usuario no autenticado");
            return;
        }

        isLoading.setValue(true);

        repository.reactivarTarjeta(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                // Actualizar el objeto en memoria
                TarjetaCredito tarjetaActual = tarjetaData.getValue();
                if (tarjetaActual != null) {
                    tarjetaActual.setActiva(true);
                    tarjetaData.setValue(tarjetaActual);
                }

                hasTarjeta.setValue(true);
                successMessage.setValue("Tarjeta reactivada correctamente");
                operationSuccessful.setValue(true);
                isLoading.setValue(false);

                // Recargar datos completos de la tarjeta
                cargarDatosTarjeta();
            }
        }, new OnFailureListener() {
            @Override
            public void onFailure(Exception e) {
                Log.e(TAG, "Error al reactivar tarjeta", e);
                errorMessage.setValue("Error al reactivar tarjeta: " + e.getMessage());
                operationSuccessful.setValue(false);
                isLoading.setValue(false);
            }
        });
    }

    /**
     * Valida los datos de una tarjeta
     * @param tarjeta La tarjeta a validar
     * @return true si la tarjeta es válida, false en caso contrario
     */
    private boolean validarTarjeta(TarjetaCredito tarjeta) {
        if (tarjeta == null) {
            errorMessage.setValue("Datos de tarjeta no válidos");
            return false;
        }

        if (!tarjeta.esNumeroValido()) {
            errorMessage.setValue("El número de tarjeta no es válido");
            return false;
        }

        if (!tarjeta.esCvvValido()) {
            errorMessage.setValue("El código de seguridad (CVV) no es válido");
            return false;
        }

        if (!tarjeta.esFechaValida()) {
            errorMessage.setValue("La fecha de expiración no es válida o la tarjeta está vencida");
            return false;
        }

        if (tarjeta.getTitular() == null || tarjeta.getTitular().trim().isEmpty()) {
            errorMessage.setValue("El nombre del titular no puede estar vacío");
            return false;
        }

        return true;
    }

    /**
     * Limpia los mensajes de error y éxito
     * Útil para limpiar el estado después de mostrar mensajes al usuario
     */
    public void limpiarMensajes() {
        errorMessage.setValue(null);
        successMessage.setValue(null);
        operationSuccessful.setValue(null);
    }

    /**
     * Crea una instancia de TarjetaCredito con datos formateados
     * @param numero Número completo de la tarjeta
     * @param titular Nombre del titular
     * @param mes Mes de expiración (1-12)
     * @param anio Año de expiración (4 dígitos)
     * @param cvv Código de seguridad
     * @return Una nueva instancia de TarjetaCredito
     */
    public TarjetaCredito crearTarjeta(String numero, String titular, int mes, int anio, String cvv) {
        // Formatear mes con dos dígitos
        String mesStr = String.format("%02d", mes);
        // Formatear año como string
        String anioStr = String.valueOf(anio);

        return new TarjetaCredito(numero, titular, mesStr, anioStr, cvv);
    }
}
