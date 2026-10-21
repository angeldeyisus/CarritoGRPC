package com.tienda.grpc.server;

import com.tienda.grpc.*;
import io.grpc.stub.StreamObserver;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class CarritoServiceImpl extends CarritoServiceGrpc.CarritoServiceImplBase {

    // Inventario concurrente para múltiples clientes
    private final Map<String, Integer> inventario = new ConcurrentHashMap<>();
    private final Map<String, Producto> catalogo = new ConcurrentHashMap<>();
    private final ServidorGUI gui; // Referencia a la interfaz gráfica

    public CarritoServiceImpl(ServidorGUI gui) {
        this.gui = gui;
        // Inicializar inventario
        catalogo.put("PROD-001", Producto.newBuilder().setId("PROD-001").setNombre("Laptop").setPrecio(1200.0).build());
        inventario.put("PROD-001", 10);

        catalogo.put("PROD-002", Producto.newBuilder().setId("PROD-002").setNombre("Mouse").setPrecio(25.0).build());
        inventario.put("PROD-002", 50);

        gui.actualizarInventario(inventario);
    }

    @Override
    public void obtenerCatalogo(Empty request, StreamObserver<CatalogoResponse> responseObserver) {
        CatalogoResponse.Builder responseBuilder = CatalogoResponse.newBuilder();
        for (Producto p : catalogo.values()) {
            responseBuilder.addProductos(p);
        }
        responseObserver.onNext(responseBuilder.build());
        responseObserver.onCompleted();
        gui.log("Catálogo solicitado por un cliente.");
    }

    @Override
    public void procesarCarrito(CarritoRequest request, StreamObserver<CarritoResponse> responseObserver) {
        gui.log("Recibido carrito de: " + request.getUsuarioId());

        if (request.getItemsCount() == 0) {
            enviarError(responseObserver, "El carrito está vacío.");
            return;
        }

        double subtotal = 0;

        // Primera pasada: Validaciones de stock, precio y cantidad
        for (Producto p : request.getItemsList()) {
            if (p.getCantidad() <= 0 || p.getPrecio() <= 0) {
                enviarError(responseObserver, "Cantidades y precios deben ser mayores a cero.");
                return;
            }
            int stockActual = inventario.getOrDefault(p.getId(), 0);
            if (stockActual < p.getCantidad()) {
                enviarError(responseObserver, "Inventario insuficiente para: " + p.getNombre());
                return;
            }
            subtotal += p.getPrecio() * p.getCantidad();
        }

        // Segunda pasada: Descontar inventario real
        for (Producto p : request.getItemsList()) {
            inventario.put(p.getId(), inventario.get(p.getId()) - p.getCantidad());
        }

        gui.actualizarInventario(inventario); // Actualizar UI

        double impuestos = subtotal * 0.16;
        double total = subtotal + impuestos;

        CarritoResponse response = CarritoResponse.newBuilder()
                .setTransaccionId(UUID.randomUUID().toString())
                .setTotalNeto(subtotal)
                .setImpuestos(impuestos)
                .setTotalPagar(total)
                .setEstado("EXITOSO")
                .setMensaje("Compra realizada con éxito")
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
        gui.log("Compra procesada con éxito. ID: " + response.getTransaccionId());
    }

    private void enviarError(StreamObserver<CarritoResponse> responseObserver, String mensaje) {
        CarritoResponse response = CarritoResponse.newBuilder()
                .setEstado("ERROR")
                .setMensaje(mensaje)
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
        gui.log("Error en orden: " + mensaje);
    }
}