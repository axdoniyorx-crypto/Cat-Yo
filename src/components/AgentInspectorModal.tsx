import React from 'react';
import { Agent, STATE_ICONS } from '../simulation/types';
import { X, ChevronLeft, ChevronRight, Utensils, Zap, Sparkles } from 'lucide-react';

interface AgentInspectorModalProps {
  agent: Agent | null;
  onDismiss: () => void;
  onFeed: () => void;
  onRest: () => void;
  onInspire: () => void;
  onSelectNext: () => void;
  onSelectPrev: () => void;
}

export const AgentInspectorModal: React.FC<AgentInspectorModalProps> = ({
  agent,
  onDismiss,
  onFeed,
  onRest,
  onInspire,
  onSelectNext,
  onSelectPrev
}) => {
  if (!agent) return null;

  const xpNeeded = agent.skillLevel * 100;
  const xpPct = Math.min(100, Math.max(0, (agent.skillXp / xpNeeded) * 100));
  const efficiency = (1.0 + (agent.skillLevel - 1) * 0.25).toFixed(2);

  return (
    <div
      style={{
        position: 'absolute',
        bottom: '12px',
        left: '12px',
        right: '12px',
        maxHeight: '75vh',
        overflowY: 'auto',
        backgroundColor: '#141923',
        border: '1px solid #26334a',
        borderRadius: '24px',
        padding: '16px',
        boxShadow: '0 12px 36px rgba(0,0,0,0.7)',
        zIndex: 30,
        color: '#ffffff'
      }}
    >
      {/* Drag handle */}
      <div
        style={{
          width: '40px',
          height: '4px',
          borderRadius: '2px',
          backgroundColor: '#3b4861',
          margin: '0 auto 12px auto'
        }}
      />

      {/* Header Row */}
      <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '14px' }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
          <div
            style={{
              width: '48px',
              height: '48px',
              borderRadius: '50%',
              backgroundColor: agent.color,
              border: '2px solid rgba(255,255,255,0.8)',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              fontSize: '22px',
              fontWeight: 'bold',
              color: '#ffffff'
            }}
          >
            {agent.activeEmote || agent.name.charAt(0)}
          </div>
          <div>
            <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
              <span style={{ fontSize: '18px', fontWeight: 800 }}>{agent.name}</span>
              <div
                style={{
                  backgroundColor: '#1f2b3e',
                  border: `1px solid ${agent.color}`,
                  borderRadius: '8px',
                  padding: '2px 8px',
                  fontSize: '11px',
                  fontWeight: 600,
                  color: '#e2e8f0'
                }}
              >
                {STATE_ICONS[agent.activeState]} {agent.activeState}
              </div>
            </div>
            <div style={{ fontSize: '11px', color: '#94a3b8', marginTop: '2px' }}>
              {agent.personality} • Speed: {efficiency}x
            </div>
          </div>
        </div>

        {/* Action Controls */}
        <div style={{ display: 'flex', alignItems: 'center', gap: '4px' }}>
          <button onClick={onSelectPrev} style={smallIconBtnStyle} title="Previous Agent">
            <ChevronLeft size={18} color="#94a3b8" />
          </button>
          <button onClick={onSelectNext} style={smallIconBtnStyle} title="Next Agent">
            <ChevronRight size={18} color="#94a3b8" />
          </button>
          <button onClick={onDismiss} style={smallIconBtnStyle} title="Close">
            <X size={18} color="#ffffff" />
          </button>
        </div>
      </div>

      {/* Vital Needs Stats (0-100) */}
      <div style={sectionTitleStyle}>VITAL NEEDS (0-100)</div>
      <div style={{ display: 'flex', flexDirection: 'column', gap: '8px', marginBottom: '14px' }}>
        <StatBar
          label="Hunger / Fullness"
          value={agent.hunger}
          display={`${Math.round(agent.hunger)}%`}
          color={agent.hunger < 25 ? '#ef4444' : '#22c55e'}
          icon="🍎"
        />
        <StatBar
          label="Energy / Stamina"
          value={agent.energy}
          display={`${Math.round(agent.energy)}%`}
          color={agent.energy < 20 ? '#ef4444' : '#3b82f6'}
          icon="⚡"
        />
        <StatBar
          label="Social Satisfaction"
          value={agent.social}
          display={`${Math.round(agent.social)}%`}
          color="#ec4899"
          icon="💬"
        />
        <StatBar
          label="Mental Stimulation"
          value={100 - agent.boredom}
          display={`${Math.round(100 - agent.boredom)}% (Boredom: ${Math.round(agent.boredom)}%)`}
          color="#a855f7"
          icon="🌸"
        />
      </div>

      {/* Skill & Learning Progression */}
      <div style={sectionTitleStyle}>SKILL & LEARNING PROGRESSION</div>
      <div
        style={{
          backgroundColor: '#1b2332',
          borderRadius: '12px',
          padding: '12px',
          display: 'flex',
          justifyContent: 'space-between',
          alignItems: 'center',
          marginBottom: '14px'
        }}
      >
        <div>
          <div style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
            <span style={{ fontSize: '13px', fontWeight: 'bold' }}>Gathering Skill Level {agent.skillLevel}</span>
            <span
              style={{
                fontSize: '10px',
                fontWeight: 'bold',
                color: '#fbbf24',
                backgroundColor: 'rgba(245, 158, 11, 0.2)',
                padding: '2px 5px',
                borderRadius: '4px'
              }}
            >
              +{Math.round((parseFloat(efficiency) - 1.0) * 100)}% Yield
            </span>
          </div>
          <div
            style={{
              width: '180px',
              height: '6px',
              backgroundColor: '#334155',
              borderRadius: '3px',
              margin: '6px 0 4px 0',
              overflow: 'hidden'
            }}
          >
            <div
              style={{
                width: `${xpPct}%`,
                height: '100%',
                backgroundColor: '#f59e0b',
                transition: 'width 0.2s'
              }}
            />
          </div>
          <div style={{ fontSize: '11px', color: '#94a3b8' }}>
            {Math.round(agent.skillXp)} / {xpNeeded} XP to Lv. {agent.skillLevel + 1}
          </div>
        </div>

        <div style={{ textAlign: 'right' }}>
          <div style={{ fontSize: '13px', fontWeight: 'bold', color: '#38bdf8' }}>
            {agent.tasksCompleted} Tasks
          </div>
          <div style={{ fontSize: '11px', color: '#94a3b8' }}>
            {agent.resourcesGathered} Harvested
          </div>
        </div>
      </div>

      {/* Recent Memories & Thoughts */}
      <div style={sectionTitleStyle}>RECENT MEMORIES & THOUGHTS</div>
      <div
        style={{
          backgroundColor: '#0f141e',
          borderRadius: '10px',
          padding: '10px',
          display: 'flex',
          flexDirection: 'column',
          gap: '4px',
          marginBottom: '14px'
        }}
      >
        {agent.recentThoughts.slice(0, 3).map((thought, idx) => (
          <div key={idx} style={{ fontSize: '12px', color: '#cbd5e1', display: 'flex', gap: '6px' }}>
            <span style={{ color: '#38bdf8' }}>•</span>
            <span>{thought}</span>
          </div>
        ))}
      </div>

      {/* Overseer God Powers */}
      <div style={{ display: 'flex', gap: '8px' }}>
        <button
          onClick={onFeed}
          style={{
            ...godBtnStyle,
            backgroundColor: '#1e3a2b',
            color: '#4ade80'
          }}
        >
          <Utensils size={15} /> Feed Snack
        </button>
        <button
          onClick={onRest}
          style={{
            ...godBtnStyle,
            backgroundColor: '#1e293b',
            color: '#60a5fa'
          }}
        >
          <Zap size={15} /> Energize
        </button>
        <button
          onClick={onInspire}
          style={{
            ...godBtnStyle,
            backgroundColor: '#37243e',
            color: '#f472b6'
          }}
        >
          <Sparkles size={15} /> Inspire Skill
        </button>
      </div>
    </div>
  );
};

