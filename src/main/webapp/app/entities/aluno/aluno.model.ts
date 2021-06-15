import * as dayjs from 'dayjs';
import { ITurma } from 'app/entities/turma/turma.model';
import { Status } from 'app/entities/enumerations/status.model';

export interface IAluno {
  id?: number;
  nome?: string | null;
  email?: string | null;
  dataNascimento?: dayjs.Dayjs | null;
  rm?: number | null;
  fotoContentType?: string | null;
  foto?: string | null;
  status?: Status | null;
  turmas?: ITurma[] | null;
}

export class Aluno implements IAluno {
  constructor(
    public id?: number,
    public nome?: string | null,
    public email?: string | null,
    public dataNascimento?: dayjs.Dayjs | null,
    public rm?: number | null,
    public fotoContentType?: string | null,
    public foto?: string | null,
    public status?: Status | null,
    public turmas?: ITurma[] | null
  ) {}
}

export function getAlunoIdentifier(aluno: IAluno): number | undefined {
  return aluno.id;
}
