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
import javafx.scene.layout.Priority;
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

/**
 * Classe principal da interface gráfica da aplicação.
 */
public class App extends Application {

    // Lógica principal do jogo.
    private Jogo jogoReversi = new Jogo(); 
    // Grelha visual do tabuleiro.
    private GridPane tabuleiroVisual; 
    // Gestor da ligação por Sockets.
    private GestorRede gestorRede;
    
    // Identidade do jogador local (para controlo de turnos).
    private String meuNome;
    private CorPeca minhaCor;
    
    // Elementos visuais atualizáveis.
    private Label vezDe;
    private Label lPretas;
    private Label lBrancas;
    private ProgressBar barraProgresso;
    private Label lblMensagens;

    /**
     * Obtém o nome do jogador local.
     * @return String com o nome do jogador
     */
    public String getMeuNome() {
        return meuNome;
    }

    /**
     * Configura as cores e nomes consoante a ligação de rede.
     * @param isServidor define se o jogador local é o servidor
     * @param nomeAdversario nome recebido do outro jogador
     */
    public void configurarPartida(boolean isServidor, String nomeAdversario) {
        if (isServidor) {
            minhaCor = CorPeca.PRETO;
            jogoReversi.getJogador1().setNome(meuNome); 
            jogoReversi.getJogador2().setNome(nomeAdversario); 
            atualizarMensagem("🟢 Tu és as Pretas (começas tu)!");
        } else {
            minhaCor = CorPeca.BRANCO;
            jogoReversi.getJogador1().setNome(nomeAdversario); 
            jogoReversi.getJogador2().setNome(meuNome); 
            atualizarMensagem("🟢 Tu és as Brancas (espera pelo adversário)!");
        }
        atualizarTabuleiroVisual();
    }

