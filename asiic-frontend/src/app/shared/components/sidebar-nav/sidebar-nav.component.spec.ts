import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { provideAnimations } from '@angular/platform-browser/animations';
import { SidebarNavComponent } from './sidebar-nav.component';

describe('SidebarNavComponent', () => {
  let component: SidebarNavComponent;
  let fixture: ComponentFixture<SidebarNavComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [SidebarNavComponent],
      providers: [
        provideRouter([]),
        provideAnimations(),
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(SidebarNavComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('debe crearse correctamente', () => {
    expect(component).toBeTruthy();
  });

  it('debe emitir collapsedChange cuando cambia el estado', () => {
    spyOn(component.collapsedChange, 'emit');
    component.onCollapseChange(true);
    expect(component.collapsed).toBeTrue();
    expect(component.collapsedChange.emit).toHaveBeenCalledWith(true);
  });
});
