package pt.grupo05.cliente;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import javafx.application.Platform;

/**
 * Gere a ligação de rede entre os dois jogadores usando Sockets.
 */
public class GestorRede {

    // Canal de comunicação principal.
    private Socket socket;
    
    // Canais de envio e receção de dados.
    private ObjectOutputStream saida;
    private ObjectInputStream entrada;
    
    // Referência à interface gráfica para a podermos atualizar.
    private App appPrincipal; 

    /**
     * Inicializa o gestor com a referência à App.
     */
    public GestorRede(App app) {
        this.appPrincipal = app;
    }

    /**
     * Tenta ligar como cliente. Se falhar, inicia como servidor.
     * Utiliza uma Thread para não bloquear o ecrã do jogo.
     */
    public void iniciarConexao(String ip, int porta) {
        new Thread(() -> {
            try {
                // Tenta ligar ao IP fornecido (modo Cliente).
                System.out.println("A tentar ligar ao IP: " + ip);
                socket = new Socket(ip, porta);
                System.out.println("Ligado com sucesso como CLIENTE.");
            } catch (Exception e) {
                // Se falhar, cria o seu próprio servidor (modo Servidor).
                try {
                    System.out.println("Nenhum servidor encontrado. A iniciar como SERVIDOR na porta " + porta + "...");
                    ServerSocket serverSocket = new ServerSocket(porta);
                    
                    // Fica à espera que o adversário se ligue.
                    socket = serverSocket.accept(); 
                    System.out.println("Adversário ligou-se! Ligado como SERVIDOR.");
                } catch (Exception ex) {
                    System.out.println("Erro ao iniciar servidor: " + ex.getMessage());
                    return;
                }
            }

            try {
                // Configura os canais de envio e receção (a saída tem de ser sempre a primeira a iniciar).
                saida = new ObjectOutputStream(socket.getOutputStream());
                entrada = new ObjectInputStream(socket.getInputStream());
                
                // Inicia o ciclo de escuta contínua.
                ouvirRede();
            } catch (Exception e) {
                System.out.println("Erro a configurar as streams de rede: " + e.getMessage());
            }
        }).start();
    }

    /**
     * Fica constantemente à espera de receber as coordenadas do adversário.
     */
    private void ouvirRede() {
        while (true) {
            try {
                // Recebe as coordenadas da jogada num array de inteiros.
                int[] jogada = (int[]) entrada.readObject();
                int c = jogada[0];
                int l = jogada[1];

                // Atualiza o tabuleiro visual no ecrã (obrigatoriamente via Platform.runLater).
                Platform.runLater(() -> {
                    appPrincipal.processarJogadaAdversario(c, l);
                });
            } catch (Exception e) {
                System.out.println("A ligação de rede foi perdida.");
                break; // Sai do ciclo se a ligação cair.
            }
        }
    }

    /**
     * Envia a jogada feita localmente para o adversário.
     */
    public void enviarJogada(int c, int l) {
        if (saida != null) {
            try {
                int[] jogada = {c, l};
                saida.writeObject(jogada);
                saida.flush(); // Garante que os dados são enviados imediatamente.
            } catch (Exception e) {
                System.out.println("Erro ao enviar a jogada pela rede.");
            }
        }
    }
}