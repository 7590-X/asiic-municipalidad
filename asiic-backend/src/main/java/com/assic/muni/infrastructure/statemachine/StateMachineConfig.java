package com.assic.muni.infrastructure.statemachine;

import com.assic.muni.domain.enums.IncidenciaEvent;
import com.assic.muni.domain.enums.IncidenciaState;
import org.springframework.context.annotation.Configuration;
import org.springframework.statemachine.config.EnableStateMachineFactory;
import org.springframework.statemachine.config.StateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineConfigurationConfigurer;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;

import java.util.EnumSet;

@Configuration
@EnableStateMachineFactory
public class StateMachineConfig extends StateMachineConfigurerAdapter<IncidenciaState, IncidenciaEvent> {

    @Override
    public void configure(StateMachineConfigurationConfigurer<IncidenciaState, IncidenciaEvent> config) throws Exception {
        config
                .withConfiguration()
                .autoStartup(false); // La máquina se inicia programáticamente tras la rehidratación
    }

    @Override
    public void configure(StateMachineStateConfigurer<IncidenciaState, IncidenciaEvent> states) throws Exception {
        states
                .withStates()
                .initial(IncidenciaState.BORRADOR)
                .end(IncidenciaState.FINALIZADA)
                .states(EnumSet.allOf(IncidenciaState.class));
    }

    @Override
    public void configure(StateMachineTransitionConfigurer<IncidenciaState, IncidenciaEvent> transitions) throws Exception {
        transitions
                //////////////////////////////////////////////////
                // HAPPY PATH                                   //
                //////////////////////////////////////////////////
                // BORRADOR -> ENVIADA
                .withExternal()
                .source(IncidenciaState.BORRADOR)
                .event(IncidenciaEvent.ENVIAR_INCIDENCIA)
                .target(IncidenciaState.ENVIADA)
                .and()
                // ENVIADA -> ASIGNADA
                .withExternal()
                .source(IncidenciaState.ENVIADA)
                .event(IncidenciaEvent.ASIGNAR_ANALISTA)
                .target(IncidenciaState.ASIGNADA)
                .and()
                // ASIGNADA -> ACEPTADA
                .withExternal()
                .source(IncidenciaState.ASIGNADA)
                .event(IncidenciaEvent.ACEPTAR_INCIDENCIA)
                .target(IncidenciaState.ACEPTADA)
                .and()
                // ACEPTADA -> ASIGNADA_CAMPO
                .withExternal()
                .source(IncidenciaState.ACEPTADA)
                .event(IncidenciaEvent.ASIGNAR_CAMPO)
                .target(IncidenciaState.ASIGNADA_CAMPO)
                .and()
                // ASIGNADA_CAMPO -> RECOLECCION_FIN
                .withExternal()
                .source(IncidenciaState.ASIGNADA_CAMPO)
                .event(IncidenciaEvent.FINALIZAR_RECOLECCION)
                .target(IncidenciaState.RECOLECCION_FIN)
                .and()
                // RECOLECCION_FIN -> CONFIRMADA
                .withExternal()
                .source(IncidenciaState.RECOLECCION_FIN)
                .event(IncidenciaEvent.CONFIRMAR_INCIDENCIA)
                .target(IncidenciaState.CONFIRMADA)
                .and()
                // CONFIRMADA -> APERTURADA
                .withExternal()
                .source(IncidenciaState.CONFIRMADA)
                .event(IncidenciaEvent.APERTURAR_CASO)
                .target(IncidenciaState.APERTURADA)
                .and()
                // APERTURADA -> SOLUCIONANDO
                .withExternal()
                .source(IncidenciaState.APERTURADA)
                .event(IncidenciaEvent.INICIAR_CASO)
                .target(IncidenciaState.SOLUCIONANDO)
                .and()
                // SOLUCIONANDO -> SOLUCIONADA
                .withExternal()
                .source(IncidenciaState.SOLUCIONADA)
                .event(IncidenciaEvent.CONFIRMAR_SOLUCION)
                .target(IncidenciaState.SOLUCIONADA)
                .and()
                // SOLUCIONADA -> FINALIZADA
                .withExternal()
                .source(IncidenciaState.SOLUCIONADA)
                .event(IncidenciaEvent.FINALIZAR)
                .target(IncidenciaState.FINALIZADA)
                .and()

                //////////////////////////////////////////////////
                // SUBFLOW #1                                   //
                //////////////////////////////////////////////////

                // ASIGNADA -> RECHAZADA
                .withExternal()
                .source(IncidenciaState.ASIGNADA)
                .event(IncidenciaEvent.RECHAZAR_INCIDENCIA)
                .target(IncidenciaState.RECHAZADA)
                .and()

                //////////////////////////////////////////////////
                // SUBFLOW #2                                   //
                //////////////////////////////////////////////////

                // RECOLECCION_FIN -> RECHAZADA
                .withExternal()
                .target(IncidenciaState.RECOLECCION_FIN)
                .event(IncidenciaEvent.RECHAZAR_INCIDENCIA)
                .target(IncidenciaState.RECHAZADA)
                .and()

                //////////////////////////////////////////////////
                // SUBFLOW #3                                   //
                //////////////////////////////////////////////////

                // RECHAZADA -> APELADA
                .withExternal()
                .source(IncidenciaState.RECHAZADA)
                .event(IncidenciaEvent.APELAR_INCIDENCIA)
                .target(IncidenciaState.APELADA)
                .and()
                // APELADA -> APELACION_ASIGNADA
                .withExternal()
                .source(IncidenciaState.APELACION_ASIGNADA)
                .event(IncidenciaEvent.ASIGNAR_APELACION_ANALISTA)
                .target(IncidenciaState.ASIGNADA)
                .and()
                // RECHAZADA -> FINALIZADA
                .withExternal()
                .source(IncidenciaState.RECHAZADA)
                .event(IncidenciaEvent.FINALIZAR)
                .target(IncidenciaState.FINALIZADA)
                .and()

                //////////////////////////////////////////////////
                // SUBFLOW #3                                   //
                //////////////////////////////////////////////////

                // ACEPTADA -> CONFIRMADA
                .withExternal()
                .source(IncidenciaState.ACEPTADA)
                .event(IncidenciaEvent.CONFIRMAR_INCIDENCIA)
                .target(IncidenciaState.CONFIRMADA)
                .and()

                //////////////////////////////////////////////////
                // SUBFLOW #4                                   //
                //////////////////////////////////////////////////

                // SOLUCIONANDO -> BLOQUEADA
                .withExternal()
                .source(IncidenciaState.SOLUCIONANDO)
                .event(IncidenciaEvent.BLOQUEAR_CASO)
                .target(IncidenciaState.BLOQUEADA)
                .and()
                // BLOQUEADA -> APERTURADA
                .withExternal()
                .source(IncidenciaState.BLOQUEADA)
                .event(IncidenciaEvent.RE_APERTURAR_CASO)
                .target(IncidenciaState.APERTURADA);
    }
}
