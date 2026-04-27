package pt.grupo05.modelo;

import java.util.List;

/**
 * Gere a grelha 8x8 do jogo Reversi.
 * Responsável por inicializar, colocar, inverter e contar as peças[cite: 171, 194].
 */
public class Tabuleiro {
    
    // Matriz bidimensional que representa as 64 casas do tabuleiro[cite: 196, 222].
    private CorPeca[][] grelha;

    /**
     * Construtor da classe Tabuleiro.
     * Instancia a grelha e define o estado inicial das peças.
     */
    public Tabuleiro() {
        grelha = new CorPeca[8][8];
        inicializar();
    }

    /**
     * Limpa o tabuleiro e posiciona as 4 peças iniciais no centro[cite: 142, 198].
     */
    public void inicializar() {
        // Define todas as posições da matriz como VAZIO[cite: 180, 222].
        for (int linha = 0; linha < 8; linha++) {
            for (int coluna = 0; coluna < 8; coluna++) {
                grelha[linha][coluna] = CorPeca.VAZIO;
            }
        }
        
        // Define a configuração inicial padrão do Reversi[cite: 142, 147].
        grelha[3][3] = CorPeca.BRANCO;
        grelha[4][4] = CorPeca.BRANCO;
        grelha[3][4] = CorPeca.PRETO;
        grelha[4][3] = CorPeca.PRETO;
    }

    /**
     * Coloca uma peça no tabuleiro numa posição específica[cite: 155, 199].
     * @param x Coluna (0-7)
     * @param y Linha (0-7)
     * @param cor Cor da peça a colocar
     * @return true se a peça foi colocada com sucesso
     */
    public boolean colocarPeca(int x, int y, CorPeca cor) {
        if (x >= 0 && x < 8 && y >= 0 && y < 8) {
            grelha[y][x] = cor;
            return true;
        }
        return false; 
    }

    /**
     * Inverte a cor das peças capturadas numa jogada[cite: 157, 200].
     * @param x Coluna (0-7) da jogada realizada
     * @param y Linha (0-7) da jogada realizada
     * @param cor Cor do jogador que efetuou a jogada
     */
    public void virarPecas(int x, int y, CorPeca cor) {
        // Obtém a lista de coordenadas das peças a inverter através das regras[cite: 219].
        List<int[]> capturadas = Regras.pecasCapturadas(this, x, y, cor);
        for (int[] pos : capturadas) {
            colocarPeca(pos[0], pos[1], cor);
        }
    }

    /**
     * Conta o número total de peças de uma determinada cor no tabuleiro[cite: 159, 201].
     * @param cor Cor da peça a ser contabilizada
     * @return O número total de peças dessa cor
     */
    public int contarPecas(CorPeca cor) {
        int contagem = 0;
        for (int y = 0; y < 8; y++) {
            for (int x = 0; x < 8; x++) {
                if (grelha[y][x] == cor) {
                    contagem++;
                }
            }
        }
        return contagem;
    }

    /**
     * Obtém o estado de uma casa específica do tabuleiro[cite: 202].
     * @param x Coluna (0-7)
     * @param y Linha (0-7)
     * @return O estado atual da casa (PRETO, BRANCO ou VAZIO)[cite: 180].
     */
    public CorPeca getCasa(int x, int y) {
        return grelha[y][x];
    }
}