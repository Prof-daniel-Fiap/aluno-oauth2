import { Component, OnInit, ElementRef } from '@angular/core';
import { HttpResponse } from '@angular/common/http';
import { FormBuilder } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { Observable } from 'rxjs';
import { finalize, map } from 'rxjs/operators';

import { ICurso, Curso } from '../curso.model';
import { CursoService } from '../service/curso.service';
import { AlertError } from 'app/shared/alert/alert-error.model';
import { EventManager, EventWithContent } from 'app/core/util/event-manager.service';
import { DataUtils, FileLoadError } from 'app/core/util/data-util.service';
import { ITurma } from 'app/entities/turma/turma.model';
import { TurmaService } from 'app/entities/turma/service/turma.service';

@Component({
  selector: 'jhi-curso-update',
  templateUrl: './curso-update.component.html',
})
export class CursoUpdateComponent implements OnInit {
  isSaving = false;

  turmasSharedCollection: ITurma[] = [];

  editForm = this.fb.group({
    id: [],
    nomeCurso: [],
    dataCriacao: [],
    descricao: [],
    logoCurso: [],
    logoCursoContentType: [],
    tipo: [],
    turma: [],
  });

  constructor(
    protected dataUtils: DataUtils,
    protected eventManager: EventManager,
    protected cursoService: CursoService,
    protected turmaService: TurmaService,
    protected elementRef: ElementRef,
    protected activatedRoute: ActivatedRoute,
    protected fb: FormBuilder
  ) {}

  ngOnInit(): void {
    this.activatedRoute.data.subscribe(({ curso }) => {
      this.updateForm(curso);

      this.loadRelationshipsOptions();
    });
  }

  byteSize(base64String: string): string {
    return this.dataUtils.byteSize(base64String);
  }

  openFile(base64String: string, contentType: string | null | undefined): void {
    this.dataUtils.openFile(base64String, contentType);
  }

  setFileData(event: Event, field: string, isImage: boolean): void {
    this.dataUtils.loadFileToForm(event, this.editForm, field, isImage).subscribe({
      error: (err: FileLoadError) =>
        this.eventManager.broadcast(
          new EventWithContent<AlertError>('alunoApp.error', { message: err.message })
        ),
    });
  }

  clearInputImage(field: string, fieldContentType: string, idInput: string): void {
    this.editForm.patchValue({
      [field]: null,
      [fieldContentType]: null,
    });
    if (idInput && this.elementRef.nativeElement.querySelector('#' + idInput)) {
      this.elementRef.nativeElement.querySelector('#' + idInput).value = null;
    }
  }

  previousState(): void {
    window.history.back();
  }

  save(): void {
    this.isSaving = true;
    const curso = this.createFromForm();
    if (curso.id !== undefined) {
      this.subscribeToSaveResponse(this.cursoService.update(curso));
    } else {
      this.subscribeToSaveResponse(this.cursoService.create(curso));
    }
  }

  trackTurmaById(index: number, item: ITurma): number {
    return item.id!;
  }

  protected subscribeToSaveResponse(result: Observable<HttpResponse<ICurso>>): void {
    result.pipe(finalize(() => this.onSaveFinalize())).subscribe(
      () => this.onSaveSuccess(),
      () => this.onSaveError()
    );
  }

  protected onSaveSuccess(): void {
    this.previousState();
  }

  protected onSaveError(): void {
    // Api for inheritance.
  }

  protected onSaveFinalize(): void {
    this.isSaving = false;
  }

  protected updateForm(curso: ICurso): void {
    this.editForm.patchValue({
      id: curso.id,
      nomeCurso: curso.nomeCurso,
      dataCriacao: curso.dataCriacao,
      descricao: curso.descricao,
      logoCurso: curso.logoCurso,
      logoCursoContentType: curso.logoCursoContentType,
      tipo: curso.tipo,
      turma: curso.turma,
    });

    this.turmasSharedCollection = this.turmaService.addTurmaToCollectionIfMissing(this.turmasSharedCollection, curso.turma);
  }

  protected loadRelationshipsOptions(): void {
    this.turmaService
      .query()
      .pipe(map((res: HttpResponse<ITurma[]>) => res.body ?? []))
      .pipe(map((turmas: ITurma[]) => this.turmaService.addTurmaToCollectionIfMissing(turmas, this.editForm.get('turma')!.value)))
      .subscribe((turmas: ITurma[]) => (this.turmasSharedCollection = turmas));
  }

  protected createFromForm(): ICurso {
    return {
      ...new Curso(),
      id: this.editForm.get(['id'])!.value,
      nomeCurso: this.editForm.get(['nomeCurso'])!.value,
      dataCriacao: this.editForm.get(['dataCriacao'])!.value,
      descricao: this.editForm.get(['descricao'])!.value,
      logoCursoContentType: this.editForm.get(['logoCursoContentType'])!.value,
      logoCurso: this.editForm.get(['logoCurso'])!.value,
      tipo: this.editForm.get(['tipo'])!.value,
      turma: this.editForm.get(['turma'])!.value,
    };
  }
}
