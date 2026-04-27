package pt.grupo05.modelo;

/**
 * Representa um jogador no jogo Reversi.
 * Mantém o registo do nome, da cor das peças e da pontuação atual.
 */
public class Jogador {
    
    // Nome do jogador.
    private String nome;
    
    // Cor das peças atribuídas ao jogador (PRETO ou BRANCO).
    private CorPeca cor;
    
    // Pontuação atual do jogador (número de peças no tabuleiro).
    private int pontuacao;

    /**
     * Instancia um novo jogador com a pontuação inicial predefinida.
     * * @param nome Nome do jogador
     * @param cor Cor das peças atribuídas ao jogador
     */
    public Jogador(String nome, CorPeca cor) {
        this.nome = nome;
        this.cor = cor;
        // A pontuação inicial é sempre 2 peças.
        this.pontuacao = 2; 
    }

    /**
     * Obtém o nome do jogador.
     * * @return O nome do jogador
     */
    public String getNome() {
        return nome;
    }

    /**
     * Obtém a cor das peças do jogador.
     * * @return A cor (PRETO ou BRANCO)
     */
    public CorPeca getCor() {
        return cor;
    }

    /**
     * Obtém a pontuação atual do jogador.
     * * @return O número de peças do jogador presentes no tabuleiro
     */
    public int getPontuacao() {
        return pontuacao;
    }

    /**
     * Atualiza a pontuação do jogador.
     * * @param pontuacao O novo valor da pontuação
     */
    public void setPontuacao(int pontuacao) {
        this.pontuacao = pontuacao;
    }
}