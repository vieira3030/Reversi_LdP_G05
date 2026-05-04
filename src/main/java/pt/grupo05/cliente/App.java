package pt.grupo05.cliente;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
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

    // --- VARIÁVEIS GLOBAIS ---
    private Jogo jogoReversi = new Jogo(); 
    private GridPane tabuleiroVisual; 
    
    // Labels globais para podermos atualizar os textos mais tarde
    private Label vezDe;
    private Label lPretas;
    private Label lBrancas;
    
    // Variável simples para controlar o turno visualmente (podes ligar ao teu Jogo.java depois)
    private boolean turnoPretas = true; 

    @Override
    public void start(Stage stage) {
        HBox layoutPrincipal = new HBox(40); 
        layoutPrincipal.setPadding(new Insets(30));
        layoutPrincipal.setAlignment(Pos.CENTER);
        layoutPrincipal.setStyle("-fx-background-color: #2b2b2b;");

        // --- 1. TABULEIRO ---
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
                        // Se a jogada for válida, troca o turno
                        turnoPretas = !turnoPretas;
                        
                        // Atualiza a interface (peças, pontos e texto do turno)
                        atualizarTabuleiroVisual();
                    } else {
                        System.out.println("Jogada inválida!");
                    }
                });

                tabuleiroVisual.add(casa, coluna, linha);
            }
        }

        // --- 2. PAINEL DE INFORMAÇÃO ---
        VBox painelInfo = new VBox(30);
        painelInfo.setAlignment(Pos.TOP_CENTER);
        painelInfo.setMinWidth(220);

        Label titulo = new Label("REVERSI");
        titulo.setStyle("-fx-text-fill: white; -fx-font-size: 34px; -fx-font-weight: bold;");

        // Inicialização das Labels Globais
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
        painelInfo.getChildren().addAll(titulo, vezDe, boxPontos);

        layoutPrincipal.getChildren().addAll(tabuleiroVisual, painelInfo);

        Scene cena = new Scene(layoutPrincipal);
        stage.setTitle("Reversi - Grupo 05");
        stage.setScene(cena);
        stage.setResizable(false); 
        
        atualizarTabuleiroVisual();
        
        stage.show();
    }

    /**
     * Lê a matriz da lógica, desenha as peças no ecrã e atualiza as pontuações.
     */
    private void atualizarTabuleiroVisual() {
        int contagemPretas = 0;
        int contagemBrancas = 0;

        for (Node node : tabuleiroVisual.getChildren()) {
            if (node instanceof StackPane) {
                StackPane casa = (StackPane) node;
                Integer c = GridPane.getColumnIndex(node);
                Integer l = GridPane.getRowIndex(node);
                if (c == null) c = 0;
                if (l == null) l = 0;

                // Limpa as peças antigas
                casa.getChildren().removeIf(filho -> filho instanceof Circle);

                // Vai buscar a peça à lógica
                CorPeca estado = jogoReversi.getTabuleiro().getCasa(c, l);

                // Desenha a peça e soma aos pontos
                if (estado == CorPeca.BRANCO) {
                    casa.getChildren().add(new Circle(25, Color.WHITE));
                    contagemBrancas++;
                } else if (estado == CorPeca.PRETO) {
                    casa.getChildren().add(new Circle(25, Color.BLACK));
                    contagemPretas++;
                }
            }
        }
        
        // --- 3. ATUALIZAÇÃO DO PAINEL LATERAL ---
        // Atualiza os pontos no ecrã
        lPretas.setText("Pretas: " + contagemPretas);
        lBrancas.setText("Brancas: " + contagemBrancas);
        
        // Atualiza a etiqueta do turno
        if (turnoPretas) {
            vezDe.setText("VEZ DAS PRETAS");
        } else {
            vezDe.setText("VEZ DAS BRANCAS");
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}