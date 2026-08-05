# StreamLab API

Backend REST per **StreamLab**, un'app streaming personale sviluppata in Angular. Gestisce autenticazione, abbonamenti e titoli preferiti — il catalogo dei contenuti (titoli, poster, trame) resta interamente delegato alle API pubbliche
della piattaforma TMDB, consultato direttamente dal frontend.

Primo progetto Spring Boot sviluppato da zero, un pezzo alla volta, con particolare attenzione a autenticazione sicura, gestione errori centralizzata e architettura a layer (controller → service → repository).

## Stack tecnico

- **Java 17**, **Spring Boot** (Web, Data JPA, Validation)
- **MySQL** come database
- **jjwt** per la generazione/validazione dei JWT
- **springdoc-openapi** per la documentazione API (Swagger UI)
- Autenticazione **scritta a mano**, senza Spring Security

## Architettura dell'autenticazione

- **JWT in cookie httpOnly**, mai esposti a JavaScript
- **Access token** (breve durata, stateless, verificato solo tramite firma) + **refresh token** (lunga durata, verificato anche a database per essere revocabile)
- **Filtro custom** (`OncePerRequestFilter`) per la validazione dell'access token su ogni richiesta protetta
- **CORS** con origine esplicita
- **SameSite=Strict** sui cookie, come protezione aggiuntiva da CSRF

## Struttura del progetto

src/main/java/it/labforweb/streamlabapi/
├── controllers/ # endpoint REST
├── services/ # logica di business
├── repositories/ # accesso ai dati (Spring Data JPA)
├── models/ # entity JPA
├── dtos/ # oggetti di richiesta/risposta
├── exceptions/ # eccezioni custom + gestore globale
├── security/ # JWT, filtro di autenticazione
├── utils/ # classi di supporto interne
└── config/ # bean di configurazione (CORS, PasswordEncoder, OpenAPI)


## Funzionalità principali

- **Autenticazione**: registrazione (con creazione contestuale di un abbonamento), login, refresh, logout
- **Riattivazione account**: se un utente si registra con un'email già esistente ma disattivata, l'account viene riattivato invece di generarne uno nuovo
- **Gestione profilo**: lettura, cambio password, cambio piano abbonamento, disattivazione account
- **Catalogo abbonamenti**: endpoint pubblico con i piani disponibili
- **Preferiti**: aggiunta, rimozione e lettura (filtrata per film/serie) dei titoli salvati da ciascun utente

## Setup

### Prerequisiti

- Java 17+
- MySQL 8+ (o Docker, vedi sotto)

### Variabili d'ambiente richieste

| Nome | Descrizione |
|---|---|
| `DB_PASSWORD` | Password dell'utente MySQL |
| `JWT_SECRET` | Chiave segreta per la firma dei JWT (generabile con `openssl rand -base64 32`) |


### Avvio

```bash
./mvnw spring-boot:run
```

L'app parte su `http://localhost:8080`.

### Documentazione API

Con l'app avviata, la documentazione interattiva è disponibile su:

http://localhost:8080/swagger-ui.html


## Frontend

Il frontend Angular corrispondente gira di default su `http://localhost:4200` — la configurazione CORS del backend è impostata per accettare richieste solo da quell'origine.
