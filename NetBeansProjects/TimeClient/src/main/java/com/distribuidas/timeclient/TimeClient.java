package com.distribuidas.timeclient;

import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class TimeClient {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(TimeClient::createAndShowGUI);
    }

    private static void createAndShowGUI() {
        // Crear la ventana principal
        JFrame frame = new JFrame("Time Client");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 200);

        // Crear un panel para la interfaz
        JPanel panel = new JPanel(new BorderLayout());

        // Dropdown para seleccionar zona horaria
        String[] timeZones = ZoneId.getAvailableZoneIds().toArray(new String[0]);
        JComboBox<String> timeZoneDropdown = new JComboBox<>(timeZones);
        timeZoneDropdown.setSelectedItem("UTC");

        // Etiqueta para mostrar la hora
        JLabel timeLabel = new JLabel("Hora sincronizada: ", SwingConstants.CENTER);
        timeLabel.setFont(new Font("Arial", Font.BOLD, 16));

        // Botón para sincronizar
        JButton syncButton = new JButton("Sincronizar");
        syncButton.addActionListener(e -> {
            String selectedZone = (String) timeZoneDropdown.getSelectedItem();
            String serverTime = getServerTime();
            if (serverTime != null) {
                try {
                    LocalDateTime serverDateTime = LocalDateTime.parse(serverTime, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                    ZonedDateTime zonedDateTime = serverDateTime.atZone(ZoneId.of("UTC"))
                            .withZoneSameInstant(ZoneId.of(selectedZone));
                    timeLabel.setText("Hora sincronizada: " + zonedDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                } catch (Exception ex) {
                    timeLabel.setText("Error al procesar la hora del servidor");
                    ex.printStackTrace();
                }
            } else {
                timeLabel.setText("Error al sincronizar con el servidor");
            }
        });

        // Agregar componentes al panel
        panel.add(timeZoneDropdown, BorderLayout.NORTH);
        panel.add(timeLabel, BorderLayout.CENTER);
        panel.add(syncButton, BorderLayout.SOUTH);

        // Configurar la ventana
        frame.add(panel);
        frame.setVisible(true);
    }

    private static String getServerTime() {
        String serverAddress = "localhost"; // Dirección del servidor de tiempo
        int port = 12345; // Puerto del servidor de tiempo

        try (Socket socket = new Socket(serverAddress, port);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            // Leer el tiempo desde el servidor
            return in.readLine();

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}