package network;

import network.model.payload.NodeInfo;
import state.AppState;

public class CommandProcessor {
    private ConnectionManager connectionManager;

    AppState appState = AppState.getInstance();

    public CommandProcessor(ConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
    }

    public void process(String command) {
        String[] commandParts = command.split(" ");
        String commandName = commandParts[0];
        String[] commandArgs = new String[commandParts.length - 1];
        System.arraycopy(commandParts, 1, commandArgs, 0, commandParts.length - 1);
        switch (commandName) {
            case "connect":
                connect(commandArgs);
                break;
            case "disconnect":
                disconnect(commandArgs);
                break;
            case "send":
                send(commandArgs);
                break;
            case "broadcast":
                broadcast(commandArgs);
                break;
            case "list":
                list(commandArgs);
                break;
            case "help":
                help(commandArgs);
                break;
            case "exit":
                exit(commandArgs);
                break;
            case "clear":
                System.out.print("\033[H\033[2J");
                break;
            case "status":
                status(commandArgs);
                break;
            default:
                //TODO: ack to avoid put broadcast in a test mode
                connectionManager.broadcastUserMessage(command);
                break;

        }
    }

    private void connect(String[] commandArgs) {
        NodeInfo nodeInfo = appState.getNodeInfo(commandArgs[0]);
        connectionManager.connect(nodeInfo);
    }

    private void disconnect(String[] commandArgs) {
    }

    private void send(String[] commandArgs) {
    }

    private void broadcast(String command) {
    }

    private void broadcast(String[] commandArgs) {
        //join the command args into a single string
        String message = String.join(" ", commandArgs);
        connectionManager.broadcastUserMessage(message);
    }

    private void list(String[] commandArgs) {
    }

    private void help(String[] commandArgs) {
    }

    private void exit(String[] commandArgs) {
    }

    private void status(String[] commandArgs) {
        System.out.println("************ STATUS ************");
        System.out.println("Popularity: " + appState.getMyNodeInfo().getPopularity());
        System.out.println("My Node Info: " + appState.getMyNodeInfo());
        System.out.println("Total Nodes in the network:" + appState.getAllNodeInfoMap().size());
        System.out.println("and they are: ");
        appState.getAllNodeInfoMap().forEach((k, v) -> System.out.println(k + " " + v));
        System.out.println("Nodes I am connected to: " + appState.getAllNodesIamConnectedTo().size());
        System.out.println("and they are: ");
        appState.getAllNodesIamConnectedTo().forEach((k, v) -> System.out.println(k + " " + v));
        System.out.println("Nodes connected to me: " + appState.getAllNodesConnectedToMe().size());
        System.out.println("and they are: ");
        appState.getAllNodesConnectedToMe().forEach((k, v) -> System.out.println(k + " " + v));
        System.out.println("********************************");
    }

}
