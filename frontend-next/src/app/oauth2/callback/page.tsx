"use client";

import { useEffect, Suspense } from "react";
import { useRouter } from "next/navigation";
import { setLoggedIn } from "@/lib/token";
import { useAuth } from "@/providers/AuthProvider";
import React from "react";

function CallbackContent() {
  const router = useRouter();
  const { checkAuth } = useAuth();
  const [isProcessing, setIsProcessing] = React.useState(false);

  useEffect(() => {
    if (isProcessing) return;

    // With HTTP-only cookies, the backend has already set the tokens.
    // We just need to flag ourselves as logged in and verify the session.
    setIsProcessing(true);
    console.log("SSO Callback: Setting login flag and verifying session...");
    setLoggedIn(true);

    checkAuth()
      .then(() => {
        router.push("/dashboard");
      })
      .catch((err) => {
        console.error("Failed to verify SSO session:", err);
        router.push("/login?error=verification_failed");
      });
  }, [router, checkAuth, isProcessing]);

  return (
    <div className="min-h-screen flex flex-col items-center justify-center bg-slate-950 text-white">
      <div className="space-y-4 text-center">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-500 mx-auto"></div>
        <h2 className="text-xl font-semibold">Completing login...</h2>
        <p className="text-slate-400">
          Please wait while we set up your session.
        </p>
      </div>
    </div>
  );
}

export default function OAuth2Callback() {
  return (
    <Suspense
      fallback={
        <div className="min-h-screen flex items-center justify-center bg-slate-950">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-500"></div>
        </div>
      }
    >
      <CallbackContent />
    </Suspense>
  );
}
