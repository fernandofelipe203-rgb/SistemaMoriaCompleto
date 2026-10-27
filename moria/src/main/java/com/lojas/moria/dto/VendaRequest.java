
package com.lojas.moria.dto;

import com.lojas.moria.model.FormaPagamento;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public class VendaRequest {

    private String cliente;

    @NotNull(message = "A forma de pagamento é obrigatória")
    private FormaPagamento pagamento;

    @NotNull(message = "A venda deve possuir uma lista de itens")
    @Size(min = 1, message = "A venda deve possuir pelo menos um produto")
    @Valid
    private List<ItemVendaRequest> itens;

    public VendaRequest() {
    }

    public String getCliente() {
        return cliente;
    }

    public void setCliente(String cliente) {
        this.cliente = cliente;
    }

    public FormaPagamento getPagamento() {
        return pagamento;
    }

    public void setPagamento(FormaPagamento pagamento) {
        this.pagamento = pagamento;
    }

    public List<ItemVendaRequest> getItens() {
        return itens;
    }

    public void setItens(List<ItemVendaRequest> itens) {
        this.itens = itens;
    }
}
