package pt.grupo05.cliente;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
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

public class App extends Application {

    // Instância principal do jogo.
    private Jogo jogoReversi = new Jogo(); 
    // Grelha visual do tabuleiro.
    private GridPane tabuleiroVisual; 
    
    // Etiquetas para o painel de informação.
    private Label vezDe;
    private Label lPretas;
    private Label lBrancas;
    
    // Barra indicadora da ocupação do tabuleiro.
    private ProgressBar barraProgresso;

    @Override
    public void start(Stage stage) {
        // Configura o layout principal.
        HBox layoutPrincipal = new HBox(40); 
        layoutPrincipal.setPadding(new Insets(30));
        layoutPrincipal.setAlignment(Pos.CENTER);
        layoutPrincipal.setStyle("-fx-background-color: #2b2b2b;");

        // Configura a grelha visual do tabuleiro.
        tabuleiroVisual = new GridPane();
        Color corCasa = Color.DARKGREEN;
        Color corLinha = Color.BLACK;

        // Constrói as 64 casas do tabuleiro.
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

                // Define a ação de clique em cada casa.
                casa.setOnMouseClicked(evento -> {
                    boolean jogadaValida = jogoReversi.jogar(c, l);
                    if (jogadaValida) {
                        atualizarTabuleiroVisual();
                    } else {
                        System.out.println("Jogada inválida!");
                    }
                });

                tabuleiroVisual.add(casa, coluna, linha);
            }
        }

        // Configura o painel lateral de informação.
        VBox painelInfo = new VBox(30);
        painelInfo.setAlignment(Pos.TOP_CENTER);
        painelInfo.setMinWidth(220);

        Label titulo = new Label("REVERSI");
        titulo.setStyle("-fx-text-fill: white; -fx-font-size: 34px; -fx-font-weight: bold;");

        vezDe = new Label("VEZ DAS PRETAS"); 
        vezDe.setStyle("-fx-text-fill: #f1c40f; -fx-font-size: 18px; -fx-font-weight: bold;");

        VBox boxPontos = new VBox(15);
        boxPontos.setAlignment(Pos.CENTER);
        boxPontos.setStyle("-fx-background-color: #3c3f41; -fx-padding: 20; -fx-background-radius: 15;");

        lPretas = new Label("Pretas: 2");
        lBrancas = new Label("Brancas: 2");
        lPretas.setStyle("-fx-text-fill: white; -fx-font-size: 18px;");
        lBrancas.setStyle("-fx-text-fill: white; -fx-font-size: 18px;");

        boxPontos.getChildren().addAll(lPretas, lBrancas);

        // Inicializa e configura a barra de progresso.
        barraProgresso = new ProgressBar(0);
        barraProgresso.setPrefWidth(200);
        barraProgresso.setStyle("-fx-accent: #27ae60; -fx-control-inner-background: #3c3f41;");

        // Agrupa os elementos no painel lateral.
        painelInfo.getChildren().addAll(titulo, vezDe, boxPontos, barraProgresso);

        layoutPrincipal.getChildren().addAll(tabuleiroVisual, painelInfo);

        // Define e mostra a cena.
        Scene cena = new Scene(layoutPrincipal);
        stage.setTitle("Reversi - Grupo 05");
        stage.setScene(cena);
        stage.setResizable(false); 
        
        // Garante que a interface inicializa com os valores corretos.
        atualizarTabuleiroVisual();
        
        stage.show();
    }

    /**
     * Sincroniza a interface gráfica com o estado atual da lógica do jogo.
     */
    private void atualizarTabuleiroVisual() {
        // Atualiza a representação gráfica das peças.
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
        
        // Obtém as pontuações atualizadas.
        int ptsPretas = jogoReversi.getJogador1().getPontuacao();
        int ptsBrancas = jogoReversi.getJogador2().getPontuacao();
        
        lPretas.setText("Pretas: " + ptsPretas);
        lBrancas.setText("Brancas: " + ptsBrancas);
        
        // Atualiza o progresso de ocupação do tabuleiro.
        double progresso = (ptsPretas + ptsBrancas) / 64.0;
        barraProgresso.setProgress(progresso);
        
        // Atualiza a indicação visual do turno.
        if (jogoReversi.getJogadorAtual().getCor() == CorPeca.PRETO) {
            vezDe.setText("VEZ DAS PRETAS");
        } else {
            vezDe.setText("VEZ DAS BRANCAS");
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}