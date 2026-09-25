import React from 'react';
import { BrowserRouter, Navigate, Route, Routes } from 'react-router-dom';
import { AppHeader } from './design/components/AppHeader';
import { Toaster } from './design/components/ui/Sonner';
import { Etudiant } from './design/pages/Etudiant';
import { Formateur } from './design/pages/Formateur';
import { Home } from './design/pages/Home';
import { Suivi } from './design/pages/Suivi';

export function App() {
  return (
    <BrowserRouter>
      <div className="min-h-screen w-full bg-muted text-foreground">
        <AppHeader />
        <main className="mx-auto w-full max-w-7xl px-4 py-6 sm:px-6 lg:py-8">
          <Routes>
            <Route path="/" element={<Home />} />
            <Route path="/formateur" element={<Formateur />} />
            <Route path="/etudiant" element={<Etudiant />} />
            <Route path="/suivi" element={<Suivi />} />
            <Route path="*" element={<Navigate to="/" replace />} />
          </Routes>
        </main>
        <Toaster position="top-center" richColors closeButton />
      </div>
    </BrowserRouter>);

}export default App;
