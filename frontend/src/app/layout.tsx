import type { Metadata } from "next";
import "../globals.css";

export const metadata: Metadata = {
  title: "Presence55 — KFOKAM48",
  description: "Sessions, presences et relectures par les pairs",
};

export default function RootLayout({ children }: { children: React.ReactNode }) {
  return (
    <html lang="fr">
      <body>{children}</body>
    </html>
  );
}
