import { ComponentFixture, TestBed } from '@angular/core/testing';

import { FormularioMedico } from './formulario-medico';

describe('FormularioMedico', () => {
  let component: FormularioMedico;
  let fixture: ComponentFixture<FormularioMedico>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [FormularioMedico]
    })
    .compileComponents();

    fixture = TestBed.createComponent(FormularioMedico);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
