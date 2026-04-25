import { ComponentFixture, TestBed } from '@angular/core/testing';

import { GestionLote } from './gestion-lote';

describe('GestionLote', () => {
  let component: GestionLote;
  let fixture: ComponentFixture<GestionLote>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [GestionLote]
    })
    .compileComponents();

    fixture = TestBed.createComponent(GestionLote);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
