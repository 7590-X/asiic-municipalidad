import { AbstractControl, ValidationErrors, ValidatorFn } from '@angular/forms';

export const passwordMatchValidator: ValidatorFn = (control: AbstractControl): ValidationErrors | null => {
  const password = control.get('password');
  const confirmPassword = control.get('confirmPassword');

  if (password && confirmPassword && password.value !== confirmPassword.value) {
    if (confirmPassword.errors) {
      confirmPassword.setErrors({ ...confirmPassword.errors, passwordMismatch: true });
    } else {
      confirmPassword.setErrors({ passwordMismatch: true });
    }
    return { passwordMismatch: true };
  } else if (confirmPassword?.hasError('passwordMismatch')) {
    const errors = { ...confirmPassword.errors };
    delete errors['passwordMismatch'];
    confirmPassword.setErrors(Object.keys(errors).length ? errors : null);
  }
  return null;
};
