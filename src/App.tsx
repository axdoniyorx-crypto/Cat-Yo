import React, { useState, useEffect, useRef, useCallback } from 'react';
import { SimulationEngine } from './engine/SimulationEngine';
import { Agent, PointOfInterest, SimulationWorld } from './types';
import { SimulationCanvas } from './components/SimulationCanvas';
import { SimulationHUD } from './components/SimulationHUD';
import { AgentInspector } from './components/AgentInspector';
import { CityStatsModal } from './components/CityStatsModal';

export const App: React.FC = () => {
  const [world, setWorld] = useState<SimulationWorld>(() => SimulationEngine.createDefaultWorld());
  const [agents, setAgents] = useState<Agent[]>(() =>
    SimulationEngine.createInitialAgents(SimulationEngine.createDefaultWorld())
  );
  const [selectedAgentId, setSelectedAgentId] = useState<number | undefined>(0); // Default to Reno
  const [selectedPoi, setSelectedPoi] = useState<PointOfInterest | undefined>(undefined);
  const [speed, setSpeed] = useState<number>(1.0);
  const [isPaused, setIsPaused] = useState<boolean>(false);
  const [showStatsModal, setShowStatsModal] = useState<boolean>(false);

  // References for the animation loop
  const worldRef = useRef(world);
  const agentsRef = useRef(agents);
  const speedRef = useRef(speed);
  const isPausedRef = useRef(isPaused);

  worldRef.current = world;
  agentsRef.current = agents;
  speedRef.current = speed;
  isPausedRef.current = isPaused;

  useEffect(() => {
    let animId: number;
    let lastTime = performance.now();

    const loop = (currentTime: number) => {
      const elapsedSeconds = Math.min(0.1, Math.max(0.005, (currentTime - lastTime) / 1000));
      lastTime = currentTime;

      if (!isPausedRef.current && speedRef.current > 0) {
        const effectiveDt = elapsedSeconds * speedRef.current;
        const result = SimulationEngine.tick(worldRef.current, agentsRef.current, effectiveDt);
        setWorld(result.world);
        setAgents(result.agents);
      }

      animId = requestAnimationFrame(loop);
    };

    animId = requestAnimationFrame(loop);
    return () => cancelAnimationFrame(animId);
  }, []);

  // God-Mode Overseer Touch Actions
  const handleFeed = useCallback(() => {
    if (selectedAgentId === undefined) return;
    setAgents((prev) =>
      prev.map((a) => {
        if (a.id === selectedAgentId) {
          const thoughts = ['Received a heavenly fruit from the overseer! (+35 Hunger)', ...a.recentThoughts].slice(0, 8);
          return {
            ...a,
            hunger: Math.min(100, a.hunger + 35),
            recentThoughts: thoughts,
            activeEmote: '🍏',
            emoteDuration: 2.5,
          };
        }
        return a;
      })
    );
  }, [selectedAgentId]);

  const handleRest = useCallback(() => {
    if (selectedAgentId === undefined) return;
    setAgents((prev) =>
      prev.map((a) => {
        if (a.id === selectedAgentId) {
          const thoughts = ['Blessed with rejuvenating vitality (+30 Energy)', ...a.recentThoughts].slice(0, 8);
          return {
            ...a,
            energy: Math.min(100, a.energy + 30),
            recentThoughts: thoughts,
            activeEmote: '⚡',
            emoteDuration: 2.5,
          };
        }
        return a;
      })
    );
  }, [selectedAgentId]);

  const handleInspire = useCallback(() => {
    if (selectedAgentId === undefined) return;
    setAgents((prev) =>
      prev.map((a) => {
        if (a.id === selectedAgentId) {
          let xp = a.skillXp + 45;
          let level = a.skillLevel;
          const xpForNext = level * 100;
          let thoughts = [...a.recentThoughts];

          if (xp >= xpForNext) {
            xp -= xpForNext;
            level += 1;
            thoughts = [`Divine inspiration prompted a Skill Level Up! (Lv. ${level})`, ...thoughts];
          } else {
            thoughts = ['Felt deeply inspired to master city craft! (+45 XP)', ...thoughts];
          }

          return {
            ...a,
            skillLevel: level,
            skillXp: xp,
            boredom: Math.max(0, a.boredom - 30),
            recentThoughts: thoughts.slice(0, 8),
            activeEmote: '✨',
            emoteDuration: 3.0,
          };
        }
        return a;
      })
    );
  }, [selectedAgentId]);

  const handleReset = useCallback(() => {
    const defaultWorld = SimulationEngine.createDefaultWorld();
    setWorld(defaultWorld);
    setAgents(SimulationEngine.createInitialAgents(defaultWorld));
    setSelectedAgentId(0);
    setSpeed(1.0);
    setIsPaused(false);
  }, []);

  const selectedAgent = agents.find((a) => a.id === selectedAgentId);

  return (
    <div className="relative w-full h-full overflow-hidden bg-[#0a0e17]">
      {/* 2D Top-Down Simulation Canvas */}
      <SimulationCanvas
        world={world}
        agents={agents}
        selectedAgentId={selectedAgentId}
        onSelectAgent={(id) => {
          setSelectedAgentId(id);
          setSelectedPoi(undefined);
        }}
        onSelectPoi={(poi) => {
          setSelectedPoi(poi);
          setSelectedAgentId(undefined);
        }}
      />

      {/* Top HUD */}
      <SimulationHUD
        world={world}
        agents={agents}
        selectedAgentId={selectedAgentId}
        speed={speed}
        isPaused={isPaused}
        onSelectAgent={(id) => {
          setSelectedAgentId(id);
          setSelectedPoi(undefined);
        }}
        onSetSpeed={(s) => {
          setSpeed(s);
          setIsPaused(false);
        }}
        onTogglePause={() => setIsPaused((prev) => !prev)}
        onOpenStats={() => setShowStatsModal(true)}
      />

      {/* POI Inspector Card */}
      {selectedPoi && !selectedAgent && (
        <div className="absolute bottom-4 left-4 right-4 max-w-md mx-auto bg-[#131926]/95 border border-[#26334a] rounded-2xl p-4 shadow-2xl text-white z-30">
          <div className="flex items-center justify-between mb-2">
            <div className="flex items-center gap-2">
              <span className="text-2xl">{selectedPoi.emoji}</span>
              <div>
                <h3 className="font-black text-sm">{selectedPoi.name}</h3>
                <p className="text-xs text-slate-400">{selectedPoi.description}</p>
              </div>
            </div>
            <button
              onClick={() => setSelectedPoi(undefined)}
              className="p-1 rounded bg-slate-800 text-slate-400 hover:text-white"
            >
              ✕
            </button>
          </div>
          <div className="text-xs font-semibold text-sky-400">
            Occupants Present:{' '}
            {agents.filter((a) => {
              const b = selectedPoi.bounds;
              return a.x >= b.x && a.x <= b.x + b.width && a.y >= b.y && a.y <= b.y + b.height;
            }).length}{' '}
            Citizens
          </div>
        </div>
      )}

      {/* Agent Inspector Modal */}
      {selectedAgent && (
        <AgentInspector
          agent={selectedAgent}
          onDismiss={() => setSelectedAgentId(undefined)}
          onFeed={handleFeed}
          onRest={handleRest}
          onInspire={handleInspire}
          onNext={() => {
            const nextId = (selectedAgent.id + 1) % agents.length;
            setSelectedAgentId(nextId);
          }}
          onPrev={() => {
            const prevId = (selectedAgent.id - 1 + agents.length) % agents.length;
            setSelectedAgentId(prevId);
          }}
        />
      )}

      {/* Municipal Leaderboard Modal */}
      {showStatsModal && (
        <CityStatsModal
          world={world}
          agents={agents}
          onClose={() => setShowStatsModal(false)}
          onSelectAgent={(id) => {
            setSelectedAgentId(id);
            setSelectedPoi(undefined);
          }}
          onReset={handleReset}
        />
      )}
    </div>
  );
};
