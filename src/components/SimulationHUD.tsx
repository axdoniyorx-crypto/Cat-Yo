import React from 'react';
import { Agent, PointOfInterest, SimulationWorld, STATE_METADATA, WEATHER_METADATA } from '../types';
import { Play, Pause, BarChart2, Sun, Moon } from 'lucide-react';

interface SimulationHUDProps {
  world: SimulationWorld;
  agents: Agent[];
  selectedAgentId?: number;
  speed: number;
  isPaused: boolean;
  onSelectAgent: (id: number) => void;
  onSetSpeed: (speed: number) => void;
  onTogglePause: () => void;
  onOpenStats: () => void;
}

export const SimulationHUD: React.FC<SimulationHUDProps> = ({
  world,
  agents,
  selectedAgentId,
  speed,
  isPaused,
  onSelectAgent,
  onSetSpeed,
  onTogglePause,
  onOpenStats,
}) => {
  const isNight = world.timeOfDayHours < 6.0 || world.timeOfDayHours > 20.0;
  const totalMinutes = Math.floor(world.timeOfDayHours * 60) % (24 * 60);
  const h24 = Math.floor(totalMinutes / 60);
  const m = totalMinutes % 60;
  const amPm = h24 < 12 ? 'AM' : 'PM';
  const h12 = h24 === 0 ? 12 : h24 > 12 ? h24 - 12 : h24;
  const clockString = `${String(h12).padStart(2, '0')}:${String(m).padStart(2, '0')} ${amPm} (Day ${world.simulationDay})`;

  return (
    <div className="absolute top-2 left-2 right-2 max-w-2xl mx-auto flex flex-col gap-2 z-20 pointer-events-none">
      {/* Top Status Bar */}
      <div className="pointer-events-auto bg-[#131926]/90 backdrop-blur-md border border-[#26334a] rounded-2xl px-3.5 py-2 shadow-xl flex items-center justify-between gap-3 text-white">
        {/* Clock & Day */}
        <div className="flex items-center gap-2">
          {isNight ? (
            <Moon className="w-4 h-4 text-indigo-400" />
          ) : (
            <Sun className="w-4 h-4 text-amber-400" />
          )}
          <div>
            <div className="font-extrabold text-xs tracking-tight text-slate-100">{clockString}</div>
            <div className="text-[10px] text-sky-400 font-semibold">{agents.length} AI Citizens</div>
          </div>
        </div>

        {/* Dynamic Weather Badge */}
        {(() => {
          const wType = world.weather?.current || 'CLEAR';
          const meta = WEATHER_METADATA[wType];
          const spdDiff = Math.round((meta.speedMultiplier - 1.0) * 100);
          const spdText = spdDiff === 0 ? '100% Spd' : `${spdDiff}% Spd`;
          return (
            <div
              className="flex items-center gap-1.5 bg-[#172133] px-2 py-1 rounded-xl border border-slate-700/80 text-xs shadow-sm cursor-help"
              title={`${meta.displayName}: ${meta.description}`}
            >
              <span className="text-sm">{meta.emoji}</span>
              <div>
                <div className="text-[10px] font-bold text-slate-100 leading-tight">{meta.displayName}</div>
                <div className={`text-[9px] font-semibold leading-none ${spdDiff < 0 ? 'text-red-400' : 'text-emerald-400'}`}>
                  {spdText}
                </div>
              </div>
            </div>
          );
        })()}

        {/* City Stockpile */}
        <div className="flex items-center gap-2 bg-[#0c101a] px-2.5 py-1 rounded-xl border border-slate-800 text-xs font-bold">
          <span title="Food Supply">🍏 {world.stockpile.foodStock}</span>
          <span className="text-slate-600">|</span>
          <span title="Timber Supply">🪵 {world.stockpile.timberStock}</span>
          <span className="text-slate-600">|</span>
          <span title="Mineral Supply">💎 {world.stockpile.mineralStock}</span>
        </div>

        {/* Speed Controls & Leaderboard */}
        <div className="flex items-center gap-1">
          <button
            onClick={onTogglePause}
            className={`p-1.5 rounded-lg border text-xs transition-colors ${
              isPaused
                ? 'bg-amber-500/20 border-amber-500 text-amber-400'
                : 'bg-slate-800 border-slate-700 text-slate-200'
            }`}
            title={isPaused ? 'Resume' : 'Pause'}
          >
            {isPaused ? <Play size={14} /> : <Pause size={14} />}
          </button>

          {[1, 2, 4].map((s) => (
            <button
              key={s}
              onClick={() => onSetSpeed(s)}
              className={`px-2 py-1 rounded-lg text-xs font-bold transition-colors ${
                !isPaused && speed === s
                  ? 'bg-sky-500 text-slate-950 font-black'
                  : 'bg-slate-800 text-slate-400 hover:text-slate-200'
              }`}
            >
              {s}x
            </button>
          ))}

          <button
            onClick={onOpenStats}
            className="p-1.5 rounded-lg bg-slate-800 hover:bg-slate-700 text-sky-400 border border-slate-700 transition-colors ml-1"
            title="City Registry & Leaderboard"
          >
            <BarChart2 size={16} />
          </button>
        </div>
      </div>

      {/* Citizen Quick-Access Carousel */}
      <div className="pointer-events-auto flex items-center gap-1.5 overflow-x-auto pb-1 scrollbar-none px-1">
        {agents.map((agent) => {
          const isSelected = agent.id === selectedAgentId;
          const isFounder = agent.id < 3; // Reno, Ather, Liora
          const stateMeta = STATE_METADATA[agent.activeState];

          return (
            <button
              key={agent.id}
              onClick={() => onSelectAgent(agent.id)}
              className={`flex items-center gap-1.5 px-2.5 py-1 rounded-xl text-xs whitespace-nowrap transition-all border shrink-0 ${
                isSelected
                  ? 'bg-sky-950 border-sky-400 text-white shadow-md shadow-sky-500/20'
                  : isFounder
                  ? 'bg-slate-900/90 border-amber-500/50 text-slate-200'
                  : 'bg-slate-900/80 border-slate-800 text-slate-300 hover:border-slate-700'
              }`}
            >
              <div
                className="w-2.5 h-2.5 rounded-full shrink-0"
                style={{ backgroundColor: agent.color }}
              />
              <span className={`font-semibold ${isFounder ? 'text-amber-300' : ''}`}>
                {agent.name}
              </span>
              <span>{stateMeta?.emoji || '🚶'}</span>
              {agent.skillLevel > 1 && (
                <span className="text-[9px] font-bold text-amber-400">v{agent.skillLevel}</span>
              )}
            </button>
          );
        })}
      </div>
    </div>
  );
};
