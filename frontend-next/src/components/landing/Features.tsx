"use client";

import { motion } from "framer-motion";
import { 
  Briefcase, 
  Share2, 
  Mail, 
  Wallet, 
  GraduationCap, 
  Heart, 
  Cpu, 
  Shield, 
  Zap,
  Globe
} from "lucide-react";

const agents = [
  {
    title: "Job Agent",
    description: "AI-powered job matching, scoring, and application tracking.",
    icon: Briefcase,
    color: "text-blue-500",
    bg: "bg-blue-500/10",
    border: "border-blue-500/20"
  },
  {
    title: "Social Agent",
    description: "Manage your social presence with AI drafting and scheduling.",
    icon: Share2,
    color: "text-purple-500",
    bg: "bg-purple-500/10",
    border: "border-purple-500/20"
  },
  {
    title: "Email Agent",
    description: "Inbox summarization and smart reply generation.",
    icon: Mail,
    color: "text-green-500",
    bg: "bg-green-500/10",
    border: "border-green-500/20"
  },
  {
    title: "Finance Agent",
    description: "Track expenses and budgets with AI spending insights.",
    icon: Wallet,
    color: "text-yellow-500",
    bg: "bg-yellow-500/10",
    border: "border-yellow-500/20"
  },
  {
    title: "Learning Agent",
    description: "Personal roadmap generator and skill gap analyzer.",
    icon: GraduationCap,
    color: "text-indigo-500",
    bg: "bg-indigo-500/10",
    border: "border-indigo-500/20"
  },
  {
    title: "Health Agent",
    description: "Habit tracking and weekly life-score reporting.",
    icon: Heart,
    color: "text-red-500",
    bg: "bg-red-500/10",
    border: "border-red-500/20"
  }
];

export default function Features() {
  return (
    <section id="features" className="py-24 bg-slate-950">
      <div className="container px-6">
        <div className="text-center mb-16">
          <motion.h2 
            initial={{ opacity: 0, y: 20 }}
            whileInView={{ opacity: 1, y: 0 }}
            viewport={{ once: true }}
            className="text-3xl md:text-5xl font-bold text-white mb-4"
          >
            Meet Your New <span className="text-blue-500">Agent Ecosystem</span>
          </motion.h2>
          <motion.p 
            initial={{ opacity: 0, y: 20 }}
            whileInView={{ opacity: 1, y: 0 }}
            viewport={{ once: true }}
            transition={{ delay: 0.1 }}
            className="text-slate-400 text-lg max-w-2xl mx-auto"
          >
            A suite of specialized agents working in parallel to keep your digital life in sync.
          </motion.p>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {agents.map((agent, index) => (
            <motion.div
              key={index}
              initial={{ opacity: 0, y: 20 }}
              whileInView={{ opacity: 1, y: 0 }}
              viewport={{ once: true }}
              transition={{ delay: index * 0.1 }}
              className={`p-8 rounded-2xl border ${agent.border} bg-slate-900/40 backdrop-blur-sm hover:bg-slate-900/60 transition-all group`}
            >
              <div className={`w-12 h-12 rounded-xl ${agent.bg} flex items-center justify-center mb-6 group-hover:scale-110 transition-transform`}>
                <agent.icon className={`w-6 h-6 ${agent.color}`} />
              </div>
              <h3 className="text-xl font-bold text-white mb-3">{agent.title}</h3>
              <p className="text-slate-400 leading-relaxed">
                {agent.description}
              </p>
            </motion.div>
          ))}
        </div>

        {/* Technical Foundation Section */}
        <div className="mt-24 p-12 rounded-3xl border border-white/5 bg-gradient-to-br from-blue-500/5 to-purple-500/5 relative overflow-hidden">
            <div className="grid md:grid-cols-2 gap-12 items-center">
                <div>
                    <h3 className="text-3xl font-bold text-white mb-6">Built on an Unstoppable Foundation</h3>
                    <div className="space-y-6">
                        <div className="flex gap-4">
                            <div className="w-10 h-10 rounded-lg bg-white/5 flex items-center justify-center shrink-0">
                                <Cpu className="w-5 h-5 text-blue-400" />
                            </div>
                            <div>
                                <h4 className="font-semibold text-white">LangGraph4j AI Brain</h4>
                                <p className="text-slate-400 text-sm">Stateful multi-agent orchestration for complex workflows.</p>
                            </div>
                        </div>
                        <div className="flex gap-4">
                            <div className="w-10 h-10 rounded-lg bg-white/5 flex items-center justify-center shrink-0">
                                <Globe className="w-5 h-5 text-indigo-400" />
                            </div>
                            <div>
                                <h4 className="font-semibold text-white">Event-Driven Architecture</h4>
                                <p className="text-slate-400 text-sm">Powered by Kafka for real-time reactivity and scaling.</p>
                            </div>
                        </div>
                        <div className="flex gap-4">
                            <div className="w-10 h-10 rounded-lg bg-white/5 flex items-center justify-center shrink-0">
                                <Shield className="w-5 h-5 text-green-400" />
                            </div>
                            <div>
                                <h4 className="font-semibold text-white">Enterprise Security</h4>
                                <p className="text-slate-400 text-sm">JWT token rotation and HTTP-only cookie-based authentication.</p>
                            </div>
                        </div>
                    </div>
                </div>
                <div className="relative aspect-square md:aspect-auto h-64 md:h-full bg-slate-900/50 rounded-2xl border border-white/5 p-6 flex flex-col items-center justify-center text-center">
                    <Zap className="w-16 h-16 text-blue-500 fill-blue-500/20 mb-4 animate-pulse" />
                    <div className="text-2xl font-bold text-white mb-2">MyOS Neural Core</div>
                    <p className="text-slate-500 text-sm">Ready to ingest your personal knowledge.</p>
                </div>
            </div>
        </div>
      </div>
    </section>
  );
}
