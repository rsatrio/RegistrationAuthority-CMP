import { Component } from '@angular/core';
import { FieldType } from '@ngx-formly/material';


@Component({
  selector: 'formly-field-file',
  template: `
    <label>Proof of Identity:  </label>
    <input type="file" [formControl]="formControl" [formlyAttributes]="field">
  `,
})
export class FormlyFieldFile extends FieldType {}
