package com.lojas.moria.service;

import com.lojas.moria.execption.ProdutoNaoEncontradoException;
import com.lojas.moria.model.CategoriaProduto;
import com.lojas.moria.model.Produto;
import com.lojas.moria.repository.ProdutoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
/*Aqui e onde eu coloco as regras de nogocios, como eu quero que o sitema se comporte. O meu service esta conversando com o
repository. e o repository*/
public class ProdutoService {
    @Autowired
    private ProdutoRepository produtoRepository;


    public List<Produto> listarProdutos(){
        return produtoRepository.findAll();
    }

    public Produto buscarPorId(Long id){
        return produtoRepository.findById(id).orElseThrow(() -> new ProdutoNaoEncontradoException("Produto nao econtrado com o ID: " + id));
    }
    public List<Produto> buscarPorNome(String nome) {
        return produtoRepository.findByNomeContainingIgnoreCase(nome);
    }

    public Produto cadastrarProduto(Produto produto){
        return produtoRepository.save(produto);
    }
        public  Produto atulizarProduto(Long id, Produto produto){
         Produto produtoExistente = produtoRepository.findById(id).orElseThrow(() -> new ProdutoNaoEncontradoException("Produto nao encontrado com o ID: " + id));

         produtoExistente.setNome(produto.getNome());
         produtoExistente.setPreco(produto.getPreco());
         produtoExistente.setDescricao(produto.getDescricao());
         produtoExistente.setQuantidadeEmEstoque(produto.getQuantidadeEmEstoque());
         produtoExistente.setCategoria(produto.getCategoria());

            return  produtoRepository.save(produtoExistente);
        }

   public void  excluirProduto(Long id){
         produtoRepository.findById(id)
                 .orElseThrow(()-> new ProdutoNaoEncontradoException("Produto nao encontrado com o ID: " +id));

         produtoRepository.deleteById(id);

   }
    public List<Produto> buscarPorCategoria(CategoriaProduto categoria) {
        return produtoRepository.findByCategoria(categoria);
    }
}
