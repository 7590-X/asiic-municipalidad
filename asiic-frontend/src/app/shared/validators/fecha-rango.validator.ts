import { AbstractControl, ValidationErrors, ValidatorFn } from '@angular/forms';

/**
 * Validador para fechas de incidencias:
 * - No permite fechas posteriores a la fecha actual.
 * - No permite fechas mayores a un mes atrás.
 */
export function fechaRangoIncidenciaValidator(): ValidatorFn {
  return (control: AbstractControl): ValidationErrors | null => {
    if (!control.value) {
      return null;
    }

    const valDate = new Date(control.value);
    if (isNaN(valDate.getTime())) {
      return { fechaInvalida: true };
    }

    const now = new Date();
    // Margen de tolerancia de 5 minutos para que la hora actual no falle por segundos
    now.setMinutes(now.getMinutes() + 5);

    const unMesAtras = new Date();
    unMesAtras.setMonth(unMesAtras.getMonth() - 1);
    unMesAtras.setHours(0, 0, 0, 0);

    if (valDate > now) {
      return { fechaFutura: true };
    }

    if (valDate < unMesAtras) {
      return { fechaAntigua: true };
    }

    return null;
  };
}
