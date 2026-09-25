import React, { useEffect } from 'react';
import { useSearchParams } from 'react-router-dom';
import { MousePointerClick } from 'lucide-react';
import { PageHeader } from '../components/PageHeader';
import { EmptyState } from '../components/feedback/EmptyState';
import { OpenSessionForm } from '../components/formateur/OpenSessionForm';
import { SessionDetail } from '../components/formateur/SessionDetail';
import { SessionList } from '../components/formateur/SessionList';
import { useApiQuery } from '../hooks/useApiQuery';
import { api } from '../utils/api';

export function Formateur() {
  const [params, setParams] = useSearchParams();
  const selectedId = params.get('session');
  const sessionsQ = useApiQuery(() => api.listerSessions(), []);
  const promotionsQ = useApiQuery(() => api.listerPromotions(), []);

  const select = (id: string) => setParams({ session: id }, { replace: true });

  useEffect(() => {
    const sessions = sessionsQ.data;
    if (!sessions || sessions.length === 0) return;
    if (!selectedId || !sessions.some((s) => s.id === selectedId)) {
      const ouverte = sessions.find((s) => s.statut === 'OUVERTE');
      select((ouverte ?? sessions[0]).id);
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [sessionsQ.data, selectedId]);

  return (
    <div className="space-y-6">
      <PageHeader
        title="Espace formateur"
        description="Ouvrez une session, partagez le code de présence, ajoutez les présences manquantes et clôturez la session en fin de cours." />
      
      <div className="grid items-start gap-6 lg:grid-cols-[minmax(0,340px)_minmax(0,1fr)]">
        <div className="space-y-6">
          <OpenSessionForm
            promotions={promotionsQ.data}
            promotionsLoading={promotionsQ.loading}
            sessions={sessionsQ.data}
            onCreated={(s) => select(s.id)}
            onSelectSession={select} />
          
          <SessionList query={sessionsQ} selectedId={selectedId} onSelect={select} />
        </div>
        {selectedId ?
        <SessionDetail key={selectedId} sessionId={selectedId} /> :

        <EmptyState icon={MousePointerClick} title="Sélectionnez une session" description="Ou ouvrez-en une nouvelle." />
        }
      </div>
    </div>);

}