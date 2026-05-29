package pt.grupo05.cliente;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

import pt.grupo05.modelo.Jogo;
import pt.grupo05.modelo.CorPeca;

import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.util.Optional;

public class App extends Application {

    // Instância principal da lógica do jogo.
    private Jogo jogoReversi = new Jogo(); 
    // Grelha visual para representação do tabuleiro.
    private GridPane tabuleiroVisual; 
    
    // Gestor de comunicação em rede (Sockets).
    private GestorRede gestorRede;
    
    // Etiquetas de texto para o painel lateral.
    private Label vezDe;
    private Label lPretas;
    private Label lBrancas;
    
    // Barra de progresso para ocupação das 64 casas.
    private ProgressBar barraProgresso;

    @Override
    public void start(Stage stage) {
        // --- MENU INICIAL (JANELA DE CONEXÃO) ---
        
        Dialog<String[]> dialog = new Dialog<>();
        dialog.setTitle("REVERSI — Ligar ao Servidor");
        dialog.setHeaderText("Configuração de Rede Local");

        ButtonType btnLigar = new ButtonType("Ligar", ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnLigar, ButtonType.CANCEL);

        GridPane gridDialogo = new GridPane();
        gridDialogo.setHgap(10);
        gridDialogo.setVgap(10);
        gridDialogo.setPadding(new Insets(20, 150, 10, 10));

        TextField txtNome = new TextField();
        txtNome.setPromptText("ex: rodrigo");
        
        TextField txtIp = new TextField();
        txtIp.setPromptText("ex: 192.168.1.10 (ou localhost)");
        
        TextField txtPorta = new TextField();
        txtPorta.setPromptText("ex: 8080");

        gridDialogo.add(new Label("Nome do jogador:"), 0, 0);
        gridDialogo.add(txtNome, 1, 0);
        gridDialogo.add(new Label("IP do servidor:"), 0, 1);
        gridDialogo.add(txtIp, 1, 1);
        gridDialogo.add(new Label("Porta:"), 0, 2);
        gridDialogo.add(txtPorta, 1, 2);

        dialog.getDialogPane().setContent(gridDialogo);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == btnLigar) {
                return new String[]{txtNome.getText(), txtIp.getText(), txtPorta.getText()};
            }
            return null;
        });

        Optional<String[]> resultado = dialog.showAndWait();
        if (resultado.isPresent()) {
            String nome = resultado.get()[0];
            String ip = resultado.get()[1];
            // Converte a porta de String para número inteiro
            int porta = Integer.parseInt(resultado.get()[2]); 
            
            jogoReversi.getJogador1().setNome(nome + " (P1)");
            jogoReversi.getJogador2().setNome("Adversário (P2)");
            
            // Inicia o gestor de rede com os dados introduzidos!
            gestorRede = new GestorRede(this);
            gestorRede.iniciarConexao(ip, porta);
            
        } else {
            System.exit(0);
        }

        // --- CONFIGURAÇÃO DA INTERFACE PRINCIPAL ---
        
        HBox layoutPrincipal = new HBox(40); 
        layoutPrincipal.setPadding(new Insets(30));
        layoutPrincipal.setAlignment(Pos.CENTER);
        layoutPrincipal.setStyle("-fx-background-color: #2b2b2b;");

        tabuleiroVisual = new GridPane();
        Color corCasa = Color.DARKGREEN;
        Color corLinha = Color.BLACK;

        for (int linha = 0; linha < 8; linha++) {
            for (int coluna = 0; coluna < 8; coluna++) {
                StackPane casa = new StackPane();
                
                Rectangle fundo = new Rectangle(60, 60);
                fundo.setFill(corCasa);
                fundo.setStroke(corLinha);
                fundo.setStrokeWidth(2); 
                
                casa.getChildren().add(fundo);

                final int l = linha;
                final int c = coluna;

                casa.setOnMouseClicked(evento -> {
                    boolean jogadaValida = jogoReversi.jogar(c, l);
                    if (jogadaValida) {
                        atualizarTabuleiroVisual();
                        
                        // Envia a jogada válida para o adversário pela rede!
                        if (gestorRede != null) {
                            gestorRede.enviarJogada(c, l);
                        }
                        
                        if (!jogoReversi.isJogoAtivo()) {
                            anunciarVencedor();
                        }
                    } else {
                        System.out.println("Jogada inválida!");
                    }
                });

                tabuleiroVisual.add(casa, coluna, linha);
            }
        }

        VBox painelInfo = new VBox(30);
        painelInfo.setAlignment(Pos.TOP_CENTER);
        painelInfo.setMinWidth(250);

        Label titulo = new Label("REVERSI");
        titulo.setStyle("-fx-text-fill: white; -fx-font-size: 34px; -fx-font-weight: bold;");

        vezDe = new Label("VEZ DE: "); 
        vezDe.setStyle("-fx-text-fill: #f1c40f; -fx-font-size: 16px; -fx-font-weight: bold;");

        VBox boxPontos = new VBox(15);
        boxPontos.setAlignment(Pos.CENTER);
        boxPontos.setStyle("-fx-background-color: #3c3f41; -fx-padding: 20; -fx-background-radius: 15;");

        lPretas = new Label();
        lBrancas = new Label();
        lPretas.setStyle("-fx-text-fill: white; -fx-font-size: 16px;");
        lBrancas.setStyle("-fx-text-fill: white; -fx-font-size: 16px;");

        boxPontos.getChildren().addAll(lPretas, lBrancas);

        barraProgresso = new ProgressBar(0);
        barraProgresso.setPrefWidth(200);
        barraProgresso.setStyle("-fx-accent: #27ae60; -fx-control-inner-background: #3c3f41;");

        Button btnGravar = new Button("Gravar Jogo");
        btnGravar.setMaxWidth(Double.MAX_VALUE);
        btnGravar.setStyle("-fx-background-color: #2980b9; -fx-text-fill: white; -fx-font-weight: bold;");
        btnGravar.setOnAction(evento -> gravarJogo());

        Button btnCarregar = new Button("Carregar Jogo");
        btnCarregar.setMaxWidth(Double.MAX_VALUE);
        btnCarregar.setStyle("-fx-background-color: #e67e22; -fx-text-fill: white; -fx-font-weight: bold;");
        btnCarregar.setOnAction(evento -> carregarJogo());

        painelInfo.getChildren().addAll(titulo, vezDe, boxPontos, barraProgresso, btnGravar, btnCarregar);
        layoutPrincipal.getChildren().addAll(tabuleiroVisual, painelInfo);

        Scene cena = new Scene(layoutPrincipal);
        stage.setTitle("Reversi - Grupo 05");
        stage.setScene(cena);
        stage.setResizable(false); 
        
        atualizarTabuleiroVisual();
        stage.show();
    }

    private void atualizarTabuleiroVisual() {
        for (Node node : tabuleiroVisual.getChildren()) {
            if (node instanceof StackPane) {
                StackPane casa = (StackPane) node;
                Integer c = GridPane.getColumnIndex(node);
                Integer l = GridPane.getRowIndex(node);
                if (c == null) c = 0;
                if (l == null) l = 0;

                casa.getChildren().removeIf(filho -> filho instanceof Circle);
                CorPeca estado = jogoReversi.getTabuleiro().getCasa(c, l);

                if (estado == CorPeca.BRANCO) {
                    casa.getChildren().add(new Circle(25, Color.WHITE));
                } else if (estado == CorPeca.PRETO) {
                    casa.getChildren().add(new Circle(25, Color.BLACK));
                }
            }
        }
        
        int ptsP = jogoReversi.getJogador1().getPontuacao();
        int ptsB = jogoReversi.getJogador2().getPontuacao();
        String nomeP = jogoReversi.getJogador1().getNome();
        String nomeB = jogoReversi.getJogador2().getNome();
        
        lPretas.setText(nomeP + ": " + ptsP);
        lBrancas.setText(nomeB + ": " + ptsB);
        vezDe.setText("VEZ DE: " + jogoReversi.getJogadorAtual().getNome().toUpperCase());
        
        barraProgresso.setProgress((ptsP + ptsB) / 64.0);
    }

    private void anunciarVencedor() {
        Alert aviso = new Alert(AlertType.INFORMATION);
        aviso.setTitle("Fim da Partida");
        aviso.setHeaderText("O jogo terminou!");

        pt.grupo05.modelo.Jogador vencedor = jogoReversi.getVencedor();
        
        if (vencedor != null) {
            aviso.setContentText("Vitória de " + vencedor.getNome() + " com " + vencedor.getPontuacao() + " peças!");
        } else {
            aviso.setContentText("A partida terminou em empate!");
        }
        
        aviso.showAndWait();
    }

    /**
     * NOVO MÉTODO: Recebe a jogada da rede e aplica-a no tabuleiro local.
     */
    public void processarJogadaAdversario(int c, int l) {
        boolean jogadaValida = jogoReversi.jogar(c, l);
        if (jogadaValida) {
            atualizarTabuleiroVisual();
            if (!jogoReversi.isJogoAtivo()) {
                anunciarVencedor();
            }
        }
    }

    private void gravarJogo() {
        try (FileOutputStream fos = new FileOutputStream("reversi_save.dat");
             ObjectOutputStream oos = new ObjectOutputStream(fos)) {
             
            oos.writeObject(jogoReversi);
            
            Alert aviso = new Alert(AlertType.INFORMATION);
            aviso.setTitle("Gravação");
            aviso.setHeaderText("Sucesso!");
            aviso.setContentText("A partida foi gravada corretamente.");
            aviso.showAndWait();
            
        } catch (IOException e) {
            Alert erro = new Alert(AlertType.ERROR);
            erro.setTitle("Erro de Gravação");
            erro.setHeaderText("Não foi possível gravar o jogo.");
            erro.setContentText(e.getMessage());
            erro.showAndWait();
        }
    }

    private void carregarJogo() {
        try (FileInputStream fis = new FileInputStream("reversi_save.dat");
             ObjectInputStream ois = new ObjectInputStream(fis)) {
             
            jogoReversi = (Jogo) ois.readObject();
            atualizarTabuleiroVisual();
            
            Alert aviso = new Alert(AlertType.INFORMATION);
            aviso.setTitle("Carregamento");
            aviso.setHeaderText("Sucesso!");
            aviso.setContentText("A partida foi retomada.");
            aviso.showAndWait();
            
        } catch (Exception e) {
            Alert erro = new Alert(AlertType.ERROR);
            erro.setTitle("Erro de Carregamento");
            erro.setHeaderText("Não foi possível carregar o jogo.");
            erro.setContentText("Certifica-te que gravaste um jogo primeiro.");
            erro.showAndWait();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}