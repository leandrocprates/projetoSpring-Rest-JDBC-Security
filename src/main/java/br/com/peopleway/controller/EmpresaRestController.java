/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.com.peopleway.controller;


import java.util.List;

import javax.sql.DataSource;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import br.com.peopleway.dao.EmpresaDAO;
import br.com.peopleway.model.EmpresaBean;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.web.multipart.MultipartFile;

@RestController
public class EmpresaRestController {
    
    private static final Logger logger = LogManager.getLogger(EmpresaRestController.class);
    
    
    @Autowired
    private Environment env;
    
    @Autowired
    private DataSource dataSource;
    
    @Autowired
    private DataSourceTransactionManager transactionManager;
    
    
    @RequestMapping(value = "/empresa", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<List<EmpresaBean>> getAllEmpresas(@RequestParam(value="limitInicial", defaultValue="0") int limitInicial, 
                                                       @RequestParam(value="limitFinal", defaultValue="20")int limitFinal) {

        List<EmpresaBean> listaEmpresas = null ; 
        
        logger.debug("Valor Inicial: {} , Valor Final: {}" , limitInicial , limitFinal );
        
        EmpresaDAO empresaDao = new EmpresaDAO(dataSource);
        listaEmpresas = empresaDao.getEmpresa(limitInicial, limitFinal);
        
        if(listaEmpresas.isEmpty()){
            logger.error(" Nao foram encontradas Empresas cadastradas no banco de dados. " );
            return new ResponseEntity<List<EmpresaBean>>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<List<EmpresaBean>>(listaEmpresas, HttpStatus.OK);
        
    }
    
    
    /**
     * Funcao responsavel por receber o Upload do Logo da Empresa
     * @param file Stream do Arquivo Carregado
     * @param id Id da empresa 
     * @return 
     */
    @RequestMapping(value = "/empresa/{id}/upload", method = RequestMethod.POST)
    public ResponseEntity<EmpresaBean>  loadLogo(@RequestParam("file") MultipartFile file , @PathVariable("id") long id ) {
        
            logger.debug("Id da Empresa: {} , Nome Arquivo: {}" , id , file.getOriginalFilename() );
        
            String diretorio = env.getProperty("file.location") + file.getOriginalFilename() ; 
            
            if (!file.isEmpty()) {
                try{
                    byte[] bytes = file.getBytes();
                    BufferedOutputStream stream = new BufferedOutputStream(new FileOutputStream(new File(diretorio)));
                    stream.write(bytes);
                    stream.close();
                    logger.info("Upload de Imagem realizado no diretorio : {}",diretorio );
                    
                    EmpresaDAO empresaDao = new EmpresaDAO(dataSource);
                    EmpresaBean empresaBean = new EmpresaBean();
                    empresaBean.setId(id);
                    empresaBean.setImagem(diretorio);
                    empresaDao.updateLogoEmpresa(empresaBean);
                    
                    return new ResponseEntity<EmpresaBean>(HttpStatus.OK);
                }catch(Exception ex){
                    logger.error("Erro no upload de Imagem : {}" , ex);
                }
            }
            return new ResponseEntity<EmpresaBean>(HttpStatus.INTERNAL_SERVER_ERROR);
    }
    
    /**
     * Funcao responsavel pela criacao e update de uma nova Empresa
     * @param empresaBean
     * @param ucBuilder
     * @return 
     */
    @RequestMapping(value = "/empresa", method = RequestMethod.POST, produces = {MediaType.APPLICATION_JSON_VALUE} )
    public ResponseEntity<EmpresaBean> createEmpresa(@RequestBody EmpresaBean empresaBean, UriComponentsBuilder ucBuilder) {

        TransactionDefinition def = new DefaultTransactionDefinition();
        TransactionStatus status = transactionManager.getTransaction(def);
        HttpHeaders headers = new HttpHeaders();
        
        try{

            EmpresaDAO empresaDao = new EmpresaDAO(dataSource);
            Long newEmpresaId = empresaBean.getId(); 

            if ( empresaBean.getId() > 0 ){
                empresaDao.updateEmpresa(empresaBean);
            } else {
                newEmpresaId = empresaDao.insertEmpresa(empresaBean);
            }

            transactionManager.commit(status);
            
            empresaBean.setId(newEmpresaId);
            
            headers.setLocation(ucBuilder.path("/empresa/{id}").buildAndExpand(newEmpresaId).toUri());
            return new ResponseEntity<EmpresaBean>(empresaBean , headers, HttpStatus.CREATED);
            
        }catch(Exception ex){
            logger.error("Erro ao cadastrar Empresa no banco de dados. Error {} " , ex);
            transactionManager.rollback(status);
            return new ResponseEntity<EmpresaBean>(headers, HttpStatus.INTERNAL_SERVER_ERROR );
        }
        
    }
    
    

    /**
     * Funcao responsavel por deletar a Empresa pelo Id passado
     * @param id
     * @return 
     */
    
    @RequestMapping(value = "/empresa/{id}", method = RequestMethod.DELETE)
    public ResponseEntity<EmpresaBean> deleteEmpresa(@PathVariable("id") long id) {
        
        logger.debug("Deletando Empresa Id : {} " , id  );
        
        EmpresaDAO empresaDao = new EmpresaDAO(dataSource);
        EmpresaBean empresaBean =  empresaDao.getEmpresaById(id);

        if (empresaBean == null) {
            logger.error(" Empresa  Id : {} nao encontrada no banco de dados. " , id );
            return new ResponseEntity<EmpresaBean>(HttpStatus.NOT_FOUND);
        }
 
        empresaDao.deleteEmpresaById(id);
        return new ResponseEntity<EmpresaBean>(HttpStatus.OK );
    }

    
    /**
     * Funcao responsavel por desabilitar a empresa pelo Id passado
     * @param id
     * @return 
     */
    @RequestMapping(value = "/empresa/disable/{id}", method = RequestMethod.DELETE)
    public ResponseEntity<EmpresaBean> desabilitarEmpresa(@PathVariable("id") long id) {
        
        logger.debug("Desabilitar Empresa Id : {} " , id  );
        
        EmpresaDAO empresaDao = new EmpresaDAO(dataSource);
        EmpresaBean empresaBean =  empresaDao.getEmpresaById(id);

        if (empresaBean == null) {
            logger.error(" Empresa  Id : {} nao encontrada no banco de dados. " , id );
            return new ResponseEntity<EmpresaBean>(HttpStatus.NOT_FOUND);
        }
 
        empresaDao.disableEmpresaById(id);
        return new ResponseEntity<EmpresaBean>(HttpStatus.OK );
    }
    
    
    /**
     * Funcao responsavel por habilitar a empresa pelo Id passado
     * @param id
     * @return 
     */
    @RequestMapping(value = "/empresa/enable/{id}", method = RequestMethod.GET)
    public ResponseEntity<EmpresaBean> habilitarEmpresa(@PathVariable("id") long id) {
        
        logger.debug("Habilitar Empresa Id : {} " , id  );
        
        EmpresaDAO empresaDao = new EmpresaDAO(dataSource);
        EmpresaBean empresaBean =  empresaDao.getEmpresaById(id);

        if (empresaBean == null) {
            logger.error(" Empresa  Id : {} nao encontrada no banco de dados. " , id );
            return new ResponseEntity<EmpresaBean>(HttpStatus.NOT_FOUND);
        }
 
        empresaDao.enableEmpresaById(id);
        return new ResponseEntity<EmpresaBean>(HttpStatus.OK );
    }
    
}