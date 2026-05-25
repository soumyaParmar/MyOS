"use client";

import React, { useState, useEffect, useCallback } from "react";
import ProtectedRoute from "@/components/auth/ProtectedRoute";
import { Sidebar } from "@/components/layout/Sidebar";
import { IngestForm } from "@/components/knowledge/IngestForm";
import { SnippetCard } from "@/components/knowledge/SnippetCard";
import { KnowledgeSnippet, knowledgeService } from "@/services/knowledge.service";
import { Input } from "@/components/ui/input";
import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";
import { Skeleton } from "@/components/ui/skeleton";
import { 
  Brain, 
  Search, 
  RefreshCw, 
  Database,
  Sparkles,
  HelpCircle,
  AlertCircle
} from "lucide-react";
import { toast } from "sonner";

// High-fidelity fallback mock data in case the backend database isn't fully ready yet
const FALLBACK_MOCK_KNOWLEDGE: KnowledgeSnippet[] = [
  {
    id: "mock-1",
    content: "Planning a vacation to Kyoto, Japan in mid-October. Hotel reservations confirmed at Kyoto Grand Hotel. Booking ref: JP-88274.",
    metadata: {
      category: "personal",
      source: "note",
      tags: ["travel", "japan", "kyoto"],
      user_id: "mock-user"
    }
  },
  {
    id: "mock-2",
    content: "Spring Security Filter Chain processes requests in order: WebAsyncManagerIntegrationFilter -> SecurityContextHolderFilter -> HeaderWriterFilter -> CorsFilter -> CsrfFilter -> LogoutFilter -> UsernamePasswordAuthenticationFilter -> AuthorizationFilter.",
    metadata: {
      category: "learning",
      source: "document",
      tags: ["spring-boot", "java", "security"],
      user_id: "mock-user"
    }
  },
  {
    id: "mock-3",
    content: "Flyway database migration scripts must follow a strict alphanumeric naming convention: e.g. V1__init_schema.sql, V2__add_users.sql. Once executed, their checksums are locked in flyway_schema_history.",
    metadata: {
      category: "work",
      source: "document",
      tags: ["database", "sql", "flyway"],
      user_id: "mock-user"
    }
  },
  {
    id: "mock-4",
    content: "Monthly grocery budget threshold is strictly capped at $400. Alerts will be fired via Apache Kafka event bus if spending reaches 80% ($320).",
    metadata: {
      category: "finance",
      source: "note",
      tags: ["finance", "budget", "kafka"],
      user_id: "mock-user"
    }
  },
  {
    id: "mock-5",
    content: "Morning workout habits: Drink 500ml water, perform 15-minute dynamic stretches, and log habit streaks in Notion before 9 AM.",
    metadata: {
      category: "health",
      source: "note",
      tags: ["habit", "health", "notion"],
      user_id: "mock-user"
    }
  }
];

