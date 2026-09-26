import { AbstractControl, AsyncValidatorFn, ValidationErrors } from "@angular/forms";
import { ValidacionesService } from "../../core/services/validaciones.service";
import { catchError, map, of, switchMap, timer, Observable } from "rxjs";
import { HttpErrorResponse } from "@angular/common/http";

export function cuiValidator(service: ValidacionesService): AsyncValidatorFn {
    // Cache manual para evitar loops infinitos
    let lastValue = '';
    let lastResult: ValidationErrors | null = null;

    return (control: AbstractControl): Observable<ValidationErrors | null> => {
        if (control.value === lastValue) {
            return of(lastResult);
        }

        console.log("cuiValidator: ejecutando validación para", control.value);
        return timer(500).pipe(
            switchMap(() => {
                console.log("cuiValidator: enviando petición HTTP...");
                return service.validacionNumero('cui', control.value);
            }),
            map(res => {
                console.log("cuiValidator: respuesta exitosa", res);
                lastValue = control.value;
                lastResult = null;
                return lastResult;
            }),
            catchError((err: HttpErrorResponse) => {
                console.log("cuiValidator: error HTTP capturado", err);
                const message = err.error?.message || 'CUI inválido o ya registrado';
                lastValue = control.value;
                lastResult = { invalidCui: message };
                return of(lastResult);
            })
        );
    }
}