package com.espe.berkeley;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Server {
    private static final int PORT = 12345;

    public static void main(String[] args) {
        new Server().start();
    }

    public void start() {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Servidor iniciado, esperando conexiones...");

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Cliente conectado desde " + clientSocket.getInetAddress());
                new Thread(() -> handleClient(clientSocket)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleClient(Socket clientSocket) {
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);

            // Recibir horas de los relojes
            List<Long> times = new ArrayList<>();
            for (int i = 0; i < 3; i++) {
                long time = Long.parseLong(in.readLine());
                System.out.println("Hora recibida del cliente: " + time);
                times.add(time);
            }

            // Calcular promedio
            long average = times.stream().mapToLong(Long::longValue).sum() / times.size();
            System.out.println("Promedio calculado: " + average + " segundos");

            // Calcular ajustes y enviar
            for (long time : times) {
                long adjustment = average - time;
                System.out.println("Ajuste enviado al cliente: " + adjustment);
                out.println(adjustment);
            }

            clientSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