export default function KnowledgePage() {
  const [snippets, setSnippets] = useState<KnowledgeSnippet[]>([]);
  const [searchQuery, setSearchQuery] = useState("");
  const [isLoading, setIsLoading] = useState(true);
  const [isSearching, setIsSearching] = useState(false);
  const [isUsingFallback, setIsUsingFallback] = useState(false);

  // Load and refresh knowledge snippets
  const loadKnowledge = useCallback(async (query: string = "") => {
    setIsSearching(true);
    try {
      const data = await knowledgeService.search(query);
      setSnippets(data);
      setIsUsingFallback(false);
    } catch (error) {
      console.warn("Backend API not connected or offline, running fallback mode.", error);
      setIsUsingFallback(true);
      
      // Perform an elegant client-side keyword search overlay for mock data
      if (query.trim()) {
        const filtered = FALLBACK_MOCK_KNOWLEDGE.filter(item => 
          item.content.toLowerCase().includes(query.toLowerCase()) ||
          (Array.isArray(item.metadata.tags) && (item.metadata.tags as string[]).some((t: string) => t.toLowerCase().includes(query.toLowerCase()))) ||
          item.metadata.category?.toLowerCase().includes(query.toLowerCase()) ||
          item.metadata.source?.toLowerCase().includes(query.toLowerCase())
        ).map(item => {
          // Mock a dynamic similarity score for visual presentation
          const score = item.content.toLowerCase().includes(query.toLowerCase()) ? 0.92 : 0.65;
          return { ...item, similarityScore: score };
        });
        setSnippets(filtered);
      } else {
        setSnippets(FALLBACK_MOCK_KNOWLEDGE);
      }
    } finally {
      setIsLoading(false);
      setIsSearching(false);
    }
  }, []);

  useEffect(() => {
    loadKnowledge();
  }, [loadKnowledge]);

  const handleSearchSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    loadKnowledge(searchQuery);
  };

  const handleReload = () => {
    loadKnowledge(searchQuery);
  };

  return (
    <ProtectedRoute>
      <div className="min-h-screen bg-slate-950 text-slate-100 flex">
        <Sidebar />

        {/* Main Workspace Scroll Wrapper */}
        <main className="flex-1 p-6 md:p-10 overflow-auto">
          {/* Header Title Grid */}
          <header className="flex flex-col md:flex-row justify-between items-start md:items-center gap-4 mb-8">
            <div>
              <h2 className="text-3xl font-bold tracking-tight flex items-center gap-2.5">
                <Brain className="w-8 h-8 text-blue-500 animate-pulse" />
                Knowledge Base
              </h2>
              <p className="text-slate-400 mt-1">
                Manage notes, summaries, and documents synced to your personal AI vector memory.
              </p>
            </div>
            
            <div className="flex items-center gap-3">
              {isUsingFallback && (
                <span className="inline-flex items-center gap-1.5 px-3 py-1 bg-amber-500/10 text-amber-400 border border-amber-500/20 text-xs rounded-full" title="No connection detected to Spring Boot. Utilizing local localStorage fallback.">
                  <AlertCircle className="w-3.5 h-3.5" />
                  Offline Mock Mode
                </span>
              )}
              <Button
                variant="outline"
                size="icon"
                onClick={handleReload}
                disabled={isLoading || isSearching}
                className="border-slate-800 bg-slate-900/50 hover:bg-slate-800 text-slate-400 hover:text-slate-100"
              >
                <RefreshCw className={`w-4 h-4 ${isSearching ? "animate-spin" : ""}`} />
              </Button>
            </div>
          </header>

          <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
            {/* Column Left: Ingestion Controller */}
            <div className="lg:col-span-1 space-y-6">
              <IngestForm onSuccess={handleReload} />

              {/* RAG Context Panel Info Card */}
              <Card className="bg-slate-900/20 border-slate-800/60 text-slate-400 text-xs leading-relaxed p-5">
                <h4 className="text-slate-300 font-semibold mb-2 flex items-center gap-1.5">
                  <Database className="w-3.5 h-3.5 text-purple-400" />
                  Semantic Search RAG pipeline
                </h4>
                <p className="mb-2">
                  When you add snippets to the memory, Spring AI embeds them using Ollama's <code className="bg-slate-950 px-1 py-0.5 rounded text-blue-400 text-[10px]">nomic-embed-text</code> or OpenAI embeddings.
                </p>
                <p>
                  Searching matches not just the words you typed, but the <strong>semantic meaning</strong> of your thoughts, supplying the perfect long-term context to your active Spring Boot AI agents.
                </p>
              </Card>
            </div>

            {/* Column Right: Search and Ingested Cards */}
            <div className="lg:col-span-2 space-y-6">
              {/* Search Bar Grid */}
              <form onSubmit={handleSearchSubmit} className="flex gap-2">
                <div className="relative flex-1">
                  <Search className="w-4 h-4 text-slate-500 absolute left-3 top-1/2 -translate-y-1/2" />
                  <Input
                    type="text"
                    placeholder="Search vector database semantically... (e.g. travel itinerary, db mapping)"
                    value={searchQuery}
                    onChange={(e) => setSearchQuery(e.target.value)}
                    className="pl-10 bg-slate-900/50 border-slate-800 text-slate-100 placeholder:text-slate-500 focus:border-blue-500/50 h-11"
                  />
                </div>
                <Button 
                  type="submit"
                  disabled={isSearching}
                  className="bg-blue-600 hover:bg-blue-500 text-white font-medium px-5 h-11"
                >
                  {isSearching ? <RefreshCw className="w-4 h-4 animate-spin" /> : "Query DB"}
                </Button>
              </form>

              {/* Ingested Snippets Grid */}
              <div>
                <div className="flex justify-between items-center mb-4">
                  <h3 className="text-lg font-semibold text-slate-300 flex items-center gap-2">
                    Memory Snippets
                    <span className="text-xs font-normal text-slate-500 bg-slate-900 px-2 py-0.5 rounded-full">
                      {snippets.length} loaded
                    </span>
                  </h3>
                  {searchQuery && (
                    <Button 
                      variant="link" 
                      onClick={() => { setSearchQuery(""); loadKnowledge(""); }} 
                      className="text-xs text-blue-400 hover:text-blue-300 p-0"
                    >
                      Clear search filters
                    </Button>
                  )}
                </div>

                {isLoading ? (
                  /* Skeletal Loading Grid */
                  <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                    {[1, 2, 3, 4].map(idx => (
                      <Card key={idx} className="bg-slate-900/30 border-slate-800 p-5 space-y-3">
                        <div className="flex justify-between">
                          <Skeleton className="h-5 w-20 bg-slate-800" />
                          <Skeleton className="h-5 w-24 bg-slate-800" />
                        </div>
                        <Skeleton className="h-16 w-full bg-slate-800" />
                        <Skeleton className="h-4 w-1/3 bg-slate-800" />
                      </Card>
                    ))}
                  </div>
                ) : snippets.length > 0 ? (
                  /* Snippet Cards */
                  <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                    {snippets.map(snippet => (
                      <SnippetCard 
                        key={snippet.id} 
                        snippet={snippet} 
                        onDeleteSuccess={handleReload}
                      />
                    ))}
                  </div>
                ) : (
                  /* Empty state */
                  <Card className="bg-slate-900/20 border-slate-800/60 p-12 text-center">
                    <CardContent className="flex flex-col items-center justify-center space-y-4 pt-6">
                      <div className="w-12 h-12 rounded-full bg-slate-950 flex items-center justify-center border border-slate-800">
                        <Sparkles className="w-6 h-6 text-slate-600" />
                      </div>
                      <div>
                        <h4 className="text-slate-300 font-semibold">No knowledge snippets found</h4>
                        <p className="text-slate-500 text-sm mt-1 max-w-sm">
                          {searchQuery 
                            ? "Your similarity search did not match any memories. Try searching for different keywords or clear query filters."
                            : "Your AI brain currently has no ingested memories. Type some notes on the left panel to populate vector store."}
                        </p>
                      </div>
                    </CardContent>
                  </Card>
                )}
              </div>
            </div>
          </div>
        </main>
      </div>
    </ProtectedRoute>
  );
}
