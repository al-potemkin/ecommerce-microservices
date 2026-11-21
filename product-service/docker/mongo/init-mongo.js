const dbName = process.env.PRODUCT_DB_NAME;

db = db.getSiblingDB(dbName);
db.createUser({
  user: process.env.PRODUCT_DB_USER,
  pwd: process.env.PRODUCT_DB_PASSWORD,
  roles: [{ role: "readWrite", db: dbName }]
});