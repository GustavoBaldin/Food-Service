package foodservice.pagamentos.controller;

import foodservice.pagamentos.dto.PagamentoDTO;
import foodservice.pagamentos.service.PagamentoService;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/pagamentos")
public class PagamentoController {

    @Autowired
    private PagamentoService service;

    @GetMapping
    public ResponseEntity<Page<PagamentoDTO>> retornarTodos(@PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok().body(service.obterTodos(pageable));
    }

    @GetMapping("{id}")
    public ResponseEntity<PagamentoDTO> detalharPorId(@PathVariable @Valid Long id) {
        PagamentoDTO pagamento = service.obterPorId(id);
        return ResponseEntity.ok().body(pagamento);
    }

    @PostMapping
    public ResponseEntity<PagamentoDTO> cadastrar(@RequestBody @Valid PagamentoDTO json, UriComponentsBuilder uriComponentsBuilder) {

        PagamentoDTO pagamento = service.criarPagamento(json);
        URI uri = uriComponentsBuilder.path("/pagamentos/id").buildAndExpand(pagamento).toUri();

        return ResponseEntity.created(uri).body(pagamento);
    }

    @PutMapping("{id}")
    public ResponseEntity<PagamentoDTO> atualizar(@PathVariable Long id, @RequestBody PagamentoDTO json) {
        PagamentoDTO pagamento = service.atualizarPagamento(id, json);
        return ResponseEntity.ok().body(pagamento);
    }

    @DeleteMapping("{id}")
    public ResponseEntity<PagamentoDTO> excluir(@PathVariable Long id) {
        service.excluirPagamento(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/confirmar")
    @CircuitBreaker(name = "atualizarPedidos", fallbackMethod = "pagamentoAutorizadoComIntegracaoPendente")
    public void confirmarPagamento(@PathVariable @NotNull Long id){
        service.confirmaPagamento(id);
    }

    public void pagamentoAutorizadoComIntegracaoPendente(Long id, Exception e){
        service.alteraStatus(id);
    }



}
