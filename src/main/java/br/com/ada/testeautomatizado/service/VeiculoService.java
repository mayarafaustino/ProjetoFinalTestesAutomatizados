package br.com.ada.testeautomatizado.service;

import br.com.ada.testeautomatizado.dto.VeiculoDTO;
import br.com.ada.testeautomatizado.exception.PlacaInvalidaException;
import br.com.ada.testeautomatizado.exception.VeiculoNaoEncontradoException;
import br.com.ada.testeautomatizado.model.Veiculo;
import br.com.ada.testeautomatizado.repository.VeiculoRepository;
import br.com.ada.testeautomatizado.dto.ResponseDTO;
import br.com.ada.testeautomatizado.util.ValidacaoPlaca;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
public class VeiculoService {

    @Autowired
    private VeiculoRepository veiculoRepository;

    @Autowired
    private ValidacaoPlaca validacaoPlaca;

    public ResponseEntity<ResponseDTO<VeiculoDTO>> cadastrar(VeiculoDTO veiculoDTO) {
        log.debug("Executando cadastrar no VeiculoService {}");
        try {
            validacaoPlaca.isPlacaValida(veiculoDTO.getPlaca());
            Veiculo veiculo = new Veiculo();
            veiculo.setPlaca(veiculoDTO.getPlaca());
            veiculo.setMarca(veiculoDTO.getMarca());
            veiculo.setModelo(veiculoDTO.getModelo());
            veiculo.setDisponivel(veiculoDTO.getDisponivel());
            veiculo.setDataFabricacao(veiculoDTO.getDataFabricacao());
            veiculoRepository.save(veiculo);
            return ResponseEntity.ok(new ResponseDTO<VeiculoDTO>("Sucesso", veiculoDTO));
        }catch (PlacaInvalidaException e) {
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                    .body(new ResponseDTO<VeiculoDTO>(e.getMessage(), null));
        }catch (Exception e) {
            log.error("Erro no cadastrar do ClienteService {}", e.getMessage());
            throw e;
        }
    }

    public ResponseEntity<ResponseDTO<Boolean>> deletarVeiculoPelaPlaca(String placa) {

        try {
            buscarVeiculoPelaPlaca(placa).ifPresent(veiculoRepository::delete);
            return ResponseEntity.ok(new ResponseDTO<>("Sucesso", Boolean.TRUE));
        } catch (PlacaInvalidaException e) {
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                    .body(new ResponseDTO<>(e.getMessage(), null));
        }

    }

    public ResponseEntity<ResponseDTO<VeiculoDTO>> atualizar(VeiculoDTO veiculoDTO) {

        try {

            validacaoPlaca.isPlacaValida(veiculoDTO.getPlaca());

            Optional<Veiculo> optionalVeiculo = veiculoRepository.findByPlaca(veiculoDTO.getPlaca());
            if (optionalVeiculo.isPresent()) {

                Veiculo veiculo = new Veiculo();
                veiculo.setId(optionalVeiculo.get().getId());
                veiculo.setMarca(veiculoDTO.getMarca());
                veiculo.setModelo(veiculoDTO.getModelo());
                veiculo.setDisponivel(veiculoDTO.getDisponivel());
                veiculo.setDataFabricacao(veiculoDTO.getDataFabricacao());
                veiculoRepository.save(veiculo);

                Veiculo veiculoAtualizadoBD = veiculoRepository.save(veiculo);
                VeiculoDTO veiculoDTOAtualizado = new VeiculoDTO(
                        veiculoAtualizadoBD.getPlaca(),
                        veiculoAtualizadoBD.getModelo(),
                        veiculoAtualizadoBD.getMarca(),
                        veiculoAtualizadoBD.getDisponivel(),
                        veiculoAtualizadoBD.getDataFabricacao()
                );
                ResponseDTO<VeiculoDTO> response = new ResponseDTO<>("Sucesso", veiculoDTOAtualizado);

                return ResponseEntity.ok(response);
            } else {
                throw new VeiculoNaoEncontradoException();
            }
        } catch (VeiculoNaoEncontradoException e) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        } catch (PlacaInvalidaException e) {
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }

    }

    public ResponseEntity<ResponseDTO<List<VeiculoDTO>>> listarTodos() {
        try {
            List<VeiculoDTO> collect = veiculoRepository.findAll().stream().map(veiculo -> {
                VeiculoDTO veiculoDTO = new VeiculoDTO();
                veiculoDTO.setPlaca(veiculo.getPlaca());
                veiculoDTO.setMarca(veiculo.getMarca());
                veiculoDTO.setModelo(veiculo.getModelo());
                veiculoDTO.setDataFabricacao(veiculo.getDataFabricacao());
                veiculoDTO.setDisponivel(veiculo.getDisponivel());
                return veiculoDTO;
            }).collect(Collectors.toList());

            ResponseDTO<List<VeiculoDTO>> responseDTO = new ResponseDTO<>("Sucesso",collect);

            return ResponseEntity.ok(responseDTO);
        } catch ( VeiculoNaoEncontradoException e){
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        }

    }

    private Optional<Veiculo> buscarVeiculoPelaPlaca(String placa) {
        return this.veiculoRepository.findByPlaca(placa);
    }
}

