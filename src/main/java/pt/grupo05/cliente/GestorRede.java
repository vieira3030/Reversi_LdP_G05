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

    private Socket socket;
    private ObjectOutputStream saida;
    private ObjectInputStream entrada;
    private App appPrincipal; 

    public GestorRede(App app) {
        this.appPrincipal = app;
    }

    /**
     * Tenta ligar como cliente. Se falhar, inicia como servidor e sincroniza identidades.
     */
    public void iniciarConexao(String ip, int porta) {
        new Thread(() -> {
            boolean servidor = false; 
            
            try {
                String ipLimpo = ip.trim(); 
                Platform.runLater(() -> appPrincipal.atualizarMensagem("A tentar ligar a " + ipLimpo + "..."));
                socket = new Socket(ipLimpo, porta);
                servidor = false; 
                Platform.runLater(() -> appPrincipal.atualizarMensagem("🟢 Ligado! A sincronizar nomes..."));
            } catch (Exception e) {
                try {
                    Platform.runLater(() -> appPrincipal.atualizarMensagem("🟡 A aguardar que o adversário se ligue..."));
                    ServerSocket serverSocket = new ServerSocket(porta);
                    socket = serverSocket.accept(); 
                    servidor = true; 
                    Platform.runLater(() -> appPrincipal.atualizarMensagem("🟢 Adversário chegou! A sincronizar nomes..."));
                } catch (Exception ex) {
                    Platform.runLater(() -> appPrincipal.atualizarMensagem("🔴 Erro de rede. Tenta reiniciar."));
                    return;
                }
            }

            final boolean isServidor = servidor;

            try {
                // Prepara canais de comunicação de rede.
                saida = new ObjectOutputStream(socket.getOutputStream());
                saida.flush(); // Fundamental para não bloquear a stream inicial.
                entrada = new ObjectInputStream(socket.getInputStream());
                
                // Envia o nome local para a rede.
                saida.writeObject(appPrincipal.getMeuNome());
                saida.flush();
                
                // Recebe o nome do adversário.
                String nomeAdversario = (String) entrada.readObject();
                
                // Aplica nomes e atribui a cor na UI.
                Platform.runLater(() -> {
                    appPrincipal.configurarPartida(isServidor, nomeAdversario);
                });

                ouvirRede();
            } catch (Exception e) {
                Platform.runLater(() -> appPrincipal.atualizarMensagem("🔴 A ligação caiu durante a configuração."));
            }
        }).start();
    }

    /**
     * Ciclo contínuo à escuta de coordenadas recebidas.
     */
    private void ouvirRede() {
        while (true) {
            try {
                int[] jogada = (int[]) entrada.readObject();
                int c = jogada[0];
                int l = jogada[1];

                Platform.runLater(() -> {
                    appPrincipal.processarJogadaAdversario(c, l);
                });
            } catch (Exception e) {
                Platform.runLater(() -> appPrincipal.adversarioDesistiu());
                break; 
            }
        }
    }

    /**
     * Envia os índices (coluna, linha) para o oponente.
     */
    public void enviarJogada(int c, int l) {
        if (saida != null) {
            try {
                int[] jogada = {c, l};
                saida.writeObject(jogada);
                saida.flush(); 
            } catch (Exception e) {
                Platform.runLater(() -> appPrincipal.atualizarMensagem("🔴 Erro ao enviar a jogada."));
            }
        }
    }
}