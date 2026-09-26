import { AbstractControl, AsyncValidatorFn, ValidationErrors } from "@angular/forms";
import { ValidacionesService } from "../../core/services/validaciones.service";
import { catchError, map, of, switchMap, timer, Observable } from "rxjs";
import { HttpErrorResponse } from "@angular/common/http";

export function telefonoValidator(service: ValidacionesService): AsyncValidatorFn {
    // Cache manual para evitar loops infinitos
    let lastValue = '';
    let lastResult: ValidationErrors | null = null;

    return (control: AbstractControl): Observable<ValidationErrors | null> => {
        if (control.value === lastValue) {
            return of(lastResult);
        }

        return timer(500).pipe(
            switchMap(() => {
                return service.validacionNumero('correo', control.value);
            }),
            map(res => {
                lastValue = control.value;
                lastResult = null;
                return lastResult;
            }),
            catchError((err: HttpErrorResponse) => {
                const message = err.error?.message;
                lastValue = control.value;
                lastResult = { invalidCorreo: message };
                return of(lastResult);
            })
        );
    }
}