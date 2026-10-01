import { PRESET_NAMES, AGENT_PALETTES, DEFAULT_POIS } from './constants';
import { Agent, AgentState, Personality, SimulationWorld } from './types';
import { UtilityBrain } from './UtilityBrain';

export class SimulationEngine {
  createInitialAgents(world: SimulationWorld): Agent[] {
    const personalities: Personality[] = [
      'Balanced Citizen',
      'Industrious Builder',
      'Outgoing Conversationalist',
      'Epicurean Explorer',
      'Curious Thinker'
    ];

    return Array.from({ length: 20 }, (_, id) => {
      const name = PRESET_NAMES[id] || `Citizen-${id}`;
      const color = AGENT_PALETTES[id % AGENT_PALETTES.length];
      const personality = personalities[id % personalities.length];

      const angle = (id / 20) * 2 * Math.PI;
      const radius = 60 + (id % 5) * 25;
      const spawnX = world.width / 2 + Math.cos(angle) * radius;
      const spawnY = world.height / 2 + Math.sin(angle) * radius;

      return {
        id,
        name,
        color,
        personality,
        position: { x: spawnX, y: spawnY },
        velocity: { x: 0, y: 0 },
        targetPosition: null,
        facingAngle: 0,
        moveSpeed: 75,
        hunger: 60 + ((id * 9) % 35),
        energy: 70 + ((id * 11) % 25),
        social: 50 + ((id * 13) % 45),
        boredom: 20 + ((id * 7) % 30),
        skillLevel: id === 0 ? 2 : 1, // Reno starts with extra skill
        skillXp: 0,
        tasksCompleted: 0,
        resourcesGathered: 0,
        recentThoughts: ['Excited to settle into the micro-city!'],
        activeState: 'Wandering',
        stateDuration: 0,
        targetPoiId: null,
        targetAgentId: null,
        activeEmote: null,
        emoteDuration: 0
      };
    });
  }

