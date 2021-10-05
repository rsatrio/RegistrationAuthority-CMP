import { BasicResp } from '@/model/BasicResp';
import { HttpClient } from '@angular/common/http';
import {
    Component,
    OnInit,
    Renderer2,
    OnDestroy,
    HostBinding
} from '@angular/core';
import { FormGroup, FormControl, Validators } from '@angular/forms';
import { AppService } from '@services/app.service';
import { RegisterService } from '@services/register.service';
import { BlockUI, NgBlockUI } from 'ng-block-ui';
import { ToastrService } from 'ngx-toastr';
import { Observable } from 'rxjs';

@Component({
    selector: 'app-register',
    templateUrl: './register.component.html',
    styleUrls: ['./register.component.scss']
})
export class RegisterComponent implements OnInit, OnDestroy {
    @HostBinding('class') class = 'register-box';

    @BlockUI() blockUI: NgBlockUI;
    public registerForm: FormGroup;
    public isAuthLoading = false;
    public isGoogleLoading = false;
    public isFacebookLoading = false;

    constructor(
        private renderer: Renderer2,
        private toastr: ToastrService,
        private appService: AppService,
        private regService: RegisterService,
        
    ) { }

    ngOnInit() {
        this.renderer.addClass(
            document.querySelector('app-root'),
            'register-page'
        );
        this.registerForm = new FormGroup({
            email: new FormControl(null, [Validators.required,]),
            password: new FormControl(null, [Validators.required,Validators.minLength(8),Validators.maxLength(30)]),
            retypePassword: new FormControl(null, [Validators.required,Validators.minLength(8),Validators.maxLength(30)])
        });
    }

    async registerByAuth() {
        if(/^[a-zA-Z0-9._-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,4}$/.test(this.registerForm.get('email').value)==false)  {
            this.toastr.error("Wrong email format");
            return 100;
        }

        let pass:string=this.registerForm.get('password').value;
        if(pass.length<8)   {
            this.toastr.error("Password minimum length is 8 characters");
            return 100;
        }

        if(this.registerForm.get('password').value!=this.registerForm.get('retypePassword').value)  {
            this.toastr.error("Password not matched");
            return 100;
        }

        if (this.registerForm.valid) {
            this.isAuthLoading = true;
            this.blockUI.start("Please wait a moment...")
            // await this.appService.registerByAuth(this.registerForm.value);
            let resp: Observable<BasicResp> = this.regService.registerNow(this.registerForm.get('email').value,
                this.registerForm.get('password').value);
            resp.subscribe((resp)=> {
                if(resp.statusOk)   {
                    this.toastr.success('Registration Successfull');
                }
                else{
                    this.toastr.error('Registration Failed. Please try again later');
                }
                this.blockUI.stop();
            },
            (err)=> {
                this.toastr.error('Registration Failed. Please try again later');
                this.blockUI.stop();
            });
            this.isAuthLoading = false;
        } else {
            this.toastr.error('Form is not valid. Please check all the values again');
        }
    }

    async registerByGoogle() {
        this.isGoogleLoading = true;
        await this.appService.registerByGoogle();
        this.isGoogleLoading = false;
    }

    async registerByFacebook() {
        this.isFacebookLoading = true;
        await this.appService.registerByFacebook();
        this.isFacebookLoading = false;
    }

    ngOnDestroy() {
        this.renderer.removeClass(
            document.querySelector('app-root'),
            'register-page'
        );
    }
}
