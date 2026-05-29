package pt.grupo05.cliente;

import javafx.application.Application;
import javafx.application.Platform;
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

    // Lógica do jogo.
    private Jogo jogoReversi = new Jogo(); 
    // Grelha visual.
    private GridPane tabuleiroVisual; 
    
    // Gestor de comunicação.
    private GestorRede gestorRede;
    
    // Elementos da interface.
    private Label vezDe;
    private Label lPretas;
    private Label lBrancas;
    private ProgressBar barraProgresso;
    
    // NOVO: Etiqueta para as mensagens de estado.
    private Label lblMensagens;

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
        
        // Campos preenchidos por defeito para facilitar testes
        TextField txtIp = new TextField("localhost"); 
        TextField txtPorta = new TextField("8080"); 

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
            return null; // Resolve o problema do crash ao fechar a janela
        });
        
        Optional<String[]> resultado = dialog.showAndWait();
        if (resultado.isPresent()) {
            String nome = resultado.get()[0];
            String ip = resultado.get()[1];
            
            // Proteção contra crashes na conversão da porta
            int porta = 8080;
            try {
                porta = Integer.parseInt(resultado.get()[2]); 
            } catch (NumberFormatException e) {
                System.out.println("Porta inválida inserida. A usar a porta 8080 por segurança.");
            }
            
            jogoReversi.getJogador1().setNome(nome + " (P1)");
            jogoReversi.getJogador2().setNome("Adversário (P2)");
            
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
                        atualizarMensagem("✅ Jogada válida efetuada.");
                        atualizarTabuleiroVisual();
                        
                        if (gestorRede != null) {
                            gestorRede.enviarJogada(c, l);
                        }
                        
                        if (!jogoReversi.isJogoAtivo()) {
                            anunciarVencedor();
                        }
                    } else {
                        atualizarMensagem("❌ Jogada inválida — escolhe outra casa.");
                    }
                });

                tabuleiroVisual.add(casa, coluna, linha);
            }
        }

        VBox painelInfo = new VBox(20);
        painelInfo.setAlignment(Pos.TOP_CENTER);
        painelInfo.setMinWidth(250);

        Label titulo = new Label("REVERSI");
        titulo.setStyle("-fx-text-fill: white; -fx-font-size: 34px; -fx-font-weight: bold;");

        vezDe = new Label("VEZ DE: "); 
        vezDe.setStyle("-fx-text-fill: #f1c40f; -fx-font-size: 16px; -fx-font-weight: bold;");

        VBox boxPontos = new VBox(10);
        boxPontos.setAlignment(Pos.CENTER);
        boxPontos.setStyle("-fx-background-color: #3c3f41; -fx-padding: 15; -fx-background-radius: 10;");
        lPretas = new Label();
        lBrancas = new Label();
        lPretas.setStyle("-fx-text-fill: white; -fx-font-size: 16px;");
        lBrancas.setStyle("-fx-text-fill: white; -fx-font-size: 16px;");
        boxPontos.getChildren().addAll(lPretas, lBrancas);

        barraProgresso = new ProgressBar(0);
        barraProgresso.setPrefWidth(200);
        barraProgresso.setStyle("-fx-accent: #27ae60; -fx-control-inner-background: #3c3f41;");

        // NOVO: Área de mensagens de estado
        lblMensagens = new Label("▶ Jogo iniciado.");
        lblMensagens.setStyle("-fx-text-fill: white; -fx-font-size: 13px; -fx-background-color: #444; -fx-padding: 10; -fx-background-radius: 5;");
        lblMensagens.setWrapText(true);
        lblMensagens.setPrefWidth(200);

        // Agrupa os botões de ação do jogo
        VBox boxBotoes = new VBox(10);
        
        Button btnNovoJogo = new Button("▶ Novo Jogo");
        btnNovoJogo.setMaxWidth(Double.MAX_VALUE);
        btnNovoJogo.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-weight: bold;");
        btnNovoJogo.setOnAction(e -> iniciarNovoJogo());

        Button btnReiniciar = new Button("↺ Reiniciar");
        btnReiniciar.setMaxWidth(Double.MAX_VALUE);
        btnReiniciar.setStyle("-fx-background-color: #f39c12; -fx-text-fill: white; -fx-font-weight: bold;");
        btnReiniciar.setOnAction(e -> pedirConfirmacaoReiniciar());

        Button btnTerminar = new Button("✕ Terminar");
        btnTerminar.setMaxWidth(Double.MAX_VALUE);
        btnTerminar.setStyle("-fx-background-color: #c0392b; -fx-text-fill: white; -fx-font-weight: bold;");
        btnTerminar.setOnAction(e -> pedirConfirmacaoTerminar());

        // Botões antigos de ficheiro
        HBox boxFicheiros = new HBox(10);
        Button btnGravar = new Button("Gravar");
        btnGravar.setMaxWidth(Double.MAX_VALUE);
        btnGravar.setStyle("-fx-background-color: #2980b9; -fx-text-fill: white;");
        btnGravar.setOnAction(evento -> gravarJogo());
        
        Button btnCarregar = new Button("Carregar");
        btnCarregar.setMaxWidth(Double.MAX_VALUE);
        btnCarregar.setStyle("-fx-background-color: #8e44ad; -fx-text-fill: white;");
        btnCarregar.setOnAction(evento -> carregarJogo());
        boxFicheiros.getChildren().addAll(btnGravar, btnCarregar);
        boxFicheiros.setAlignment(Pos.CENTER);

        boxBotoes.getChildren().addAll(btnNovoJogo, btnReiniciar, btnTerminar, boxFicheiros);

        painelInfo.getChildren().addAll(titulo, vezDe, boxPontos, barraProgresso, lblMensagens, boxBotoes);
        layoutPrincipal.getChildren().addAll(tabuleiroVisual, painelInfo);

        Scene cena = new Scene(layoutPrincipal);
        stage.setTitle("Reversi - Grupo 05");
        stage.setScene(cena);
        stage.setResizable(false); 
        
        atualizarTabuleiroVisual();
        stage.show();
    }

    /**
     * Atualiza o texto da área de mensagens.
     */
    private void atualizarMensagem(String msg) {
        lblMensagens.setText(msg);
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

    /**
     * Confirmação antes de reiniciar a partida atual.
     */
    private void pedirConfirmacaoReiniciar() {
        Alert alerta = new Alert(AlertType.CONFIRMATION);
        alerta.setTitle("Reiniciar Jogo");
        alerta.setHeaderText("Tens a certeza que queres reiniciar?");
        alerta.setContentText("O progresso atual será perdido.");

        Optional<ButtonType> resposta = alerta.showAndWait();
        if (resposta.isPresent() && resposta.get() == ButtonType.OK) {
            iniciarNovoJogo();
            atualizarMensagem("↺ O jogo foi reiniciado.");
        }
    }

    /**
     * Confirmação antes de encerrar o programa.
     */
    private void pedirConfirmacaoTerminar() {
        Alert alerta = new Alert(AlertType.CONFIRMATION);
        alerta.setTitle("Terminar Aplicação");
        alerta.setHeaderText("Tens a certeza que queres sair?");

        Optional<ButtonType> resposta = alerta.showAndWait();
        if (resposta.isPresent() && resposta.get() == ButtonType.OK) {
            Platform.exit(); // Encerra a aplicação de forma limpa.
            System.exit(0);
        }
    }

    /**
     * Repõe o estado inicial do jogo.
     */
    private void iniciarNovoJogo() {
        // Mantém os nomes atuais, mas cria uma nova instância da lógica.
        String nome1 = jogoReversi.getJogador1().getNome();
        String nome2 = jogoReversi.getJogador2().getNome();
        
        jogoReversi = new Jogo();
        jogoReversi.getJogador1().setNome(nome1);
        jogoReversi.getJogador2().setNome(nome2);
        
        atualizarTabuleiroVisual();
        atualizarMensagem("▶ Novo jogo iniciado. Vez das pretas.");
    }

    private void anunciarVencedor() {
        Alert aviso = new Alert(AlertType.INFORMATION);
        aviso.setTitle("Fim da Partida");
        aviso.setHeaderText("O jogo terminou!");

        pt.grupo05.modelo.Jogador vencedor = jogoReversi.getVencedor();
        
        if (vencedor != null) {
            aviso.setContentText("Vitória de " + vencedor.getNome() + " com " + vencedor.getPontuacao() + " peças!");
            atualizarMensagem("🏆 Fim do jogo! Vitória de " + vencedor.getNome());
        } else {
            aviso.setContentText("A partida terminou em empate!");
            atualizarMensagem("🤝 Fim do jogo! Empate.");
        }
        
        aviso.showAndWait();
    }

    public void processarJogadaAdversario(int c, int l) {
        boolean jogadaValida = jogoReversi.jogar(c, l);
        if (jogadaValida) {
            atualizarMensagem("✅ O adversário jogou.");
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
            atualizarMensagem("💾 Jogo gravado com sucesso.");
            
        } catch (IOException e) {
            atualizarMensagem("❌ Erro ao gravar jogo.");
        }
    }

    private void carregarJogo() {
        try (FileInputStream fis = new FileInputStream("reversi_save.dat");
             ObjectInputStream ois = new ObjectInputStream(fis)) {
             
            jogoReversi = (Jogo) ois.readObject();
            atualizarTabuleiroVisual();
            atualizarMensagem("📂 Jogo carregado com sucesso.");
            
        } catch (Exception e) {
            atualizarMensagem("❌ Erro ao carregar jogo.");
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}