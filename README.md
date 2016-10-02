# Projeto utilizando Spring criando serviços Rest , JDBC NamedParameter e  Security Security com Basic Authentication.


Para criacao deste projeto foi criada uma classe **AbstractDAO<T>** no pacote **br.com.springTeste.abstractdao** que é uma classe 
Generica que deve ser estendida para cada DAO criado para acesso ao Banco de Dados.

No pacote **br.com.springTeste.dao** estão as classes que fazem acesso ao Banco de Dados como por exemplo a classe **EmpresaDAO**,
que estende **AbstractDAO** implementando as funcoes de acesso ao Banco de Dados para a **Tabela Empresa**. 

```javascript
public class EmpresaDAO extends AbstractDAO<EmpresaBean>
```

A classe **EmpresaDAO** passa no construtor o tipo da Classe Bean , o Mapper **EmpresaMapper** receber o recordset que ira preencher e retornar o Bean **EmpresaBean.java** e o **DataSource** que é a conexao com o Banco de Dados.   

```javascript
	public EmpresaDAO(DataSource dataSource) {
		super(EmpresaBean.class, new EmpresaMapper(), dataSource);
	}
```

No pacote **br.com.springTeste.mapper** serão encontrados os Mappers que receber o recordset da Query Select executada e 
preenche o Bean. 

```javascript
public class EmpresaMapper implements RowMapper<EmpresaBean> {
    public EmpresaBean mapRow(ResultSet rs, int rowNum) throws SQLException {
        EmpresaBean empresaBean = new EmpresaBean();
        empresaBean.setId(rs.getLong("id"));
        empresaBean.setNomeApp(rs.getString("nomeApp"));
        empresaBean.setNome(rs.getString("nome"));
        empresaBean.setCnpj(rs.getString("cnpj"));
        empresaBean.setIdioma(rs.getString("idioma"));
        empresaBean.setTimezone(rs.getString("timezone"));
        empresaBean.setClassificacao(rs.getString("classificacao"));
        empresaBean.setDeficiente(rs.getBoolean("isDeficiente"));
        empresaBean.setNumeroLicensas(rs.getInt("numeroLicensas"));
        empresaBean.setImagem(rs.getString("imagem"));
        empresaBean.setNomeAdministrador(rs.getString("nomeAdministrador"));
        empresaBean.setEmailAdministrador(rs.getString("emailAdministrador"));
        empresaBean.setPasswordAdministrador(rs.getString("passwordAdministrador"));
        empresaBean.setAtivo(rs.getBoolean("isAtivo"));
        empresaBean.setPlanoId(rs.getInt("planoId"));
        return empresaBean;
    }

}

```












