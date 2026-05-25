"use client";

import React, { useState } from "react";
import { toast } from "sonner";
import { Card, CardHeader, CardTitle, CardDescription, CardContent } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Textarea } from "@/components/ui/textarea";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Loader2, Plus, Sparkles } from "lucide-react";
import { knowledgeService } from "@/services/knowledge.service";

interface IngestFormProps {
  onSuccess: () => void;
}

export function IngestForm({ onSuccess }: IngestFormProps) {
  const [content, setContent] = useState("");
  const [source, setSource] = useState("note");
  const [category, setCategory] = useState("personal");
  const [tags, setTags] = useState("");
  const [isLoading, setIsLoading] = useState(false);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    if (!content.trim()) {
      toast.error("Knowledge content cannot be empty.");
      return;
    }

    setIsLoading(true);

    try {
      // Parse optional tags into a list in metadata
      const metadata: Record<string, any> = {
        category,
      };

      if (tags.trim()) {
        metadata.tags = tags
          .split(",")
          .map((t) => t.trim())
          .filter((t) => t.length > 0);
      }

      await knowledgeService.ingest({
        content: content.trim(),
        source,
        metadata,
      });

      toast.success("Knowledge successfully ingested!");
      setContent("");
      setTags("");
      onSuccess(); // Trigger reload of the snippet list
    } catch (error: any) {
      console.error("Ingestion failed:", error);
      toast.error(
        error.response?.data?.message ||
          "Failed to ingest knowledge. Please verify database connection."
      );
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <Card className="bg-slate-900/40 border-slate-800 backdrop-blur-md shadow-xl transition-all duration-300 hover:border-slate-700/60">
      <CardHeader>
        <CardTitle className="text-xl font-bold flex items-center gap-2 bg-gradient-to-r from-blue-400 to-purple-400 bg-clip-text text-transparent">
          <Sparkles className="w-5 h-5 text-blue-400" />
          Ingest Knowledge
        </CardTitle>
        <CardDescription className="text-slate-400">
          Feed new notes, snippets, or documents into your AI's long-term memory.
        </CardDescription>
      </CardHeader>
      <CardContent>
        <form onSubmit={handleSubmit} className="space-y-5">
          {/* Content Textarea */}
          <div className="space-y-2">
            <Label htmlFor="content" className="text-slate-300 font-medium">
              Snippet Content
            </Label>
            <Textarea
              id="content"
              placeholder="e.g. Kyoto travel itinerary: Kyoto tour on Oct 12, booking ref #JP8827, tour guide Sarah."
              value={content}
              onChange={(e) => setContent(e.target.value)}
              className="bg-slate-950/70 border-slate-800 text-slate-100 placeholder:text-slate-600 focus:border-blue-500/50 min-h-[120px] resize-y"
              disabled={isLoading}
            />
          </div>

          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
            {/* Source Selection */}
            <div className="space-y-2">
              <Label htmlFor="source" className="text-slate-300 font-medium">
                Source Type
              </Label>
              <select
                id="source"
                value={source}
                onChange={(e) => setSource(e.target.value)}
                className="w-full bg-slate-950/70 border border-slate-800 text-slate-100 rounded-md p-2 text-sm focus:outline-none focus:border-blue-500/50 h-10 disabled:opacity-50"
                disabled={isLoading}
              >
                <option value="note">📝 Note</option>
                <option value="email">📧 Email</option>
                <option value="document">📄 Document</option>
                <option value="web_page">🌐 Web Page</option>
                <option value="manual">✍️ Manual Input</option>
              </select>
            </div>

            {/* Category Selection */}
            <div className="space-y-2">
              <Label htmlFor="category" className="text-slate-300 font-medium">
                Category
              </Label>
              <select
                id="category"
                value={category}
                onChange={(e) => setCategory(e.target.value)}
                className="w-full bg-slate-950/70 border border-slate-800 text-slate-100 rounded-md p-2 text-sm focus:outline-none focus:border-blue-500/50 h-10 disabled:opacity-50"
                disabled={isLoading}
              >
                <option value="personal">👨‍💼 Personal</option>
                <option value="work">🏢 Work</option>
                <option value="learning">🎓 Learning</option>
                <option value="finance">💰 Finance</option>
                <option value="health">❤️ Health & Habit</option>
              </select>
            </div>
          </div>

          {/* Tags Input */}
          <div className="space-y-2">
            <Label htmlFor="tags" className="text-slate-300 font-medium">
              Metadata Tags <span className="text-xs text-slate-500">(comma-separated)</span>
            </Label>
            <Input
              id="tags"
              type="text"
              placeholder="e.g. travel, japan, schedule"
              value={tags}
              onChange={(e) => setTags(e.target.value)}
              className="bg-slate-950/70 border-slate-800 text-slate-100 placeholder:text-slate-600 focus:border-blue-500/50 h-10"
              disabled={isLoading}
            />
          </div>

          {/* Submit Button */}
          <Button
            type="submit"
            disabled={isLoading}
            className="w-full bg-gradient-to-r from-blue-600 to-purple-600 hover:from-blue-500 hover:to-purple-500 text-white font-medium shadow-md shadow-blue-500/10 transition-all duration-300 hover:shadow-blue-500/20 active:scale-[0.98] h-11"
          >
            {isLoading ? (
              <>
                <Loader2 className="w-4 h-4 mr-2 animate-spin" />
                Ingesting to vector memory...
              </>
            ) : (
              <>
                <Plus className="w-4 h-4 mr-2" />
                Add to Knowledge Base
              </>
            )}
          </Button>
        </form>
      </CardContent>
    </Card>
  );
}
