package br.com.rdevs.ecommerce.cadastro.service;

import br.com.rdevs.ecommerce.cadastro.model.dto.*;
import br.com.rdevs.ecommerce.cadastro.model.entity.TbCartaoCredito;
import br.com.rdevs.ecommerce.cadastro.model.entity.TbCliente;
import br.com.rdevs.ecommerce.cadastro.model.entity.TbEndereco;
import br.com.rdevs.ecommerce.cadastro.model.entity.TbEnderecoCliente;
import br.com.rdevs.ecommerce.cadastro.repository.*;
import br.com.rdevs.ecommerce.cadastro.service.bo.CadastroBO;
import br.com.rdevs.ecommerce.cadastro.service.bo.CartaoCreditoBO;
import br.com.rdevs.ecommerce.cadastro.service.bo.EnderecoBO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

@Service
public class ClienteService {
    @Autowired
    private Cadastro2Repository cadastro2Repository;

    @Autowired
    private CadastroRepository cadastroRepository;
    @Autowired
    private CartaoRepository cartaoRepository;
    @Autowired
    private EnderecoRepository enderecoRepository;
    @Autowired
    private EnderecoClienteRepository enderecoClienteRepository;
    @Autowired
    private CadastroBO cadastroBO;
    @Autowired
    private EnderecoBO enderecoBO ;
    @Autowired
    private CartaoCreditoBO cartaoCreditoBO;
    @PersistenceContext
    private EntityManager manager;


    public List<ClienteDTO> buscarTodas(){
        List<ClienteDTO> listDTO = new ArrayList<>();
        List<TbCliente> listEntity = cadastroRepository.findAll();

        for(TbCliente clienteEntity : listEntity){
            ClienteDTO clienteDTO = cadastroBO.parseToDTO(clienteEntity);
            List<EnderecoDTO> enderecoDTOS = new ArrayList<>();
            for (TbEndereco enderecoEntity: clienteEntity.getEnderecos()){
                EnderecoDTO dto = enderecoBO.parseToDTO(enderecoEntity);
                enderecoDTOS.add(dto);
            }
            clienteDTO.setEnderecos(enderecoDTOS);
            List<CartaoCreditoDTO> cartaoCreditoDTOS = new ArrayList<>();
            for (TbCartaoCredito cartaoCreditoEntity: clienteEntity.getCartoesCredito()){
                CartaoCreditoDTO dtoCard = cartaoCreditoBO.parseToDTO(cartaoCreditoEntity);
                cartaoCreditoDTOS.add(dtoCard);
            }
            clienteDTO.setCartoesCreditoDTO(cartaoCreditoDTOS);
            listDTO.add(clienteDTO);
        }

        return listDTO;
    }

    public TbCliente buscarPorCpf(String cpf){
        return cadastroRepository.findByNrCpf(cpf);
    }

    public ClienteDTO buscarPorId(BigInteger idCliente){
        TbCliente clienteEntity=cadastroRepository.findByIdCliente(idCliente);
        ClienteDTO clienteDTO = cadastroBO.parseToDTO(clienteEntity);

        List<EnderecoDTO> enderecoDTOS = new ArrayList<>();
        for (TbEndereco enderecoEntity: clienteEntity.getEnderecos()){
            EnderecoDTO dto = enderecoBO.parseToDTO(enderecoEntity);
            enderecoDTOS.add(dto);
        }
        clienteDTO.setEnderecos(enderecoDTOS);

        List<CartaoCreditoDTO> cartaoCreditoDTOS = new ArrayList<>();
        for (TbCartaoCredito cartaoCreditoEntity: clienteEntity.getCartoesCredito()){

            CartaoCreditoDTO dtoCard = cartaoCreditoBO.parseToDTO(cartaoCreditoEntity);

            cartaoCreditoDTOS.add(dtoCard);

        }
        clienteDTO.setCartoesCreditoDTO(cartaoCreditoDTOS);

        return clienteDTO;
    }

    public TbCliente inserir (ClienteDTO clienteDTO){
        TbCliente clienteEntity = cadastroBO.parseToEntity(clienteDTO, null);
        if (clienteDTO.getPwCliente().equals(clienteDTO.getConfirmarSenha())){
            List<TbEndereco> enderecos = new ArrayList<>();
            for (EnderecoDTO enderecoDTO: clienteDTO.getEnderecos()){
                TbEndereco endereco = enderecoBO.parseToEntity(enderecoDTO,null);
                enderecos.add(endereco);
            }
            clienteEntity.setEnderecos(enderecos);

            return cadastroRepository.save(clienteEntity);
        }
        else{
            return null;
        }

    }

