package pt.grupo05.cliente;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
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

/**
 * Classe principal da interface gráfica do jogo Reversi.
 * Gere a visualização do tabuleiro, o painel de informações e a interação com o utilizador.
 */
public class App extends Application {

    /**
     * Inicializa e configura os componentes da interface JavaFX.
     * Implementa a grelha de jogo, os eventos de clique e o painel de estado lateral.
     * @param stage O contentor principal da aplicação
     */
    @Override
    public void start(Stage stage) {
        // Contentor principal em HBox para separar o tabuleiro do painel lateral
        HBox layoutPrincipal = new HBox(40); 
        layoutPrincipal.setPadding(new Insets(30));
        layoutPrincipal.setAlignment(Pos.CENTER);
        layoutPrincipal.setStyle("-fx-background-color: #2b2b2b;");

        // --- 1. GRELHA DO TABULEIRO (8x8) ---
        GridPane tabuleiroVisual = new GridPane();
        Color corCasa = Color.DARKGREEN;
        Color corLinha = Color.BLACK;

        for (int linha = 0; linha < 8; linha++) {
            for (int coluna = 0; coluna < 8; coluna++) {
                // StackPane permite sobrepor as peças às casas do tabuleiro
                StackPane casa = new StackPane();
                
                // Desenho da casa (quadrado verde)
                Rectangle fundo = new Rectangle(60, 60);
                fundo.setFill(corCasa);
                fundo.setStroke(corLinha);
                fundo.setStrokeWidth(2); 
                
                casa.getChildren().add(fundo);

                // --- IMPLEMENTAÇÃO DA PARTE 1: CAPTURA DE CLIQUE ---
                // Variáveis finais necessárias para serem lidas dentro do evento Lambda
                final int l = linha;
                final int c = coluna;

                casa.setOnMouseClicked(evento -> {
                    // Confirmação da coordenada no terminal para testes
                    System.out.println("Clicou na Linha " + l + ", Coluna " + c);
                });
                // --------------------------------------------------

                // Configuração das 4 peças iniciais no centro do tabuleiro
                if ((linha == 3 && coluna == 3) || (linha == 4 && coluna == 4)) {
                    Circle pecaBranca = new Circle(25, Color.WHITE);
                    casa.getChildren().add(pecaBranca);
                } 
                else if ((linha == 3 && coluna == 4) || (linha == 4 && coluna == 3)) {
                    Circle pecaPreta = new Circle(25, Color.BLACK);
                    casa.getChildren().add(pecaPreta);
                }
                
                // Adiciona a casa à grelha nas coordenadas correspondentes
                tabuleiroVisual.add(casa, coluna, linha);
            }
        }

        // --- 2. PAINEL DE INFORMAÇÃO LATERAL ---
        VBox painelInfo = new VBox(30);
        painelInfo.setAlignment(Pos.TOP_CENTER);
        painelInfo.setMinWidth(220);

        Label titulo = new Label("REVERSI");
        titulo.setStyle("-fx-text-fill: white; -fx-font-size: 34px; -fx-font-weight: bold;");

        Label vezDe = new Label("PRÓXIMA JOGADA:\nPRETAS");
        vezDe.setAlignment(Pos.CENTER);
        vezDe.setStyle("-fx-text-fill: #f1c40f; -fx-font-size: 18px; -fx-font-weight: bold; -fx-text-alignment: center;");

        // Contentor para as pontuações
        VBox boxPontos = new VBox(15);
        boxPontos.setAlignment(Pos.CENTER);
        boxPontos.setStyle("-fx-background-color: #3c3f41; -fx-padding: 20; -fx-background-radius: 15;");

        Label labelPretas = new Label("Pretas: 2");
        labelPretas.setStyle("-fx-text-fill: white; -fx-font-size: 18px;");
        
        Label labelBrancas = new Label("Brancas: 2");
        labelBrancas.setStyle("-fx-text-fill: white; -fx-font-size: 18px;");

        boxPontos.getChildren().addAll(labelPretas, labelBrancas);
        painelInfo.getChildren().addAll(titulo, vezDe, boxPontos);

        // --- 3. MONTAGEM E EXIBIÇÃO ---
        layoutPrincipal.getChildren().addAll(tabuleiroVisual, painelInfo);

        Scene cena = new Scene(layoutPrincipal);
        stage.setTitle("Reversi - Grupo 05");
        stage.setScene(cena);
        stage.setResizable(false); 
        stage.show();
    }

    /**
     * Ponto de entrada da aplicação.
     * @param args Argumentos da linha de comandos
     */
    public static void main(String[] args) {
        launch(args);
    }
}