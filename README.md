# StreamLab API

Backend REST per StreamLab, un'app streaming personale sviluppata in Angular. Gestisce autenticazione, abbonamenti e titoli preferiti. Il catalogo dei contenuti (titoli, poster, trame) proviene dalle API di [TMDB](https://www.themoviedb.org/documentation/api), consultate dal frontend tramite un endpoint proxy esposto da questo backend, così che la chiave API TMDB non sia mai esposta lato client.

Primo progetto Spring Boot sviluppato da zero, un pezzo alla volta, con particolare attenzione a autenticazione sicura, gestione errori centralizzata e architettura a layer (controller → service → repository).

## Stack tecnico

- Java 17, Spring Boot (Web, Data JPA, Validation)
- MySQL come database
- `jjwt` per la generazione/validazione dei JWT
- `springdoc-openapi` per la documentazione API (Swagger UI)
- Autenticazione scritta a mano, senza Spring Security

## Architettura dell'autenticazione

- JWT in cookie `httpOnly`, mai esposti a JavaScript
- Access token (breve durata, stateless, verificato solo tramite firma) + refresh token (lunga durata, verificato anche a database per essere revocabile)
- Filtro custom (`OncePerRequestFilter`) per la validazione dell'access token su ogni richiesta protetta
- CORS con origini esplicite (configurabili)
- `SameSite=Strict` sui cookie, come protezione aggiuntiva da CSRF

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
- **Proxy TMDB**: inoltra le richieste del frontend verso le API di TMDB (`/api/tmdb/**`), aggiungendo la chiave API lato server — mai esposta al client

## Setup

### Prerequisiti

- Java 17+
- MySQL 8+ (o Docker, vedi sotto)

### Variabili d'ambiente richieste

| Nome           | Descrizione                                                                                                              |
|----------------|--------------------------------------------------------------------------------------------------------------------------|
| `DB_PASSWORD`  | Password dell'utente MySQL                                                                                               |
| `JWT_SECRET`   | Chiave segreta per la firma dei JWT (generabile con `openssl rand -base64 32`)                                           |
| `TMDB_API_KEY` | Chiave API di TMDB, usata dal proxy lato server ([ottienila gratuitamente qui](https://www.themoviedb.org/settings/api)) |

Vanno impostate come variabili d'ambiente prima dell'avvio (via terminale, IDE, o il meccanismo che preferisci) — nessun valore va scritto direttamente nei file `.properties`.

### Avvio

```bash
./mvnw spring-boot:run
```

L'app parte su `http://localhost:8080`.

## Documentazione API

Con l'app avviata, la documentazione interattiva è disponibile su:

http://localhost:8080/swagger-ui.html


## Frontend

Il frontend Angular corrispondente gira di default su `http://localhost:4200`. La configurazione CORS del backend accetta richieste dalle origini configurate (produzione e/o sviluppo locale, a seconda dell'ambiente).