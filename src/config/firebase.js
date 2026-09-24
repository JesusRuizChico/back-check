const admin = require('firebase-admin');
const dotenv = require('dotenv');
dotenv.config();

// Inicializamos la app con applicationDefault()
// Nota: en producción necesitas setear la variable de entorno GOOGLE_APPLICATION_CREDENTIALS
admin.initializeApp({
  credential: admin.credential.applicationDefault()
});

const db = admin.firestore();
const auth = admin.auth();

module.exports = { admin, db, auth };
