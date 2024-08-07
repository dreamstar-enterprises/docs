/***** Angular Imports *****/
import {Component, inject} from '@angular/core';
import {HttpClient} from "@angular/common/http";
/***** Services Imports *****/
import {
  User,
  UserAuthService
} from "../../services/authentication-service/authentication.service";
import {Subscription} from "rxjs";
import {NavigationComponent} from "../navigation/navigation.component";

/**********************************************************************************************************************/
/****************************************************** INTERFACE *****************************************************/
/**********************************************************************************************************************/

// Define an interface for the greeting object
interface Greeting {
  id: string;
  content: string;
}

/**********************************************************************************************************************/
/***************************************************** COMPONENT ******************************************************/
/**********************************************************************************************************************/

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [
    NavigationComponent
  ],
  templateUrl: './home.component.html',
  styleUrl: './home.component.scss'
})
export class HomeComponent {

/**********************************************************************************************************************/
/************************************************** STORES & SERVICES *************************************************/
/**********************************************************************************************************************/

  private user: UserAuthService = inject(UserAuthService);

/**********************************************************************************************************************/
/***************************************************** PROPERTIES *****************************************************/
/**********************************************************************************************************************/

  /* data properties */
  protected message = '';

  /* subscriptions */
  private userSubscription?: Subscription;

/**********************************************************************************************************************/
/************************************************** LIFE-CYCLE HOOKS **************************************************/
/**********************************************************************************************************************/

  protected ngOnInit(){
    this.userSubscription = this.user.valueChanges.subscribe((u) => {
      this.message = u.isAuthenticated
        ? `Hi ${u.name}, you are granted with ${HomeComponent.rolesStr(u)}.`
        : 'You are not authenticated.';
    });
  }

  ngOnDestroy() {
    this.userSubscription?.unsubscribe();
  }

/**********************************************************************************************************************/
/****************************************************** FUNCTIONS *****************************************************/
/**********************************************************************************************************************/

  static rolesStr(user: User) {
    if(!user?.roles?.length) {
      return '[]'
    }
    return `["${user.roles.join('", "')}"]`
  }

}

/**********************************************************************************************************************/
/*************************************************** END OF ANGULAR ***************************************************/
/**********************************************************************************************************************/
