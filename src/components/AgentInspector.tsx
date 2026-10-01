import React from 'react';
import { Agent, STATE_METADATA } from '../types';
import { ChevronLeft, ChevronRight, X, Utensils, Zap, Sparkles, Brain, Award } from 'lucide-react';

interface AgentInspectorProps {
  agent: Agent;
  onDismiss: () => void;
  onFeed: () => void;
  onRest: () => void;
  onInspire: () => void;
  onNext: () => void;
  onPrev: () => void;
}

export const AgentInspector: React.FC<AgentInspectorProps> = ({
  agent,
  onDismiss,
  onFeed,
  onRest,
  onInspire,
  onNext,
  onPrev,
}) => {
  const stateMeta = STATE_METADATA[agent.activeState] || { name: agent.activeState, emoji: '🤖', color: '#fff' };
  const efficiency = (1.0 + (agent.skillLevel - 1) * 0.25).toFixed(2);
  const xpNeeded = agent.skillLevel * 100;
  const xpPercent = Math.min(100, Math.max(0, (agent.skillXp / xpNeeded) * 100));

  return (
    <div
      className="absolute bottom-3 left-3 right-3 max-w-lg mx-auto bg-[#131926]/95 backdrop-blur-md border border-[#26334a] rounded-2xl shadow-2xl p-4 text-white animate-in slide-in-from-bottom duration-200 z-30"
      data-testid="agent_inspector_card"
    >
      {/* Top Handle */}
      <div className="w-10 h-1 bg-slate-600 rounded-full mx-auto mb-3" />

      {/* Header Row */}
      <div className="flex items-center justify-between gap-3 mb-3">
        <div className="flex items-center gap-3">
          {/* Avatar Dot */}
          <div
            className="w-12 h-12 rounded-full flex items-center justify-center font-bold text-xl border-2 border-white/80 shadow-md"
            style={{ backgroundColor: agent.color }}
          >
            {agent.activeEmote || agent.name[0]}
          </div>

          <div>
            <div className="flex items-center gap-2">
              <h2 className="text-lg font-black tracking-tight" data-testid="inspector_agent_name">
                {agent.name}
              </h2>
              <span
                className="text-xs px-2 py-0.5 rounded-full border flex items-center gap-1 font-semibold"
                style={{ borderColor: agent.color, backgroundColor: `${agent.color}22` }}
              >
                <span>{stateMeta.emoji}</span>
                <span>{stateMeta.name}</span>
              </span>
            </div>
            <p className="text-xs text-slate-400">
              {agent.personality} • Efficiency: {efficiency}x
            </p>
          </div>
        </div>

        {/* Navigation & Close */}
        <div className="flex items-center gap-1">
          <button
            onClick={onPrev}
            className="p-1.5 rounded-lg bg-slate-800 hover:bg-slate-700 text-slate-300 transition-colors"
            title="Previous Agent"
          >
            <ChevronLeft size={18} />
          </button>
          <button
            onClick={onNext}
            className="p-1.5 rounded-lg bg-slate-800 hover:bg-slate-700 text-slate-300 transition-colors"
            title="Next Agent"
          >
            <ChevronRight size={18} />
          </button>
          <button
            onClick={onDismiss}
            className="p-1.5 rounded-lg bg-slate-800 hover:bg-red-900/60 text-slate-300 hover:text-white transition-colors ml-1"
            title="Close"
          >
            <X size={18} />
          </button>
        </div>
      </div>

      {/* Dynamic Needs Grid */}
      <div className="bg-[#0f141f] rounded-xl p-3 border border-slate-800/80 mb-3 space-y-2">
        <div className="text-[10px] font-bold text-slate-400 tracking-wider">CORE VITAL NEEDS (0-100)</div>

        {/* Hunger */}
        <StatBar
          label="Hunger / Fullness"
          value={agent.hunger}
          color={agent.hunger < 25 ? '#ef4444' : '#22c55e'}
          icon="🍎"
        />

        {/* Energy */}
        <StatBar
          label="Energy / Stamina"
          value={agent.energy}
          color={agent.energy < 20 ? '#ef4444' : '#3b82f6'}
          icon="⚡"
        />

        {/* Social */}
        <StatBar
          label="Social Satisfaction"
          value={agent.social}
          color="#ec4899"
          icon="💬"
        />

        {/* Boredom / Stimulation */}
        <StatBar
          label={`Stimulation (Boredom: ${Math.round(agent.boredom)}%)`}
          value={100 - agent.boredom}
          color="#a855f7"
          icon="🌸"
        />
      </div>

      {/* Skill Progression */}
      <div className="bg-[#1b2332] rounded-xl p-3 border border-slate-700/60 mb-3 flex items-center justify-between">
        <div>
          <div className="flex items-center gap-1.5">
            <Award className="w-4 h-4 text-amber-400" />
            <span className="font-bold text-sm text-slate-100">Gathering Skill Lv. {agent.skillLevel}</span>
            <span className="text-[10px] font-bold px-1.5 py-0.5 rounded bg-amber-500/20 text-amber-300">
              +{Math.round((parseFloat(efficiency) - 1.0) * 100)}% Speed
            </span>
          </div>
          <div className="w-48 h-1.5 bg-slate-700 rounded-full mt-2 overflow-hidden">
            <div
              className="h-full bg-gradient-to-r from-amber-500 to-yellow-300 rounded-full transition-all duration-300"
              style={{ width: `${xpPercent}%` }}
            />
          </div>
          <span className="text-[10px] text-slate-400 mt-1 block">
            {Math.round(agent.skillXp)} / {xpNeeded} XP to Lv. {agent.skillLevel + 1}
          </span>
        </div>

        <div className="text-right">
          <div className="text-xs font-bold text-sky-400">{agent.tasksCompleted} Tasks</div>
          <div className="text-[10px] text-slate-400">{agent.resourcesGathered} Harvested</div>
        </div>
      </div>

      {/* Recent Thoughts */}
      <div className="bg-[#0b0f17] rounded-lg p-2.5 border border-slate-800 text-xs mb-3">
        <div className="flex items-center gap-1 text-[10px] font-bold text-sky-400 mb-1">
          <Brain className="w-3 h-3" /> RECENT MEMORY
        </div>
        <p className="text-slate-300 italic truncate">
          "{agent.recentThoughts[0] || 'Observing the bustling micro-city.'}"
        </p>
      </div>

      {/* Interactive God-Mode Overseer Actions */}
      <div className="grid grid-cols-3 gap-2">
        <button
          onClick={onFeed}
          className="flex items-center justify-center gap-1.5 py-2 px-3 bg-emerald-950/80 hover:bg-emerald-900 border border-emerald-600/50 rounded-xl text-emerald-400 text-xs font-bold transition-all active:scale-95"
        >
          <Utensils size={14} /> Feed
        </button>
        <button
          onClick={onRest}
          className="flex items-center justify-center gap-1.5 py-2 px-3 bg-blue-950/80 hover:bg-blue-900 border border-blue-600/50 rounded-xl text-blue-400 text-xs font-bold transition-all active:scale-95"
        >
          <Zap size={14} /> Energize
        </button>
        <button
          onClick={onInspire}
          className="flex items-center justify-center gap-1.5 py-2 px-3 bg-purple-950/80 hover:bg-purple-900 border border-purple-600/50 rounded-xl text-purple-400 text-xs font-bold transition-all active:scale-95"
        >
          <Sparkles size={14} /> Inspire
        </button>
      </div>
    </div>
  );
};

const StatBar: React.FC<{ label: string; value: number; color: string; icon: string }> = ({
  label,
  value,
  color,
  icon,
}) => {
  const percent = Math.min(100, Math.max(0, value));
  return (
    <div>
      <div className="flex justify-between text-xs mb-1">
        <span className="flex items-center gap-1 text-slate-300">
          <span>{icon}</span>
          <span>{label}</span>
        </span>
        <span className="font-semibold text-slate-200">{Math.round(percent)}%</span>
      </div>
      <div className="w-full h-1.5 bg-slate-800 rounded-full overflow-hidden">
        <div
          className="h-full rounded-full transition-all duration-300"
          style={{ width: `${percent}%`, backgroundColor: color }}
        />
      </div>
    </div>
  );
};
