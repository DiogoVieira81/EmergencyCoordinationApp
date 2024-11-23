package application;

import client.Client;
import enums.OperationType;
import enums.UserRole;
import models.Channel;
import models.Message;
import models.Notification;

import java.io.IOException;
import java.util.List;
import java.util.Scanner;

public class EmergencyCoordinationCLI {
    private Client client;
    private Scanner scanner;
    private boolean isRunning;

    public EmergencyCoordinationCLI(Client client) {
        this.client = client;
        this.scanner = new Scanner(System.in);
        this.isRunning = true;
    }

    public void run() {
        System.out.println("==== Sistema de Coordenação de Emergência ====");
        while (isRunning) {
            if (client.getCurrentUser() == null) {
                showLoginMenu();
            } else {
                showMainMenu();
            }
        }
    }

    private void showLoginMenu() {
        System.out.println("\n1. Login");
        System.out.println("0. Sair");
        System.out.print("Escolha uma opção: ");
        String choice = scanner.nextLine();

        switch (choice) {
            case "1":
                login();
                break;
            case "0":
                System.exit(0);
                break;
            default:
                System.out.println("Opção inválida.");
        }
    }

    private void register() {
        System.out.println("\n== Registo de Novo Utilizador ==");
        System.out.print("Nome: ");
        String name = scanner.nextLine();
        System.out.print("Senha: ");
        String password = scanner.nextLine();
        System.out.print("Perfil (LOW_LEVEL, MID_LEVEL, HIGH_LEVEL, ADMIN): ");
        String role = scanner.nextLine().toUpperCase();

        try {
            UserRole userRole = UserRole.valueOf(role);
            client.registerUser(name, password, userRole);
            System.out.println("Registo concluído com sucesso!");
        } catch (IllegalArgumentException | IOException e) {
            System.out.println("Falha no registo: " + e.getMessage());
        }
    }

    private void login() {
        try {
            System.out.println("\n== Login ==");
            System.out.print("Nome de utilizador: ");
            String username = scanner.nextLine();
            System.out.print("Senha: ");
            String password = scanner.nextLine();


            if (client.login(username, password)) {
                System.out.println("Login bem-sucedido!");
            } else {
                System.out.println("Falha no login.");
            }
        } catch (IOException e) {
            System.out.println("Erro ao conectar: " + e.getMessage());
        }
    }

    private void showMainMenu() {
        System.out.println("\n== Menu Principal ==");
        if (client.getCurrentUser().getRole() == UserRole.ADMIN) {
            System.out.println("1. Registar Novo Utilizador");
        }
        System.out.println("2. Enviar Mensagem");
        System.out.println("3. Criar Canal");
        System.out.println("4. Entrar em Canal");
        System.out.println("5. Listar Canais");
        System.out.println("6. Iniciar Operação");
        System.out.println("7. Aprovar Operação");
        System.out.println("8. Ver Notificações");
        System.out.println("9. Logout");
        System.out.print("Escolha uma opção: ");
        String choice = scanner.nextLine();

        try {
            int offset = client.getCurrentUser().getRole() == UserRole.ADMIN ? 0 : 1;
            switch (Integer.parseInt(choice) + offset) {
                case 1:
                    register();
                    break;
                case 2:
                    sendMessage();
                    break;
                case 3:
                    createChannel();
                    break;
                case 4:
                    joinChannel();
                    break;
                case 5:
                    listChannels();
                    break;
                case 6:
                    initiateOperation();
                    break;
                case 7:
                    approveOperation();
                    break;
                case 8:
                    viewNotifications();
                    break;
                case 9:
                    logout();
                    break;
                default:
                    System.out.println("Opção inválida.");
            }
        } catch (IOException | NumberFormatException | ClassNotFoundException e) {
            System.out.println("Erro: " + e.getMessage());
        }
    }


    private void sendMessage() throws IOException {
        System.out.print("Destinatário (usuário ou canal): ");
        String recipient = scanner.nextLine();
        System.out.print("Mensagem: ");
        String content = scanner.nextLine();

        Message message = new Message(client.getCurrentUser().getId(), recipient, null, content, Message.MessageType.DIRECT);
        client.sendMessage(message);
        System.out.println("Mensagem enviada com sucesso!");
    }

    private void createChannel() throws IOException {
        System.out.print("Nome do Canal: ");
        String name = scanner.nextLine();
        System.out.print("Descrição do Canal: ");
        String description = scanner.nextLine();
        System.out.print("É um canal de emergência? (s/n): ");
        boolean isEmergency = scanner.nextLine().equalsIgnoreCase("s");

        if (client.createChannel(name, description, client.getCurrentUser(), isEmergency)) {
            System.out.println("Canal criado com sucesso!");
        } else {
            System.out.println("Falha ao criar o canal.");
        }
    }

    private void joinChannel() throws IOException {
        System.out.print("ID do Canal: ");
        String channelId = scanner.nextLine();

        client.joinChannel(channelId);
        System.out.println("Entrou no canal com sucesso!");
    }

    private void listChannels() throws IOException {
        List<Channel> channels = client.requestChannelList();
        System.out.println("=== Lista de Canais ===");
        for (Channel channel : channels) {
            System.out.println(channel.getId() + ": " + channel.getName() + " - Emergência: " + (channel.isEmergencyChannel() ? "Sim" : "Não"));
        }
    }

    private void initiateOperation() throws IOException {
        System.out.println("\n== Iniciar Nova Operação ==");
        System.out.print("Nome da Operação: ");
        String name = scanner.nextLine();
        System.out.print("Descrição: ");
        String description = scanner.nextLine();
        System.out.print("Tipo (mass_evacuation, emergency_communications_activation, emergency_resources_distribution): ");
        String typeInput = scanner.nextLine();

        try {
            OperationType type = OperationType.valueOf(typeInput);
            client.initiateOperation(name, description, type);
            System.out.println("Operação iniciada com sucesso!");
        } catch (IllegalArgumentException e) {
            System.out.println("Tipo de operação inválido.");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private void approveOperation() throws IOException {
        System.out.println("\n== Aprovar Operação ==");
        System.out.print("ID da Operação: ");
        String operationId = scanner.nextLine();

        client.approveOperation(operationId);
        System.out.println("Operação aprovada com sucesso!");
    }

    private void viewNotifications() throws IOException, ClassNotFoundException {
        List<Notification> notifications = client.getNotifications();
        System.out.println("=== Notificações ===");
        for (Notification notification : notifications) {
            System.out.println(notification.getId() + ": " + notification.getTitle() + " - " + notification.getContent());
        }
    }

    private void logout() throws IOException, ClassNotFoundException {
        client.logout();
        System.out.println("Logout realizado com sucesso.");
    }

    public static void main(String[] args) {
        Client client = new Client("localhost", 8080);
        EmergencyCoordinationCLI cli = new EmergencyCoordinationCLI(client);
        cli.run();
    }
}