  tick(world: SimulationWorld, agents: Agent[], dt: number): { world: SimulationWorld; agents: Agent[] } {
    const clampedDt = Math.max(0.001, Math.min(0.25, dt));

    // 1. Advance World Clock (1 day = ~7.2 real minutes at 1x)
    const hoursPerSecond = 1.0 / 18.0;
    let newHours = world.timeOfDayHours + clampedDt * hoursPerSecond;
    let newDay = world.simulationDay;
    if (newHours >= 24) {
      newHours -= 24;
      newDay += 1;
    }

    const updatedStockpile = { ...world.stockpile };
    const updatedNews = [...world.recentNews];

    // 2. Update agents
    const updatedAgents = agents.map((agent, index) => {
      let a = { ...agent };

      // Needs drain
      const hungerDrain = 1.2 * clampedDt;
      const energyDrain = a.activeState === 'Gathering' ? 3.5 * clampedDt : 1.0 * clampedDt;
      const socialDrain = 1.1 * clampedDt;
      const boredomGain = a.activeState === 'Gathering' ? 1.8 * clampedDt : 1.2 * clampedDt;

      a.hunger = Math.max(0, Math.min(100, a.hunger - hungerDrain));
      a.energy = Math.max(0, Math.min(100, a.energy - energyDrain));
      a.social = Math.max(0, Math.min(100, a.social - socialDrain));
      a.boredom = Math.max(0, Math.min(100, a.boredom + boredomGain));

      a.emoteDuration = Math.max(0, a.emoteDuration - clampedDt);
      if (a.emoteDuration <= 0) a.activeEmote = null;

      a.stateDuration += clampedDt;

      // FSM State transitions
      switch (a.activeState) {
        case 'Wandering': {
          if (a.hunger < 30 || a.energy < 25 || a.boredom > 70 || a.stateDuration > 5) {
            Object.assign(a, UtilityBrain.decideNextState(a, world, agents));
          }
          break;
        }

        case 'Seeking Food': {
          const foodPoi = world.pois.find(p => p.id === (a.targetPoiId || 'poi_food'));
          if (!a.targetPosition && foodPoi) {
            a.targetPosition = UtilityBrain.getPoiInteriorTarget(foodPoi, a.id);
          }
          if (a.targetPosition && UtilityBrain.dist(a.position, a.targetPosition) < 40) {
            a.activeState = 'Eating';
            a.stateDuration = 0;
            a.activeEmote = '🍎';
            a.emoteDuration = 4.0;
          }
          break;
        }

        case 'Eating': {
          a.hunger = Math.min(100, a.hunger + 25 * clampedDt);
          a.energy = Math.min(100, a.energy + 5 * clampedDt);
          a.activeEmote = '🍎';

          if (a.stateDuration >= 3.5 || a.hunger >= 98) {
            a.recentThoughts = ['Enjoyed fresh delicious orchard berries.', ...a.recentThoughts.slice(0, 7)];
            Object.assign(a, UtilityBrain.decideNextState(a, world, agents));
          }
          break;
        }

        case 'Seeking Rest': {
          const restPoi = world.pois.find(p => p.id === (a.targetPoiId || 'poi_rest'));
          if (!a.targetPosition && restPoi) {
            a.targetPosition = UtilityBrain.getPoiInteriorTarget(restPoi, a.id);
          }
          if (a.targetPosition && UtilityBrain.dist(a.position, a.targetPosition) < 40) {
            a.activeState = 'Sleeping';
            a.stateDuration = 0;
            a.activeEmote = '💤';
            a.emoteDuration = 6.0;
          }
          break;
        }

        case 'Sleeping': {
          a.energy = Math.min(100, a.energy + 20 * clampedDt);
          a.boredom = Math.max(0, a.boredom - 10 * clampedDt);
          a.activeEmote = '💤';

          if (a.stateDuration >= 5.0 || a.energy >= 98) {
            a.recentThoughts = ['Woke up feeling deeply rested and energized.', ...a.recentThoughts.slice(0, 7)];
            Object.assign(a, UtilityBrain.decideNextState(a, world, agents));
          }
          break;
        }

        case 'Seeking Work': {
          const workPoi = world.pois.find(p => p.id === (a.targetPoiId || 'poi_work'));
          if (!a.targetPosition && workPoi) {
            a.targetPosition = UtilityBrain.getPoiInteriorTarget(workPoi, a.id);
          }
          if (a.targetPosition && UtilityBrain.dist(a.position, a.targetPosition) < 40) {
            a.activeState = 'Gathering';
            a.stateDuration = 0;
            a.activeEmote = '⛏️';
            a.emoteDuration = 5.0;
          }
          break;
        }

        case 'Gathering': {
          const efficiencyMultiplier = 1.0 + (a.skillLevel - 1) * 0.25;
          a.activeEmote = '⛏️';

          if (a.hunger < 20 || a.energy < 15) {
            Object.assign(a, UtilityBrain.decideNextState(a, world, agents));
          } else if (a.stateDuration >= 4.0 / efficiencyMultiplier) {
            a.tasksCompleted += 1;
            const yieldAmt = Math.floor(1 + a.skillLevel * 0.75);
            a.resourcesGathered += yieldAmt;

            updatedStockpile.timberStock += yieldAmt;
            updatedStockpile.mineralStock += Math.max(1, Math.floor(yieldAmt / 2));
            updatedStockpile.totalWorkDone += 1;

            const xpGain = 25 * efficiencyMultiplier;
            a.skillXp += xpGain;

            const xpNeeded = a.skillLevel * 100;
            if (a.skillXp >= xpNeeded) {
              a.skillXp -= xpNeeded;
              a.skillLevel += 1;
              a.recentThoughts = [`⭐ Promoted! Advanced to Gathering Skill Lv. ${a.skillLevel}!`, ...a.recentThoughts.slice(0, 7)];
              a.activeEmote = '⭐';
              a.emoteDuration = 3.5;
              updatedNews.unshift(`${a.name} reached Gathering Skill Level ${a.skillLevel}!`);
              if (updatedNews.length > 8) updatedNews.pop();
            } else {
              a.recentThoughts = [`Harvested ${yieldAmt} resources at the workshop (Lv. ${a.skillLevel}).`, ...a.recentThoughts.slice(0, 7)];
            }

            Object.assign(a, UtilityBrain.decideNextState(a, world, agents));
          }
          break;
        }

        case 'Seeking Social': {
          const partner = a.targetAgentId !== null ? agents.find(ag => ag.id === a.targetAgentId) : null;
          const dest = partner ? partner.position : a.targetPosition;

          if (dest && UtilityBrain.dist(a.position, dest) < 55) {
            a.activeState = 'Socializing';
            a.stateDuration = 0;
            a.activeEmote = '💬';
            a.emoteDuration = 3.5;
            updatedStockpile.socialInteractionsCount += 1;
          } else if (a.stateDuration > 8.0) {
            Object.assign(a, UtilityBrain.decideNextState(a, world, agents));
          }
          break;
        }

        case 'Socializing': {
          a.social = Math.min(100, a.social + 30 * clampedDt);
          a.boredom = Math.max(0, a.boredom - 25 * clampedDt);
          a.activeEmote = '💬';

          if (a.stateDuration >= 3.0 || a.social >= 95) {
            const pName = a.targetAgentId !== null ? agents.find(ag => ag.id === a.targetAgentId)?.name : 'citizens';
            a.recentThoughts = [`Had an inspiring conversation with ${pName}.`, ...a.recentThoughts.slice(0, 7)];
            Object.assign(a, UtilityBrain.decideNextState(a, world, agents));
          }
          break;
        }

        case 'Entertaining': {
          a.boredom = Math.max(0, a.boredom - 30 * clampedDt);
          a.activeEmote = '🌸';

          if (a.stateDuration >= 3.5 || a.boredom <= 10) {
            a.recentThoughts = ['Relaxed by the peaceful lake and cherry blossoms.', ...a.recentThoughts.slice(0, 7)];
            Object.assign(a, UtilityBrain.decideNextState(a, world, agents));
          }
          break;
        }
      }

      // Smooth continuous movement
      const stationary = ['Sleeping', 'Eating', 'Gathering', 'Socializing'].includes(a.activeState);
      if (!stationary && a.targetPosition) {
        const dx = a.targetPosition.x - a.position.x;
        const dy = a.targetPosition.y - a.position.y;
        const dist = Math.sqrt(dx * dx + dy * dy);

        if (dist > 6) {
          const nx = dx / dist;
          const ny = dy / dist;
          const speed = a.moveSpeed * (a.activeState === 'Seeking Food' && a.hunger < 20 ? 1.25 : 1.0);
          a.velocity = { x: nx * speed, y: ny * speed };
          a.position = {
            x: a.position.x + a.velocity.x * clampedDt,
            y: a.position.y + a.velocity.y * clampedDt
          };
          a.facingAngle = Math.atan2(ny, nx);
        }
      }

      // Agent-Agent Soft Separation
      let sepX = 0;
      let sepY = 0;
      for (let j = 0; j < agents.length; j++) {
        if (j !== index) {
          const other = agents[j];
          const odx = a.position.x - other.position.x;
          const ody = a.position.y - other.position.y;
          const odist = Math.sqrt(odx * odx + ody * ody);
          if (odist > 0.1 && odist < 32) {
            const force = (32 - odist) / 32;
            sepX += (odx / odist) * force * 35 * clampedDt;
            sepY += (ody / odist) * force * 35 * clampedDt;
          }
        }
      }
      a.position.x += sepX;
      a.position.y += sepY;

      // Strict boundaries
      const margin = 26;
      a.position.x = Math.max(margin, Math.min(world.width - margin, a.position.x));
      a.position.y = Math.max(margin + 50, Math.min(world.height - margin - 30, a.position.y));

      return a;
    });

    const updatedWorld: SimulationWorld = {
      ...world,
      timeOfDayHours: newHours,
      simulationDay: newDay,
      stockpile: updatedStockpile,
      recentNews: updatedNews
    };

    return { world: updatedWorld, agents: updatedAgents };
  }
}
