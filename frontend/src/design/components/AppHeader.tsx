import React, { useState } from 'react';
import { Link, NavLink, useNavigate } from 'react-router-dom';
import { GraduationCap, LayoutList, LogIn, LogOut, Presentation, RotateCcw, Settings2, UserRound } from 'lucide-react';
import { toast } from 'sonner';
import { Button } from './ui/Button';
import { cn } from '../utils/cn';
import { api, toApiError } from '../utils/api';
import { useAuthSession } from '../auth/authStore';

const links = [
{ to: '/formateur', label: 'Formateur', icon: Presentation },
{ to: '/etudiant', label: 'Étudiant', icon: UserRound },
{ to: '/suivi', label: 'Suivi', icon: LayoutList },
{ to: '/gestion', label: 'Gestion', icon: Settings2 }];


export function AppHeader() {
  const [resetting, setResetting] = useState(false);
  const { session, deconnexion } = useAuthSession();
  const navigate = useNavigate();

  async function handleReset() {
    setResetting(true);
    try {
      await api.reinitialiserDemo();
      toast.success('Données de démonstration réinitialisées');
    } catch (e) {
      toast.error(toApiError(e).message);
    } finally {
      setResetting(false);
    }
  }

  return (
    <header className="sticky top-0 z-30 border-b border-border bg-background">
      <div className="mx-auto flex h-14 max-w-7xl items-center gap-3 px-4 sm:px-6">
        <Link to="/" className="flex shrink-0 items-center gap-2 rounded-md focus-visible:outline-none focus-visible:ring-[3px] focus-visible:ring-ring">
          <span className="flex size-8 items-center justify-center rounded-lg bg-primary text-primary-foreground">
            <GraduationCap className="size-4" aria-hidden="true" />
          </span>
          <span className="hidden text-sm font-semibold sm:inline">KFOKAM48</span>
          <span className="hidden text-sm text-muted-foreground lg:inline">· Sessions, présences & relectures</span>
        </Link>
        <nav aria-label="Espaces fonctionnels" className="ml-auto flex items-center gap-1">
          {links.map(({ to, label, icon: Icon }) =>
          <NavLink
            key={to}
            to={to}
            className={({ isActive }) =>
            cn(
              'inline-flex h-8 items-center gap-1.5 rounded-md px-2.5 text-sm font-medium transition-colors focus-visible:outline-none focus-visible:ring-[3px] focus-visible:ring-ring',
              isActive ? 'bg-secondary text-foreground' : 'text-muted-foreground hover:bg-secondary hover:text-foreground'
            )
            }>
            
              <Icon className="hidden size-4 sm:block" aria-hidden="true" />
              {label}
            </NavLink>
          )}
        </nav>
        {session ?
        <Button
          variant="outline"
          size="sm"
          onClick={() => {
            deconnexion();
            navigate('/');
          }}
          title={`Déconnexion (${session.login})`}>
          
          <LogOut />
          <span className="hidden sm:inline">{session.login}</span>
        </Button> :

        <Button
          variant="outline"
          size="sm"
          onClick={() => navigate('/connexion')}>
          
          <LogIn />
          <span className="hidden sm:inline">Connexion</span>
        </Button>
        }
        <Button
          variant="ghost"
          size="icon-sm"
          onClick={handleReset}
          disabled={resetting}
          aria-label="Réinitialiser les données de démonstration"
          title="Réinitialiser les données de démonstration">
          
          <RotateCcw className={cn('size-4', resetting && 'animate-spin')} />
        </Button>
      </div>
    </header>);

}