import React from 'react';
import { Agent, SimulationWorld } from '../types';
import { X, Trophy, RotateCcw } from 'lucide-react';

interface CityStatsModalProps {
  world: SimulationWorld;
  agents: Agent[];
  onClose: () => void;
  onSelectAgent: (id: number) => void;
  onReset: () => void;
}

export const CityStatsModal: React.FC<CityStatsModalProps> = ({
  world,
  agents,
  onClose,
  onSelectAgent,
  onReset,
}) => {
  const sorted = [...agents].sort((a, b) => {
    if (b.skillLevel !== a.skillLevel) return b.skillLevel - a.skillLevel;
    if (b.skillXp !== a.skillXp) return b.skillXp - a.skillXp;
    return b.resourcesGathered - a.resourcesGathered;
  });

  return (
    <div className="fixed inset-0 bg-black/75 backdrop-blur-sm z-50 flex items-center justify-center p-4">
      <div className="bg-[#111723] border border-[#2b3a52] rounded-3xl w-full max-w-md max-h-[85vh] flex flex-col shadow-2xl text-white overflow-hidden">
        {/* Modal Header */}
        <div className="p-4 border-b border-slate-800 flex items-center justify-between">
          <div className="flex items-center gap-2">
            <Trophy className="text-amber-400 w-5 h-5" />
            <h2 className="font-extrabold text-base tracking-tight">MicroLife Municipal Registry</h2>
          </div>
          <button
            onClick={onClose}
            className="p-1.5 rounded-lg bg-slate-800 hover:bg-slate-700 text-slate-300 transition-colors"
          >
            <X size={18} />
          </button>
        </div>

        {/* City Stats Summary Card */}
        <div className="p-4 bg-[#1b2435] border-b border-slate-800 grid grid-cols-3 gap-2 text-center text-xs">
          <div>
            <div className="text-slate-400">Population</div>
            <div className="font-extrabold text-white text-sm mt-0.5">{agents.length} Citizens</div>
          </div>
          <div>
            <div className="text-slate-400">Tasks Finished</div>
            <div className="font-extrabold text-sky-400 text-sm mt-0.5">{world.stockpile.totalWorkDone}</div>
          </div>
          <div>
            <div className="text-slate-400">Social Bonds</div>
            <div className="font-extrabold text-pink-400 text-sm mt-0.5">{world.stockpile.socialInteractionsCount}</div>
          </div>
        </div>

        {/* Citizen Leaderboard */}
        <div className="flex-1 overflow-y-auto p-3 space-y-1.5">
          <div className="text-[10px] font-bold text-slate-400 px-2 tracking-wider mb-2">
            CITIZEN LEADERBOARD & SKILL RANKINGS
          </div>
          {sorted.map((agent, index) => {
            const isFounder = agent.id < 3;
            return (
              <div
                key={agent.id}
                onClick={() => {
                  onSelectAgent(agent.id);
                  onClose();
                }}
                className="flex items-center justify-between p-2.5 rounded-xl bg-[#161e2e] hover:bg-[#1f2b40] cursor-pointer border border-transparent hover:border-sky-500/50 transition-all"
              >
                <div className="flex items-center gap-2.5">
                  <span className={`text-xs font-black w-5 text-center ${index < 3 ? 'text-amber-400' : 'text-slate-500'}`}>
                    #{index + 1}
                  </span>
                  <div
                    className="w-3 h-3 rounded-full shrink-0"
                    style={{ backgroundColor: agent.color }}
                  />
                  <div>
                    <div className="flex items-center gap-1.5">
                      <span className="font-bold text-xs text-white">{agent.name}</span>
                      {isFounder && (
                        <span className="text-[9px] px-1 rounded bg-amber-500/20 text-amber-300 font-bold">
                          Founder
                        </span>
                      )}
                    </div>
                    <span className="text-[10px] text-slate-400">
                      {agent.personality}
                    </span>
                  </div>
                </div>

                <div className="text-right">
                  <div className="text-xs font-bold text-sky-400">Skill Lv. {agent.skillLevel}</div>
                  <div className="text-[10px] text-slate-500">{agent.resourcesGathered} Harvested</div>
                </div>
              </div>
            );
          })}
        </div>

        {/* Footer Actions */}
        <div className="p-3 border-t border-slate-800 bg-[#0e131d]">
          <button
            onClick={() => {
              onReset();
              onClose();
            }}
            className="w-full flex items-center justify-center gap-2 py-2.5 px-4 bg-red-950/70 hover:bg-red-900 border border-red-700/50 rounded-xl text-red-200 text-xs font-bold transition-all"
          >
            <RotateCcw size={14} /> Re-seed Simulation World
          </button>
        </div>
      </div>
    </div>
  );
};
