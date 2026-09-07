import { ComponentFixture, TestBed } from '@angular/core/testing';

import { FormularioProveedor } from './formulario-proveedor';

describe('FormularioProveedor', () => {
  let component: FormularioProveedor;
  let fixture: ComponentFixture<FormularioProveedor>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [FormularioProveedor]
    })
    .compileComponents();

    fixture = TestBed.createComponent(FormularioProveedor);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
