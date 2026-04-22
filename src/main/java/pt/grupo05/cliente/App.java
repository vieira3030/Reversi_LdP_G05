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

public class App extends Application {

    @Override
    public void start(Stage stage) {
        HBox layoutPrincipal = new HBox(40); 
        layoutPrincipal.setPadding(new Insets(30));
        layoutPrincipal.setAlignment(Pos.CENTER);
        layoutPrincipal.setStyle("-fx-background-color: #2b2b2b;");

        // --- 1. O TABULEIRO ---
        GridPane tabuleiroVisual = new GridPane();
        
        Color corCasa = Color.DARKGREEN;
        Color corLinha = Color.BLACK;

        for (int linha = 0; linha < 8; linha++) {
            for (int coluna = 0; coluna < 8; coluna++) {
                // Usar StackPane para poder sobrepor peças às casas
                StackPane casa = new StackPane();
                
                // O fundo da casa (quadrado)
                Rectangle fundo = new Rectangle(60, 60);
                fundo.setFill(corCasa);
                fundo.setStroke(corLinha);
                fundo.setStrokeWidth(2); 
                
                // Adicionar o fundo verde à casa
                casa.getChildren().add(fundo);
                
                // --- COLOCAR AS 4 PEÇAS INICIAIS ---
                // Linha 3 e 4, Coluna 3 e 4 representam o centro do tabuleiro (0 a 7)
                if ((linha == 3 && coluna == 3) || (linha == 4 && coluna == 4)) {
                    Circle pecaBranca = new Circle(25, Color.WHITE);
                    casa.getChildren().add(pecaBranca); // Fica por cima do fundo
                } 
                else if ((linha == 3 && coluna == 4) || (linha == 4 && coluna == 3)) {
                    Circle pecaPreta = new Circle(25, Color.BLACK);
                    casa.getChildren().add(pecaPreta); // Fica por cima do fundo
                }
                
                // Adicionar a casa (já com fundo e possível peça) à grelha principal
                tabuleiroVisual.add(casa, coluna, linha);
            }
        }

        // --- 2. PAINEL DE INFORMAÇÃO ---
        VBox painelInfo = new VBox(30);
        painelInfo.setAlignment(Pos.TOP_CENTER);
        painelInfo.setMinWidth(220);

        Label titulo = new Label("REVERSI");
        titulo.setStyle("-fx-text-fill: white; -fx-font-size: 34px; -fx-font-weight: bold;");

        Label vezDe = new Label("PRÓXIMA JOGADA:\nPRETAS");
        vezDe.setAlignment(Pos.CENTER);
        vezDe.setStyle("-fx-text-fill: #f1c40f; -fx-font-size: 18px; -fx-font-weight: bold; -fx-text-alignment: center;");

        VBox boxPontos = new VBox(15);
        boxPontos.setAlignment(Pos.CENTER);
        boxPontos.setStyle("-fx-background-color: #3c3f41; -fx-padding: 20; -fx-background-radius: 15;");

        Label labelPretas = new Label("Pretas: 2");
        labelPretas.setStyle("-fx-text-fill: white; -fx-font-size: 18px;");
        
        Label labelBrancas = new Label("Brancas: 2");
        labelBrancas.setStyle("-fx-text-fill: white; -fx-font-size: 18px;");

        boxPontos.getChildren().addAll(labelPretas, labelBrancas);
        painelInfo.getChildren().addAll(titulo, vezDe, boxPontos);

        // --- 3. MONTAGEM FINAL ---
        layoutPrincipal.getChildren().addAll(tabuleiroVisual, painelInfo);

        Scene cena = new Scene(layoutPrincipal);
        stage.setTitle("Reversi - Grupo 05");
        stage.setScene(cena);
        stage.setResizable(false); 
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}