package com.tienda.grpc.client;

import com.tienda.grpc.CarritoRequest;
import com.tienda.grpc.CarritoResponse;
import com.tienda.grpc.CarritoServiceGrpc;
import com.tienda.grpc.Producto;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

public class CarritoGRPCclient {
    public static void main(String[] args) {
        ManagedChannel channel = ManagedChannelBuilder
                .forAddress("localhost", 50051)
                .usePlaintext()
                .build();

        CarritoServiceGrpc.CarritoServiceBlockingStub stub = CarritoServiceGrpc.newBlockingStub(channel);
        Producto p1 = Producto.newBuilder()
                .setId("PROD-001").setNombre("Laptop Gamer")
                .setPrecio(1200.00).setCantidad(1).build();

        Producto p2 = Producto.newBuilder()
                .setId("PROD-002").setNombre("Mouse Óptico")
                .setPrecio(25.50).setCantidad(2).build();

        CarritoRequest request = CarritoRequest.newBuilder()
                .setUsuarioId("USER-123")
                .addItems(p1) // Agregamos productos individualmente
                .addItems(p2)
                .build();

        System.out.println("Enviando carrito al servidor...");
        CarritoResponse response = stub.procesarCarrito(request);

        System.out.println("--- Factura Generada ---");
        System.out.println("ID Transacción: " + response.getTransaccionId());
        System.out.println("Subtotal: $" + response.getTotalNeto());
        System.out.println("Impuestos (16%): $" + response.getImpuestos());
        System.out.println("TOTAL A PAGAR: $" + response.getTotalPagar());

        channel.shutdown();
    }
}
