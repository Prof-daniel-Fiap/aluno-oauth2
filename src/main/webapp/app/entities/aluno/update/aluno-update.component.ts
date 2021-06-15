import { Component, OnInit, ElementRef } from '@angular/core';
import { HttpResponse } from '@angular/common/http';
import { FormBuilder } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { Observable } from 'rxjs';
import { finalize, map } from 'rxjs/operators';

import { IAluno, Aluno } from '../aluno.model';
import { AlunoService } from '../service/aluno.service';
import { AlertError } from 'app/shared/alert/alert-error.model';
import { EventManager, EventWithContent } from 'app/core/util/event-manager.service';
import { DataUtils, FileLoadError } from 'app/core/util/data-util.service';
import { ITurma } from 'app/entities/turma/turma.model';
import { TurmaService } from 'app/entities/turma/service/turma.service';

@Component({
  selector: 'jhi-aluno-update',
  templateUrl: './aluno-update.component.html',
})
export class AlunoUpdateComponent implements OnInit {
  isSaving = false;

  turmasSharedCollection: ITurma[] = [];

  editForm = this.fb.group({
    id: [],
    nome: [],
    email: [],
    dataNascimento: [],
    rm: [],
    foto: [],
    fotoContentType: [],
    status: [],
    turmas: [],
  });

  constructor(
    protected dataUtils: DataUtils,
    protected eventManager: EventManager,
    protected alunoService: AlunoService,
    protected turmaService: TurmaService,
    protected elementRef: ElementRef,
    protected activatedRoute: ActivatedRoute,
    protected fb: FormBuilder
  ) {}

  ngOnInit(): void {
    this.activatedRoute.data.subscribe(({ aluno }) => {
      this.updateForm(aluno);

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
    const aluno = this.createFromForm();
    if (aluno.id !== undefined) {
      this.subscribeToSaveResponse(this.alunoService.update(aluno));
    } else {
      this.subscribeToSaveResponse(this.alunoService.create(aluno));
    }
  }

  trackTurmaById(index: number, item: ITurma): number {
    return item.id!;
  }

  getSelectedTurma(option: ITurma, selectedVals?: ITurma[]): ITurma {
    if (selectedVals) {
      for (const selectedVal of selectedVals) {
        if (option.id === selectedVal.id) {
          return selectedVal;
        }
      }
    }
    return option;
  }

  protected subscribeToSaveResponse(result: Observable<HttpResponse<IAluno>>): void {
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

  protected updateForm(aluno: IAluno): void {
    this.editForm.patchValue({
      id: aluno.id,
      nome: aluno.nome,
      email: aluno.email,
      dataNascimento: aluno.dataNascimento,
      rm: aluno.rm,
      foto: aluno.foto,
      fotoContentType: aluno.fotoContentType,
      status: aluno.status,
      turmas: aluno.turmas,
    });

    this.turmasSharedCollection = this.turmaService.addTurmaToCollectionIfMissing(this.turmasSharedCollection, ...(aluno.turmas ?? []));
  }

  protected loadRelationshipsOptions(): void {
    this.turmaService
      .query()
      .pipe(map((res: HttpResponse<ITurma[]>) => res.body ?? []))
      .pipe(
        map((turmas: ITurma[]) => this.turmaService.addTurmaToCollectionIfMissing(turmas, ...(this.editForm.get('turmas')!.value ?? [])))
      )
      .subscribe((turmas: ITurma[]) => (this.turmasSharedCollection = turmas));
  }

  protected createFromForm(): IAluno {
    return {
      ...new Aluno(),
      id: this.editForm.get(['id'])!.value,
      nome: this.editForm.get(['nome'])!.value,
      email: this.editForm.get(['email'])!.value,
      dataNascimento: this.editForm.get(['dataNascimento'])!.value,
      rm: this.editForm.get(['rm'])!.value,
      fotoContentType: this.editForm.get(['fotoContentType'])!.value,
      foto: this.editForm.get(['foto'])!.value,
      status: this.editForm.get(['status'])!.value,
      turmas: this.editForm.get(['turmas'])!.value,
    };
  }
}
