package br.com.rdevs.ecommerce.documentoFiscal.model.CBD;


import br.com.rdevs.ecommerce.documentoFiscal.model.dto.DocumentoFiscalDTO;
import br.com.rdevs.ecommerce.documentoFiscal.model.entity.TbDocumentoItem;
import br.com.rdevs.ecommerce.pagamentopedido.model.entity.TbTipoPagamento;
import br.com.rdevs.ecommerce.produto.repository.ProdutoRepository;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@Component
public class ConsultaBanco {

    @PersistenceContext
    private EntityManager manager;

    @Autowired
    private ProdutoRepository produtoRepository;


    public TbTipoPagamento tipoPagamento(BigInteger idTipoPagamento){
        TbTipoPagamento tbTipoPagamento = new TbTipoPagamento();
        Query query = manager.createNativeQuery("SELECT \n" +
                "ttp.ID_TIPO_PAGAMENTO,\n" +
                "ttp.DS_TIPO_PAGAMENTO_ECOM\n" +
                "FROM TB_TIPO_PAGAMENTO ttp\n" +
                "WHERE ID_TIPO_PAGAMENTO = "+idTipoPagamento+" ");
        List<Object[]> ListEntity = query.getResultList();
        Object[] entity = ListEntity.get(0);

        return tbTipoPagamento;
    }

    public List<TbDocumentoItem> itensNF(BigInteger idDocumentoFiscal){
        Query query = manager.createNativeQuery("SELECT \n" +
                "tdi.ID_DOCUMENTO_FISCAL,\n" +//0
                "tdi.NR_ITEM_DOCUMENTO,\n" +//1
                "tdi.CD_PRODUTO,\n" +//2
                "tdi.QT_ITEM,\n" +//3
                "tdi.VL_ITEM,\n" +//4
                "tdi.PC_ICMS,\n" +//5
                "tdi.VL_ICMS\n" +//6
                "FROM TB_DOCUMENTO_ITEM tdi \n" +
                "WHERE tdi.ID_DOCUMENTO_FISCAL = "+idDocumentoFiscal+" ");

        List<Object[]> listaItens = query.getResultList();
        List<TbDocumentoItem> itensDocumento = new ArrayList<>();

        for (Object[] item: listaItens){
            TbDocumentoItem itemEntity = new TbDocumentoItem();
            itemEntity.setNrItemDocumento((BigInteger) item[1]);
            itemEntity.setProduto(produtoRepository.getOne((BigInteger) item[2]));
            itemEntity.setQtItem((Integer) item[3]);
            itemEntity.setVlItem((BigDecimal) item[4]);
            itemEntity.setPcIcms((BigDecimal) item[5]);
            itemEntity.setVlIcms((BigDecimal) item[6]);
            itensDocumento.add(itemEntity);
        }

        return itensDocumento;
    }





}
