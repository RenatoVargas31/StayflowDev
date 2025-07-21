package com.iot.stayflowdev.repositories;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.iot.stayflowdev.model.Hotel;
import com.iot.stayflowdev.model.LugaresCercanos;
import com.iot.stayflowdev.model.Servicio;

import java.util.ArrayList;
import java.util.List;

public class HotelRepository {
    private static final String TAG = "HotelRepository";
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final MutableLiveData<List<Hotel>> hotelesLiveData = new MutableLiveData<>();
    private final MutableLiveData<List<Hotel>> hotelesMejorValoradosLiveData = new MutableLiveData<>();
    private final MutableLiveData<Hotel> hotelLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> errorLiveData = new MutableLiveData<>();

    public LiveData<List<Hotel>> obtenerHoteles(){
        db.collection("hoteles")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<Hotel> hoteles = new ArrayList<>();
                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        Hotel hotel = doc.toObject(Hotel.class);
                        if (hotel != null) {
                            hoteles.add(hotel);
                        }
                    }
                    hotelesLiveData.setValue(hoteles);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error al obtener hoteles", e);
                    errorLiveData.setValue("Error al cargar hoteles: " + e.getMessage());
                });
        return hotelesLiveData;
    }

    /**
     * Obtiene los 10 hoteles mejor valorados de la base de datos.
     * @return LiveData con la lista de los 10 hoteles con mejor calificación
     */
    public LiveData<List<Hotel>> obtenerHotelesMejorValorados() {
        db.collection("hoteles")
                .orderBy("calificacion", Query.Direction.DESCENDING)
                .limit(10)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<Hotel> hoteles = new ArrayList<>();
                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        Hotel hotel = doc.toObject(Hotel.class);
                        if (hotel != null) {
                            hoteles.add(hotel);
                        }
                    }
                    Log.d(TAG, "Hoteles mejor valorados obtenidos: " + hoteles.size());
                    hotelesMejorValoradosLiveData.setValue(hoteles);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error al obtener hoteles mejor valorados", e);
                    errorLiveData.setValue("Error al cargar hoteles mejor valorados: " + e.getMessage());
                });
        return hotelesMejorValoradosLiveData;
    }

    /**
     * Obtiene un hotel específico por su ID
     * @param hotelId ID del hotel a obtener
     * @return LiveData con el hotel solicitado
     */
    public LiveData<Hotel> obtenerHotelPorId(String hotelId) {
        db.collection("hoteles")
                .document(hotelId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    Hotel hotel = documentSnapshot.toObject(Hotel.class);
                    hotelLiveData.setValue(hotel);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error al obtener hotel por ID: " + hotelId, e);
                    errorLiveData.setValue("Error al cargar el hotel: " + e.getMessage());
                });
        return hotelLiveData;
    }
    /**
     * Busca hoteles por nombre (coincidencia parcial en cualquier posición)
     * @param query Texto a buscar en nombre
     * @return LiveData con la lista de hoteles que coinciden con la búsqueda
    */
    public LiveData<List<Hotel>> buscarHotelesPorNombre(String query){
        MutableLiveData<List<Hotel>> hotelesEncontradosLiveData = new MutableLiveData<>();
        String lowerCaseQuery = query.toLowerCase().trim();

        // Primero obtenemos todos los hoteles y luego filtramos en memoria
        // ya que Firestore no soporta nativamente búsquedas tipo "contains"
        db.collection("hoteles")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<Hotel> hoteles = new ArrayList<>();
                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        Hotel hotel = doc.toObject(Hotel.class);
                        if (hotel != null && hotel.getNombre() != null) {
                            // Convertir a minúsculas para búsqueda insensible a mayúsculas/minúsculas
                            String nombreHotel = hotel.getNombre().toLowerCase();

                            // Comprobar si el nombre contiene el término de búsqueda en cualquier posición
                            if (nombreHotel.contains(lowerCaseQuery)) {
                                hoteles.add(hotel);
                                Log.d(TAG, "Hotel coincidente encontrado: " + hotel.getNombre());
                            }
                        }
                    }
                    Log.d(TAG, "Total hoteles encontrados con '" + lowerCaseQuery + "': " + hoteles.size());
                    hotelesEncontradosLiveData.setValue(hoteles);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error al buscar hoteles por nombre", e);
                    errorLiveData.setValue("Error al buscar hoteles: " + e.getMessage());
                });
        return hotelesEncontradosLiveData;
    }
    /**
     * Obtiene los servicios de un hotel específico
     * @param hotelId ID del hotel
     * @return LiveData con la lista de servicios del hotel
     */
    public LiveData<List<Servicio>> obtenerServiciosPorHotel(String hotelId) {
        MutableLiveData<List<Servicio>> serviciosLiveData = new MutableLiveData<>();

        db.collection("hoteles")
                .document(hotelId)
                .collection("servicios")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<Servicio> servicios = new ArrayList<>();
                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        Servicio servicio = doc.toObject(Servicio.class);
                        if (servicio != null) {
                            servicios.add(servicio);
                        }
                    }
                    serviciosLiveData.setValue(servicios);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error al obtener servicios del hotel: " + hotelId, e);
                    errorLiveData.setValue("Error al cargar los servicios: " + e.getMessage());
                });
        return serviciosLiveData;
    }
    /**
     * Obtiene los lugares históricos cercanos a un hotel específico
     * @param hotelId ID del hotel
     * @return LiveData con la lista de lugares históricos cercanos al hotel
     */
    public LiveData<List<LugaresCercanos>> obtenerLugaresHistoricosCercanos(String hotelId) {
        MutableLiveData<List<LugaresCercanos>> lugaresHistoricosLiveData = new MutableLiveData<>();

        db.collection("hoteles")
                .document(hotelId)
                .collection("lugaresCercanos")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<LugaresCercanos> lugares = new ArrayList<>();
                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        LugaresCercanos lugar = doc.toObject(LugaresCercanos.class);
                        if (lugar != null) {
                            lugares.add(lugar);
                        }
                    }
                    lugaresHistoricosLiveData.setValue(lugares);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error al obtener lugares históricos del hotel: " + hotelId, e);
                    errorLiveData.setValue("Error al cargar los lugares históricos: " + e.getMessage());
                });
        return lugaresHistoricosLiveData;
    }

    //Disminuir cantidad de habitaciones disponibles en un hotel, cada hotel de la colección "hoteles" tiene una subcolección "habitaciones" y cada habitción tiene un campo "cantidad" esta disminución no es fija, el id del hotel, el id de la habitación y la cantidad a disminuir se pasan como parámetros
    /**
     * Disminuye la cantidad de habitaciones disponibles en un hotel.
     * @param hotelId ID del hotel
     * @param habitacionId ID de la habitación
     * @param cantidad Cantidad a disminuir
     */

    public void disminuirHabitacionesDisponibles(String hotelId, String habitacionId, int cantidad) {
        db.collection("hoteles")
                .document(hotelId)
                .collection("habitaciones")
                .document(habitacionId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // Convertimos el string a entero, operamos y luego volvemos a string
                        String cantidadActualStr = documentSnapshot.getString("cantidad");
                        Log.d(TAG, "Cantidad actual: " + cantidadActualStr);
                        try {
                            int cantidadActual = Integer.parseInt(cantidadActualStr);
                            int nuevaCantidad = Math.max(0, cantidadActual - cantidad); // Evita cantidades negativas
                            Log.d(TAG, "Nueva cantidad después de disminuir: " + nuevaCantidad);
                            // Actualizamos el valor como string
                            db.collection("hoteles")
                                    .document(hotelId)
                                    .collection("habitaciones")
                                    .document(habitacionId)
                                    .update("cantidad", String.valueOf(nuevaCantidad))
                                    .addOnSuccessListener(aVoid ->
                                            Log.d(TAG, "Cantidad de habitaciones disminuida correctamente"))
                                    .addOnFailureListener(e -> {
                                        Log.e(TAG, "Error al disminuir habitaciones disponibles", e);
                                        errorLiveData.setValue("Error al disminuir habitaciones: " + e.getMessage());
                                    });
                        } catch (NumberFormatException e) {
                            Log.e(TAG, "Error: el valor de cantidad no es un número válido", e);
                            errorLiveData.setValue("Error: el formato de cantidad es inválido");
                        }
                    } else {
                        Log.e(TAG, "La habitación no existe");
                        errorLiveData.setValue("Error: La habitación especificada no existe");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error al obtener la habitación", e);
                    errorLiveData.setValue("Error al obtener la habitación: " + e.getMessage());
                });
    }
    /**
     * Obtiene un LiveData que contiene mensajes de error que puedan ocurrir durante las operaciones con hoteles.
     * @return LiveData con mensajes de error
     */
    public LiveData<String> getErrorLiveData() {
        return errorLiveData;
    }
}
