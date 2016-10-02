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









