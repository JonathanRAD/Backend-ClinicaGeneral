import { ComponentFixture, TestBed } from '@angular/core/testing';

import { DashboardInventario } from './dashboard-inventario';

describe('DashboardInventario', () => {
  let component: DashboardInventario;
  let fixture: ComponentFixture<DashboardInventario>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [DashboardInventario]
    })
    .compileComponents();

    fixture = TestBed.createComponent(DashboardInventario);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
