"use client";

import React, { useState } from "react";
import { toast } from "sonner";
import { Card, CardContent } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { 
  Trash2, 
  Tag, 
  Calendar, 
  Activity, 
  Award,
  FileText,
  Mail,
  BookOpen,
  DollarSign,
  Heart,
  Globe,
  StickyNote
} from "lucide-react";
import { KnowledgeSnippet, knowledgeService } from "@/services/knowledge.service";

interface SnippetCardProps {
  snippet: KnowledgeSnippet;
  onDeleteSuccess: () => void;
}

export function SnippetCard({ snippet, onDeleteSuccess }: SnippetCardProps) {
  const [isDeleting, setIsDeleting] = useState(false);

  const handleDelete = async () => {
    if (!window.confirm("Are you sure you want to delete this snippet from vector memory?")) {
      return;
    }

    setIsDeleting(true);
    try {
      await knowledgeService.deleteSnippet(snippet.id);
      toast.success("Snippet deleted successfully!");
      onDeleteSuccess();
    } catch (error) {
      console.error("Deletion failed:", error);
      toast.error("Failed to delete snippet.");
    } finally {
      setIsDeleting(false);
    }
  };

  // Select appropriate icon for Source
  const getSourceIcon = (source?: string) => {
    switch (source?.toLowerCase()) {
      case "email":
        return <Mail className="w-3.5 h-3.5" />;
      case "document":
        return <FileText className="w-3.5 h-3.5" />;
      case "web_page":
        return <Globe className="w-3.5 h-3.5" />;
      case "manual":
      case "note":
      default:
        return <StickyNote className="w-3.5 h-3.5" />;
    }
  };

  // Select appropriate styles for Category badge
  const getCategoryStyles = (category?: string) => {
    switch (category?.toLowerCase()) {
      case "work":
        return "bg-indigo-500/10 text-indigo-400 border-indigo-500/20";
      case "learning":
        return "bg-emerald-500/10 text-emerald-400 border-emerald-500/20";
      case "finance":
        return "bg-amber-500/10 text-amber-400 border-amber-500/20";
      case "health":
        return "bg-rose-500/10 text-rose-400 border-rose-500/20";
      case "personal":
      default:
        return "bg-blue-500/10 text-blue-400 border-blue-500/20";
    }
  };

  // Score Pill rendering for Vector Search similarity ratings
  const renderScorePill = (score?: number) => {
    if (score === undefined || score === null) return null;
    
    // Convert decimal similarity distance score into percentage
    const matchPercent = Math.round(score * 100);
    
    let colorClass = "bg-slate-500/10 text-slate-400 border-slate-500/20";
    if (matchPercent >= 80) {
      colorClass = "bg-green-500/15 text-green-400 border-green-500/30 font-bold";
    } else if (matchPercent >= 50) {
      colorClass = "bg-yellow-500/15 text-yellow-400 border-yellow-500/30";
    }

    return (
      <span className={`inline-flex items-center gap-1.5 px-2 py-0.5 rounded-full border text-xs ${colorClass}`}>
        <Award className="w-3 h-3 animate-pulse" />
        {matchPercent}% Match
      </span>
    );
  };

  return (
    <Card className="bg-slate-900/30 border-slate-800/80 backdrop-blur-sm relative overflow-hidden transition-all duration-300 hover:-translate-y-1 hover:border-slate-700/50 hover:bg-slate-900/50 hover:shadow-lg group">
      {/* Visual Accent Bar */}
      <div className="absolute top-0 left-0 right-0 h-[2px] bg-gradient-to-r from-blue-500/20 via-purple-500/20 to-transparent group-hover:from-blue-500/50 group-hover:via-purple-500/50" />
      
      <CardContent className="p-5 flex flex-col justify-between h-full space-y-4">
        {/* Card Header & Badges */}
        <div className="flex justify-between items-start gap-4">
          <div className="flex flex-wrap gap-2">
            {/* Category Badge */}
            <span className={`px-2.5 py-0.5 rounded-md border text-xs font-semibold capitalize tracking-wide transition-all ${getCategoryStyles(snippet.metadata.category)}`}>
              {snippet.metadata.category || "personal"}
            </span>

            {/* Source Type Badge */}
            <span className="inline-flex items-center gap-1 px-2.5 py-0.5 rounded-md border border-slate-800 bg-slate-950/40 text-slate-400 text-xs uppercase tracking-wider font-semibold">
              {getSourceIcon(snippet.metadata.source)}
              <span className="ml-0.5">{snippet.metadata.source || "note"}</span>
            </span>

            {/* Semantic Score Badge */}
            {renderScorePill(snippet.similarityScore)}
          </div>

          {/* Delete Button */}
          <Button
            size="icon"
            variant="ghost"
            onClick={handleDelete}
            disabled={isDeleting}
            className="w-8 h-8 rounded-lg text-slate-500 hover:text-red-400 hover:bg-red-500/10 opacity-0 group-hover:opacity-100 focus:opacity-100 transition-all duration-200"
            title="Delete from vector memory"
          >
            <Trash2 className="w-4 h-4" />
          </Button>
        </div>

        {/* Content Snippet */}
        <div className="flex-1">
          <p className="text-slate-200 text-sm leading-relaxed whitespace-pre-wrap select-text break-words line-clamp-6 group-hover:line-clamp-none transition-all duration-300">
            {snippet.content}
          </p>
        </div>

        {/* Card Footer: Metadata Tags and ID reference */}
        <div className="pt-3 border-t border-slate-800/40 flex flex-wrap justify-between items-center gap-2">
          {/* Metadata Tags */}
          <div className="flex flex-wrap gap-1.5">
            {Array.isArray(snippet.metadata.tags) && snippet.metadata.tags.length > 0 ? (
              snippet.metadata.tags.map((tag: string, idx: number) => (
                <span
                  key={`${tag}-${idx}`}
                  className="inline-flex items-center gap-0.5 px-2 py-0.5 rounded-md bg-slate-800/40 hover:bg-slate-800/80 text-slate-500 hover:text-slate-300 text-[10px] border border-slate-800/40 transition-colors"
                >
                  <Tag className="w-2.5 h-2.5 text-slate-600" />
                  {tag}
                </span>
              ))
            ) : (
              <span className="text-[10px] text-slate-600 italic">No custom tags</span>
            )}
          </div>

          {/* Short ID Reference for developer audit */}
          <span className="text-[10px] text-slate-600 font-mono" title={`Doc UUID: ${snippet.id}`}>
            ref:{snippet.id.substring(0, 8)}
          </span>
        </div>
      </CardContent>
    </Card>
  );
}
