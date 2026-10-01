import { Agent, PointOfInterest, SimulationWorld, WeatherType, WEATHER_METADATA } from '../types';
import { UtilityBrain } from '../ai/UtilityBrain';

export const PRESET_NAMES = [
  'Reno',     // Required Agent 1
  'Ather',    // Required Agent 2
  'Liora',    // Required Agent 3
  'Kael',
  'Vesper',
  'Dax',
  'Maya',
  'Zephyr',
  'Lyra',
  'Orion',
  'Silas',
  'Nova',
  'Finn',
  'Cassian',
  'Rowan',
  'Tessa',
  'Bram',
  'Mira',
  'Jace',
  'Elora'
];

export const AGENT_PALETTES = [
  '#38ef7d', '#00c9ff', '#ff416c', '#ffa07a', '#9d50bb',
  '#ffd200', '#4facfe', '#ff6a00', '#20bf6b', '#eb3b5a',
  '#0fb9b1', '#45aaf2', '#fa8231', '#a55eea', '#26de81',
  '#fc5c65', '#2bcbba', '#fd9644', '#8854d0', '#3867d6'
];

export const PERSONALITIES = [
  'Balanced Citizen',
  'Hardworker',
  'Socialite',
  'Bon Vivant',
  'Scholar'
];

export class SimulationEngine {
  static createDefaultWorld(): SimulationWorld {
    const pois: PointOfInterest[] = [
      {
        id: 'poi_food',
        name: 'Berry Orchard & Farm',
        type: 'FOOD_SOURCE',
        bounds: { x: 40, y: 110, width: 400, height: 290 },
        color: '#1e3a2b',
        accentColor: '#2ecc71',
        emoji: '🍏',
        description: 'Nutritious orchard and hydroponic berry garden. Satisfies hunger.',
        resourceType: 'Berries'
      },
      {
        id: 'poi_rest',
        name: 'Cozy Cabins & Dorms',
        type: 'REST_AREA',
        bounds: { x: 560, y: 110, width: 400, height: 290 },
        color: '#1a2744',
        accentColor: '#4a90e2',
        emoji: '🛌',
        description: 'Warm sleeping pods and plush beds. Restores stamina and energy.'
      },
      {
        id: 'poi_plaza',
        name: 'Central Plaza & Fountain',
        type: 'TOWN_PLAZA',
        bounds: { x: 310, y: 500, width: 380, height: 290 },
        color: '#32274a',
        accentColor: '#9b59b6',
        emoji: '⛲',
        description: 'Vibrant gathering plaza with marble fountain. Social hub of the city.'
      },
      {
        id: 'poi_work',
        name: 'Workshop & Quarry',
        type: 'WORK_AREA',
        bounds: { x: 40, y: 890, width: 400, height: 290 },
        color: '#3d271d',
        accentColor: '#e67e22',
        emoji: '⚒️',
        description: 'Timber yard and artisan crafting workshop. Trains gathering skills and builds stockpile.',
        resourceType: 'Timber & Minerals'
      },
      {
        id: 'poi_park',
        name: 'Zen Garden & Lake',
        type: 'RECREATION_PARK',
        bounds: { x: 560, y: 890, width: 400, height: 290 },
        color: '#19323a',
        accentColor: '#1abc9c',
        emoji: '🌸',
        description: 'Peaceful walking paths, cherry blossom trees, and koi lake. Alleviates boredom.'
      }
    ];

    return {
      width: 1000,
      height: 1350,
      pois,
      stockpile: {
        foodStock: 120,
        timberStock: 45,
        mineralStock: 30,
        totalWorkDone: 0,
        socialInteractionsCount: 0
      },
      timeOfDayHours: 8.0,
      simulationDay: 1,
      weather: {
        current: 'CLEAR',
        durationHoursRemaining: 3.5
      },
      recentNews: ['MicroLife simulation initialized with 20 autonomous citizens.']
    };
  }