interface StatBarProps {
  label: string;
  value: number;
  display: string;
  color: string;
  icon: string;
}

const StatBar: React.FC<StatBarProps> = ({ label, value, display, color, icon }) => (
  <div>
    <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '11px', marginBottom: '3px' }}>
      <span style={{ color: '#cbd5e1' }}>
        {icon} {label}
      </span>
      <span style={{ fontWeight: 600, color: '#e2e8f0' }}>{display}</span>
    </div>
    <div style={{ width: '100%', height: '6px', backgroundColor: '#1e293b', borderRadius: '3px', overflow: 'hidden' }}>
      <div
        style={{
          width: `${Math.max(0, Math.min(100, value))}%`,
          height: '100%',
          backgroundColor: color,
          transition: 'width 0.15s ease'
        }}
      />
    </div>
  </div>
);

const sectionTitleStyle: React.CSSProperties = {
  fontSize: '10px',
  fontWeight: 700,
  color: '#64748B',
  letterSpacing: '0.08em',
  marginBottom: '6px'
};

const smallIconBtnStyle: React.CSSProperties = {
  background: 'none',
  border: 'none',
  cursor: 'pointer',
  padding: '6px',
  display: 'flex',
  alignItems: 'center',
  justifyContent: 'center',
  borderRadius: '8px'
};

const godBtnStyle: React.CSSProperties = {
  flex: 1,
  border: 'none',
  borderRadius: '10px',
  padding: '8px 4px',
  fontSize: '12px',
  fontWeight: 700,
  cursor: 'pointer',
  display: 'flex',
  alignItems: 'center',
  justifyContent: 'center',
  gap: '4px'
};
