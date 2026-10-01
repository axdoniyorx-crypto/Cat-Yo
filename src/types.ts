export type AgentState =
  | 'WANDERING'
  | 'SEEKING_FOOD'
  | 'EATING'
  | 'SEEKING_REST'
  | 'SLEEPING'
  | 'SEEKING_WORK'
  | 'GATHERING'
  | 'SEEKING_SOCIAL'
  | 'SOCIALIZING'
  | 'ENTERTAINING';

export interface StateInfo {
  name: string;
  emoji: string;
  color: string;
}

export const STATE_METADATA: Record<AgentState, StateInfo> = {
  WANDERING: { name: 'Wandering', emoji: '🚶', color: '#94a3b8' },
  SEEKING_FOOD: { name: 'Seeking Food', emoji: '🍎', color: '#22c55e' },
  EATING: { name: 'Eating', emoji: '🍴', color: '#4ade80' },
  SEEKING_REST: { name: 'Seeking Rest', emoji: '😴', color: '#3b82f6' },
  SLEEPING: { name: 'Sleeping', emoji: '💤', color: '#60a5fa' },
  SEEKING_WORK: { name: 'Seeking Work', emoji: '⚒️', color: '#f59e0b' },
  GATHERING: { name: 'Gathering', emoji: '⛏️', color: '#fbbf24' },
  SEEKING_SOCIAL: { name: 'Seeking Social', emoji: '💬', color: '#ec4899' },
  SOCIALIZING: { name: 'Socializing', emoji: '🗣️', color: '#f472b6' },
  ENTERTAINING: { name: 'Entertaining', emoji: '🌸', color: '#a855f7' },
};

export type WeatherType =
  | 'CLEAR'
  | 'RAIN'
  | 'THUNDERSTORM'
  | 'HEATWAVE'
  | 'SNOW';

export interface WeatherInfo {
  displayName: string;
  emoji: string;
  speedMultiplier: number;
  energyDrainMultiplier: number;
  description: string;
}

export const WEATHER_METADATA: Record<WeatherType, WeatherInfo> = {
  CLEAR: {
    displayName: 'Clear & Sunny',
    emoji: '☀️',
    speedMultiplier: 1.0,
    energyDrainMultiplier: 1.0,
    description: 'Optimal conditions. Normal travel speed and stamina.'
  },
  RAIN: {
    displayName: 'Rainfall',
    emoji: '🌧️',
    speedMultiplier: 0.80,
    energyDrainMultiplier: 1.25,
    description: 'Slick streets slow citizens; cold rain increases stamina drain.'
  },
  THUNDERSTORM: {
    displayName: 'Thunderstorm',
    emoji: '⛈️',
    speedMultiplier: 0.65,
    energyDrainMultiplier: 1.50,
    description: 'Heavy gales and thunder heavily impair travel and exhaust citizens.'
  },
  HEATWAVE: {
    displayName: 'Heatwave',
    emoji: '☀️🔥',
    speedMultiplier: 0.85,
    energyDrainMultiplier: 1.40,
    description: 'Scorching sun rapidly drains citizens stamina.'
  },
  SNOW: {
    displayName: 'Gentle Snow',
    emoji: '❄️',
    speedMultiplier: 0.75,
    energyDrainMultiplier: 1.30,
    description: 'Icy flurries cool the city, slowing foot movement.'
  }
};

export interface WeatherState {
  current: WeatherType;
  durationHoursRemaining: number;
}

export type POIType =
  | 'FOOD_SOURCE'
  | 'REST_AREA'
  | 'WORK_AREA'
  | 'TOWN_PLAZA'
  | 'RECREATION_PARK';

export interface PointOfInterest {
  id: string;
  name: string;
  type: POIType;
  bounds: { x: number; y: number; width: number; height: number };
  color: string;
  accentColor: string;
  emoji: string;
  description: string;
  resourceType?: string;
}

export interface Agent {
  id: number;
  name: string;
  color: string;
  personality: string;
  // Position in world space (1000 x 1350)
  x: number;
  y: number;
  vx: number;
  vy: number;
  targetX?: number;
  targetY?: number;
  facingAngle: number;
  moveSpeed: number;

  // Dynamic Needs (0 - 100)
  hunger: number;
  energy: number;
  social: number;
  boredom: number;

  // Progression & Skill
  skillLevel: number;
  skillXp: number;
  tasksCompleted: number;
  resourcesGathered: number;
  recentThoughts: string[];

  // State Machine
  activeState: AgentState;
  stateDuration: number;
  targetPoiId?: string;
  targetAgentId?: number;
  activeEmote?: string;
  emoteDuration: number;
}

export interface CityStockpile {
  foodStock: number;
  timberStock: number;
  mineralStock: number;
  totalWorkDone: number;
  socialInteractionsCount: number;
}

export interface SimulationWorld {
  width: number;
  height: number;
  pois: PointOfInterest[];
  stockpile: CityStockpile;
  timeOfDayHours: number;
  simulationDay: number;
  weather: WeatherState;
  recentNews: string[];
}
