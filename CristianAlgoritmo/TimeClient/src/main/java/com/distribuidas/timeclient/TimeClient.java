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
        frame.setSize(600, 400);
        frame.setLayout(new BorderLayout());

        // Panel principal
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(new Color(250, 220, 235));

        // Panel superior para selección de zona horaria
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        topPanel.setBackground(new Color(245, 190, 210));
        String[] timeZones = ZoneId.getAvailableZoneIds().toArray(new String[0]);
        JComboBox<String> timeZoneDropdown = new JComboBox<>(timeZones);
        timeZoneDropdown.setSelectedItem("UTC");
        timeZoneDropdown.setFont(new Font("Arial", Font.PLAIN, 14));
        timeZoneDropdown.setBackground(new Color(255, 182, 193));
        timeZoneDropdown.setForeground(Color.DARK_GRAY);

        JLabel titleLabel = new JLabel("Seleccione Zona Horaria");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        titleLabel.setForeground(new Color(102, 0, 51));

        topPanel.add(titleLabel);
        topPanel.add(timeZoneDropdown);

        // Panel central para mostrar la hora sincronizada
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBackground(new Color(250, 220, 235));
        JLabel timeLabel = new JLabel("Hora sincronizada: --", SwingConstants.CENTER);
        timeLabel.setFont(new Font("Arial", Font.BOLD, 18));
        timeLabel.setForeground(new Color(102, 0, 51));
        centerPanel.add(timeLabel, BorderLayout.CENTER);

        // Lista para almacenar horas sincronizadas
        DefaultListModel<String> listModel = new DefaultListModel<>();
        JList<String> timeList = new JList<>(listModel);
        timeList.setFont(new Font("Arial", Font.PLAIN, 14));
        timeList.setBackground(new Color(255, 240, 245));
        timeList.setBorder(BorderFactory.createTitledBorder("Historial de horas sincronizadas"));

        JScrollPane listScrollPane = new JScrollPane(timeList);
        listScrollPane.setPreferredSize(new Dimension(200, 150));
        centerPanel.add(listScrollPane, BorderLayout.SOUTH);

        // Botón de sincronización
        JButton syncButton = new JButton("Sincronizar");
        syncButton.setFont(new Font("Arial", Font.BOLD, 16));
        syncButton.setBackground(new Color(255, 105, 180));
        syncButton.setForeground(Color.WHITE);
        syncButton.setFocusPainted(false);
        syncButton.setBorder(BorderFactory.createLineBorder(new Color(204, 0, 102), 2));
        syncButton.addActionListener(e -> {
            String selectedZone = (String) timeZoneDropdown.getSelectedItem();
            String adjustedTime = synchronizeTime(selectedZone);
            if (!adjustedTime.startsWith("Error")) {
                timeLabel.setText("Hora sincronizada: " + adjustedTime);
                // Agregar hora y zona horaria al historial
                listModel.addElement("Hora: " + adjustedTime + " | Zona Horaria: " + selectedZone);
            } else {
                timeLabel.setText(adjustedTime);
            }
        });

        // Panel inferior para el botón
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        bottomPanel.setBackground(new Color(245, 190, 210));
        bottomPanel.add(syncButton);

        // Agregar paneles al marco principal
        mainPanel.add(topPanel, BorderLayout.NORTH);
        mainPanel.add(centerPanel, BorderLayout.CENTER);
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        frame.add(mainPanel);
        frame.setVisible(true);
    }

    private static String synchronizeTime(String selectedZone) {
        String serverAddress = "localhost"; // Dirección del servidor de tiempo
        int port = 12345; // Puerto del servidor de tiempo

        try (Socket socket = new Socket(serverAddress, port);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {

            // Tiempo T1: antes de enviar la solicitud al servidor
            long T1 = System.currentTimeMillis();

            // Enviar una solicitud vacía al servidor
            out.println("SYNC");

            // Leer la hora del servidor
            String serverTimeString = in.readLine();

            // Tiempo T2: después de recibir la respuesta
            long T2 = System.currentTimeMillis();

            // Parsear la hora del servidor
            LocalDateTime serverDateTime = LocalDateTime.parse(serverTimeString, DateTimeFormatter.ISO_LOCAL_DATE_TIME);

            // Calcular RTT (Round Trip Time)
            long RTT = T2 - T1;

            // Ajustar la hora del servidor con el RTT/2
            ZonedDateTime serverTimeUtc = serverDateTime.atZone(ZoneId.of("UTC"));
            ZonedDateTime adjustedTime = serverTimeUtc.plusNanos((RTT / 2) * 1_000_000);

            // Convertir a la zona horaria seleccionada
            ZonedDateTime adjustedTimeInZone = adjustedTime.withZoneSameInstant(ZoneId.of(selectedZone));

            return adjustedTimeInZone.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        } catch (IOException e) {
            e.printStackTrace();
            return "Error al sincronizar";
        }
    }
}
