import { ComponentFixture, TestBed } from '@angular/core/testing';
import { calendarioCitas } from './calendario-citas';

describe('CalendarioCitas', () => {
  let component: calendarioCitas;
  let fixture: ComponentFixture<calendarioCitas>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [calendarioCitas]
    })
    .compileComponents();

    fixture = TestBed.createComponent(calendarioCitas);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
