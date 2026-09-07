import { ComponentFixture, TestBed } from '@angular/core/testing';

import { NuestrosMedicos } from './nuestros-medicos';

describe('NuestrosMedicos', () => {
  let component: NuestrosMedicos;
  let fixture: ComponentFixture<NuestrosMedicos>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [NuestrosMedicos]
    })
    .compileComponents();

    fixture = TestBed.createComponent(NuestrosMedicos);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
