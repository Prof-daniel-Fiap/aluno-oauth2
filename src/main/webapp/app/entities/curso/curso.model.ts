import * as dayjs from 'dayjs';
import { ITurma } from 'app/entities/turma/turma.model';
import { TipoCurso } from 'app/entities/enumerations/tipo-curso.model';

export interface ICurso {
  id?: number;
  nomeCurso?: string | null;
  dataCriacao?: dayjs.Dayjs | null;
  descricao?: string | null;
  logoCursoContentType?: string | null;
  logoCurso?: string | null;
  tipo?: TipoCurso | null;
  turma?: ITurma | null;
}

export class Curso implements ICurso {
  constructor(
    public id?: number,
    public nomeCurso?: string | null,
    public dataCriacao?: dayjs.Dayjs | null,
    public descricao?: string | null,
    public logoCursoContentType?: string | null,
    public logoCurso?: string | null,
    public tipo?: TipoCurso | null,
    public turma?: ITurma | null
  ) {}
}

export function getCursoIdentifier(curso: ICurso): number | undefined {
  return curso.id;
}
