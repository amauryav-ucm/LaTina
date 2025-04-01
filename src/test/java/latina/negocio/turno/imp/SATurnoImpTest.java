package latina.negocio.turno.imp;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;
import latina.negocio.turno.SATurno;
import latina.negocio.turno.Turno;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class SATurnoImpTest {

    @Test
    public void testGetTurnosSemanaConTurnos() {
        EntityManager em = mock(EntityManager.class);
        SATurnoImp sat = Mockito.spy(new SATurnoImp());
        doReturn(em).when(sat).createEntityManager();

        // Simular valores de entrada
        Timestamp semana = Timestamp.valueOf(LocalDateTime.now().with(TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY)));

        List<Turno> turnosSimulados = new ArrayList<>();
        turnosSimulados.add(new Turno());
        turnosSimulados.add(new Turno());

        TypedQuery<Turno> query = mock(TypedQuery.class);
        when(em.createQuery(anyString(), eq(Turno.class))).thenReturn(query);
        when(query.setParameter(anyString(), any())).thenReturn(query);
        when(query.getResultList()).thenReturn(turnosSimulados);

        List<Turno> resultado = sat.getTurnosSemana(semana);
        assertEquals(2, resultado.size());
    }

    @Test
    public void testGetTurnosSemanaSinTurnos() {
        EntityManager em = mock(EntityManager.class);
        SATurnoImp sat = Mockito.spy(new SATurnoImp());
        doReturn(em).when(sat).createEntityManager();

        Timestamp semana = Timestamp.valueOf(LocalDateTime.now().with(TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY)));

        TypedQuery<Turno> query = mock(TypedQuery.class);
        when(em.createQuery(anyString(), eq(Turno.class))).thenReturn(query);
        when(query.setParameter(anyString(), any())).thenReturn(query);
        when(query.getResultList()).thenReturn(new ArrayList<>());

        List<Turno> resultado = sat.getTurnosSemana(semana);
        assertTrue(resultado.isEmpty());
    }

    @Test
    public void testGetTurnosSemanaError() {
        EntityManager em = mock(EntityManager.class);
        SATurnoImp sat = Mockito.spy(new SATurnoImp());
        doReturn(em).when(sat).createEntityManager();

        Timestamp semana = Timestamp.valueOf(LocalDateTime.now().with(TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY)));

        when(em.createQuery(anyString(), eq(Turno.class))).thenThrow(new RuntimeException("Database error"));

        List<Turno> resultado = sat.getTurnosSemana(semana);
        assertNull(resultado);
}
}