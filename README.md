# Payment System <img align="center" src="https://raw.githubusercontent.com/devicons/devicon/6910f0503efdd315c8f9b858234310c06e04d9c0/icons/linux/linux-original.svg" alt="Linux" width="40" height="40">

![Spring](https://img.shields.io/badge/Spring-6DB33F?style=plastic&logo=spring&logoColor=white)
![Java](https://img.shields.io/badge/Java-ED8B00?style=plastic&logo=openjdk&logoColor=white)
![Docker](https://img.shields.io/badge/Docker-2496ED?style=plastic&logo=docker&logoColor=white)
![Linux](https://img.shields.io/badge/Linux-FCC624?style=plastic&logo=linux&logoColor=black)
![Swagger](https://img.shields.io/badge/Swagger-85EA2D?style=plastic&logo=swagger&logoColor=black)


Estratégias de Teste em Microsserviços com Spring Boot

---

## Documentação:

<img align="center" src="https://raw.githubusercontent.com/devicons/devicon/6910f0503efdd315c8f9b858234310c06e04d9c0/icons/swagger/swagger-original.svg" alt="Swagger" width="40" height="30"> Swagger

- [Swagger - local](http://localhost:8080/swagger-ui/index.html)

---

## <img align="center" alt="Diego-Docker" height="30" width="40" src="https://raw.githubusercontent.com/devicons/devicon/master/icons/docker/docker-plain.svg"> Docker Compose (`compose.yaml`)

O arquivo `compose.yaml` é utilizado para definir e gerenciar os serviços Docker necessários para a aplicação. Ele permite organizar e configurar múltiplos contêineres de forma simples e eficiente.

No caso deste projeto, o `compose.yaml` configura dois serviços principais:

1. **database**: Um contêiner executando o MySQL.
2. **exactpro-app**: O contêiner da aplicação.

Trecho do `compose.yaml`:

```yaml
services:
  database:
    image: "mysql:8.2.0-oraclelinux8"
    container_name: payment_mysql_db
    environment:
      - MYSQL_DATABASE=${DB_DATABASE}
      - MYSQL_ROOT_PASSWORD=${DB_PASSWORD}
    ports:
      - "3307:3306"
    volumes:
      - db-mysql-payment:/var/lib/mysql

volumes:
  db-mysql-payment:
```

#### Arquivo `.env`

O arquivo `.env` é utilizado para armazenar variáveis de ambiente de forma segura e prática. Ele permite separar configurações sensíveis e específicas do ambiente de execução do código da aplicação.

Exemplo de um arquivo `.env`:

```dotenv
PORT=8080
API_VERSION=develop
BASE_URL=http://localhost:8080
DB_USERNAME=root
DB_PASSWORD=local_password
DB_DATABASE=payment_system_db
DB_PORT=3307
DB_HOST=localhost
```
### Benefícios de Usar `compose.yaml` e `.env`

- **Facilidade de Configuração**: Permite definir configurações e relações entre múltiplos serviços em um único arquivo.
- **Separação de Responsabilidades**: Mantém variáveis sensíveis fora do código-fonte, melhorando a segurança e facilitando a manutenção.
- **Flexibilidade**: Facilita a troca entre diferentes ambientes (desenvolvimento, teste, produção) alterando apenas o arquivo `.env`.

### Executando os Serviços

Para iniciar os serviços definidos no `compose.yaml`, execute:
```shell
docker-compose up -d
```

Certifique-se de que o arquivo `.env` esteja no mesmo diretório do `compose.yaml` para que o Docker Compose carregue as variáveis adequadamente.

### Resumo

- **Arquivo `compose.yaml`**: Gerencia e configura os serviços Docker.
- **Arquivo `.env`**: Armazena variáveis de ambiente de forma segura.

---