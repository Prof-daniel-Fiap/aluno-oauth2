jest.mock('@angular/router');

import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpResponse } from '@angular/common/http';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { FormBuilder } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { of, Subject } from 'rxjs';

import { AlunoService } from '../service/aluno.service';
import { IAluno, Aluno } from '../aluno.model';
import { ITurma } from 'app/entities/turma/turma.model';
import { TurmaService } from 'app/entities/turma/service/turma.service';

import { AlunoUpdateComponent } from './aluno-update.component';

describe('Component Tests', () => {
  describe('Aluno Management Update Component', () => {
    let comp: AlunoUpdateComponent;
    let fixture: ComponentFixture<AlunoUpdateComponent>;
    let activatedRoute: ActivatedRoute;
    let alunoService: AlunoService;
    let turmaService: TurmaService;

    beforeEach(() => {
      TestBed.configureTestingModule({
        imports: [HttpClientTestingModule],
        declarations: [AlunoUpdateComponent],
        providers: [FormBuilder, ActivatedRoute],
      })
        .overrideTemplate(AlunoUpdateComponent, '')
        .compileComponents();

      fixture = TestBed.createComponent(AlunoUpdateComponent);
      activatedRoute = TestBed.inject(ActivatedRoute);
      alunoService = TestBed.inject(AlunoService);
      turmaService = TestBed.inject(TurmaService);

      comp = fixture.componentInstance;
    });

    describe('ngOnInit', () => {
      it('Should call Turma query and add missing value', () => {
        const aluno: IAluno = { id: 456 };
        const turmas: ITurma[] = [{ id: 14263 }];
        aluno.turmas = turmas;

        const turmaCollection: ITurma[] = [{ id: 88598 }];
        spyOn(turmaService, 'query').and.returnValue(of(new HttpResponse({ body: turmaCollection })));
        const additionalTurmas = [...turmas];
        const expectedCollection: ITurma[] = [...additionalTurmas, ...turmaCollection];
        spyOn(turmaService, 'addTurmaToCollectionIfMissing').and.returnValue(expectedCollection);

        activatedRoute.data = of({ aluno });
        comp.ngOnInit();

        expect(turmaService.query).toHaveBeenCalled();
        expect(turmaService.addTurmaToCollectionIfMissing).toHaveBeenCalledWith(turmaCollection, ...additionalTurmas);
        expect(comp.turmasSharedCollection).toEqual(expectedCollection);
      });

      it('Should update editForm', () => {
        const aluno: IAluno = { id: 456 };
        const turmas: ITurma = { id: 58232 };
        aluno.turmas = [turmas];

        activatedRoute.data = of({ aluno });
        comp.ngOnInit();

        expect(comp.editForm.value).toEqual(expect.objectContaining(aluno));
        expect(comp.turmasSharedCollection).toContain(turmas);
      });
    });

    describe('save', () => {
      it('Should call update service on save for existing entity', () => {
        // GIVEN
        const saveSubject = new Subject();
        const aluno = { id: 123 };
        spyOn(alunoService, 'update').and.returnValue(saveSubject);
        spyOn(comp, 'previousState');
        activatedRoute.data = of({ aluno });
        comp.ngOnInit();

        // WHEN
        comp.save();
        expect(comp.isSaving).toEqual(true);
        saveSubject.next(new HttpResponse({ body: aluno }));
        saveSubject.complete();

        // THEN
        expect(comp.previousState).toHaveBeenCalled();
        expect(alunoService.update).toHaveBeenCalledWith(aluno);
        expect(comp.isSaving).toEqual(false);
      });

      it('Should call create service on save for new entity', () => {
        // GIVEN
        const saveSubject = new Subject();
        const aluno = new Aluno();
        spyOn(alunoService, 'create').and.returnValue(saveSubject);
        spyOn(comp, 'previousState');
        activatedRoute.data = of({ aluno });
        comp.ngOnInit();

        // WHEN
        comp.save();
        expect(comp.isSaving).toEqual(true);
        saveSubject.next(new HttpResponse({ body: aluno }));
        saveSubject.complete();

        // THEN
        expect(alunoService.create).toHaveBeenCalledWith(aluno);
        expect(comp.isSaving).toEqual(false);
        expect(comp.previousState).toHaveBeenCalled();
      });

      it('Should set isSaving to false on error', () => {
        // GIVEN
        const saveSubject = new Subject();
        const aluno = { id: 123 };
        spyOn(alunoService, 'update').and.returnValue(saveSubject);
        spyOn(comp, 'previousState');
        activatedRoute.data = of({ aluno });
        comp.ngOnInit();

        // WHEN
        comp.save();
        expect(comp.isSaving).toEqual(true);
        saveSubject.error('This is an error!');

        // THEN
        expect(alunoService.update).toHaveBeenCalledWith(aluno);
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

    describe('Getting selected relationships', () => {
      describe('getSelectedTurma', () => {
        it('Should return option if no Turma is selected', () => {
          const option = { id: 123 };
          const result = comp.getSelectedTurma(option);
          expect(result === option).toEqual(true);
        });

        it('Should return selected Turma for according option', () => {
          const option = { id: 123 };
          const selected = { id: 123 };
          const selected2 = { id: 456 };
          const result = comp.getSelectedTurma(option, [selected2, selected]);
          expect(result === selected).toEqual(true);
          expect(result === selected2).toEqual(false);
          expect(result === option).toEqual(false);
        });

        it('Should return option if this Turma is not selected', () => {
          const option = { id: 123 };
          const selected = { id: 456 };
          const result = comp.getSelectedTurma(option, [selected]);
          expect(result === option).toEqual(true);
          expect(result === selected).toEqual(false);
        });
      });
    });
  });
});