  static createInitialAgents(world: SimulationWorld): Agent[] {
    return Array.from({ length: 20 }, (_, id) => {
      const name = PRESET_NAMES[id] || `Citizen-${id}`;
      const color = AGENT_PALETTES[id % AGENT_PALETTES.length];
      const personality = PERSONALITIES[id % PERSONALITIES.length];

      const angle = (id / 20) * 2 * Math.PI;
      const radius = 60 + (id % 5) * 25;
      const spawnX = world.width / 2 + Math.cos(angle) * radius;
      const spawnY = world.height / 2 + Math.sin(angle) * radius;

      return {
        id,
        name,
        color,
        personality,
        x: spawnX,
        y: spawnY,
        vx: 0,
        vy: 0,
        facingAngle: 0,
        moveSpeed: 75,
        hunger: 60 + ((id * 9) % 35),
        energy: 70 + ((id * 11) % 25),
        social: 50 + ((id * 13) % 45),
        boredom: 20 + ((id * 7) % 30),
        skillLevel: id === 0 ? 2 : 1, // Reno starts with bonus experience
        skillXp: 0,
        tasksCompleted: 0,
        resourcesGathered: 0,
        recentThoughts: ['Excited to settle into the micro-city!'],
        activeState: 'WANDERING',
        stateDuration: 0,
        emoteDuration: 0
      };
    });
  }

