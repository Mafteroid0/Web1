package ru.mafteroid;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main {
    private static final int PORT = 9000;
    private static final int THREAD_POOL_SIZE = 10;

    public static void main(String[] args) {
//        ExecutorService threadPool = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
//
//        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
//            System.out.println("FastCGI Java server listening on port " + PORT);
//
//            while (true) {
//                Socket clientSocket = serverSocket.accept();
//                threadPool.execute(new FastCGIHandler(clientSocket));
//            }
//
//        } catch (IOException e) {
//            e.printStackTrace();
//        } finally {
//            threadPool.shutdown();
//        }
    }
}