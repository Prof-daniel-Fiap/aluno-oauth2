import * as dayjs from 'dayjs';
import { ICurso } from 'app/entities/curso/curso.model';
import { IAluno } from 'app/entities/aluno/aluno.model';

export interface ITurma {
  id?: number;
  codigoTurma?: string | null;
  dataCriacao?: dayjs.Dayjs | null;
  unidade?: string | null;
  observacoes?: string | null;
  cursos?: ICurso[] | null;
  alunos?: IAluno[] | null;
}

export class Turma implements ITurma {
  constructor(
    public id?: number,
    public codigoTurma?: string | null,
    public dataCriacao?: dayjs.Dayjs | null,
    public unidade?: string | null,
    public observacoes?: string | null,
    public cursos?: ICurso[] | null,
    public alunos?: IAluno[] | null
  ) {}
}

export function getTurmaIdentifier(turma: ITurma): number | undefined {
  return turma.id;
}
