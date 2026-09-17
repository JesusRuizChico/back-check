const express = require('express');
const cors = require('cors');
const dotenv = require('dotenv');
const authRoutes = require('./routes/authRoutes');

dotenv.config();

const app = express();
app.use(cors());
app.use(express.json());

app.use('/api/auth', authRoutes);

app.get('/', (req, res) => {
    res.send('API Backend Arrendamiento Seguro funcionando!');
});

const PORT = process.env.PORT || 5000;
app.listen(PORT, () => {
    console.log('?? Servidor backend corriendo en puerto ' + PORT);
});
