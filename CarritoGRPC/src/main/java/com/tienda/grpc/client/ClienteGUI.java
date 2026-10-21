package com.tienda.grpc.client;

import com.tienda.grpc.*;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class ClienteGUI extends JFrame {
    private ManagedChannel channel;
    private CarritoServiceGrpc.CarritoServiceBlockingStub stub;
    private List<Producto> carrito = new ArrayList<>();
    private JComboBox<String> cbProductos;
    private List<Producto> catalogoCache = new ArrayList<>();
    private JTextArea txtCarrito;

    public ClienteGUI() {
        // Conexión gRPC
        channel = ManagedChannelBuilder.forAddress("localhost", 50051).usePlaintext().build();
        stub = CarritoServiceGrpc.newBlockingStub(channel);

        setTitle("Cliente Tienda gRPC");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        JPanel panelSuperior = new JPanel();
        cbProductos = new JComboBox<>();
        JButton btnAgregar = new JButton("Agregar al Carrito");
        panelSuperior.add(cbProductos);
        panelSuperior.add(btnAgregar);

        txtCarrito = new JTextArea();
        txtCarrito.setEditable(false);

        JButton btnComprar = new JButton("Comprar");

        add(panelSuperior, BorderLayout.NORTH);
        add(new JScrollPane(txtCarrito), BorderLayout.CENTER);
        add(btnComprar, BorderLayout.SOUTH);

        cargarCatalogo();

        btnAgregar.addActionListener(e -> agregarAlCarrito());
        btnComprar.addActionListener(e -> procesarCompra());
    }

    private void cargarCatalogo() {
        try {
            CatalogoResponse response = stub.obtenerCatalogo(Empty.newBuilder().build());
            for (Producto p : response.getProductosList()) {
                catalogoCache.add(p);
                cbProductos.addItem(p.getNombre() + " - $" + p.getPrecio());
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error conectando al servidor");
        }
    }

    private void agregarAlCarrito() {
        int index = cbProductos.getSelectedIndex();
        if (index >= 0) {
            Producto prodSeleccionado = catalogoCache.get(index);
            String strCant = JOptionPane.showInputDialog("Cantidad:");
            if (strCant != null && !strCant.isEmpty()) {
                int cant = Integer.parseInt(strCant);
                Producto pItem = Producto.newBuilder(prodSeleccionado).setCantidad(cant).build();
                carrito.add(pItem);
                txtCarrito.append(pItem.getNombre() + " x" + cant + "\n");
            }
        }
    }

    private void procesarCompra() {
        CarritoRequest.Builder requestBuilder = CarritoRequest.newBuilder().setUsuarioId("USER-" + System.currentTimeMillis());
        carrito.forEach(requestBuilder::addItems);

        CarritoResponse response = stub.procesarCarrito(requestBuilder.build());

        if ("EXITOSO".equals(response.getEstado())) {
            JOptionPane.showMessageDialog(this,
                    "Compra Exitosa!\nTotal: $" + response.getTotalPagar(),
                    "Éxito", JOptionPane.INFORMATION_MESSAGE);
            carrito.clear();
            txtCarrito.setText("");
        } else {
            JOptionPane.showMessageDialog(this,
                    "Error: " + response.getMensaje(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ClienteGUI().setVisible(true));
    }
}