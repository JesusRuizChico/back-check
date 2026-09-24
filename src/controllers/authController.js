const { db } = require('../config/firebase');
const { validarEspecialidades } = require('../validators/especialidadValidator');

exports.registerUser = async (req, res) => {
    try {
        const { uid, email, nombre, telefono, rol, especialidades } = req.body;

        if (!uid || !email || !rol) {
            return res.status(400).json({ error: 'Faltan datos obligatorios' });
        }

        // Validación exclusiva para Prestadores de Servicios (Escenarios 2 y 3)
        if (rol === 'servicios') {
            const { valid, error } = validarEspecialidades(especialidades);
            if (!valid) {
                return res.status(400).json({ error });
            }
        }

        const userRef = db.collection('usuarios').doc(uid);
        await userRef.set({
            nombre: nombre || '',
            correo: email,
            telefono: telefono || '',
            rol: rol, // 'arrendador', 'arrendatario' o 'servicios'
            especialidades: rol === 'servicios' ? especialidades : [], // Solo para prestadores
            foto_perfil: '',
            createdAt: new Date()
        });

        res.status(201).json({ message: 'Usuario registrado exitosamente en Firestore' });
    } catch (error) {
        console.error(error);
        res.status(500).json({ error: 'Error interno del servidor' });
    }
};
