package com.distribuidas.timeserver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.ZoneId;

public class TimeServer {

    public static void main(String[] args) {
        int port = 12345; // Puerto del servidor de tiempo

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Servidor de tiempo iniciado en el puerto " + port + "...");

            while (true) {
                // Esperar conexiones de clientes
                Socket clientSocket = serverSocket.accept();
                System.out.println("Cliente conectado: " + clientSocket.getInetAddress());

                // Crear un hilo para manejar la conexión del cliente
                new Thread(() -> handleClient(clientSocket)).start();
            }
        } catch (IOException e) {
            System.err.println("Error al iniciar el servidor: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void handleClient(Socket clientSocket) {
        try (Socket socket = clientSocket;
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {

            // Leer la solicitud del cliente
            String request = in.readLine();
            if ("SYNC".equals(request)) {
                // Enviar la hora actual en UTC
                ZonedDateTime nowUtc = ZonedDateTime.now(ZoneId.of("UTC"));
                String formattedTime = nowUtc.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                out.println(formattedTime);
                System.out.println("Hora enviada al cliente (UTC): " + formattedTime);
            }

        } catch (IOException e) {
            System.err.println("Error al manejar la conexión del cliente: " + e.getMessage());
            e.printStackTrace();
        } finally {
            System.out.println("Cliente desconectado: " + clientSocket.getInetAddress());
        }
    }
}
