import React, { useRef, useEffect } from 'react';
import { Agent, PointOfInterest, SimulationWorld, STATE_METADATA } from '../types';

interface SimulationCanvasProps {
  world: SimulationWorld;
  agents: Agent[];
  selectedAgentId?: number;
  onSelectAgent: (id?: number) => void;
  onSelectPoi: (poi?: PointOfInterest) => void;
}

export const SimulationCanvas: React.FC<SimulationCanvasProps> = ({
  world,
  agents,
  selectedAgentId,
  onSelectAgent,
  onSelectPoi,
}) => {
  const canvasRef = useRef<HTMLCanvasElement | null>(null);
  const transformRef = useRef({ scale: 1, offsetX: 0, offsetY: 0 });

  useEffect(() => {
    const canvas = canvasRef.current;
    if (!canvas) return;
    const ctx = canvas.getContext('2d');
    if (!ctx) return;

    // Handle high DPI
    const dpr = window.devicePixelRatio || 1;
    const rect = canvas.getBoundingClientRect();
    canvas.width = rect.width * dpr;
    canvas.height = rect.height * dpr;
    ctx.scale(dpr, dpr);

    const screenWidth = rect.width;
    const screenHeight = rect.height;

    const scaleX = screenWidth / world.width;
    const scaleY = screenHeight / world.height;
    const scale = Math.min(scaleX, scaleY);
    const offsetX = (screenWidth - world.width * scale) / 2;
    const offsetY = (screenHeight - world.height * scale) / 2;

    transformRef.current = { scale, offsetX, offsetY };

    // 1. Clear background
    ctx.fillStyle = '#0a0e17';
    ctx.fillRect(0, 0, screenWidth, screenHeight);

    // Save before world transformation
    ctx.save();
    ctx.translate(offsetX, offsetY);
    ctx.scale(scale, scale);

    // 2. Draw Grid Lines
    ctx.strokeStyle = '#141c2c';
    ctx.lineWidth = 1;
    for (let x = 0; x <= world.width; x += 60) {
      ctx.beginPath();
      ctx.moveTo(x, 0);
      ctx.lineTo(x, world.height);
      ctx.stroke();
    }
    for (let y = 0; y <= world.height; y += 60) {
      ctx.beginPath();
      ctx.moveTo(0, y);
      ctx.lineTo(world.width, y);
      ctx.stroke();
    }

    // 3. Draw Enclosed City Wall
    ctx.strokeStyle = '#243048';
    ctx.lineWidth = 3;
    ctx.beginPath();
    ctx.roundRect(15, 35, world.width - 30, world.height - 55, 24);
    ctx.stroke();

    // 4. Pathways & Roads
    ctx.fillStyle = '#1e2838';
    ctx.strokeStyle = '#28364c';
    ctx.lineWidth = 1.5;

    // North-South Avenue
    ctx.fillRect(465, 110, 70, world.height - 230);
    ctx.strokeRect(465, 110, 70, world.height - 230);

    // East-West Avenue
    ctx.fillRect(40, 610, world.width - 80, 70);
    ctx.strokeRect(40, 610, world.width - 80, 70);

    // 5. Draw Points of Interest
    world.pois.forEach((poi) => {
      ctx.save();
      const { x, y, width, height } = poi.bounds;

      // Base card
      ctx.fillStyle = poi.color;
      ctx.beginPath();
      ctx.roundRect(x, y, width, height, 18);
      ctx.fill();

      // Border glow
      ctx.strokeStyle = poi.accentColor;
      ctx.lineWidth = 2.5;
      ctx.stroke();

      // Interior decorations
      if (poi.type === 'FOOD_SOURCE') {
        // Orchard trees
        for (let row = 0; row < 3; row++) {
          for (let col = 0; col < 4; col++) {
            const tx = x + 45 + col * 85;
            const ty = y + 130 + row * 70;
            ctx.fillStyle = '#795548';
            ctx.fillRect(tx - 3, ty, 6, 16);
            ctx.fillStyle = '#27ae60';
            ctx.beginPath();
            ctx.arc(tx, ty - 6, 20, 0, Math.PI * 2);
            ctx.fill();
            // Berries
            ctx.fillStyle = '#e74c3c';
            ctx.beginPath();
            ctx.arc(tx - 7, ty - 10, 3.5, 0, Math.PI * 2);
            ctx.arc(tx + 8, ty - 8, 3.5, 0, Math.PI * 2);
            ctx.arc(tx, ty - 16, 3.5, 0, Math.PI * 2);
            ctx.fill();
          }
        }
      } else if (poi.type === 'REST_AREA') {
        // Dormitory cabins
        for (let col = 0; col < 2; col++) {
          for (let row = 0; row < 2; row++) {
            const cx = x + 35 + col * 180;
            const cy = y + 110 + row * 125;
            ctx.fillStyle = '#34495e';
            ctx.beginPath();
            ctx.roundRect(cx, cy, 150, 95, 8);
            ctx.fill();
            ctx.fillStyle = '#2c3e50';
            ctx.beginPath();
            ctx.roundRect(cx - 5, cy - 8, 160, 28, 6);
            ctx.fill();
            // Warm glowing windows
            ctx.fillStyle = '#f1c40f';
            ctx.fillRect(cx + 25, cy + 40, 24, 24);
            ctx.fillRect(cx + 95, cy + 40, 24, 24);
          }
        }
      } else if (poi.type === 'TOWN_PLAZA') {
        // Central Fountain & Cobblestones
        const cx = x + width / 2;
        const cy = y + height / 2;

        ctx.fillStyle = '#251c37';
        ctx.beginPath();
        ctx.arc(cx, cy, 110, 0, Math.PI * 2);
        ctx.fill();
        ctx.strokeStyle = '#4a3b69';
        ctx.lineWidth = 2;
        ctx.stroke();

        ctx.fillStyle = '#3b2f55';
        ctx.beginPath();
        ctx.arc(cx, cy, 75, 0, Math.PI * 2);
        ctx.fill();

        // Water basin
        ctx.fillStyle = '#3498db';
        ctx.beginPath();
        ctx.arc(cx, cy, 38, 0, Math.PI * 2);
        ctx.fill();
        ctx.fillStyle = '#e0f7fa';
        ctx.beginPath();
        ctx.arc(cx, cy, 16, 0, Math.PI * 2);
        ctx.fill();
      } else if (poi.type === 'WORK_AREA') {
        // Lumber timber stacks & stone boulders
        ctx.fillStyle = '#d35400';
        for (let i = 0; i < 3; i++) {
          const lx = x + 40 + i * 115;
          const ly = y + 130;
          ctx.beginPath();
          ctx.roundRect(lx, ly, 90, 35, 8);
          ctx.fill();
          ctx.fillStyle = '#e67e22';
          ctx.beginPath();
          ctx.arc(lx + 80, ly + 17, 12, 0, Math.PI * 2);
          ctx.fill();
          ctx.fillStyle = '#d35400';
        }
        ctx.fillStyle = '#7f8c8d';
        for (let i = 0; i < 4; i++) {
          const sx = x + 50 + i * 85;
          const sy = y + 215;
          ctx.beginPath();
          ctx.arc(sx, sy, 22, 0, Math.PI * 2);
          ctx.fill();
        }
      } else if (poi.type === 'RECREATION_PARK') {
        // Zen pond & cherry blossom trees
        const lx = x + width * 0.45;
        const ly = y + height * 0.55;
        ctx.fillStyle = '#16a085';
        ctx.beginPath();
        ctx.arc(lx, ly, 68, 0, Math.PI * 2);
        ctx.fill();
        ctx.fillStyle = '#1abc9c';
        ctx.beginPath();
        ctx.arc(lx, ly, 58, 0, Math.PI * 2);
        ctx.fill();

        ctx.fillStyle = '#ff85a1';
        ctx.beginPath();
        ctx.arc(x + 50, y + 125, 26, 0, Math.PI * 2);
        ctx.arc(x + width - 60, y + 135, 24, 0, Math.PI * 2);
        ctx.arc(x + width - 55, y + height - 55, 22, 0, Math.PI * 2);
        ctx.fill();
      }

      // POI Title Header
      ctx.fillStyle = '#ffffff';
      ctx.font = 'bold 21px sans-serif';
      ctx.textAlign = 'left';
      ctx.fillText(`${poi.emoji} ${poi.name}`, x + 16, y + 36);

      ctx.fillStyle = '#cccccc';
      ctx.font = '14px sans-serif';
      ctx.fillText(poi.description, x + 16, y + 60);

      ctx.restore();
    });

    // 6. Draw Agents
    const pulse = 1.0 + Math.sin(Date.now() / 250) * 0.25;

    agents.forEach((agent) => {
      const isSelected = agent.id === selectedAgentId;
      const radius = 15;

      ctx.save();

      // Halo if selected
      if (isSelected) {
        ctx.strokeStyle = '#00e5ff';
        ctx.lineWidth = 3.5;
        ctx.beginPath();
        ctx.arc(agent.x, agent.y, (radius + 12) * pulse, 0, Math.PI * 2);
        ctx.stroke();

        ctx.fillStyle = 'rgba(0, 229, 255, 0.15)';
        ctx.fill();
      }

      // Drop shadow
      ctx.fillStyle = 'rgba(0, 0, 0, 0.45)';
      ctx.beginPath();
      ctx.arc(agent.x, agent.y + 3, radius + 2, 0, Math.PI * 2);
      ctx.fill();

      // Outer color ring
      ctx.fillStyle = agent.color;
      ctx.beginPath();
      ctx.arc(agent.x, agent.y, radius, 0, Math.PI * 2);
      ctx.fill();

      // Core dot
      const stateMeta = STATE_METADATA[agent.activeState];
      ctx.fillStyle = stateMeta ? stateMeta.color : '#ffffff';
      ctx.beginPath();
      ctx.arc(agent.x, agent.y, radius * 0.55, 0, Math.PI * 2);
      ctx.fill();

      // Direction facing line
      const noseX = agent.x + Math.cos(agent.facingAngle) * (radius * 1.35);
      const noseY = agent.y + Math.sin(agent.facingAngle) * (radius * 1.35);
      ctx.strokeStyle = '#ffffff';
      ctx.lineWidth = 2.5;
      ctx.beginPath();
      ctx.moveTo(agent.x, agent.y);
      ctx.lineTo(noseX, noseY);
      ctx.stroke();

      // Overhead Name & State Tag (MANDATORY REQUIREMENT)
      // Format: "Reno: Sleeping", "Ather: Gathering"
      const infoTag = `${agent.name}: ${stateMeta?.name || agent.activeState}`;
      ctx.font = 'bold 13px sans-serif';
      ctx.textAlign = 'center';
      const textWidth = ctx.measureText(infoTag).width;

      const pillPadH = 7;
      const pillPadV = 4;
      const textY = agent.y - radius - 10;
      const pillLeft = agent.x - textWidth / 2 - pillPadH;
      const pillRight = agent.x + textWidth / 2 + pillPadH;
      const pillTop = textY - 14 - pillPadV;
      const pillHeight = 18 + pillPadV * 2;

      ctx.fillStyle = 'rgba(17, 22, 34, 0.9)';
      ctx.beginPath();
      ctx.roundRect(pillLeft, pillTop, pillRight - pillLeft, pillHeight, 6);
      ctx.fill();

      ctx.fillStyle = isSelected ? '#00e5ff' : '#ffffff';
      ctx.fillText(infoTag, agent.x, textY);

      // Emote Bubble
      const emote = agent.activeEmote || (stateMeta ? stateMeta.emoji : null);
      if (emote) {
        const bubbleY = pillTop - 12;
        ctx.fillStyle = 'rgba(31, 40, 57, 0.95)';
        ctx.beginPath();
        ctx.arc(agent.x, bubbleY, 13, 0, Math.PI * 2);
        ctx.fill();

        ctx.strokeStyle = agent.color;
        ctx.lineWidth = 1.5;
        ctx.stroke();

        ctx.font = '14px sans-serif';
        ctx.fillText(emote, agent.x, bubbleY + 5);
      }

      ctx.restore();
    });

    // 7. Day / Night Environmental Lighting Overlay
    const h = world.timeOfDayHours;
    if (h >= 21 || h < 5) {
      ctx.fillStyle = 'rgba(7, 11, 25, 0.38)';
      ctx.fillRect(0, 0, world.width, world.height);
    } else if (h >= 5 && h < 7) {
      ctx.fillStyle = 'rgba(230, 126, 34, 0.12)';
      ctx.fillRect(0, 0, world.width, world.height);
    } else if (h >= 18 && h < 21) {
      ctx.fillStyle = 'rgba(155, 89, 182, 0.18)';
      ctx.fillRect(0, 0, world.width, world.height);
    }

    // 8. Atmospheric Weather Effects
    const weather = world.weather?.current || 'CLEAR';
    const now = Date.now();

    if (weather === 'CLEAR') {
      if (h >= 7 && h <= 18) {
        ctx.fillStyle = 'rgba(253, 224, 71, 0.03)';
        ctx.fillRect(0, 0, world.width, world.height);
      }
    } else if (weather === 'RAIN' || weather === 'THUNDERSTORM') {
      const isStorm = weather === 'THUNDERSTORM';
      ctx.strokeStyle = isStorm ? 'rgba(96, 165, 250, 0.55)' : 'rgba(96, 165, 250, 0.38)';
      ctx.lineWidth = isStorm ? 2 : 1.5;
      const count = isStorm ? 70 : 45;

      for (let i = 0; i < count; i++) {
        const seed = i * 197 + Math.floor(now / 14);
        const rx = (seed * 37) % world.width;
        const ry = (seed * 53) % world.height;
        ctx.beginPath();
        ctx.moveTo(rx, ry);
        ctx.lineTo(rx - 6, ry + 18);
        ctx.stroke();
      }

      if (isStorm && (now % 4500) < 160) {
        ctx.fillStyle = 'rgba(255, 255, 255, 0.28)';
        ctx.fillRect(0, 0, world.width, world.height);
      }
    } else if (weather === 'HEATWAVE') {
      ctx.fillStyle = 'rgba(245, 158, 11, 0.15)';
      ctx.fillRect(0, 0, world.width, world.height);
    } else if (weather === 'SNOW') {
      ctx.fillStyle = 'rgba(255, 255, 255, 0.8)';
      for (let i = 0; i < 50; i++) {
        const seed = i * 131 + Math.floor(now / 32);
        const rx = (seed * 41) % world.width;
        const ry = (seed * 29) % world.height;
        ctx.beginPath();
        ctx.arc(rx, ry, 2.2 + (i % 3), 0, Math.PI * 2);
        ctx.fill();
      }
    }

    ctx.restore();
  }, [world, agents, selectedAgentId]);

  const handlePointerDown = (e: React.PointerEvent<HTMLCanvasElement>) => {
    const canvas = canvasRef.current;
    if (!canvas) return;

    const rect = canvas.getBoundingClientRect();
    const tapX = e.clientX - rect.left;
    const tapY = e.clientY - rect.top;

    const { scale, offsetX, offsetY } = transformRef.current;
    const worldX = (tapX - offsetX) / scale;
    const worldY = (tapY - offsetY) / scale;

    // Check agent tap (generous touch radius)
    const touchRadius = 38;
    const tappedAgent = agents.find((a) => {
      const dist = Math.hypot(a.x - worldX, a.y - worldY);
      return dist <= touchRadius;
    });

    if (tappedAgent) {
      onSelectAgent(tappedAgent.id);
      return;
    }

    // Check POI tap
    const tappedPoi = world.pois.find((p) => {
      const b = p.bounds;
      return worldX >= b.x && worldX <= b.x + b.width && worldY >= b.y && worldY <= b.y + b.height;
    });

    if (tappedPoi) {
      onSelectPoi(tappedPoi);
      return;
    }

    onSelectAgent(undefined);
    onSelectPoi(undefined);
  };

  return (
    <canvas
      ref={canvasRef}
      onPointerDown={handlePointerDown}
      className="w-full h-full block cursor-pointer"
      data-testid="simulation_canvas"
    />
  );
};
