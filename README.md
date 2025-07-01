# MultiCash Transfer Service

MultiCash is a Spring Boot application designed for secure and efficient money transfers. It provides a RESTful API for peer-to-peer transfers, as well as deposits and withdrawals to external bank accounts. The application is built with a focus on security, using JWT for authentication and using transactional operations for its features.

## Features

* **User Authentication**: Secure user registration and login using JWT (JSON Web Tokens).
* **Account Management**: Automatic creation of a "CHECKING" account for new users with a starting balance of zero.
* **Peer-to-Peer Transfers**: Transfer funds to other users on the platform.
* **Bank Integration**:
    * Withdraw funds from a MultiCash account to an external bank account.
    * Deposit funds from an external bank account into a MultiCash account.
* **External Service Communication**: Integrates with `Versebank` and `Multibank` external services for bank transactions using Feign clients.
* **Transaction History**: Retrieve a list of all transactions for the authenticated user.
* **Data Seeding**: Comes with pre-loaded mock data for demonstration and testing purposes.

## Technologies Used

* **Framework**: Spring Boot
* **Security**: Spring Security, JWT
* **Database**: Spring Data JDBC
* **API Client**: Spring Cloud OpenFeign
* **Build Tool**: Maven

## Getting Started

### Prerequisites

* Java Development Kit (JDK) 17 or later
* Maven
* An SQL database (like PostgreSQL or H2)
* Docker (for running dependent services)

### Installation & Setup

1.  **Clone the repository:**
    ```bash
    git clone https://github.com/EmmanuelMultiverse/cashtransferservice.git
    cd cashtransferservice
    ```

2.  **Configure the database:**
    Open the `src/main/resources/application.properties` file (you may need to create it) and add your database connection details.

    *Example for H2 (development):*
    ```properties
    spring.profiles.active=h2
    spring.datasource.url=jdbc:h2:mem:testdb
    spring.datasource.driverClassName=org.h2.Driver
    spring.datasource.username=sa
    spring.datasource.password=password
    ```
    *Example for PostgreSQL:*
    ```properties
    spring.profiles.active=postgres
    spring.datasource.url=jdbc:postgresql://localhost:5432/your_db
    spring.datasource.username=your_user
    spring.datasource.password=your_password
    ```

3.  **Run Dependent Services:**
    WILL ADD DOCKER COMPOSE FILE HERE 
### Running the Application

Once the configuration is complete, you can run the application using Maven:

```bash
mvn spring-boot:run
```