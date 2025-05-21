
<p align="center">
  <img src="src/main/resources/static/payflow.png" alt="PayFlow Logo" width="200"/>
</p>

# Payflow - Digital Wallet Application

[![GitHub stars](https://img.shields.io/github/stars/NiharikaPremkumar/Payflow--Digital-Wallet-Application?style=social)](https://github.com/NiharikaPremkumar/Payflow--Digital-Wallet-Application/stargazers)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Java](https://img.shields.io/badge/Java-17-blue?logo=java&logoColor=white)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring_Boot-2.7-green?logo=springboot)](https://spring.io/projects/spring-boot)

## Overview

Payflow is a secure and user-friendly digital wallet application designed to simplify your financial transactions. With Payflow, users can register, add funds, send money, and track their transaction history seamlessly. The application also supports account management features such as account verification and freezing/unfreezing for enhanced security.

## Features

- **User Module**
  - User Registration and Login (JWT authentication)
  - Account verification and deactivation
  - Password encryption for secure authentication

- **Wallet Module**
  - Add funds to wallet
  - View current wallet balance

- **Transaction Module**
  - Send money securely between users
  - View detailed transaction history
  - Validation of funds and transaction limits

- **Admin Module (Optional)**
  - Freeze/unfreeze user accounts
  - Audit user transactions for compliance

## Technologies Used

- **Backend:** Java 17 with Spring Boot 2.7
- **Version Control:** Git and GitHub
- **Authentication:** JWT (JSON Web Tokens)
- **Database:** *(Specify your database, e.g., MySQL, PostgreSQL)*
- **Build Tool:** Maven
- **IDE:** IntelliJ IDEA

## Installation & Setup

1. Clone the repository:

   ```bash
   git clone https://github.com/NiharikaPremkumar/Payflow--Digital-Wallet-Application.git
   ```

2. Navigate to the project directory:

   ```bash
   cd Payflow--Digital-Wallet-Application
   ```

3. Configure your database settings in `src/main/resources/application.properties`.

4. Build and run the application:

   ```bash
   mvn clean install
   mvn spring-boot:run
   ```
## Usage
-  Register a new user account.
-  Add funds to your wallet.
-  Send money to other registered users.
-  Track your transaction history.
-  Admins can manage user accounts and review transactions.
## Contributing
Contributions are welcome! Feel free to submit issues or pull requests.
## License
This project is licensed under the MIT License. See the [MIT License](LICENSE) file for details.

