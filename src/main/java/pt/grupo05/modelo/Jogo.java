package pt.grupo05.modelo;

/**
 * Controla o fluxo geral do jogo Reversi.
 * Gere os turnos, a validação de jogadas e o estado da partida.
 */
public class Jogo {
    
    private Tabuleiro tabuleiro;
    private Jogador jogador1;
    private Jogador jogador2;
    private Jogador jogadorAtual;
    private boolean jogoAtivo;

    /**
     * Construtor da classe Jogo.
     * Instancia o tabuleiro e os jogadores.
     */
    public Jogo() {
        tabuleiro = new Tabuleiro();
        jogador1 = new Jogador("Jogador 1", CorPeca.PRETO);
        jogador2 = new Jogador("Jogador 2", CorPeca.BRANCO);
        iniciarJogo();
    }

    /**
     * Inicializa uma nova partida.
     */
    public void iniciarJogo() {
        tabuleiro.inicializar();
        jogadorAtual = jogador1; 
        jogoAtivo = true;
        atualizarPontuacoes();
    }

    /**
     * Executa uma jogada no tabuleiro.
     * @param x Coluna (0-7)
     * @param y Linha (0-7)
     * @return true se a jogada for válida e executada, false caso contrário
     */
    public boolean jogar(int x, int y) {
        if (!jogoAtivo) {
            return false;
        }

        if (Regras.jogadaValida(tabuleiro, x, y, jogadorAtual.getCor())) {
            // Coloca a peça e inverte as peças capturadas
            tabuleiro.colocarPeca(x, y, jogadorAtual.getCor());
            tabuleiro.virarPecas(x, y, jogadorAtual.getCor());
            
            atualizarPontuacoes();
            
            // Verifica o estado do jogo após a jogada
            if (Regras.jogoTerminado(tabuleiro)) {
                jogoAtivo = false;
            } else {
                trocarJogador();
                
                // Se o próximo jogador não tiver jogadas válidas, o turno passa novamente
                if (!Regras.temJogadasValidas(tabuleiro, jogadorAtual.getCor())) {
                    trocarJogador();
                    
                    // Validação de segurança dupla
                    if (!Regras.temJogadasValidas(tabuleiro, jogadorAtual.getCor())) {
                        jogoAtivo = false;
                    }
                }
            }
            return true;
        }
        return false;
    }

    /**
     * Alterna o jogador atual.
     */
    public void trocarJogador() {
        if (jogadorAtual == jogador1) {
            jogadorAtual = jogador2;
        } else {
            jogadorAtual = jogador1;
        }
    }

    /**
     * Determina o vencedor da partida.
     * @return O objeto Jogador vencedor, ou null em caso de empate ou jogo ativo
     */
    public Jogador getVencedor() {
        if (jogoAtivo) {
            return null;
        }

        if (jogador1.getPontuacao() > jogador2.getPontuacao()) {
            return jogador1;
        } else if (jogador2.getPontuacao() > jogador1.getPontuacao()) {
            return jogador2;
        }
        return null; // Representa um empate
    }

    /**
     * Atualiza as pontuações dos jogadores com base no estado do tabuleiro.
     */
    private void atualizarPontuacoes() {
        jogador1.setPontuacao(tabuleiro.contarPecas(jogador1.getCor()));
        jogador2.setPontuacao(tabuleiro.contarPecas(jogador2.getCor()));
    }

    // Métodos de acesso (Getters) necessários para a interface gráfica
    public Tabuleiro getTabuleiro() { return tabuleiro; }
    public Jogador getJogadorAtual() { return jogadorAtual; }
    public boolean isJogoAtivo() { return jogoAtivo; }
    public Jogador getJogador1() { return jogador1; }
    public Jogador getJogador2() { return jogador2; }
}