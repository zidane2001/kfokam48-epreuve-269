import React from 'react';
import { Link } from 'react-router-dom';
import {
  ArrowRight,
  ClipboardCheck,
  FileUp,
  KeyRound,
  LayoutList,
  Presentation,
  Radio,
  UserRound,
  Users } from
'lucide-react';
import { ButtonLink } from '../components/ButtonLink';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '../components/ui/Card';
import { Skeleton } from '../components/ui/Skeleton';
import { ErrorState } from '../components/feedback/ErrorState';
import { useApiQuery } from '../hooks/useApiQuery';
import { useStudentIdentity } from '../hooks/useStudentIdentity';
import { api } from '../utils/api';
import { formatHeure } from '../utils/format';

const etapes = [
{ icon: Presentation, label: 'Ouverture', detail: 'Le formateur obtient un code valable 15 min' },
{ icon: KeyRound, label: 'Présence', detail: 'L’étudiant saisit le code (5 essais)' },
{ icon: FileUp, label: 'Dépôt', detail: 'Lien de l’exercice, jusqu’à la clôture' },
{ icon: ClipboardCheck, label: 'Relecture', detail: 'Un camarade présent, tiré au sort' },
{ icon: Users, label: 'Suivi', detail: 'Moyennes et relectures en attente' }];


export function Home() {
  const { identite } = useStudentIdentity();
  const sessionsQ = useApiQuery(() => api.listerSessions(), []);
  const ouvertes = sessionsQ.data?.filter((s) => s.statut === 'OUVERTE') ?? [];

  const espaces = [
  {
    to: '/formateur',
    icon: Presentation,
    title: 'Espace formateur',
    description: 'Ouvrir une session, projeter le code, ajouter une présence et clôturer.',
    cta: 'Gérer les sessions'
  },
  {
    to: '/etudiant',
    icon: UserRound,
    title: 'Espace étudiant',
    description: 'Présence, dépôt d’exercice, relecture d’un camarade et résultats.',
    cta: identite ? 'Reprendre mon espace' : 'Choisir mon nom'
  },
  {
    to: '/suivi',
    icon: LayoutList,
    title: 'Tableau de suivi',
    description: 'Présences, exercices, moyennes et relectures en attente par étudiant.',
    cta: 'Consulter le tableau'
  }];


  return (
    <div className="space-y-10">
      <section className="space-y-3 pt-2">
        <p className="text-sm font-medium text-muted-foreground">Direction de la formation KFOKAM48</p>
        <h1 className="max-w-2xl text-3xl font-semibold tracking-tight sm:text-4xl">
          Sessions, présences et relectures entre pairs
        </h1>
        <p className="max-w-2xl text-muted-foreground">
          Choisissez votre espace. Des données de démonstration sont chargées ; le bouton ↺ en haut à droite les
          réinitialise.
        </p>
      </section>

      <section aria-label="Espaces fonctionnels" className="grid gap-4 md:grid-cols-3">
        {espaces.map(({ to, icon: Icon, title, description, cta }) =>
        <Link
          key={to}
          to={to}
          className="group rounded-xl focus-visible:outline-none focus-visible:ring-[3px] focus-visible:ring-ring">
          
            <Card className="h-full transition-colors group-hover:border-primary">
              <CardHeader className="h-full">
                <span className="mb-2 flex size-10 items-center justify-center rounded-lg bg-secondary">
                  <Icon className="size-5" aria-hidden="true" />
                </span>
                <CardTitle>{title}</CardTitle>
                <CardDescription>{description}</CardDescription>
                <span className="mt-auto inline-flex items-center gap-1 pt-3 text-sm font-medium">
                  {cta}
                  <ArrowRight className="size-4 transition-transform group-hover:translate-x-0.5" aria-hidden="true" />
                </span>
              </CardHeader>
            </Card>
          </Link>
        )}
      </section>

      <section aria-labelledby="live-title" className="space-y-3">
        <h2 id="live-title" className="flex items-center gap-2 text-sm font-medium text-muted-foreground">
          <Radio className="size-4 text-emerald-600" aria-hidden="true" /> En ce moment
        </h2>
        {sessionsQ.error ?
        <ErrorState error={sessionsQ.error} onRetry={sessionsQ.reload} /> :
        !sessionsQ.data ?
        <Skeleton className="h-20 w-full rounded-xl" /> :
        ouvertes.length === 0 ?
        <Card size="sm">
            <CardContent className="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
              <p className="text-sm text-muted-foreground">Aucune session ouverte actuellement.</p>
              <ButtonLink to="/formateur" variant="default" size="sm">
                Ouvrir une session
              </ButtonLink>
            </CardContent>
          </Card> :

        <ul className="grid gap-3 md:grid-cols-2">
            {ouvertes.map((s) =>
          <li key={s.id}>
                <Card size="sm">
                  <CardContent className="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
                    <div className="min-w-0">
                      <p className="truncate font-medium">{s.titre}</p>
                      <p className="text-sm text-muted-foreground">
                        {s.promotionNom.replace('KFOKAM48 · ', '')} · ouverte à {formatHeure(s.ouvertureAt)} ·{' '}
                        {s.nbPresents}/{s.nbEtudiants} présents
                      </p>
                    </div>
                    <div className="flex gap-2">
                      <ButtonLink to={`/formateur?session=${s.id}`} size="sm">
                        Gérer
                      </ButtonLink>
                      <ButtonLink to={`/suivi?promotion=${s.promotionId}&session=${s.id}`} size="sm" variant="ghost">
                        Suivi
                      </ButtonLink>
                    </div>
                  </CardContent>
                </Card>
              </li>
          )}
          </ul>
        }
      </section>

      <section aria-labelledby="cycle-title" className="space-y-3">
        <h2 id="cycle-title" className="text-sm font-medium text-muted-foreground">
          Cycle d’une session
        </h2>
        <Card>
          <CardContent>
            <ol className="grid gap-5 sm:grid-cols-5 sm:gap-4">
              {etapes.map(({ icon: Icon, label, detail }, i) =>
              <li key={label} className="relative flex items-start gap-3 sm:flex-col sm:gap-2">
                  <span className="flex size-8 shrink-0 items-center justify-center rounded-full bg-primary text-xs font-semibold text-primary-foreground">
                    {i + 1}
                  </span>
                  {i < etapes.length - 1 &&
                <span className="absolute left-10 right-0 top-4 hidden h-px bg-border sm:block" aria-hidden="true" />
                }
                  <div>
                    <p className="flex items-center gap-1.5 text-sm font-medium">
                      <Icon className="size-3.5 text-muted-foreground" aria-hidden="true" />
                      {label}
                    </p>
                    <p className="text-xs text-muted-foreground">{detail}</p>
                  </div>
                </li>
              )}
            </ol>
          </CardContent>
        </Card>
      </section>
    </div>);

}