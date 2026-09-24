/**
 * Validador de especialidades para Prestadores de Servicios.
 * Principio SRP: Solo tiene la responsabilidad de validar especialidades.
 * Principio DIP: No depende de Express ni de Firestore; es pura lógica de dominio.
 * Es completamente testeable de forma aislada.
 */
const { ESPECIALIDADES_VALIDAS } = require('../constants/especialidades');

/**
 * Valida que el arreglo de especialidades sea válido.
 * @param {any} especialidades - Valor recibido del body del request.
 * @returns {{ valid: boolean, error: string|null }}
 */
function validarEspecialidades(especialidades) {
  // Escenario 2: Sin especialidad seleccionada
  if (!Array.isArray(especialidades) || especialidades.length === 0) {
    return {
      valid: false,
      error: 'Debes seleccionar al menos una especialidad.',
    };
  }

  // Escenario 3: Especialidad no reconocida por el sistema
  const invalidas = especialidades.filter(
    (e) => !ESPECIALIDADES_VALIDAS.includes(e)
  );
  if (invalidas.length > 0) {
    return {
      valid: false,
      error: `Especialidad(es) no reconocida(s): ${invalidas.join(', ')}. Selecciona una opción válida de la lista disponible.`,
    };
  }

  return { valid: true, error: null };
}

module.exports = { validarEspecialidades };