    @Override
    public void start(Stage stage) {
        // Cria a janela inicial para configuração da rede.
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
        TextField txtIp = new TextField("localhost"); 
        TextField txtPorta = new TextField("8080"); 

        gridDialogo.add(new Label("Nome do jogador:"), 0, 0);
        gridDialogo.add(txtNome, 1, 0);
        gridDialogo.add(new Label("IP do servidor:"), 0, 1);
        gridDialogo.add(txtIp, 1, 1);
        gridDialogo.add(new Label("Porta:"), 0, 2);
        gridDialogo.add(txtPorta, 1, 2);

        dialog.getDialogPane().setContent(gridDialogo);

        // Processa as informações introduzidas na janela de ligação.
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == btnLigar) {
                return new String[]{txtNome.getText(), txtIp.getText(), txtPorta.getText()};
            }
            return null; 
        });
        
        Optional<String[]> resultado = dialog.showAndWait();
        if (resultado.isPresent()) {
            this.meuNome = resultado.get()[0]; 
            String ip = resultado.get()[1];
            
            int porta = 8080;
            try {
                porta = Integer.parseInt(resultado.get()[2]); 
            } catch (NumberFormatException e) {
                System.out.println("Porta inválida. Assumida a 8080.");
            }
            
            // Define nomes temporários enquanto a rede não sincroniza.
            jogoReversi.getJogador1().setNome(meuNome);
            jogoReversi.getJogador2().setNome("A procurar...");
            
            gestorRede = new GestorRede(this);
            gestorRede.iniciarConexao(ip, porta);
        } else {
            System.exit(0); 
        }

        HBox layoutPrincipal = new HBox(40); 
        layoutPrincipal.setPadding(new Insets(30));
        layoutPrincipal.setAlignment(Pos.CENTER);
        layoutPrincipal.setStyle("-fx-background-color: #2b2b2b;");

        // Constrói as 64 casas visuais do tabuleiro.
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

                // Evento de clique para efetuar jogada.
                casa.setOnMouseClicked(evento -> {
                    // Bloqueia jogadas fora do turno ou antes da ligação.
                    if (minhaCor == null || jogoReversi.getJogadorAtual().getCor() != minhaCor) {
                        atualizarMensagem("✋ Não é a tua vez! Espera pelo adversário.");
                        return;
                    }

                    boolean jogadaValida = jogoReversi.jogar(c, l);
                    if (jogadaValida) {
                        atualizarMensagem("✅ Jogada enviada!");
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

        // Configuração do painel lateral de informações.
        VBox painelInfo = new VBox(15);
        painelInfo.setAlignment(Pos.TOP_CENTER);
        painelInfo.setPrefWidth(350); 

        Label titulo = new Label("REVERSI");
        titulo.setStyle("-fx-text-fill: white; -fx-font-size: 30px; -fx-font-weight: bold;");
        StackPane boxTitulo = new StackPane(titulo);
        boxTitulo.setStyle("-fx-background-color: #1a5276; -fx-padding: 15;");

        vezDe = new Label(); 
        StackPane boxVez = new StackPane(vezDe);
        boxVez.setStyle("-fx-background-color: #3c3f41; -fx-padding: 15; -fx-border-color: #555; -fx-border-width: 1;");

        VBox boxPontos = new VBox(20);
        boxPontos.setAlignment(Pos.CENTER_LEFT);
        boxPontos.setStyle("-fx-background-color: #343535; -fx-padding: 25; -fx-border-color: #555; -fx-border-width: 1;");
        
        HBox linhaPretas = new HBox(15);
        linhaPretas.setAlignment(Pos.CENTER_LEFT);
        Circle iconePreto = new Circle(18, Color.rgb(11, 22, 44));
        iconePreto.setStroke(Color.BLACK);
        lPretas = new Label();
        lPretas.setStyle("-fx-text-fill: white; -fx-font-size: 18px;");
        linhaPretas.getChildren().addAll(iconePreto, lPretas);

        HBox linhaBrancas = new HBox(15);
        linhaBrancas.setAlignment(Pos.CENTER_LEFT);
        Circle iconeBranco = new Circle(18, Color.WHITE);
        iconeBranco.setStroke(Color.BLACK);
        lBrancas = new Label();
        lBrancas.setStyle("-fx-text-fill: white; -fx-font-size: 18px;");
        linhaBrancas.getChildren().addAll(iconeBranco, lBrancas);
        
        boxPontos.getChildren().addAll(linhaPretas, linhaBrancas);

        lblMensagens = new Label("▶ A aguardar ligação da rede...");
        lblMensagens.setStyle("-fx-text-fill: white; -fx-font-size: 13px; -fx-background-color: #444; -fx-padding: 12; -fx-background-radius: 5; -fx-border-color: #555;");
        lblMensagens.setMaxWidth(Double.MAX_VALUE);

        barraProgresso = new ProgressBar(0);
        barraProgresso.setMaxWidth(Double.MAX_VALUE);
        barraProgresso.setStyle("-fx-accent: #1a5276; -fx-control-inner-background: #444; -fx-border-color: #555; -fx-border-width: 1;");

        // Botão Novo Jogo (Notifica o adversário com o código -1).
        Button btnNovoJogo = new Button("▶ Novo Jogo");
        btnNovoJogo.setMaxWidth(Double.MAX_VALUE);
        btnNovoJogo.setStyle("-fx-background-color: #f1c40f; -fx-text-fill: black; -fx-font-weight: bold; -fx-font-size: 14px; -fx-padding: 12;");
        btnNovoJogo.setOnAction(e -> {
            iniciarNovoJogo();
            if (gestorRede != null) {
                gestorRede.enviarJogada(-1, -1);
            }
        });

        // Botões de Ação.
        HBox boxBotoesAcao = new HBox(10);
        Button btnReiniciar = new Button("↺ Reiniciar");
        btnReiniciar.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(btnReiniciar, Priority.ALWAYS); 
        btnReiniciar.setStyle("-fx-background-color: #2ecc71; -fx-text-fill: black; -fx-font-weight: bold; -fx-padding: 12;");
        btnReiniciar.setOnAction(e -> pedirConfirmacaoReiniciar());

        Button btnTerminar = new Button("✕ Terminar");
        btnTerminar.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(btnTerminar, Priority.ALWAYS); 
        btnTerminar.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 12;");
        btnTerminar.setOnAction(e -> pedirConfirmacaoTerminar());
        boxBotoesAcao.getChildren().addAll(btnReiniciar, btnTerminar);

        // Botões de Ficheiro.
        HBox boxFicheiros = new HBox(10);
        Button btnGravar = new Button("Gravar");
        btnGravar.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(btnGravar, Priority.ALWAYS); 
        btnGravar.setStyle("-fx-background-color: #8e44ad; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10;");
        btnGravar.setOnAction(evento -> gravarJogo());
        
        Button btnCarregar = new Button("Carregar");
        btnCarregar.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(btnCarregar, Priority.ALWAYS); 
        btnCarregar.setStyle("-fx-background-color: #8e44ad; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10;");
        btnCarregar.setOnAction(evento -> {
            carregarJogo();
            if (gestorRede != null) {
                gestorRede.enviarJogada(-2, -2);
            }
        });
        boxFicheiros.getChildren().addAll(btnGravar, btnCarregar);

        painelInfo.getChildren().addAll(boxTitulo, boxVez, boxPontos, lblMensagens, barraProgresso, btnNovoJogo, boxBotoesAcao, boxFicheiros);
        layoutPrincipal.getChildren().addAll(tabuleiroVisual, painelInfo);

        Scene cena = new Scene(layoutPrincipal);
        stage.setTitle("Reversi - Grupo 05");
        stage.setScene(cena);
        stage.setResizable(false); 
        
        stage.setOnCloseRequest(e -> System.exit(0));
        
        atualizarTabuleiroVisual();
        stage.show();
    }

    /**
     * Atualiza a mensagem na caixa de informações.
     */
    public void atualizarMensagem(String msg) {
        lblMensagens.setText(msg);
    }

    /**
     * Redesenha as peças e atualiza as pontuações no ecrã.
     */
    private void atualizarTabuleiroVisual() {
        for (Node node : tabuleiroVisual.getChildren()) {
            if (node instanceof StackPane) {
                StackPane casa = (StackPane) node;
                Integer c = GridPane.getColumnIndex(node);
                Integer l = GridPane.getRowIndex(node);
                if (c == null) c = 0;
                if (l == null) l = 0;

                casa.getChildren().removeIf(filho -> child instanceof Circle);
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
        
        lPretas.setText("Pretas (" + nomeP + "): " + ptsP);
        lBrancas.setText("Brancas (" + nomeB + "): " + ptsB);
        
        if (minhaCor != null) {
            if (jogoReversi.getJogadorAtual().getCor() == minhaCor) {
                vezDe.setText("É A TUA VEZ");
                vezDe.setStyle("-fx-text-fill: #f1c40f; -fx-font-size: 16px; -fx-font-weight: bold;"); 
            } else {
                vezDe.setText("VEZ DO ADVERSÁRIO");
                vezDe.setStyle("-fx-text-fill: #ecf0f1; -fx-font-size: 16px; -fx-font-weight: bold;"); 
            }
        } else {
            vezDe.setText("A AGUARDAR LIGAÇÃO...");
            vezDe.setStyle("-fx-text-fill: #bdc3c7; -fx-font-size: 16px; -fx-font-weight: bold;"); 
        }
        
        barraProgresso.setProgress((ptsP + ptsB) / 64.0);
    }

    /**
     * Pede confirmação local e avisa a rede em caso de reinício.
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
            if (gestorRede != null) {
                gestorRede.enviarJogada(-1, -1);
            }
        }
    }

    /**
     * Pede confirmação antes de encerrar o programa.
     */
    private void pedirConfirmacaoTerminar() {
        Alert alerta = new Alert(AlertType.CONFIRMATION);
        alerta.setTitle("Terminar Aplicação");
        alerta.setHeaderText("Tens a certeza que queres sair?");

        Optional<ButtonType> resposta = alerta.showAndWait();
        if (resposta.isPresent() && resposta.get() == ButtonType.OK) {
            Platform.exit(); 
            System.exit(0);
        }
    }

    /**
     * Limpa o tabuleiro e inicia uma nova partida mantendo os nomes.
     */
    private void iniciarNovoJogo() {
        String nome1 = jogoReversi.getJogador1().getNome();
        String nome2 = jogoReversi.getJogador2().getNome();
        
        jogoReversi = new Jogo();
        jogoReversi.getJogador1().setNome(nome1);
        jogoReversi.getJogador2().setNome(nome2);
        
        tabuleiroVisual.setDisable(false);
        atualizarTabuleiroVisual();
    }

    /**
     * Mostra o resultado final da partida.
     */
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

    /**
     * Sincroniza a jogada ou interceta comandos especiais enviados via rede.
     * @param c coluna da jogada ou sinal de controlo
     * @param l linha da jogada ou sinal de controlo
     */
    public void processarJogadaAdversario(int c, int l) {
        // Interceta sinal para reiniciar o tabuleiro (-1, -1)
        if (c == -1 && l == -1) {
            iniciarNovoJogo();
            atualizarMensagem("🔄 O adversário reiniciou o jogo.");
            return;
        }

        // Interceta sinal para carregar ficheiro local (-2, -2)
        if (c == -2 && l == -2) {
            carregarJogo();
            atualizarMensagem("📂 O adversário carregou uma gravação.");
            return;
        }

        // Processamento normal de jogada de peça
        boolean jogadaValida = jogoReversi.jogar(c, l);
        if (jogadaValida) {
            atualizarMensagem("✅ O adversário jogou.");
            atualizarTabuleiroVisual();
            if (!jogoReversi.isJogoAtivo()) {
                anunciarVencedor();
            }
        }
    }

    /**
     * Declara vitória se a comunicação cair.
     */
    public void adversarioDesistiu() {
        atualizarMensagem("🔴 O adversário saiu da partida.");
        tabuleiroVisual.setDisable(true); 
        
        Alert aviso = new Alert(AlertType.INFORMATION);
        aviso.setTitle("Fim por Desistência");
        aviso.setHeaderText("Vitória por Desistência!");
        aviso.setContentText("O teu adversário abandonou o jogo ou a ligação caiu. Foste declarado o vencedor!");
        aviso.showAndWait();
    }

    /**
     * Guarda o estado atual da partida num ficheiro.
     */
    private void gravarJogo() {
        try (FileOutputStream fos = new FileOutputStream("reversi_save.dat");
             ObjectOutputStream oos = new ObjectOutputStream(fos)) {
            oos.writeObject(jogoReversi);
            atualizarMensagem("💾 Jogo gravado com sucesso.");
        } catch (IOException e) {
            atualizarMensagem("❌ Erro ao gravar jogo.");
        }
    }

    /**
     * Repõe uma partida gravada.
     */
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
    
    /**
     * Método principal que arranca a aplicação JavaFX.
     * @param args argumentos de linha de comandos
     */
    public static void main(String[] args) {
        launch(args);
    }
}