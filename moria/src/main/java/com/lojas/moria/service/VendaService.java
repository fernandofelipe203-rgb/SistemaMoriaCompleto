
package com.lojas.moria.service;

import com.lojas.moria.dto.ItemVendaRequest;
import com.lojas.moria.dto.ItemVendaResponse;
import com.lojas.moria.dto.VendaRequest;
import com.lojas.moria.dto.VendaResponse;
import com.lojas.moria.model.ItemMovimentacaoEstoque;
import com.lojas.moria.model.ItemVenda;
import com.lojas.moria.model.MovimentacaoEstoque;
import com.lojas.moria.model.Produto;
import com.lojas.moria.model.TipoMovimentacao;
import com.lojas.moria.model.Venda;
import com.lojas.moria.repository.ProdutoRepository;
import com.lojas.moria.repository.VendaRepository;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class VendaService {

    private final VendaRepository vendaRepository;
    private final ProdutoRepository produtoRepository;
    private final MovimentacaoEstoqueService movimentacaoEstoqueService;

    public VendaService(
            VendaRepository vendaRepository,
            ProdutoRepository produtoRepository,
            MovimentacaoEstoqueService movimentacaoEstoqueService) {

        this.vendaRepository = vendaRepository;
        this.produtoRepository = produtoRepository;
        this.movimentacaoEstoqueService = movimentacaoEstoqueService;
    }

    @Transactional
    public VendaResponse registrarVenda(VendaRequest request) {

        if (request == null
                || request.getPagamento() == null
                || request.getItens() == null
                || request.getItens().isEmpty()) {
            throw new IllegalArgumentException(
                    "Informe o pagamento e pelo menos um produto");
        }

        Venda venda = new Venda();
        venda.setCliente(request.getCliente());
        venda.setPagamento(request.getPagamento());
        venda.setTotal(BigDecimal.ZERO);

        BigDecimal total = BigDecimal.ZERO;

        MovimentacaoEstoque movimentacao = new MovimentacaoEstoque();
        movimentacao.setTipo(TipoMovimentacao.SAIDA);
        movimentacao.setDescricao("Saída referente a uma venda");

        for (ItemVendaRequest itemRequest : request.getItens()) {

            if (itemRequest == null
                    || itemRequest.getProdutoId() == null
                    || itemRequest.getQuantidade() == null
                    || itemRequest.getQuantidade() <= 0) {
                throw new IllegalArgumentException(
                        "Informe um produto válido e uma quantidade maior que zero");
            }

            Produto produto = produtoRepository
                    .findById(itemRequest.getProdutoId())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Produto não encontrado: "
                                    + itemRequest.getProdutoId()));

            if (produto.getPreco() == null
                    || produto.getPreco().compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalStateException(
                        "O produto possui um preço inválido: "
                                + produto.getNome());
            }

            ItemVenda itemVenda = new ItemVenda();
            itemVenda.setProduto(produto);
            itemVenda.setQuantidade(itemRequest.getQuantidade());

            // O preço vem do banco, não do frontend.
            itemVenda.setPrecoUnitario(produto.getPreco());

            venda.adicionarItem(itemVenda);

            BigDecimal subtotal = produto.getPreco().multiply(
                    BigDecimal.valueOf(itemRequest.getQuantidade()));

            total = total.add(subtotal);

            ItemMovimentacaoEstoque itemMovimentacao =
                    new ItemMovimentacaoEstoque();

            itemMovimentacao.setProduto(produto);
            itemMovimentacao.setQuantidade(itemRequest.getQuantidade());

            movimentacao.adicionarItem(itemMovimentacao);
        }

        venda.setTotal(total);

        // Salva a venda e seus itens.
        Venda vendaSalva = vendaRepository.save(venda);

        // Registra a saída e atualiza o estoque usando a regra existente.
        movimentacao.setDescricao(
                "Saída referente à venda #" + vendaSalva.getId());

        movimentacaoEstoqueService.registrarMovimentacao(movimentacao);

        return converterParaResponse(vendaSalva);
    }

    public List<VendaResponse> listarVendas(int quantidade) {

        if (quantidade <= 0) {
            throw new IllegalArgumentException(
                    "A quantidade deve ser maior que zero");
        }

        Pageable pagina = PageRequest.of(0, quantidade);

        return vendaRepository.findAllByOrderByDataDesc(pagina)
                .stream()
                .map(this::converterParaResponse)
                .collect(Collectors.toList());
    }

    private VendaResponse converterParaResponse(Venda venda) {
        VendaResponse response = new VendaResponse();

        response.setId(venda.getId());
        response.setData(venda.getData());
        response.setCliente(venda.getCliente());
        response.setPagamento(venda.getPagamento());
        response.setTotal(venda.getTotal());

        List<ItemVendaResponse> itens = venda.getItens()
                .stream()
                .map(item -> {
                    ItemVendaResponse itemResponse = new ItemVendaResponse();

                    itemResponse.setProdutoId(item.getProduto().getId());
                    itemResponse.setCodigo(item.getProduto().getCodigo());
                    itemResponse.setNome(item.getProduto().getNome());
                    itemResponse.setQuantidade(item.getQuantidade());
                    itemResponse.setPrecoUnitario(item.getPrecoUnitario());
                    itemResponse.setSubtotal(item.getSubtotal());

                    return itemResponse;
                })
                .collect(Collectors.toList());

        response.setItens(itens);

        return response;
    }
}
