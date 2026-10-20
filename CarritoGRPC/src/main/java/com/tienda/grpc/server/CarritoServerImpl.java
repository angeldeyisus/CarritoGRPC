package com.tienda.grpc.server;

import com.tienda.grpc.CarritoRequest;
import com.tienda.grpc.CarritoResponse;
import com.tienda.grpc.CarritoServiceGrpc.CarritoServiceImplBase;
import com.tienda.grpc.Producto;
import io.grpc.stub.StreamObserver;

import java.util.UUID;

public class CarritoServerImpl extends CarritoServiceImplBase{
    @Override
    public void procesarCarrito(CarritoRequest request, StreamObserver<CarritoResponse> responseObserver) {
        System.out.println("Procesando carrito para el usuario: " + request.getUsuarioId());

        double subtotal = 0;

        for (Producto p : request.getItemsList())
            subtotal += p.getPrecio() * p.getCantidad();
        double impuestos = subtotal * 0.16;
        double total = subtotal + impuestos;

        CarritoResponse response = CarritoResponse.newBuilder()
                .setTransaccionId(UUID.randomUUID().toString())
                .setTotalNeto(subtotal)
                .setImpuestos(impuestos)
                .setTotalPagar(total)
                .setEstado("EXITOSO")
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}
