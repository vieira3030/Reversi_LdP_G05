package pt.grupo05.cliente;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

public class App extends Application {

    @Override
    public void start(Stage stage) {
        // Layout principal: HBox (para pôr o tabuleiro e as informações lado a lado)
        HBox layoutPrincipal = new HBox(30); // 30 pixels de distância entre o tabuleiro e o painel
        layoutPrincipal.setPadding(new Insets(20)); // Margem à volta da janela toda

        // 1. O Tabuleiro (Metade Esquerda)
        GridPane tabuleiroVisual = new GridPane();
        for (int linha = 0; linha < 8; linha++) {
            for (int coluna = 0; coluna < 8; coluna++) {
                Rectangle casa = new Rectangle(60, 60);
                casa.setFill(Color.DARKGREEN);
                casa.setStroke(Color.BLACK);
                tabuleiroVisual.add(casa, coluna, linha);
            }
        }

        // 2. O Painel de Informações (Metade Direita) -
        VBox painelInfo = new VBox(20); // 20 pixels entre cada linha de texto
        painelInfo.setAlignment(Pos.CENTER_LEFT); // Alinhar o texto direitinho

        Label titulo = new Label("REVERSI");
        titulo.setStyle("-fx-font-size: 28px; -fx-font-weight: bold;");

        Label infoVez = new Label("Vez de jogar: Pretas");
        infoVez.setStyle("-fx-font-size: 18px;");

        Label pontuacao = new Label("Pretas: 2  |  Brancas: 2");
        pontuacao.setStyle("-fx-font-size: 16px;");

        // Adicionar os textos ao painel da direita
        painelInfo.getChildren().addAll(titulo, infoVez, pontuacao);

        // Juntar as duas metades no layout principal
        layoutPrincipal.getChildren().addAll(tabuleiroVisual, painelInfo);

        // Criar e mostrar a janela
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