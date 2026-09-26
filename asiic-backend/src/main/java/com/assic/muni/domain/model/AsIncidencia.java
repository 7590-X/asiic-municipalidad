package com.assic.muni.domain.model;

import com.assic.muni.domain.enums.IncidenciaState;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;

@Getter
@Setter
@Entity
@NoArgsConstructor
@Table(name = "as_incidencias")
@EntityListeners(AuditingEntityListener.class)
public class AsIncidencia {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "in_id", nullable = false)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "in_privacidad", insertable = false, updatable = false)
    private AsCatalogo inPrivacidadObj;

    @Column(name = "in_privacidad", insertable = true, updatable = false, nullable = false)
    private Short inPrivacidad;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "in_vecino", insertable = false, updatable = false)
    private AsVecino inVecinoObj;

    @Column(name = "in_vecino", insertable = true, updatable = false)
    private Integer inVecino;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "in_contador", nullable = false, insertable = false, updatable = false)
    private AsDomicilio inContadorObj;

    @Column(name = "in_contador", insertable = true, updatable = false)
    private String inContador;

    @Size(max = 100)
    @Column(name = "in_direccion", length = 100)
    private String inDireccion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "in_unidad", insertable = false, updatable = false)
    private AsCatalogo inUnidadObj;

    @Column(name = "in_unidad")
    private Short inUnidad;

    @Size(max = 100)
    @Column(name = "in_empleado", length = 100)
    private String inEmpleado;

    @Column(name = "in_fec_insidencia", nullable = false)
    private Instant inFecInsidencia;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "in_lugar")
    private AsCatalogo inLugar;

    @NotNull
    @Column(name = "in_comentario", nullable = false, length = Integer.MAX_VALUE)
    private String inComentario;

    @Size(max = 20)
    @Column(name = "in_latitud", length = 20)
    private String inLatitud;

    @Size(max = 20)
    @Column(name = "in_longitud", length = 20)
    private String inLongitud;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "in_estado", nullable = false)
    private IncidenciaState inEstado;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "in_tipo_servicio", insertable = false, updatable = false)
    private AsCatalogo inTipoServicioObj;

    @Column(name = "in_tipo_servicio", insertable = true)
    private Short inTipoServicio;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "in_tipo_insidencia", insertable = false, updatable = false)
    private AsTipoInsidencia inTipoIncidenciaObj;

    @Column(name = "in_tipo_insidencia", insertable = true, updatable = false)
    private Short inTipoIncidencia;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "in_tipo_denuncia")
    private AsCatalogo inTipoDenuncia;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "in_area")
    private AsCatalogo inArea;

    @Column(name = "in_propuesta", nullable = false, length = Integer.MAX_VALUE)
    private String inPropuesta;

    @CreatedDate
    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "in_fec_registro", nullable = false)
    private Instant inFecRegistro;

    @Size(max = 36)
    @CreatedBy
    @Column(name = "in_usr_registro", nullable = false, length = 36)
    private String inUsrRegistro;

    @LastModifiedDate
    @Column(name = "in_fec_modifico")
    private Instant inFecModifico;

    @Size(max = 36)
    @LastModifiedBy
    @Column(name = "in_usr_modifico", length = 36)
    private String inUsrModifico;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "in_analista")
    private AsUsuario inAnalista;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "in_jsons", columnDefinition = "jsonb")
    private JsonNode inJsons;
}