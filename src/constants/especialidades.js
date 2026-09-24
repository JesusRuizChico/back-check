/**
 * Catálogo centralizado de especialidades válidas para Prestadores de Servicios.
 * Principio OCP: Para agregar una nueva especialidad, solo se modifica este archivo.
 * Ningún controlador ni validador necesita ser alterado.
 */
const ESPECIALIDADES_VALIDAS = [
  'albanileria',
  'pintura',
  'plomeria',
  'electricidad',
];

module.exports = { ESPECIALIDADES_VALIDAS };
