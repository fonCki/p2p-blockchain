package com.fonCki.p2pblockchain;


import com.fonCki.p2pblockchain.network.CommandProcessor;
import com.fonCki.p2pblockchain.network.ConnectionManager;
import com.fonCki.p2pblockchain.network.NetworkConfig;
import com.fonCki.p2pblockchain.network.model.payload.NodeInfo;
import com.fonCki.p2pblockchain.state.AppState;
import com.fonCki.p2pblockchain.network.threads.ServerThread;
import com.fonCki.p2pblockchain.utils.PeriodicTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Scanner;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class.getName());
    private static AppState appState = AppState.getInstance();
    private static ConnectionManager connectionManager = new ConnectionManager();

    public static void main(String[] args) {
        //Clear the console and State
        System.out.print("\033[H\033[2J");

        //Start State
        appState.clear();


        //STEP 1: INITIALIZE THE SERVER THREAD
        ServerThread serverThread = new ServerThread();
        serverThread.start();

        //set my node info payload to broadcast to the network with the welcome message
        appState.setMyNodeInfo(new NodeInfo(NetworkConfig.SERVER_IP,
                                            NetworkConfig.SERVER_PORT));

        logger.info("MY NODE ID: " + appState.getMyNodeInfo().getNodeId());

        //Add myself to the node info table (Node Info Map is the table with all the nodes)
        appState.addNodeInfoMap(appState.getMyNodeInfo());

        //STEP 2: CONNECT TO THE NODES TO THE MASTER NODE, AND FETCH THE NODE INFO TABLE

        //STEP 2.1: CONNECT TO THE MASTER NODE, AND FETCH THE NODE INFO TABLE
        //IF THE MASTER NODE REJECT THEN CONNECTION, STILL SEND ME THE NODE INFO TABLE
        connectionManager.initConnection();

        //STEP 2.2: CONNECT TO THE NODES IN THE NODE INFO TABLE (MAX_CONNECTIONS)
        connectionManager.establishConnections();

        //STEP 3: RUN THE COMMAND PROMPT
        runCommandPrompt();

        runScheduledTask();
    }

    private static void runScheduledTask() {
        PeriodicTask periodicTask = new PeriodicTask();
        ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
        executorService.scheduleAtFixedRate(periodicTask, 0, 5, TimeUnit.SECONDS);

        // Keep your application running to let the task execute
        try {
            Thread.sleep(30000); // wait for 30 seconds
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        executorService.shutdown();
    }


    private static void runCommandPrompt() {
        CommandProcessor commandProcessor = new CommandProcessor(connectionManager);
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.print("Enter your message: ");
            String userInput = scanner.nextLine();
            commandProcessor.process(userInput);
        }
    }
}