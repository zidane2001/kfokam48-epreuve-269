"use client";

import dynamic from "next/dynamic";

// Le design (frontendModel) est un SPA react-router ; on le monte tel quel
// pour une fidelite parfaite au design valide par le PO. 100 % client.
// Route catch-all optionnelle : toutes les URLs (/formateur, /etudiant, /suivi...)
// servent la SPA, sinon les liens directs renvoient 404.
const App = dynamic(() => import("@/AppClient"), {
  ssr: false,
  loading: () => <div style={{ padding: 40 }}>Chargement…</div>,
});

export default function Page() {
  return <App />;
}
