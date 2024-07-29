/***** Angular Imports *****/
import {Component, inject} from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {Router} from "@angular/router";
import {FormControl, FormsModule, ReactiveFormsModule, Validators} from "@angular/forms";
/***** Services Imports *****/
import {UserAuthService} from "../../services/authentication-service/authentication.service";
import {map, Observable} from "rxjs";
import {DomSanitizer, SafeUrl} from "@angular/platform-browser";
import {baseUri} from "../../../main";

/**********************************************************************************************************************/
/****************************************************** INTERFACE *****************************************************/
/**********************************************************************************************************************/

enum LoginExperience {
  IFRAME,
  DEFAULT,
}

interface LoginOptionDto {
  label: string;
  loginUri: string;
  isSameAuthority: boolean;
}

function loginOptions(http: HttpClient): Observable<Array<LoginOptionDto>> {
  return http
    .get('/bff/login-options')
    .pipe(map((dto: any) => dto as LoginOptionDto[]));
}

/**********************************************************************************************************************/
/***************************************************** COMPONENT ******************************************************/
/**********************************************************************************************************************/

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [
    FormsModule,
    ReactiveFormsModule
  ],
  templateUrl: './login.component.html',
  styleUrl: './login.component.scss'
})
export class LoginComponent {

/**********************************************************************************************************************/
/************************************************** STORES & SERVICES *************************************************/
/**********************************************************************************************************************/

  private http: HttpClient = inject(HttpClient);
  private user: UserAuthService = inject(UserAuthService);
  private router: Router = inject(Router)
  private sanitizer: DomSanitizer = inject(DomSanitizer);

/**********************************************************************************************************************/
/***************************************************** PROPERTIES *****************************************************/
/**********************************************************************************************************************/

  protected isLoginModalDisplayed = false;
  protected iframeSrc?: SafeUrl;
  protected loginExperiences: LoginExperience[] = [];
  protected selectedLoginExperience = new FormControl<LoginExperience | null>(null, [
    Validators.required,
  ]);
  private loginUri?: string;

/**********************************************************************************************************************/
/************************************************** LIFE-CYCLE HOOKS **************************************************/
/**********************************************************************************************************************/

  protected ngOnInit(){
    loginOptions(this.http).subscribe((opts) => {
      if (opts.length) {
        this.loginUri = opts[0].loginUri;
        if (opts[0].isSameAuthority) {
          this.loginExperiences.push(LoginExperience.IFRAME);
        }
        this.loginExperiences.push(LoginExperience.DEFAULT);
        this.selectedLoginExperience.patchValue(this.loginExperiences[0]);
      }
    });
  }

/**********************************************************************************************************************/
/****************************************************** FUNCTIONS *****************************************************/
/**********************************************************************************************************************/

  protected get isLoginEnabled(): boolean {
    return (
      this.selectedLoginExperience.valid && !this.user.current.isAuthenticated
    );
  }

  protected get isAuthenticated(): boolean {
    return this.user.current.isAuthenticated;
  }

  protected login() {
    if (!this.loginUri) {
      return;
    }

    const url = new URL(this.loginUri);
    url.searchParams.append(
      'post_login_success_uri',
      `${baseUri}${this.router.url}`
    );
    url.searchParams.append(
      'post_login_failure_uri',
      `${baseUri}login-error`
    );
    const loginUrl = url.toString();

    if (this.selectedLoginExperience.value === LoginExperience.IFRAME) {
      this.iframeSrc = this.sanitizer.bypassSecurityTrustResourceUrl(loginUrl);
      this.isLoginModalDisplayed = true;
    } else {
      window.location.href = loginUrl;
    }
  }

  protected onIframeLoad(event: any) {
    if (!!event.currentTarget.src) {
      this.user.refresh();
      this.isLoginModalDisplayed = !this.user.current.isAuthenticated;
    }
  }

  protected loginExperienceLabel(le: LoginExperience) {
    return LoginExperience[le].toLowerCase()
  }
}

/**********************************************************************************************************************/
/*************************************************** END OF ANGULAR ***************************************************/
/**********************************************************************************************************************/
