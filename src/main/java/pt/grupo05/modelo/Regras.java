package pt.grupo05.modelo;

import java.util.ArrayList;
import java.util.List;

/**
 * Classe utilitária que valida jogadas e verifica o estado da partida.
 */
public class Regras {

    // Define as 8 direções possíveis no tabuleiro (dx, dy).
    private static final int[][] DIRECOES = {
        {-1, -1}, {0, -1}, {1, -1},
        {-1,  0},          {1,  0},
        {-1,  1}, {0,  1}, {1,  1}
    };

    /**
     * Verifica se uma jogada é válida numa determinada posição.
     */
    public static boolean jogadaValida(Tabuleiro tab, int x, int y, CorPeca cor) {
        // A posição deve estar vazia.
        if (tab.getCasa(x, y) != CorPeca.VAZIO) {
            return false;
        }

        // A jogada deve capturar pelo menos uma peça adversária.
        return !pecasCapturadas(tab, x, y, cor).isEmpty();
    }

    /**
     * Devolve uma lista com as coordenadas [x, y] das peças a inverter.
     */
    public static List<int[]> pecasCapturadas(Tabuleiro tab, int x, int y, CorPeca cor) {
        List<int[]> capturadas = new ArrayList<>();
        CorPeca corAdversario = (cor == CorPeca.PRETO) ? CorPeca.BRANCO : CorPeca.PRETO;

        // Itera sobre as 8 direções adjacentes.
        for (int[] direcao : DIRECOES) {
            int dx = direcao[0];
            int dy = direcao[1];
            
            int atualX = x + dx;
            int atualY = y + dy;
            
            List<int[]> possiveisCapturas = new ArrayList<>();

            // Percorre a direção enquanto encontrar peças do adversário.
            while (atualX >= 0 && atualX < 8 && atualY >= 0 && atualY < 8 && tab.getCasa(atualX, atualY) == corAdversario) {
                possiveisCapturas.add(new int[]{atualX, atualY});
                atualX += dx;
                atualY += dy;
            }

            // Valida a captura se a sequência terminar com uma peça da cor do jogador atual.
            if (atualX >= 0 && atualX < 8 && atualY >= 0 && atualY < 8 && tab.getCasa(atualX, atualY) == cor && !possiveisCapturas.isEmpty()) {
                capturadas.addAll(possiveisCapturas);
            }
        }

        return capturadas;
    }

    /**
     * Verifica se existem jogadas válidas para uma determinada cor no tabuleiro.
     */
    public static boolean temJogadasValidas(Tabuleiro tab, CorPeca cor) {
        for (int y = 0; y < 8; y++) {
            for (int x = 0; x < 8; x++) {
                if (jogadaValida(tab, x, y, cor)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Verifica se ambos os jogadores não possuem jogadas válidas.
     */
    public static boolean jogoTerminado(Tabuleiro tab) {
        return !temJogadasValidas(tab, CorPeca.PRETO) && !temJogadasValidas(tab, CorPeca.BRANCO);
    }
}