    public TbCliente atualizar(ClienteDTO clienteDTO){
        TbCliente clienteEntity = cadastroRepository.getOne(clienteDTO.getIdCliente());

        if(clienteEntity!= null) {
            clienteEntity = cadastroBO.parseToEntity(clienteDTO,null);
            clienteDTO.setPwCliente(clienteEntity.getPwCliente());
            List<TbEndereco> enderecosEntitys = new ArrayList<>();
            for (EnderecoDTO enderecoDTO: clienteDTO.getEnderecos()){
                TbEndereco enderecoEntity = enderecoBO.parseToEntity(enderecoDTO,null);
                enderecosEntitys.add(enderecoEntity);
                enderecoRepository.save(enderecoEntity);
            }
            TbCliente clienteEntity1 = cadastroRepository.getOne(clienteDTO.getIdCliente());
            clienteEntity.setPwCliente(clienteEntity1.getPwCliente());

            clienteEntity.setEnderecos(enderecosEntitys);


        }
        return cadastroRepository.save(clienteEntity);
    }

    public ClienteDTO loginCadastro(Login login) throws Exception{


        if (login.getLogin().matches("[0-9]+")) {

            TbCliente clienteEntity = cadastroRepository.findByNrCpf(login.getLogin());
            if (clienteEntity != null) {
                ClienteDTO clienteDTO = cadastroBO.parseToDTO(clienteEntity);
                List<EnderecoDTO> enderecoDTOS = new ArrayList<>();
                for (TbEndereco enderecoEntity : clienteEntity.getEnderecos()) {
                    EnderecoDTO dto = enderecoBO.parseToDTO(enderecoEntity);
                    enderecoDTOS.add(dto);
                }
                clienteDTO.setEnderecos(enderecoDTOS);

                List<CartaoCreditoDTO> cartaoCreditoDTOS = new ArrayList<>();
                for (TbCartaoCredito cartaoCreditoEntity : clienteEntity.getCartoesCredito()) {
                    CartaoCreditoDTO dtoCard = cartaoCreditoBO.parseToDTO(cartaoCreditoEntity);
                    cartaoCreditoDTOS.add(dtoCard);
                }
                clienteDTO.setCartoesCreditoDTO(cartaoCreditoDTOS);

                return clienteDTO;
            }else {
                return null;
            }
        } else {
            TbCliente clienteEntity = cadastroRepository.findByDsEmail(login.getLogin()).get(0);
            if (clienteEntity != null) {
                ClienteDTO clienteDTO = cadastroBO.parseToDTO(clienteEntity);
                List<EnderecoDTO> enderecoDTOS = new ArrayList<>();
                for (TbEndereco enderecoEntity : clienteEntity.getEnderecos()) {
                    EnderecoDTO dto = enderecoBO.parseToDTO(enderecoEntity);
                    enderecoDTOS.add(dto);
                }
                clienteDTO.setEnderecos(enderecoDTOS);

                List<CartaoCreditoDTO> cartaoCreditoDTOS = new ArrayList<>();
                for (TbCartaoCredito cartaoCreditoEntity : clienteEntity.getCartoesCredito()) {
                    CartaoCreditoDTO dtoCard = cartaoCreditoBO.parseToDTO(cartaoCreditoEntity);
                    cartaoCreditoDTOS.add(dtoCard);
                }
                clienteDTO.setCartoesCreditoDTO(cartaoCreditoDTOS);

                return clienteDTO;
            }else {
                return null;
            }
        }
    }

    public TbCliente adicionaEndereco(EnderecoDTO enderecoDTO, BigInteger idCliente){
        TbCliente clienteEntity = cadastroRepository.getOne(idCliente);
        List <TbEnderecoCliente> idClienteEndereco = enderecoClienteRepository.findByIdCliente(idCliente);
        List<TbEndereco> enderecosBanco = new ArrayList<>();

        for(TbEnderecoCliente enderecos:idClienteEndereco){
            enderecosBanco.add(enderecoRepository.getOne(enderecos.getIdEndereco()));
        }

        TbEndereco enderecoNovo = enderecoBO.parseToEntity(enderecoDTO, null);
        enderecoRepository.save(enderecoNovo);
        enderecosBanco.add(enderecoNovo);
        clienteEntity.setEnderecos(enderecosBanco);
        return cadastroRepository.save(clienteEntity);
    }

    public TbEndereco atualizaEndereco(EnderecoDTO enderecoDTO, BigInteger idEndereco){
        enderecoDTO.setIdEndereco(idEndereco);
        TbEndereco enderecoNovo = enderecoBO.parseToEntity(enderecoDTO, null);
        return  enderecoRepository.save(enderecoNovo);
    }

