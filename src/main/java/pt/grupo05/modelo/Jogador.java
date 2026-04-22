package pt.grupo05.modelo;

/**
 * Representa um jogador no sistema Reversi.
 * Guarda o nome, a cor das peças e a pontuação atual.
 */
public class Jogador {
    
    // Atributos definidos no UML [cite: 205, 206, 207, 208]
    private String nome;
    private CorPeca cor;
    private int pontuacao;

    /**
     * Construtor para criar um novo jogador.
     * @param nome Nome do jogador
     * @param cor Cor das peças (PRETO ou BRANCO)
     */
    public Jogador(String nome, CorPeca cor) {
        this.nome = nome;
        this.cor = cor;
        this.pontuacao = 2; // O jogo começa sempre com 2 peças para cada um [cite: 142]
    }

    // --- Métodos de Acesso (Getters e Setters) [cite: 209, 210, 211, 212, 213] ---

    public String getNome() {
        return nome;
    }

    public CorPeca getCor() {
        return cor;
    }

    public int getPontuacao() {
        return pontuacao;
    }

    public void setPontuacao(int pontuacao) {
        this.pontuacao = pontuacao;
    }
}