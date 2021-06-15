jest.mock('@angular/router');

import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpResponse } from '@angular/common/http';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { FormBuilder } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { of, Subject } from 'rxjs';

import { CursoService } from '../service/curso.service';
import { ICurso, Curso } from '../curso.model';
import { ITurma } from 'app/entities/turma/turma.model';
import { TurmaService } from 'app/entities/turma/service/turma.service';

import { CursoUpdateComponent } from './curso-update.component';

describe('Component Tests', () => {
  describe('Curso Management Update Component', () => {
    let comp: CursoUpdateComponent;
    let fixture: ComponentFixture<CursoUpdateComponent>;
    let activatedRoute: ActivatedRoute;
    let cursoService: CursoService;
    let turmaService: TurmaService;

    beforeEach(() => {
      TestBed.configureTestingModule({
        imports: [HttpClientTestingModule],
        declarations: [CursoUpdateComponent],
        providers: [FormBuilder, ActivatedRoute],
      })
        .overrideTemplate(CursoUpdateComponent, '')
        .compileComponents();

      fixture = TestBed.createComponent(CursoUpdateComponent);
      activatedRoute = TestBed.inject(ActivatedRoute);
      cursoService = TestBed.inject(CursoService);
      turmaService = TestBed.inject(TurmaService);

      comp = fixture.componentInstance;
    });

    describe('ngOnInit', () => {
      it('Should call Turma query and add missing value', () => {
        const curso: ICurso = { id: 456 };
        const turma: ITurma = { id: 62864 };
        curso.turma = turma;

        const turmaCollection: ITurma[] = [{ id: 6161 }];
        spyOn(turmaService, 'query').and.returnValue(of(new HttpResponse({ body: turmaCollection })));
        const additionalTurmas = [turma];
        const expectedCollection: ITurma[] = [...additionalTurmas, ...turmaCollection];
        spyOn(turmaService, 'addTurmaToCollectionIfMissing').and.returnValue(expectedCollection);

        activatedRoute.data = of({ curso });
        comp.ngOnInit();

        expect(turmaService.query).toHaveBeenCalled();
        expect(turmaService.addTurmaToCollectionIfMissing).toHaveBeenCalledWith(turmaCollection, ...additionalTurmas);
        expect(comp.turmasSharedCollection).toEqual(expectedCollection);
      });

      it('Should update editForm', () => {
        const curso: ICurso = { id: 456 };
        const turma: ITurma = { id: 25419 };
        curso.turma = turma;

        activatedRoute.data = of({ curso });
        comp.ngOnInit();

        expect(comp.editForm.value).toEqual(expect.objectContaining(curso));
        expect(comp.turmasSharedCollection).toContain(turma);
      });
    });

    describe('save', () => {
      it('Should call update service on save for existing entity', () => {
        // GIVEN
        const saveSubject = new Subject();
        const curso = { id: 123 };
        spyOn(cursoService, 'update').and.returnValue(saveSubject);
        spyOn(comp, 'previousState');
        activatedRoute.data = of({ curso });
        comp.ngOnInit();

        // WHEN
        comp.save();
        expect(comp.isSaving).toEqual(true);
        saveSubject.next(new HttpResponse({ body: curso }));
        saveSubject.complete();

        // THEN
        expect(comp.previousState).toHaveBeenCalled();
        expect(cursoService.update).toHaveBeenCalledWith(curso);
        expect(comp.isSaving).toEqual(false);
      });

      it('Should call create service on save for new entity', () => {
        // GIVEN
        const saveSubject = new Subject();
        const curso = new Curso();
        spyOn(cursoService, 'create').and.returnValue(saveSubject);
        spyOn(comp, 'previousState');
        activatedRoute.data = of({ curso });
        comp.ngOnInit();

        // WHEN
        comp.save();
        expect(comp.isSaving).toEqual(true);
        saveSubject.next(new HttpResponse({ body: curso }));
        saveSubject.complete();

        // THEN
        expect(cursoService.create).toHaveBeenCalledWith(curso);
        expect(comp.isSaving).toEqual(false);
        expect(comp.previousState).toHaveBeenCalled();
      });

      it('Should set isSaving to false on error', () => {
        // GIVEN
        const saveSubject = new Subject();
        const curso = { id: 123 };
        spyOn(cursoService, 'update').and.returnValue(saveSubject);
        spyOn(comp, 'previousState');
        activatedRoute.data = of({ curso });
        comp.ngOnInit();

        // WHEN
        comp.save();
        expect(comp.isSaving).toEqual(true);
        saveSubject.error('This is an error!');

        // THEN
        expect(cursoService.update).toHaveBeenCalledWith(curso);
        expect(comp.isSaving).toEqual(false);
        expect(comp.previousState).not.toHaveBeenCalled();
      });
    });

    describe('Tracking relationships identifiers', () => {
      describe('trackTurmaById', () => {
        it('Should return tracked Turma primary key', () => {
          const entity = { id: 123 };
          const trackResult = comp.trackTurmaById(0, entity);
          expect(trackResult).toEqual(entity.id);
        });
      });
    });
  });
});
