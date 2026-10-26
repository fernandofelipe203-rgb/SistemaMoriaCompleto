
package com.lojas.moria.service;

import com.lojas.moria.execption.EstoqueInsuficienteException;
import com.lojas.moria.execption.MovimentacaoNaoEncontradaException;
import com.lojas.moria.execption.ProdutoNaoEncontradoException;
import com.lojas.moria.model.ItemMovimentacaoEstoque;
import com.lojas.moria.model.MovimentacaoEstoque;
import com.lojas.moria.model.Produto;
import com.lojas.moria.model.TipoMovimentacao;
import com.lojas.moria.repository.MovimentacaoEstoqueRepository;
import com.lojas.moria.repository.ProdutoRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MovimentacaoEstoqueService {

    private final MovimentacaoEstoqueRepository movimentacaoEstoqueRepository;

    private final ProdutoRepository produtoRepository;


    public MovimentacaoEstoqueService(
            MovimentacaoEstoqueRepository movimentacaoEstoqueRepository,
            ProdutoRepository produtoRepository) {

        this.movimentacaoEstoqueRepository = movimentacaoEstoqueRepository;
        this.produtoRepository = produtoRepository;
    }



    public MovimentacaoEstoque salvar(MovimentacaoEstoque movimentacao) {
        return movimentacaoEstoqueRepository.save(movimentacao);
    }
    public Produto buscarProduto(Long id) {
        return produtoRepository.findById(id)
                .orElseThrow(() ->
                        new ProdutoNaoEncontradoException("Produto não encontrado"));
    }

    @Transactional
    public void registrarMovimentacao(MovimentacaoEstoque movimentacao) {

        if (movimentacao.getTipo() == null) {
            throw new IllegalArgumentException(
                    "O tipo da movimentação é obrigatório");
        }

        if (movimentacao.getTipo() != TipoMovimentacao.ENTRADA
                && movimentacao.getTipo() != TipoMovimentacao.SAIDA) {
            throw new IllegalArgumentException(
                    "Tipo de movimentação não suportado");
        }
        if (movimentacao.getItens() == null
                || movimentacao.getItens().isEmpty()) {
            throw new IllegalArgumentException(
                    "A movimentação deve possuir pelo menos um produto");
        }
        for (ItemMovimentacaoEstoque item : movimentacao.getItens()) {

            if (item.getQuantidade() == null || item.getQuantidade() <= 0) {
                throw new IllegalArgumentException(
                        "A quantidade do item deve ser maior que zero");
            }

            if (item.getProduto() == null
                    || item.getProduto().getId() == null) {
                throw new IllegalArgumentException(
                        "O produto do item é obrigatório");
            }
            Produto produto = buscarProduto(item.getProduto().getId());

            item.setProduto(produto);
            Integer estoqueAtual = produto.getQuantidadeEmEstoque();

            if (estoqueAtual == null || estoqueAtual < 0) {
                throw new IllegalStateException(
                        "O estoque atual do produto é inválido");
            }

            if (movimentacao.getTipo() == TipoMovimentacao.ENTRADA) {


                Integer quantidadeEntrada = item.getQuantidade();
                if (quantidadeEntrada > Integer.MAX_VALUE - estoqueAtual) {
                    throw new IllegalArgumentException(
                            "A quantidade ultrapassa o limite permitido para o estoque");
                }

                produto.setQuantidadeEmEstoque(estoqueAtual + quantidadeEntrada);


            } else if (movimentacao.getTipo() == TipoMovimentacao.SAIDA) {


                Integer quantidadeSaida = item.getQuantidade();

                if (quantidadeSaida > estoqueAtual) {
                    throw new EstoqueInsuficienteException(
                            "Estoque insuficiente para o produto: " + produto.getNome()
                    );
                }

                produto.setQuantidadeEmEstoque(estoqueAtual - quantidadeSaida);


            }
        }
        movimentacaoEstoqueRepository.save(movimentacao);
    }



    public List<MovimentacaoEstoque> listarTodas() {
        return movimentacaoEstoqueRepository.findAll();
    }


    public MovimentacaoEstoque buscarPorId(Integer id) {
        return movimentacaoEstoqueRepository.findById(id)
                .orElseThrow(() ->
                        new MovimentacaoNaoEncontradaException(
                                "Movimentação não encontrada"));
    }



}
