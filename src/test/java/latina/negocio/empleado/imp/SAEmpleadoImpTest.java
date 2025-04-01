package latina.negocio.empleado.imp;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.Query;
import latina.negocio.disponibilidad.Disponibilidad;
import latina.negocio.empleado.Empleado;
import latina.negocio.empleado.TEmpleado;
import latina.negocio.turno.Turno;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SAEmpleadoImpTest {

    @Test
    void testDNIRepetido() {
        TEmpleado stubEmpleado = new TEmpleado("12345678A", "Antonio", "Pérez Salamanca", "toñito@ucm.com", "123456789", true);
        Empleado empleadoExistente = new Empleado(stubEmpleado);

        List<Object> stubResultList = new ArrayList<>();
        stubResultList.add(empleadoExistente);

        Query stubQueryBuscarPorDni = mock(Query.class);
        when(stubQueryBuscarPorDni.getResultList()).thenReturn(stubResultList);

        EntityTransaction stubTransaction = mock(EntityTransaction.class);
        EntityManager stubEntityManager = mock(EntityManager.class);
        when(stubEntityManager.createNamedQuery("Empleado.findByDNI")).thenReturn(stubQueryBuscarPorDni);
        when(stubEntityManager.getTransaction()).thenReturn(stubTransaction);

        SAEmpleadoImp sa = Mockito.spy(new SAEmpleadoImp());
        doReturn(stubEntityManager).when(sa).crearEntityManager();

        int resultado = sa.altaEmpleado(stubEmpleado);

        assertEquals(-1, resultado);
    }

    @Test
    void testCorreoRepetido() {
        // Crear un empleado con datos
        TEmpleado stubEmpleado = new TEmpleado("87654321B", "Norberto", "García Morales", "norbert777@ucm.com", "987654321", true);
        Empleado empleadoExistente = new Empleado(stubEmpleado);

        // Configurar el resultado de la consulta para DNI (vacío)
        Query stubQueryBuscarPorDni = mock(Query.class);
        when(stubQueryBuscarPorDni.setParameter("DNI", stubEmpleado.getDNI())).thenReturn(stubQueryBuscarPorDni);
        when(stubQueryBuscarPorDni.getResultList()).thenReturn(new ArrayList());

        // Configurar el resultado de la consulta para Correo
        List<Empleado> stubResultList = new ArrayList<>();
        stubResultList.add(empleadoExistente);

        // Mockear Query para buscar por Correo
        Query stubQueryBuscarPorCorreo = mock(Query.class);
        when(stubQueryBuscarPorCorreo.setParameter("correo", stubEmpleado.getCorreo())).thenReturn(stubQueryBuscarPorCorreo);
        when(stubQueryBuscarPorCorreo.getResultList()).thenReturn(stubResultList);

        // Mockear EntityManager y Transaction
        EntityTransaction stubTransaction = mock(EntityTransaction.class);
        EntityManager stubEntityManager = mock(EntityManager.class);
        when(stubEntityManager.createNamedQuery("Empleado.findByDNI")).thenReturn(stubQueryBuscarPorDni);
        when(stubEntityManager.createNamedQuery("Empleado.findByCorreo")).thenReturn(stubQueryBuscarPorCorreo);
        when(stubEntityManager.getTransaction()).thenReturn(stubTransaction);

        // Configurar el SAEmpleadoImp para usar el EntityManager mockeado
        SAEmpleadoImp sa = Mockito.spy(new SAEmpleadoImp());
        doReturn(stubEntityManager).when(sa).crearEntityManager();

        // Ejecutar el método a probar
        int resultado = sa.altaEmpleado(stubEmpleado);

        // Verificar el resultado
        assertEquals(-2, resultado);
        verify(stubTransaction).begin();
        verify(stubTransaction).rollback();
        verify(stubEntityManager, never()).persist(any(Empleado.class));
    }

    @Test
    void testDniFormatoIncorrecto() {
        // Crear un empleado con DNI en formato incorrecto
        TEmpleado stubEmpleado = new TEmpleado("12345A", "Super", "López", "superlo@ucm.com", "123456789", true);

        // Configurar mock para que las consultas devuelvan listas vacías
        Query stubQuery = mock(Query.class);
        when(stubQuery.setParameter(anyString(), anyString())).thenReturn(stubQuery);
        when(stubQuery.getResultList()).thenReturn(new ArrayList<>());

        // Mockear EntityManager y Transaction
        EntityTransaction stubTransaction = mock(EntityTransaction.class);
        EntityManager stubEntityManager = mock(EntityManager.class);
        when(stubEntityManager.createNamedQuery(anyString())).thenReturn(stubQuery);
        when(stubEntityManager.getTransaction()).thenReturn(stubTransaction);

        // Configurar el SAEmpleadoImp para usar el EntityManager mockeado
        SAEmpleadoImp sa = Mockito.spy(new SAEmpleadoImp());
        doReturn(stubEntityManager).when(sa).crearEntityManager();

        // Ejecutar el método a probar
        int resultado = sa.altaEmpleado(stubEmpleado);

        // Verificar el resultado
        assertEquals(-3, resultado);
        verify(stubTransaction).begin();
        verify(stubTransaction).rollback();
    }

    @Test
    void testTelefonoFormatoIncorrecto() {
        // Crear un empleado con teléfono en formato incorrecto
        TEmpleado stubEmpleado = new TEmpleado("12345678C", "ET", "Mi casa telefono", "Xtra@terrestre.com", "12345", true);

        // Configurar mock para que las consultas devuelvan listas vacías
        Query stubQuery = mock(Query.class);
        when(stubQuery.setParameter(anyString(), anyString())).thenReturn(stubQuery);
        when(stubQuery.getResultList()).thenReturn(new ArrayList<>());

        // Mockear EntityManager y Transaction
        EntityTransaction stubTransaction = mock(EntityTransaction.class);
        EntityManager stubEntityManager = mock(EntityManager.class);
        when(stubEntityManager.createNamedQuery(anyString())).thenReturn(stubQuery);
        when(stubEntityManager.getTransaction()).thenReturn(stubTransaction);

        // Configurar el SAEmpleadoImp para usar el EntityManager mockeado
        SAEmpleadoImp sa = Mockito.spy(new SAEmpleadoImp());
        doReturn(stubEntityManager).when(sa).crearEntityManager();

        // Ejecutar el método a probar
        int resultado = sa.altaEmpleado(stubEmpleado);

        // Verificar el resultado
        assertEquals(-4, resultado);
        verify(stubTransaction).begin();
        verify(stubTransaction).rollback();
    }

    @Test
    void testNombreFormatoIncorrecto() {
        // Crear un empleado con teléfono en formato incorrecto
        TEmpleado stubEmpleado = new TEmpleado("12345678C", "Antoniooo007", "Olaya Reverte", "aaaaaa@oooooo.com", "123456789", true);

        // Configurar mock para que las consultas devuelvan listas vacías
        Query stubQuery = mock(Query.class);
        when(stubQuery.setParameter(anyString(), anyString())).thenReturn(stubQuery);
        when(stubQuery.getResultList()).thenReturn(new ArrayList<>());

        // Mockear EntityManager y Transaction
        EntityTransaction stubTransaction = mock(EntityTransaction.class);
        EntityManager stubEntityManager = mock(EntityManager.class);
        when(stubEntityManager.createNamedQuery(anyString())).thenReturn(stubQuery);
        when(stubEntityManager.getTransaction()).thenReturn(stubTransaction);

        // Configurar el SAEmpleadoImp para usar el EntityManager mockeado
        SAEmpleadoImp sa = Mockito.spy(new SAEmpleadoImp());
        doReturn(stubEntityManager).when(sa).crearEntityManager();

        // Ejecutar el método a probar
        int resultado = sa.altaEmpleado(stubEmpleado);

        // Verificar el resultado
        assertEquals(-5, resultado);
        verify(stubTransaction).begin();
        verify(stubTransaction).rollback();
    }

    @Test
    void testApellidosFormatoIncorrecto() {
        // Crear un empleado con teléfono en formato incorrecto
        TEmpleado stubEmpleado = new TEmpleado("12345678C", "Ratón", "Pérez McDonal22", "rpérez@ucm.com", "123456789", true);

        // Configurar mock para que las consultas devuelvan listas vacías
        Query stubQuery = mock(Query.class);
        when(stubQuery.setParameter(anyString(), anyString())).thenReturn(stubQuery);
        when(stubQuery.getResultList()).thenReturn(new ArrayList<>());

        // Mockear EntityManager y Transaction
        EntityTransaction stubTransaction = mock(EntityTransaction.class);
        EntityManager stubEntityManager = mock(EntityManager.class);
        when(stubEntityManager.createNamedQuery(anyString())).thenReturn(stubQuery);
        when(stubEntityManager.getTransaction()).thenReturn(stubTransaction);

        // Configurar el SAEmpleadoImp para usar el EntityManager mockeado
        SAEmpleadoImp sa = Mockito.spy(new SAEmpleadoImp());
        doReturn(stubEntityManager).when(sa).crearEntityManager();

        // Ejecutar el método a probar
        int resultado = sa.altaEmpleado(stubEmpleado);

        // Verificar el resultado
        assertEquals(-6, resultado);
        verify(stubTransaction).begin();
        verify(stubTransaction).rollback();
    }

    @Test
    void testCorreoFormatoIncorrecto() {
        // Crear un empleado con teléfono en formato incorrecto
        TEmpleado stubEmpleado = new TEmpleado("12345678C", "Hola", "Adios McDonal", "correo_incorrecto", "123456789", true);

        // Configurar mock para que las consultas devuelvan listas vacías
        Query stubQuery = mock(Query.class);
        when(stubQuery.setParameter(anyString(), anyString())).thenReturn(stubQuery);
        when(stubQuery.getResultList()).thenReturn(new ArrayList<>());

        // Mockear EntityManager y Transaction
        EntityTransaction stubTransaction = mock(EntityTransaction.class);
        EntityManager stubEntityManager = mock(EntityManager.class);
        when(stubEntityManager.createNamedQuery(anyString())).thenReturn(stubQuery);
        when(stubEntityManager.getTransaction()).thenReturn(stubTransaction);

        // Configurar el SAEmpleadoImp para usar el EntityManager mockeado
        SAEmpleadoImp sa = Mockito.spy(new SAEmpleadoImp());
        doReturn(stubEntityManager).when(sa).crearEntityManager();

        // Ejecutar el método a probar
        int resultado = sa.altaEmpleado(stubEmpleado);

        // Verificar el resultado
        assertEquals(-7, resultado);
        verify(stubTransaction).begin();
        verify(stubTransaction).rollback();
    }

    @Test
    void testAltaEmpleadoExitoso() {
        // Crear un empleado con datos válidos
        TEmpleado stubEmpleado = new TEmpleado("12345678D", "Pepa", "Pig de Magdalena", "pepamagda02@hotmail.com", "987654321", true);

        // Configurar mock para que las consultas devuelvan listas vacías
        Query stubQuery = mock(Query.class);
        when(stubQuery.setParameter(anyString(), anyString())).thenReturn(stubQuery);
        when(stubQuery.getResultList()).thenReturn(new ArrayList<>());

        // Mockear EntityManager y Transaction
        EntityTransaction stubTransaction = mock(EntityTransaction.class);
        EntityManager stubEntityManager = mock(EntityManager.class);
        when(stubEntityManager.createNamedQuery(anyString())).thenReturn(stubQuery);
        when(stubEntityManager.getTransaction()).thenReturn(stubTransaction);

        // Mockear el comportamiento de persist para asignar un ID
        doAnswer(invocation -> {
            Empleado nuevoEmpleado = invocation.getArgument(0);
            nuevoEmpleado.setId(1);
            return null;
        }).when(stubEntityManager).persist(any(Empleado.class));

        // Configurar el SAEmpleadoImp para usar el EntityManager mockeado
        SAEmpleadoImp sa = Mockito.spy(new SAEmpleadoImp());
        doReturn(stubEntityManager).when(sa).crearEntityManager();

        // Ejecutar el método a probar
        int resultado = sa.altaEmpleado(stubEmpleado);

        // Verificar el resultado
        assertTrue(resultado > 0);
        assertEquals(1, resultado);
        verify(stubTransaction).begin();
        verify(stubTransaction).commit();
        verify(stubEntityManager).persist(any(Empleado.class));
    }

    @Test
    void testGetEmpleadosDisponiblesBueno() {
        // Mockear el Turno
        Turno stubTurno = mock(Turno.class);
        Timestamp fechaInicio = new Timestamp(System.currentTimeMillis());
        Timestamp fechaFin = new Timestamp(fechaInicio.getTime() + 3600000); // 1 hora después
        when(stubTurno.getFechaHoraInicio()).thenReturn(fechaInicio);
        when(stubTurno.getFechaHoraFin()).thenReturn(fechaFin);

        // Mockear empleados y disponibilidades
        TEmpleado tEmpleado1 = new TEmpleado("12345678E", "Juan", "Pérez", "juan@ucm.com", "123456789", true);
        Empleado empleado1 = new Empleado(tEmpleado1);
        empleado1.setId(1);

        Disponibilidad disponibilidad1 = mock(Disponibilidad.class);
        when(disponibilidad1.getEmpleado()).thenReturn(empleado1);

        List<Disponibilidad> disponibilidades = new ArrayList<>();
        disponibilidades.add(disponibilidad1);

        // Mockear Query para buscar disponibilidades
        Query stubQuery = mock(Query.class);
        when(stubQuery.setParameter("fechaHoraIni", fechaInicio)).thenReturn(stubQuery);
        when(stubQuery.setParameter("fechaHoraFin", fechaFin)).thenReturn(stubQuery);
        when(stubQuery.getResultList()).thenReturn(disponibilidades);

        // Mockear EntityManager
        EntityManager stubEntityManager = mock(EntityManager.class);
        when(stubEntityManager.find(Turno.class, 1)).thenReturn(stubTurno);
        when(stubEntityManager.createNamedQuery("Disponibilidad.findByRangoFecha")).thenReturn(stubQuery);

        // Configurar el SAEmpleadoImp para usar el EntityManager mockeado
        SAEmpleadoImp sa = Mockito.spy(new SAEmpleadoImp());
        doReturn(stubEntityManager).when(sa).crearEntityManager();

        // Ejecutar el método a probar
        List<TEmpleado> resultado = sa.getEmpleadosDisponibles(1);

        // Verificar el resultado
        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertEquals("12345678E", resultado.get(0).getDNI());
    }

    @Test
    void testGetEmpleadosDisponiblesVacio() {
        // Mockear el Turno
        Turno stubTurno = mock(Turno.class);
        Timestamp fechaInicio = new Timestamp(System.currentTimeMillis());
        Timestamp fechaFin = new Timestamp(fechaInicio.getTime() + 3600000); // 1 hora después
        when(stubTurno.getFechaHoraInicio()).thenReturn(fechaInicio);
        when(stubTurno.getFechaHoraFin()).thenReturn(fechaFin);

        // Lista vacía de disponibilidades
        List<Disponibilidad> disponibilidades = new ArrayList<>();

        // Mockear Query para buscar disponibilidades
        Query stubQuery = mock(Query.class);
        when(stubQuery.setParameter("fechaHoraIni", fechaInicio)).thenReturn(stubQuery);
        when(stubQuery.setParameter("fechaHoraFin", fechaFin)).thenReturn(stubQuery);
        when(stubQuery.getResultList()).thenReturn(disponibilidades);

        // Mockear EntityManager
        EntityManager stubEntityManager = mock(EntityManager.class);
        when(stubEntityManager.find(Turno.class, 1)).thenReturn(stubTurno);
        when(stubEntityManager.createNamedQuery("Disponibilidad.findByRangoFecha")).thenReturn(stubQuery);

        // Configurar el SAEmpleadoImp para usar el EntityManager mockeado
        SAEmpleadoImp sa = Mockito.spy(new SAEmpleadoImp());
        doReturn(stubEntityManager).when(sa).crearEntityManager();

        // Ejecutar el método a probar
        List<TEmpleado> resultado = sa.getEmpleadosDisponibles(1);

        // Verificar el resultado
        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());
    }

    @Test
    void testPersistenciaFalla() {
        EntityTransaction stubTransaction = mock(EntityTransaction.class);
        EntityManager stubEntityManager = mock(EntityManager.class);
        when(stubEntityManager.getTransaction()).thenReturn(stubTransaction);

        // Simulación de consultas vacías (DNI y Correo no existen)
        Query stubQueryDNI = mock(Query.class);
        when(stubQueryDNI.getResultList()).thenReturn(new ArrayList<>());
        when(stubEntityManager.createNamedQuery("Empleado.findByDNI")).thenReturn(stubQueryDNI);

        Query stubQueryCorreo = mock(Query.class);
        when(stubQueryCorreo.getResultList()).thenReturn(new ArrayList<>());
        when(stubEntityManager.createNamedQuery("Empleado.findByCorreo")).thenReturn(stubQueryCorreo);

        SAEmpleadoImp saEmpleado = Mockito.spy(new SAEmpleadoImp());
        doReturn(stubEntityManager).when(saEmpleado).crearEntityManager();

        // Simulación de error en persistencia
        doThrow(new RuntimeException("Error en persistencia")).when(stubEntityManager).persist(any());

        TEmpleado empleado = new TEmpleado("12345678D", "Si", "No Si", "hasta@luego.com", "987654321", true);

        int resultado = saEmpleado.altaEmpleado(empleado);

        assertEquals(-8, resultado);
    }

    //--------------------------------------------------------

    @Test
    void testBuscarEmpleados_DevuelveLista() {

        //devuelve lista con empleados
        //se comprueba que la transaccion se realizo de manera correcta y se comprueban los datos de los empleados

        EntityTransaction stubTransaction = mock(EntityTransaction.class);


        EntityManager stubEntityManager = mock(EntityManager.class);
        when(stubEntityManager.getTransaction()).thenReturn(stubTransaction);


        List<Empleado> empleadosFalsos = new ArrayList<>();
        Empleado emp1 = mock(Empleado.class);
        Empleado emp2 = mock(Empleado.class);

        when(emp1.toTransfer()).thenReturn(new TEmpleado(1, "12345678A", "Juan", "Pérez", "juan@example.com", "600123456", true));
        when(emp2.toTransfer()).thenReturn(new TEmpleado(2, "87654321B", "María", "Gómez", "maria@example.com", "611987654", true));

        empleadosFalsos.add(emp1);
        empleadosFalsos.add(emp2);


        Query stubQueryBuscarEmpleados = mock(Query.class);
        when(stubQueryBuscarEmpleados.getResultList()).thenReturn(empleadosFalsos);
        when(stubEntityManager.createNamedQuery("Empleado.findAll")).thenReturn(stubQueryBuscarEmpleados);

        SAEmpleadoImp sa = Mockito.spy(new SAEmpleadoImp());
        doReturn(stubEntityManager).when(sa).crearEntityManager();


        List<TEmpleado> resultado = sa.buscarEmpleados();


        verify(stubTransaction, times(1)).begin();
        verify(stubTransaction, times(0)).rollback();


        assertNotNull(resultado);
        assertEquals(2, resultado.size());
        assertEquals(1, resultado.get(0).getId());
        assertEquals("12345678A", resultado.get(0).getDNI());
        assertEquals("Juan", resultado.get(0).getNombre());
        assertEquals("Pérez", resultado.get(0).getApellidos());
        assertEquals("600123456", resultado.get(0).getTelefono());

        assertEquals(2, resultado.get(1).getId());
        assertEquals("87654321B", resultado.get(1).getDNI());
        assertEquals("María", resultado.get(1).getNombre());
        assertEquals("Gómez", resultado.get(1).getApellidos());
        assertEquals("611987654", resultado.get(1).getTelefono());
    }

    @Test
    void testBuscarEmpleados_ListaVacia() {


        //devuelve lista vacía , se comprueba que la transaccion fue exitosa


        EntityTransaction stubTransaction = mock(EntityTransaction.class);
        EntityManager stubEntityManager = mock(EntityManager.class);
        when(stubEntityManager.getTransaction()).thenReturn(stubTransaction);

        // Simulamos una consulta que devuelve una lista vacía
        Query stubQueryBuscarEmpleados = mock(Query.class);
        when(stubQueryBuscarEmpleados.getResultList()).thenReturn(new ArrayList<>());
        when(stubEntityManager.createNamedQuery("Empleado.findAll")).thenReturn(stubQueryBuscarEmpleados);

        SAEmpleadoImp sa = Mockito.spy(new SAEmpleadoImp());
        doReturn(stubEntityManager).when(sa).crearEntityManager();

        List<TEmpleado> resultado = sa.buscarEmpleados();

        verify(stubTransaction, times(1)).begin();
        verify(stubTransaction, times(0)).rollback();

        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());
    }

    @Test
    void testBuscarEmpleados_ErrorBaseDatos() {
        EntityTransaction stubTransaction = mock(EntityTransaction.class);
        EntityManager stubEntityManager = mock(EntityManager.class);

        when(stubEntityManager.getTransaction()).thenReturn(stubTransaction);
        when(stubTransaction.isActive()).thenReturn(true);


        Query stubQueryBuscarEmpleados = mock(Query.class);
        when(stubQueryBuscarEmpleados.getResultList()).thenThrow(new RuntimeException("Error en BD"));
        when(stubEntityManager.createNamedQuery("Empleado.findAll")).thenReturn(stubQueryBuscarEmpleados);

        SAEmpleadoImp sa = Mockito.spy(new SAEmpleadoImp());
        doReturn(stubEntityManager).when(sa).crearEntityManager();

        List<TEmpleado> resultado = sa.buscarEmpleados();

        // inicia y revierte la transacción correctamente
        verify(stubTransaction, times(1)).begin();
        verify(stubTransaction, times(1)).rollback();  // 🔥 Esto ahora debería ejecutarse

        assertNull(resultado);
    }

    }
