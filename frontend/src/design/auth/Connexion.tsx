import React, { useState } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import { KeyRound, LogIn } from 'lucide-react';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '../components/ui/Card';
import { Input } from '../components/ui/Input';
import { Label } from '../components/ui/Label';
import { Alert, AlertDescription } from '../components/ui/Alert';
import { AlertCircle } from 'lucide-react';
import { Button } from '../components/ui/Button';
import { Spinner } from '../components/ui/Spinner';
import { useAuthSession } from './authStore';
import { toApiError } from '../utils/api';

/** Ecran de connexion (evolution PO) — remplace la selection libre d'identite. */
export function Connexion() {
  const { connexion } = useAuthSession();
  const navigate = useNavigate();
  const location = useLocation();
  const origine = (location.state as { from?: string } | null)?.from ?? '/';

  const [login, setLogin] = useState('');
  const [motDePasse, setMotDePasse] = useState('');
  const [soumission, setSoumission] = useState(false);
  const [erreur, setErreur] = useState<string | null>(null);

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    setSoumission(true);
    setErreur(null);
    try {
      const s = await connexion(login.trim(), motDePasse);
      navigate(s.role === 'FORMATEUR' ? '/formateur' : '/etudiant', { replace: true });
    } catch (err) {
      setErreur(toApiError(err).message);
    } finally {
      setSoumission(false);
    }
  }

  return (
    <div className="mx-auto w-full max-w-md space-y-4">
      <Card>
        <CardHeader>
          <CardTitle className="text-lg">Connexion</CardTitle>
          <CardDescription>
            Identifiez-vous avec vos identifiants. Étudiant : prenom.nom · mot de passe initial prenom.nom.
          </CardDescription>
        </CardHeader>
        <CardContent>
          <form onSubmit={handleSubmit} className="space-y-4" noValidate>
            <div className="space-y-2">
              <Label htmlFor="connexion-login">Identifiant</Label>
              <Input
                id="connexion-login"
                value={login}
                onChange={(e) => setLogin(e.target.value)}
                placeholder="ex. yannick.tchoupo"
                autoComplete="username"
                autoCapitalize="none"
                spellCheck={false}
                aria-invalid={erreur ? true : undefined} />
            </div>
            <div className="space-y-2">
              <Label htmlFor="connexion-mdp">Mot de passe</Label>
              <Input
                id="connexion-mdp"
                type="password"
                value={motDePasse}
                onChange={(e) => setMotDePasse(e.target.value)}
                placeholder="••••••••"
                autoComplete="current-password"
                aria-invalid={erreur ? true : undefined} />
            </div>
            {erreur &&
            <Alert variant="destructive">
                <AlertCircle />
                <AlertDescription>{erreur}</AlertDescription>
              </Alert>
            }
            <Button type="submit" className="w-full" size="lg" disabled={soumission || !login.trim() || !motDePasse}>
              {soumission ? <Spinner /> : <KeyRound />}
              <LogIn className="hidden" />
              Se connecter
            </Button>
          </form>
        </CardContent>
      </Card>
    </div>);

}
