package com.espe.berkeley;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.Socket;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;

public class ClockClient {
    private static final String SERVER_ADDRESS = "localhost";
    private static final int SERVER_PORT = 12345;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            // Crear ventana principal
            JFrame frame = new JFrame("Algoritmo de Berkeley - Cliente");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(850, 500);
            frame.getContentPane().setBackground(new Color(240, 248, 255)); // Fondo suave

            // Crear panel principal
            JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
            mainPanel.setOpaque(false);

            // Panel para selección de zonas horarias
            JPanel selectionPanel = new JPanel(new GridLayout(4, 2, 10, 10));
            selectionPanel.setBorder(BorderFactory.createTitledBorder(
                    BorderFactory.createLineBorder(new Color(100, 149, 237), 2),
                    "Seleccione zona horaria para cada reloj",
                    0,
                    0,
                    new Font("Arial", Font.BOLD, 14),
                    new Color(25, 25, 112)
            ));
            selectionPanel.setOpaque(false);

            String[] zones = ZoneId.getAvailableZoneIds().toArray(new String[0]);
            Arrays.sort(zones);

            JComboBox<String> zoneSelector1 = new JComboBox<>(zones);
            JComboBox<String> zoneSelector2 = new JComboBox<>(zones);
            JComboBox<String> zoneSelector3 = new JComboBox<>(zones);

            selectionPanel.add(new JLabel("Reloj 1:"));
            selectionPanel.add(zoneSelector1);
            selectionPanel.add(new JLabel("Reloj 2:"));
            selectionPanel.add(zoneSelector2);
            selectionPanel.add(new JLabel("Reloj 3:"));
            selectionPanel.add(zoneSelector3);

            JButton syncButton = new JButton("Sincronizar");
            syncButton.setBackground(new Color(30, 144, 255));
            syncButton.setForeground(Color.WHITE);
            syncButton.setFont(new Font("Arial", Font.BOLD, 14));
            syncButton.setFocusPainted(false);
            syncButton.setBorder(BorderFactory.createRaisedBevelBorder());
            selectionPanel.add(new JLabel()); // Espacio vacío
            selectionPanel.add(syncButton);

            // Panel para mostrar horas antes de sincronizar
            JPanel beforePanel = new JPanel(new GridLayout(4, 1, 10, 10));
            beforePanel.setBorder(BorderFactory.createTitledBorder(
                    BorderFactory.createLineBorder(new Color(70, 130, 180), 2),
                    "Horas antes de la sincronización",
                    0,
                    0,
                    new Font("Arial", Font.BOLD, 14),
                    new Color(25, 25, 112)
            ));
            beforePanel.setOpaque(false);

            JLabel originalClock1Label = new JLabel("Reloj 1 (antes): No sincronizado");
            JLabel originalClock2Label = new JLabel("Reloj 2 (antes): No sincronizado");
            JLabel originalClock3Label = new JLabel("Reloj 3 (antes): No sincronizado");

            Font labelFont = new Font("Arial", Font.PLAIN, 12);
            originalClock1Label.setFont(labelFont);
            originalClock2Label.setFont(labelFont);
            originalClock3Label.setFont(labelFont);

            beforePanel.add(originalClock1Label);
            beforePanel.add(originalClock2Label);
            beforePanel.add(originalClock3Label);

            // Panel para mostrar horas después de sincronizar
            JPanel afterPanel = new JPanel(new GridLayout(4, 1, 10, 10));
            afterPanel.setBorder(BorderFactory.createTitledBorder(
                    BorderFactory.createLineBorder(new Color(70, 130, 180), 2),
                    "Horas después de la sincronización",
                    0,
                    0,
                    new Font("Arial", Font.BOLD, 14),
                    new Color(25, 25, 112)
            ));
            afterPanel.setOpaque(false);

            JLabel syncedClock1Label = new JLabel("Reloj 1 (después): No sincronizado");
            JLabel syncedClock2Label = new JLabel("Reloj 2 (después): No sincronizado");
            JLabel syncedClock3Label = new JLabel("Reloj 3 (después): No sincronizado");

            syncedClock1Label.setFont(labelFont);
            syncedClock2Label.setFont(labelFont);
            syncedClock3Label.setFont(labelFont);

            afterPanel.add(syncedClock1Label);
            afterPanel.add(syncedClock2Label);
            afterPanel.add(syncedClock3Label);

            // Acción del botón de sincronización
            syncButton.addActionListener(e -> {
                try (Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT)) {
                    System.out.println("Conectado al servidor");

                    ZoneId zone1 = ZoneId.of((String) zoneSelector1.getSelectedItem());
                    ZoneId zone2 = ZoneId.of((String) zoneSelector2.getSelectedItem());
                    ZoneId zone3 = ZoneId.of((String) zoneSelector3.getSelectedItem());

                    ZonedDateTime dateTime1 = ZonedDateTime.now(zone1);
                    ZonedDateTime dateTime2 = ZonedDateTime.now(zone2);
                    ZonedDateTime dateTime3 = ZonedDateTime.now(zone3);

                    // Formateador con 6 decimales para los segundos
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss.SSSSSS");

                    originalClock1Label.setText("Reloj 1 (antes): " + dateTime1.format(formatter) + " (" + zone1 + ")");
                    originalClock2Label.setText("Reloj 2 (antes): " + dateTime2.format(formatter) + " (" + zone2 + ")");
                    originalClock3Label.setText("Reloj 3 (antes): " + dateTime3.format(formatter) + " (" + zone3 + ")");

                    long seconds1 = dateTime1.toLocalTime().toSecondOfDay();
                    long seconds2 = dateTime2.toLocalTime().toSecondOfDay();
                    long seconds3 = dateTime3.toLocalTime().toSecondOfDay();

                    PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                    out.println(seconds1);
                    out.println(seconds2);
                    out.println(seconds3);

                    BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    long adjustment1 = Long.parseLong(in.readLine());
                    long adjustment2 = Long.parseLong(in.readLine());
                    long adjustment3 = Long.parseLong(in.readLine());

                    ZonedDateTime adjustedTime1 = dateTime1.plusSeconds(adjustment1);
                    ZonedDateTime adjustedTime2 = dateTime2.plusSeconds(adjustment2);
                    ZonedDateTime adjustedTime3 = dateTime3.plusSeconds(adjustment3);

                    syncedClock1Label.setText("Reloj 1 (después): " + adjustedTime1.format(formatter) + " (" + zone1 + ")");
                    syncedClock2Label.setText("Reloj 2 (después): " + adjustedTime2.format(formatter) + " (" + zone2 + ")");
                    syncedClock3Label.setText("Reloj 3 (después): " + adjustedTime3.format(formatter) + " (" + zone3 + ")");
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            });

            // Agregar todo al panel principal
            mainPanel.add(selectionPanel, BorderLayout.NORTH);
            mainPanel.add(beforePanel, BorderLayout.CENTER);
            mainPanel.add(afterPanel, BorderLayout.SOUTH);

            frame.add(mainPanel);
            frame.setVisible(true);
        });
    }
}
