// Healthcheck для product-mongo: инициализирует replica set (если ещё не создан)
// и проверяет, что нода стала PRIMARY. Запускается через mongosh --file.
try {
  rs.status();
} catch (e) {
  rs.initiate({ _id: 'rs0', members: [{ _id: 0, host: 'product-mongo:27017' }] });
}
if (!db.hello().isWritablePrimary) {
  quit(1);
}