    public boolean deletarEndereco(BigInteger idCliente, BigInteger idEndereco){

        TbEnderecoCliente enderecoClienteId = enderecoClienteRepository.findByIdClienteAndIdEndereco(idCliente,idEndereco);

        enderecoClienteRepository.delete(enderecoClienteId);
        List<TbEnderecoCliente> enderecoCliente2 = enderecoClienteRepository.findByIdEndereco(idEndereco);

        if(enderecoCliente2.size()<1){
            TbEndereco enderecoEntity = enderecoRepository.getOne(idEndereco);
            enderecoRepository.delete(enderecoEntity);
        }

        return true;
    }

    public TbCliente adicionaCartaoCredito(CartaoCreditoDTO cartaoCreditoDTO, BigInteger idCliente){
        TbCliente clienteEntity = cadastroRepository.getOne(idCliente);

        List<TbCartaoCredito> cartoesCreditoEntitys = new ArrayList<>();
        for (TbCartaoCredito cartaoCredito: clienteEntity.getCartoesCredito()){
            cartoesCreditoEntitys.add(cartaoCredito);
        }

        TbCartaoCredito cartaoCreditoEntity = cartaoCreditoBO.parseToEntity(cartaoCreditoDTO,null);
        cartaoCreditoEntity.setClienteCartao(clienteEntity);
        cartaoCreditoDTO.setIdCliente(cartaoCreditoEntity.getIdCartaoCredito());
        cartaoRepository.save(cartaoCreditoEntity);
        cartoesCreditoEntitys.add(cartaoCreditoEntity);
        clienteEntity.setCartoesCredito(cartoesCreditoEntitys);

        return cadastroRepository.save(clienteEntity);
    }

    public TbCartaoCredito atualizaCartaoCredito(CartaoCreditoDTO cartaoCreditoDTO,BigInteger idCliente){
        TbCartaoCredito cartaoCreditoEntity = cartaoCreditoBO.parseToEntity(cartaoCreditoDTO,null);
        TbCliente cliente = cadastroRepository.getOne(idCliente);
        cartaoCreditoEntity.setClienteCartao(cliente);
        return cartaoRepository.save(cartaoCreditoEntity);
    }

    public CartaoCreditoDTO deletarCartaoCredito(BigInteger idCartaoCredito){
        TbCartaoCredito cartaoCreditoEntity = cartaoRepository.getOne(idCartaoCredito);
        CartaoCreditoDTO cartaoCreditoDTO = cartaoCreditoBO.parseToDTO(cartaoCreditoEntity);
        cartaoRepository.delete(cartaoCreditoEntity);
        return cartaoCreditoDTO;
    }

    public TbCliente alterarSenha(AlterarSenha alterarSenha){
        TbCliente cliente = cadastroRepository.getOne(alterarSenha.getIdCliente());

        byte[] decodedBytes = Base64.getDecoder().decode(cliente.getPwCliente());
        String decodedString = new String(decodedBytes);

        if (alterarSenha.getNovaSenha().equals(alterarSenha.getConfirmarSenha()) && alterarSenha.getSenhaAtual().equals(decodedString)){
            alterarSenha.setNovaSenha(Base64.getEncoder().encodeToString(alterarSenha.getNovaSenha().getBytes()));
            cliente.setPwCliente(alterarSenha.getNovaSenha());


            return cadastroRepository.save(cliente);
        }
        return cliente;
    }


    public List<TbCliente> buscarPorNmEmail(String nmEmail) {
        return cadastroRepository.findByDsEmail(nmEmail);
    }


    public TbEndereco buscaEndereco(BigInteger idEndereco){

        TbEndereco enderecoDTO = new TbEndereco();
        Query query = manager.createNativeQuery("SELECT \n" +
                "te.ID_ENDERECO,\n" +
                "te.DS_ENDERECO,\n" +
                "te.NR_ENDERECO,\n" +
                "te.NR_CEP,\n" +
                "te.DS_BAIRRO,\n" +
                "te.DS_CIDADE,\n" +
                "te.SG_ESTADO,\n" +
                "te.NM_COMPLEMENTO\n" +
                "FROM TB_ENDERECO te\n" +
                "WHERE te.ID_ENDERECO ="+idEndereco+"\n" +
                "Order BY ID_ENDERECO;");
        List<Object[] > listEntity = query.getResultList();
        Object[] endereçoEntity= listEntity.get(0);

        enderecoDTO.setIdEndereco((BigInteger) endereçoEntity[0]);
        enderecoDTO.setDsEndereco((String) endereçoEntity[1]);
        enderecoDTO.setNrEndereco((String) endereçoEntity[2]);
        enderecoDTO.setNrCep((String) endereçoEntity[3]);
        enderecoDTO.setDsBairro((String) endereçoEntity[4]);
        enderecoDTO.setDsCidade((String) endereçoEntity[5]);
        enderecoDTO.setSgEstado((String) endereçoEntity[6]);
        enderecoDTO.setNmCompleto((String) endereçoEntity[7]);

        return enderecoDTO;
    }





}