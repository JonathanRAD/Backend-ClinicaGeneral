
import { Component } from '@angular/core';
import { RouterModule } from '@angular/router';
import { Footer } from '../../../../core/componentes/footer/footer';
import { Navbar } from '../../../../core/componentes/navbar/navbar';

@Component({
  selector: 'app-layout-portal',
  standalone: true,
  imports: [
    RouterModule,
    Footer,
    Navbar, 
  ],
  templateUrl: './layout-portal.html',
  styleUrls: ['./layout-portal.css']
})
export class LayoutPortalComponent {
}