# AuthResponse com artistId

## Contexto

O app mobile precisa saber qual `Artist` pertence ao usuario autenticado para criar mensagens, candidaturas e outros registros com autoria correta.

Antes da correcao, `/auth/register` e `/auth/login` retornavam `token`, `userId`, `name`, `email` e `role`, mas nao retornavam `artistId`. Quando o mobile nao tinha esse campo, algumas telas podiam cair em fallback para o primeiro artista carregado.

## Contrato atual

As respostas de autenticacao passam a incluir:

- `token`
- `userId`
- `artistId`
- `name`
- `email`
- `role`

O campo `password` nao deve aparecer na resposta.

## Comportamento

- Se o usuario autenticado possui `Artist` vinculado, `artistId` retorna o id desse artista.
- Se o usuario nao possui `Artist` vinculado, `artistId` retorna `null`.
- O cadastro usado pelo mobile envia `stageName` e `mainSpecialty`, entao normalmente cria usuario e artista juntos.

## Validacao

O teste de integracao `ApiIntegrationTest.shouldRegisterAndLogin` valida que:

- register retorna `artistId`;
- login retorna o mesmo `artistId`;
- `password` nao aparece nas respostas.

Tambem foi validado manualmente via HTTP com backend Docker que:

- `/auth/register` retornou `artistId`;
- `/auth/login` retornou o mesmo `artistId`;
- `/api/artists` continha artista com `id == artistId` e `userId == userId`;
- mensagem criada em `/api/projects/{projectId}/messages` retornou `senderArtistId` igual ao `artistId` autenticado.
