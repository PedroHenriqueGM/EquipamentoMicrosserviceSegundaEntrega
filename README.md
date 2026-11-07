# Microsserviço de Gerenciamento de Equipamentos

Microsserviço desenvolvido em **Java e Spring Boot** para um sistema de gerenciamento de bicicletários. O serviço é responsável pelo gerenciamento de bicicletas, trancas e totens, incluindo o controle de seus estados, associação entre equipamentos e comunicação com outros microsserviços.

## Tecnologias utilizadas

- **Java 17**
- **Spring Boot 3.5.7**
- **Spring Data JPA**
- **Spring Web**
- **Spring WebFlux**
- **H2 Database**
- **Maven**
- **Docker**
- **JUnit e Mockito**
- **JaCoCo**

## Funcionalidades

### Bicicletas
- Cadastro, consulta, listagem e atualização de bicicletas.
- Controle de estados operacionais, como `NOVA`, `DISPONIVEL`, `EM_USO` e `EM_REPARO`.
- Integração e retirada de bicicletas da rede de bicicletários.
- Associação e desassociação de bicicletas às trancas.
- Validação das condições para manutenção, reparo e aposentadoria.

### Trancas
- Cadastro, consulta, listagem e atualização de trancas.
- Controle de estados operacionais, como `NOVA`, `DISPONIVEL`, `OCUPADA` e `EM_REPARO`.
- Operações de trancar e destrancar bicicletas.
- Integração e retirada de trancas da rede de totens.
- Associação de trancas aos totens.
- Validação das condições para manutenção e aposentadoria.

### Totens
- Cadastro, consulta, listagem, atualização e exclusão de totens.
- Associação de trancas aos totens.
- Consulta das trancas e bicicletas vinculadas a cada totem.

### Integrações entre serviços
- Consulta de dados de funcionários em outro microsserviço.
- Envio de notificações por e-mail em operações de inclusão e retirada de equipamentos da rede.
- Comunicação HTTP utilizando Spring WebClient.

## Endpoints principais

Os endpoints abaixo representam as rotas declaradas nos controllers da aplicação.

### Bicicletas — `/bicicleta`

| Método | Endpoint | Descrição |
|---|---|---|
| POST | `/bicicleta` | Cadastrar bicicleta |
| GET | `/bicicleta` | Listar bicicletas |
| GET | `/bicicleta/{idBicicleta}` | Consultar bicicleta |
| PUT | `/bicicleta/{idBicicleta}` | Atualizar bicicleta |
| DELETE | `/bicicleta/{idBicicleta}` | Excluir bicicleta |
| POST | `/bicicleta/integrarNaRede` | Integrar bicicleta à rede |
| POST | `/bicicleta/retirarDaRede` | Retirar bicicleta da rede |
| POST | `/bicicleta/{idBicicleta}/status/{acao}` | Alterar status |

### Trancas — `/tranca`

| Método | Endpoint | Descrição |
|---|---|---|
| POST | `/tranca` | Cadastrar tranca |
| GET | `/tranca` | Listar trancas |
| GET | `/tranca/{idTranca}` | Consultar tranca |
| PUT | `/tranca/{idTranca}` | Atualizar tranca |
| DELETE | `/tranca/{idTranca}` | Excluir tranca |
| GET | `/tranca/{idTranca}/bicicleta` | Consultar bicicleta associada |
| POST | `/tranca/{idTranca}/trancar` | Trancar bicicleta |
| POST | `/tranca/{idTranca}/destrancar` | Destrancar bicicleta |
| POST | `/tranca/integrarNaRede` | Integrar tranca à rede |
| POST | `/tranca/retirarDaRede` | Retirar tranca da rede |
| POST | `/tranca/{idTranca}/status/{acao}` | Alterar status |

### Totens — `/totem`

| Método | Endpoint | Descrição |
|---|---|---|
| POST | `/totem` | Cadastrar totem |
| GET | `/totem` | Listar totens |
| PUT | `/totem/{idTotem}` | Atualizar totem |
| DELETE | `/totem/{idTotem}` | Excluir totem |
| GET | `/totem/{idTotem}/trancas` | Listar trancas do totem |
| GET | `/totem/{idTotem}/bicicletas` | Listar bicicletas do totem |

### Banco de dados

| Método | Endpoint | Descrição |
|---|---|---|
| GET | `/restaurarBanco` | Restaurar dados de demonstração |

**Atenção:** o endpoint `/restaurarBanco` apaga os registros existentes e recria dados de demonstração. Não deve ser utilizado em um ambiente com dados que precisam ser preservados.

## Arquitetura e integrações

O projeto utiliza uma arquitetura organizada em camadas:

- **Controller:** recebe as requisições HTTP e disponibiliza os endpoints REST.
- **Service:** concentra as regras de negócio e operações dos equipamentos.
- **Repository:** realiza as operações de persistência utilizando Spring Data JPA.
- **Model:** representa as entidades e seus relacionamentos.
- **DTO:** organiza os dados utilizados na comunicação entre operações e serviços.

O microsserviço realiza integrações externas por meio do `WebClient`:

| Serviço | Responsabilidade |
|---|---|
| Service B | Consulta de funcionários e seus dados cadastrais |
| Service C | Envio de notificações por e-mail |

URLs configuradas:

- Service B: `https://sistemadecontroledebicicletario-scb.onrender.com`
- Service C: `https://scb-msexterno.onrender.com`

## Banco de dados

A aplicação utiliza o **H2 Database** em memória, com persistência gerenciada pelo Spring Data JPA.

O console H2 está disponível em:

`http://localhost:8081/h2-console`

Configurações padrão:

| Propriedade | Valor |
|---|---|
| JDBC URL | `jdbc:h2:mem:bicicletas` |
| User | `sa` |
| Password | *(vazio)* |
| Driver | `org.h2.Driver` |

Por utilizar um banco em memória, os dados são reinicializados quando a aplicação é encerrada.

## Como executar o projeto

### Pré-requisitos

- JDK 17 ou superior.
- Git.

### 1. Clonar o repositório

```bash
git clone https://github.com/PedroHenriqueGM/EquipamentoMicrosserviceSegundaEntrega.git
```

### 2. Acessar a pasta

```bash
cd EquipamentoMicrosserviceSegundaEntrega
```

### 3. Executar a aplicação

No Windows:

```powershell
.\mvnw.cmd spring-boot:run
```

No Linux ou macOS:

```bash
./mvnw spring-boot:run
```

A aplicação ficará disponível em:

`http://localhost:8081`

## Testes

O projeto possui testes automatizados utilizando JUnit e Mockito, abrangendo controllers e serviços.

Para executar os testes:

No Windows:

```powershell
.\mvnw.cmd test
```

No Linux ou macOS:

```bash
./mvnw test
```

O projeto também possui configuração do JaCoCo para geração de relatórios de cobertura de testes.

## Docker

O repositório possui um `Dockerfile` para construção e execução da aplicação em container.

Para construir a imagem:

```bash
docker build -t equipamento-microsservice .
```

Para executar:

```bash
docker run -p 8081:8081 equipamento-microsservice
```

A aplicação ficará disponível em `http://localhost:8081`.

## Autor

Desenvolvido por [Pedro Henrique Gonçalves Magalhães](https://github.com/PedroHenriqueGM).
