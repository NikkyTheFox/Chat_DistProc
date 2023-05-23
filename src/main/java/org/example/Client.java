package org.example;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

public class Client {
    static Socket clientSocket;

    static boolean finishedFlag;

    public static void main(String[] args) throws IOException {
        finishedFlag = false;
        clientSocket = new Socket(InetAddress.getLocalHost().getHostAddress(),6969);
        PrintWriter output = new PrintWriter(clientSocket.getOutputStream(), true);
        //BufferedReader serverInput = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        BufferedReader consoleInput = new BufferedReader(new InputStreamReader(System.in));
        Scanner in = new Scanner(System.in);
        Thread serverInputThread = new Thread(new ServerInputHandler());
        serverInputThread.start();
        while(true){
            String messageToServer = consoleInput.readLine();
            output.println(messageToServer);
            if(messageToServer.isEmpty()){
                finishedFlag = true;
                System.out.printf("XD %s%n", finishedFlag);
                serverInputThread.interrupt();
                System.out.println("Im out cya!");
                System.exit(1);
            }
        }
    }

    static class ServerInputHandler implements Runnable{

        @Override
        public void run() {
            BufferedReader serverInput;
            try {
                serverInput = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            while(true){
                String messageFromSever = null;
                try {
                    messageFromSever = serverInput.readLine();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                if (messageFromSever != null){
                    System.out.println(messageFromSever);
                }
            }
        }
    }

}
