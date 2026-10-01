export type AgentState =
  | 'Wandering'
  | 'Seeking Food'
  | 'Eating'
  | 'Seeking Rest'
  | 'Sleeping'
  | 'Seeking Work'
  | 'Gathering'
  | 'Seeking Social'
  | 'Socializing'
  | 'Entertaining';

export const STATE_ICONS: Record<AgentState, string> = {
  'Wandering': '🚶',
  'Seeking Food': '🍎',
  'Eating': '🍴',
  'Seeking Rest': '😴',
  'Sleeping': '💤',
  'Seeking Work': '⚒️',
  'Gathering': '⛏️',
  'Seeking Social': '💬',
  'Socializing': '🗣️',
  'Entertaining': '🎉'
};

export type Personality =
  | 'Balanced Citizen'
  | 'Industrious Builder'
  | 'Outgoing Conversationalist'
  | 'Epicurean Explorer'
  | 'Curious Thinker';

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

export interface Offset {
  x: number;
  y: number;
}

export interface Agent {
  id: number;
  name: string;
  color: string;
  personality: Personality;
  position: Offset;
  velocity: Offset;
  targetPosition: Offset | null;
  facingAngle: number;
  moveSpeed: number;

  // Dynamic depleting needs (0 - 100)
  hunger: number;
  energy: number;
  social: number;
  boredom: number; // 0 = stimulated, 100 = critically bored

  // Progression & Learning
  skillLevel: number;
  skillXp: number;
  tasksCompleted: number;
  resourcesGathered: number;
  recentThoughts: string[];

  // FSM Active State
  activeState: AgentState;
  stateDuration: number;
  targetPoiId: string | null;
  targetAgentId: number | null;
  activeEmote: string | null;
  emoteDuration: number;
}

export type POIType =
  | 'FOOD_SOURCE'
  | 'REST_AREA'
  | 'WORK_AREA'
  | 'TOWN_PLAZA'
  | 'RECREATION_PARK';

export interface Rect {
  left: number;
  top: number;
  right: number;
  bottom: number;
}

export interface PointOfInterest {
  id: string;
  name: string;
  type: POIType;
  bounds: Rect;
  color: string;
  accentColor: string;
  emoji: string;
  description: string;
  resourceType?: string;
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
