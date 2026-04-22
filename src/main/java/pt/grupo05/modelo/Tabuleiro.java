package pt.grupo05.modelo;

/**
 * Gere a grelha 8x8 do jogo Reversi.
 * Responsável por colocar, virar e contar as peças.
 */
public class Tabuleiro {
    
    // O atributo que definiste no UML: uma matriz 8x8 de CorPeca
    private CorPeca[][] grelha;

    // Construtor: corre automaticamente quando criamos um Tabuleiro novo
    public Tabuleiro() {
        grelha = new CorPeca[8][8]; // Cria a grelha de 8x8 casas
        inicializar();              // Chama logo a função para pôr as peças iniciais
    }

    /**
     * Limpa o tabuleiro e coloca as 4 peças iniciais no centro.
     */
    public void inicializar() {
        // 1. Pôr todas as 64 casas VAZIAS primeiro
        for (int linha = 0; linha < 8; linha++) {
            for (int coluna = 0; coluna < 8; coluna++) {
                grelha[linha][coluna] = CorPeca.VAZIO;
            }
        }
        
        // 2. Colocar as 4 peças centrais cruzadas (posições 3 e 4)
        grelha[3][3] = CorPeca.BRANCO;
        grelha[4][4] = CorPeca.BRANCO;
        
        grelha[3][4] = CorPeca.PRETO;
        grelha[4][3] = CorPeca.PRETO;
    }

    // --- MÉTODOS DO UML (Por enquanto vazios, só para montar a estrutura) ---

    public boolean colocarPeca(int x, int y, CorPeca cor) {
        // A lógica de colocar a peça virá para aqui
        return false; 
    }

    public void virarPecas(int x, int y, CorPeca cor) {
        // A lógica de virar as peças virá para aqui
    }

    public int contarPecas(CorPeca cor) {
        // A lógica de contar as peças virá para aqui
        return 0;
    }

    public CorPeca getCasa(int x, int y) {
        return grelha[y][x]; // Devolve o que está numa casa específica
    }
}