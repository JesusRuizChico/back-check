const { db } = require('../config/firebase');

exports.registerUser = async (req, res) => {
    try {
        const { uid, email, nombre, telefono, rol } = req.body;
        if (!uid || !email || !rol) {
            return res.status(400).json({ error: 'Faltan datos obligatorios' });
        }

        const userRef = db.collection('usuarios').doc(uid);
        await userRef.set({
            nombre: nombre || '',
            correo: email,
            telefono: telefono || '',
            rol: rol, // 'arrendador' o 'arrendatario'
            foto_perfil: '',
            createdAt: new Date()
        });

        res.status(201).json({ message: 'Usuario registrado exitosamente en Firestore' });
    } catch (error) {
        console.error(error);
        res.status(500).json({ error: 'Error interno del servidor' });
    }
};
