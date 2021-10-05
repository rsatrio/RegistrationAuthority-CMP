import { Directive } from '@angular/core';
import { NG_VALUE_ACCESSOR, ControlValueAccessor } from '@angular/forms';

@Directive({
  // tslint:disable-next-line
  selector: 'input[type=file]',
  host: {
    '(change)': 'onChange($event.target.files)',
    '(blur)': 'onTouched()',
  },
  providers: [
    { provide: NG_VALUE_ACCESSOR, useExisting: FileValueAccessor, multi: true },
  ],
})
// https://github.com/angular/angular/issues/7341
export class FileValueAccessor implements ControlValueAccessor {
  value: any;
  onChange = (_) => {

  };
  onTouched = () => { };



  writeValue(value) {
    if (value instanceof FileList) {
      console.log("FileList instance");
      this.onChange(value.item(0));

    } else {
      if (value) {
        this.onChange(value.item(0));
      }
      console.log("NOT FileList instance");
    }
  }

  registerOnChange(fn: any) {
    // console.log('oke2');
    this.onChange = fn;
    // console.log(this.onChange.toString);

  }
  registerOnTouched(fn: any) {
    this.onTouched = fn;


  }
}
