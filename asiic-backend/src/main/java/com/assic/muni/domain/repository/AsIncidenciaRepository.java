package com.assic.muni.domain.repository;

import com.assic.muni.domain.enums.IncidenciaState;
import com.assic.muni.domain.model.AsIncidencia;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AsIncidenciaRepository extends JpaRepository<AsIncidencia, Integer> {

    /**
     * Obtener incidencia por identificador y vecino
     *
     * @param incidenciaId Identificador de incidencia
     * @param vecinoId     Identificador de vecino
     * @return Incidencia
     */
    Optional<AsIncidencia> findByIdAndInVecino(int incidenciaId, int vecinoId);

    @Query("""
                SELECT i FROM AsIncidencia i
                LEFT JOIN FETCH i.inTipoIncidenciaObj
                LEFT JOIN FETCH i.inPrivacidadObj
                LEFT JOIN FETCH i.inUnidadObj
                LEFT JOIN FETCH i.inVecinoObj v
                LEFT JOIN FETCH v.vePersona
                LEFT JOIN FETCH v.veCorreo
                LEFT JOIN FETCH v.veTelefono
                LEFT JOIN FETCH i.inContadorObj c
                LEFT JOIN FETCH c.doDireccion
                WHERE i.id = :id AND i.inVecino = :vecinoId
            """)
    Optional<AsIncidencia> findIncidenciaDetalleByIdAndVecino(@Param("id") int id, @Param("vecinoId") int vecinoId);

    @Query("""
                SELECT i FROM AsIncidencia i
                LEFT JOIN FETCH i.inTipoIncidenciaObj
                LEFT JOIN FETCH i.inPrivacidadObj
                LEFT JOIN FETCH i.inUnidadObj
                LEFT JOIN FETCH i.inVecinoObj v
                LEFT JOIN FETCH v.vePersona
                LEFT JOIN FETCH v.veCorreo
                LEFT JOIN FETCH v.veTelefono
                LEFT JOIN FETCH i.inContadorObj c
                LEFT JOIN FETCH c.doDireccion
                WHERE i.inVecino = :vecinoId
            """)
    List<AsIncidencia> findIncidenciasDetalleByVecino(@Param("vecinoId") int vecinoId);

    @Query("""
                SELECT i FROM AsIncidencia i
                LEFT JOIN FETCH i.inTipoIncidenciaObj
                LEFT JOIN FETCH i.inPrivacidadObj
                LEFT JOIN FETCH i.inUnidadObj
                LEFT JOIN FETCH i.inVecinoObj v
                LEFT JOIN FETCH v.vePersona
                LEFT JOIN FETCH v.veCorreo
                LEFT JOIN FETCH v.veTelefono
                LEFT JOIN FETCH i.inContadorObj c
                LEFT JOIN FETCH c.doDireccion
                WHERE i.id = :id AND i.inTipoIncidencia = 1
            """)
    Optional<AsIncidencia> findQuejaDetalleById(@Param("id") int id);

    /**
     * Comprobar si existe una incidencia con el identificador indicado y el estado indicado
     *
     * @param id     Identificador de incidencia
     * @param estado Estado de incidencia
     * @return true si existe, false en caso contrario
     */
    boolean existsByIdAndInEstado(@Param("id") int id, @Param("estado") IncidenciaState estado);

    /**
     * Obtener estado de una incidencia
     *
     * @param id Identificador de incidencia
     * @return Estado de incidencia
     */
    @Query("select i.inEstado from AsIncidencia i where i.id = :id")
    Optional<IncidenciaState> findStateById(@Param("id") int id);

    /**
     * Obtener listado de incidencias asignadas a un usuario analista o agente de campo
     *
     * @param usuarioId Código de usuario
     * @param estados   Estados de incidencia
     * @return Listado de incidencias
     */
    @Query("SELECT i FROM AsIncidencia i WHERE (i.inAnalistaId = :usuarioId OR i.inAgente = :usuarioId) AND i.inEstado IN :estados")
    List<AsIncidencia> findByUsuarioAsignadoAndInEstadoIn(@Param("usuarioId") String usuarioId, @Param("estados") List<IncidenciaState> estados);

    /**
     * Incrementar cantidad de rechazos de una incidencia
     *
     * @param id Código de incidencia
     * @return Cantidad de rechazos
     */
    @Modifying
    @Query("update AsIncidencia i set  i.inRechazos = coalesce(i.inRechazos,0) + 1 where i.id = :id")
    int actualizarCantidadRechazos(@Param("id") int id);

    /**
     * Obtener la cantidad de rechazos de una incidencia
     *
     * @param id Código de incidencia
     * @return Cantidad de rechazos
     */
    @Query("select coalesce(i.inRechazos,0)  from AsIncidencia i where i.id = :id")
    Optional<Short> findInRechazosById(int id);

    /**
     * Validar si incidencia está asignada a analista
     *
     * @param id         Código de incidencia
     * @param analistaId Código UUID analista
     * @return True = asignada, false = no asignada
     */
    @Query("select exists(select 1 from AsIncidencia i where i.id = :id and (i.inAnalistaId = :analistaId or i.inAgente = :analistaId))")
    boolean existsByIdAndInAnalista(@Param("id") int id, @Param("analistaId") String analistaId);

    /**
     * Validar si incidencia pertenece a un vecino y en estados específicos
     *
     * @param id       Código de incidencia
     * @param vecinoId Código de vecino
     * @param estados  Estado permitido
     * @return
     */
    @Query("select exists(select 1 from AsIncidencia i where i.id = :id and i.inVecino = :vecinoId and i.inEstado in :estados)")
    boolean existsByIdAndInVecinoAndInEstadoIn(int id, int vecinoId, List<IncidenciaState> estados);

    /**
     * Validar si existe una incidencia por su id y tipo de incidencia
     *
     * @param id   ID de incidencia
     * @param tipo Tipo de incidencia
     * @return true = existe, false = no existe
     */
    boolean existsByIdAndInTipoIncidencia(int id, short tipo);

    /**
     * Validar si la incidencia está asignada a un agente de campo
     *
     * @param id       Código de incidencia
     * @param agenteId Código UUID agente campo
     * @return True = asignada, false = no asignada
     */
    boolean existsByIdAndInAgente(int id, String agenteId);
}
