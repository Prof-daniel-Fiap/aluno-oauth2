import { NgModule } from '@angular/core';
import { RouterModule } from '@angular/router';

@NgModule({
  imports: [
    RouterModule.forChild([
      {
        path: 'aluno',
        data: { pageTitle: 'Alunos' },
        loadChildren: () => import('./aluno/aluno.module').then(m => m.AlunoModule),
      },
      {
        path: 'curso',
        data: { pageTitle: 'Cursos' },
        loadChildren: () => import('./curso/curso.module').then(m => m.CursoModule),
      },
      {
        path: 'turma',
        data: { pageTitle: 'Turmas' },
        loadChildren: () => import('./turma/turma.module').then(m => m.TurmaModule),
      },
      /* jhipster-needle-add-entity-route - JHipster will add entity modules routes here */
    ]),
  ],
})
export class EntityRoutingModule {}
