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

    public void iniciarConexao(String ip, int porta) {
        new Thread(() -> {
            try {
                // Limpa espaços em branco que tenham sido digitados sem querer
                String ipLimpo = ip.trim(); 
                
                Platform.runLater(() -> appPrincipal.atualizarMensagem("A tentar ligar a " + ipLimpo + "..."));
                socket = new Socket(ipLimpo, porta);
                Platform.runLater(() -> appPrincipal.atualizarMensagem("🟢 Ligado ao adversário como CLIENTE!"));
            } catch (Exception e) {
                try {
                    Platform.runLater(() -> appPrincipal.atualizarMensagem("🟡 A aguardar que o adversário se ligue..."));
                    ServerSocket serverSocket = new ServerSocket(porta);
                    socket = serverSocket.accept(); 
                    Platform.runLater(() -> appPrincipal.atualizarMensagem("🟢 Adversário ligou-se! (SERVIDOR)"));
                } catch (Exception ex) {
                    Platform.runLater(() -> appPrincipal.atualizarMensagem("🔴 Erro de rede. Tenta reiniciar."));
                    return;
                }
            }

            try {
                saida = new ObjectOutputStream(socket.getOutputStream());
                entrada = new ObjectInputStream(socket.getInputStream());
                ouvirRede();
            } catch (Exception e) {
                Platform.runLater(() -> appPrincipal.atualizarMensagem("🔴 A ligação caiu."));
            }
        }).start();
    }

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
                Platform.runLater(() -> appPrincipal.atualizarMensagem("🔴 O adversário saiu do jogo."));
                break; 
            }
        }
    }

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