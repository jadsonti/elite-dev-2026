# Elite Eventos

Plataforma full stack de eventos e ingressos desenvolvida para o Desafio Elite Dev 2026. Organizadores criam eventos a partir do catálogo TMDb, clientes reservam e simulam o pagamento, e a portaria valida ingressos assinados por QR Code.

## Tecnologias

- Frontend: React 19, Vite 8 e CSS responsivo.
- Backend: Java 21, Spring Boot 4, Spring Security, JWT, JPA e Flyway.
- Banco de dados: PostgreSQL 16 pelo Docker Compose.
- Integrações: TMDb e ZXing 3.5.4.

## Funcionalidades

- Autenticação e autorização para `ADMIN`, `CUSTOMER` e `GATE`.
- Busca pública de eventos e catálogo externo TMDb.
- Criação, publicação, atualização e cancelamento de eventos.
- Reservas transacionais com proteção contra venda acima da capacidade.
- Pagamento simulado com aprovação e recusa.
- Emissão automática de um ingresso por unidade comprada.
- Token de ingresso assinado com HMAC-SHA256 e QR Code PNG.
- Link público para compartilhamento sem dados pessoais do cliente.
- Portaria com leitura pela câmera ou código manual.
- Resultados de validação: válido, inválido, já utilizado e evento incorreto.

## Requisitos

- Java 21.
- Docker Desktop com Docker Compose.
- Node.js 20 ou superior.

## Configuração

Copie `.env.example` para `.env` e informe ao menos o token de leitura do TMDb:

```env
JWT_SECRET=uma-chave-segura-com-32-ou-mais-caracteres
TMDB_READ_TOKEN=seu-token-de-leitura-tmdb
TICKET_SIGNING_SECRET=outra-chave-segura-com-32-ou-mais-caracteres
TICKET_SHARE_BASE_URL=http://localhost:8080/api/tickets/shared
CORS_ALLOWED_ORIGINS=http://localhost:5173,http://localhost:4173
```

As variáveis podem ser exportadas no terminal ou configuradas na IDE. Há valores locais padrão, exceto para a integração TMDb.

## Execução

Inicie o PostgreSQL:

```bash
docker compose up -d
```

Inicie o backend:

```bash
cd backend
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

No Windows:

```powershell
cd backend
.\mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=dev
```

Inicie o frontend em outro terminal:

```bash
cd frontend
npm ci
npm run dev
```

Acesse `http://localhost:5173`. A API utiliza `http://localhost:8080` e o PostgreSQL utiliza a porta `5432`.

## Usuários de demonstração

Todos usam a senha `123456` quando o backend é iniciado com o perfil `dev`.

- Organizador: `organizador@demo.com`
- Cliente 1: `cliente1@demo.com`
- Cliente 2: `cliente2@demo.com`
- Portaria: `portaria@demo.com`

O perfil também cria um evento publicado com ingressos disponíveis.

## Testes e build

```bash
cd backend
./mvnw test
```

```bash
cd frontend
npm run lint
npm run build
```

A suíte atual possui 14 testes de backend, incluindo concorrência de reservas e pagamentos, assinatura e decodificação do QR e reutilização de ingresso. O frontend possui lint e build de produção validados.

## Fluxo de avaliação

1. Entre como organizador e consulte o TMDb para criar e publicar um evento.
2. Entre como cliente, reserve ingressos e aprove ou recuse o pagamento simulado.
3. Abra “Meus ingressos”, visualize o QR e copie o link compartilhável.
4. Entre como portaria, selecione o evento e leia o QR pela câmera ou cole o token.
5. Valide novamente o mesmo ingresso para conferir o bloqueio de reutilização.

## Decisões técnicas

- Bloqueios pessimistas no PostgreSQL protegem estoque, pagamento e validação concorrentes.
- A assinatura HMAC impede a fabricação de códigos de ingresso válidos.
- O link público apresenta apenas informações necessárias do evento.
- A cobrança é síncrona e simulada para demonstrar aprovação, recusa e nova tentativa sem serviço financeiro real.
- O frontend usa a API `BarcodeDetector` do navegador e mantém a digitação manual como alternativa.

## Uso de IA

O Codex foi usado como agente de implementação, revisão e testes. As decisões de escopo, fluxo por features, stack Java 21/PostgreSQL, identidade visual e critérios de entrega foram conduzidas e aprovadas pelo autor do projeto.

## Observações

- A câmera exige navegador compatível e contexto seguro; em caso de indisponibilidade, use o código manual.
- O deploy não está incluído nesta entrega.
- Nunca publique os segredos reais do JWT, da assinatura dos ingressos ou do TMDb.
