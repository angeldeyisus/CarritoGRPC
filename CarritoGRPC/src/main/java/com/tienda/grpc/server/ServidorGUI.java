package com.tienda.grpc.server;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import javax.swing.*;
import java.awt.*;
import java.util.Map;

public class ServidorGUI extends JFrame {
    private JTextArea txtLog;
    private JTextArea txtInventario;

    public ServidorGUI() {
        setTitle("Servidor gRPC - Inventario");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new GridLayout(1, 2));

        txtLog = new JTextArea();
        txtLog.setEditable(false);
        txtInventario = new JTextArea();
        txtInventario.setEditable(false);

        add(new JScrollPane(txtLog));
        add(new JScrollPane(txtInventario));

        iniciarServidor();
    }

    public void log(String msj) {
        txtLog.append(msj + "\n");
    }

    public void actualizarInventario(Map<String, Integer> inv) {
        StringBuilder sb = new StringBuilder("--- INVENTARIO ---\n");
        inv.forEach((k, v) -> sb.append(k).append(" : ").append(v).append(" unidades\n"));
        txtInventario.setText(sb.toString());
    }

    private void iniciarServidor() {
        new Thread(() -> {
            try {
                Server server = ServerBuilder.forPort(50051)
                        .addService(new CarritoServiceImpl(this))
                        .build().start();
                log("Servidor iniciado en puerto 50051");
                server.awaitTermination();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ServidorGUI().setVisible(true));
    }
}