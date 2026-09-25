import React, { useEffect, useState } from 'react';
import { Plus, UserPlus, Users } from 'lucide-react';
import { toast } from 'sonner';
import { PageHeader } from '../components/PageHeader';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '../components/ui/Card';
import { Input } from '../components/ui/Input';
import { Label } from '../components/ui/Label';
import { Button } from '../components/ui/Button';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '../components/ui/Select';
import { Spinner } from '../components/ui/Spinner';
import { ErrorState } from '../components/feedback/ErrorState';
import { LoadingRows } from '../components/feedback/LoadingRows';
import { useApiQuery } from '../hooks/useApiQuery';
import { api, toApiError } from '../utils/api';

/** Ecran de gestion (evolution PO, closes #29) : promotions et etudiants, formateur only. */
export function Gestion() {
  const promotionsQ = useApiQuery(() => api.listerPromotions(), []);
  const [promotionId, setPromotionId] = useState('');
  const etudiantsQ = useApiQuery(
    () => api.listerEtudiants(promotionId), [promotionId], { enabled: Boolean(promotionId) });

  useEffect(() => {
    if (!promotionId && promotionsQ.data && promotionsQ.data.length > 0) setPromotionId(promotionsQ.data[0].id);
  }, [promotionsQ.data, promotionId]);

  const [nomPromotion, setNomPromotion] = useState('');
  const [creePromotion, setCreePromotion] = useState(false);
  const [prenom, setPrenom] = useState('');
  const [nom, setNom] = useState('');
  const [creeEtudiant, setCreeEtudiant] = useState(false);
  const [dernierCompte, setDernierCompte] = useState<{ login: string } | null>(null);

  async function creerPromotion(e: React.FormEvent) {
    e.preventDefault();
    setCreePromotion(true);
    try {
      const p = await (api as any).creerPromotion(nomPromotion.trim());
      toast.success(`Promotion « ${p.nom} » créée`);
      setNomPromotion('');
      setPromotionId(String(p.id));
      promotionsQ.reload();
    } catch (err) {
      toast.error(toApiError(err).message);
    } finally {
      setCreePromotion(false);
    }
  }

  async function inscrire(e: React.FormEvent) {
    e.preventDefault();
    setCreeEtudiant(true);
    setDernierCompte(null);
    try {
      const res = await (api as any).inscrireEtudiant(promotionId, prenom.trim(), nom.trim());
      toast.success(`${res.prenom} ${res.nom} inscrit · compte « ${res.login} »`);
      setDernierCompte({ login: res.login });
      setPrenom('');
      setNom('');
      etudiantsQ.reload();
    } catch (err) {
      toast.error(toApiError(err).message);
    } finally {
      setCreeEtudiant(false);
    }
  }

  return (
    <div className="space-y-6">
      <PageHeader
        title="Gestion des promotions"
        description="Créez vos promotions et inscrivez vos étudiants. Chaque étudiant reçoit automatiquement ses identifiants de connexion." />

      <div className="grid items-start gap-6 lg:grid-cols-2">
        <Card>
          <CardHeader>
            <CardTitle className="flex items-center gap-2"><Plus className="size-4" /> Nouvelle promotion</CardTitle>
            <CardDescription>Le nom apparaîtra dans tous les écrans de sélection.</CardDescription>
          </CardHeader>
          <CardContent>
            <form onSubmit={creerPromotion} className="space-y-3" noValidate>
              <div className="space-y-2">
                <Label htmlFor="gestion-nom-promo">Nom de la promotion</Label>
                <Input id="gestion-nom-promo" value={nomPromotion} onChange={(e) => setNomPromotion(e.target.value)}
                  placeholder="Ex. KFOKAM49 — Promotion 2027" maxLength={120} />
              </div>
              <Button type="submit" className="w-full" disabled={creePromotion || !nomPromotion.trim()}>
                {creePromotion ? <Spinner /> : <Plus />} Créer la promotion
              </Button>
            </form>
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle className="flex items-center gap-2"><UserPlus className="size-4" /> Inscrire un étudiant</CardTitle>
            <CardDescription>Compte créé automatiquement : login prenom.nom, mot de passe initial identique.</CardDescription>
          </CardHeader>
          <CardContent className="space-y-4">
            <div className="space-y-2">
              <Label htmlFor="gestion-promo">Promotion</Label>
              <Select value={promotionId} onValueChange={setPromotionId} disabled={!promotionsQ.data}>
                <SelectTrigger id="gestion-promo" className="w-full">
                  <SelectValue placeholder={promotionsQ.loading ? 'Chargement…' : 'Choisir une promotion'} />
                </SelectTrigger>
                <SelectContent>
                  {(promotionsQ.data ?? []).map((p) =>
                    <SelectItem key={p.id} value={p.id}>{p.nom}</SelectItem>)}
                </SelectContent>
              </Select>
            </div>
            <form onSubmit={inscrire} className="space-y-3" noValidate>
              <div className="grid grid-cols-2 gap-3">
                <div className="space-y-2">
                  <Label htmlFor="gestion-prenom">Prénom</Label>
                  <Input id="gestion-prenom" value={prenom} onChange={(e) => setPrenom(e.target.value)} maxLength={80} />
                </div>
                <div className="space-y-2">
                  <Label htmlFor="gestion-nom">Nom</Label>
                  <Input id="gestion-nom" value={nom} onChange={(e) => setNom(e.target.value)} maxLength={120} />
                </div>
              </div>
              <Button type="submit" className="w-full" disabled={creeEtudiant || !promotionId || !prenom.trim() || !nom.trim()}>
                {creeEtudiant ? <Spinner /> : <UserPlus />} Inscrire et créer le compte
              </Button>
            </form>
            {dernierCompte &&
              <p className="rounded-lg bg-muted p-3 text-sm">
                Compte créé : <span className="font-mono font-medium">{dernierCompte.login}</span> — mot de passe
                initial identique au login, à changer à la première connexion.
              </p>
            }
          </CardContent>
        </Card>
      </div>

      <Card>
        <CardHeader>
          <CardTitle className="flex items-center gap-2"><Users className="size-4" /> Étudiants de la promotion</CardTitle>
        </CardHeader>
        <CardContent>
          {etudiantsQ.error ? <ErrorState error={etudiantsQ.error} onRetry={etudiantsQ.reload} /> :
            !etudiantsQ.data ? <LoadingRows rows={4} className="h-10" /> :
              etudiantsQ.data.length === 0 ? <p className="text-sm text-muted-foreground">Aucun étudiant inscrit.</p> :
                <ul className="divide-y divide-border overflow-hidden rounded-lg border border-border">
                  {etudiantsQ.data.map((e) =>
                    <li key={e.id} className="flex items-center justify-between px-3 py-2 text-sm">
                      <span className="font-medium">{e.prenom} {e.nom}</span>
                      <span className="font-mono text-muted-foreground">{(e.prenom + '.' + e.nom).toLowerCase()}</span>
                    </li>)}
                </ul>}
        </CardContent>
      </Card>
    </div>);

}