  static tick(
    world: SimulationWorld,
    agents: Agent[],
    dt: number
  ): { world: SimulationWorld; agents: Agent[] } {
    const clampedDt = Math.min(0.25, Math.max(0.001, dt));

    // 1. Advance Clock
    const hoursPerSecond = 1.0 / 18.0;
    const elapsedHours = clampedDt * hoursPerSecond;
    let newHours = world.timeOfDayHours + elapsedHours;
    let newDay = world.simulationDay;
    if (newHours >= 24) {
      newHours -= 24;
      newDay += 1;
    }

    // Dynamic Weather Update
    let currentWeather: WeatherType = world.weather?.current || 'CLEAR';
    let remainingWeatherHours = (world.weather?.durationHoursRemaining ?? 3.5) - elapsedHours;
    const updatedNews = [...world.recentNews];

    if (remainingWeatherHours <= 0) {
      const roll = Math.floor(Math.random() * 100);
      currentWeather =
        roll < 35 ? 'CLEAR' :
        roll < 58 ? 'RAIN' :
        roll < 74 ? 'HEATWAVE' :
        roll < 88 ? 'THUNDERSTORM' : 'SNOW';
      remainingWeatherHours = 2.5 + (roll % 25) / 10;

      const meta = WEATHER_METADATA[currentWeather];
      const speedPct = Math.round(meta.speedMultiplier * 100);
      const staminaPct = Math.round(meta.energyDrainMultiplier * 100);
      updatedNews.unshift(`Weather Shift: ${meta.displayName} ${meta.emoji} has set in! (Speed: ${speedPct}%, Stamina Drain: ${staminaPct}%)`);
      if (updatedNews.length > 8) updatedNews.pop();
    }

    const weatherMeta = WEATHER_METADATA[currentWeather];
    let updatedStockpile = { ...world.stockpile };

    // 2. Update each agent
    const updatedAgents = agents.map((agent, index) => {
      let a = { ...agent };

      // Drain needs (scaled by weather conditions)
      const hungerDrain = 1.2 * clampedDt;
      const baseEnergyDrain = a.activeState === 'GATHERING' ? 3.5 * clampedDt : 1.0 * clampedDt;
      const energyDrain = baseEnergyDrain * weatherMeta.energyDrainMultiplier;
      const socialDrain = 1.1 * clampedDt;
      const boredomGain = a.activeState === 'GATHERING' ? 1.8 * clampedDt : 1.2 * clampedDt;

      a.hunger = Math.max(0, Math.min(100, a.hunger - hungerDrain));
      a.energy = Math.max(0, Math.min(100, a.energy - energyDrain));
      a.social = Math.max(0, Math.min(100, a.social - socialDrain));
      a.boredom = Math.max(0, Math.min(100, a.boredom + boredomGain));

      a.stateDuration += clampedDt;
      a.emoteDuration = Math.max(0, a.emoteDuration - clampedDt);
      if (a.emoteDuration <= 0) {
        a.activeEmote = undefined;
      }

      // State execution
      const efficiencyMultiplier = 1.0 + (a.skillLevel - 1) * 0.25;

      switch (a.activeState) {
        case 'WANDERING': {
          if (a.hunger < 30 || a.energy < 25 || a.boredom > 70 || a.stateDuration > 5.0) {
            Object.assign(a, UtilityBrain.decideNextState(a, world, agents));
          }
          break;
        }

        case 'SEEKING_FOOD': {
          const poi = world.pois.find((p) => p.id === 'poi_food');
          if (a.targetX === undefined && poi) {
            a.targetX = poi.bounds.x + poi.bounds.width / 2;
            a.targetY = poi.bounds.y + poi.bounds.height / 2;
          }
          if (a.targetX !== undefined && a.targetY !== undefined) {
            if (Math.hypot(a.x - a.targetX, a.y - a.targetY) < 40) {
              a.activeState = 'EATING';
              a.stateDuration = 0;
              a.activeEmote = '🍎';
              a.emoteDuration = 4.0;
            }
          }
          break;
        }

        case 'EATING': {
          a.hunger = Math.min(100, a.hunger + 25 * clampedDt);
          a.energy = Math.min(100, a.energy + 5 * clampedDt);
          a.activeEmote = '🍎';

          if (a.stateDuration >= 3.5 || a.hunger >= 98) {
            const thoughts = ['Enjoyed fresh orchard berries.', ...a.recentThoughts].slice(0, 8);
            a.recentThoughts = thoughts;
            Object.assign(a, UtilityBrain.decideNextState(a, world, agents));
          }
          break;
        }

        case 'SEEKING_REST': {
          const poi = world.pois.find((p) => p.id === 'poi_rest');
          if (a.targetX === undefined && poi) {
            a.targetX = poi.bounds.x + poi.bounds.width / 2;
            a.targetY = poi.bounds.y + poi.bounds.height / 2;
          }
          if (a.targetX !== undefined && a.targetY !== undefined) {
            if (Math.hypot(a.x - a.targetX, a.y - a.targetY) < 40) {
              a.activeState = 'SLEEPING';
              a.stateDuration = 0;
              a.activeEmote = '💤';
              a.emoteDuration = 6.0;
            }
          }
          break;
        }

        case 'SLEEPING': {
          a.energy = Math.min(100, a.energy + 20 * clampedDt);
          a.boredom = Math.max(0, a.boredom - 10 * clampedDt);
          a.activeEmote = '💤';

          if (a.stateDuration >= 5.0 || a.energy >= 98) {
            const thoughts = ['Woke up feeling deeply rested and energized.', ...a.recentThoughts].slice(0, 8);
            a.recentThoughts = thoughts;
            Object.assign(a, UtilityBrain.decideNextState(a, world, agents));
          }
          break;
        }

        case 'SEEKING_WORK': {
          const poi = world.pois.find((p) => p.id === 'poi_work');
          if (a.targetX === undefined && poi) {
            a.targetX = poi.bounds.x + poi.bounds.width / 2;
            a.targetY = poi.bounds.y + poi.bounds.height / 2;
          }
          if (a.targetX !== undefined && a.targetY !== undefined) {
            if (Math.hypot(a.x - a.targetX, a.y - a.targetY) < 40) {
              a.activeState = 'GATHERING';
              a.stateDuration = 0;
              a.activeEmote = '⛏️';
              a.emoteDuration = 5.0;
            }
          }
          break;
        }

        case 'GATHERING': {
          a.activeEmote = '⛏️';

          if (a.hunger < 20 || a.energy < 15) {
            Object.assign(a, UtilityBrain.decideNextState(a, world, agents));
          } else if (a.stateDuration >= 4.0 / efficiencyMultiplier) {
            a.tasksCompleted += 1;
            const yieldAmount = Math.max(1, Math.floor(1 + a.skillLevel * 0.75));
            a.resourcesGathered += yieldAmount;

            updatedStockpile.timberStock += yieldAmount;
            updatedStockpile.mineralStock += Math.max(1, Math.floor(yieldAmount / 2));
            updatedStockpile.totalWorkDone += 1;

            // XP gain
            const xpGain = 25 * efficiencyMultiplier;
            a.skillXp += xpGain;
            const xpForNextLevel = a.skillLevel * 100;

            if (a.skillXp >= xpForNextLevel) {
              a.skillXp -= xpForNextLevel;
              a.skillLevel += 1;
              a.recentThoughts = [
                `⭐ Promoted! Advanced to Gathering Skill Lv. ${a.skillLevel}!`,
                ...a.recentThoughts
              ].slice(0, 8);
              a.activeEmote = '⭐';
              a.emoteDuration = 3.5;

              updatedNews.unshift(`${a.name} mastered Gathering Skill Level ${a.skillLevel}!`);
              if (updatedNews.length > 6) updatedNews.pop();
            } else {
              a.recentThoughts = [
                `Harvested ${yieldAmount} resources at the workshop (Lv. ${a.skillLevel}).`,
                ...a.recentThoughts
              ].slice(0, 8);
            }

            Object.assign(a, UtilityBrain.decideNextState(a, world, agents));
          }
          break;
        }

        case 'SEEKING_SOCIAL': {
          const partner = a.targetAgentId !== undefined ? agents.find((p) => p.id === a.targetAgentId) : null;
          const destX = partner ? partner.x : a.targetX;
          const destY = partner ? partner.y : a.targetY;

          if (destX !== undefined && destY !== undefined) {
            if (Math.hypot(a.x - destX, a.y - destY) < 55) {
              a.activeState = 'SOCIALIZING';
              a.stateDuration = 0;
              a.activeEmote = '💬';
              a.emoteDuration = 3.5;
              updatedStockpile.socialInteractionsCount += 1;
            } else if (a.stateDuration > 8.0) {
              Object.assign(a, UtilityBrain.decideNextState(a, world, agents));
            }
          }
          break;
        }

        case 'SOCIALIZING': {
          a.social = Math.min(100, a.social + 30 * clampedDt);
          a.boredom = Math.max(0, a.boredom - 25 * clampedDt);
          a.activeEmote = '💬';

          if (a.stateDuration >= 3.0 || a.social >= 95) {
            const partnerName = a.targetAgentId !== undefined ? agents.find((p) => p.id === a.targetAgentId)?.name || 'citizens' : 'citizens';
            a.recentThoughts = [`Had an inspiring conversation with ${partnerName}.`, ...a.recentThoughts].slice(0, 8);
            Object.assign(a, UtilityBrain.decideNextState(a, world, agents));
          }
          break;
        }

        case 'ENTERTAINING': {
          a.boredom = Math.max(0, a.boredom - 30 * clampedDt);
          a.activeEmote = '🌸';

          if (a.stateDuration >= 3.5 || a.boredom <= 10) {
            a.recentThoughts = ['Relaxed by the peaceful lake and cherry blossoms.', ...a.recentThoughts].slice(0, 8);
            Object.assign(a, UtilityBrain.decideNextState(a, world, agents));
          }
          break;
        }
      }

      // Movement (speed scaled by weather)
      const isStationary = ['SLEEPING', 'EATING', 'GATHERING', 'SOCIALIZING'].includes(a.activeState);
      if (!isStationary && a.targetX !== undefined && a.targetY !== undefined) {
        const dx = a.targetX - a.x;
        const dy = a.targetY - a.y;
        const dist = Math.hypot(dx, dy);

        if (dist > 6) {
          const nx = dx / dist;
          const ny = dy / dist;
          const urgencySpeed = a.activeState === 'SEEKING_FOOD' && a.hunger < 20 ? 1.25 : 1.0;
          const speed = a.moveSpeed * weatherMeta.speedMultiplier * urgencySpeed;
          a.vx = nx * speed;
          a.vy = ny * speed;
          a.x += a.vx * clampedDt;
          a.y += a.vy * clampedDt;
          a.facingAngle = Math.atan2(ny, nx);
        }
      }

      // Weather reaction thought
      if (currentWeather !== 'CLEAR' && (a.id + Math.floor(newHours * 10)) % 17 === 0 && a.stateDuration < clampedDt * 2) {
        const weatherThought =
          currentWeather === 'RAIN' ? 'The rain makes the cobblestones slick.' :
          currentWeather === 'THUNDERSTORM' ? 'The thunderstorm is ferocious! Hard to move.' :
          currentWeather === 'HEATWAVE' ? 'The heatwave is scorching and exhausting.' :
          currentWeather === 'SNOW' ? 'Snow flurries are blanketing the avenues.' : null;
        if (weatherThought && (!a.recentThoughts.length || a.recentThoughts[0] !== weatherThought)) {
          a.recentThoughts = [weatherThought, ...a.recentThoughts].slice(0, 8);
        }
      }

      // Separation force
      let sepX = 0;
      let sepY = 0;
      for (let j = 0; j < agents.length; j++) {
        if (j !== index) {
          const other = agents[j];
          const odx = a.x - other.x;
          const ody = a.y - other.y;
          const odist = Math.hypot(odx, ody);
          if (odist > 0.1 && odist < 32) {
            const force = (32 - odist) / 32;
            sepX += (odx / odist) * force * 35 * clampedDt;
            sepY += (ody / odist) * force * 35 * clampedDt;
          }
        }
      }
      a.x += sepX;
      a.y += sepY;

      // Bound clamping
      const margin = 28;
      a.x = Math.max(margin, Math.min(world.width - margin, a.x));
      a.y = Math.max(margin + 50, Math.min(world.height - margin - 30, a.y));

      return a;
    });

    return {
      world: {
        ...world,
        timeOfDayHours: newHours,
        simulationDay: newDay,
        weather: {
          current: currentWeather,
          durationHoursRemaining: remainingWeatherHours
        },
        stockpile: updatedStockpile,
        recentNews: updatedNews
      },
      agents: updatedAgents
    };
  }
}
