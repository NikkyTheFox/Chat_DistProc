package org.example;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.concurrent.locks.ReentrantLock;

class HandleClient implements Runnable{
    String clientName;
    Socket clientSocket;
    ReentrantLock lock;

    LinkedList<String> ownPipe;

    LinkedList<LinkedList<String>> allPipes;

    Boolean finished;

    HandleClient(Socket socket, ReentrantLock lock, LinkedList<String> messageList, LinkedList<LinkedList<String>> pipes){
        this.clientName = null;
        this.clientSocket = socket;
        this.lock = lock;
        this.ownPipe = messageList;
        this.allPipes = pipes;
        this.finished = false;
    }

    @Override
    public void run() {
        Thread pipeThread = new Thread(new HandlePipeMessages());
        try {
            PrintWriter output = new PrintWriter(clientSocket.getOutputStream(), true);
            BufferedReader input = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            output.println("Hello new user, how do you wanna be called?");
            clientName = input.readLine();
            output.println("Welcome to the chat " + clientName);
            System.out.println("the name of new user is " + clientName);
            pipeThread.start();
            while (true) {
                String message = input.readLine();
                if (message.isEmpty()) {
                    finished = true;
                    break;
                }
                lock.lock();
                String toSend = String.format(clientName + ": " + message);
                for (LinkedList<String> pipe : allPipes) {
                    if (pipe != ownPipe) {
                        try {
                            //dziala
                            pipe.add(toSend);
                        } catch (Exception e) {
                            e.getCause();
                        }
                    }
                }
                lock.unlock();
            }
            try {
                pipeThread.join();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        lock.lock();
        allPipes.remove(ownPipe);
        lock.unlock();
    }

    class HandlePipeMessages implements Runnable{

        @Override
        public void run() {
            PrintWriter output = null;
            try{
                output = new PrintWriter(clientSocket.getOutputStream(), true);
            }
            catch (IOException e) {
                e.getCause();
            }
            for(LinkedList<String> pipe :allPipes){
                if (pipe == ownPipe){
                    System.out.println("im in list of pipes! - " + clientName);
                }
            }
            while(true){
                //dziala
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                lock.lock();
                String message = ownPipe.poll();
                lock.unlock();
                if(message != null) {
                    output.println(message);
                }
                if(finished){
                    System.out.println(clientName + " has disconnected");
                    break;
                }
            }
        }
    }
}

public class Server {
    private ServerSocket socketServer;

    public void start() throws IOException {
        socketServer = new ServerSocket(6969);
        ReentrantLock mutex = new ReentrantLock();
        LinkedList<LinkedList<String>> allPipes = new LinkedList<LinkedList<String>>();
        System.out.println("Server started");
        while (true){
            Socket client = socketServer.accept();
            LinkedList<String> messageList = new LinkedList<String>();
            mutex.lock();
            allPipes.add(messageList);
            mutex.unlock();
            Thread user = new Thread(new HandleClient(client, mutex, messageList, allPipes));
            user.start();
        }
    }
}
