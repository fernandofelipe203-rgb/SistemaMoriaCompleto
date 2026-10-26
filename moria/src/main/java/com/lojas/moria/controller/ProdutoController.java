package com.lojas.moria.controller;

import com.lojas.moria.model.CategoriaProduto;
import com.lojas.moria.model.Produto;
import com.lojas.moria.service.ProdutoService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/produtos")
public class ProdutoController {
    @Autowired
    private ProdutoService produtoService;


    //Buscando produtos
    @GetMapping
    public List<Produto> listar() {
        return produtoService.listarProdutos();
    }

    //Buscar produto por ID
    @GetMapping("/{id}")
    public Produto buscarPorId(@PathVariable Long id) {
        return produtoService.buscarPorId(id);
    }
    //buscar por nome
    @GetMapping("/buscar")
    public List<Produto> buscarPorNome(@RequestParam String nome) {
        return produtoService.buscarPorNome(nome);
    }

    //Criando produtos
    @PostMapping
    public Produto cadastrar(@Valid @RequestBody Produto produto) {
        return produtoService.cadastrarProduto(produto);

    }
    @PutMapping("/{id}")
    public Produto atulizar(@PathVariable Long id, @RequestBody Produto produto){
        return produtoService.atulizarProduto(id,produto);
    }

    @DeleteMapping("/{id}")
    public  String excluir(@PathVariable Long id){
       produtoService.excluirProduto(id);
       return "Produto excluido com sucesso";
    }
    @GetMapping("/categoria/{categoria}")
    public List<Produto> buscarPorCategoria(
            @PathVariable CategoriaProduto categoria) {
        return produtoService.buscarPorCategoria(categoria);
    }
}


