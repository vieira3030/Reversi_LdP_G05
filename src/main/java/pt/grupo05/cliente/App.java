package pt.grupo05.cliente;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextInputDialog;
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
    
    // Etiquetas de texto para o painel lateral.
    private Label vezDe;
    private Label lPretas;
    private Label lBrancas;
    
    // Barra de progresso para ocupação das 64 casas.
    private ProgressBar barraProgresso;

    @Override
    public void start(Stage stage) {
        // --- MENU INICIAL (PARÂMETROS) ---
        
        // Solicita o nome do Jogador 1 (Pretas).
        TextInputDialog dialog1 = new TextInputDialog("Jogador 1");
        dialog1.setTitle("Configuração");
        dialog1.setHeaderText("Jogador das Peças Pretas");
        dialog1.setContentText("Introduza o nome:");
        
        Optional<String> res1 = dialog1.showAndWait();
        if (res1.isPresent()) {
            jogoReversi.getJogador1().setNome(res1.get());
        } else {
            System.exit(0); // Encerra se o utilizador cancelar.
        }

        // Solicita o nome do Jogador 2 (Brancas).
        TextInputDialog dialog2 = new TextInputDialog("Jogador 2");
        dialog2.setTitle("Configuração");
        dialog2.setHeaderText("Jogador das Peças Brancas");
        dialog2.setContentText("Introduza o nome:");
        
        Optional<String> res2 = dialog2.showAndWait();
        if (res2.isPresent()) {
            jogoReversi.getJogador2().setNome(res2.get());
        } else {
            System.exit(0); // Encerra se o utilizador cancelar.
        }

        // Configuração do layout principal da aplicação.
        HBox layoutPrincipal = new HBox(40); 
        layoutPrincipal.setPadding(new Insets(30));
        layoutPrincipal.setAlignment(Pos.CENTER);
        layoutPrincipal.setStyle("-fx-background-color: #2b2b2b;");

        // Inicialização do tabuleiro visual.
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

                // Gere o evento de clique nas coordenadas selecionadas.
                casa.setOnMouseClicked(evento -> {
                    boolean jogadaValida = jogoReversi.jogar(c, l);
                    if (jogadaValida) {
                        atualizarTabuleiroVisual();
                        
                        // Verifica se o estado do jogo passou a inativo.
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

        // Configuração do painel lateral de informações.
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

        // Configuração visual da barra de progresso.
        barraProgresso = new ProgressBar(0);
        barraProgresso.setPrefWidth(200);
        barraProgresso.setStyle("-fx-accent: #27ae60; -fx-control-inner-background: #3c3f41;");

        // Botão para gravar o estado atual do jogo.
        Button btnGravar = new Button("Gravar Jogo");
        btnGravar.setMaxWidth(Double.MAX_VALUE);
        btnGravar.setStyle("-fx-background-color: #2980b9; -fx-text-fill: white; -fx-font-weight: bold;");
        btnGravar.setOnAction(evento -> gravarJogo());

        // Botão para carregar um jogo guardado.
        Button btnCarregar = new Button("Carregar Jogo");
        btnCarregar.setMaxWidth(Double.MAX_VALUE);
        btnCarregar.setStyle("-fx-background-color: #e67e22; -fx-text-fill: white; -fx-font-weight: bold;");
        btnCarregar.setOnAction(evento -> carregarJogo());

        painelInfo.getChildren().addAll(titulo, vezDe, boxPontos, barraProgresso, btnGravar, btnCarregar);
        layoutPrincipal.getChildren().addAll(tabuleiroVisual, painelInfo);

        // Inicialização da cena e exibição da janela.
        Scene cena = new Scene(layoutPrincipal);
        stage.setTitle("Reversi - Grupo 05");
        stage.setScene(cena);
        stage.setResizable(false); 
        
        atualizarTabuleiroVisual();
        stage.show();
    }

    /**
     * Atualiza todos os elementos visuais com base no estado da lógica.
     */
    private void atualizarTabuleiroVisual() {
        // Atualiza a disposição das peças na grelha.
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
        
        // Obtém dados atuais dos jogadores.
        int ptsP = jogoReversi.getJogador1().getPontuacao();
        int ptsB = jogoReversi.getJogador2().getPontuacao();
        String nomeP = jogoReversi.getJogador1().getNome();
        String nomeB = jogoReversi.getJogador2().getNome();
        
        // Atualiza etiquetas de pontuação e turno.
        lPretas.setText(nomeP + ": " + ptsP);
        lBrancas.setText(nomeB + ": " + ptsB);
        vezDe.setText("VEZ DE: " + jogoReversi.getJogadorAtual().getNome().toUpperCase());
        
        // Atualiza o rácio de preenchimento do tabuleiro.
        barraProgresso.setProgress((ptsP + ptsB) / 64.0);
    }

    /**
     * Exibe uma caixa de diálogo com o resultado final da partida.
     */
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
     * Guarda o estado atual da partida num ficheiro.
     */
    private void gravarJogo() {
        // Tenta escrever o objeto do jogo num ficheiro local.
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

    /**
     * Carrega o estado da partida a partir de um ficheiro.
     */
    private void carregarJogo() {
        // Tenta ler o ficheiro local e repor o objeto do jogo.
        try (FileInputStream fis = new FileInputStream("reversi_save.dat");
             ObjectInputStream ois = new ObjectInputStream(fis)) {
             
            jogoReversi = (Jogo) ois.readObject(); // Substitui o jogo atual pelo gravado
            atualizarTabuleiroVisual(); // Refresca o ecrã com as posições antigas
            
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