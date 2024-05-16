# Currency API

## Descrição

Este projeto é uma API de conversão de moedas desenvolvida em Java utilizando o framework Spring Boot. O principal foco deste projeto é a implementação de testes unitários e de integração para garantir uma cobertura completa do código, utilizando ferramentas como JUnit, Mockito, AssertJ e JaCoCo.

## Tecnologias Utilizadas

- Java 17
- Spring Boot
- Spring Data JPA
- Spring Web
- Spring Cloud OpenFeign
- H2 Database
- Lombok
- JUnit 5
- Mockito
- AssertJ
- JaCoCo

## Funcionalidades

- Criação, atualização, listagem e remoção de moedas.
- Conversão de valores entre diferentes moedas utilizando uma API externa (AwesomeAPI).
- Tratamento de exceções personalizado.

## Estrutura do Projeto

O projeto está estruturado em diferentes pacotes, cada um com uma responsabilidade específica:

- **controller**: Contém os controladores REST que expõem os endpoints da API.
- **domain**: Contém as classes de domínio (modelos) utilizadas na aplicação.
- **exception**: Contém as classes de exceção personalizadas e o manipulador de exceções globais.
- **repository**: Contém as interfaces de repositório para interação com o banco de dados.
- **repository.feign**: Contém a interface FeignClient para comunicação com a API externa.
- **service**: Contém as classes de serviço com a lógica de negócios.

## Endpoints

### Moedas

- `GET /currency`: Lista todas as moedas cadastradas.
- `POST /currency`: Cria uma nova moeda.
- `PUT /currency/{id}`: Atualiza uma moeda existente.
- `DELETE /currency/{id}`: Remove uma moeda.

### Conversão de Moedas

- `POST /currency/convert`: Converte um valor de uma moeda para outra.

## Exceções

As exceções são tratadas globalmente pela classe `RestExceptionHandler`, que mapeia diferentes tipos de exceções para respostas HTTP apropriadas.

## Testes

### Testes Unitários

Os testes unitários são focados nas classes de serviço e utilitários, utilizando Mockito para criar mocks dos componentes dependentes. Asserções são feitas utilizando AssertJ para uma sintaxe mais fluida e legível.

### Testes de Integração

Os testes de integração verificam a integração entre os componentes da aplicação e o comportamento das APIs REST. Utilizam MockMvc para simular requisições HTTP e verificar as respostas.

### Cobertura de Código

A ferramenta JaCoCo é utilizada para medir a cobertura de código. O objetivo é atingir 100% de cobertura, garantindo que todas as linhas e ramificações do código sejam testadas.

## Como Executar o Projeto

### Pré-requisitos

- Java 17
- Maven

### Passos para Execução

1. Clone o repositório:
   ```sh
   git clone https://github.com/yourusername/currency-api.git
   cd currency-api
   ```

2. Compile e execute os testes:
   ```sh
   mvn clean verify
   ```

3. Execute a aplicação:
   ```sh
   mvn spring-boot:run
   ```

### Acessando a API

Após iniciar a aplicação, a API estará disponível em `http://localhost:8080`.

### Autor

Nome: Yasmin Barcelos

---

# Currency API

## Description

This project is a currency conversion API developed in Java using the Spring Boot framework. The main focus of this project is the implementation of unit and integration tests to ensure complete code coverage, using tools like JUnit, Mockito, AssertJ, and JaCoCo.

## Technologies Used

- Java 17
- Spring Boot
- Spring Data JPA
- Spring Web
- Spring Cloud OpenFeign
- H2 Database
- Lombok
- JUnit 5
- Mockito
- AssertJ
- JaCoCo

## Features

- Creation, updating, listing, and deletion of currencies.
- Conversion of values between different currencies using an external API (AwesomeAPI).
- Custom exception handling.

## Project Structure

The project is structured into different packages, each with a specific responsibility:

- **controller**: Contains the REST controllers that expose the API endpoints.
- **domain**: Contains the domain classes (models) used in the application.
- **exception**: Contains custom exception classes and the global exception handler.
- **repository**: Contains repository interfaces for database interaction.
- **repository.feign**: Contains the FeignClient interface for communication with the external API.
- **service**: Contains the service classes with business logic.

## Endpoints

### Currencies

- `GET /currency`: Lists all registered currencies.
- `POST /currency`: Creates a new currency.
- `PUT /currency/{id}`: Updates an existing currency.
- `DELETE /currency/{id}`: Deletes a currency.

### Currency Conversion

- `POST /currency/convert`: Converts a value from one currency to another.

## Exceptions

Exceptions are globally handled by the `RestExceptionHandler` class, which maps different types of exceptions to appropriate HTTP responses.

## Tests

### Unit Tests

Unit tests focus on service classes and utilities, using Mockito to create mocks of dependent components. Assertions are made using AssertJ for a more fluent and readable syntax.

### Integration Tests

Integration tests verify the integration between application components and the behavior of REST APIs. They use MockMvc to simulate HTTP requests and verify responses.

### Code Coverage

The JaCoCo tool is used to measure code coverage. The goal is to achieve 100% coverage, ensuring that all lines and branches of the code are tested.

## How to Run the Project

### Prerequisites

- Java 17
- Maven

### Steps to Run

1. Clone the repository:
   ```sh
   git clone https://github.com/yourusername/currency-api.git
   cd currency-api
   ```

2. Compile and run the tests:
   ```sh
   mvn clean verify
   ```

3. Run the application:
   ```sh
   mvn spring-boot:run
   ```

### Accessing the API

After starting the application, the API will be available at `http://localhost:8080`.

### Author

- Name: Yasmin Barcelos